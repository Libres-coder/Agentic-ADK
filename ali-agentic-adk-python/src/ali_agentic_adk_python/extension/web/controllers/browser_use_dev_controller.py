# Copyright (C) 2025 AIDC-AI
# This project incorporates components from the Open Source Software below.
# The original copyright notices and the licenses under which we received such components are set forth below for informational purposes.
#
# Open Source Software Licensed under the MIT License:
# --------------------------------------------------------------------
# 1. vscode-extension-updater-gitlab 3.0.1 https://www.npmjs.com/package/vscode-extension-updater-gitlab
# Copyright (c) Microsoft Corporation. All rights reserved.
# Copyright (c) 2015 David Owens II
# Copyright (c) Microsoft Corporation.
# Terms of the MIT:
# --------------------------------------------------------------------
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


import asyncio
import json
import logging
import uuid
from typing import List, Optional, Dict, Any, AsyncGenerator
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor

from fastapi import APIRouter, HTTPException
from fastapi.responses import StreamingResponse
from fastapi import Query

#from src.ali_agentic_adk_python.extension.web.configuration.browser_runner_service import BrowserRunnerService
from ..dto.models import (
    ResultDO, ComputerDetailDTO, MobileDetailDTO, 
    BrowserAgentRunRequest, BrowserAgentSessionRequest,
)
from ..dto.browser_use_properties import BrowserUseProperties
from src.ali_agentic_adk_python.extension.web.configuration.browser_agent_register import browser_agent_register
from google.adk.sessions import BaseSessionService, InMemorySessionService, Session
from google.adk.agents import RunConfig, BaseAgent
from google.adk.agents.run_config import StreamingMode
from google.adk.events import Event
from src.ali_agentic_adk_python.extension.web.service.ecd import EcdService


logger = logging.getLogger(__name__)
image_url = {}

class BrowserUseDevController:

    def __init__(self, properties: Optional[BrowserUseProperties] = None):
        """
        初始化
        
        Args:
            properties: ADK浏览器使用配置
        """
        self.properties = properties or BrowserUseProperties()
        self.router = APIRouter()

        # 线程池执行器，用于SSE处理
        self.sse_executor = ThreadPoolExecutor(max_workers=10, thread_name_prefix="sse-")


        self._registered_apps: Dict[str, BaseAgent]

        self._sessions: Dict[str, Session] = {}
        logger.info(f"properties: {self.properties}")
        self.client = EcdService(self.properties)
        self._session_service = InMemorySessionService()
        self._sessions: Dict[str, BaseSessionService] = {}
        from ..configuration.browser_runner_service import BrowserRunnerService

        self.browser_runner_service = BrowserRunnerService(None, self._session_service)
        self._setup_routes()

    def _setup_routes(self):
        """设置路由"""
        
        @self.router.get("/getComputerDetail", response_model=ResultDO[ComputerDetailDTO])
        async def get_computer_detail():
            """获取桌面设备详情"""
            try:
                detail = ComputerDetailDTO(
                    desktopId=self.properties.computer_resource_id,
                    regionId=self.properties.region_id or "cn-hangzhou",
                    loginToken=self._get_login_token()
                )
                return ResultDO.success_result(detail)
            except Exception as e:
                logger.error(f"获取桌面设备详情失败: {e}")
                return ResultDO.error_result(f"获取桌面设备详情失败: {str(e)}")
        
        @self.router.get("/getMobileDetail", response_model=ResultDO[MobileDetailDTO])
        async def get_mobile_detail():
            """获取移动设备详情"""
            try:
                detail = MobileDetailDTO(
                    desktop_id=self.properties.mobile_resource_id,
                    auth_code=self._get_auth_code(),
                    resource_id=self._get_persistent_app_instance_id()
                )
                return ResultDO.success_result(detail)
            except Exception as e:
                logger.error(f"获取移动设备详情失败: {e}")
                return ResultDO.error_result(f"获取移动设备详情失败: {str(e)}")
        
        @self.router.get("/latestSession", response_model=ResultDO[Optional[str]])
        async def latest_session(app_name: str = Query(alias="appName"),
                                 user: str = Query(alias="user")):
            """获取最新会话ID"""
            try:
                if not app_name or not user:
                    return ResultDO.error_result("应用名称和用户ID不能为空")
                
                # 查找用户的最新会话
                user_sessions = [
                    session for session in self._sessions.values()
                    if session.app_name == app_name and session.user_id == user
                ]
                
                if not user_sessions:
                    return ResultDO.success_result(None)
                
                # 按最后更新时间排序，获取最新的
                latest = max(user_sessions, key=lambda s: s.last_update_time or datetime.min)
                return ResultDO.success_result(latest.id)
            except Exception as e:
                logger.error(f"获取最新会话失败: {e}")
                return ResultDO.error_result(f"获取最新会话失败: {str(e)}")
        
        @self.router.get("/list-apps", response_model=ResultDO[List[str]])
        async def list_apps():
            """列出所有可用的应用"""
            try:
                return ResultDO.success_result(list(browser_agent_register.get_agents_names()))
            except Exception as e:
                logger.error(f"列出应用失败: {e}")
                return ResultDO.error_result(f"列出应用失败: {str(e)}")
        
        @self.router.post("/run_sse")
        async def agent_run_sse(request: BrowserAgentRunRequest):
            """Agent运行SSE流式接口 """
            try:
                # 参数验证
                if not request.app_name or not request.app_name.strip():
                    logger.warning(
                        f"应用名称不能为空 in SSE request for appName: {request.app_name}, session: {request.session_id}"
                    )
                    raise HTTPException(status_code=400, detail="应用名称不能为空")

                if not request.session_id or not request.session_id.strip():
                    logger.warning(
                        f"会话ID不能为空 in SSE request for appName: {request.app_name}, session: {request.session_id}"
                    )
                    raise HTTPException(status_code=400, detail="会话ID不能为空")
                
                logger.info(f"SSE请求接收 POST /run_sse for session: {request.session_id}")
                
                # 生成SSE事件流
                return StreamingResponse(
                    self._generate_sse_events_with_runner(request),
                    media_type="text/event-stream",
                    headers={
                        "Cache-Control": "no-cache",
                        "Connection": "keep-alive",
                        "X-Accel-Buffering": "no"
                    }
                )
            except HTTPException:
                raise
            except Exception as e:
                logger.error(f"SSE处理失败: {e}")
                raise HTTPException(status_code=500, detail=f"SSE处理失败: {str(e)}")
        
        @self.router.post("/createSession", response_model=ResultDO[Session])
        async def create_session(request: BrowserAgentSessionRequest):
            """创建新会话"""
            try:
                logger.info(f"创建会话请求: app={request.app_name}, user={request.user}")
                
                # 生成新会话ID
                session_id = str(uuid.uuid4())
                initial_state = {}

                session = await self._session_service.create_session(app_name=request.app_name, user_id=request.user,state=initial_state, session_id=session_id)

                if session is None:
                    logger.error(f"会话创建调用完成但返回 None: user={request.user}")
                    raise HTTPException(status_code=500, detail="创建会话失败（结果为 None）")
                # 存储会话
                self._sessions[session_id] = session
                
                logger.info(f"会话创建成功: {session_id}")
                return ResultDO.success_result(session)
            except Exception as e:
                logger.error(f"创建会话失败: {e}")
                raise HTTPException(status_code=500, detail=f"创建会话失败: {str(e)}")

        @self.router.get("/getSession", response_model=ResultDO[Session])
        async def get_session(app_name: str = Query(alias="appName"), user: str = Query(alias="user"),
                              session_id: str = Query(alias="sessionId")):
            """获取指定会话"""
            try:
                logger.info(f"获取会话: app={app_name}, user={user}, session={session_id}")
                session = await self._find_session(app_name, user, session_id)
                return ResultDO.success_result(session)
            except Exception as e:
                logger.error(f"获取会话失败: {e}")
                return ResultDO.error_result(f"获取会话失败: {str(e)}")
    
    def _get_login_token(self) -> str:
        """获取登录令牌"""
        # 模拟生成登录令牌
        return self.client.get_login_token()

    def _get_auth_code(self) -> str:
        """获取授权码"""
        # 模拟生成授权码
        return f"auth_code_{uuid.uuid4().hex[:12]}"
    
    def _get_persistent_app_instance_id(self) -> str:
        """获取持久化应用实例ID"""
        # 模拟生成实例ID
        return f"instance_{uuid.uuid4().hex[:20]}"
    
    async def _find_session(self, app_name: str, user_id: str, session_id: str) -> Optional[Session]:
        """查找会话"""
        session = await self._session_service.get_session(app_name=app_name, user_id=user_id, session_id=session_id)
        
        if not session:
            logger.warning(f"会话未找到: app={app_name}, user={user_id}, session={session_id}")
            return None
        
        if session.app_name != app_name or session.user_id != user_id:
            logger.warning(f"会话信息不匹配: 期望 {app_name}/{user_id}, 实际 {session.app_name}/{session.user_id}")
            return None
        
        logger.debug(f"找到会话: {session_id}")
        return session
    
    async def _generate_sse_events_with_runner(self, request: BrowserAgentRunRequest) -> AsyncGenerator[str, None]:
        """生成SSE事件流"""
        session_id = request.session_id

        try:
            # 获取Runner
            try:
                runner = self.browser_runner_service.get_runner(request.app_name)
            except HTTPException as e:
                logger.warning(f"设置失败 for SSE request for session {session_id}: {e.detail}")
                # 发送错误事件并结束
                error_event = {
                    'type': 'error',
                    'session_id': session_id,
                    'error': e.detail,
                    'timestamp': datetime.now().isoformat()
                }
                yield f"data: {json.dumps(error_event)}\n\n"
                return

            # 构建RunConfig
            run_config = RunConfig(
                streaming_mode=StreamingMode.SSE if request.streaming else StreamingMode.NONE
            )


            # 运行异步任务
            event_generator = runner.run_async(
                user_id=request.user_id,
                session_id=request.session_id,
                new_message=request.new_message,
                run_config=run_config
            )

            # 处理事件流
            async for event in event_generator:
                try:
                    logger.debug(f"SSE: 发送事件 {event.id} for session {session_id}")
                    logger.info(f"此时的event: {event}")
                    # 将Event对象转换为JSON并发送
                    event_json = self._event_to_json(event)
                    yield f"data: {event_json}\n\n"

                except Exception as e:
                    logger.error(f"SSE: 发送事件时发生异常 for session {session_id}: {e}")
                    error_event = {
                        'type': 'error',
                        'session_id': session_id,
                        'error': f"发送事件失败: {str(e)}",
                        'timestamp': datetime.now().isoformat()
                    }
                    yield f"data: {json.dumps(error_event)}\n\n"
                    return

            # 流正常完成
            logger.debug(f"SSE: 流正常完成 for session: {session_id}")
            complete_event = {
                'type': 'complete',
                'session_id': session_id,
                'message': '处理完成',
                'timestamp': datetime.now().isoformat()
            }
            yield f"data: {json.dumps(complete_event)}\n\n"

        except asyncio.CancelledError:
            logger.debug(f"SSE: 连接被取消 for session: {session_id}")
            raise
        except Exception as e:
            # 流错误处理
            logger.error(f"SSE: 流错误 for session {session_id}: {e}")
            error_event = {
                'type': 'error',
                'session_id': session_id,
                'error': str(e),
                'timestamp': datetime.now().isoformat()
            }
            yield f"data: {json.dumps(error_event)}\n\n"

    def _event_to_json(self, event: Event) -> str:
        """将Event对象转换为JSON字符串"""
        try:
            # 如果Event有toJson方法，直接使用
            if hasattr(event, 'to_json'):
                return event.to_json()

            # 否则手动构建JSON
            event_dict = {
                'id': getattr(event, 'id', None),
                'type': type(event).__name__,
                'content': getattr(event, 'content', None),
                'author': getattr(event, 'author', None),
                'timestamp': datetime.now().isoformat()
            }

            # 处理Content对象
            if hasattr(event, 'content') and event.content:
                if hasattr(event.content, 'parts') and event.content.parts:
                    event_dict['content'] = {
                        'parts': [{'text': part.text if hasattr(part, 'text') else str(part)}
                                for part in event.content.parts]
                    }

            return json.dumps(event_dict, ensure_ascii=False)

        except Exception as e:
            logger.warning(f"事件转换JSON失败: {e}")
            return json.dumps({
                'type': 'unknown_event',
                'error': f'事件序列化失败: {str(e)}',
                'timestamp': datetime.now().isoformat()
            }, ensure_ascii=False)

    async def _generate_sse_events(self, request: BrowserAgentRunRequest):
        """生成SSE事件流 - 保留原有方法作为备用"""
        try:
            # 发送开始事件
            yield f"data: {json.dumps({'type': 'start', 'session_id': request.session_id, 'message': '开始处理请求'})}\n\n"
            
            # 模拟处理过程
            for i in range(5):
                await asyncio.sleep(1)  # 模拟处理时间
                event_data = {
                    'type': 'progress',
                    'session_id': request.session_id,
                    'step': i + 1,
                    'message': f'处理步骤 {i + 1}: 执行 {request.new_message}',
                    'timestamp': datetime.now().isoformat()
                }
                yield f"data: {json.dumps(event_data)}\n\n"
            
            # 发送完成事件
            final_event = {
                'type': 'complete',
                'session_id': request.session_id,
                'message': f'请求处理完成: {request.new_message}',
                'result': '模拟的处理结果',
                'timestamp': datetime.now().isoformat()
            }
            yield f"data: {json.dumps(final_event)}\n\n"
            
        except asyncio.CancelledError:
            logger.info(f"SSE连接被取消: session={request.session_id}")
            raise
        except Exception as e:
            logger.error(f"SSE事件生成失败: {e}")
            error_event = {
                'type': 'error',
                'session_id': request.session_id,
                'error': str(e),
                'timestamp': datetime.now().isoformat()
            }
            yield f"data: {json.dumps(error_event)}\n\n"
    
    def register_app(self, app_name: str, app_info: Dict[str, Any]):
        """注册应用"""
        self._registered_apps[app_name] = app_info
        logger.info(f"注册应用: {app_name}")
    
    def get_router(self) -> APIRouter:
        """获取路由器"""
        return self.router


# 创建默认实例
browser_use_dev_controller = BrowserUseDevController()
logger.info("BrowserUseDevController initialized.")
# 导出路由器
dev_router = browser_use_dev_controller.get_router()

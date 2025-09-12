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


import logging
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, HTTPException
from fastapi.responses import HTMLResponse

logger = logging.getLogger(__name__)


class BrowserUseResourceController:

    def __init__(self, template_base_path: Optional[str] = None, static_base_path: Optional[str] = None):
        """
        初始化
        
        Args:
            template_base_path: 模板文件基础路径
            static_base_path: 静态文件基础路径
        """
        if template_base_path is None:
            current_file = Path(__file__)
            project_root = current_file.parent.parent
            template_base_path = project_root / "template"
        
        if static_base_path is None:
            current_file = Path(__file__)
            project_root = current_file.parent.parent
            static_base_path = project_root / "static"
        
        self.template_base_path = Path(template_base_path)
        self.static_base_path = Path(static_base_path)
        self.router = APIRouter()
        self._setup_routes()
        
        # 确保目录存在
        self.template_base_path.mkdir(parents=True, exist_ok=True)
        self.static_base_path.mkdir(parents=True, exist_ok=True)
    
    def _setup_routes(self):
        """设置路由"""
        
        @self.router.get("/container1", response_class=HTMLResponse)
        async def rest_page():
            """容器视图页面"""
            return await self._serve_template("containerView.html")
        
        @self.router.get("/sdk/ASP/container.html", response_class=HTMLResponse)
        async def rest_mobile_page():
            """移动端容器页面"""
            return await self._serve_static_html("alibaba/mobile/sdk/ASP/container.html")
        
        @self.router.get("/alibaba/desktop", response_class=HTMLResponse)
        async def desktop_index():
            """桌面端首页"""
            return await self._serve_static_html("alibaba/index.html")
        
        @self.router.get("/alibaba/mobile", response_class=HTMLResponse)
        async def mobile_index():
            """移动端首页"""
            return await self._serve_static_html("alibaba/mobile/index.html")
    
    async def _serve_template(self, template_name: str) -> HTMLResponse:
        """
        服务模板文件
        
        Args:
            template_name: 模板文件名
            
        Returns:
            HTML响应
        """
        template_path = self.template_base_path / template_name
        
        if not template_path.exists():
            raise HTTPException(status_code=404, detail="<UNK>")

        try:
            content = template_path.read_text(encoding='utf-8')
            return HTMLResponse(
                content=content,
                headers={
                    "Content-Security-Policy": "frame-ancestors *",
                    "X-Frame-Options": ""
                }
            )
        except Exception as e:
            logger.error(f"读取模板文件失败: {template_path}, 错误: {e}")
            raise HTTPException(status_code=500, detail=f"读取模板文件失败: {str(e)}")
    
    async def _serve_static_html(self, file_path: str) -> HTMLResponse:
        """
        服务静态HTML文件
        
        Args:
            file_path: 相对于静态目录的文件路径
            
        Returns:
            HTML响应
        """
        full_path = self.static_base_path / file_path
        
        if not full_path.exists():
            raise HTTPException(status_code=404, detail=f"<UNK>: {str(file_path)}")

        try:
            content = full_path.read_text(encoding='utf-8')
            return HTMLResponse(
                content=content,
                headers={
                    "Content-Security-Policy": "frame-ancestors *",
                    "X-Frame-Options": ""
                }
            )
        except Exception as e:
            logger.error(f"读取HTML文件失败: {full_path}, 错误: {e}")
            raise HTTPException(status_code=500, detail=f"读取HTML文件失败: {str(e)}")

    
    def get_router(self) -> APIRouter:
        return self.router


browser_use_resource_controller = BrowserUseResourceController()

resource_router = browser_use_resource_controller.get_router()

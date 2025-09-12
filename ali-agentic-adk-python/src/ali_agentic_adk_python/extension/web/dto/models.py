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


"""
Web API的数据传输对象（DTO）模型们
"""
import os
from typing import Optional, List, Dict, Any, Generic, TypeVar

from google.genai.types import Content
from pydantic import BaseModel, Field
from datetime import datetime
from pydantic.alias_generators import to_camel



T = TypeVar('T')


class ResultDO(BaseModel, Generic[T]):
    """统一返回结果模型"""
    success: bool = True
    code: Optional[str] = None
    message: Optional[str] = None
    data: Optional[T] = None

    @classmethod
    def success_result(cls, data: Optional[T] = None, message: str = "操作成功") -> "ResultDO[T]":
        """创建成功结果"""
        return cls(success=True, data=data, message=message)

    @classmethod
    def error_result(cls, message: str = "操作失败", code: str = "ERROR") -> "ResultDO[T]":
        """创建错误结果"""
        return cls(success=False, message=message, code=code)


class ComputerDetailDTO(BaseModel):
    """桌面设备详情DTO"""
    desktopId: Optional[str] = None
    regionId: Optional[str] = "cn-hangzhou"
    loginToken: Optional[str] = None
    resolution_width: Optional[int] = 1920
    resolution_height: Optional[int] = 1080
    monitors_config: Optional[int] = 150

    class Config:
        alias_generator = to_camel
        allow_population_by_field_name = True


class MobileDetailDTO(BaseModel):
    """移动设备详情DTO"""
    desktop_id: Optional[str] = None
    auth_code: Optional[str] = None
    resource_id: Optional[str] = None
    resource_group_id: Optional[str] = None
    app_instance_group_id: Optional[str] = None

    class Config:
        alias_generator = to_camel
        allow_population_by_field_name = True


class BrowserAgentRunRequest(BaseModel):
    """浏览器Agent运行请求"""
    app_name: str
    user_id: str
    session_id: str
    new_message: Content
    streaming: bool = False

    class Config:
        alias_generator = to_camel
        allow_population_by_field_name = True


class BrowserAgentSessionRequest(BaseModel):
    """浏览器Agent会话请求"""
    app_name: str = Field(..., alias="appName")
    user: str


class SessionDTO(BaseModel):
    """会话DTO"""
    id: str
    app_name: str
    user_id: str
    state: Dict[str, Any] = dict
    created_time: Optional[datetime] = None
    last_update_time: Optional[datetime] = None

    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat() if v else None
        }
        alias_generator = to_camel
        allow_population_by_field_name = True

    def to_dict(self, **kwargs) -> dict:
        """转换为字典，支持自定义参数"""
        return self.dict(
            by_alias=True,
            exclude_none=kwargs.get('exclude_none', False),
            **kwargs
        )


class ListSessionsResponse(BaseModel):
    """会话列表响应"""
    sessions: List[SessionDTO] = list






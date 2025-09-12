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


import os
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, HTTPException, Response
from fastapi.responses import FileResponse


class BrowserUseStaticController:

    def __init__(self, static_base_path: Optional[str] = None):
        """
        初始化
        
        Args:
            static_base_path: 静态资源基础路径，默认为项目根目录下的 static 目录
        """
        if static_base_path is None:
            # 获取项目根目录 - 从当前文件向上找到项目根目录
            current_file = Path(__file__)
            project_root = current_file.parent.parent
            static_base_path = project_root / "static"
        
        self.static_base_path = Path(static_base_path)
        self.router = APIRouter()
        self._setup_routes()
    
    def _setup_routes(self):
        """设置路由"""
        
        @self.router.get("/alibaba/desktop/{filename:path}")
        async def serve_alibaba_desktop_resource(filename: str):
            """
            服务桌面端静态资源
            
            Args:
                filename: 文件名（支持路径）
                
            Returns:
                文件响应
            """
            return await self._serve_resource("alibaba", filename)
        
        @self.router.get("/alibaba/mobile/{filename:path}")
        async def serve_alibaba_mobile_resource(filename: str):
            """
            服务移动端静态资源
            
            Args:
                filename: 文件名（支持路径）
                
            Returns:
                文件响应
            """
            return await self._serve_resource("alibaba/mobile", filename)
    
    async def _serve_resource(self, resource_path: str, filename: str) -> FileResponse:
        """
        服务静态资源文件
        
        Args:
            resource_path: 资源路径
            filename: 文件名
            
        Returns:
            文件响应
            
        Raises:
            HTTPException: 文件不存在时抛出 404 错误
        """
        # 构建文件路径
        file_path = self.static_base_path / resource_path / filename
        
        # 安全检查：确保文件路径在允许的目录内
        try:
            file_path = file_path.resolve()
            if not str(file_path).startswith(str(self.static_base_path.resolve())):
                raise HTTPException(status_code=403, detail="Access denied")
        except (OSError, ValueError):
            raise HTTPException(status_code=400, detail="Invalid file path")
        
        # 检查文件是否存在
        if not file_path.exists() or not file_path.is_file():
            raise HTTPException(status_code=404, detail="File not found")
        
        # 获取媒体类型
        media_type = self._get_media_type_for_filename(filename)
        
        return FileResponse(
            path=str(file_path),
            media_type=media_type,
            filename=filename
        )
    
    def _get_media_type_for_filename(self, filename: str) -> str:
        """
        根据文件名获取媒体类型
        
        Args:
            filename: 文件名
            
        Returns:
            媒体类型字符串
        """
        # 获取文件扩展名
        extension = Path(filename).suffix.lower().lstrip('.')
        
        # 媒体类型映射表
        media_type_map = {
            "css": "text/css",
            "js": "application/javascript",
            "png": "image/png",
            "jpg": "image/jpeg",
            "jpeg": "image/jpeg",
            "gif": "image/gif",
            "svg": "image/svg+xml",
            "ico": "image/x-icon",
            "html": "text/html",
            "htm": "text/html",
            "txt": "text/plain",
            "json": "application/json",
            "xml": "application/xml",
            "pdf": "application/pdf",
            "zip": "application/zip",
            "mp4": "video/mp4",
            "mp3": "audio/mpeg",
            "wav": "audio/wav",
            "webp": "image/webp"
        }
        
        return media_type_map.get(extension, "application/octet-stream")
    
    def get_router(self) -> APIRouter:
        """获取路由器"""
        return self.router


# 创建默认实例
browser_use_static_controller = BrowserUseStaticController()

# 导出路由器以便在主应用中使用
static_router = browser_use_static_controller.get_router()

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

import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from src.ali_agentic_adk_python.extension.web.configuration.browser_agent_register import browser_agent_register
from src.ali_agentic_adk_python.extension.web.controllers.browser_use_callback_controller import callback_router
from src.ali_agentic_adk_python.extension.web.controllers.browser_use_dev_controller import dev_router
from src.ali_agentic_adk_python.extension.web.controllers.browser_use_resource_controller import resource_router
from src.ali_agentic_adk_python.extension.web.controllers.browser_use_static_controller import static_router
from src.ali_agentic_adk_python.extension.web.starter import BrowserUseStarter

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def create_app() -> FastAPI:
    """
    创建 FastAPI 应用实例
    
    Returns:
        配置好的 FastAPI 应用
    """
    # 初始化应用配置
    app_config = BrowserUseStarter.initialize_application()
    
    app = FastAPI(
        title=app_config["title"],
        description=app_config["description"],
        version=app_config["version"]
    )
    
    # 添加 CORS 中间件
    cors_config = app_config["cors"]
    app.add_middleware(
        CORSMiddleware,
        allow_origins=cors_config["allow_origins"],
        allow_credentials=cors_config["allow_credentials"],
        allow_methods=cors_config["allow_methods"],
        allow_headers=cors_config["allow_headers"],
    )
    
    # 注册静态资源路由
    app.include_router(
        static_router,
        tags=["静态资源"]
    )
    
    # 注册开发接口路由
    app.include_router(
        dev_router,
        prefix="/adk/browser/_dev",
        tags=["开发接口"]
    )

    app.include_router(
        callback_router,
        prefix="/adk/browser/_callback",
        tags=["回调接口"]
    )
    
    # 注册资源页面路由
    app.include_router(
        resource_router,
        tags=["页面资源"]
    )
    
    @app.get("/")
    async def root():
        """根路径"""
        return {
            "message": "Ali ADK Python Web API",
            "version": "1.0.0",
            "status": "running"
        }

    
    @app.get("/.well-known/appspecific/com.chrome.devtools.json")
    async def chrome_devtools_config():
        """Chrome开发者工具配置"""
        return {
            "version": "1.0",
            "name": "Ali ADK Python Browser Use",
            "description": "阿里ADK Python浏览器使用工具",
            "devtools_frontend_url": "chrome-devtools://devtools/bundled/inspector.html?experiments=true&v8only=true&ws=localhost:8000/ws"
        }
    
    return app


# 创建应用实例
app = create_app()


def main():

    logger.info("正在启动 Ali ADK Python Web 服务...")
    logger.info(f"get_agent_count: {browser_agent_register.get_agent_count()}")
    # 启动服务器
    uvicorn.run(
        "src.ali_agentic_adk_python.extension.web.app:app",
        host="0.0.0.0",
        port=7001,
        reload=False,  # 开发模式下自动重载
        log_level="info"
    )


if __name__ == "__main__":
    main()
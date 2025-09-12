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
import sys
from typing import Dict, Any

logger = logging.getLogger(__name__)


class BrowserUseStarter:

    PYTHON_ADK_VERSION = "1.0.0"

    @classmethod
    def print_welcome_banner(cls):
        """打印欢迎横幅"""
        banner = f"""
  ___  ____  _  __   ____
 / _ \\|  _ \\| |/ /  | __ ) _ __ _____      _____  ___ _ __
| | | | | | | ' /   |  _ \\| '__/ _ \\ \\ /\\ / / __|/ _ \\ '__|
| |_| | |_| | . \\   | |_) | | | (_) \\ V  V /\\__ \\  __/ |
 \\___/|____/|_|\\_\\  |____/|_|  \\___/ \\_/\\_/ |___/\\___|_|

🐍 Python ADK version {cls.PYTHON_ADK_VERSION}
"""
        print(banner)

    @classmethod
    def get_cors_config(cls) -> Dict[str, Any]:
        """
        获取CORS配置

        Returns:
            CORS配置字典
        """
        return {
            "allow_origins": ["*"],
            "allow_methods": ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
            "allow_headers": ["*"],
            "expose_headers": ["Authorization"],
            "allow_credentials": False,
            "max_age": 3600
        }

    @classmethod
    def get_security_config(cls) -> Dict[str, Any]:
        """
        获取安全配置

        Returns:
            安全配置字典
        """
        return {
            "csrf_disabled": True,
            "permit_all": True
        }

    @classmethod
    def initialize_application(cls, app_config: Dict[str, Any] = None) -> Dict[str, Any]:
        """
        初始化应用配置

        Args:
            app_config: 应用配置

        Returns:
            完整的应用配置
        """

        cls.print_welcome_banner()

        # 默认配置
        default_config = {
            "title": "Ali ADK Python - Browser Use",
            "description": "阿里ADK Python Browser Use平台",
            "version": cls.PYTHON_ADK_VERSION,
            "cors": cls.get_cors_config(),
            "security": cls.get_security_config(),
        }

        # 合并用户配置
        if app_config:
            default_config.update(app_config)

        logger.info(f"Browser Use 应用初始化完成 (版本: {cls.PYTHON_ADK_VERSION})")
        return default_config



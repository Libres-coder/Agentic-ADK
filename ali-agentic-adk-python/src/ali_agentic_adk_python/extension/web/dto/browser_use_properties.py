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


from pydantic import BaseModel
from src.ali_agentic_adk_python.extension.web.utils.environment import properties
from typing import List

class BrowserUseProperties(BaseModel):
    """ADK浏览器使用配置"""
    enable: bool = properties.get('ali.adk.browser.use.properties.enable', False)
    enable_wuying: bool = properties.get('ali.adk.browser.use.properties.enableWuying', False)
    computer_resource_id: str = properties.get('ali.adk.browser.use.properties.computerResourceId', None)
    mobile_resource_id: str = properties.get('GOOGLE_GENAI_USE_VERTEXAI', None)
    ak: str = properties.get('ali.adk.browser.use.properties.ak', None)
    sk: str = properties.get('ali.adk.browser.use.properties.sk', None)
    endpoint: str = properties.get('ali.adk.browser.use.properties.endpoint', 'ecd.cn-hangzhou.aliyuncs.com')
    mobile_end_point: str = properties.get('ali.adk.browser.use.properties.endpoint', 'ecd.cn-hangzhou.aliyuncs.com')
    app_stream_end_point: str = properties.get('ali.adk.browser.use.properties.endpoint',
                                               'ecd.cn-hangzhou.aliyuncs.com')
    endpoints: List[str] = list
    path: str = "D:\\scripts"
    user_id: str = properties.get('ali.adk.browser.use.properties.endUserId', None)
    region_id: str = "cn-hangzhou"
    password: str = properties.get('ali.adk.browser.use.properties.password', None)
    office_site_id: str = properties.get('ali.adk.browser.use.properties.officeSiteId', None)
    instance_group_id: str = None

    port: int = properties.get('ali.adk.browser.use.properties.port', 7001)
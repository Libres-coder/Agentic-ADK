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


import base64
import logging
import os
from typing import Dict, Any, Optional, List

from alibabacloud_ecd20200930.models import RunCommandRequest, DescribeInvocationsRequest

from src.ali_agentic_adk_python.extension.web.service.ecd import EcdService
from src.ali_agentic_adk_python.extension.web.utils.snowflake import StringBasedLetterSnowflake
from src.ali_agentic_adk_python.extension.web.utils.code_to_bat_converter import CodeToBatConverter
from src.ali_agentic_adk_python.extension.web.service.browser_use_service_caller import BrowserUseServiceCaller
from src.ali_agentic_adk_python.extension.web.dto.desktop_command_response import DesktopCommandResponse
from src.ali_agentic_adk_python.extension.web.dto.browser_use_properties import BrowserUseProperties


logger = logging.getLogger(__name__)

class BrowserUseTool:
    python_execute_command = '& "C:\\Users\\%s\\AppData\\Local\\Programs\\Python\\Python313\\python.exe" "D:\\scripts\\%s"'

    def __init__(self, ecd_command_service: EcdService, adk_browser_use_properties: BrowserUseProperties,
                 browser_use_service_caller: BrowserUseServiceCaller,
                 snowflake: StringBasedLetterSnowflake):
        self.ecd_command_service = ecd_command_service
        self.adk_browser_use_properties = adk_browser_use_properties
        self.browser_use_service_caller = browser_use_service_caller
        self.snowflake = snowflake

    def create_python_file(self, python_script: str, file_name: str) -> str:
        run_command_request = RunCommandRequest(
            desktop_id=[self.adk_browser_use_properties.computer_resource_id],
            content_encoding='Base64',
            type='RunBatScript',
        )
        command = CodeToBatConverter.convert_code_to_bat(
            python_script, self.adk_browser_use_properties.path, file_name
        )
        encoded_content = base64.b64encode(command.encode()).decode()
        run_command_request.command_content = encoded_content
        return self.ecd_command_service.run_command(run_command_request)

    def run_script(self, file_name: str) -> str:
        run_command_request = RunCommandRequest(
            desktop_id= [self.adk_browser_use_properties.computer_resource_id],
            content_encoding="Base64",
            type="RunPowerShellScript",
            end_user_id=self.adk_browser_use_properties.user_id
        )
        command = self.python_execute_command % (self.adk_browser_use_properties.user_id, file_name)
        encoded_content = base64.b64encode(command.encode()).decode()
        run_command_request.command_content = encoded_content
        return self.ecd_command_service.run_command(run_command_request)

    def run_command(self, python_script: str) -> Dict[str, str]:
        file_name = self.snowflake.next_id() + ".py"
        invoke_id = self.create_python_file(python_script, file_name)
        response = self.get_command_result(invoke_id)
        if response is None:
            return {"result": f"任务下发失败，任务id: {invoke_id}"}
        while response.invocation_status.lower() in ("pending", "running"):
            response = self.get_command_result(invoke_id)
            if response is None:
                return {"result": f"任务下发失败，任务id: {invoke_id}"}
        if response.invocation_status.lower() != "success":
            return {"result": f"任务下发失败，output: {response.output}"}
        return {"result": self.run_script(file_name)}

    def get_command_result(self, invoke_id: str) -> Optional[DesktopCommandResponse]:
        request = DescribeInvocationsRequest(
            invoke_id= invoke_id,
        )
        response: List[DesktopCommandResponse] = self.ecd_command_service.get_command_result(request)
        if response:
            return response[0]
        return None

    def start_browser_mission(self) -> Dict[str, str]:
        from src.ali_agentic_adk_python.extension.web.utils.python_script_utils import PythonScriptUtils
        return {"result": self.create_python_file(PythonScriptUtils.start_browser_mission_script, "start_browser.py")}

    def close_browser_mission(self) -> Dict[str, str]:
        from src.ali_agentic_adk_python.extension.web.utils.python_script_utils import PythonScriptUtils
        return {"result": self.create_python_file(PythonScriptUtils.close_browser_mission_script, "close_browser.py")}

    def inner_open_browser(self, url: str, request_id: str) -> str:
        command = self.python_execute_command % (self.adk_browser_use_properties.user_id, 'browser_use.py') + f' {url}' + f' {request_id}'
        run_command_request = RunCommandRequest(
            desktop_id=[self.adk_browser_use_properties.computer_resource_id],
            content_encoding="Base64",
            type="RunPowerShellScript",
            end_user_id=self.adk_browser_use_properties.user_id
        )

        encoded_content = base64.b64encode(command.encode()).decode()
        run_command_request.command_content = encoded_content
        invoke_id =  self.ecd_command_service.run_command(run_command_request)
        return invoke_id

    def open_browser(self, url: str) -> Dict[str, str]:
        request_id = self.snowflake.next_id()
        self.browser_use_service_caller.call(request_id, lambda: self.inner_open_browser(url, request_id))
        return {"result": request_id}

    def inner_login_finish(self, file_name: str) -> str:
        run_command_request = RunCommandRequest(
            desktop_id=[self.adk_browser_use_properties.computer_resource_id],
            content_encoding="Base64",
            type="RunBatScript",
        )
        command = CodeToBatConverter.convert_code_to_bat(
            "Trueeeee", "D:\\scripts\\operate", file_name, False
        )
        encoded_content = base64.b64encode(command.encode()).decode()
        run_command_request.command_content = encoded_content
        invoke_id =  self.ecd_command_service.run_command(run_command_request)
        response = self.get_command_result(invoke_id)
        while response.invocation_status.lower() != "success":
            # 休息
            import time
            time.sleep(1)
            response = self.get_command_result(invoke_id)
        return invoke_id

    def login_finish(self) -> Dict[str, str]:
        request_id = self.snowflake.next_id()
        self.browser_use_service_caller.call(request_id, lambda: self.inner_login_finish(request_id))
        return {"result": request_id}


    def get_html(self, request_id: str) -> Dict[str, Any]:
        result = self.browser_use_service_caller.get_by_request_id(request_id)
        if not result:
            return {"result": "html info is not exist"}
        return {"html": result}


    def inner_operate_browser(self, browser_command: str, file_name: str) -> str:
        run_command_request = RunCommandRequest(
            desktop_id= [self.adk_browser_use_properties.computer_resource_id],
            content_encoding="Base64",
            type="RunBatScript",
        )
        command = CodeToBatConverter.convert_code_to_bat(
            browser_command, "D:\\scripts\\commands", file_name, False
        )
        encoded_content = base64.b64encode(command.encode()).decode()
        run_command_request.command_content = encoded_content
        invoke_id =  self.ecd_command_service.run_command(run_command_request)
        response = self.get_command_result(invoke_id)
        while response.invocation_status.lower() != "success":
            # 休息
            import time
            time.sleep(1)
            response = self.get_command_result(invoke_id)
        return invoke_id

    def operate_browser(self, browser_command: str) -> Dict[str, Any]:
        request_id = self.snowflake.next_id()
        self.browser_use_service_caller.call(request_id, lambda: self.inner_operate_browser(browser_command, request_id))
        return {"result": request_id}

    def poll_and_fetch_file(self, remote_file_path: str, poll_interval: float = 2.0) -> Optional[str]:
        remote_file_path = "D:\\scripts\\dom\\" + remote_file_path + ".json"
        import time
        powershell_script = f"""
        if (Test-Path '{remote_file_path}') {{
            Get-Content '{remote_file_path}' | Out-String
        }} else {{
            Write-Output '__NOT_FOUND__'
        }}
        """
        run_command_request = RunCommandRequest(
            desktop_id=[self.adk_browser_use_properties.computer_resource_id],
            content_encoding="Base64",
            type="RunPowerShellScript",
            end_user_id=self.adk_browser_use_properties.user_id
        )
        encoded_content = base64.b64encode(powershell_script.encode()).decode()
        run_command_request.command_content = encoded_content
        invoke_id = self.ecd_command_service.run_command(run_command_request)
        response = self.get_command_result(invoke_id)
        output = None
        while response.invocation_status.lower() != "success" and output != "__NOT_FOUND__":
            # 休息
            import time
            time.sleep(poll_interval)
            response = self.get_command_result(invoke_id)
            if response and response.invocation_status.lower() == "success":
                output = response.output.strip()
        return output

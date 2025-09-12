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


import json
import logging
import uuid
from alibabacloud_ecd20200930.models import CreateCdsFileRequest, RunCommandRequest
from alibabacloud_ecd20200930.client import Client as ecd20200930Client
from alibabacloud_ecd20201002.models import GetLoginTokenRequest
from alibabacloud_ecd20201002.client import Client as ecd20201002Client
from alibabacloud_eds_aic20230930.client import Client as edsaic20230930Client
from alibabacloud_eds_aic20230930.models import DescribeInvocationsRequest

from alibabacloud_appstream_center20210218.client import Client as appStreamClient
from alibabacloud_appstream_center20210218.models import GetAuthCodeRequest

from src.ali_agentic_adk_python.extension.web.configuration.browser_use_configuration import BrowserUseConfiguration
from src.ali_agentic_adk_python.extension.web.dto.browser_use_properties import BrowserUseProperties
from src.ali_agentic_adk_python.extension.web.dto.desktop_command_response import DesktopCommandResponse

logger = logging.getLogger(__name__)


class EcdService:
    client: ecd20200930Client
    client_jp: ecd20200930Client
    app_stream_client: appStreamClient
    mobile_client: edsaic20230930Client
    client1002: ecd20201002Client
    # 访问方式请参见：https://help.aliyun.com/document_detail/378659.html
    def __init__(self, properties: BrowserUseProperties = None):
        self.properties = properties or BrowserUseProperties()
        configuration = BrowserUseConfiguration(properties)
        self.client = configuration.client()
        self.client_jp = configuration.client_jp()
        self.app_stream_client = configuration.app_stream_client()
        self.mobile_client = configuration.mobile_client()
        self.client1002 = configuration.client1002()

    def get_login_token(self) -> str:
        try:
            request = GetLoginTokenRequest(
                region_id=self.properties.region_id,
                client_id=str(uuid.uuid4()),
                office_site_id=self.properties.office_site_id,
                end_user_id=self.properties.user_id,
                password=self.properties.password
            )
            response = self.client1002.get_login_token(request)
            logger.info(f"get_login_token: {request}, response: {response.body}")
            return response.body.login_token
        except Exception as e:
            logger.error(f"AliyunEcdServiceImpl get_login_token error, request: {json.dumps(request.__dict__)}", exc_info=e)
            return ""


    def upload_script(self, create_cds_file_request:CreateCdsFileRequest):
        try:
            create_cds_file_request.file_type = "file"
            logger.warning(f"uploadScript request in, request: {json.dumps(create_cds_file_request.__dict__)}")
            response = self.client.create_cds_file(create_cds_file_request)
            if not response.body or not getattr(response.body, 'file_model', None):
                logger.warning(f"uploadScript response is null, request: {json.dumps(create_cds_file_request.__dict__)}, response: {json.dumps(response.body.__dict__ if response.body else {})}")
                return None
            return response.body.file_model.file_id
        except Exception as e:
            logger.error(f"AliyunEcdServiceImpl uploadScript error, request: {json.dumps(create_cds_file_request.__dict__)}", exc_info=e)
            return None

    def run_command(self, request: RunCommandRequest, endpoint=None):
        try:
            logger.warning(f"runCommand request in, request: {json.dumps(request.__dict__)}, endpoint: {endpoint}")
            if endpoint == "ecd.ap-northeast-1.aliyuncs.com":
                response = self.client_jp.run_command(request)
            elif endpoint == "ecd-share.cn-hangzhou.aliyuncs.com":
                response = self.client.run_command(request)
            else:
                response = self.client.run_command(request)
            logger.info(f"runCommand request in, response: {json.dumps(response.body.__dict__)}")
            return response.body.invoke_id
        except Exception as e:
            logger.error(f"AliyunEcdServiceImpl runCommand error, request: {json.dumps(request.__dict__)}", exc_info=e)
            return None

    def get_command_result(self, request: DescribeInvocationsRequest, endpoint=None):
        try:
            request.include_output = True
            request.content_encoding = "plain_text"
            logger.info(f"AliyunEcdServiceImpl getCommandResult request: {json.dumps(request.__dict__)}, endpoint: {endpoint}")
            if endpoint == "ecd.ap-northeast-1.aliyuncs.com":
                response = self.client_jp.describe_invocations(request)
            elif endpoint == "ecd-share.cn-hangzhou.aliyuncs.com":
                response = self.client.describe_invocations(request)
            else:
                response = self.client.describe_invocations(request)
            logger.info(f"AliyunEcdServiceImpl getCommandResult success, request: {json.dumps(request.__dict__)}, endpoint: {endpoint}, original response:{str(response.body)}")
            return self.convert(response.body.invocations)
        except Exception as e:
            logger.error(f"AliyunEcdServiceImpl getCommandResult error, request: {json.dumps(request.__dict__)}", exc_info=e)
            return []

    def get_auth_code(self, user_id):
        request = GetAuthCodeRequest(end_user_id=user_id)
        try:
            response = self.app_stream_client.get_auth_code(request)
            return response.body.auth_model.auth_code
        except Exception as e:
            logger.error(f"AliyunEcdServiceImpl getAuthCode error, userId: {user_id}", exc_info=e)
        return ""

    def get_android_instance(self, request):
        try:
            response = self.mobile_client.describe_android_instances(request)
            return getattr(response.body, 'instance_model', [])
        except Exception as e:
            logger.error(f"getAndroidInstance getAuthCode error, request: {json.dumps(request.__dict__)}", exc_info=e)
            raise RuntimeError("getAndroidInstance getAuthCode error")

    def convert(self, invocations):
        response_list = []
        if not invocations:
            return response_list
        for invoke_desktop in getattr(invocations[0], 'invoke_desktops', []):
            response = DesktopCommandResponse()
            response.output = getattr(invoke_desktop, 'output', None)
            response.computer_id = getattr(invoke_desktop, 'desktop_id', None)
            response.finish_time = getattr(invoke_desktop, 'finish_time', None)
            response.invocation_status = getattr(invocations[0], 'invocation_status', None)
            response.dropped = getattr(invoke_desktop, 'dropped', None)
            response_list.append(response)
        return response_list


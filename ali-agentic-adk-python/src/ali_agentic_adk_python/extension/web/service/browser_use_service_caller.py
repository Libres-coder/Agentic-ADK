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


import threading
import time
# from concurrent.futures import Future
from asyncio.futures import Future
from typing import Callable, Dict, Optional
import logging
logger = logging.getLogger(__name__)

class BrowserUseServiceCaller:
    def __init__(self):
        self.pending_request: Dict[str, Future] = {}

    def call_and_wait(self, request_id: str, runnable: Callable[[], None], timeout: int = 120) -> Optional[str]:
        future = Future()
        self.pending_request[request_id] = future
        # 执行下发
        threading.Thread(target=runnable).start()
        try:
            return future.result()
        except Exception as e:
            logger.error(f"call_and_wait fail, request_id: {request_id}, error: {e}")
            return None

    def call(self, request_id: str, runnable: Callable[[], str|None] ):
        # 异步执行任务并注册请求
        future = Future()
        self.pending_request[request_id] = future
        threading.Thread(target=runnable).start()

    def handle_callback(self, request_id: str, result: str):
        future = self.pending_request.get(request_id)
        logger.info(f"handle_callback request_id: {request_id}, future: {future}")
        if future and not future.done():
            logger.info(f"handle_callback, request_id: {request_id}")
            future.set_result(result)

    def get_by_request_id(self, request_id: str, timeout: int = 120) -> Optional[str]:
        future = self.pending_request.get(request_id)
        if not future:
            logger.info(f"not future, request_id: {request_id}, timeout: {timeout}")
            return None
        try:
            logger.info(f"get_by_request_id, request_id: {request_id}, timeout: {timeout}")
            return future.result()
        except Exception as e:
            logger.error(f"get_by_request_id fail, request_id: {request_id}, error: {e}")
            return None

browser_use_service_caller = BrowserUseServiceCaller()
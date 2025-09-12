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
from typing import Dict
from google.adk.runners import Runner
from google.adk.agents import BaseAgent
from google.adk.artifacts import BaseArtifactService
from google.adk.sessions import BaseSessionService

from src.ali_agentic_adk_python.extension.web.configuration.browser_agent_register import browser_agent_register

logger = logging.getLogger(__name__)


class BrowserRunnerService:

    def __init__(self, artifact_service: BaseArtifactService, session_service: BaseSessionService):
        self.artifact_service = artifact_service
        self.session_service = session_service
        self.runner_cache: Dict[str, Runner] = {}
    
    def get_runner(self, app_name: str) -> Runner:

        if app_name not in self.runner_cache:
            agent = browser_agent_register.get_agents().get(app_name)
            
            if agent is None:
                available_apps = list(browser_agent_register.get_agents().keys())
                logger.error(
                    f"Agent/App named '{app_name}' not found in registry. Available apps: {available_apps}"
                )
                raise RuntimeError(f"Agent/App not found: {app_name}")
            else:
                logger.info(
                    f"RunnerService: Creating Runner for appName: {app_name}, using agent definition: {agent.name}"
                )
                runner = Runner(
                    agent=agent,
                    app_name=app_name,
                    session_service=self.session_service
                )
                self.runner_cache[app_name] = runner
        
        return self.runner_cache[app_name]
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
from typing import Dict, Optional, List
from threading import Lock

from google.adk.agents import BaseAgent

logger = logging.getLogger(__name__)


class BrowserAgentRegister:


    # 线程安全锁
    _lock = Lock()
    
    def __init__(self):
        self._agents = {}
    
    def register(self, agent: BaseAgent) -> None:
        if not isinstance(agent, BaseAgent):
            raise TypeError(f"Expected BaseAgent instance, got {type(agent)}")
        
        agent_name = agent.name
        
        with self._lock:
            if agent_name in self._agents:
                error_msg = f"Agent with name {agent_name} already registered"
                logger.error(error_msg)
                raise ValueError(error_msg)
            
            self._agents[agent_name] = agent
            logger.info(f"Successfully registered agent: {agent_name}")
            logger.info(self._agents)

    def get_agent(self, agent_name: str) -> Optional[BaseAgent]:

        with self._lock:
            agent = self._agents.get(agent_name)
            if agent:
                logger.debug(f"Found agent: {agent_name}")
            else:
                logger.warning(f"Agent not found: {agent_name}")
            return agent

    def get_agents(self) -> Dict[str, BaseAgent]:

        with self._lock:
            agents_copy = self._agents.copy()
            logger.debug(f"Retrieved {len(agents_copy)} registered agents")
            return agents_copy

    def get_agents_names(self) -> List[str]:
        """Get the names of all registered agents."""
        with self._lock:
            logger.info(f"Retrieved {len(self._agents)} registered agents")
            return list(self._agents.keys())

    def get_agent_count(self) -> int:
        """
        获取已注册代理数量 - 额外功能
        
        Returns:
            已注册代理的数量
        """
        with self._lock:
            return len(self._agents)
    
    def unregister(self, agent_name: str) -> bool:
        if not agent_name:
            logger.warning("Agent name cannot be empty")
            return False
        
        with self._lock:
            if agent_name in self._agents:
                del self._agents[agent_name]
                logger.info(f"Successfully unregistered agent: {agent_name}")
                return True
            else:
                logger.warning(f"Cannot unregister non-existent agent: {agent_name}")
                return False

    def is_registered(self, agent_name: str) -> bool:
        if not agent_name:
            return False
        
        with self._lock:
            return agent_name in self._agents


browser_agent_register = BrowserAgentRegister()


# 为了方便使用，提供模块级别的函数
def register_agent(agent: BaseAgent) -> None:
    browser_agent_register.register(agent)


def get_agent(agent_name: str) -> Optional[BaseAgent]:
    return browser_agent_register.get_agent(agent_name)


def get_all_agents() -> Dict[str, BaseAgent]:
    return browser_agent_register.get_agents()

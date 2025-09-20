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
logger = logging.getLogger(__name__)

class PromptUtils:
    @staticmethod
    def generate_prompt(prompt_template: str, params: dict) -> str:
        return PromptUtils._replace(prompt_template, params)

    @staticmethod
    def generate_prompt_with_supplier(prompt_template: str, param_supplier) -> str:
        logger.debug("generate_prompt_with_supplier")
        return PromptUtils._replace(prompt_template, param_supplier())


    @staticmethod
    def _replace(origin_content: str, prompt_params: dict) -> str:
        logger.debug(f"prompt_params: {prompt_params}")
        if prompt_params:
            replaced_prompt = origin_content
            # 先替换${key}
            for k, v in prompt_params.items():
                replaced_prompt = replaced_prompt.replace('${' + k + '}', str(v))
            # 再替换$!{key}
            for k, v in prompt_params.items():
                replaced_prompt = replaced_prompt.replace('$!{' + k + '}', str(v))
            # 再替换{key}
            for k, v in prompt_params.items():
                replaced_prompt = replaced_prompt.replace('{' + k + '}', str(v))
            return replaced_prompt
        else:
            return origin_content
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


from typing import Optional, Any

from dashscope import Application, Assistants
from google.adk.tools import BaseTool, ToolContext, AgentTool, FunctionTool
from google.genai import types
import inspect
from google.adk.agents import BaseAgent

from google.genai.types import Schema, Type


class DashscopeAppTool(BaseTool):
    app_id: str
    prompt: str = None
    api_key: str = None

    def __init__(self, name: str, description: str = ""):
        super().__init__(name=name, description=description)
        """初始化工具
        
        Args:
            name: 工具名称
            description: 工具描述
        """
        self.self_define_param = {}
        self.skip_summarization = False

    def _get_declaration(self) -> Optional[types.FunctionDeclaration]:
        declaration = types.FunctionDeclaration()
        declaration.name = self.name
        declaration.description = self.description
        declaration.parameters = Schema(
            type=types.Type.OBJECT,
            properties={
                "app_id": Schema(type=Type("string"), description="Dashscope应用ID"),
                "prompt": Schema(type=Type("string"), description="用户输入的内容"),
                "session_id": Schema(type=Type("string"), description="会话ID"),
                "api_key": Schema(type=Type("string"), description="Dashscope API Key"),
            },
            required=["app_id", "prompt"]
        )

        return declaration

    async def run_async(
      self, *, args: dict[str, Any], tool_context: ToolContext
  ) -> Any:

        try:
            print("调用的工具（应用）参数", "app_id:", self.app_id, "prompt:", args.get("prompt", None), "api_key", self.api_key)
            result = Application.call(app_id = self.app_id, prompt= args.get("prompt", None), api_key= self.api_key)
            return {
                "text": result.output.text,
                "session_id": result.output.session_id
            }
        except Exception as e:
            raise e


    def function_to_schema(self, func) -> dict:
        # 将 Python 类型映射为 JSON schema 类型
        type_map = {
            str: "string",
            int: "integer",
            float: "number",
            bool: "boolean",
            list: "array",
            dict: "object",
            type(None): "null",
        }

        # 尝试获取函数的签名
        try:
            signature = inspect.signature(func)
        except ValueError as e:
            # 如果签名获取失败，则抛出错误并附带错误信息
            raise ValueError(
                f"Failed to get signature for function {func.__name__}: {str(e)}"
            )

        # 初始化一个字典来存储参数类型
        parameters = {}
        # 遍历函数的参数，并映射它们的类型
        for param in signature.parameters.values():
            try:
                param_type = type_map.get(param.annotation, "string")
            except KeyError as e:
                # 如果参数的类型注解未知，则抛出错误
                raise KeyError(
                    f"Unknown type annotation {param.annotation} for parameter {param.name}: {str(e)}"
                )
            parameters[param.name] = {"type": param_type}

        # 创建必需参数的列表（那些没有默认值的参数）
        required = [
            param.name
            for param in signature.parameters.values()
            if param.default == inspect._empty
        ]

        # 返回函数的 schema 作为字典
        return {
            "type": "function",
            "function": {
                "name": func.__name__,
                "description": (func.__doc__ or "").strip(),  # 获取函数描述（docstring）
                "parameters": {
                    "type": "object",
                    "properties": parameters,  # 参数类型
                    "required": required,  # 必需参数列表
                },
            },
        }

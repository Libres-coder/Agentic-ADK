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


from dataclasses import dataclass
from typing import Optional, Dict, Any, List, Union

@dataclass
class FunctionItem:
    type: str = ""

    def to_dict(self) -> Dict[str, Any]:
        return {
            "type": self.type
        }

    @classmethod
    def from_dict(cls, d: Dict[str, Any]) -> "FunctionItem":
        return cls(
            type=d.get("type", "")
        )

    def set_type(self, type_):
        self.type = type_

@dataclass
class FunctionProperty:
    description: str = ""
    type: str = ""
    items: Any = None  # FunctionItem or FunctionProperty or None
    properties: Optional[Dict[str, "FunctionProperty"]] = None
    required: Optional[List[str]] = None
    default: Any = None

    def __post_init__(self):
        if self.properties is None:
            self.properties = {}
        if self.required is None:
            self.required = []

    def to_dict(self) -> Dict[str, Any]:
        # items 可能是 FunctionItem 或 FunctionProperty 或 None
        if isinstance(self.items, FunctionItem):
            items_dict = self.items.to_dict()
        elif isinstance(self.items, FunctionProperty):
            items_dict = self.items.to_dict()
        else:
            items_dict = self.items
        return {
            "description": self.description,
            "type": self.type,
            "items": items_dict,
            "properties": {k: v.to_dict() for k, v in self.properties.items()} if self.properties else {},
            "required": self.required,
            "default": self.default
        }

    @classmethod
    def from_dict(cls, d: Dict[str, Any]) -> "FunctionProperty":
        # 反序列化 items
        items_data = d.get("items", None)
        items = None
        if isinstance(items_data, dict):
            # 判断是 FunctionItem 还是 FunctionProperty
            if "properties" in items_data or "items" in items_data:
                items = FunctionProperty.from_dict(items_data)
            elif "type" in items_data:
                items = FunctionItem.from_dict(items_data)
        # 反序列化 properties
        properties_data = d.get("properties", {})
        properties = {k: FunctionProperty.from_dict(v) for k, v in properties_data.items()} if isinstance(properties_data, dict) else {}
        return cls(
            description=d.get("description", ""),
            type=d.get("type", ""),
            items=items,
            properties=properties,
            required=d.get("required", []),
            default=d.get("default", None)
        )

    def set_description(self, description):
        self.description = description

    def set_type(self, type_):
        self.type = type_

    def set_items(self, items):
        self.items = items

@dataclass
class FunctionParameter:
    type: str = "object"
    required: Optional[List[str]] = None
    properties: Optional[Dict[str, FunctionProperty]] = None

    def __post_init__(self):
        if self.required is None:
            self.required = []
        if self.properties is None:
            self.properties = {}

    def to_dict(self) -> Dict[str, Any]:
        return {
            "type": self.type,
            "required": self.required,
            "properties": {k: v.to_dict() for k, v in self.properties.items()} if self.properties else {}
        }

    @classmethod
    def from_dict(cls, d: Dict[str, Any]) -> "FunctionParameter":
        properties_data = d.get("properties", {})
        properties = {k: FunctionProperty.from_dict(v) for k, v in properties_data.items()} if isinstance(properties_data, dict) else {}
        return cls(
            type=d.get("type", "object"),
            required=d.get("required", []),
            properties=properties
        )

    def set_required(self, required):
        self.required = required

    def set_properties(self, properties):
        self.properties = properties


@dataclass
class FunctionDefinition:
    name: str = ""
    description: Optional[str] = None
    parameters: Optional[FunctionParameter] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "description": self.description,
            "parameters": self.parameters.to_dict() if self.parameters else None
        }

    @classmethod
    def from_dict(cls, d: Dict[str, Any]) -> "FunctionDefinition":
        params_data = d.get("parameters", None)
        parameters = FunctionParameter.from_dict(params_data) if params_data else None
        return cls(
            name=d.get("name", ""),
            description=d.get("description", None),
            parameters=parameters
        )
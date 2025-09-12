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



class CodeToBatConverter:
    @staticmethod
    def convert_code_to_bat(input_code: str, save_path: str, file_name: str, utf8_config: bool = True) -> str:
        file_name = file_name + ".txt"
        bat_script = []
        bat_script.append("@echo off\n")
        bat_script.append("setlocal enabledelayedexpansion\n\n")
        bat_script.append(f'set "savepath={save_path}"\n')
        bat_script.append(f'set "filename={file_name}\n\n')
        bat_script.append('if not exist "%savepath%" mkdir "%savepath%"\n\n')
        bat_script.append("(\n")

        if utf8_config:
            bat_script.append('echo # -*- coding: utf-8 -*-\n')

        for line in input_code.split("\n"):
            line = (line.replace("^", "^^")
                        .replace("&", "^&")
                        .replace("<", "^<")
                        .replace(">", "^>")
                        .replace("|", "^|")
                        .replace("(", "^(")
                        .replace(")", "^)")
                        .replace("%", "%%"))
            line = line.replace('"', '^"')
            if line.strip() == "":
                bat_script.append("echo.\n")
            else:
                bat_script.append(f"echo {line}\n")

        bat_script.append(') > "%savepath%\\%filename%"\n\n')
        bat_script.append('echo Python脚本已保存为 %savepath%\\%filename%\n')
        return "".join(bat_script)

    @staticmethod
    def main():
        code_string = (
            "from selenium import webdriver\n"
            "from selenium.webdriver.common.by import By\n\n"
            "# 初始化浏览器驱动\n"
            "driver = webdriver.Chrome()\n\n"
            "# 打开网页\n"
            "driver.get(\"https://www.example.com\")\n\n"
            "# 找到元素并点击\n"
            "element = driver.find_element(By.ID, \"submit-button\")\n"
            "element.click()\n\n"
            "# 关闭浏览器\n"
            "driver.quit()"
        )
        save_path = "D:\\scripts"
        file_name = "generated_script.py"
        bat_script = CodeToBatConverter.convert_code_to_bat(code_string, save_path, file_name)
        print("生成的 BAT 脚本：")
        print(bat_script)

if __name__ == "__main__":
    CodeToBatConverter.main()
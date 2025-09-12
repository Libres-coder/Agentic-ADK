/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.agentic.computer.use.utils;

public class CodeToBatConverter {


    public static String convertCodeToBat(String inputCode, String savePath, String fileName, boolean utf8Config) {
        StringBuilder batScript = new StringBuilder();

        // 构建 BAT 脚本
        batScript.append("@echo off\n");
        batScript.append("setlocal enabledelayedexpansion\n\n");
        batScript.append("set \"savepath=").append(savePath).append("\"\n");
        batScript.append("set \"filename=").append(fileName).append("\"\n\n");
        batScript.append("if not exist \"%savepath%\" mkdir \"%savepath%\"\n\n");
        batScript.append("(\n");

        if(utf8Config) {
            //指定utf-8编码，防止中文解析问题
            batScript.append("echo ").append("# -*- coding: utf-8 -*-").append("\n");
        }


        // 处理每一行代码
        for (String line : inputCode.split("\n")) {
            // 转义特殊字符
            line = line.replace("^", "^^")
                    .replace("&", "^&")
                    .replace("<", "^<")
                    .replace(">", "^>")
                    .replace("|", "^|")
                    .replace("(", "^(")
                    .replace(")", "^)")
                    .replace("%", "%%");

            // 处理引号：在BAT脚本中使用 ^ 转义双引号
            line = line.replace("\"", "^\"");

            // 处理空行
            if (line.trim().isEmpty()) {
                batScript.append("echo.\n");
            } else {
                batScript.append("echo ").append(line).append("\n");
            }
        }

        batScript.append(") > \"%savepath%\\%filename%\"\n\n");
        batScript.append("echo Python脚本已保存为 %savepath%\\%filename%\n");

        return batScript.toString();
    }


    public static String convertCodeToBat(String inputCode, String savePath, String fileName) {
        return convertCodeToBat(inputCode, savePath, fileName, true);
    }

    public static void main(String[] args) {
        // 示例代码字符串
        String codeString = "from selenium import webdriver\n" +
                "from selenium.webdriver.common.by import By\n\n" +
                "# 初始化浏览器驱动\n" +
                "driver = webdriver.Chrome()\n\n" +
                "# 打开网页\n" +
                "driver.get(\"https://www.example.com\")\n\n" +
                "# 找到元素并点击\n" +
                "element = driver.find_element(By.ID, \"submit-button\")\n" +
                "element.click()\n\n" +
                "# 关闭浏览器\n" +
                "driver.quit()";

        // 指定保存路径和文件名
        String savePath = "D:\\scripts";
        String fileName = "generated_script.py";

        String batScript = convertCodeToBat(codeString, savePath, fileName);
        System.out.println("生成的 BAT 脚本：");
        System.out.println(batScript);
    }
}

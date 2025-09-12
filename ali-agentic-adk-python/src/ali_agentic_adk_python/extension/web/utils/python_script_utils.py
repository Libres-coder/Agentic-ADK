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


class PythonScriptUtils:
    """Python脚本工具类"""

    # 启动浏览器任务脚本
    start_browser_mission_script = '''
from playwright.sync_api import sync_playwright
import json
import os

def check_and_close_browser():
    """检查并关闭现有浏览器实例"""
    if os.path.exists('browser_info.json'):
        try:
            with open('browser_info.json', 'r') as f:
                browser_info = json.load(f)

            with sync_playwright() as p:
                try:
                    browser = p.chromium.connect_over_cdp(browser_info['endpoint'])
                    browser.close()
                    print("Existing browser instance closed.")
                except Exception as e:
                    print(f"No active browser instance found or error occurred: {e}")
        except json.JSONDecodeError:
            print("Invalid browser info file. It will be overwritten.")

        # 删除旧的 browser_info.json 文件
        os.remove('browser_info.json')
    else:
        print("No existing browser info found.")

def start_browser():
    """启动新的浏览器实例"""
    # 首先检查并关闭任何现有的浏览器实例
    check_and_close_browser()

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=False)
        context = browser.new_context()
        page = context.new_page()

        # 获取 CDP 会话 URL
        endpoint = browser.ws_endpoint

        # 保存 CDP 端点信息到文件
        with open('browser_info.json', 'w') as f:
            json.dump({'endpoint': endpoint}, f)

        print(f"New browser started. CDP endpoint saved to browser_info.json")
        print("Press Enter to close the browser...")
        input()

        browser.close()
        print("Browser closed.")

if __name__ == "__main__":
    start_browser()
'''

    # 关闭浏览器任务脚本
    close_browser_mission_script = '''
from playwright.sync_api import sync_playwright
import json
import os

def close_browser():
    """关闭浏览器实例"""
    try:
        if not os.path.exists('browser_info.json'):
            print("No browser info file found. Browser may already be closed.")
            return

        with open('browser_info.json', 'r') as f:
            browser_info = json.load(f)

        with sync_playwright() as p:
            browser = p.chromium.connect_over_cdp(browser_info['endpoint'])
            browser.close()
            print("Browser closed successfully.")

        # 清理browser_info.json文件
        os.remove('browser_info.json')
        print("Browser info file cleaned up.")

    except Exception as e:
        print(f"Error closing browser: {e}")

if __name__ == "__main__":
    close_browser()
'''

    # 网页操作脚本模板
    web_operation_script_template = '''
from playwright.sync_api import sync_playwright
import json
import time

def perform_web_operation():
    """执行网页操作"""
    try:
        # 连接到现有浏览器实例
        with open('browser_info.json', 'r') as f:
            browser_info = json.load(f)

        with sync_playwright() as p:
            browser = p.chromium.connect_over_cdp(browser_info['endpoint'])
            context = browser.contexts[0] if browser.contexts else browser.new_context()
            page = context.pages[0] if context.pages else context.new_page()

            # 在这里添加具体的网页操作
            # 例如：导航、点击、输入等
            {operations}

            print("Web operation completed successfully.")

    except Exception as e:
        print(f"Error performing web operation: {e}")

if __name__ == "__main__":
    perform_web_operation()
'''

    # 获取页面HTML脚本
    get_html_script = '''
from playwright.sync_api import sync_playwright
import json

def get_page_html():
    """获取当前页面的HTML"""
    try:
        with open('browser_info.json', 'r') as f:
            browser_info = json.load(f)

        with sync_playwright() as p:
            browser = p.chromium.connect_over_cdp(browser_info['endpoint'])
            context = browser.contexts[0] if browser.contexts else browser.new_context()
            page = context.pages[0] if context.pages else context.new_page()

            # 获取页面HTML
            html_content = page.content()

            # 保存到文件
            with open('page_content.html', 'w', encoding='utf-8') as f:
                f.write(html_content)

            print("Page HTML saved to page_content.html")
            return html_content

    except Exception as e:
        print(f"Error getting page HTML: {e}")
        return None

if __name__ == "__main__":
    get_page_html()
'''

    # 页面截图脚本
    screenshot_script = '''
from playwright.sync_api import sync_playwright
import json
import datetime

def take_screenshot():
    """截取当前页面截图"""
    try:
        with open('browser_info.json', 'r') as f:
            browser_info = json.load(f)

        with sync_playwright() as p:
            browser = p.chromium.connect_over_cdp(browser_info['endpoint'])
            context = browser.contexts[0] if browser.contexts else browser.new_context()
            page = context.pages[0] if context.pages else context.new_page()

            # 生成截图文件名
            timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            screenshot_path = f"screenshot_{timestamp}.png"

            # 截图
            page.screenshot(path=screenshot_path, full_page=True)

            print(f"Screenshot saved to {screenshot_path}")
            return screenshot_path

    except Exception as e:
        print(f"Error taking screenshot: {e}")
        return None

if __name__ == "__main__":
    take_screenshot()
'''


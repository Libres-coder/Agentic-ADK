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
package com.alibaba.agentic.computer.use.tool;

public abstract class PythonScriptUtils {

    public static String startBrowserMissionScript =
            """
                    from playwright.sync_api import sync_playwright
                    import json
                    import os
                        
                    def check_and_close_browser():
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
                           \s
                            # 删除旧的 browser_info.json 文件
                            os.remove('browser_info.json')
                        else:
                            print("No existing browser info found.")
                        
                    def start_browser():
                        # 首先检查并关闭任何现有的浏览器实例
                        check_and_close_browser()
                        
                        with sync_playwright() as p:
                            browser = p.chromium.launch(headless=False)
                            context = browser.new_context()
                            page = context.new_page()
                           \s
                            # 获取 CDP 会话 URL
                            endpoint = browser.ws_endpoint
                           \s
                            # 保存 CDP 端点信息到文件
                            with open('browser_info.json', 'w') as f:
                                json.dump({'endpoint': endpoint}, f)
                           \s
                            print(f"New browser started. CDP endpoint saved to browser_info.json")
                            print("Press Enter to close the browser...")
                            input()
                           \s
                            browser.close()
                            print("Browser closed.")
                        
                    if __name__ == "__main__":
                        start_browser()
            """;

    public static String closeBrowserMissionScript =
            """
                    from playwright.sync_api import sync_playwright
                    import json
                                
                    def close_browser():
                        with open('browser_info.json', 'r') as f:
                            browser_info = json.load(f)
                                
                        with sync_playwright() as p:
                            browser = p.chromium.connect_over_cdp(browser_info['endpoint'])
                            browser.close()
                            print("Browser closed successfully.")
                                
                    if __name__ == "__main__":
                        close_browser()
            """;

}

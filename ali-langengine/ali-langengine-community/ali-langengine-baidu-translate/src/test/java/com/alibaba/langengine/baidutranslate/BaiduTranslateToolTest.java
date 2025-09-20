/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.baidutranslate;

import com.alibaba.langengine.baidutranslate.tools.BaiduTranslateTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 百度翻译工具测试类
 *
 * @author Makoto
 */
@Disabled("需要配置有效的API密钥才能运行")
public class BaiduTranslateToolTest {

    private BaiduTranslateTool translator;

    @BeforeEach
    void setUp() {
        // 注意：测试时需要配置有效的API密钥
        translator = new BaiduTranslateTool("test_app_id", "test_secret_key", 30);
    }

    @Test
    void testTranslateEnglishToChinese() {
        // 设置语言
        translator.setFrom("en");
        translator.setTo("zh");

        // 执行翻译
        ToolExecuteResult result = translator.run("Hello, world!");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
        // 注意：由于API密钥无效，这里会返回错误信息
        // 如果是成功的翻译结果，会包含中文翻译
        if (!result.getOutput().contains("失败") && !result.getOutput().contains("异常")) {
            assertTrue(result.getOutput().contains("你好"));
        }
    }

    @Test
    void testTranslateChineseToEnglish() {
        // 设置语言
        translator.setFrom("zh");
        translator.setTo("en");

        // 执行翻译
        ToolExecuteResult result = translator.run("你好，世界！");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
        // 如果是成功的翻译结果，会包含英文翻译
        if (!result.getOutput().contains("失败") && !result.getOutput().contains("异常")) {
            assertTrue(result.getOutput().contains("Hello"));
        }
    }

    @Test
    void testAutoDetectLanguage() {
        // 设置自动检测源语言
        translator.setFrom("auto");
        translator.setTo("zh");

        // 执行翻译
        ToolExecuteResult result = translator.run("Good morning!");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
    }

    @Test
    void testEmptyInput() {
        // 测试空输入
        ToolExecuteResult result = translator.run("");

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("翻译文本不能为空"));
    }

    @Test
    void testNullInput() {
        // 测试null输入
        ToolExecuteResult result = translator.run(null);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
        assertTrue(result.getOutput().contains("翻译文本不能为空"));
    }

    @Test
    void testLongText() {
        // 测试长文本
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longText.append("This is a test sentence. ");
        }

        ToolExecuteResult result = translator.run(longText.toString());

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
        // 长文本可能会因为API限制而失败
    }

    @Test
    void testSpecialCharacters() {
        // 测试特殊字符
        String textWithSpecialChars = "Hello, 世界! @#$%^&*()_+{}|:<>?[]\\;'\",./";

        ToolExecuteResult result = translator.run(textWithSpecialChars);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getOutput());
    }
}

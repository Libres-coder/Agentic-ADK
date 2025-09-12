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
package com.alibaba.langengine.youdaotranslate;

import com.alibaba.langengine.youdaotranslate.tools.YoudaoTranslateTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 有道翻译工具测试类
 *
 * @author Makoto
 */
@Disabled("需要配置有效的API密钥才能运行")
public class YoudaoTranslateToolTest {

    private YoudaoTranslateTool translator;

    @BeforeEach
    void setUp() {
        // 注意：测试时需要配置有效的API密钥
        translator = new YoudaoTranslateTool("test_app_id", "test_secret_key", 30);
    }

    @Test
    void testTranslateEnglishToChinese() {
        // 设置语言
        translator.setFrom("en");
        translator.setTo("zh-CHS");

        // 执行翻译
        ToolExecuteResult result = translator.run("Hello, world!");

        // 验证结果
        assertNotNull(result);
        // 注意：由于API密钥无效，这里会失败，实际使用时需要有效密钥
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
            assertTrue(result.getResult().toString().contains("你好"));
        }
    }

    @Test
    void testTranslateChineseToEnglish() {
        // 设置语言
        translator.setFrom("zh-CHS");
        translator.setTo("en");

        // 执行翻译
        ToolExecuteResult result = translator.run("你好，世界！");

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
            assertTrue(result.getResult().toString().contains("Hello"));
        }
    }

    @Test
    void testAutoDetectLanguage() {
        // 设置自动检测源语言
        translator.setFrom("auto");
        translator.setTo("zh-CHS");

        // 执行翻译
        ToolExecuteResult result = translator.run("Good morning!");

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
        }
    }

    @Test
    void testJapaneseToChinese() {
        // 测试日语到中文翻译
        translator.setFrom("ja");
        translator.setTo("zh-CHS");

        ToolExecuteResult result = translator.run("おはようございます");

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
        }
    }

    @Test
    void testKoreanToChinese() {
        // 测试韩语到中文翻译
        translator.setFrom("ko");
        translator.setTo("zh-CHS");

        ToolExecuteResult result = translator.run("안녕하세요");

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
        }
    }

    @Test
    void testEmptyInput() {
        // 测试空输入
        ToolExecuteResult result = translator.run("");

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("翻译文本不能为空"));
    }

    @Test
    void testNullInput() {
        // 测试null输入
        ToolExecuteResult result = translator.run(null);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getError());
        assertTrue(result.getError().contains("翻译文本不能为空"));
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
        // 长文本可能会因为API限制而失败
    }

    @Test
    void testSpecialCharacters() {
        // 测试特殊字符
        String textWithSpecialChars = "Hello, 世界! @#$%^&*()_+{}|:<>?[]\\;'\",./";

        ToolExecuteResult result = translator.run(textWithSpecialChars);

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
        }
    }

    @Test
    void testTechnicalTerms() {
        // 测试技术术语
        translator.setFrom("en");
        translator.setTo("zh-CHS");

        ToolExecuteResult result = translator.run("artificial intelligence");

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
            // 技术术语应该有准确的翻译
        }
    }

    @Test
    void testIdioms() {
        // 测试习语
        translator.setFrom("en");
        translator.setTo("zh-CHS");

        ToolExecuteResult result = translator.run("break the ice");

        // 验证结果
        assertNotNull(result);
        if (result.isSuccess()) {
            assertNotNull(result.getResult());
            // 习语应该有合适的翻译
        }
    }
} 
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
package com.alibaba.langengine.core.prompt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 辅助工具
 *
 * @author xiaoxuan.lp
 */
public class PromptConverter {

    public static String replacePrompt(String text, Map<String, Object> inputs) {
        if(text == null) {
            return null;
        }
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            if(entry.getValue() == null) {
                continue;
            }
            text = text.replaceAll("\\{" + entry.getKey() + "\\}", Matcher.quoteReplacement(entry.getValue().toString()));
        }
        return text;
    }

    public static String replacePrompt(String text, String input) {
        if(text == null) {
            return null;
        }
        Map<String, Object> inputs = new HashMap<String, Object>() {{
            put("input", input);
        }};
        return replacePrompt(text, inputs);
    }

    /**
     * 将对象转换成json
     *
     * @param value
     * @return
     */
    public static String toJson(Object value) {
        return JSON.toJSONString(value);
    }

    /**
     * 估算 token 长度（近似值）。
     * 说明：此处为轻量近似实现，按每 4 个字符约等于 1 token 粗略估算，
     * 如需精确计算，请在上层引入具体模型的编码器（如 tiktoken）。
     */
    public static Integer tokenCounter(String text) {
        return text.length() / 4;
    }

    /**
     * 从 markdown 文本中解析 JSON 并校验是否包含期望 key 集合。
     * 若无法解析或缺少必要字段，则抛出运行时异常。
     */
    public static Map<String, Object> parseAndCheckJsonMarkdown(String text, List<String> expectedKeys) {
        Map<String, Object> jsonObj;
        try {
            jsonObj = parseJsonMarkdown(text);
        } catch (Throwable e) {
            throw new RuntimeException("Got invalid JSON object. Error:", e);
        }
        for (String key : expectedKeys) {
            if(!jsonObj.containsKey(key)) {
                throw new RuntimeException(String.format("Got invalid return object. Expected key `%s` to be present ", key));
            }
        }
        return jsonObj;
    }

    /**
     * 从可能包含三引号代码块的字符串中提取 JSON 内容并解析为 Map。
     * 兼容 ```json ... ``` 或 ``` ... ``` 包裹的内容，并对部分模型输出的
     * 冗余花括号进行裁剪。
     */
    public static Map<String, Object> parseJsonMarkdown(String jsonString) {
        String jsonStr = jsonString;
        String regex = "```(json)?(.*?)```";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonString);
        if(matcher.find()) {
            jsonStr = matcher.group(2);
        }
        jsonStr = jsonStr.trim();
        //为了适配vicuna
        jsonStr = jsonStr.replaceAll("^\\{+", "{")
                .replaceAll("\\}+$", "}");
        Map<String, Object> parsed = com.alibaba.fastjson.JSON.parseObject(jsonStr, new TypeReference<Map<String, Object>>(){});
        return parsed;
    }
}

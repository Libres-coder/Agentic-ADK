/**
 * Copyright (C) 2025 AIDC-AI
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

package com.alibaba.langengine.gitee.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Gitee API语言参数映射器
 * 将常见的语言名称映射为Gitee API接受的格式
 */
public class LanguageMapper {

    private static final Map<String, String> LANGUAGE_MAP = new HashMap<>();

    static {
        // Java相关
        LANGUAGE_MAP.put("java", "Java");
        LANGUAGE_MAP.put("JAVA", "Java");

        // JavaScript相关
        LANGUAGE_MAP.put("javascript", "JavaScript");
        LANGUAGE_MAP.put("JAVASCRIPT", "JavaScript");
        LANGUAGE_MAP.put("js", "JavaScript");

        // Python相关
        LANGUAGE_MAP.put("python", "Python");
        LANGUAGE_MAP.put("PYTHON", "Python");
        LANGUAGE_MAP.put("py", "Python");

        // C++相关
        LANGUAGE_MAP.put("c++", "C++");
        LANGUAGE_MAP.put("cpp", "C++");
        LANGUAGE_MAP.put("cxx", "C++");

        // C相关
        LANGUAGE_MAP.put("c", "C");

        // C#相关
        LANGUAGE_MAP.put("c#", "C#");
        LANGUAGE_MAP.put("csharp", "C#");

        // Go相关
        LANGUAGE_MAP.put("go", "Go");
        LANGUAGE_MAP.put("golang", "Go");

        // TypeScript相关
        LANGUAGE_MAP.put("typescript", "TypeScript");
        LANGUAGE_MAP.put("ts", "TypeScript");

        // PHP相关
        LANGUAGE_MAP.put("php", "PHP");

        // Ruby相关
        LANGUAGE_MAP.put("ruby", "Ruby");
        LANGUAGE_MAP.put("rb", "Ruby");

        // Rust相关
        LANGUAGE_MAP.put("rust", "Rust");
        LANGUAGE_MAP.put("rs", "Rust");

        // Swift相关
        LANGUAGE_MAP.put("swift", "Swift");

        // Kotlin相关
        LANGUAGE_MAP.put("kotlin", "Kotlin");
        LANGUAGE_MAP.put("kt", "Kotlin");

        // Scala相关
        LANGUAGE_MAP.put("scala", "Scala");

        // Shell相关
        LANGUAGE_MAP.put("shell", "Shell");
        LANGUAGE_MAP.put("bash", "Shell");
        LANGUAGE_MAP.put("sh", "Shell");

        // HTML/CSS相关
        LANGUAGE_MAP.put("html", "HTML");
        LANGUAGE_MAP.put("css", "CSS");

        // SQL相关
        LANGUAGE_MAP.put("sql", "SQL");

        // Objective-C相关
        LANGUAGE_MAP.put("objective-c", "Objective-C");
        LANGUAGE_MAP.put("objc", "Objective-C");

        // Dart相关
        LANGUAGE_MAP.put("dart", "Dart");

        // Lua相关
        LANGUAGE_MAP.put("lua", "Lua");

        // Perl相关
        LANGUAGE_MAP.put("perl", "Perl");

        // R相关
        LANGUAGE_MAP.put("r", "R");

        // MATLAB相关
        LANGUAGE_MAP.put("matlab", "MATLAB");

        // Vue相关
        LANGUAGE_MAP.put("vue", "Vue");
    }

    /**
     * 将输入的语言名称映射为Gitee API接受的格式
     *
     * @param language 输入的语言名称
     * @return 映射后的语言名称，如果没有找到映射则返回首字母大写的原值
     */
    public static String mapLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return null;
        }

        String trimmed = language.trim();

        // 先查找精确映射
        String mapped = LANGUAGE_MAP.get(trimmed.toLowerCase());
        if (mapped != null) {
            return mapped;
        }

        // 如果没找到映射，返回首字母大写的版本
        return capitalizeFirstLetter(trimmed);
    }

    /**
     * 首字母大写
     */
    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * 获取所有支持的语言映射
     */
    public static Map<String, String> getSupportedLanguages() {
        return new HashMap<>(LANGUAGE_MAP);
    }

    /**
     * 检查语言是否被支持
     */
    public static boolean isSupported(String language) {
        if (language == null || language.trim().isEmpty()) {
            return false;
        }
        return LANGUAGE_MAP.containsKey(language.trim().toLowerCase());
    }
}
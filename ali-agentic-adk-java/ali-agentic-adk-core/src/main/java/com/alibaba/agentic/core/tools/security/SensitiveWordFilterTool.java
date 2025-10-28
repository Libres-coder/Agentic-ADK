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
package com.alibaba.agentic.core.tools.security;

import com.alibaba.agentic.core.executor.SystemContext;
import com.alibaba.agentic.core.tools.BaseTool;
import io.reactivex.rxjava3.core.Flowable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Libres-coder
 * @date 2025/10/27
 */
public class SensitiveWordFilterTool implements BaseTool {

    private final Map<String, Object> sensitiveWordMap;
    private static final String END_FLAG = "END";
    
    public enum ReplaceStrategy {
        ASTERISK,
        DELETE,
        CUSTOM,
        DETECT_ONLY
    }

    public SensitiveWordFilterTool() {
        this(getDefaultSensitiveWords());
    }

    public SensitiveWordFilterTool(Set<String> sensitiveWords) {
        this.sensitiveWordMap = new ConcurrentHashMap<>();
        initSensitiveWordMap(sensitiveWords);
    }

    private void initSensitiveWordMap(Set<String> sensitiveWords) {
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return;
        }

        for (String word : sensitiveWords) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            
            Map<String, Object> currentMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                String key = String.valueOf(word.charAt(i));
                
                @SuppressWarnings("unchecked")
                Map<String, Object> nextMap = (Map<String, Object>) currentMap.get(key);
                
                if (nextMap == null) {
                    nextMap = new ConcurrentHashMap<>();
                    currentMap.put(key, nextMap);
                }
                
                currentMap = nextMap;
                
                if (i == word.length() - 1) {
                    currentMap.put(END_FLAG, word);
                }
            }
        }
    }

    @Override
    public String name() {
        return "sensitive_word_filter";
    }

    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        return Flowable.fromCallable(() -> {
            String text = (String) args.get("text");
            String strategyStr = (String) args.getOrDefault("strategy", "ASTERISK");
            String customReplace = (String) args.getOrDefault("custom_replace", "[已屏蔽]");
            
            if (text == null || text.isEmpty()) {
                return createResult(text, new ArrayList<>(), text, false);
            }

            ReplaceStrategy strategy;
            try {
                strategy = ReplaceStrategy.valueOf(strategyStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                strategy = ReplaceStrategy.ASTERISK;
            }

            List<SensitiveWordResult> detectedWords = detectSensitiveWords(text);
            String filteredText = filterText(text, detectedWords, strategy, customReplace);
            
            return createResult(text, detectedWords, filteredText, !detectedWords.isEmpty());
        });
    }

    private List<SensitiveWordResult> detectSensitiveWords(String text) {
        List<SensitiveWordResult> results = new ArrayList<>();
        
        for (int i = 0; i < text.length(); i++) {
            int length = checkSensitiveWord(text, i);
            if (length > 0) {
                String word = text.substring(i, i + length);
                results.add(new SensitiveWordResult(word, i, i + length));
                i += length - 1;
            }
        }
        
        return results;
    }

    private int checkSensitiveWord(String text, int startIndex) {
        int matchLength = 0;
        Map<String, Object> currentMap = sensitiveWordMap;
        
        for (int i = startIndex; i < text.length(); i++) {
            String key = String.valueOf(text.charAt(i));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> nextMap = (Map<String, Object>) currentMap.get(key);
            
            if (nextMap == null) {
                break;
            }
            
            matchLength++;
            
            if (nextMap.containsKey(END_FLAG)) {
                return matchLength;
            }
            
            currentMap = nextMap;
        }
        
        return 0;
    }

    private String filterText(String text, List<SensitiveWordResult> detectedWords,
                              ReplaceStrategy strategy, String customReplace) {
        if (strategy == ReplaceStrategy.DETECT_ONLY || detectedWords.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder(text);
        int offset = 0;

        for (SensitiveWordResult wordResult : detectedWords) {
            int start = wordResult.startIndex + offset;
            int end = wordResult.endIndex + offset;
            String replacement;

            switch (strategy) {
                case ASTERISK:
                    replacement = "*".repeat(wordResult.word.length());
                    break;
                case DELETE:
                    replacement = "";
                    break;
                case CUSTOM:
                    replacement = customReplace;
                    break;
                default:
                    replacement = wordResult.word;
            }

            result.replace(start, end, replacement);
            offset += replacement.length() - wordResult.word.length();
        }

        return result.toString();
    }

    private Map<String, Object> createResult(String originalText,
                                            List<SensitiveWordResult> detectedWords,
                                            String filteredText,
                                            boolean hasSensitiveWords) {
        Map<String, Object> result = new HashMap<>();
        result.put("original_text", originalText);
        result.put("filtered_text", filteredText);
        result.put("has_sensitive_words", hasSensitiveWords);
        result.put("detected_words_count", detectedWords.size());
        
        List<Map<String, Object>> wordsList = new ArrayList<>();
        for (SensitiveWordResult wordResult : detectedWords) {
            Map<String, Object> wordInfo = new HashMap<>();
            wordInfo.put("word", wordResult.word);
            wordInfo.put("start_index", wordResult.startIndex);
            wordInfo.put("end_index", wordResult.endIndex);
            wordsList.add(wordInfo);
        }
        result.put("detected_words", wordsList);
        
        return result;
    }

    public void addSensitiveWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            initSensitiveWordMap(Collections.singleton(word));
        }
    }

    public void addSensitiveWords(Set<String> words) {
        initSensitiveWordMap(words);
    }

    private static Set<String> getDefaultSensitiveWords() {
        Set<String> words = new HashSet<>();
        words.add("赌博");
        words.add("诈骗");
        words.add("色情");
        words.add("暴力");
        words.add("非法");
        words.add("枪支");
        words.add("毒品");
        words.add("恐怖");
        words.add("反动");
        words.add("邪教");
        return words;
    }

    private static class SensitiveWordResult {
        String word;
        int startIndex;
        int endIndex;

        SensitiveWordResult(String word, int startIndex, int endIndex) {
            this.word = word;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}

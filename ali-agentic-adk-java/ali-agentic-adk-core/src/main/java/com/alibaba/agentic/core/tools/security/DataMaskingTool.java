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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Libres-coder
 * @date 2025/10/27
 */
public class DataMaskingTool implements BaseTool {

    public enum PIIType {
        PHONE("phone", "1[3-9]\\d{9}", 3, 4),
        ID_CARD("id_card", "[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]", 6, 4),
        EMAIL("email", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", 2, -1),
        BANK_CARD("bank_card", "\\d{13,19}", 4, 4),
        IP_ADDRESS("ip_address", "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", -1, -1),
        PASSWORD("password", "(?i)(password|passwd|pwd)[\"']?\\s*[:=]\\s*[\"']?([^\"'\\s,}]+)", -1, -1);

        private final String type;
        private final String pattern;
        private final int prefixKeep;
        private final int suffixKeep;

        PIIType(String type, String pattern, int prefixKeep, int suffixKeep) {
            this.type = type;
            this.pattern = pattern;
            this.prefixKeep = prefixKeep;
            this.suffixKeep = suffixKeep;
        }

        public String getType() {
            return type;
        }

        public Pattern getPattern() {
            return Pattern.compile(pattern);
        }

        public int getPrefixKeep() {
            return prefixKeep;
        }

        public int getSuffixKeep() {
            return suffixKeep;
        }
    }

    @Override
    public String name() {
        return "data_masking";
    }

    @Override
    public Flowable<Map<String, Object>> run(Map<String, Object> args, SystemContext systemContext) {
        return Flowable.fromCallable(() -> {
            String text = (String) args.get("text");
            @SuppressWarnings("unchecked")
            List<String> enabledTypes = (List<String>) args.getOrDefault("types", Arrays.asList("all"));
            String maskChar = (String) args.getOrDefault("mask_char", "*");
            
            if (text == null || text.isEmpty()) {
                return createResult(text, text, new ArrayList<>());
            }

            Set<PIIType> typesToCheck = determineTypes(enabledTypes);
            MaskingResult result = maskText(text, typesToCheck, maskChar);
            
            return createResult(text, result.maskedText, result.detectedPII);
        });
    }

    private Set<PIIType> determineTypes(List<String> enabledTypes) {
        Set<PIIType> types = new HashSet<>();
        
        if (enabledTypes.contains("all")) {
            types.addAll(Arrays.asList(PIIType.values()));
        } else {
            for (String typeStr : enabledTypes) {
                for (PIIType type : PIIType.values()) {
                    if (type.getType().equalsIgnoreCase(typeStr)) {
                        types.add(type);
                        break;
                    }
                }
            }
        }
        
        return types;
    }

    private MaskingResult maskText(String text, Set<PIIType> types, String maskChar) {
        String maskedText = text;
        List<PIIDetection> detectedPII = new ArrayList<>();
        
        for (PIIType type : types) {
            Pattern pattern = type.getPattern();
            Matcher matcher = pattern.matcher(maskedText);
            
            StringBuffer sb = new StringBuffer();
            int offset = 0;
            
            while (matcher.find()) {
                String original = matcher.group();
                String masked;
                
                if (type == PIIType.PASSWORD && matcher.groupCount() >= 2) {
                    original = matcher.group(2);
                    masked = matcher.group(1) + matcher.group(0).substring(matcher.group(1).length()).replaceAll("[^\"':=\\s]", maskChar);
                } else {
                    masked = maskString(original, type.getPrefixKeep(), type.getSuffixKeep(), maskChar);
                }
                
                PIIDetection detection = new PIIDetection(
                    type.getType(),
                    original,
                    masked,
                    matcher.start(),
                    matcher.end()
                );
                detectedPII.add(detection);
                
                matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
            }
            matcher.appendTail(sb);
            maskedText = sb.toString();
        }
        
        return new MaskingResult(maskedText, detectedPII);
    }

    private String maskString(String str, int prefixKeep, int suffixKeep, String maskChar) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        int length = str.length();
        
        if (suffixKeep == -1 && str.contains("@")) {
            String[] parts = str.split("@");
            if (parts.length == 2) {
                String localPart = parts[0];
                int keepLength = Math.min(prefixKeep, localPart.length() / 2);
                String maskedLocal = localPart.substring(0, keepLength) + 
                                    maskChar.repeat(Math.max(localPart.length() - keepLength, 3));
                return maskedLocal + "@" + parts[1];
            }
        }
        
        if (prefixKeep == -1 && suffixKeep == -1) {
            return maskChar.repeat(Math.min(length, 10));
        }
        
        if (length <= prefixKeep + suffixKeep) {
            return maskChar.repeat(length);
        }
        
        String prefix = str.substring(0, prefixKeep);
        String suffix = str.substring(length - suffixKeep);
        int maskLength = length - prefixKeep - suffixKeep;
        
        return prefix + maskChar.repeat(maskLength) + suffix;
    }

    private Map<String, Object> createResult(String originalText, String maskedText, 
                                             List<PIIDetection> detectedPII) {
        Map<String, Object> result = new HashMap<>();
        result.put("original_text", originalText);
        result.put("masked_text", maskedText);
        result.put("has_pii", !detectedPII.isEmpty());
        result.put("pii_count", detectedPII.size());
        
        List<Map<String, Object>> piiList = new ArrayList<>();
        for (PIIDetection pii : detectedPII) {
            Map<String, Object> piiInfo = new HashMap<>();
            piiInfo.put("type", pii.type);
            piiInfo.put("original_value", pii.originalValue);
            piiInfo.put("masked_value", pii.maskedValue);
            piiInfo.put("start_index", pii.startIndex);
            piiInfo.put("end_index", pii.endIndex);
            piiList.add(piiInfo);
        }
        result.put("detected_pii", piiList);
        
        return result;
    }

    private static class MaskingResult {
        String maskedText;
        List<PIIDetection> detectedPII;

        MaskingResult(String maskedText, List<PIIDetection> detectedPII) {
            this.maskedText = maskedText;
            this.detectedPII = detectedPII;
        }
    }

    private static class PIIDetection {
        String type;
        String originalValue;
        String maskedValue;
        int startIndex;
        int endIndex;

        PIIDetection(String type, String originalValue, String maskedValue, 
                     int startIndex, int endIndex) {
            this.type = type;
            this.originalValue = originalValue;
            this.maskedValue = maskedValue;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
    }
}

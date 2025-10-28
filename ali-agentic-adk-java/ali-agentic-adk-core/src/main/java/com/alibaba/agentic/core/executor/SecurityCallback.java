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
package com.alibaba.agentic.core.executor;

import com.alibaba.agentic.core.tools.security.DataMaskingTool;
import com.alibaba.agentic.core.tools.security.SensitiveWordFilterTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Security callback for automated security checks.
 *
 * @author Libres-coder
 * @date 2025/10/27
 */
public class SecurityCallback implements Callback {

    private static final Logger logger = LoggerFactory.getLogger(SecurityCallback.class);

    private boolean enableSensitiveWordFilter = true;
    private boolean enableDataMasking = true;
    private boolean blockOnSensitiveWord = false;
    private boolean maskLogs = true;
    
    private final SensitiveWordFilterTool sensitiveWordFilter;
    private final DataMaskingTool dataMaskingTool;
    
    private final List<SecurityEvent> securityEvents = new ArrayList<>();

    public SecurityCallback() {
        this.sensitiveWordFilter = new SensitiveWordFilterTool();
        this.dataMaskingTool = new DataMaskingTool();
    }

    public SecurityCallback(Set<String> customSensitiveWords) {
        this.sensitiveWordFilter = new SensitiveWordFilterTool(customSensitiveWords);
        this.dataMaskingTool = new DataMaskingTool();
    }

    @Override
    public void execute(SystemContext systemContext, Request request, Result result, CallbackChain chain) {
        logger.debug("[SecurityCallback] Executing security checks before agent execution");
        
        try {
            if (request != null && request.getParam() != null) {
                checkSecurity(request.getParam(), "REQUEST", systemContext);
            }
            
            chain.execute(systemContext, request, result);
            
        } catch (SecurityException e) {
            logger.error("[SecurityCallback] Security violation detected: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void receive(SystemContext systemContext, Request request, Result result, CallbackChain chain) {
        logger.debug("[SecurityCallback] Executing security checks after agent execution");
        
        try {
            if (result != null && result.getData() != null) {
                checkSecurity(result.getData(), "RESPONSE", systemContext);
            }
            
            chain.receive(systemContext, request, result);
            
        } catch (SecurityException e) {
            logger.error("[SecurityCallback] Security violation in response: {}", e.getMessage());
            throw e;
        }
    }

    private void checkSecurity(Object payload, String stage, SystemContext systemContext) {
        if (payload == null) {
            return;
        }

        String content = extractTextContent(payload);
        if (content == null || content.isEmpty()) {
            return;
        }

        if (enableSensitiveWordFilter) {
            Map<String, Object> filterArgs = new HashMap<>();
            filterArgs.put("text", content);
            filterArgs.put("strategy", "DETECT_ONLY");
            
            try {
                Map<String, Object> filterResult = sensitiveWordFilter.run(filterArgs, systemContext)
                    .blockingFirst();
                
                Boolean hasSensitiveWords = (Boolean) filterResult.get("has_sensitive_words");
                if (Boolean.TRUE.equals(hasSensitiveWords)) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> detectedWords = 
                        (List<Map<String, Object>>) filterResult.get("detected_words");
                    
                    SecurityEvent event = new SecurityEvent(
                        SecurityEventType.SENSITIVE_WORD_DETECTED,
                        stage,
                        "Detected " + detectedWords.size() + " sensitive word(s)",
                        detectedWords
                    );
                    securityEvents.add(event);
                    
                    logger.warn("[SecurityCallback] {} - Sensitive words detected: {}", 
                               stage, detectedWords.size());
                    
                    if (blockOnSensitiveWord) {
                        SecurityException secEx = new SecurityException(
                            "Sensitive word detected in " + stage + ". Request blocked.");
                        logger.error("[SecurityCallback] {}", secEx.getMessage());
                        throw secEx;
                    }
                }
            } catch (SecurityException e) {
                throw e;
            } catch (Exception e) {
                logger.error("[SecurityCallback] Error during sensitive word filter: {}", e.getMessage());
            }
        }

        if (enableDataMasking) {
            Map<String, Object> maskArgs = new HashMap<>();
            maskArgs.put("text", content);
            maskArgs.put("types", Arrays.asList("all"));
            
            try {
                Map<String, Object> maskResult = dataMaskingTool.run(maskArgs, systemContext)
                    .blockingFirst();
                
                Boolean hasPII = (Boolean) maskResult.get("has_pii");
                if (Boolean.TRUE.equals(hasPII)) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> detectedPII = 
                        (List<Map<String, Object>>) maskResult.get("detected_pii");
                    
                    SecurityEvent event = new SecurityEvent(
                        SecurityEventType.PII_DETECTED,
                        stage,
                        "Detected " + detectedPII.size() + " PII item(s)",
                        detectedPII
                    );
                    securityEvents.add(event);
                    
                    logger.warn("[SecurityCallback] {} - PII detected: {}", stage, detectedPII.size());
                    
                    if (maskLogs) {
                        String maskedText = (String) maskResult.get("masked_text");
                        logger.debug("[SecurityCallback] Masked content: {}", maskedText);
                    }
                }
            } catch (Exception e) {
                logger.error("[SecurityCallback] Error during data masking: {}", e.getMessage());
            }
        }
    }

    private String extractTextContent(Object payload) {
        if (payload instanceof String) {
            return (String) payload;
        } else if (payload instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) payload;
            
            for (String key : Arrays.asList("text", "content", "message", "prompt", "response")) {
                Object value = map.get(key);
                if (value instanceof String) {
                    return (String) value;
                }
            }
            
            return map.toString();
        } else {
            return payload.toString();
        }
    }

    public SecurityCallback enableSensitiveWordFilter(boolean enable) {
        this.enableSensitiveWordFilter = enable;
        return this;
    }

    public SecurityCallback enableDataMasking(boolean enable) {
        this.enableDataMasking = enable;
        return this;
    }

    public SecurityCallback setBlockOnSensitiveWord(boolean block) {
        this.blockOnSensitiveWord = block;
        return this;
    }

    public SecurityCallback setMaskLogs(boolean mask) {
        this.maskLogs = mask;
        return this;
    }

    public SecurityCallback addSensitiveWord(String word) {
        this.sensitiveWordFilter.addSensitiveWord(word);
        return this;
    }

    public SecurityCallback addSensitiveWords(Set<String> words) {
        this.sensitiveWordFilter.addSensitiveWords(words);
        return this;
    }

    public List<SecurityEvent> getSecurityEvents() {
        return new ArrayList<>(securityEvents);
    }

    public void clearSecurityEvents() {
        securityEvents.clear();
    }

    public enum SecurityEventType {
        SENSITIVE_WORD_DETECTED,
        PII_DETECTED,
        SECURITY_VIOLATION
    }

    public static class SecurityEvent {
        private final SecurityEventType type;
        private final String stage;
        private final String message;
        private final List<Map<String, Object>> details;
        private final long timestamp;

        public SecurityEvent(SecurityEventType type, String stage, String message, 
                           List<Map<String, Object>> details) {
            this.type = type;
            this.stage = stage;
            this.message = message;
            this.details = details;
            this.timestamp = System.currentTimeMillis();
        }

        public SecurityEventType getType() {
            return type;
        }

        public String getStage() {
            return stage;
        }

        public String getMessage() {
            return message;
        }

        public List<Map<String, Object>> getDetails() {
            return details;
        }

        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s - %s: %s", 
                new Date(timestamp), type, stage, message);
        }
    }
}

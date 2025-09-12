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
package com.alibaba.langengine.youdaotranslate.tools;

import com.alibaba.langengine.youdaotranslate.YoudaoTranslateConfiguration;
import com.alibaba.langengine.youdaotranslate.model.YoudaoTranslateRequest;
import com.alibaba.langengine.youdaotranslate.model.YoudaoTranslateResponse;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.youdaotranslate.YoudaoTranslateConfiguration.*;

/**
 * 有道翻译工具
 *
 * @author Makoto
 */
@Slf4j
@Data
public class YoudaoTranslateTool extends DefaultTool {

    private static final String DEFAULT_FROM = "auto";
    private static final String DEFAULT_TO = "zh-CHS";
    private static final String SIGN_TYPE = "v3";
    private static final String SOURCE = "youdao";
    private static final String TYPE = "text";
    private static final String STRICT = "1";
    private static final String VOCAB_ID = "0";

    private String from = DEFAULT_FROM;
    private String to = DEFAULT_TO;
    private String appKey;
    private String secretKey;
    private Integer timeout;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    public YoudaoTranslateTool() {
        this.appKey = YOUDAO_TRANSLATE_APP_ID;
        this.secretKey = YOUDAO_TRANSLATE_SECRET_KEY;
        this.timeout = Integer.parseInt(YOUDAO_TRANSLATE_TIMEOUT);
        init();
    }

    public YoudaoTranslateTool(String appKey, String secretKey, Integer timeout) {
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.timeout = timeout;
        init();
    }

    private void init() {
        setName("YoudaoTranslateTool");
        setDescription("有道翻译工具，支持多种语言之间的互译，提供详细的词典释义和音标信息");
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("有道翻译输入: {}", toolInput);
        
        if (StringUtils.isBlank(toolInput)) {
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译文本不能为空")
                    .build();
        }

        try {
            YoudaoTranslateResponse response = translate(toolInput);
            
            if (!"0".equals(response.getErrorCode())) {
                return ToolExecuteResult.builder()
                        .success(false)
                        .error("翻译失败: " + response.getErrorMsg())
                        .build();
            }

            String translatedText = response.getTranslation().get(0);
            log.info("有道翻译结果: {}", translatedText);
            
            return ToolExecuteResult.builder()
                    .success(true)
                    .result(translatedText)
                    .build();
                    
        } catch (Exception e) {
            log.error("有道翻译异常", e);
            return ToolExecuteResult.builder()
                    .success(false)
                    .error("翻译异常: " + e.getMessage())
                    .build();
        }
    }

    private YoudaoTranslateResponse translate(String text) throws IOException {
        String salt = String.valueOf(System.currentTimeMillis());
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateSign(text, salt, curtime);

        YoudaoTranslateRequest request = YoudaoTranslateRequest.builder()
                .q(text)
                .from(from)
                .to(to)
                .appKey(appKey)
                .salt(salt)
                .sign(sign)
                .signType(SIGN_TYPE)
                .curtime(curtime)
                .source(SOURCE)
                .type(TYPE)
                .strict(STRICT)
                .vocabId(VOCAB_ID)
                .build();

        String url = buildUrl(request);
        Request httpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP请求失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, YoudaoTranslateResponse.class);
        }
    }

    private String buildUrl(YoudaoTranslateRequest request) {
        return String.format("%s?q=%s&from=%s&to=%s&appKey=%s&salt=%s&sign=%s&signType=%s&curtime=%s&source=%s&type=%s&strict=%s&vocabId=%s",
                YOUDAO_TRANSLATE_SERVER_URL,
                request.getQ(),
                request.getFrom(),
                request.getTo(),
                request.getAppKey(),
                request.getSalt(),
                request.getSign(),
                request.getSignType(),
                request.getCurtime(),
                request.getSource(),
                request.getType(),
                request.getStrict(),
                request.getVocabId());
    }

    private String generateSign(String text, String salt, String curtime) {
        String input = appKey + text + salt + curtime + secretKey;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }
} 
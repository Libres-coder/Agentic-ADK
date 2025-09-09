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
package com.alibaba.langengine.baidutranslate.tools;

import com.alibaba.langengine.baidutranslate.model.BaiduTranslateRequest;
import com.alibaba.langengine.baidutranslate.model.BaiduTranslateResponse;
import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.baidutranslate.BaiduTranslateConfiguration.*;

/**
 * 百度翻译工具
 *
 * @author aihe.ah
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class BaiduTranslateTool extends DefaultTool {

    private static final String DEFAULT_FROM = "auto";
    private static final String DEFAULT_TO = "zh";
    private static final String SIGN_TYPE = "v3";
    private static final String SOURCE = "baidu";

    @Setter
    private String from = DEFAULT_FROM;
    @Setter
    private String to = DEFAULT_TO;
    private String appId;
    private String secretKey;
    private Integer timeout;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;

    public BaiduTranslateTool(String appId, String secretKey, Integer timeout) {
        this.appId = appId;
        this.secretKey = secretKey;
        this.timeout = timeout;
        init();
    }

    private void init() {
        setName("BaiduTranslateTool");
        setDescription("百度翻译工具，支持多种语言之间的互译，包括中文、英文、日文、韩文等");
        
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ToolExecuteResult run(String toolInput) {
        log.info("百度翻译输入: {}", toolInput);
        
        if (StringUtils.isBlank(toolInput)) {
            return new ToolExecuteResult("翻译文本不能为空");
        }

        try {
            BaiduTranslateResponse response = translate(toolInput);
            
            if (response.getErrorCode() != null) {
                return new ToolExecuteResult("翻译失败: " + response.getErrorMsg());
            }

            String translatedText = response.getTransResult().get(0).getDst();
            log.info("百度翻译结果: {}", translatedText);
            
            return new ToolExecuteResult(translatedText);

        } catch (Exception e) {
            log.error("百度翻译异常", e);
            return new ToolExecuteResult("翻译异常: " + e.getMessage());
        }
    }

    private BaiduTranslateResponse translate(String text) throws IOException {
        String salt = String.valueOf(System.currentTimeMillis());
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateSign(text, salt, curtime);

        BaiduTranslateRequest request = BaiduTranslateRequest.builder()
                .q(text)
                .from(from)
                .to(to)
                .appid(appId)
                .salt(salt)
                .sign(sign)
                .signType(SIGN_TYPE)
                .curtime(curtime)
                .source(SOURCE)
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
            
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("响应体为空");
            }
            String responseStr = responseBody.string();
            return objectMapper.readValue(responseStr, BaiduTranslateResponse.class);
        }
    }

    private String buildUrl(BaiduTranslateRequest request) {
        return String.format("%s?q=%s&from=%s&to=%s&appid=%s&salt=%s&sign=%s&signType=%s&curtime=%s&source=%s",
                BAIDU_TRANSLATE_SERVER_URL,
                request.getQ(),
                request.getFrom(),
                request.getTo(),
                request.getAppid(),
                request.getSalt(),
                request.getSign(),
                request.getSignType(),
                request.getCurtime(),
                request.getSource());
    }

    private String generateSign(String text, String salt, String curtime) {
        String input = appId + text + salt + curtime + secretKey;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }
}

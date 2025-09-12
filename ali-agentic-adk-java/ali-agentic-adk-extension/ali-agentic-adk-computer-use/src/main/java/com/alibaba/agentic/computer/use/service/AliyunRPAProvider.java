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
package com.alibaba.agentic.computer.use.service;

import com.alibaba.agentic.computer.use.configuration.AdkBrowserUseProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AliyunRPAProvider {

    @Autowired
    private AdkBrowserUseProperties adkBrowserUseProperties;

    private OkHttpClient okHttpClient;

    private static final String SIGNATURE_METHOD = "HMAC-SHA1";
    private static final String SIGNATURE_VERSION = "1.0";
    private static final String VERSION = "20200430";
    private static final String DATA_FORMAT = "json";

    @PostConstruct
    public void init() {
        // 创建一个连接池，最大空闲连接数为10个，保持连接时间为5分钟
        ConnectionPool connectionPool = new ConnectionPool(10, 5, TimeUnit.MINUTES);
        okHttpClient = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
    }

    public String getAuthCode(String robotId) {
        String path = "https://console-rpa.aliyun.com/rpa/openapi/raas/resource/GetAuthCode";

        String signatureNonce = getRandomString(26);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = df.format(new Date());

        Map<String, String> params = new TreeMap<>();
        params.put("AccessKeyId", adkBrowserUseProperties.getAk());
        params.put("Format", DATA_FORMAT);
        params.put("SignatureMethod", SIGNATURE_METHOD);
        params.put("SignatureNonce", signatureNonce);
        params.put("SignatureVersion", SIGNATURE_VERSION);
        params.put("Timestamp", timestamp);
        params.put("Version", VERSION);
        params.put("RobotId", robotId);

        String signature = sign("GET", params);
        params.put("Signature", signature);

        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            canonicalizedQueryString.append("&")
                    .append(percentEncode(entry.getKey()))
                    .append("=")
                    .append(percentEncode(entry.getValue()));
        }
        String queryString = canonicalizedQueryString.toString().substring(1); // remove first "&"

        Request request = new Request.Builder()
                .url(path + "?" + queryString)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                Assert.notNull(responseBody, "response body is null");
                String invokeResult = responseBody.string();
                return invokeResult;
            } else {
                return null;
            }
        } catch (Throwable e) {
            log.error("getTaskDetails error", e);
            throw new RuntimeException(e);
        }
    }

    public String getRobot(String desktopId) {
        String path = "https://console-rpa.aliyun.com/rpa/openapi/raas/resource/ListRobots";

        String signatureNonce = getRandomString(26);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = df.format(new Date());

        Map<String, String> params = new TreeMap<>();
        params.put("AccessKeyId", adkBrowserUseProperties.getAk());

        params.put("Format", DATA_FORMAT);
        params.put("SignatureMethod", SIGNATURE_METHOD);
        params.put("SignatureNonce", signatureNonce);
        params.put("SignatureVersion", SIGNATURE_VERSION);
        params.put("Timestamp", timestamp);
        params.put("Version", VERSION);
        params.put("MachineInstanceId", desktopId);

        String signature = sign("GET", params);

        params.put("Signature", signature);

        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            canonicalizedQueryString.append("&")
                    .append(percentEncode(entry.getKey()))
                    .append("=")
                    .append(percentEncode(entry.getValue()));
        }
        String queryString = canonicalizedQueryString.toString().substring(1); // remove first "&"

        Request request = new Request.Builder()
                .url(path + "?" + queryString)
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                Assert.notNull(responseBody, "response body is null");
                String invokeResult = responseBody.string();
                return invokeResult;
            } else {
                return null;
            }
        } catch (Throwable e) {
            log.error("getTaskDetails error", e);
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        AliyunRPAProvider provider = new AliyunRPAProvider();
        provider.init();

        String robotId = "28EED96F467CADFD091152732483CAD5";
        String authCode = provider.getAuthCode(robotId);
        System.out.println(authCode);
    }

    private String sign(String method, Map<String, String> params) {
        try {
            StringBuilder canonicalizedQueryString = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                canonicalizedQueryString.append("&")
                        .append(percentEncode(entry.getKey()))
                        .append("=")
                        .append(percentEncode(entry.getValue()));
            }
            String queryString = canonicalizedQueryString.toString().substring(1); // remove first "&"

            String stringToSign = method + "&" + percentEncode("/") + "&" + percentEncode(queryString);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec((adkBrowserUseProperties.getSk() + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signatureBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static String percentEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8")
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    private static String getRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}

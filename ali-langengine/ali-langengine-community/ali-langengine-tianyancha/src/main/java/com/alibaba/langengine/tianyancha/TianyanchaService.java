package com.alibaba.langengine.tianyancha;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.tianyancha.model.CompanyBasicInfoResponse;
import com.alibaba.langengine.tianyancha.model.CompanyHolderResponse;
import com.alibaba.langengine.tianyancha.model.CompanyVerifyResponse;
import com.alibaba.langengine.tianyancha.model.SpecialCompanyInfoResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import static com.alibaba.langengine.tianyancha.TianyanchaConfiguration.TIANYANCHA_API_TOKEN;
import static com.alibaba.langengine.tianyancha.TianyanchaConfiguration.TIANYANCHA_BASE_URL;
import static com.alibaba.langengine.tianyancha.TianyanchaConstant.*;
/**
 * Tianyancha Service
 * 
 * This class provides methods for interacting with the Tianyancha API.
*/
public class TianyanchaService {
    
    private final String apiToken;
    private final String baseUrl;
    private final OkHttpClient client;
    
    public TianyanchaService(String apiToken) {
        this.apiToken = apiToken;
        this.baseUrl = TIANYANCHA_BASE_URL;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }
    
    public TianyanchaService() {
        this.apiToken = TIANYANCHA_API_TOKEN;
        this.baseUrl = TIANYANCHA_BASE_URL;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    public CompanyBasicInfoResponse getCompanyBasicInfo(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = baseUrl + BASIC_INFO_ENDPOINT + "?keyword=" + encodedKeyword;
            String response = doGet(url);
            return JSON.parseObject(response, CompanyBasicInfoResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("获取企业基本信息失败", e);
        }
    }

    public SpecialCompanyInfoResponse getSpecialCompanyInfo(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = baseUrl + SPECIAL_INFO_ENDPOINT + "?keyword=" + encodedKeyword;
            String response = doGet(url);
            return JSON.parseObject(response, SpecialCompanyInfoResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("获取特殊企业信息失败", e);
        }
    }

    public CompanyHolderResponse getCompanyHolders(String keyword) {
        return getCompanyHolders(keyword, DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE);
    }

    public CompanyHolderResponse getCompanyHolders(String keyword, int pageNum, int pageSize) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = baseUrl + HOLDER_ENDPOINT + "?keyword=" + encodedKeyword 
                    + "&pageNum=" + pageNum + "&pageSize=" + pageSize + "&source=1";
            String response = doGet(url);
            return JSON.parseObject(response, CompanyHolderResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("获取企业股东信息失败", e);
        }
    }

    public CompanyVerifyResponse verifyCompanyInfo(String code, String name, String legalPersonName) {
        try {
            String encodedCode = URLEncoder.encode(code, "UTF-8");
            String encodedName = URLEncoder.encode(name, "UTF-8");
            String encodedLegalPerson = URLEncoder.encode(legalPersonName, "UTF-8");
            String url = baseUrl + VERIFY_ENDPOINT + "?code=" + encodedCode 
                    + "&name=" + encodedName + "&legalPersonName=" + encodedLegalPerson;
            String response = doGet(url);
            return JSON.parseObject(response, CompanyVerifyResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("企业三要素验证失败", e);
        }
    }

    private String doGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", apiToken)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code() + " " + response.message());
            }
            
            if (response.body() != null) {
                return response.body().string();
            } else {
                throw new IOException("响应体为空");
            }
        }
    }
}
package com.alibaba.langengine.tencent.cos.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TencentCosPresignedUploadTool extends BaseTool {

    public TencentCosPresignedUploadTool() {
        setName("TencentCos.get_presigned_url_upload");
        setDescription("生成对象上传(PUT)的预签名 URL");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"secret_id\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"scheme\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"expires_seconds\": {\"type\": \"integer\", \"default\": 600},\n" +
                "    \"content_type\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"secret_id\", \"secret_key\", \"region\", \"bucket\", \"key\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        COSClient client = null;
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String secretId = (String) args.get("secret_id");
            String secretKey = (String) args.get("secret_key");
            String region = (String) args.get("region");
            String scheme = (String) args.getOrDefault("scheme", "https");
            String bucket = (String) args.get("bucket");
            String key = (String) args.get("key");
            String contentType = (String) args.get("content_type");
            Integer expires = args.get("expires_seconds") == null ? 600 : Integer.valueOf(String.valueOf(args.get("expires_seconds")));
            if (expires < 60) expires = 60;
            if (expires > 7 * 24 * 3600) expires = 7 * 24 * 3600;

            if (secretId == null || secretKey == null || region == null || bucket == null || key == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket/key");
            }

            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            clientConfig.setHttpProtocol("http".equalsIgnoreCase(scheme) ? HttpProtocol.http : HttpProtocol.https);
            client = new COSClient(cred, clientConfig);

            Date expiration = new Date(System.currentTimeMillis() + expires * 1000L);
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, key, HttpMethodName.PUT);
            req.setExpiration(expiration);
            if (contentType != null) {
                req.addRequestHeader("Content-Type", contentType);
            }
            URL url = client.generatePresignedUrl(req);

            Map<String, Object> data = new HashMap<>();
            data.put("url", url.toString());
            data.put("expires_at", expiration.getTime() / 1000);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("TencentCos.get_presigned_url_upload failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}

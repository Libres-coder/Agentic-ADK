package com.alibaba.langengine.tencent.cos.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TencentCosPutObjectTextTool extends BaseTool {

    public TencentCosPutObjectTextTool() {
        setName("TencentCos.put_object_text");
        setDescription("上传文本内容到对象");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"secret_id\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"scheme\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"content\": {\"type\": \"string\"},\n" +
                "    \"content_type\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"secret_id\", \"secret_key\", \"region\", \"bucket\", \"key\", \"content\"]\n" +
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
            String content = (String) args.get("content");
            String contentType = (String) args.get("content_type");

            if (secretId == null || secretKey == null || region == null || bucket == null || key == null || content == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket/key/content");
            }
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            if (bytes.length > 1024 * 1024) {
                return ToolExecuteResult.fail("TooLarge", "content exceeds 1MB, please use presigned upload");
            }

            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            clientConfig.setHttpProtocol("http".equalsIgnoreCase(scheme) ? HttpProtocol.http : HttpProtocol.https);
            client = new COSClient(cred, clientConfig);

            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(bytes.length);
            meta.setContentType(contentType != null ? contentType : "text/plain; charset=utf-8");

            client.putObject(bucket, key, new ByteArrayInputStream(bytes), meta);

            Map<String, Object> data = new HashMap<>();
            data.put("bucket", bucket);
            data.put("key", key);
            data.put("size", bytes.length);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}

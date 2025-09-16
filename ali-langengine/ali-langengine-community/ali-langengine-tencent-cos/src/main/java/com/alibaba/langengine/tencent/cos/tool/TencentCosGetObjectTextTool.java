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
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TencentCosGetObjectTextTool extends BaseTool {

    public TencentCosGetObjectTextTool() {
        setName("TencentCos.get_object_text");
        setDescription("下载对象文本内容（小文件）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"secret_id\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"scheme\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"}\n" +
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

            if (secretId == null || secretKey == null || region == null || bucket == null || key == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket/key");
            }

            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            clientConfig.setHttpProtocol("http".equalsIgnoreCase(scheme) ? HttpProtocol.http : HttpProtocol.https);
            client = new COSClient(cred, clientConfig);

            COSObject obj = client.getObject(bucket, key);
            try (InputStream in = obj.getObjectContent(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int n;
                int maxBytes = 1024 * 1024;
                int total = 0;
                while ((n = in.read(buf)) > 0) {
                    total += n;
                    if (total > maxBytes) {
                        return ToolExecuteResult.fail("TooLarge", "object exceeds 1MB, please use presigned download");
                    }
                    out.write(buf, 0, n);
                }
                String text = new String(out.toByteArray(), StandardCharsets.UTF_8);
                Map<String, Object> data = new HashMap<>();
                data.put("content", text);
                data.put("size", total);
                return ToolExecuteResult.success(data);
            }
        } catch (Exception e) {
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}

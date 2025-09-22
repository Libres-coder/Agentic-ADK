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
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TencentCosDeleteObjectTool extends BaseTool {

    public TencentCosDeleteObjectTool() {
        setName("TencentCos.delete_object");
        setDescription("删除对象");
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

            client.deleteObject(bucket, key);

            Map<String, Object> data = new HashMap<>();
            data.put("deleted", true);
            data.put("bucket", bucket);
            data.put("key", key);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}

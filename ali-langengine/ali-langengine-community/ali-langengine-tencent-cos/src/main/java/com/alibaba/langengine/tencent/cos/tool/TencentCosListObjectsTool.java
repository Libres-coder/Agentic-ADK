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
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.S3ObjectSummary;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TencentCosListObjectsTool extends BaseTool {

    public TencentCosListObjectsTool() {
        setName("TencentCos.list_objects");
        setDescription("列出指定 bucket 的对象，可选 prefix/delimiter/marker/max_keys");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"secret_id\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"region\": {\"type\": \"string\"},\n" +
                "    \"scheme\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"prefix\": {\"type\": \"string\"},\n" +
                "    \"delimiter\": {\"type\": \"string\"},\n" +
                "    \"marker\": {\"type\": \"string\"},\n" +
                "    \"max_keys\": {\"type\": \"integer\"}\n" +
                "  },\n" +
                "  \"required\": [\"secret_id\", \"secret_key\", \"region\", \"bucket\"]\n" +
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
            String prefix = (String) args.getOrDefault("prefix", null);
            String delimiter = (String) args.getOrDefault("delimiter", null);
            String marker = (String) args.getOrDefault("marker", null);
            Integer maxKeys = args.get("max_keys") == null ? null : Integer.valueOf(String.valueOf(args.get("max_keys")));

            if (secretId == null || secretKey == null || region == null || bucket == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket");
            }

            COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            clientConfig.setHttpProtocol("http".equalsIgnoreCase(scheme) ? HttpProtocol.http : HttpProtocol.https);
            client = new COSClient(cred, clientConfig);

            ListObjectsRequest req = new ListObjectsRequest();
            req.setBucketName(bucket);
            if (prefix != null) req.setPrefix(prefix);
            if (delimiter != null) req.setDelimiter(delimiter);
            if (marker != null) req.setMarker(marker);
            if (maxKeys != null) req.setMaxKeys(maxKeys);

            ObjectListing listing = client.listObjects(req);
            List<Map<String, Object>> objects = listing.getObjectSummaries().stream().map(s -> {
                Map<String, Object> o = new HashMap<>();
                o.put("key", s.getKey());
                o.put("size", s.getSize());
                o.put("etag", s.getETag());
                o.put("last_modified", s.getLastModified());
                return o;
            }).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("objects", objects);
            data.put("next_marker", listing.getNextMarker());
            data.put("is_truncated", listing.isTruncated());
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("TencentCos.list_objects failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        } finally {
            if (client != null) client.shutdown();
        }
    }
}

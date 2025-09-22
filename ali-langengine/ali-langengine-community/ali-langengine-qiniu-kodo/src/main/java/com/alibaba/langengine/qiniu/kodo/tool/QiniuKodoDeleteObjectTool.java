package com.alibaba.langengine.qiniu.kodo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QiniuKodoDeleteObjectTool extends BaseTool {

    public QiniuKodoDeleteObjectTool() {
        setName("QiniuKodo.delete_object");
        setDescription("删除对象");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"bucket_region\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key\", \"secret_key\", \"bucket_region\", \"bucket\", \"key\"]\n" +
                "}");
    }

    private Region resolveRegion(String code) {
        if (code == null) return Region.autoRegion();
        switch (code) {
            case "z0": return Region.huadong();
            case "z1": return Region.huabei();
            case "z2": return Region.huanan();
            case "na0": return Region.beimei();
            case "as0": return Region.xinjiapo();
            default: return Region.autoRegion();
        }
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String ak = (String) args.get("access_key");
            String sk = (String) args.get("secret_key");
            String region = (String) args.get("bucket_region");
            String bucket = (String) args.get("bucket");
            String key = (String) args.get("key");

            if (ak == null || sk == null || region == null || bucket == null || key == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket/key");
            }

            Auth auth = Auth.create(ak, sk);
            Configuration cfg = new Configuration(resolveRegion(region));
            BucketManager bucketManager = new BucketManager(auth, cfg);
            bucketManager.delete(bucket, key);

            Map<String, Object> data = new HashMap<>();
            data.put("deleted", true);
            data.put("bucket", bucket);
            data.put("key", key);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("QiniuKodo.delete_object failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        }
    }
}

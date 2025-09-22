package com.alibaba.langengine.qiniu.kodo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class QiniuKodoListObjectsTool extends BaseTool {

    public QiniuKodoListObjectsTool() {
        setName("QiniuKodo.list_objects");
        setDescription("列出指定 bucket 的对象，支持 prefix/marker/limit");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"bucket_region\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"prefix\": {\"type\": \"string\"},\n" +
                "    \"marker\": {\"type\": \"string\"},\n" +
                "    \"limit\": {\"type\": \"integer\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key\", \"secret_key\", \"bucket_region\", \"bucket\"]\n" +
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
            String prefix = (String) args.getOrDefault("prefix", null);
            String marker = (String) args.getOrDefault("marker", null);
            Integer limit = args.get("limit") == null ? 1000 : Integer.valueOf(String.valueOf(args.get("limit")));

            if (ak == null || sk == null || region == null || bucket == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket");
            }

            Auth auth = Auth.create(ak, sk);
            Configuration cfg = new Configuration(resolveRegion(region));
            BucketManager bucketManager = new BucketManager(auth, cfg);

            BucketManager.FileListIterator it = bucketManager.createFileListIterator(bucket, prefix, limit, marker);
            List<Map<String, Object>> objects = new ArrayList<>();
            String nextMarker = null;
            if (it.hasNext()) {
                com.qiniu.storage.model.FileInfo[] items = it.next();
                for (com.qiniu.storage.model.FileInfo f : items) {
                    Map<String, Object> o = new HashMap<>();
                    o.put("key", f.key);
                    o.put("size", f.fsize);
                    o.put("etag", f.hash);
                    o.put("last_modified", f.putTime);
                    objects.add(o);
                }
                nextMarker = it.marker();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("objects", objects);
            data.put("next_marker", nextMarker);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("QiniuKodo.list_objects failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        }
    }
}

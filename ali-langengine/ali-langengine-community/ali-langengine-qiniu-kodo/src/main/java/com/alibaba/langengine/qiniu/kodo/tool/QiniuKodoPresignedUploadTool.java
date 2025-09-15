package com.alibaba.langengine.qiniu.kodo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QiniuKodoPresignedUploadTool extends BaseTool {

    public QiniuKodoPresignedUploadTool() {
        setName("QiniuKodo.get_presigned_url_upload");
        setDescription("生成对象上传的授权（返回 upload_url 与 upload_token）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"bucket_region\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"expires_seconds\": {\"type\": \"integer\", \"default\": 600},\n" +
                "    \"content_type\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key\", \"secret_key\", \"bucket\", \"bucket_region\", \"key\"]\n" +
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
            String bucket = (String) args.get("bucket");
            String region = (String) args.get("bucket_region");
            String key = (String) args.get("key");
            Integer expires = args.get("expires_seconds") == null ? 600 : Integer.valueOf(String.valueOf(args.get("expires_seconds")));
            if (expires < 60) expires = 60;
            if (expires > 7 * 24 * 3600) expires = 7 * 24 * 3600;

            if (ak == null || sk == null || bucket == null || region == null || key == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or bucket/key");
            }

            Auth auth = Auth.create(ak, sk);
            String upToken = auth.uploadToken(bucket, key, expires, null);

            // 七牛根据区域有不同上传域名，这里给出常见自动区域与华东示例
            String uploadUrl;
            switch (region) {
                case "z1": uploadUrl = "https://upload-z1.qiniup.com"; break;
                case "z2": uploadUrl = "https://upload-z2.qiniup.com"; break;
                case "na0": uploadUrl = "https://upload-na0.qiniup.com"; break;
                case "as0": uploadUrl = "https://upload-as0.qiniup.com"; break;
                case "z0":
                default:
                    uploadUrl = "https://upload.qiniup.com";
            }

            Map<String, Object> data = new HashMap<>();
            data.put("upload_url", uploadUrl);
            data.put("upload_token", upToken);
            data.put("expires_in", expires);
            data.put("key", key);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("QiniuKodo.get_presigned_url_upload failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        }
    }
}

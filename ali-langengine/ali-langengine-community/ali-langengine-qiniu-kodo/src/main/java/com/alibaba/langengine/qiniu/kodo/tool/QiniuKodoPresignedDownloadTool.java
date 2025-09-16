package com.alibaba.langengine.qiniu.kodo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QiniuKodoPresignedDownloadTool extends BaseTool {

    public QiniuKodoPresignedDownloadTool() {
        setName("QiniuKodo.get_presigned_url_download");
        setDescription("生成对象下载的预签名 URL（私有空间需授权）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"bucket_domain\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"expires_seconds\": {\"type\": \"integer\", \"default\": 600},\n" +
                "    \"force_download\": {\"type\": \"boolean\"},\n" +
                "    \"download_filename\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key\", \"secret_key\", \"bucket_domain\", \"bucket\", \"key\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String ak = (String) args.get("access_key");
            String sk = (String) args.get("secret_key");
            String domain = (String) args.get("bucket_domain");
            String key = (String) args.get("key");
            Integer expires = args.get("expires_seconds") == null ? 600 : Integer.valueOf(String.valueOf(args.get("expires_seconds")));
            Boolean forceDownload = args.get("force_download") == null ? null : Boolean.valueOf(String.valueOf(args.get("force_download")));
            String downloadFilename = (String) args.get("download_filename");

            if (expires < 60) expires = 60;
            if (expires > 7 * 24 * 3600) expires = 7 * 24 * 3600;

            if (ak == null || sk == null || domain == null || key == null) {
                return ToolExecuteResult.fail("InvalidArgument", "missing required credentials or domain/key");
            }

            String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.name()).replace("+", "%20");
            String baseUrl = String.format("http://%s/%s", domain, encodedKey);
            if (Boolean.TRUE.equals(forceDownload) && downloadFilename != null) {
                baseUrl = baseUrl + "?attname=" + URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8.name());
            }

            Auth auth = Auth.create(ak, sk);
            String url = auth.privateDownloadUrl(baseUrl, expires);

            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("expires_in", expires);
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("QiniuKodo.get_presigned_url_download failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        }
    }
}

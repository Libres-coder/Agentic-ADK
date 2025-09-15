package com.alibaba.langengine.qiniu.kodo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QiniuKodoPutObjectTextTool extends BaseTool {

    public QiniuKodoPutObjectTextTool() {
        setName("QiniuKodo.put_object_text");
        setDescription("上传文本内容到对象（建议配合 get_presigned_url_upload/UploadToken 使用）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"access_key\": {\"type\": \"string\"},\n" +
                "    \"secret_key\": {\"type\": \"string\"},\n" +
                "    \"bucket\": {\"type\": \"string\"},\n" +
                "    \"bucket_region\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"},\n" +
                "    \"content\": {\"type\": \"string\"},\n" +
                "    \"content_type\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"access_key\", \"secret_key\", \"bucket\", \"bucket_region\", \"key\", \"content\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String content = (String) args.get("content");
            if (content == null) {
                return ToolExecuteResult.fail("InvalidArgument", "content is required");
            }
            if (content.getBytes(StandardCharsets.UTF_8).length > 1024 * 1024) {
                return ToolExecuteResult.fail("TooLarge", "content exceeds 1MB, please use presigned upload");
            }
            // 为简化与安全，建议使用 presigned upload（upload_token）在客户端上传
            Map<String, Object> data = new HashMap<>();
            data.put("hint", "Use QiniuKodo.get_presigned_url_upload to obtain upload_token and upload via client.");
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            log.error("QiniuKodo.put_object_text failed", e);
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        }
    }
}

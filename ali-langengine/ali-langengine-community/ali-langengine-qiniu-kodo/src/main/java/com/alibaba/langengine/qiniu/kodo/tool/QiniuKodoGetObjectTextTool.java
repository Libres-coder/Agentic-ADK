package com.alibaba.langengine.qiniu.kodo.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.BaseTool;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class QiniuKodoGetObjectTextTool extends BaseTool {

    public QiniuKodoGetObjectTextTool() {
        setName("QiniuKodo.get_object_text");
        setDescription("下载对象文本内容（建议使用 get_presigned_url_download 获取带鉴权链接）");
        setParameters("{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"bucket_domain\": {\"type\": \"string\"},\n" +
                "    \"key\": {\"type\": \"string\"}\n" +
                "  },\n" +
                "  \"required\": [\"bucket_domain\", \"key\"]\n" +
                "}");
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        try {
            Map<String, Object> args = JSON.parseObject(toolInput, Map.class);
            String domain = (String) args.get("bucket_domain");
            String key = (String) args.get("key");
            if (domain == null || key == null) {
                return ToolExecuteResult.fail("InvalidArgument", "bucket_domain and key are required");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("hint", "Use QiniuKodo.get_presigned_url_download to get an authorized URL then fetch content.");
            return ToolExecuteResult.success(data);
        } catch (Exception e) {
            return ToolExecuteResult.fail("InternalError", e.getMessage());
        }
    }
}

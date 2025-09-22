package com.alibaba.langengine.tencent.cos.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.alibaba.langengine.tencent.cos.tool.TencentCosListObjectsTool;
import com.alibaba.langengine.tencent.cos.tool.TencentCosPresignedDownloadTool;

import java.util.HashMap;
import java.util.Map;

public class TencentCosExample {
    private static String env(String k) { return System.getenv(k); }

    public static void main(String[] args) {
        String sid = env("TENCENT_SID");
        String sk = env("TENCENT_SK");
        String region = env("TENCENT_REGION");
        String bucket = env("TENCENT_BUCKET");
        String key = env("TENCENT_KEY");

        Map<String, Object> listArgs = new HashMap<>();
        listArgs.put("secret_id", sid);
        listArgs.put("secret_key", sk);
        listArgs.put("region", region);
        listArgs.put("bucket", bucket);
        listArgs.put("max_keys", 10);
        ToolExecuteResult listRes = new TencentCosListObjectsTool().run(JSON.toJSONString(listArgs), new ExecutionContext());
        System.out.println("list_objects => " + (listRes.isSuccess() ? listRes.getData() : listRes.getMessage()));

        if (key != null) {
            Map<String, Object> dlArgs = new HashMap<>();
            dlArgs.put("secret_id", sid);
            dlArgs.put("secret_key", sk);
            dlArgs.put("region", region);
            dlArgs.put("bucket", bucket);
            dlArgs.put("key", key);
            ToolExecuteResult dlRes = new TencentCosPresignedDownloadTool().run(JSON.toJSONString(dlArgs), new ExecutionContext());
            System.out.println("presigned_download => " + (dlRes.isSuccess() ? dlRes.getData() : dlRes.getMessage()));
        }
    }
}

package com.alibaba.langengine.qiniu.kodo.examples;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.alibaba.langengine.qiniu.kodo.tool.QiniuKodoListObjectsTool;
import com.alibaba.langengine.qiniu.kodo.tool.QiniuKodoPresignedDownloadTool;

import java.util.HashMap;
import java.util.Map;

public class QiniuKodoExample {
    private static String env(String k) { return System.getenv(k); }

    public static void main(String[] args) {
        String ak = env("QINIU_AK");
        String sk = env("QINIU_SK");
        String region = env("QINIU_REGION");
        String bucket = env("QINIU_BUCKET");
        String domain = env("QINIU_DOMAIN");
        String key = env("QINIU_KEY");

        Map<String, Object> listArgs = new HashMap<>();
        listArgs.put("access_key", ak);
        listArgs.put("secret_key", sk);
        listArgs.put("bucket_region", region);
        listArgs.put("bucket", bucket);
        listArgs.put("limit", 10);
        ToolExecuteResult listRes = new QiniuKodoListObjectsTool().run(JSON.toJSONString(listArgs), new ExecutionContext());
        System.out.println("list_objects => " + (listRes.isSuccess() ? listRes.getData() : listRes.getMessage()));

        if (key != null) {
            Map<String, Object> dlArgs = new HashMap<>();
            dlArgs.put("access_key", ak);
            dlArgs.put("secret_key", sk);
            dlArgs.put("bucket_domain", domain);
            dlArgs.put("bucket", bucket);
            dlArgs.put("key", key);
            ToolExecuteResult dlRes = new QiniuKodoPresignedDownloadTool().run(JSON.toJSONString(dlArgs), new ExecutionContext());
            System.out.println("presigned_download => " + (dlRes.isSuccess() ? dlRes.getData() : dlRes.getMessage()));
        }
    }
}

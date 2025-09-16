package com.alibaba.langengine.qiniu.kodo;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.alibaba.langengine.qiniu.kodo.tool.QiniuKodoListObjectsTool;
import com.alibaba.langengine.qiniu.kodo.tool.QiniuKodoPresignedDownloadTool;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class QiniuKodoToolsTest {

    private static String env(String key) { return System.getenv(key); }

    @Test
    void testListObjects() {
        String ak = env("QINIU_AK");
        String sk = env("QINIU_SK");
        String region = env("QINIU_REGION");
        String bucket = env("QINIU_BUCKET");
        Assumptions.assumeTrue(ak != null && sk != null && region != null && bucket != null,
                "skip: env QINIU_AK/SK/REGION/BUCKET not set");

        Map<String, Object> args = new HashMap<>();
        args.put("access_key", ak);
        args.put("secret_key", sk);
        args.put("bucket_region", region);
        args.put("bucket", bucket);
        args.put("limit", 10);

        ToolExecuteResult res = new QiniuKodoListObjectsTool().run(JSON.toJSONString(args), new ExecutionContext());
        assertNotNull(res);
        assertTrue(res.isSuccess(), "list_objects should succeed: " + res.getMessage());
    }

    @Test
    void testPresignedDownload() {
        String ak = env("QINIU_AK");
        String sk = env("QINIU_SK");
        String domain = env("QINIU_DOMAIN");
        String bucket = env("QINIU_BUCKET");
        String key = env("QINIU_KEY");
        Assumptions.assumeTrue(ak != null && sk != null && domain != null && bucket != null && key != null,
                "skip: env QINIU_AK/SK/DOMAIN/BUCKET/KEY not set");

        Map<String, Object> args = new HashMap<>();
        args.put("access_key", ak);
        args.put("secret_key", sk);
        args.put("bucket_domain", domain);
        args.put("bucket", bucket);
        args.put("key", key);

        ToolExecuteResult res = new QiniuKodoPresignedDownloadTool().run(JSON.toJSONString(args), new ExecutionContext());
        assertNotNull(res);
        assertTrue(res.isSuccess(), "presigned_download should succeed: " + res.getMessage());
        assertTrue(String.valueOf(res.getData()).contains("http"));
    }
}

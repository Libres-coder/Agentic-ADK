package com.alibaba.langengine.tencent.cos;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.ExecutionContext;
import com.alibaba.langengine.core.model.ToolExecuteResult;
import com.alibaba.langengine.tencent.cos.tool.TencentCosListObjectsTool;
import com.alibaba.langengine.tencent.cos.tool.TencentCosPresignedDownloadTool;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TencentCosToolsTest {

    private static String env(String key) { return System.getenv(key); }

    @Test
    void testListObjects() {
        String sid = env("TENCENT_SID");
        String sk = env("TENCENT_SK");
        String region = env("TENCENT_REGION");
        String bucket = env("TENCENT_BUCKET");
        Assumptions.assumeTrue(sid != null && sk != null && region != null && bucket != null,
                "skip: env TENCENT_SID/SK/REGION/BUCKET not set");

        Map<String, Object> args = new HashMap<>();
        args.put("secret_id", sid);
        args.put("secret_key", sk);
        args.put("region", region);
        args.put("bucket", bucket);
        args.put("max_keys", 10);

        ToolExecuteResult res = new TencentCosListObjectsTool().run(JSON.toJSONString(args), new ExecutionContext());
        assertNotNull(res);
        assertTrue(res.isSuccess(), "list_objects should succeed: " + res.getMessage());
    }

    @Test
    void testPresignedDownload() {
        String sid = env("TENCENT_SID");
        String sk = env("TENCENT_SK");
        String region = env("TENCENT_REGION");
        String bucket = env("TENCENT_BUCKET");
        String key = env("TENCENT_KEY");
        Assumptions.assumeTrue(sid != null && sk != null && region != null && bucket != null && key != null,
                "skip: env TENCENT_SID/SK/REGION/BUCKET/KEY not set");

        Map<String, Object> args = new HashMap<>();
        args.put("secret_id", sid);
        args.put("secret_key", sk);
        args.put("region", region);
        args.put("bucket", bucket);
        args.put("key", key);
        args.put("expires_seconds", 300);

        ToolExecuteResult res = new TencentCosPresignedDownloadTool().run(JSON.toJSONString(args), new ExecutionContext());
        assertNotNull(res);
        assertTrue(res.isSuccess(), "presigned_download should succeed: " + res.getMessage());
        assertTrue(String.valueOf(res.getData()).contains("http"));
    }
}

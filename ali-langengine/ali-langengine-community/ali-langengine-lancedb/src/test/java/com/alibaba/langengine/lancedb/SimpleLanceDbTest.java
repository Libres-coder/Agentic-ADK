package com.alibaba.langengine.lancedb;

import com.alibaba.langengine.lancedb.client.LanceDbClient;
import com.alibaba.langengine.lancedb.model.LanceDbVector;
import com.alibaba.langengine.lancedb.vectorstore.LanceDbParam;
import com.alibaba.langengine.lancedb.vectorstore.LanceDbService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;


public class SimpleLanceDbTest {

    @Test
    public void testConfigurationCreation() {
        // 测试配置创建
        LanceDbConfiguration config = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .tableName("test-table")
                .maxRetries(3)
                .build();
        
        assert config.getBaseUrl().equals("http://localhost:8080");
        assert config.getTableName().equals("test-table");
        assert config.getMaxRetries() == 3;
        System.out.println("✅ Configuration creation test passed");
    }

    @Test
    public void testVectorCreation() {
        // 测试向量创建
        LanceDbVector vector = LanceDbVector.builder()
                .id("test-1")
                .vector(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f))
                .content("Test content")
                .build();
        
        assert vector.getId().equals("test-1");
        assert vector.getVector().size() == 4;
        assert vector.getContent().equals("Test content");
        System.out.println("✅ Vector creation test passed");
    }

    @Test
    public void testParameterCreation() {
        // 测试参数创建
        LanceDbParam param = LanceDbParam.builder()
                .configuration(LanceDbConfiguration.builder()
                        .baseUrl("http://localhost:8080")
                        .tableName("test-table")
                        .build())
                .build();
        
        assert param.getConfiguration() != null;
        assert param.getConfiguration().getBaseUrl().equals("http://localhost:8080");
        assert param.getConfiguration().getTableName().equals("test-table");
        System.out.println("✅ Parameter creation test passed");
    }

    @Test
    public void testExceptionHierarchy() {
        // 测试异常层次结构
        try {
            throw new LanceDbClientException("TEST_ERROR", "Test error message");
        } catch (LanceDbException e) {
            assert e.getErrorCode().equals("TEST_ERROR");
            assert e.getMessage().contains("Test error message");
            System.out.println("✅ Exception hierarchy test passed");
        }
    }

    @Test
    public void testServiceInstantiation() {
        // 测试服务实例化
        LanceDbConfiguration config = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .tableName("test-table")
                .build();
        
        LanceDbClient client = new LanceDbClient(config);
        LanceDbService service = new LanceDbService(client);
        
        assert client != null;
        assert service != null;
        System.out.println("✅ Service instantiation test passed");
    }
}

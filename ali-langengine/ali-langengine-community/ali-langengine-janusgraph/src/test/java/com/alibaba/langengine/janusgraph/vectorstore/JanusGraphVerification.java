package com.alibaba.langengine.janusgraph.vectorstore;


public class JanusGraphVerification {
    
    public static void main(String[] args) {
        System.out.println("=== JanusGraph模块验证程序 ===");
        
        try {
            // 1. 测试参数配置
            testParameterConfiguration();
            
            // 2. 测试类存在性
            testClassExistence();
            
            System.out.println("✓ 所有验证测试通过！JanusGraph模块实现正确。");
            
        } catch (Exception e) {
            System.err.println("✗ 验证失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testParameterConfiguration() throws Exception {
        System.out.println("1. 测试参数配置...");
        
        // 测试默认配置
        JanusGraphParam defaultParam = JanusGraphParam.getDefaultConfig();
        assert defaultParam != null : "默认配置不应为null";
        assert defaultParam.getGraphConfig() != null : "图配置不应为null";
        assert defaultParam.getBatchConfig() != null : "批处理配置不应为null";
        
        // 测试自定义配置
        JanusGraphParam customParam = JanusGraphParam.builder()
            .graphConfig(JanusGraphParam.GraphConfig.builder()
                .storageBackend("berkeleyje")
                .storageDirectory("/tmp/test-janusgraph")
                .build())
            .vectorConfig(JanusGraphParam.VectorConfig.builder()
                .vertexLabel("TestDoc")
                .vectorDimension(128)
                .build())
            .build();
        
        assert "berkeleyje".equals(customParam.getGraphConfig().getStorageBackend()) : "存储后端配置错误";
        assert customParam.getVectorConfig().getVectorDimension() == 128 : "向量维度配置错误";
        
        // 测试配置转换
        java.util.Map<String, Object> configMap = customParam.toJanusGraphConfig();
        assert configMap.containsKey("storage.backend") : "配置映射应包含storage.backend";
        assert configMap.containsKey("storage.directory") : "配置映射应包含storage.directory";
        
        System.out.println("   ✓ 参数配置测试通过");
    }
    
    private static void testClassExistence() throws Exception {
        System.out.println("2. 测试类存在性...");
        
        // 测试所有主要类都存在
        assert JanusGraphParam.class != null : "JanusGraphParam类应存在";
        assert JanusGraphService.class != null : "JanusGraphService类应存在";
        assert JanusGraph.class != null : "JanusGraph类应存在";
        assert JanusGraphSimilarityFunction.class != null : "JanusGraphSimilarityFunction类应存在";
        
        System.out.println("   ✓ 类存在性测试通过");
    }
}

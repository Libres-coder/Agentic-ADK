package com.alibaba.langengine.wenxin.model;

import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WenxinEmbeddingModel 测试类
 */
public class WenxinEmbeddingModelTest {

    private WenxinEmbeddingModel embeddingModel;
    private static final String TEST_API_KEY = "test_api_key";
    private static final String TEST_SECRET_KEY = "test_secret_key";

    @BeforeEach
    public void setUp() {
        embeddingModel = new WenxinEmbeddingModel(TEST_API_KEY, TEST_SECRET_KEY);
    }

    @Test
    public void testModelInitialization() {
        assertNotNull(embeddingModel, "WenxinEmbeddingModel should be successfully initialized");
        assertEquals(TEST_API_KEY, embeddingModel.getApiKey(), "API key should be set correctly");
        assertEquals(TEST_SECRET_KEY, embeddingModel.getSecretKey(), "Secret key should be set correctly");
    }

    @Test
    public void testModelImplementsEmbeddings() {
        assertTrue(embeddingModel instanceof Embeddings, 
                  "WenxinEmbeddingModel should implement Embeddings interface");
    }

    @Test
    public void testModelConfiguration() {
        // 测试模型配置
        embeddingModel.setModel("embedding-v1");
        assertEquals("embedding-v1", embeddingModel.getModel(), "Model should be set correctly");
    }

    @Test
    public void testDocumentCreation() {
        // 测试文档创建
        Document doc = new Document();
        doc.setPageContent("测试文档内容");
        
        assertNotNull(doc, "Document should be created");
        assertEquals("测试文档内容", doc.getPageContent(), "Document content should be set correctly");
    }

    @Test
    public void testEmbeddingMethodsExist() {
        // 验证必要的方法是否存在
        try {
            Class<?> modelClass = WenxinEmbeddingModel.class;
            assertTrue(modelClass.getDeclaredMethods().length > 0, "Model should have methods");
            
            // 验证类实现了Embeddings接口 - 修改为更宽松的检查
            boolean implementsEmbeddings = Embeddings.class.isAssignableFrom(modelClass);
            assertTrue(implementsEmbeddings, "Model should implement Embeddings interface");
        } catch (Exception e) {
            fail("Model class should be accessible: " + e.getMessage());
        }
    }

    @Test
    public void testModelPropertySetters() {
        // 测试属性设置器
        embeddingModel.setServerUrl("https://test.example.com/");
        assertEquals("https://test.example.com/", embeddingModel.getServerUrl(), "Server URL should be set correctly");
        
        embeddingModel.setTimeout(java.time.Duration.ofSeconds(30));
        assertEquals(java.time.Duration.ofSeconds(30), embeddingModel.getTimeout(), "Timeout should be set correctly");
    }

    @Test
    public void testModelServiceInitialization() {
        // 测试服务初始化
        assertNotNull(embeddingModel.getWenxinService(), "Wenxin service should be initialized");
    }

    @Test
    public void testModelDefaultConfiguration() {
        // 测试默认配置
        assertNotNull(embeddingModel.getModel(), "Default model should be set");
        assertNotNull(embeddingModel.getServerUrl(), "Default server URL should be set");
        assertTrue(embeddingModel.getTimeout().getSeconds() > 0, "Default timeout should be positive");
    }
}
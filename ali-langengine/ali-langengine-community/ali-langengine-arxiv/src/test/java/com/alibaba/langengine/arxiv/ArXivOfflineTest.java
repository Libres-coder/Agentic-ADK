package com.alibaba.langengine.arxiv;

import com.alibaba.langengine.arxiv.model.ArXivPaper;
import com.alibaba.langengine.arxiv.model.ArXivSearchRequest;
import com.alibaba.langengine.arxiv.model.ArXivSearchResponse;
import com.alibaba.langengine.arxiv.service.ArXivApiService;
import com.alibaba.langengine.arxiv.sdk.ArXivException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class ArXivOfflineTest {

    @Mock
    private ArXivApiService mockApiService;
    
    private ArXivLLM arXivLLM;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        arXivLLM = new ArXivLLM();
        arXivLLM.setApiService(mockApiService);
        arXivLLM.setMaxResults(10);
        objectMapper = new ObjectMapper();
    }

    /**
     * 创建模拟论文对象
     */
    private ArXivPaper createMockPaper(String arxivId, String title) {
        ArXivPaper paper = new ArXivPaper();
        paper.setId(arxivId);
        paper.setTitle(title);
        paper.setSummary("Mock summary for " + title);
        paper.setAuthors(Arrays.asList("Author One", "Author Two"));
        paper.setPublished(LocalDateTime.now());
        paper.setPdfUrl("http://arxiv.org/pdf/" + arxivId + ".pdf");
        paper.setArxivUrl("http://arxiv.org/abs/" + arxivId);
        paper.setPrimaryCategory("cs.AI");
        return paper;
    }

    /**
     * 测试基本搜索功能
     */
    @Test
    public void testArXivSearchWithMockService() throws Exception {
        // 设置Mock响应
        List<ArXivPaper> mockPapers = Arrays.asList(
            createMockPaper("2301.12345", "Deep Learning Advances"),
            createMockPaper("2301.67890", "Machine Learning Applications")
        );
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setPapers(mockPapers);
        mockResponse.setTotalResults(2);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenReturn(mockResponse);

        // 执行测试
        String result = arXivLLM.run("machine learning", null, null);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("Deep Learning Advances"));
        
        // 验证Mock调用
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }

    /**
     * 测试空查询处理
     */
    @Test
    public void testEmptyQueryHandling() throws Exception {
        String result = arXivLLM.run("", null, null);
        
        assertNotNull(result);
        assertTrue(result.contains("搜索参数错误"));
        
        // 验证没有调用API服务
        verifyNoInteractions(mockApiService);
    }

    /**
     * 测试null查询处理
     */
    @Test
    public void testNullQueryHandling() throws Exception {
        String result = arXivLLM.run(null, null, null);
        
        assertNotNull(result);
        assertTrue(result.contains("搜索参数错误"));
        
        // 验证没有调用API服务
        verifyNoInteractions(mockApiService);
    }

    /**
     * 测试空白字符查询处理
     */
    @Test
    public void testWhitespaceQueryHandling() throws Exception {
        String result = arXivLLM.run("   ", null, null);
        
        assertNotNull(result);
        assertTrue(result.contains("搜索参数错误"));
        
        // 验证没有调用API服务
        verifyNoInteractions(mockApiService);
    }

    /**
     * 测试空搜索结果
     */
    @Test
    public void testEmptySearchResults() throws Exception {
        // 设置空结果响应
        ArXivSearchResponse emptyResponse = new ArXivSearchResponse();
        emptyResponse.setPapers(Collections.emptyList());
        emptyResponse.setTotalResults(0);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenReturn(emptyResponse);

        String result = arXivLLM.run("nonexistent topic", null, null);

        assertNotNull(result);
        assertTrue(result.contains("未找到相关论文"));
        
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }

    /**
     * 测试JSON查询解析
     */
    @Test
    public void testJSONQueryParsing() throws Exception {
        // 设置Mock响应
        List<ArXivPaper> mockPapers = Arrays.asList(
            createMockPaper("2301.12345", "Test Paper")
        );
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setPapers(mockPapers);
        mockResponse.setTotalResults(1);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenReturn(mockResponse);

        String jsonQuery = "{\"query\":\"test topic\",\"maxResults\":5}";
        String result = arXivLLM.run(jsonQuery, null, null);

        assertNotNull(result);
        assertTrue(result.contains("Test Paper"));
        
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }

    /**
     * 测试日期范围查询解析
     */
    @Test
    public void testDateRangeQueryParsing() throws Exception {
        // 设置Mock响应
        List<ArXivPaper> mockPapers = Arrays.asList(
            createMockPaper("2301.12345", "Recent Paper")
        );
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setPapers(mockPapers);
        mockResponse.setTotalResults(1);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenReturn(mockResponse);

        String result = arXivLLM.run("machine learning 2023年的", null, null);

        assertNotNull(result);
        assertTrue(result.contains("Recent Paper"));
        
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }

    /**
     * 测试中文查询
     */
    @Test
    public void testChineseQuery() throws Exception {
        // 设置Mock响应
        List<ArXivPaper> mockPapers = Arrays.asList(
            createMockPaper("2301.12345", "Machine Learning Paper")
        );
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setPapers(mockPapers);
        mockResponse.setTotalResults(1);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenReturn(mockResponse);

        String result = arXivLLM.run("搜索机器学习论文", null, null);

        assertNotNull(result);
        assertTrue(result.contains("Machine Learning Paper"));
        
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }

    /**
     * 测试作者搜索
     */
    @Test
    public void testAuthorSearch() throws Exception {
        // 设置Mock响应
        List<ArXivPaper> mockPapers = Arrays.asList(
            createMockPaper("2301.12345", "Paper by Smith")
        );
        ArXivSearchResponse mockResponse = new ArXivSearchResponse();
        mockResponse.setPapers(mockPapers);
        mockResponse.setTotalResults(1);
        
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenReturn(mockResponse);

        String result = arXivLLM.run("find papers by Smith", null, null);

        assertNotNull(result);
        assertTrue(result.contains("Paper by Smith"));
        
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }

    /**
     * 测试API服务异常处理
     */
    @Test
    public void testApiServiceException() throws Exception {
        when(mockApiService.searchPapers(any(ArXivSearchRequest.class)))
            .thenThrow(new ArXivException("API Error"));

        String result = arXivLLM.run("test query", null, null);
        
        assertTrue(result.contains("ArXiv 搜索失败: API Error"));
        
        verify(mockApiService, times(1)).searchPapers(any(ArXivSearchRequest.class));
    }
}

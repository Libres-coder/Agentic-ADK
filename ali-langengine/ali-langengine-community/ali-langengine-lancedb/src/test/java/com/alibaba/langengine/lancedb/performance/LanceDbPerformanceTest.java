/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.lancedb.performance;

import com.alibaba.langengine.core.docloader.Document;
import com.alibaba.langengine.core.embeddings.Embeddings;
import com.alibaba.langengine.lancedb.LanceDbConfiguration;
import com.alibaba.langengine.lancedb.LanceDbParam;
import com.alibaba.langengine.lancedb.LanceDbVectorStore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInfo;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("LanceDB性能测试")
@Disabled("性能测试，按需执行")
class LanceDbPerformanceTest {

    private LanceDbVectorStore vectorStore;
    private PerformanceEmbeddings embeddings;
    private PerformanceMetrics metrics;

    @BeforeEach
    void setUp() {
        LanceDbConfiguration configuration = LanceDbConfiguration.builder()
                .baseUrl("http://localhost:8080")
                .apiKey("test-key")
                .maxRequestsPerSecond(100)
                .connectTimeoutMs(30000)
                .readTimeoutMs(60000)
                .poolMaxConnections(20)
                .poolKeepAliveDurationMs(300000)
                .build();

        LanceDbParam param = LanceDbParam.builder()
                .tableName("performance_test_" + System.currentTimeMillis())
                .dimension(256) // 更大的维度用于性能测试
                .metric("cosine")
                .build();

        vectorStore = new LanceDbVectorStore(configuration, param);
        embeddings = new PerformanceEmbeddings(256);
        vectorStore.setEmbeddings(embeddings);
        
        metrics = new PerformanceMetrics();
    }

    @Test
    @DisplayName("批量插入性能测试")
    void testBatchInsertPerformance(TestInfo testInfo) throws Exception {
        System.out.println("开始批量插入性能测试: " + testInfo.getDisplayName());
        
        int[] batchSizes = {100, 500, 1000, 2000};
        int documentsPerBatch = 1000;
        
        for (int batchSize : batchSizes) {
            vectorStore.setBatchSize(batchSize);
            
            List<Document> documents = generateDocuments(documentsPerBatch);
            
            long startTime = System.nanoTime();
            List<String> ids = vectorStore.addDocuments(documents);
            long endTime = System.nanoTime();
            
            double durationMs = (endTime - startTime) / 1_000_000.0;
            double throughput = documentsPerBatch / (durationMs / 1000.0);
            
            metrics.recordInsertPerformance(batchSize, documentsPerBatch, durationMs, throughput);
            
            assertEquals(documentsPerBatch, ids.size());
            
            System.out.printf("批次大小: %d, 文档数: %d, 耗时: %.2f ms, 吞吐量: %.2f docs/sec%n",
                    batchSize, documentsPerBatch, durationMs, throughput);
            
            // 清理数据
            vectorStore.clear();
            Thread.sleep(1000);
        }
        
        metrics.printInsertPerformanceReport();
    }

    @Test
    @DisplayName("查询性能测试")
    void testQueryPerformance(TestInfo testInfo) throws Exception {
        System.out.println("开始查询性能测试: " + testInfo.getDisplayName());
        
        // 准备测试数据
        int totalDocuments = 10000;
        List<Document> documents = generateDocuments(totalDocuments);
        vectorStore.setBatchSize(1000);
        vectorStore.addDocuments(documents);
        
        Thread.sleep(5000); // 等待索引完成
        
        int[] topKValues = {1, 5, 10, 20, 50, 100};
        int queryCount = 100;
        
        for (int topK : topKValues) {
            List<Double> queryTimes = new ArrayList<>();
            
            for (int i = 0; i < queryCount; i++) {
                String query = "performance test query " + i;
                
                long startTime = System.nanoTime();
                List<Document> results = vectorStore.similaritySearch(query, topK);
                long endTime = System.nanoTime();
                
                double queryTimeMs = (endTime - startTime) / 1_000_000.0;
                queryTimes.add(queryTimeMs);
                
                assertTrue(results.size() <= topK);
            }
            
            double avgQueryTime = queryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double p95QueryTime = calculatePercentile(queryTimes, 95);
            double qps = 1000.0 / avgQueryTime;
            
            metrics.recordQueryPerformance(topK, avgQueryTime, p95QueryTime, qps);
            
            System.out.printf("TopK: %d, 平均查询时间: %.2f ms, P95: %.2f ms, QPS: %.2f%n",
                    topK, avgQueryTime, p95QueryTime, qps);
        }
        
        metrics.printQueryPerformanceReport();
    }

    @Test
    @DisplayName("并发性能测试")
    void testConcurrentPerformance(TestInfo testInfo) throws Exception {
        System.out.println("开始并发性能测试: " + testInfo.getDisplayName());
        
        // 准备测试数据
        int totalDocuments = 5000;
        List<Document> documents = generateDocuments(totalDocuments);
        vectorStore.addDocuments(documents);
        
        Thread.sleep(3000);
        
        int[] threadCounts = {1, 2, 4, 8, 16};
        int queriesPerThread = 50;
        
        for (int threadCount : threadCounts) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Future<List<Double>>> futures = new ArrayList<>();
            
            long startTime = System.nanoTime();
            
            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                Future<List<Double>> future = executor.submit(() -> {
                    List<Double> threadQueryTimes = new ArrayList<>();
                    try {
                        for (int q = 0; q < queriesPerThread; q++) {
                            String query = "concurrent test query " + threadId + "_" + q;
                            
                            long queryStart = System.nanoTime();
                            vectorStore.similaritySearch(query, 10);
                            long queryEnd = System.nanoTime();
                            
                            threadQueryTimes.add((queryEnd - queryStart) / 1_000_000.0);
                        }
                    } finally {
                        latch.countDown();
                    }
                    return threadQueryTimes;
                });
                futures.add(future);
            }
            
            latch.await();
            long endTime = System.nanoTime();
            
            double totalTimeMs = (endTime - startTime) / 1_000_000.0;
            double totalQueries = threadCount * queriesPerThread;
            double overallQps = totalQueries / (totalTimeMs / 1000.0);
            
            // 收集所有查询时间
            List<Double> allQueryTimes = new ArrayList<>();
            for (Future<List<Double>> future : futures) {
                allQueryTimes.addAll(future.get());
            }
            
            double avgQueryTime = allQueryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double p95QueryTime = calculatePercentile(allQueryTimes, 95);
            
            metrics.recordConcurrentPerformance(threadCount, (int)totalQueries, totalTimeMs, overallQps, avgQueryTime, p95QueryTime);
            
            System.out.printf("线程数: %d, 总查询数: %.0f, 总耗时: %.2f ms, 整体QPS: %.2f, 平均查询时间: %.2f ms, P95: %.2f ms%n",
                    threadCount, totalQueries, totalTimeMs, overallQps, avgQueryTime, p95QueryTime);
            
            executor.shutdown();
        }
        
        metrics.printConcurrentPerformanceReport();
    }

    @Test
    @DisplayName("内存使用测试")
    void testMemoryUsage(TestInfo testInfo) throws Exception {
        System.out.println("开始内存使用测试: " + testInfo.getDisplayName());
        
        Runtime runtime = Runtime.getRuntime();
        
        // 初始内存状态
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        int[] documentCounts = {1000, 5000, 10000, 20000};
        
        for (int docCount : documentCounts) {
            System.gc();
            long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
            
            List<Document> documents = generateDocuments(docCount);
            vectorStore.addDocuments(documents);
            
            System.gc();
            long afterMemory = runtime.totalMemory() - runtime.freeMemory();
            
            long memoryUsed = afterMemory - beforeMemory;
            double memoryPerDoc = (double) memoryUsed / docCount;
            
            System.out.printf("文档数: %d, 内存使用: %.2f MB, 每文档内存: %.2f KB%n",
                    docCount, memoryUsed / 1024.0 / 1024.0, memoryPerDoc / 1024.0);
            
            // 清理数据
            vectorStore.clear();
            Thread.sleep(1000);
        }
    }

    @Test
    @DisplayName("大向量维度性能测试")
    void testLargeVectorDimensionPerformance(TestInfo testInfo) throws Exception {
        System.out.println("开始大向量维度性能测试: " + testInfo.getDisplayName());
        
        int[] dimensions = {128, 256, 512, 1024, 2048};
        int documentCount = 1000;
        int queryCount = 20;
        
        for (int dimension : dimensions) {
            // 创建新的向量存储
            LanceDbParam param = LanceDbParam.builder()
                    .tableName("perf_test_dim_" + dimension + "_" + System.currentTimeMillis())
                    .dimension(dimension)
                    .metric("cosine")
                    .build();
            
            LanceDbVectorStore dimVectorStore = new LanceDbVectorStore(
                    vectorStore.getConfiguration(), param);
            dimVectorStore.setEmbeddings(new PerformanceEmbeddings(dimension));
            
            try {
                // 插入测试
                List<Document> documents = generateDocuments(documentCount);
                
                long insertStart = System.nanoTime();
                dimVectorStore.addDocuments(documents);
                long insertEnd = System.nanoTime();
                
                double insertTimeMs = (insertEnd - insertStart) / 1_000_000.0;
                double insertThroughput = documentCount / (insertTimeMs / 1000.0);
                
                Thread.sleep(2000);
                
                // 查询测试
                List<Double> queryTimes = new ArrayList<>();
                for (int q = 0; q < queryCount; q++) {
                    long queryStart = System.nanoTime();
                    dimVectorStore.similaritySearch("test query " + q, 10);
                    long queryEnd = System.nanoTime();
                    
                    queryTimes.add((queryEnd - queryStart) / 1_000_000.0);
                }
                
                double avgQueryTime = queryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double queryQps = 1000.0 / avgQueryTime;
                
                System.out.printf("维度: %d, 插入耗时: %.2f ms, 插入吞吐量: %.2f docs/sec, 平均查询时间: %.2f ms, 查询QPS: %.2f%n",
                        dimension, insertTimeMs, insertThroughput, avgQueryTime, queryQps);
                
            } finally {
                dimVectorStore.clear();
                dimVectorStore.close();
            }
        }
    }

    @Test
    @DisplayName("缓存性能测试")
    void testCachePerformance(TestInfo testInfo) throws Exception {
        System.out.println("开始缓存性能测试: " + testInfo.getDisplayName());
        
        // 准备测试数据
        int documentCount = 1000;
        List<Document> documents = generateDocuments(documentCount);
        vectorStore.addDocuments(documents);
        
        Thread.sleep(2000);
        
        String[] queries = {"cache test query 1", "cache test query 2", "cache test query 3"};
        int repeatCount = 10;
        
        // 无缓存测试
        vectorStore.enableCache(false);
        List<Double> noCacheQueryTimes = new ArrayList<>();
        
        for (int i = 0; i < repeatCount; i++) {
            for (String query : queries) {
                long start = System.nanoTime();
                vectorStore.similaritySearch(query, 5);
                long end = System.nanoTime();
                noCacheQueryTimes.add((end - start) / 1_000_000.0);
            }
        }
        
        double avgNoCacheTime = noCacheQueryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        // 缓存测试
        vectorStore.enableCache(true);
        List<Double> cacheQueryTimes = new ArrayList<>();
        
        for (int i = 0; i < repeatCount; i++) {
            for (String query : queries) {
                long start = System.nanoTime();
                vectorStore.similaritySearch(query, 5);
                long end = System.nanoTime();
                cacheQueryTimes.add((end - start) / 1_000_000.0);
            }
        }
        
        double avgCacheTime = cacheQueryTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double speedupRatio = avgNoCacheTime / avgCacheTime;
        
        System.out.printf("无缓存平均查询时间: %.2f ms%n", avgNoCacheTime);
        System.out.printf("缓存平均查询时间: %.2f ms%n", avgCacheTime);
        System.out.printf("缓存加速比: %.2fx%n", speedupRatio);
        
        assertTrue(speedupRatio > 1.0, "缓存应该提高查询性能");
    }

    private List<Document> generateDocuments(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new Document(
                        "Performance test document number " + i + " with various content for testing purposes. " +
                        "This document contains information about performance, scalability, and efficiency metrics. " +
                        "Additional content to make the document more realistic and provide better test coverage.",
                        Map.of(
                                "id", i,
                                "category", "category_" + (i % 10),
                                "priority", i % 3 == 0 ? "high" : "normal",
                                "timestamp", System.currentTimeMillis(),
                                "batch", i / 100
                        )
                ))
                .toList();
    }

    private double calculatePercentile(List<Double> values, double percentile) {
        List<Double> sorted = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    /**
     * 性能测试用的嵌入实现
     */
    private static class PerformanceEmbeddings implements Embeddings {
        private final int dimension;
        private final Random random = new Random(42); // 固定种子保证可重复性

        public PerformanceEmbeddings(int dimension) {
            this.dimension = dimension;
        }

        @Override
        public List<Double> embedQuery(String text) {
            // 基于文本生成确定性向量
            Random textRandom = new Random(text.hashCode());
            List<Double> vector = new ArrayList<>(dimension);
            for (int i = 0; i < dimension; i++) {
                vector.add(textRandom.nextGaussian());
            }
            // 归一化
            double norm = Math.sqrt(vector.stream().mapToDouble(x -> x * x).sum());
            return vector.stream().map(x -> x / norm).toList();
        }

        @Override
        public List<List<Double>> embedDocuments(List<String> texts) {
            return texts.stream().map(this::embedQuery).toList();
        }
    }

    /**
     * 性能指标收集器
     */
    private static class PerformanceMetrics {
        private final List<InsertMetric> insertMetrics = new ArrayList<>();
        private final List<QueryMetric> queryMetrics = new ArrayList<>();
        private final List<ConcurrentMetric> concurrentMetrics = new ArrayList<>();

        static class InsertMetric {
            final int batchSize;
            final int documentCount;
            final double durationMs;
            final double throughput;

            InsertMetric(int batchSize, int documentCount, double durationMs, double throughput) {
                this.batchSize = batchSize;
                this.documentCount = documentCount;
                this.durationMs = durationMs;
                this.throughput = throughput;
            }
        }

        static class QueryMetric {
            final int topK;
            final double avgQueryTime;
            final double p95QueryTime;
            final double qps;

            QueryMetric(int topK, double avgQueryTime, double p95QueryTime, double qps) {
                this.topK = topK;
                this.avgQueryTime = avgQueryTime;
                this.p95QueryTime = p95QueryTime;
                this.qps = qps;
            }
        }

        static class ConcurrentMetric {
            final int threadCount;
            final int totalQueries;
            final double totalTimeMs;
            final double overallQps;
            final double avgQueryTime;
            final double p95QueryTime;

            ConcurrentMetric(int threadCount, int totalQueries, double totalTimeMs, 
                           double overallQps, double avgQueryTime, double p95QueryTime) {
                this.threadCount = threadCount;
                this.totalQueries = totalQueries;
                this.totalTimeMs = totalTimeMs;
                this.overallQps = overallQps;
                this.avgQueryTime = avgQueryTime;
                this.p95QueryTime = p95QueryTime;
            }
        }

        void recordInsertPerformance(int batchSize, int documentCount, double durationMs, double throughput) {
            insertMetrics.add(new InsertMetric(batchSize, documentCount, durationMs, throughput));
        }

        void recordQueryPerformance(int topK, double avgQueryTime, double p95QueryTime, double qps) {
            queryMetrics.add(new QueryMetric(topK, avgQueryTime, p95QueryTime, qps));
        }

        void recordConcurrentPerformance(int threadCount, int totalQueries, double totalTimeMs, 
                                       double overallQps, double avgQueryTime, double p95QueryTime) {
            concurrentMetrics.add(new ConcurrentMetric(threadCount, totalQueries, totalTimeMs, 
                    overallQps, avgQueryTime, p95QueryTime));
        }

        void printInsertPerformanceReport() {
            System.out.println("\n=== 插入性能报告 ===");
            System.out.println("批次大小\t文档数\t耗时(ms)\t吞吐量(docs/sec)");
            for (InsertMetric metric : insertMetrics) {
                System.out.printf("%d\t\t%d\t%.2f\t\t%.2f%n",
                        metric.batchSize, metric.documentCount, metric.durationMs, metric.throughput);
            }
        }

        void printQueryPerformanceReport() {
            System.out.println("\n=== 查询性能报告 ===");
            System.out.println("TopK\t平均查询时间(ms)\tP95(ms)\t\tQPS");
            for (QueryMetric metric : queryMetrics) {
                System.out.printf("%d\t%.2f\t\t\t%.2f\t\t%.2f%n",
                        metric.topK, metric.avgQueryTime, metric.p95QueryTime, metric.qps);
            }
        }

        void printConcurrentPerformanceReport() {
            System.out.println("\n=== 并发性能报告 ===");
            System.out.println("线程数\t总查询数\t总耗时(ms)\t整体QPS\t平均查询时间(ms)\tP95(ms)");
            for (ConcurrentMetric metric : concurrentMetrics) {
                System.out.printf("%d\t%d\t\t%.2f\t\t%.2f\t%.2f\t\t\t%.2f%n",
                        metric.threadCount, metric.totalQueries, metric.totalTimeMs,
                        metric.overallQps, metric.avgQueryTime, metric.p95QueryTime);
            }
        }
    }
}

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
package com.alibaba.langengine.singlestore.vectorstore;

import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;


@DisplayName("SingleStore集成测试")
public class SingleStoreTest {

    @Test
    @Disabled("需要真实的SingleStore环境")
    @DisplayName("基础相似性搜索测试")
    public void testBasicSimilaritySearch() {
        SingleStore singleStore = new SingleStore("test_db", "vector_documents");
        singleStore.setEmbedding(new FakeEmbeddings());
        singleStore.similaritySearch("hello", 2);
    }

    @Test
    @Disabled("需要真实的SingleStore环境")
    @DisplayName("完整流程测试")
    public void testFullWorkflow() {
        SingleStore singleStore = new SingleStore("test_db", "vector_documents");
        singleStore.setEmbedding(new FakeEmbeddings());

        // 注意：下面的代码在有真实环境时可以取消注释进行测试
        // SingleStoreService singleStoreService = singleStore.getSingleStoreService();
        // singleStoreService.dropTable();
        // singleStore.init();
        // singleStore.addTexts(Arrays.asList("hello", "ha ha", "hello world"));
        // singleStore.addTexts(Arrays.asList("hello everyone", "hi", "你好", "好呀"));

        singleStore.similaritySearch("hello", 2);
    }
}
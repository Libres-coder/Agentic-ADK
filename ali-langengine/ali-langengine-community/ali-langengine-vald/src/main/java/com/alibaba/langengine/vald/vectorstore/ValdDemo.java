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
package com.alibaba.langengine.vald.vectorstore;

import java.util.HashMap;
import java.util.List;

import com.alibaba.langengine.core.docloader.UnstructuredTxtLoader;
import com.alibaba.langengine.core.embeddings.FakeEmbeddings;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;


public class ValdDemo {

    public static void main(String[] args) {
        try {
            // 创建Vald存储实例
            ValdStore valdStore = new ValdStore(
                "localhost",
                8080,
                new FakeEmbeddings(),
                "demo-collection"
            );

            // 准备测试文档
            RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
            textSplitter.setMaxChunkSize(1000);

            UnstructuredTxtLoader unstructuredTxtLoader = new UnstructuredTxtLoader(
                "langengine.properties", textSplitter);
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("source", "example_data/demo.txt");
            unstructuredTxtLoader.setMetadata(metadata);

            List<Document> documents = unstructuredTxtLoader.load();

            // 添加文档到向量库
            System.out.println("Adding documents to Vald...");
            valdStore.addDocuments(documents);
            System.out.println("Documents added successfully!");

            // 执行相似性搜索
            System.out.println("Performing similarity search...");
            List<Document> results = valdStore.similaritySearch("langengine", 5);

            System.out.println("Search results:");
            results.forEach(document -> {
                System.out.println("ID: " + document.getUniqueId());
                System.out.println("Score: " + document.getScore());
                System.out.println("Metadata: " + document.getMetadata());
                System.out.println("Content: " + document.getPageContent());
                System.out.println("---");
            });

            // 关闭资源
            valdStore.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
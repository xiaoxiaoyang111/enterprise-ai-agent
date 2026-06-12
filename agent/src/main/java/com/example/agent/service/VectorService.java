package com.example.agent.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorService {

    @Autowired
    private VectorStore vectorStore;

    /**
     * 将切片文本批量导入向量数据库
     * @param chunks 切好的文本片段列表
     * @param fileName 原文件名，作为元数据存储，方便后续追溯出处
     */
    public void storeDocumentChunks(List<String> chunks, String fileName) {
        // 1. 将原生的 String 列表转化为带有元数据的 Document 领域对象
        List<Document> documents = chunks.stream()
                .map(chunkText -> new Document(
                        chunkText,
                        Map.<String, Object>of(
                                "source", fileName,
                                "timestamp", System.currentTimeMillis()
                        )
                ))
                .collect(Collectors.toList());

        // 2. 核心大招：add 方法会在底层自动并发调用 Embedding API 获取向量，并塞入 Redis Stack
        vectorStore.add(documents);

        System.out.println("【成功】已将 " + documents.size() + " 个知识分片成功向量化并写入 Redis Stack");
    }
}
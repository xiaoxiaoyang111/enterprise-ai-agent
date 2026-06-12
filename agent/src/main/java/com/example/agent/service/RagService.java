package com.example.agent.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class RagService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private LlmService llmService;

    /**
     * RAG 核心：向量库检索相关碎块 + 喂给大模型
     */
    public Flux<String> ask(String question,String sessionId) { // 💡 确保这里叫 ask，与 Controller 对应
        // 1. 自动检索 (Retrieve)：直接让 Redis 找出最相关的 3 个文档分片
        SearchRequest request = SearchRequest.query(question).withTopK(3);
        List<Document> similarDocuments = vectorStore.similaritySearch(request);

        if (similarDocuments.isEmpty()) {
            // 🚀 核心修复：用 Flux.just() 把普通字符串包装成流
            return Flux.just("抱歉，知识库中未能找到相关信息。");
        }

        // 2. 增强 (Augment)：组装上下文
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < similarDocuments.size(); i++) {
            contextBuilder.append("[参考资料 ").append(i + 1).append("]:\n");
            contextBuilder.append(similarDocuments.get(i).getContent()).append("\n\n");
        }

        // 3. 构建超级 Prompt（解除对工具调用的封印！）
        // 💡 拆分 1：构建专门给系统看的 System Prompt
        String systemPrompt = "你是一个强大的企业内部智能Agent。请综合利用以下【参考资料】以及你【自主调用工具获取的数据】来回答问题。\n" +
                "【执行策略】：\n" +
                "1. 优先调用工具：如果涉及动态业务数据（报销、API等），务必自主调用工具！\n" +
                "2. 参考静态资料：如果涉及规章制度，请基于下方的【参考资料】回答。\n" +
                "3. 兜底回复：若都没有查到，回复“未查到相关信息”。\n\n" +
                "【参考资料】:\n" + contextBuilder.toString();

        // 💡 拆分 2：把用户的原始问题直接传下去，还要多传一个 sessionId

        return llmService.chatStream(systemPrompt, question, sessionId);
    }
}
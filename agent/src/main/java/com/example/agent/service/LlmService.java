package com.example.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class LlmService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory; // 引入记忆体

    // 💡 构造器注入 ChatMemory
    public LlmService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
    }

    /**
     * 🚀 升级版：分离 System 和 User，并加入 sessionId 支持多用户并发
     * @param systemText 系统背景音（包含规则和 RAG 检索出的参考资料）- 不会被记入历史
     * @param userText 用户的真实提问 - 会被记入历史
     * @param sessionId 当前用户的会话 ID
     */
    public Flux<String> chatStream(String systemText, String userText, String sessionId) {
        return chatClient.prompt()
                .system(systemText) // 放入系统层，防止 Token 爆炸
                .user(userText)     // 放入用户层
                .functions("queryExpenseStatus", "queryCustomerApiTier", "queryITEquipmentStatus")
                // 💡 核心魔法：召唤记忆顾问！
                // 它会自动去 chatMemory 里找这个 sessionId 的历史记录，拼接后发给大模型；并在回答后，把最新的对话存进去。
                // 最后的 "10" 表示只记住最近的 10 条对话，防止越聊越卡。
                .advisors(new MessageChatMemoryAdvisor(chatMemory, sessionId, 10))
                .stream()
                .content();
    }
}
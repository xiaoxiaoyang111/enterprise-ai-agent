package com.example.agent.controller;

import com.example.agent.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@CrossOrigin // 🚀 关键魔法：允许任何前端页面调用我
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private RagService ragService;

    /**
     * 流式知识库问答接口
     */
    @PostMapping(value = "/ask-knowledge", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askKnowledge(
            @RequestParam String question,
            // 💡 3. 让接口支持接收 sessionId，默认值为 default-session
            @RequestParam(defaultValue = "default-session") String sessionId) {

        // 💡 4. 将动态的 sessionId 传给 service 层
        return ragService.ask(question, sessionId);
    }
}
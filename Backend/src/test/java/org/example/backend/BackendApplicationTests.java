package org.example.backend;

import org.example.backend.model.ChatRequest;
import org.example.backend.model.ChatResponse;
import org.example.backend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    ChatService chatService;

    @Test
    void contextLoads() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello, this is a test message.");
        ChatResponse chatResponse = chatService.processMessage(request.getMessage());
        System.out.println("Chat Response: " + chatResponse.getAnswer());

    }

}

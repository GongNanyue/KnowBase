package org.example.backend.controller;

import org.example.backend.model.ChatRequest;
import org.example.backend.model.ChatResponse;
import org.example.backend.service.ChatService;
import org.example.backend.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ChatController {
    
    private final ChatService chatService;
    private final DocumentService documentService;
    
    public ChatController(ChatService chatService, DocumentService documentService) {
        this.chatService = chatService;
        this.documentService = documentService;
    }
    
    @PostMapping("/chat/message")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        return chatService.processMessage(request.getMessage());
    }
    
    @PostMapping("/documents/upload")
    public Map<String, String> uploadDocument(@RequestParam("file") MultipartFile file) {
        String result = documentService.uploadDocument(file);
        return Map.of("message", result);
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "OK", "service", "KnowBase RAG System");
    }

    
}
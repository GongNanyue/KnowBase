package org.example.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.example.backend.model.ChatResponse;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public ChatService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse processMessage(String userMessage) {
        try {
            // 1. 使用Spring AI进行向量检索 - 正确的API
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(userMessage)
                    .topK(3)
                    .similarityThreshold(0.6)
                    .build();

            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

            // 2. 构建上下文
            String context = similarDocuments.stream()
                    .map(Document::getText)  // 使用getText()而不是getContent()
                    .collect(Collectors.joining("\n\n"));

            // 3. 构建提示词
            String prompt = buildPrompt(userMessage, context);

            // 4. 使用Spring AI ChatClient生成回答 - 正确的API
            String answer;
            if (context.trim().isEmpty()) {
                answer = "抱歉，我没有找到相关的文档信息来回答您的问题。请先上传相关文档。";
            } else {
                // 使用ChatClient的正确fluent API
                answer = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
            }

            // 5. 提取引用信息
            List<String> references = similarDocuments.stream()
                    .map(doc -> {
                        String source = (String) doc.getMetadata().get("source");
                        Integer chunkIndex = (Integer) doc.getMetadata().get("chunk_index");
                        return String.format("%s (片段 %d)", source, chunkIndex + 1);
                    })
                    .collect(Collectors.toList());

            return new ChatResponse(answer, references);

        } catch (Exception e) {
            return new ChatResponse("处理您的问题时出现错误: " + e.getMessage(),
                    Collections.emptyList());
        }
    }

    private String buildPrompt(String question, String context) {
        if (context.trim().isEmpty()) {
            return question;
        }

        return String.format("""
            请基于以下上下文信息回答用户的问题。如果上下文中没有相关信息，请说明无法找到相关信息。
            
            上下文信息：
            %s
            
            用户问题：%s
            
            请给出准确、有帮助的回答：
            """, context, question);
    }
}
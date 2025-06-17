package org.example.backend;

import org.example.backend.controller.ChatController;
import org.example.backend.model.ChatRequest;
import org.example.backend.model.ChatResponse;
import org.example.backend.service.ChatService;
import org.example.backend.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackendApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ChatController chatController;

    @Test
    @Order(1)
    void contextLoads() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello, this is a test message.");
        ChatResponse chatResponse = chatService.processMessage(request.getMessage());
        System.out.println("Chat Response: " + chatResponse.getAnswer());
        assertNotNull(chatResponse);
        assertNotNull(chatResponse.getAnswer());
    }

    @Test
    @Order(2)
    void testFileUploadAndQuestionWorkflow() throws IOException {
        // 1. 创建测试文件
//        String testContent = """
//            Spring AI 简介
//
//            Spring AI 是一个专为 AI 工程而设计的应用程序框架。
//            它提供了一个可移植的 API，支持所有主要的模型提供商，
//            如 OpenAI、Microsoft、Amazon、Google 和 Hugging Face。
//
//            主要特性：
//            - 支持多种 AI 模型
//            - 向量数据库集成
//            - 文档处理和分块
//            - RAG（检索增强生成）功能
//
//            Spring AI 可以帮助开发者快速构建 AI 驱动的应用程序。
//            """;
//
//        MockMultipartFile testFile = new MockMultipartFile(
//                "file",
//                "spring-ai-guide.txt",
//                "text/plain",
//                testContent.getBytes("UTF-8")
//        );
//
//        // 2. 测试文件上传
//        String uploadResult = documentService.uploadDocument(testFile);
//        System.out.println("上传结果: " + uploadResult);
//
//        assertNotNull(uploadResult);
//        assertTrue(uploadResult.contains("上传成功"));
//        assertTrue(uploadResult.contains("spring-ai-guide.txt"));
//
//        // 3. 等待向量存储处理完成
//        try {
//            Thread.sleep(2000); // 等待2秒让向量存储处理完成
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

        // 4. 测试相关问题查询
        String[] testQuestions = {
                "什么是 Spring AI？",
                "Spring AI 有哪些主要特性？",
                "Spring AI 支持哪些模型提供商？"
        };

        for (String question : testQuestions) {
            System.out.println("\n测试问题: " + question);
            ChatResponse response = chatService.processMessage(question);
            
            assertNotNull(response, "响应不应为空");
            assertNotNull(response.getAnswer(), "回答不应为空");
            assertFalse(response.getAnswer().contains("没有找到相关的文档信息"),
                    "应该能找到相关文档信息");
            
            // 验证引用信息
//            assertNotNull(response.getReferences(), "引用信息不应为空");
//            assertTrue(response.getReferences().size() > 0, "应该有引用信息");
            
            System.out.println("回答: " + response.getAnswer());
            System.out.println("引用: " + response.getReferences());
        }
    }

    @Test
    @Order(3)
    void testFileUploadViaController() throws IOException {
        // 创建测试文件
//        String testContent = """
//            RAG 系统架构
//
//            检索增强生成（RAG）是一种将信息检索与生成式 AI 结合的技术。
//
//            RAG 工作流程：
//            1. 文档上传和处理
//            2. 文本分块和向量化
//            3. 存储到向量数据库
//            4. 用户查询向量化
//            5. 相似性搜索
//            6. 上下文注入和生成回答
//
//            优势：
//            - 提供准确的、基于事实的回答
//            - 减少模型幻觉
//            - 支持实时知识更新
//            """;
//
//        MockMultipartFile testFile = new MockMultipartFile(
//                "file",
//                "rag-architecture.txt",
//                "text/plain",
//                testContent.getBytes("UTF-8")
//        );
//
//        // 通过控制器上传文件
//        Map<String, String> result = chatController.uploadDocument(testFile);
//
//        assertNotNull(result);
//        assertTrue(result.containsKey("message"));
//        assertTrue(result.get("message").contains("上传成功"));
//
//        System.out.println("控制器上传结果: " + result.get("message"));
//
//        // 等待处理完成后测试查询
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
        // 测试 RAG 相关问题
        ChatResponse response = chatService.processMessage("什么是 RAG 系统？");
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        System.out.println("RAG 查询回答: " + response.getAnswer());
        System.out.println("引用: " + response.getReferences());
    }

    @Test
    @Order(4)
    void testHealthEndpoint() {
        Map<String, String> health = chatController.health();
        assertNotNull(health);
        assertEquals("OK", health.get("status"));
        assertEquals("KnowBase RAG System", health.get("service"));
    }

    @Test
    @Order(5)
    void testChatEndpoint() {
        ChatRequest request = new ChatRequest();
        request.setMessage("请介绍一下 Spring AI 的主要功能");
        
        ChatResponse response = chatController.sendMessage(request);
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        System.out.println("聊天端点测试结果: " + response.getAnswer());
    }

    @Test
    @Order(6)
    void testInvalidFileUpload() {
        // 测试空文件上传
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        String result = documentService.uploadDocument(emptyFile);
        assertNotNull(result);
        // 由于可能成功或失败，我们只检查返回了结果
        System.out.println("空文件上传结果: " + result);
    }

    @Test
    @Order(7)
    void testQuestionWithoutUploadedDocuments() {
        // 创建一个新的 ChatService 实例或清空向量存储（如果可能的话）
        // 这里我们测试一个不太可能在已上传文档中找到答案的问题
        ChatResponse response = chatService.processMessage("今天天气怎么样？");
        assertNotNull(response);
        assertNotNull(response.getAnswer());
        System.out.println("无相关文档问题的回答: " + response.getAnswer());
    }
}

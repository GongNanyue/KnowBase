# 后端开发指南

## 📋 概述

KnowBase后端基于Spring Boot 3.5构建，实现RAG核心功能，包括文档处理、向量检索、LLM集成等模块。

## 🏗️ 项目结构

```
Backend/
├── src/main/java/org/example/backend/
│   ├── BackendApplication.java           # 启动类
│   ├── config/                          # 配置类
│   │   ├── MilvusConfig.java            # Milvus配置
│   │   ├── LLMConfig.java               # LLM配置
│   │   ├── WebConfig.java               # Web配置
│   │   └── SecurityConfig.java          # 安全配置
│   ├── controller/                      # 控制器层
│   │   ├── ChatController.java          # 对话接口
│   │   ├── DocumentController.java      # 文档管理
│   │   └── ConfigController.java        # 配置管理
│   ├── service/                         # 服务层
│   │   ├── ChatService.java             # 对话服务
│   │   ├── DocumentService.java         # 文档服务
│   │   ├── EmbeddingService.java        # 向量化服务
│   │   ├── RetrievalService.java        # 检索服务
│   │   └── FileStorageService.java      # 文件存储服务
│   ├── repository/                      # 数据访问层
│   │   ├── MilvusRepository.java        # Milvus数据访问
│   │   └── DocumentRepository.java      # 文档元数据访问
│   ├── model/                           # 数据模型
│   │   ├── dto/                         # 数据传输对象
│   │   ├── entity/                      # 实体类
│   │   └── vo/                          # 视图对象
│   ├── exception/                       # 异常处理
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   └── BusinessException.java       # 业务异常
│   └── util/                           # 工具类
│       ├── VectorUtils.java            # 向量工具
│       └── TextUtils.java              # 文本工具
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   ├── application-dev.yml             # 开发环境配置
│   ├── application-prod.yml            # 生产环境配置
│   └── logback-spring.xml              # 日志配置
└── pom.xml                             # Maven依赖
```

## 🛠️ 环境搭建

### 1. 基础环境要求
- **JDK 17+**
- **Maven 3.8+**
- **Docker & Docker Compose**（用于Milvus）

### 2. 依赖配置

更新 [`pom.xml`](../../Backend/pom.xml) 添加必要依赖：

```xml
<dependencies>
    <!-- Spring Boot基础依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring AI -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
        <version>1.0.0-M4</version>
    </dependency>
    
    <!-- Milvus Java SDK -->
    <dependency>
        <groupId>io.milvus</groupId>
        <artifactId>milvus-sdk-java</artifactId>
        <version>2.5.1</version>
    </dependency>
    
    <!-- 文档处理 -->
    <dependency>
        <groupId>org.apache.tika</groupId>
        <artifactId>tika-core</artifactId>
        <version>2.9.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.tika</groupId>
        <artifactId>tika-parsers-standard-package</artifactId>
        <version>2.9.1</version>
    </dependency>
    
    <!-- 数据库 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- 工具类 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
    
    <!-- 测试 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 3. 配置文件

创建 `application.yml`：

```yaml
server:
  port: 8080

spring:
  application:
    name: knowbase-backend
  profiles:
    active: dev
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

# Milvus配置
milvus:
  host: localhost
  port: 19530
  database: knowbase
  collection:
    name: documents
    dimension: 1536  # OpenAI embedding维度

# LLM配置
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-3-large

# 文件存储配置
file:
  storage:
    path: ./data/files
    max-size: 100MB

# 日志配置
logging:
  level:
    org.example.backend: DEBUG
    io.milvus: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 🔧 核心模块实现

### 1. Milvus配置类

```java
@Configuration
@EnableConfigurationProperties(MilvusProperties.class)
@Slf4j
public class MilvusConfig {
    
    @Autowired
    private MilvusProperties milvusProperties;
    
    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(milvusProperties.getHost())
                .withPort(milvusProperties.getPort())
                .build();
        
        MilvusServiceClient client = new MilvusServiceClient(connectParam);
        log.info("Milvus client connected to {}:{}", 
                milvusProperties.getHost(), milvusProperties.getPort());
        return client;
    }
    
    @PostConstruct
    public void initializeCollection() {
        // 初始化集合
        createCollectionIfNotExists();
    }
    
    private void createCollectionIfNotExists() {
        // 集合创建逻辑
    }
}
```

### 2. 文档服务实现

```java
@Service
@Slf4j
public class DocumentService {
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private MilvusRepository milvusRepository;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        try {
            // 1. 保存文件
            String filePath = fileStorageService.saveFile(file);
            
            // 2. 解析文档内容
            String content = parseDocument(file);
            
            // 3. 文本分块
            List<String> chunks = splitIntoChunks(content);
            
            // 4. 向量化并存储
            List<String> vectorIds = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                float[] vector = embeddingService.getEmbedding(chunk);
                String vectorId = milvusRepository.insertVector(vector, createMetadata(file, i, chunk));
                vectorIds.add(vectorId);
            }
            
            // 5. 返回结果
            return DocumentUploadResponse.builder()
                    .documentId(UUID.randomUUID().toString())
                    .fileName(file.getOriginalFilename())
                    .chunkCount(chunks.size())
                    .vectorIds(vectorIds)
                    .status("SUCCESS")
                    .build();
                    
        } catch (Exception e) {
            log.error("文档上传失败", e);
            throw new BusinessException("文档上传失败: " + e.getMessage());
        }
    }
    
    private String parseDocument(MultipartFile file) throws Exception {
        // 使用Apache Tika解析文档
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        
        try (InputStream stream = file.getInputStream()) {
            parser.parse(stream, handler, metadata, new ParseContext());
            return handler.toString();
        }
    }
    
    private List<String> splitIntoChunks(String content) {
        // 智能分块策略
        int chunkSize = 1000;
        int overlap = 200;
        
        List<String> chunks = new ArrayList<>();
        int start = 0;
        
        while (start < content.length()) {
            int end = Math.min(start + chunkSize, content.length());
            
            // 寻找合适的分割点（句号、换行等）
            if (end < content.length()) {
                int lastPeriod = content.lastIndexOf('.', end);
                int lastNewline = content.lastIndexOf('\n', end);
                int splitPoint = Math.max(lastPeriod, lastNewline);
                
                if (splitPoint > start + chunkSize / 2) {
                    end = splitPoint + 1;
                }
            }
            
            chunks.add(content.substring(start, end).trim());
            start = end - overlap;
        }
        
        return chunks;
    }
}
```

### 3. 检索服务实现

```java
@Service
@Slf4j
public class RetrievalService {
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private MilvusRepository milvusRepository;
    
    public List<RetrievalResult> search(String query, int topK) {
        try {
            // 1. 查询向量化
            float[] queryVector = embeddingService.getEmbedding(query);
            
            // 2. 向量检索
            List<SearchResult> searchResults = milvusRepository.search(queryVector, topK);
            
            // 3. 构建结果
            return searchResults.stream()
                    .map(this::buildRetrievalResult)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("检索失败", e);
            throw new BusinessException("检索失败: " + e.getMessage());
        }
    }
    
    private RetrievalResult buildRetrievalResult(SearchResult searchResult) {
        return RetrievalResult.builder()
                .content(searchResult.getContent())
                .score(searchResult.getScore())
                .metadata(searchResult.getMetadata())
                .build();
    }
}
```

### 4. 对话服务实现

```java
@Service
@Slf4j
public class ChatService {
    
    @Autowired
    private RetrievalService retrievalService;
    
    @Autowired
    private ChatClient chatClient;
    
    public ChatResponse chat(ChatRequest request) {
        try {
            // 1. 检索相关文档
            List<RetrievalResult> retrievalResults = retrievalService.search(
                    request.getMessage(), 5);
            
            // 2. 构建上下文
            String context = buildContext(retrievalResults);
            
            // 3. 构建Prompt
            String prompt = buildPrompt(request.getMessage(), context);
            
            // 4. LLM生成回答
            String answer = chatClient.call(prompt);
            
            // 5. 构建响应
            return ChatResponse.builder()
                    .answer(answer)
                    .references(extractReferences(retrievalResults))
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.error("对话生成失败", e);
            throw new BusinessException("对话生成失败: " + e.getMessage());
        }
    }
    
    private String buildPrompt(String question, String context) {
        return String.format("""
            基于以下上下文信息回答用户问题。如果上下文中没有相关信息，请说明无法找到相关信息。
            
            上下文：
            %s
            
            问题：%s
            
            回答：
            """, context, question);
    }
}
```

## 📡 API接口设计

### 1. 对话接口

```java
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
@Slf4j
public class ChatController {
    
    @Autowired
    private ChatService chatService;
    
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<ChatHistory>> getHistory() {
        // 获取对话历史
        return ResponseEntity.ok(new ArrayList<>());
    }
}
```

### 2. 文档管理接口

```java
@RestController
@RequestMapping("/api/documents")
@CrossOrigin
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        DocumentUploadResponse response = documentService.uploadDocument(file);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<DocumentInfo>> listDocuments() {
        // 获取文档列表
        return ResponseEntity.ok(new ArrayList<>());
    }
    
    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        // 删除文档
        return ResponseEntity.ok().build();
    }
}
```

## 🔧 开发工具和技巧

### 1. 开发工具推荐
- **IDE**：IntelliJ IDEA
- **插件**：Lombok, Spring Boot Helper
- **测试工具**：Postman, JUnit 5
- **数据库工具**：Attu（Milvus管理）

### 2. 调试技巧
- 使用Spring Boot DevTools自动重启
- 配置详细的日志输出
- 使用Actuator监控应用状态

### 3. 性能优化
- 向量检索结果缓存
- 异步处理大文档
- 连接池配置优化

## 🧪 测试策略

### 1. 单元测试
```java
@SpringBootTest
class DocumentServiceTest {
    
    @Autowired
    private DocumentService documentService;
    
    @Test
    void testDocumentUpload() {
        // 测试文档上传功能
    }
}
```

### 2. 集成测试
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ChatControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testChatEndpoint() throws Exception {
        // 测试对话接口
    }
}
```

## 📝 最佳实践

1. **异常处理**：统一异常处理，返回标准错误格式
2. **参数验证**：使用Bean Validation验证请求参数
3. **日志记录**：记录关键操作和错误信息
4. **配置管理**：敏感配置使用环境变量
5. **代码规范**：遵循阿里巴巴Java开发手册

---

> 详细的实现代码请参考各模块的具体文档
# 代码规范

## 📋 概述

本文档定义了KnowBase项目的代码风格和质量标准，确保团队协作的一致性和代码的可维护性。

## ☕ Java/Spring Boot 规范

### 1. 命名规范

#### 类命名
```java
// ✅ 正确：使用PascalCase，含义明确
public class DocumentService {}
public class ChatController {}
public class UserRepository {}

// ❌ 错误：命名不清晰或不符合规范
public class docService {}
public class chat_controller {}
public class Repo {}
```

#### 方法命名
```java
// ✅ 正确：使用camelCase，动词开头
public void saveDocument(Document document) {}
public List<Message> findMessagesByUserId(Long userId) {}
public boolean isValidFile(String fileName) {}

// ❌ 错误：命名不符合规范
public void Save_Document() {}
public List<Message> messages() {}
public boolean valid() {}
```

#### 变量命名
```java
// ✅ 正确：使用camelCase，名词含义明确
private String fileName;
private int maxRetryCount = 3;
private List<Document> uploadedDocuments;

// ❌ 错误：命名不清晰
private String fn;
private int max;
private List<Document> list;
```

#### 常量命名
```java
// ✅ 正确：使用UPPER_SNAKE_CASE
public static final String DEFAULT_ENCODING = "UTF-8";
public static final int MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
public static final long CACHE_EXPIRE_TIME = 3600L;

// ❌ 错误：命名不符合规范
public static final String defaultEncoding = "UTF-8";
public static final int maxFileSize = 100;
```

### 2. 代码结构

#### 控制器规范
```java
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
@Slf4j
@Validated
public class ChatController {
    
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    @PostMapping("/message")
    @Operation(summary = "发送聊天消息", description = "发送用户消息并获取AI回复")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {
        
        log.info("收到聊天请求: {}", request);
        
        try {
            ChatResponse response = chatService.processMessage(request);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            log.error("处理聊天消息失败: {}", e.getMessage());
            throw e;
        }
    }
    
    @GetMapping("/history")
    @Operation(summary = "获取聊天历史", description = "获取用户的聊天历史记录")
    public ResponseEntity<List<ChatHistory>> getChatHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        List<ChatHistory> history = chatService.getChatHistory(pageable);
        return ResponseEntity.ok(history);
    }
}
```

#### 服务层规范
```java
@Service
@Slf4j
@Transactional(readOnly = true)
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final FileStorageService fileStorageService;
    
    public DocumentService(DocumentRepository documentRepository,
                          EmbeddingService embeddingService,
                          FileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
        this.fileStorageService = fileStorageService;
    }
    
    @Transactional
    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        // 1. 参数验证
        validateFile(file);
        
        // 2. 保存文件
        String filePath = fileStorageService.saveFile(file);
        
        // 3. 解析内容
        String content = parseDocument(file);
        
        // 4. 处理向量化
        List<DocumentChunk> chunks = processDocument(content, file);
        
        // 5. 保存元数据
        Document document = saveDocumentMetadata(file, filePath, chunks.size());
        
        // 6. 构建响应
        return buildUploadResponse(document, chunks);
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制");
        }
        
        String fileName = file.getOriginalFilename();
        if (!isValidFileType(fileName)) {
            throw new BusinessException("不支持的文件类型");
        }
    }
}
```

#### 数据传输对象规范
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRequest {
    
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "消息长度不能超过4000字符")
    private String message;
    
    @Size(max = 64, message = "会话ID长度不能超过64字符")
    private String sessionId;
    
    @JsonProperty("include_history")
    private Boolean includeHistory = true;
    
    @JsonProperty("max_tokens")
    @Min(value = 1, message = "最大token数必须大于0")
    @Max(value = 4000, message = "最大token数不能超过4000")
    private Integer maxTokens = 1000;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    private String answer;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    private List<Reference> references;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("token_usage")
    private TokenUsage tokenUsage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reference {
        private String title;
        private String content;
        private Double score;
        private String documentId;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
```

### 3. 异常处理规范

#### 自定义异常
```java
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    
    private final String code;
    private final Object[] args;
    
    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.args = null;
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }
    
    public BusinessException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }
}

// 具体异常类型
public class FileUploadException extends BusinessException {
    public FileUploadException(String message) {
        super("FILE_UPLOAD_ERROR", message);
    }
}

public class VectorSearchException extends BusinessException {
    public VectorSearchException(String message) {
        super("VECTOR_SEARCH_ERROR", message);
    }
}
```

#### 全局异常处理
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        
        ErrorResponse response = ErrorResponse.builder()
            .code(e.getCode())
            .message(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {
        
        List<String> errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
            
        ErrorResponse response = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("参数验证失败")
            .details(errors)
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("系统异常", e);
        
        ErrorResponse response = ErrorResponse.builder()
            .code("SYSTEM_ERROR")
            .message("系统内部错误")
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### 4. 配置规范

#### 配置类
```java
@Configuration
@EnableConfigurationProperties({MilvusProperties.class, LLMProperties.class})
@Slf4j
public class ApplicationConfig {
    
    @Bean
    @ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
            
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

#### 配置属性
```java
@ConfigurationProperties(prefix = "milvus")
@Data
@Validated
public class MilvusProperties {
    
    @NotBlank(message = "Milvus主机地址不能为空")
    private String host = "localhost";
    
    @Min(value = 1, message = "端口号必须大于0")
    @Max(value = 65535, message = "端口号不能超过65535")
    private Integer port = 19530;
    
    @NotBlank(message = "数据库名称不能为空")
    private String database = "knowbase";
    
    private Collection collection = new Collection();
    
    @Data
    public static class Collection {
        @NotBlank(message = "集合名称不能为空")
        private String name = "documents";
        
        @Min(value = 1, message = "向量维度必须大于0")
        private Integer dimension = 1536;
        
        private String indexType = "HNSW";
        private String metricType = "COSINE";
    }
}
```

## 🎨 Vue.js/前端规范

### 1. 组件命名规范

#### 组件文件命名
```javascript
// ✅ 正确：使用PascalCase
ChatWindow.vue
MessageItem.vue
DocumentUpload.vue
UserProfile.vue

// ❌ 错误：命名不规范
chatWindow.vue
messageitem.vue
document_upload.vue
```

#### 组件注册命名
```javascript
// ✅ 正确：kebab-case在模板中，PascalCase在脚本中
<template>
  <chat-window />
  <message-item />
  <document-upload />
</template>

<script setup>
import ChatWindow from '@/components/ChatWindow.vue'
import MessageItem from '@/components/MessageItem.vue'
import DocumentUpload from '@/components/DocumentUpload.vue'
</script>
```

### 2. 组件结构规范

#### 单文件组件结构
```vue
<template>
  <div class="chat-window">
    <!-- 模板内容 -->
  </div>
</template>

<script setup>
// 1. 导入依赖
import { ref, computed, watch, onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import MessageItem from './MessageItem.vue'

// 2. 定义props
const props = defineProps({
  sessionId: {
    type: String,
    default: ''
  },
  autoScroll: {
    type: Boolean,
    default: true
  }
})

// 3. 定义emits
const emit = defineEmits(['message-sent', 'scroll-to-bottom'])

// 4. 响应式数据
const inputText = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)

// 5. Store使用
const chatStore = useChatStore()
const { messages, isTyping } = storeToRefs(chatStore)

// 6. 计算属性
const hasMessages = computed(() => messages.value.length > 0)
const lastMessage = computed(() => messages.value[messages.value.length - 1])

// 7. 方法定义
const sendMessage = async () => {
  if (!inputText.value.trim()) return
  
  try {
    isLoading.value = true
    await chatStore.sendMessage(inputText.value)
    inputText.value = ''
    emit('message-sent')
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isLoading.value = false
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 8. 监听器
watch(messages, () => {
  if (props.autoScroll) {
    nextTick(scrollToBottom)
  }
}, { deep: true })

// 9. 生命周期
onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-window {
  /* 样式定义 */
}
</style>
```

### 3. 状态管理规范

#### Pinia Store规范
```javascript
// stores/chat.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatAPI } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  // 状态定义
  const messages = ref([])
  const currentSession = ref(null)
  const isLoading = ref(false)
  
  // 计算属性
  const messageCount = computed(() => messages.value.length)
  const hasMessages = computed(() => messageCount.value > 0)
  const lastMessage = computed(() => {
    return hasMessages.value ? messages.value[messageCount.value - 1] : null
  })
  
  // 异步操作
  const sendMessage = async (content) => {
    if (!content?.trim()) {
      throw new Error('消息内容不能为空')
    }
    
    const userMessage = createUserMessage(content)
    messages.value.push(userMessage)
    
    try {
      isLoading.value = true
      const response = await chatAPI.sendMessage({
        message: content,
        sessionId: currentSession.value?.id
      })
      
      const aiMessage = createAIMessage(response)
      messages.value.push(aiMessage)
      
      updateSession(response.sessionId)
      
    } catch (error) {
      console.error('发送消息失败:', error)
      const errorMessage = createErrorMessage('消息发送失败，请重试')
      messages.value.push(errorMessage)
      throw error
    } finally {
      isLoading.value = false
    }
  }
  
  // 辅助方法
  const createUserMessage = (content) => ({
    id: generateId(),
    content,
    isUser: true,
    timestamp: new Date(),
    status: 'sent'
  })
  
  const createAIMessage = (response) => ({
    id: generateId(),
    content: response.answer,
    isUser: false,
    timestamp: new Date(),
    references: response.references,
    status: 'received'
  })
  
  const clearMessages = () => {
    messages.value = []
    currentSession.value = null
  }
  
  const generateId = () => `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  
  return {
    // 状态
    messages: readonly(messages),
    currentSession: readonly(currentSession),
    isLoading: readonly(isLoading),
    
    // 计算属性
    messageCount,
    hasMessages,
    lastMessage,
    
    // 方法
    sendMessage,
    clearMessages
  }
})
```

### 4. API调用规范

#### HTTP请求封装
```javascript
// api/index.js
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加认证token
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()
    
    return config
  },
  (error) => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    const { response } = error
    
    if (response) {
      const { status, data } = response
      
      // 根据状态码处理错误
      switch (status) {
        case 401:
          handleUnauthorized()
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 422:
          handleValidationError(data)
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

const generateRequestId = () => {
  return `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
}

const handleUnauthorized = () => {
  const userStore = useUserStore()
  userStore.logout()
  ElMessage.error('登录已过期，请重新登录')
  // 跳转到登录页
}

const handleValidationError = (data) => {
  if (data.details && Array.isArray(data.details)) {
    data.details.forEach(error => ElMessage.error(error))
  } else {
    ElMessage.error(data.message || '参数验证失败')
  }
}

export default request
```

#### API模块定义
```javascript
// api/chat.js
import request from './index'

/**
 * 聊天相关API
 */
export const chatAPI = {
  /**
   * 发送消息
   * @param {Object} data - 消息数据
   * @param {string} data.message - 消息内容
   * @param {string} [data.sessionId] - 会话ID
   * @returns {Promise<Object>} 响应数据
   */
  async sendMessage(data) {
    return request.post('/chat/message', data)
  },
  
  /**
   * 获取聊天历史
   * @param {Object} params - 查询参数
   * @param {string} [params.sessionId] - 会话ID
   * @param {number} [params.page=0] - 页码
   * @param {number} [params.size=20] - 每页大小
   * @returns {Promise<Object>} 聊天历史数据
   */
  async getChatHistory(params = {}) {
    return request.get('/chat/history', { params })
  },
  
  /**
   * 清空聊天历史
   * @param {string} sessionId - 会话ID
   * @returns {Promise<void>}
   */
  async clearChatHistory(sessionId) {
    return request.delete(`/chat/history/${sessionId}`)
  }
}
```

## 📝 注释规范

### 1. Java注释规范

#### 类注释
```java
/**
 * 文档服务类
 * 
 * 负责处理文档的上传、解析、向量化和存储等功能
 * 支持PDF、Word、文本等多种格式的文档处理
 * 
 * @author KnowBase Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public class DocumentService {
    // 类实现
}
```

#### 方法注释
```java
/**
 * 上传并处理文档
 * 
 * 将用户上传的文档进行解析、分块、向量化处理，
 * 并将处理结果存储到向量数据库中
 * 
 * @param file 上传的文件，不能为null且不能为空
 * @param userId 用户ID，用于权限控制和数据隔离
 * @return 文档上传响应，包含文档ID、分块数量等信息
 * @throws FileUploadException 当文件上传或处理失败时抛出
 * @throws BusinessException 当业务规则验证失败时抛出
 */
public DocumentUploadResponse uploadDocument(MultipartFile file, Long userId) {
    // 方法实现
}
```

### 2. JavaScript注释规范

#### 函数注释
```javascript
/**
 * 发送聊天消息
 * 
 * 向后端发送用户消息，获取AI回复并更新聊天状态
 * 
 * @param {string} content - 消息内容，不能为空
 * @param {string} [sessionId] - 会话ID，可选参数
 * @returns {Promise<Object>} 包含AI回复和引用文档的响应对象
 * @throws {Error} 当消息发送失败时抛出错误
 * 
 * @example
 * try {
 *   const response = await sendMessage('你好');
 *   console.log(response.answer);
 * } catch (error) {
 *   console.error('发送失败:', error);
 * }
 */
const sendMessage = async (content, sessionId) => {
  // 函数实现
}
```

## 🧪 测试规范

### 1. Java测试规范

#### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
    
    @Mock
    private DocumentRepository documentRepository;
    
    @Mock
    private EmbeddingService embeddingService;
    
    @Mock
    private FileStorageService fileStorageService;
    
    @InjectMocks
    private DocumentService documentService;
    
    @Test
    @DisplayName("应该成功上传PDF文档")
    void shouldUploadPdfDocumentSuccessfully() {
        // Given - 准备测试数据
        MultipartFile file = createMockPdfFile();
        String expectedContent = "测试文档内容";
        
        when(fileStorageService.saveFile(file)).thenReturn("/uploads/test.pdf");
        when(embeddingService.getEmbedding(anyString())).thenReturn(new float[1536]);
        
        // When - 执行测试方法
        DocumentUploadResponse response = documentService.uploadDocument(file);
        
        // Then - 验证结果
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getChunkCount()).isGreaterThan(0);
        
        verify(fileStorageService).saveFile(file);
        verify(embeddingService, atLeastOnce()).getEmbedding(anyString());
    }
    
    @Test
    @DisplayName("当文件为空时应该抛出异常")
    void shouldThrowExceptionWhenFileIsEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("file", "", "text/plain", new byte[0]);
        
        // When & Then
        assertThatThrownBy(() -> documentService.uploadDocument(emptyFile))
            .isInstanceOf(BusinessException.class)
            .hasMessage("文件不能为空");
    }
    
    private MultipartFile createMockPdfFile() {
        return new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "测试PDF内容".getBytes()
        );
    }
}
```

### 2. JavaScript测试规范

#### 组件测试
```javascript
// components/__tests__/ChatWindow.test.js
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ChatWindow from '@/components/ChatWindow.vue'
import { useChatStore } from '@/stores/chat'

describe('ChatWindow', () => {
  let wrapper
  let chatStore
  
  beforeEach(() => {
    setActivePinia(createPinia())
    chatStore = useChatStore()
    
    wrapper = mount(ChatWindow, {
      props: {
        sessionId: 'test-session'
      }
    })
  })
  
  it('应该正确渲染聊天窗口', () => {
    expect(wrapper.find('.chat-window').exists()).toBe(true)
    expect(wrapper.find('.chat-header').exists()).toBe(true)
    expect(wrapper.find('.chat-messages').exists()).toBe(true)
  })
  
  it('应该在发送消息时调用store方法', async () => {
    const sendMessageSpy = vi.spyOn(chatStore, 'sendMessage')
    const inputBox = wrapper.findComponent({ name: 'InputBox' })
    
    await inputBox.vm.$emit('send', '测试消息')
    
    expect(sendMessageSpy).toHaveBeenCalledWith('测试消息')
  })
  
  it('应该在有新消息时自动滚动到底部', async () => {
    const scrollToBottomSpy = vi.spyOn(wrapper.vm, 'scrollToBottom')
    
    chatStore.messages.push({
      id: 1,
      content: '新消息',
      isUser: true,
      timestamp: new Date()
    })
    
    await wrapper.vm.$nextTick()
    
    expect(scrollToBottomSpy).toHaveBeenCalled()
  })
})
```

## 📊 性能规范

### 1. 代码性能

#### 避免内存泄漏
```java
// ✅ 正确：使用try-with-resources
try (FileInputStream fis = new FileInputStream(file);
     BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
    // 处理文件
} catch (IOException e) {
    log.error("文件读取失败", e);
}

// ❌ 错误：没有正确关闭资源
FileInputStream fis = new FileInputStream(file);
BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
// 处理文件，但没有关闭资源
```

#### 优化集合操作
```java
// ✅ 正确：使用Stream API优化
List<String> validFiles = files.stream()
    .filter(this::isValidFile)
    .map(File::getName)
    .collect(Collectors.toList());

// ✅ 正确：预分配集合大小
List<String> results = new ArrayList<>(expectedSize);
Map<String, Object> cache = new HashMap<>(16, 0.75f);
```

### 2. 前端性能

#### 组件懒加载
```javascript
// ✅ 正确：路由懒加载
const routes = [
  {
    path: '/chat',
    component: () => import('@/views/Chat.vue')
  },
  {
    path: '/documents',
    component: () => import('@/views/Documents.vue')
  }
]

// ✅ 正确：组件按需加载
const ChatWindow = defineAsyncComponent(() => import('./ChatWindow.vue'))
```

#### 防抖和节流
```javascript
// ✅ 正确：搜索输入防抖
import { debounce } from 'lodash-es'

const searchDocuments = debounce(async (query) => {
  if (!query.trim()) return
  
  try {
    const results = await documentAPI.search(query)
    searchResults.value = results
  } catch (error) {
    console.error('搜索失败:', error)
  }
}, 300)

// ✅ 正确：滚动事件节流
import { throttle } from 'lodash-es'

const handleScroll = throttle(() => {
  const { scrollTop, scrollHeight, clientHeight } = messagesContainer.value
  
  if (scrollTop + clientHeight >= scrollHeight - 10) {
    emit('scroll-to-bottom')
  }
}, 100)
```

## 📝 代码审查清单

### 1. 功能性检查
- [ ] 代码实现符合需求规格
- [ ] 边界条件处理完整
- [ ] 错误处理机制完善
- [ ] 单元测试覆盖率达标

### 2. 可读性检查
- [ ] 命名规范统一
- [ ] 注释清晰准确
- [ ] 代码结构合理
- [ ] 复杂逻辑有解释

### 3. 性能检查
- [ ] 没有明显性能瓶颈
- [ ] 数据库查询优化
- [ ] 缓存使用合理
- [ ] 资源使用高效

### 4. 安全性检查
- [ ] 输入参数验证
- [ ] SQL注入防护
- [ ] XSS攻击防护
- [ ] 权限控制完善

## 📈 质量指标

### 1. 代码质量指标
- **圈复杂度** < 10
- **方法行数** < 50
- **类行数** < 500
- **测试覆盖率** > 80%

### 2. 性能指标
- **接口响应时间** < 2秒
- **数据库查询时间** < 500ms
- **页面加载时间** < 3秒
- **内存使用率** < 80%

---

> 代码规范需要团队共同遵守和维护，建议定期评审和更新
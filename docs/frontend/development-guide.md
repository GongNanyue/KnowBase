# 前端开发指南

## 📋 概述

KnowBase前端基于Vue 3 + Vite构建，提供现代化的聊天界面和文档管理功能，采用组合式API和TypeScript开发。

## 🏗️ 项目结构

```
fontback/
├── public/
│   ├── favicon.ico
│   └── index.html
├── src/
│   ├── main.js                     # 应用入口
│   ├── App.vue                     # 根组件
│   ├── components/                 # 通用组件
│   │   ├── chat/                   # 聊天相关组件
│   │   │   ├── ChatWindow.vue      # 聊天窗口
│   │   │   ├── MessageItem.vue     # 消息项
│   │   │   ├── InputBox.vue        # 输入框
│   │   │   └── TypingIndicator.vue # 打字指示器
│   │   ├── document/               # 文档相关组件
│   │   │   ├── DocumentList.vue    # 文档列表
│   │   │   ├── DocumentUpload.vue  # 文档上传
│   │   │   └── DocumentItem.vue    # 文档项
│   │   ├── common/                 # 通用组件
│   │   │   ├── Loading.vue         # 加载组件
│   │   │   ├── Empty.vue           # 空状态
│   │   │   └── ErrorMessage.vue    # 错误信息
│   │   └── layout/                 # 布局组件
│   │       ├── Header.vue          # 头部
│   │       ├── Sidebar.vue         # 侧边栏
│   │       └── Footer.vue          # 底部
│   ├── views/                      # 页面组件
│   │   ├── Chat.vue                # 聊天页面
│   │   ├── Documents.vue           # 文档管理页面
│   │   └── Settings.vue            # 设置页面
│   ├── router/                     # 路由配置
│   │   └── index.js                # 路由定义
│   ├── stores/                     # 状态管理
│   │   ├── chat.js                 # 聊天状态
│   │   ├── document.js             # 文档状态
│   │   └── user.js                 # 用户状态
│   ├── api/                        # API接口
│   │   ├── index.js                # API基础配置
│   │   ├── chat.js                 # 聊天接口
│   │   └── document.js             # 文档接口
│   ├── utils/                      # 工具函数
│   │   ├── request.js              # HTTP请求工具
│   │   ├── format.js               # 格式化工具
│   │   └── storage.js              # 存储工具
│   ├── styles/                     # 样式文件
│   │   ├── index.css               # 全局样式
│   │   ├── variables.css           # CSS变量
│   │   └── components.css          # 组件样式
│   └── assets/                     # 静态资源
│       ├── images/                 # 图片
│       └── icons/                  # 图标
├── package.json                    # 项目配置
├── vite.config.js                  # Vite配置
└── jsconfig.json                   # JS配置
```

## 🛠️ 环境搭建

### 1. 基础环境要求
- **Node.js 18+**
- **pnpm/npm/yarn**（推荐pnpm）

### 2. 依赖配置

更新 [`package.json`](../../fontback/package.json)：

```json
{
  "name": "knowbase-frontend",
  "version": "1.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint . --ext .vue,.js --fix",
    "format": "prettier --write src/"
  },
  "dependencies": {
    "vue": "^3.5.13",
    "vue-router": "^4.4.5",
    "pinia": "^2.2.6",
    "element-plus": "^2.8.8",
    "axios": "^1.7.9",
    "@element-plus/icons-vue": "^2.3.1",
    "markdown-it": "^14.1.0",
    "highlight.js": "^11.10.0",
    "dayjs": "^1.11.13"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2.3",
    "vite": "^6.2.4",
    "vite-plugin-vue-devtools": "^7.7.2",
    "eslint": "^9.17.0",
    "eslint-plugin-vue": "^9.32.0",
    "prettier": "^3.4.2",
    "unplugin-auto-import": "^0.18.5",
    "unplugin-vue-components": "^0.27.4"
  }
}
```

### 3. Vite配置

更新 [`vite.config.js`](../../fontback/vite.config.js)：

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia']
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api')
      }
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})
```

## 🎨 核心组件实现

### 1. 聊天窗口组件

```vue
<!-- src/components/chat/ChatWindow.vue -->
<template>
  <div class="chat-window">
    <div class="chat-header">
      <h3>KnowBase AI助手</h3>
      <el-button @click="clearChat" type="text">清空对话</el-button>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <MessageItem
        v-for="message in messages"
        :key="message.id"
        :message="message"
      />
      <TypingIndicator v-if="isTyping" />
    </div>
    
    <InputBox
      @send="handleSendMessage"
      :disabled="isLoading"
    />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import MessageItem from './MessageItem.vue'
import InputBox from './InputBox.vue'
import TypingIndicator from './TypingIndicator.vue'

const chatStore = useChatStore()
const messagesContainer = ref(null)

const { messages, isLoading, isTyping } = storeToRefs(chatStore)

const handleSendMessage = async (content) => {
  await chatStore.sendMessage(content)
  scrollToBottom()
}

const clearChat = () => {
  chatStore.clearMessages()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-window {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  scroll-behavior: smooth;
}
</style>
```

### 2. 消息项组件

```vue
<!-- src/components/chat/MessageItem.vue -->
<template>
  <div class="message-item" :class="{ 'user-message': message.isUser }">
    <div class="message-avatar">
      <el-avatar
        :src="message.isUser ? userAvatar : botAvatar"
        :icon="message.isUser ? UserFilled : ChatDotSquare"
      />
    </div>
    
    <div class="message-content">
      <div class="message-header">
        <span class="sender-name">
          {{ message.isUser ? '我' : 'AI助手' }}
        </span>
        <span class="message-time">
          {{ formatTime(message.timestamp) }}
        </span>
      </div>
      
      <div class="message-body">
        <div v-if="message.isUser" class="user-text">
          {{ message.content }}
        </div>
        <div v-else class="ai-response">
          <div class="response-text" v-html="formatMarkdown(message.content)"></div>
          <div v-if="message.references?.length" class="references">
            <h4>参考文档：</h4>
            <div class="reference-list">
              <div
                v-for="ref in message.references"
                :key="ref.id"
                class="reference-item"
                @click="showReference(ref)"
              >
                <el-icon><Document /></el-icon>
                <span>{{ ref.title }}</span>
                <span class="score">{{ (ref.score * 100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { UserFilled, ChatDotSquare, Document } from '@element-plus/icons-vue'
import { formatTime, formatMarkdown } from '@/utils/format'

defineProps({
  message: {
    type: Object,
    required: true
  }
})

const userAvatar = '/src/assets/images/user-avatar.png'
const botAvatar = '/src/assets/images/bot-avatar.png'

const showReference = (reference) => {
  // 显示参考文档详情
  console.log('显示参考文档:', reference)
}
</script>

<style scoped>
.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
}

.message-content {
  max-width: 70%;
  min-width: 200px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.sender-name {
  font-weight: 600;
  color: #409eff;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-body {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  position: relative;
}

.user-message .message-body {
  background: #409eff;
  color: white;
}

.references {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

.reference-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.reference-item:hover {
  background: #f0f9ff;
  transform: translateX(4px);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
```

### 3. 输入框组件

```vue
<!-- src/components/chat/InputBox.vue -->
<template>
  <div class="input-box">
    <div class="input-container">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="3"
        placeholder="输入您的问题..."
        :disabled="disabled"
        @keydown.enter.exact="handleSend"
        @keydown.enter.shift.exact.prevent="addNewLine"
        resize="none"
      />
      
      <div class="input-actions">
        <el-button @click="attachFile" :icon="Paperclip" circle />
        <el-button
          type="primary"
          @click="handleSend"
          :disabled="!inputText.trim() || disabled"
          :loading="disabled"
          :icon="Position"
        >
          发送
        </el-button>
      </div>
    </div>
    
    <div class="input-tips">
      <span>按 Enter 发送，Shift + Enter 换行</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Paperclip, Position } from '@element-plus/icons-vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send'])

const inputText = ref('')

const handleSend = () => {
  if (!inputText.value.trim() || props.disabled) return
  
  emit('send', inputText.value.trim())
  inputText.value = ''
}

const addNewLine = () => {
  inputText.value += '\n'
}

const attachFile = () => {
  // 处理文件上传
  console.log('上传文件')
}
</script>

<style scoped>
.input-box {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
  background: white;
}

.input-container {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-tips {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
```

## 📊 状态管理

### 1. 聊天状态管理

```javascript
// src/stores/chat.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { chatAPI } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  // 状态
  const messages = ref([])
  const isLoading = ref(false)
  const isTyping = ref(false)
  const currentSessionId = ref(null)

  // 计算属性
  const messageCount = computed(() => messages.value.length)
  const lastMessage = computed(() => messages.value[messages.value.length - 1])

  // 发送消息
  const sendMessage = async (content) => {
    if (!content.trim()) return

    const userMessage = {
      id: Date.now(),
      content,
      isUser: true,
      timestamp: new Date()
    }

    messages.value.push(userMessage)
    isLoading.value = true
    isTyping.value = true

    try {
      const response = await chatAPI.sendMessage({
        message: content,
        sessionId: currentSessionId.value
      })

      const aiMessage = {
        id: Date.now() + 1,
        content: response.answer,
        isUser: false,
        timestamp: new Date(),
        references: response.references
      }

      messages.value.push(aiMessage)
      
      if (response.sessionId) {
        currentSessionId.value = response.sessionId
      }

    } catch (error) {
      console.error('发送消息失败:', error)
      const errorMessage = {
        id: Date.now() + 1,
        content: '抱歉，发送消息时出现错误，请稍后重试。',
        isUser: false,
        timestamp: new Date(),
        isError: true
      }
      messages.value.push(errorMessage)
    } finally {
      isLoading.value = false
      isTyping.value = false
    }
  }

  // 清空消息
  const clearMessages = () => {
    messages.value = []
    currentSessionId.value = null
  }

  // 获取历史消息
  const loadHistory = async () => {
    try {
      const response = await chatAPI.getHistory()
      messages.value = response.messages || []
    } catch (error) {
      console.error('获取历史消息失败:', error)
    }
  }

  return {
    messages,
    isLoading,
    isTyping,
    messageCount,
    lastMessage,
    sendMessage,
    clearMessages,
    loadHistory
  }
})
```

### 2. 文档状态管理

```javascript
// src/stores/document.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { documentAPI } from '@/api/document'

export const useDocumentStore = defineStore('document', () => {
  const documents = ref([])
  const isUploading = ref(false)
  const uploadProgress = ref(0)

  const uploadDocument = async (file) => {
    isUploading.value = true
    uploadProgress.value = 0

    try {
      const formData = new FormData()
      formData.append('file', file)

      const response = await documentAPI.upload(formData, {
        onUploadProgress: (progressEvent) => {
          uploadProgress.value = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
        }
      })

      documents.value.unshift(response.document)
      return response

    } catch (error) {
      console.error('文档上传失败:', error)
      throw error
    } finally {
      isUploading.value = false
      uploadProgress.value = 0
    }
  }

  const deleteDocument = async (documentId) => {
    try {
      await documentAPI.delete(documentId)
      documents.value = documents.value.filter(doc => doc.id !== documentId)
    } catch (error) {
      console.error('删除文档失败:', error)
      throw error
    }
  }

  const loadDocuments = async () => {
    try {
      const response = await documentAPI.list()
      documents.value = response.documents || []
    } catch (error) {
      console.error('获取文档列表失败:', error)
    }
  }

  return {
    documents,
    isUploading,
    uploadProgress,
    uploadDocument,
    deleteDocument,
    loadDocuments
  }
})
```

## 🔌 API接口集成

### 1. HTTP请求配置

```javascript
// src/api/index.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加认证token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('API请求错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          ElMessage.error('未授权，请重新登录')
          // 跳转到登录页
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 500:
          ElMessage.error('服务器错误')
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

export default request
```

### 2. 聊天API

```javascript
// src/api/chat.js
import request from './index'

export const chatAPI = {
  // 发送消息
  sendMessage: (data) => {
    return request.post('/chat/message', data)
  },

  // 获取对话历史
  getHistory: (sessionId) => {
    return request.get('/chat/history', {
      params: { sessionId }
    })
  },

  // 清空对话
  clearHistory: (sessionId) => {
    return request.delete(`/chat/history/${sessionId}`)
  }
}
```

### 3. 文档API

```javascript
// src/api/document.js
import request from './index'

export const documentAPI = {
  // 上传文档
  upload: (formData, config) => {
    return request.post('/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      ...config
    })
  },

  // 获取文档列表
  list: (params) => {
    return request.get('/documents', { params })
  },

  // 删除文档
  delete: (documentId) => {
    return request.delete(`/documents/${documentId}`)
  },

  // 获取文档详情
  getDetail: (documentId) => {
    return request.get(`/documents/${documentId}`)
  }
}
```

## 🎨 样式设计

### 1. 全局样式

```css
/* src/styles/index.css */
@import 'element-plus/dist/index.css';
@import './variables.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
```

### 2. CSS变量

```css
/* src/styles/variables.css */
:root {
  /* 颜色系统 */
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;

  /* 背景色 */
  --bg-color: #f5f7fa;
  --bg-white: #ffffff;
  --bg-gray: #f5f7fa;

  /* 文字颜色 */
  --text-color: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  /* 边框颜色 */
  --border-color: #dcdfe6;
  --border-light: #e4e7ed;
  --border-lighter: #ebeef5;

  /* 间距 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;

  /* 圆角 */
  --border-radius: 4px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;

  /* 阴影 */
  --box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  --box-shadow-light: 0 2px 4px rgba(0, 0, 0, 0.12);
}

/* 暗色主题 */
@media (prefers-color-scheme: dark) {
  :root {
    --bg-color: #1a1a1a;
    --bg-white: #2d2d2d;
    --text-color: #e5e5e5;
    --text-regular: #c0c0c0;
    --border-color: #404040;
  }
}
```

## 🧪 开发工具和调试

### 1. Vue DevTools
- 安装Vue DevTools浏览器扩展
- 在开发环境中启用devtools

### 2. ESLint配置
```javascript
// .eslintrc.js
module.exports = {
  env: {
    browser: true,
    es2021: true,
    node: true
  },
  extends: [
    'eslint:recommended',
    '@vue/eslint-config-prettier'
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module'
  },
  plugins: ['vue'],
  rules: {
    'vue/multi-word-component-names': 'off',
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off'
  }
}
```

### 3. 开发命令
```bash
# 开发服务器
npm run dev

# 代码检查
npm run lint

# 格式化代码
npm run format

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

## 📱 响应式设计

### 1. 断点设计
```css
/* 响应式断点 */
@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
  }
  
  .sidebar {
    display: none;
  }
}

@media (max-width: 480px) {
  .chat-window {
    height: 100vh;
    border-radius: 0;
  }
}
```

### 2. 移动端适配
- 使用viewport meta标签
- 触摸友好的按钮大小
- 优化滚动体验

## 🚀 性能优化

### 1. 组件懒加载
```javascript
// 路由懒加载
const Chat = () => import('@/views/Chat.vue')
const Documents = () => import('@/views/Documents.vue')
```

### 2. 图片优化
- 使用WebP格式
- 实现图片懒加载
- 压缩图片资源

### 3. 打包优化
```javascript
// vite.config.js 构建优化
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          ui: ['element-plus']
        }
      }
    }
  }
})
```

## 📝 最佳实践

1. **组件设计**：单一职责，高复用性
2. **状态管理**：合理使用Pinia，避免过度设计
3. **API调用**：统一错误处理，loading状态管理
4. **样式管理**：使用CSS变量，保持一致性
5. **性能优化**：懒加载、缓存、防抖节流

---

> 详细的组件示例和API文档请参考对应的技术文档
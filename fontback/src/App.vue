<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  SparklesIcon,
  CheckCircleIcon,
  ExclamationTriangleIcon,
  InformationCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'
import ChatWindow from './components/ChatWindow.vue'
import DocumentUpload from './components/DocumentUpload.vue'
import { KnowBaseAPI } from './services/api.js'

// 初始化API实例
const api = new KnowBaseAPI()

// 响应式数据
const activeTab = ref('chat')

// 系统状态
const systemStatus = reactive({
  isHealthy: false,
  responseTime: 0,
  lastChecked: null
})

// 通知系统
const notification = reactive({
  show: false,
  type: 'info', // 'success', 'error', 'info'
  title: '',
  message: ''
})

// 组件引用
const chatWindow = ref(null)
const documentUpload = ref(null)
const mobileChatWindow = ref(null)
const mobileDocumentUpload = ref(null)

// 方法
const showNotification = (type, title, message, duration = 5000) => {
  notification.type = type
  notification.title = title
  notification.message = message
  notification.show = true
  
  setTimeout(() => {
    hideNotification()
  }, duration)
}

const hideNotification = () => {
  notification.show = false
}

const handleUploadSuccess = (data) => {
  showNotification('success', '上传成功', `文档已成功上传并处理`)
  
  // 刷新聊天窗口（如果需要）
  if (chatWindow.value) {
    chatWindow.value.refreshKnowledgeBase()
  }
  if (mobileChatWindow.value) {
    mobileChatWindow.value.refreshKnowledgeBase()
  }
}

const handleUploadError = (error) => {
  const errorMessage = error.response?.data?.message || error.message || '上传失败'
  showNotification('error', '上传失败', errorMessage)
}

const checkSystemHealth = async () => {
  try {
    const startTime = Date.now()
    const response = await api.checkHealth()
    const endTime = Date.now()
    
    systemStatus.isHealthy = response.status === 'UP'
    systemStatus.responseTime = endTime - startTime
    systemStatus.lastChecked = new Date().toISOString()
  } catch (error) {
    console.error('Health check failed:', error)
    systemStatus.isHealthy = false
    systemStatus.responseTime = 0
    systemStatus.lastChecked = new Date().toISOString()
  }
}

// 生命周期钩子
onMounted(async () => {
  // 初始健康检查
  await checkSystemHealth()
  
  // 定期健康检查（每30秒）
  setInterval(checkSystemHealth, 30000)
  
  // 显示欢迎消息
  showNotification('info', '欢迎使用 KnowBase', '开始上传文档或直接开始对话吧！', 3000)
})
</script>

<template>
  <div id="app" class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
    <!-- 顶部导航栏 -->
    <header class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo和标题 -->
          <div class="flex items-center space-x-4">
            <div class="flex items-center space-x-3">
              <div class="w-8 h-8 bg-gradient-to-r from-primary-500 to-primary-600 rounded-lg flex items-center justify-center">
                <SparklesIcon class="w-5 h-5 text-white" />
              </div>
              <div>
                <h1 class="text-xl font-bold text-gray-900">KnowBase</h1>
                <p class="text-xs text-gray-500">RAG AI 知识库系统</p>
              </div>
            </div>
          </div>

          <!-- 右侧操作按钮 -->
          <div class="flex items-center space-x-4">
            <!-- 切换视图按钮 -->
            <div class="hidden sm:flex bg-gray-100 rounded-lg p-1">
              <button
                @click="activeTab = 'chat'"
                :class="[
                  'px-3 py-1 rounded-md text-sm font-medium transition-all duration-200',
                  activeTab === 'chat' 
                    ? 'bg-white text-primary-600 shadow-sm' 
                    : 'text-gray-600 hover:text-gray-900'
                ]"
              >
                💬 对话
              </button>
              <button
                @click="activeTab = 'upload'"
                :class="[
                  'px-3 py-1 rounded-md text-sm font-medium transition-all duration-200',
                  activeTab === 'upload' 
                    ? 'bg-white text-primary-600 shadow-sm' 
                    : 'text-gray-600 hover:text-gray-900'
                ]"
              >
                📄 上传
              </button>
            </div>

            <!-- 状态指示器 -->
            <div class="flex items-center space-x-2">
              <div 
                :class="[
                  'w-2 h-2 rounded-full',
                  systemStatus.isHealthy ? 'bg-green-400' : 'bg-red-400'
                ]"
              ></div>
              <span class="text-sm text-gray-600">
                {{ systemStatus.isHealthy ? '系统正常' : '系统异常' }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- 移动端标签切换 -->
      <div class="sm:hidden mb-6">
        <div class="flex bg-white rounded-lg shadow-sm border border-gray-200 p-1">
          <button
            @click="activeTab = 'chat'"
            :class="[
              'flex-1 px-4 py-2 rounded-md text-sm font-medium transition-all duration-200',
              activeTab === 'chat' 
                ? 'bg-primary-500 text-white shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            💬 AI对话
          </button>
          <button
            @click="activeTab = 'upload'"
            :class="[
              'flex-1 px-4 py-2 rounded-md text-sm font-medium transition-all duration-200',
              activeTab === 'upload' 
                ? 'bg-primary-500 text-white shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
          >
            📄 文档上传
          </button>
        </div>
      </div>

      <!-- 桌面端双栏布局 -->
      <div class="hidden sm:grid sm:grid-cols-5 sm:gap-8 h-[calc(100vh-8rem)]">
        <!-- 左侧：文档上传区域 -->
        <div class="col-span-2">
          <DocumentUpload 
            @upload-success="handleUploadSuccess"
            @upload-error="handleUploadError"
            ref="documentUpload"
          />
        </div>

        <!-- 右侧：聊天区域 -->
        <div class="col-span-3">
          <ChatWindow ref="chatWindow" />
        </div>
      </div>

      <!-- 移动端单栏布局 -->
      <div class="sm:hidden">
        <!-- 对话标签页 -->
        <div v-show="activeTab === 'chat'" class="h-[calc(100vh-12rem)]">
          <ChatWindow ref="mobileChatWindow" />
        </div>

        <!-- 上传标签页 -->
        <div v-show="activeTab === 'upload'">
          <DocumentUpload 
            @upload-success="handleUploadSuccess"
            @upload-error="handleUploadError"
            ref="mobileDocumentUpload"
          />
        </div>
      </div>
    </main>

    <!-- 全局通知 -->
    <Transition
      enter-active-class="transition ease-out duration-300"
      enter-from-class="opacity-0 translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition ease-in duration-200"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-2"
    >
      <div
        v-if="notification.show"
        :class="[
          'fixed top-20 right-4 max-w-sm bg-white rounded-lg shadow-lg border border-gray-200 p-4 z-50',
          notification.type === 'success' ? 'border-l-4 border-l-green-400' :
          notification.type === 'error' ? 'border-l-4 border-l-red-400' :
          'border-l-4 border-l-blue-400'
        ]"
      >
        <div class="flex items-start space-x-3">
          <div
            :class="[
              'w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0',
              notification.type === 'success' ? 'bg-green-100' :
              notification.type === 'error' ? 'bg-red-100' :
              'bg-blue-100'
            ]"
          >
            <CheckCircleIcon 
              v-if="notification.type === 'success'"
              class="w-4 h-4 text-green-600" 
            />
            <ExclamationTriangleIcon 
              v-else-if="notification.type === 'error'"
              class="w-4 h-4 text-red-600" 
            />
            <InformationCircleIcon 
              v-else
              class="w-4 h-4 text-blue-600" 
            />
          </div>
          <div class="flex-1">
            <h4 
              :class="[
                'font-medium text-sm',
                notification.type === 'success' ? 'text-green-800' :
                notification.type === 'error' ? 'text-red-800' :
                'text-blue-800'
              ]"
            >
              {{ notification.title }}
            </h4>
            <p 
              :class="[
                'text-sm mt-1',
                notification.type === 'success' ? 'text-green-700' :
                notification.type === 'error' ? 'text-red-700' :
                'text-blue-700'
              ]"
            >
              {{ notification.message }}
            </p>
          </div>
          <button
            @click="hideNotification"
            class="text-gray-400 hover:text-gray-600"
          >
            <XMarkIcon class="w-4 h-4" />
          </button>
        </div>
      </div>
    </Transition>

    <!-- 底部信息栏 -->
    <footer class="bg-white border-t border-gray-200 mt-auto">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div class="flex flex-col sm:flex-row justify-between items-center space-y-2 sm:space-y-0">
          <div class="flex items-center space-x-4 text-sm text-gray-500">
            <span>© 2024 KnowBase RAG AI System</span>
            <span>•</span>
            <span>基于 Spring AI + Vue 3 构建</span>
          </div>
          <div class="flex items-center space-x-4 text-sm text-gray-500">
            <span>API状态: {{ systemStatus.isHealthy ? '✅ 正常' : '❌ 异常' }}</span>
            <span>•</span>
            <span>响应时间: {{ systemStatus.responseTime }}ms</span>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style>
/* 全局样式 */
@import 'tailwindcss/base';
@import 'tailwindcss/components';
@import 'tailwindcss/utilities';

/* 自定义全局样式 */
#app {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 自定义动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { 
    transform: translateY(10px); 
    opacity: 0; 
  }
  to { 
    transform: translateY(0); 
    opacity: 1; 
  }
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-in-out;
}

.animate-slide-up {
  animation: slideUp 0.3s ease-out;
}

/* 确保内容区域占满视口高度 */
#app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
}
</style>

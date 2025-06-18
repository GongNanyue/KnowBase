<template>
  <div class="chat-window bg-white rounded-lg shadow-sm border border-gray-200 flex flex-col h-full">
    <!-- 聊天头部 -->
    <div class="chat-header bg-gradient-to-r from-primary-500 to-primary-600 p-4 rounded-t-lg">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-white/20 rounded-full flex items-center justify-center">
            <ChatBubbleLeftRightIcon class="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 class="text-white font-semibold text-lg">KnowBase AI 助手</h2>
            <p class="text-primary-100 text-sm">基于您的知识库回答问题</p>
          </div>
        </div>
        <div class="flex items-center space-x-2">
          <div 
            :class="[
              'w-2 h-2 rounded-full',
              isConnected ? 'bg-green-400' : 'bg-red-400'
            ]"
          ></div>
          <span class="text-primary-100 text-sm">
            {{ isConnected ? '已连接' : '未连接' }}
          </span>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div 
      ref="messagesContainer"
      class="messages-container flex-1 p-4 overflow-y-auto space-y-4 bg-gray-50"
      style="min-height: 400px; max-height: 600px;"
    >
      <!-- 欢迎消息 -->
      <div v-if="messages.length === 0" class="text-center py-8">
        <div class="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <SparklesIcon class="w-8 h-8 text-primary-500" />
        </div>
        <h3 class="text-gray-700 font-medium mb-2">欢迎使用 KnowBase AI</h3>
        <p class="text-gray-500 text-sm max-w-md mx-auto">
          请先上传文档到知识库，然后就可以开始提问了。我会基于您的文档内容为您提供准确的回答。
        </p>
      </div>

      <!-- 消息气泡 -->
      <div
        v-for="message in messages"
        :key="message.id"
        :class="[
          'message-item animate-fade-in',
          message.isUser ? 'flex justify-end' : 'flex justify-start'
        ]"
      >
        <div
          :class="[
            'max-w-2xl px-4 py-3 rounded-lg',
            message.isUser
              ? 'bg-primary-500 text-white rounded-br-sm'
              : 'bg-white border border-gray-200 text-gray-800 rounded-bl-sm shadow-sm'
          ]"
        >
          <!-- 用户消息 -->
          <div v-if="message.isUser" class="text-right">
            <p class="break-words">{{ message.content }}</p>
            <span class="text-primary-100 text-xs mt-1 block">
              {{ formatTime(message.timestamp) }}
            </span>
          </div>

          <!-- AI回复 -->
          <div v-else>
            <div class="flex items-start space-x-3">
              <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                <SparklesIcon class="w-4 h-4 text-primary-500" />
              </div>
              <div class="flex-1">
                <div class="prose prose-sm max-w-none">
                  <p class="break-words whitespace-pre-wrap m-0">{{ message.content }}</p>
                </div>
                
                <!-- 引用文档 -->
                <div v-if="message.references && message.references.length > 0" class="mt-3">
                  <div class="text-xs text-gray-500 mb-2 flex items-center">
                    <DocumentTextIcon class="w-3 h-3 mr-1" />
                    参考文档：
                  </div>
                  <div class="space-y-1">
                    <div
                      v-for="(ref, index) in message.references"
                      :key="index"
                      class="text-xs bg-gray-50 px-2 py-1 rounded border-l-2 border-primary-200 text-gray-600"
                    >
                      📄 {{ ref }}
                    </div>
                  </div>
                </div>

                <span class="text-gray-400 text-xs mt-2 block">
                  {{ formatTime(message.timestamp) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载指示器 -->
      <div v-if="isLoading" class="flex justify-start animate-fade-in">
        <div class="bg-white border border-gray-200 rounded-lg rounded-bl-sm px-4 py-3 shadow-sm">
          <div class="flex items-center space-x-3">
            <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
              <SparklesIcon class="w-4 h-4 text-primary-500" />
            </div>
            <div class="flex items-center space-x-2">
              <div class="flex space-x-1">
                <div class="w-2 h-2 bg-primary-400 rounded-full animate-bounce"></div>
                <div class="w-2 h-2 bg-primary-400 rounded-full animate-bounce" style="animation-delay: 0.1s"></div>
                <div class="w-2 h-2 bg-primary-400 rounded-full animate-bounce" style="animation-delay: 0.2s"></div>
              </div>
              <span class="text-gray-500 text-sm">AI正在思考...</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area border-t border-gray-200 p-4 bg-white rounded-b-lg">
      <form @submit.prevent="sendMessage" class="flex space-x-3">
        <div class="flex-1 relative">
          <textarea
            v-model="inputMessage"
            @keydown="handleKeydown"
            placeholder="输入您的问题..."
            rows="1"
            class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none transition-all duration-200"
            style="min-height: 48px; max-height: 120px;"
            :disabled="isLoading"
          ></textarea>
          <div class="absolute right-3 bottom-3 text-xs text-gray-400">
            {{ inputMessage.length }}/2000
          </div>
        </div>
        <button
          type="submit"
          :disabled="!inputMessage.trim() || isLoading || inputMessage.length > 2000"
          class="bg-primary-500 hover:bg-primary-600 disabled:bg-gray-300 text-white px-6 py-3 rounded-lg transition-colors duration-200 flex items-center space-x-2 disabled:cursor-not-allowed"
        >
          <PaperAirplaneIcon class="w-4 h-4" />
          <span class="hidden sm:inline">发送</span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, watch } from 'vue'
import { 
  ChatBubbleLeftRightIcon, 
  SparklesIcon, 
  DocumentTextIcon,
  PaperAirplaneIcon
} from '@heroicons/vue/24/outline'
import { KnowBaseAPI } from '../services/api.js'

// 响应式数据
const messages = reactive([])
const inputMessage = ref('')
const isLoading = ref(false)
const isConnected = ref(false)
const messagesContainer = ref(null)

// 检查服务连接状态
const checkConnection = async () => {
  try {
    await KnowBaseAPI.checkHealth()
    isConnected.value = true
  } catch (error) {
    isConnected.value = false
    console.error('服务连接失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = {
    id: Date.now(),
    content: inputMessage.value.trim(),
    isUser: true,
    timestamp: Date.now()
  }

  messages.push(userMessage)
  const messageContent = inputMessage.value.trim()
  inputMessage.value = ''
  isLoading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    const response = await KnowBaseAPI.sendMessage(messageContent)
    
    const aiMessage = {
      id: Date.now() + 1,
      content: response.answer,
      isUser: false,
      timestamp: response.timestamp,
      references: response.references || []
    }

    messages.push(aiMessage)
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
    
  } catch (error) {
    const errorMessage = {
      id: Date.now() + 1,
      content: `抱歉，处理您的消息时出现错误：${error.message}`,
      isUser: false,
      timestamp: Date.now(),
      references: []
    }
    messages.push(errorMessage)
    
    await nextTick()
    scrollToBottom()
  } finally {
    isLoading.value = false
  }
}

// 处理键盘事件
const handleKeydown = (event) => {
  // Ctrl/Cmd + Enter 发送消息
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault()
    sendMessage()
  }
  // 自动调整高度
  const target = event.target
  target.style.height = 'auto'
  target.style.height = Math.min(target.scrollHeight, 120) + 'px'
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (date.toDateString() === now.toDateString()) { // 今天
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleString('zh-CN', { 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }
}

// 监听新消息，自动滚动
watch(messages, async () => {
  await nextTick()
  scrollToBottom()
}, { deep: true })

// 组件挂载时检查连接
onMounted(() => {
  checkConnection()
  // 定期检查连接状态
  setInterval(checkConnection, 30000)
})

// 向外暴露方法
defineExpose({
  addSystemMessage: (content) => {
    messages.push({
      id: Date.now(),
      content,
      isUser: false,
      timestamp: Date.now(),
      references: []
    })
  }
})
</script>

<style scoped>
.messages-container {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.prose p {
  margin: 0;
  line-height: 1.6;
}

textarea {
  field-sizing: content;
}
</style> 
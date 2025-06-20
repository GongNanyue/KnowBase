<template>
  <div class="document-upload bg-white rounded-lg shadow-sm border border-gray-200 p-6">
    <!-- 头部 -->
    <div class="mb-6">
      <div class="flex items-center space-x-3 mb-2">
        <div class="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
          <DocumentArrowUpIcon class="w-5 h-5 text-primary-500" />
        </div>
        <h3 class="text-lg font-semibold text-gray-800">文档上传</h3>
      </div>
      <p class="text-gray-600 text-sm">
        上传文档到知识库，支持 PDF、Word、Excel、PowerPoint、文本等格式
      </p>
    </div>

    <!-- 上传区域 -->
    <div
      @drop="handleDrop"
      @dragover.prevent
      @dragenter.prevent
      :class="[
        'upload-zone border-2 border-dashed rounded-lg p-8 text-center transition-all duration-200 cursor-pointer',
        isDragging ? 'border-primary-400 bg-primary-50' : 'border-gray-300 hover:border-primary-400 hover:bg-gray-50'
      ]"
      @click="triggerFileInput"
    >
      <input
        ref="fileInput"
        type="file"
        class="hidden"
        :accept="acceptedTypes"
        @change="handleFileSelect"
        :disabled="isUploading"
      />

      <!-- 上传图标和提示 -->
      <div v-if="!selectedFile && !isUploading">
        <CloudArrowUpIcon class="w-12 h-12 text-gray-400 mx-auto mb-4" />
        <h4 class="text-lg font-medium text-gray-700 mb-2">选择文件或拖拽到此处</h4>
        <p class="text-gray-500 text-sm mb-4">
          支持 PDF、DOC、DOCX、TXT、MD、XLS、XLSX、PPT、PPTX 格式
        </p>
        <p class="text-gray-400 text-xs">
          最大文件大小：50MB
        </p>
      </div>

      <!-- 选中的文件信息 -->
      <div v-else-if="selectedFile && !isUploading" class="space-y-4">
        <div class="flex items-center justify-center space-x-3">
          <DocumentTextIcon class="w-8 h-8 text-primary-500" />
          <div class="text-left">
            <p class="font-medium text-gray-700">{{ selectedFile.name }}</p>
            <p class="text-sm text-gray-500">{{ formatFileSize(selectedFile.size) }}</p>
          </div>
        </div>
        <div class="flex space-x-3 justify-center">
          <button
            @click.stop="uploadFile"
            class="bg-primary-500 hover:bg-primary-600 text-white px-4 py-2 rounded-lg transition-colors duration-200 flex items-center space-x-2"
          >
            <CloudArrowUpIcon class="w-4 h-4" />
            <span>开始上传</span>
          </button>
          <button
            @click.stop="clearSelection"
            class="bg-gray-200 hover:bg-gray-300 text-gray-700 px-4 py-2 rounded-lg transition-colors duration-200"
          >
            取消
          </button>
        </div>
      </div>

      <!-- 上传进度 -->
      <div v-else-if="isUploading" class="space-y-4">
        <div class="flex items-center justify-center space-x-3">
          <div class="animate-spin">
            <ArrowPathIcon class="w-8 h-8 text-primary-500" />
          </div>
          <div class="text-left">
            <p class="font-medium text-gray-700">正在上传文档...</p>
            <p class="text-sm text-gray-500">{{ selectedFile?.name }}</p>
          </div>
        </div>
        
        <!-- 进度条 -->
        <div class="w-full bg-gray-200 rounded-full h-2">
          <div
            class="bg-primary-500 h-2 rounded-full transition-all duration-300"
            :style="{ width: uploadProgress + '%' }"
          ></div>
        </div>
        <p class="text-sm text-gray-600">{{ uploadProgress }}% 完成</p>
      </div>
    </div>

    <!-- 上传历史/状态 -->
    <div v-if="uploadHistory.length > 0" class="mt-6">
      <h4 class="text-sm font-medium text-gray-700 mb-3 flex items-center">
        <ClockIcon class="w-4 h-4 mr-2" />
        上传历史
      </h4>
      <div class="space-y-2 max-h-40 overflow-y-auto">
        <div
          v-for="(item, index) in uploadHistory"
          :key="index"
          :class="[
            'flex items-center justify-between p-3 rounded-lg border',
            item.status === 'success' ? 'bg-green-50 border-green-200' :
            item.status === 'error' ? 'bg-red-50 border-red-200' :
            'bg-yellow-50 border-yellow-200'
          ]"
        >
          <div class="flex items-center space-x-3">
            <div
              :class="[
                'w-2 h-2 rounded-full',
                item.status === 'success' ? 'bg-green-400' :
                item.status === 'error' ? 'bg-red-400' :
                'bg-yellow-400'
              ]"
            ></div>
            <div>
              <p class="text-sm font-medium text-gray-700">{{ item.fileName }}</p>
              <p
                :class="[
                  'text-xs',
                  item.status === 'success' ? 'text-green-600' :
                  item.status === 'error' ? 'text-red-600' :
                  'text-yellow-600'
                ]"
              >
                {{ item.message }}
              </p>
            </div>
          </div>
          <span class="text-xs text-gray-400">
            {{ formatTime(item.timestamp) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div
      v-if="errorMessage"
      class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4 flex items-start space-x-3 animate-fade-in"
    >
      <ExclamationTriangleIcon class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
      <div>
        <h4 class="text-red-800 font-medium">上传失败</h4>
        <p class="text-red-700 text-sm mt-1">{{ errorMessage }}</p>
      </div>
      <button
        @click="errorMessage = ''"
        class="text-red-400 hover:text-red-600"
      >
        <XMarkIcon class="w-4 h-4" />
      </button>
    </div>

    <!-- 成功提示 -->
    <div
      v-if="successMessage"
      class="mt-4 bg-green-50 border border-green-200 rounded-lg p-4 flex items-start space-x-3 animate-fade-in"
    >
      <CheckCircleIcon class="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" />
      <div>
        <h4 class="text-green-800 font-medium">上传成功</h4>
        <p class="text-green-700 text-sm mt-1">{{ successMessage }}</p>
      </div>
      <button
        @click="successMessage = ''"
        class="text-green-400 hover:text-green-600"
      >
        <XMarkIcon class="w-4 h-4" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import {
  DocumentArrowUpIcon,
  CloudArrowUpIcon,
  DocumentTextIcon,
  ArrowPathIcon,
  ClockIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'
import { KnowBaseAPI } from '../services/api.js'

// 定义事件
const emit = defineEmits(['upload-success', 'upload-error'])

// 响应式数据
const fileInput = ref(null)
const selectedFile = ref(null)
const isUploading = ref(false)
const uploadProgress = ref(0)
const isDragging = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const uploadHistory = reactive([])

// 支持的文件类型
const acceptedTypes = '.pdf,.doc,.docx,.txt,.md,.xls,.xlsx,.ppt,.pptx'

// 触发文件选择
const triggerFileInput = () => {
  if (!isUploading.value) {
    fileInput.value?.click()
  }
}

// 处理文件选择
const handleFileSelect = (event) => {
  const file = event.target.files?.[0]
  if (file) {
    validateAndSetFile(file)
  }
}

// 处理拖拽上传
const handleDrop = (event) => {
  event.preventDefault()
  isDragging.value = false
  
  const files = event.dataTransfer.files
  if (files.length > 0) {
    validateAndSetFile(files[0])
  }
}

// 验证并设置文件
const validateAndSetFile = (file) => {
  errorMessage.value = ''
  
  // 检查文件大小
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    errorMessage.value = '文件大小不能超过50MB'
    return
  }

  // 检查文件类型
  const allowedExtensions = ['pdf', 'doc', 'docx', 'txt', 'md', 'xls', 'xlsx', 'ppt', 'pptx']
  const fileExtension = file.name.split('.').pop()?.toLowerCase()
  
  if (!fileExtension || !allowedExtensions.includes(fileExtension)) {
    errorMessage.value = '不支持的文件格式，请选择 PDF、DOC、DOCX、TXT、MD、XLS、XLSX、PPT、PPTX 格式的文件'
    return
  }

  selectedFile.value = file
}

// 清除选择
const clearSelection = () => {
  selectedFile.value = null
  uploadProgress.value = 0
  errorMessage.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

// 上传文件
const uploadFile = async () => {
  if (!selectedFile.value || isUploading.value) return

  isUploading.value = true
  uploadProgress.value = 0
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const response = await new KnowBaseAPI().uploadDocument(
      selectedFile.value,
      (progress) => {
        uploadProgress.value = progress
      }
    )

    // 上传成功
    successMessage.value = response.message
    
    // 添加到历史记录
    uploadHistory.unshift({
      fileName: selectedFile.value.name,
      message: response.message,
      status: 'success',
      timestamp: Date.now()
    })

    // 保持历史记录数量
    if (uploadHistory.length > 5) {
      uploadHistory.splice(5)
    }

    // 发出成功事件
    emit('upload-success', {
      fileName: selectedFile.value.name,
      message: response.message
    })

    // 清除选择
    clearSelection()

  } catch (error) {
    errorMessage.value = error.message
    
    // 添加到历史记录
    uploadHistory.unshift({
      fileName: selectedFile.value.name,
      message: error.message,
      status: 'error',
      timestamp: Date.now()
    })

    // 保持历史记录数量
    if (uploadHistory.length > 5) {
      uploadHistory.splice(5)
    }

    // 发出错误事件
    emit('upload-error', {
      fileName: selectedFile.value.name,
      error: error.message
    })

  } finally {
    isUploading.value = false
  }
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
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
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

// 处理拖拽事件
const handleDragEnter = () => {
  isDragging.value = true
}

const handleDragLeave = (event) => {
  // 只有当离开整个上传区域时才设置为false
  if (!event.currentTarget.contains(event.relatedTarget)) {
    isDragging.value = false
  }
}

// 向外暴露方法
defineExpose({
  clearSelection,
  uploadHistory
})
</script>

<style scoped>
.upload-zone {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 拖拽时的视觉反馈 */
.upload-zone.dragging {
  border-color: #3b82f6;
  background-color: #eff6ff;
}
</style> 
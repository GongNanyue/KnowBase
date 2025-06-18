import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    console.log('发送请求:', config.method?.toUpperCase(), config.url)
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    console.log('响应成功:', response.status, response.config.url)
    return response
  },
  (error) => {
    console.error('响应错误:', error.response?.status, error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export class KnowBaseAPI {
  /**
   * 检查服务健康状态
   */
  static async checkHealth() {
    try {
      const response = await api.get('/health')
      return response.data
    } catch (error) {
      throw new Error(`健康检查失败: ${error.message}`)
    }
  }

  /**
   * 发送消息给AI
   * @param {string} message - 用户消息
   */
  static async sendMessage(message) {
    try {
      if (!message || message.trim().length === 0) {
        throw new Error('消息内容不能为空')
      }

      if (message.length > 2000) {
        throw new Error('消息长度不能超过2000字符')
      }

      const response = await api.post('/chat/message', { message: message.trim() })
      return response.data
    } catch (error) {
      if (error.response?.status === 400) {
        throw new Error('请求参数无效，请检查消息内容')
      } else if (error.response?.status === 502) {
        throw new Error('AI服务暂时不可用，请稍后重试')
      } else if (error.response?.status >= 500) {
        throw new Error('服务器内部错误，请稍后重试')
      }
      throw new Error(`发送消息失败: ${error.message}`)
    }
  }

  /**
   * 上传文档
   * @param {File} file - 文件对象
   * @param {Function} onProgress - 进度回调函数
   */
  static async uploadDocument(file, onProgress = null) {
    try {
      if (!file) {
        throw new Error('请选择要上传的文件')
      }

      // 检查文件大小 (50MB)
      const maxSize = 50 * 1024 * 1024
      if (file.size > maxSize) {
        throw new Error('文件大小不能超过50MB')
      }

      // 检查文件类型
      const allowedTypes = [
        'application/pdf',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'text/plain',
        'text/markdown',
        'application/vnd.ms-excel',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'application/vnd.ms-powerpoint',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation'
      ]
      
      if (!allowedTypes.includes(file.type) && !file.name.match(/\.(txt|md|pdf|doc|docx|xls|xlsx|ppt|pptx)$/i)) {
        throw new Error('不支持的文件格式，请上传PDF、DOC、DOCX、TXT、MD等格式的文件')
      }

      const formData = new FormData()
      formData.append('file', file)

      const response = await api.post('/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (progressEvent) => {
          if (onProgress && progressEvent.total) {
            const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            onProgress(progress)
          }
        }
      })

      return response.data
    } catch (error) {
      if (error.response?.status === 413) {
        throw new Error('文件太大，请选择小于50MB的文件')
      } else if (error.response?.status === 415) {
        throw new Error('不支持的文件格式')
      } else if (error.response?.status >= 500) {
        throw new Error('服务器处理文件时出错，请稍后重试')
      }
      throw new Error(`上传失败: ${error.message}`)
    }
  }
}

export default api 
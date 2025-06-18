# KnowBase 前端

> 基于 Vue 3 + Tailwind CSS 构建的现代化 RAG AI 知识库前端界面

## 📋 功能特点

- 🎨 **现代化设计** - 采用 Tailwind CSS，界面简约美观
- 💬 **智能对话** - 与 AI 进行基于知识库的对话
- 📄 **文档上传** - 支持多种格式文档上传到知识库
- 📱 **响应式设计** - 完美适配桌面端和移动端
- ⚡ **实时交互** - 文件上传进度、消息状态等实时反馈
- 🔔 **通知系统** - 优雅的全局通知提示

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **样式**: Tailwind CSS
- **图标**: Heroicons
- **HTTP客户端**: Axios
- **UI组件**: Headless UI

## 🚀 快速开始

### 1. 安装依赖

```bash
npm install
# 或
yarn install
# 或
pnpm install
```

### 2. 启动开发服务器

```bash
npm run dev
# 或
yarn dev
# 或
pnpm dev
```

### 3. 访问应用

浏览器打开 [http://localhost:5173](http://localhost:5173)

## 📁 项目结构

```
fontback/
├── src/
│   ├── components/          # Vue组件
│   │   ├── ChatWindow.vue   # 聊天窗口组件
│   │   └── DocumentUpload.vue # 文档上传组件
│   ├── services/            # 服务层
│   │   └── api.js          # API客户端
│   ├── assets/             # 静态资源
│   │   └── main.css        # 主样式文件
│   ├── App.vue             # 根组件
│   └── main.js             # 应用入口
├── public/                 # 公共资源
├── index.html             # HTML模板
├── package.json           # 项目配置
├── tailwind.config.js     # Tailwind配置
├── postcss.config.js      # PostCSS配置
└── vite.config.js         # Vite配置
```

## 🎨 主要组件

### ChatWindow 聊天窗口
- 智能对话界面
- 消息历史记录
- 实时状态显示
- 引用文档展示

### DocumentUpload 文档上传
- 拖拽上传支持
- 文件格式验证
- 上传进度显示
- 历史记录管理

### API 服务
- 健康检查
- 消息发送
- 文档上传
- 错误处理

## 📱 响应式设计

### 桌面端 (≥640px)
- 双栏布局：左侧上传，右侧对话
- 丰富的交互效果
- 完整功能展示

### 移动端 (<640px)
- 标签页切换
- 触摸优化
- 简化界面

## 🎯 API 接口

前端与后端通过以下接口通信：

- `GET /api/health` - 健康检查
- `POST /api/chat/message` - 发送消息
- `POST /api/documents/upload` - 上传文档

> 后端服务地址：http://localhost:8080

## 🔧 配置

### 环境变量

可在 `.env` 文件中配置：

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_TITLE=KnowBase RAG AI
```

### Tailwind 自定义

在 `tailwind.config.js` 中自定义主题：

```js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          500: '#0ea5e9',
          600: '#0284c7',
          // ...
        }
      }
    }
  }
}
```

## 📦 构建部署

### 构建生产版本

```bash
npm run build
```

### 预览构建结果

```bash
npm run preview
```

## 🐛 故障排除

### 常见问题

1. **依赖安装失败**
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   ```

2. **后端连接失败**
   - 确认后端服务正在运行 (http://localhost:8080)
   - 检查网络和防火墙设置

3. **样式不生效**
   ```bash
   npm run dev -- --force
   ```

### 开发工具

推荐使用以下浏览器扩展：
- Vue DevTools
- Tailwind CSS IntelliSense

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 🔗 相关链接

- [Vue 3 文档](https://vuejs.org/)
- [Tailwind CSS 文档](https://tailwindcss.com/)
- [Vite 文档](https://vitejs.dev/)
- [后端 API 文档](../docs/backend/api-documentation.md)

---

> **提示**: 首次使用时，请先启动后端服务，然后上传一些文档到知识库，这样就可以开始智能对话了！

# KnowBase RAG AI 对话系统 - 开发文档

> 完整的开发指南和技术文档，帮助开发者快速理解和开发KnowBase系统

## 📚 文档目录

### 🏗️ 架构设计
- [系统架构设计](./architecture/system-architecture.md) - 整体架构和技术选型
- [RAG工作流程](./architecture/rag-workflow.md) - 检索增强生成的详细流程
- [模块设计](./architecture/module-design.md) - 各模块职责和接口设计
- [数据流设计](./architecture/data-flow.md) - 系统内数据流转机制

### 💻 后端开发
- [后端开发指南](./backend/development-guide.md) - Spring Boot项目开发指南
- [核心模块实现](./backend/core-modules.md) - RAG核心服务实现
- [API接口设计](./backend/api-design.md) - RESTful API规范
- [Milvus集成](./backend/milvus-integration.md) - 向量数据库集成方案
- [LLM集成](./backend/llm-integration.md) - 大语言模型集成

### 🎨 前端开发
- [前端开发指南](./frontend/development-guide.md) - Vue 3项目开发指南
- [UI组件设计](./frontend/ui-components.md) - 组件库和自定义组件
- [状态管理](./frontend/state-management.md) - 应用状态管理方案
- [API集成](./frontend/api-integration.md) - 前后端接口调用

### 🗄️ 数据库设计
- [Milvus设计](./database/milvus-design.md) - 向量数据库设计
- [向量索引策略](./database/vector-index.md) - 向量检索优化
- [元数据管理](./database/metadata-management.md) - 业务数据存储

### 🚀 部署运维
- [Docker部署](./deployment/docker-setup.md) - 容器化部署方案
- [环境配置](./deployment/environment-config.md) - 多环境配置管理
- [监控日志](./deployment/monitoring.md) - 系统监控和日志方案
- [性能优化](./deployment/performance-optimization.md) - 系统性能调优

### 📝 开发规范
- [代码规范](./standards/coding-standards.md) - 代码风格和质量规范
- [Git工作流](./standards/git-workflow.md) - 版本控制和协作规范
- [测试策略](./standards/testing-strategy.md) - 测试方案和质量保证
- [安全规范](./standards/security-guidelines.md) - 安全开发指南

## 🚀 快速开始

1. 阅读 [系统架构设计](./architecture/system-architecture.md) 了解整体架构
2. 按照 [后端开发指南](./backend/development-guide.md) 搭建后端服务
3. 参考 [前端开发指南](./frontend/development-guide.md) 开发用户界面
4. 使用 [Docker部署](./deployment/docker-setup.md) 部署完整系统

## 📖 开发路线图

```mermaid
roadmap
    title KnowBase 开发路线图
    section 第一阶段：核心功能
        后端架构搭建 : 核心模块开发
        Milvus集成 : 向量检索功能
        基础API : RESTful接口
    section 第二阶段：用户界面
        前端界面 : Vue组件开发
        聊天功能 : 实时对话界面
        文档管理 : 上传和管理功能
    section 第三阶段：优化部署
        性能优化 : 检索和生成优化
        系统部署 : 生产环境部署
        监控运维 : 系统监控体系
```

## 🤝 贡献指南

1. Fork本项目
2. 创建功能分支：`git checkout -b feature/amazing-feature`
3. 提交更改：`git commit -m 'Add amazing feature'`
4. 推送分支：`git push origin feature/amazing-feature`
5. 提交Pull Request

## 📞 技术支持

如有问题，请查阅相关文档或提交Issue。

---

> 最后更新：2024年12月
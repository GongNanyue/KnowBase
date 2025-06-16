# KnowBase RAG AI 对话系统

> 一个基于 **Retrieval-Augmented Generation (RAG)** 架构的对话式知识库，后端采用 **Spring Boot**，前端采用 **Vue 3**，向量数据库使用 **Milvus（Docker 部署）**。

---

## 目录

1. [项目简介](#项目简介)
2. [核心特色](#核心特色)
3. [技术栈](#技术栈)
4. [系统架构](#系统架构)
5. [功能模块](#功能模块)
6. [快速开始](#快速开始)
7. [配置说明](#配置说明)
8. [目录结构](#目录结构)
9. [RAG 工作流程](#rag-工作流程)
10. [常见问题](#常见问题)
11. [Roadmap](#roadmap)

---

## 项目简介

KnowBase 旨在帮助团队快速搭建自身的私有知识问答系统。通过 **RAG** 技术将企业文档、网页、数据库等异构数据整合，并在对话过程中实时检索相关片段增强大语言模型（LLM）的回答准确性与可解释性。

---

## 核心特色

- 🏗️ **模块化设计**：向量检索、LLM 调用、对话管理均解耦，便于替换。
- ⚡ **高效检索**：利用 Milvus 的 HNSW/IVF 等索引实现毫秒级 Top-K 查询。
- ✨ **前端体验**：基于 Vue 3 + Vite + Element Plus，界面简洁且支持暗色模式。
- ☁️ **云原生部署**：Docker & Docker Compose 一键启动全部依赖，轻松迁移。
- 🔒 **数据安全**：所有私有数据本地化存储，不经第三方云端。

---

## 技术栈

| 层级 | 技术 | 说明 |
| ---- | ---- | ---- |
| 前端 | Vue 3 · Vite · JavaScript · Element Plus | 交互式 UI 与对话展示 |
| 后端 | Spring Boot 3 · Spring AI | 对话 API、RAG Pipline |
| 向量库 | Milvus 2.5 (Docker) | 向量存储与相似度检索 |
| 模型 | OpenAI / Azure / 本地 LLM (可切换) | 生成式模型与 Embeddings |
| 其他 | Docker · Docker Compose | 本地及生产环境部署 |

---

## 系统架构

```mermaid
flowchart TD
  subgraph 前端
    A[Vue Chat UI]
  end
  subgraph 后端(Spring Boot)
    B[RESTful API]
    C[Embedding Service]
    D[Retriever]
    E[LLM / Chat Completion]
  end
  subgraph 数据层
    F[Milvus<br/>向量数据库]
    G[Object Storage<br/>(Docs/Images)]
  end
  A -- HTTP --> B
  B --> C
  C --> F
  B --> D
  D -. 查询向量 .-> F
  D -- Top-K Docs --> E
  E -- Answer --> B
  B -- JSON --> A
```

---

## 功能模块

1. **知识导入**：支持 TXT / PDF / Markdown 等格式，自动切分并生成向量。
2. **向量检索**：基于余弦相似度或内积的快速 Top-K 查询。
3. **上下文压缩**：对超长检索结果进行摘要，减少 Token 消耗。
4. **对话记忆**：可选的对话历史缓存，提升多轮连贯性。
5. **权限控制**：JWT + RBAC（待实现）。

---

## 快速开始

### 先决条件

- **Java 17+**
- **Node.js 18+** 与 **pnpm / npm / yarn**
- **Docker & Docker Compose**

### 1. 克隆仓库

```bash
# SSH
$ git clone https://github.com/Saika02/KnowBase.git
# OR HTTPS
$ git clone https://github.com/Saika02/KnowBase.git
$ cd knowbase
```

### 2. 启动 Milvus

```bash
$ cd docker
$ docker compose -f docker-compose.yml-milvus.yml up -d
```
> 这将启动 Milvus、MinIO、etcd 及其依赖容器。

### 3. 启动后端

```bash
$ cd backend
$ ./mvnw spring-boot:run   # 或使用 IDE 运行 KnowBaseApplication
```

### 4. 启动前端

```bash
$ cd ../frontend
$ pnpm i        # 或 npm install / yarn
$ pnpm dev      # 或 npm run dev
```

### 5. 访问系统

浏览器打开 `http://localhost:5173`，即可开始对话体验。

---

## 配置说明

| 变量 | 位置 | 默认值 | 说明 |
| ---- | ---- | ---- | ---- |
| `MILVUS_HOST` | `backend/src/main/resources/application.yml` | localhost | Milvus 服务地址 |
| `MILVUS_PORT` | 同上 | 19530 | Milvus gRPC 端口 |
| `OPENAI_API_KEY` | 环境变量 | - | OpenAI 密钥 |
| `LLM_PROVIDER` | 同上 | openai | 可切换 `local` / `azure` 等 |

> 生产环境推荐以环境变量或 K8s Secret 方式注入敏感信息。

---

## 目录结构

```text
knowbase/
├─ backend/               # Spring Boot 服务
│  ├─ src/main/java/...
│  ├─ src/main/resources/
│  └─ pom.xml
├─ frontend/              # Vue 3 客户端
│  ├─ src/
│  └─ vite.config.ts
├─ docker/
│  └─ docker-compose-milvus.yml
└─ docs/                  # 设计文档与研发笔记
```

---

## RAG 工作流程

1. **文档切分**：按照固定长度或语义边界，将原始文本拆分为段落。  
2. **向量化**：使用 Embedding 模型（如 `text-embedding-3-large` ) 将段落转换为高维向量。  
3. **向量入库**：将向量与元数据写入 Milvus。  
4. **检索增强**：用户提问 → 生成查询向量 → Top-K 相似段落召回。  
5. **Prompt 组装**：将召回段落与用户问题拼接成 Prompt 发送至 LLM。  
6. **答案生成**：LLM 给出回答并返回参考文档出处。  
7. **结果展示**：前端渲染回答文本及引用片段。

---

## 常见问题

1. **Milvus 启动失败怎么办？**  
   - 确认 Docker Desktop 已开启并分配足够内存（≥4 GB）。
2. **本地 LLM 如何接入？**  
   - 目前支持 [LM Studio](https://lmstudio.ai) 提供的 OpenAI-compatible 接口，修改 `LLM_PROVIDER=local` 即可。
3. **如何更新嵌入模型？**  
   - 在后端 `EmbeddingService` 中替换成新的模型调用逻辑，再重新向量化数据。

---



> **License**: MIT © 2024 KnowBase Contributors
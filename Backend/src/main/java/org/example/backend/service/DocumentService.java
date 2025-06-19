package org.example.backend.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Service
public class DocumentService {

    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public String uploadDocument(MultipartFile file) {
        try {
            // 1. 使用Spring AI读取文档
            Resource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            // 2. 使用Tika文档读取器
            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> documents = documentReader.get();

            // 3. 文档分块
            TokenTextSplitter textSplitter = new TokenTextSplitter(5000, 100, 5, 10000, true);
            List<Document> chunks = textSplitter.apply(documents);

            // 4. 添加元数据
            for (int i = 0; i < chunks.size(); i++) {
                Document chunk = chunks.get(i);
                chunk.getMetadata().put("source", file.getOriginalFilename());
                chunk.getMetadata().put("chunk_index", i);
                chunk.getMetadata().put("upload_time", System.currentTimeMillis());
            }

            // 5. 存储到Milvus（Spring AI会自动处理向量化）
            vectorStore.add(chunks);

            return String.format("文档 '%s' 上传成功，共处理 %d 个文档块",
                    file.getOriginalFilename(), chunks.size());

        } catch (Exception e) {
            return "文档上传失败: " + e.getMessage();
        }
    }
}
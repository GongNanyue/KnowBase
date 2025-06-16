package org.example.backend.config;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    // Spring AI会自动配置VectorStore和ChatClient
    // 这里可以添加自定义配置（可选）

    @Bean
    @ConditionalOnMissingBean
    public TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(500, 100, 5, 10000, true);
    }
}
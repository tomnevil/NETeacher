package com.neteacher.server.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.common.ai.AiProperties;
import com.neteacher.common.ai.LlmPort;
import com.neteacher.common.ai.QuestionGeneratePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 适配器装配：根据 ai.llm-provider 选择 mock 或真实大模型实现。
 *
 * <p>对话 {@link LlmPort} 与出题 {@link QuestionGeneratePort} 两套能力各自提供
 * mock 兜底与真实实现，保证无密钥时可降级运行。</p>
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.llm-provider", havingValue = "mock", matchIfMissing = true)
    public LlmPort mockLlmAdapter() {
        return new MockLlmAdapter();
    }

    @Bean
    @ConditionalOnExpression("'${ai.llm-provider:mock}' != 'mock'")
    public LlmPort openAiLlmAdapter(AiProperties props) {
        return new OpenAiLlmAdapter(props);
    }

    // ---------- 出题能力 ----------

    @Bean
    @Primary
    @ConditionalOnProperty(name = "ai.llm-provider", havingValue = "mock", matchIfMissing = true)
    public QuestionGeneratePort mockQuestionGenerateAdapter() {
        return new MockQuestionGenerateAdapter();
    }

    @Bean
    @ConditionalOnExpression("'${ai.llm-provider:mock}' != 'mock'")
    public QuestionGeneratePort llmQuestionGenerateAdapter(LlmPort llmPort, ObjectMapper objectMapper) {
        return new LlmQuestionGenerateAdapter(llmPort, objectMapper);
    }
}

package com.neteacher.server.ai;

import com.neteacher.common.ai.AiProperties;
import com.neteacher.common.ai.LlmPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 适配器装配：根据 ai.llm-provider 选择 mock 或真实大模型实现。
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
}

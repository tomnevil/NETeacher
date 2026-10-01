package com.neteacher.common.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 能力配置（六边形架构：端口与实现解耦）。
 * 通过 ai.llm-provider 切换 mock / deepseek / zhipu；未配置 apiKey 时自动降级为 mock。
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * mock | 任意非 mock 值（如 deepseek / zhipu / cmecloud）。
     * 只要不等于 mock 就会装配真实大模型适配器；未配置 apiKey 时该适配器会降级返回提示文本。
     */
    private String llmProvider = "mock";

    private Llm llm = new Llm();

    private Speech speech = new Speech();

    private Tts tts = new Tts();

    @Data
    public static class Llm {
        /** OpenAI 兼容的 chat/completions 基址 */
        private String baseUrl = "https://api.deepseek.com/v1";
        private String apiKey = "";
        private String model = "deepseek-chat";
    }

    @Data
    public static class Speech {
        private String apiKey = "";
    }

    @Data
    public static class Tts {
        private String apiKey = "";
    }
}

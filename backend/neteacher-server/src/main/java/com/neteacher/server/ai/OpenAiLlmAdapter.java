package com.neteacher.server.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.common.ai.AiProperties;
import com.neteacher.common.ai.LlmPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 真实大模型适配器（OpenAI 兼容协议），可对接 DeepSeek / 智谱 GLM。
 * 通过 ai.llm.base-url / api-key / model 配置，provider 不为 mock 时启用。
 * 调用失败时返回错误说明，避免阻塞主流程。
 */
public class OpenAiLlmAdapter implements LlmPort {

    private final AiProperties.Llm cfg;
    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public OpenAiLlmAdapter(AiProperties props) {
        this.cfg = props.getLlm();
    }

    @Override
    public String chat(String prompt) {
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            return "(未配置 apiKey) " + prompt;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", cfg.getModel(),
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.7
            );
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(cfg.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                return "(api error " + resp.statusCode() + ") " + resp.body();
            }
            JsonNode root = om.readTree(resp.body());
            return root.path("choices").path(0).path("message").path("content").asText("");
        } catch (Exception e) {
            return "(call failed) " + e.getMessage();
        }
    }

    @Override
    public String vendor() {
        return "openai:" + cfg.getModel();
    }
}

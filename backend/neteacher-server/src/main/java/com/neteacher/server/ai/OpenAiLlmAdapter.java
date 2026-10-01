package com.neteacher.server.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.common.ai.AiProperties;
import com.neteacher.common.ai.LlmPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 真实大模型适配器（OpenAI 兼容协议），可对接 DeepSeek / 智谱 GLM / 移动云 tokenplan 网关等。
 * 通过 ai.llm.base-url / api-key / model 配置，provider 不为 mock 时启用。
 * 调用失败时记录日志并返回错误说明，避免阻塞主流程。
 */
public class OpenAiLlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLlmAdapter.class);

    private final AiProperties.Llm cfg;
    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public OpenAiLlmAdapter(AiProperties props) {
        this.cfg = props.getLlm();
    }

    @Override
    public String chat(String prompt) {
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            log.warn("[ai] 未配置 apiKey，本次调用降级返回提示文本");
            return "(未配置 apiKey) " + prompt;
        }
        String url = cfg.getBaseUrl() + "/chat/completions";
        try {
            Map<String, Object> body = Map.of(
                    "model", cfg.getModel(),
                    "messages", List.of(Map.of("role", "user", "content", prompt)),
                    "temperature", 0.7
            );
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    // 推理型/慢模型首 token 可能较久，给足超时
                    .timeout(Duration.ofSeconds(90))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cfg.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 400) {
                log.warn("[ai] 调用失败 status={} url={} body={}",
                        resp.statusCode(), url, abbreviate(resp.body()));
                return "(api error " + resp.statusCode() + ") " + resp.body();
            }
            JsonNode root = om.readTree(resp.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                log.warn("[ai] 返回内容为空 url={} resp={}", url, abbreviate(resp.body()));
            }
            return content;
        } catch (Exception e) {
            log.warn("[ai] 调用异常 url={} msg={}", url, e.getMessage());
            return "(call failed) " + e.getMessage();
        }
    }

    private String abbreviate(String s) {
        if (s == null) {
            return "";
        }
        String flat = s.replaceAll("\\s+", " ").trim();
        return flat.length() > 300 ? flat.substring(0, 300) + "…" : flat;
    }

    @Override
    public String vendor() {
        return "openai:" + cfg.getModel();
    }
}

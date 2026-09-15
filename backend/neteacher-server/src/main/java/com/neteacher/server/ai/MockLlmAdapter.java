package com.neteacher.server.ai;

import com.neteacher.common.ai.LlmPort;

/**
 * 大模型降级实现。provider=mock 或缺少 apiKey 时启用。后续可被 OpenAiLlmAdapter 替换。
 */
public class MockLlmAdapter implements LlmPort {

    @Override
    public String chat(String prompt) {
        return "(mock) 已收到你的提问，请在配置 ai.llm.api-key 后接入真实大模型：" + prompt;
    }

    @Override
    public String vendor() {
        return "mock";
    }
}

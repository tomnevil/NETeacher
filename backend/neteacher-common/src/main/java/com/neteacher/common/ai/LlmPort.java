package com.neteacher.common.ai;

/**
 * 大语言模型能力端口。实现可对接混元大模型 / 自部署 Qwen 系列，用于对话训练、语法纠错、作文批改。
 */
public interface LlmPort {

    String chat(String prompt);

    String vendor();
}

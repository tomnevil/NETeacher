package com.neteacher.common.ai;

import java.util.List;

/**
 * AI 出题端口。
 *
 * <p>屏蔽厂商差异：实现可对接混元 / DeepSeek 等大模型，也可使用本地模板兜底。
 * 支持按 {@code ai.llm-provider} 灰度切量、降级与多厂商热切换（见系统架构设计 §3.1）。</p>
 */
public interface QuestionGeneratePort {

    /**
     * 按条件生成题目草稿（不落库）。实现应保证返回的条数不超过请求的数量，且尽量不去重。
     *
     * @param request 生成条件
     * @return 草稿列表，可能为 null/空（当生成失败或模板不足时）
     */
    List<QuestionDraft> generate(QuestionGenRequest request);

    /** 当前实际使用的出题提供方标识，如 mock / deepseek，用于前端提示与问题追溯 */
    String provider();
}

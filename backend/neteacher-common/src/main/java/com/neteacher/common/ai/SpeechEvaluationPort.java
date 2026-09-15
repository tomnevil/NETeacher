package com.neteacher.common.ai;

import com.neteacher.common.ai.dto.SpeechScoreRequest;
import com.neteacher.common.ai.dto.SpeechScoreResponse;

/**
 * 口语评测能力端口（六边形架构适配器接口）。
 * 实现可对接腾讯云智聆口语评测 SOE / 科大讯飞 / 阿里云，或本地降级实现。
 */
public interface SpeechEvaluationPort {

    SpeechScoreResponse evaluate(SpeechScoreRequest request);

    String vendor();
}

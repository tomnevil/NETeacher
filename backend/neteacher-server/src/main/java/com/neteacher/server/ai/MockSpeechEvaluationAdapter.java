package com.neteacher.server.ai;

import com.neteacher.common.ai.SpeechEvaluationPort;
import com.neteacher.common.ai.dto.SpeechScoreRequest;
import com.neteacher.common.ai.dto.SpeechScoreResponse;
import org.springframework.stereotype.Component;

/**
 * 口语评测降级实现（无外部 AI 时返回模拟分）。后续替换为腾讯云智聆 SOE 适配器。
 */
@Component
public class MockSpeechEvaluationAdapter implements SpeechEvaluationPort {

    @Override
    public SpeechScoreResponse evaluate(SpeechScoreRequest request) {
        SpeechScoreResponse r = new SpeechScoreResponse();
        r.setOverall(88.0);
        r.setAccuracy(90.0);
        r.setFluency(85.0);
        r.setIntegrity(92.0);
        return r;
    }

    @Override
    public String vendor() {
        return "mock";
    }
}

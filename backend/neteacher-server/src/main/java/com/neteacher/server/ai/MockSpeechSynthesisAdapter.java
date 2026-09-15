package com.neteacher.server.ai;

import com.neteacher.common.ai.SpeechSynthesisPort;
import org.springframework.stereotype.Component;

/**
 * 语音合成降级实现。后续替换为腾讯云 TTS 适配器。
 */
@Component
public class MockSpeechSynthesisAdapter implements SpeechSynthesisPort {

    @Override
    public byte[] synthesize(String text, String voiceType) {
        return new byte[0];
    }

    @Override
    public String vendor() {
        return "mock";
    }
}

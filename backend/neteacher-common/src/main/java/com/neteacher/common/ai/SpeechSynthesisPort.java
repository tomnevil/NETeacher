package com.neteacher.common.ai;

/**
 * 语音合成（TTS）能力端口。实现可对接腾讯云 TTS（童声/外教音色）等。
 */
public interface SpeechSynthesisPort {

    byte[] synthesize(String text, String voiceType);

    String vendor();
}

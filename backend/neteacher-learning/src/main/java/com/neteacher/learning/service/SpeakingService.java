package com.neteacher.learning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.common.ai.LlmPort;
import com.neteacher.learning.dto.SpeakingRequest;
import com.neteacher.learning.dto.SpeakingResult;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.repository.LearningRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 口语/听力评测（M3 扩展）：基于用户语音转写文本与目标文本的相似度给出分数，并用 LLM 生成反馈。
 * 真实场景下 transcript 由前端 Web Speech API / 云端 ASR 提供；无 key 时降级为 mock 反馈。
 */
@Service
@RequiredArgsConstructor
public class SpeakingService {

    private final LearningRecordRepository recordRepo;
    private final ObjectMapper objectMapper;
    private final LlmPort llmPort;

    public SpeakingResult evaluate(Long userId, SpeakingRequest req) {
        int score = score(req.getTargetText(), req.getTranscript());
        String feedback = feedback(req.getTargetText(), req.getTranscript(), score);

        LearningRecord r = new LearningRecord();
        r.setUserId(userId);
        r.setCourseId(req.getCourseId());
        r.setModule(req.getModule() == null ? "speaking" : req.getModule());
        r.setScore(score);
        r.setDurationSec(req.getDurationSec());
        r.setFinished(true);
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("targetText", req.getTargetText());
        detail.put("transcript", req.getTranscript());
        detail.put("feedback", feedback);
        try {
            r.setDetail(objectMapper.writeValueAsString(detail));
        } catch (Exception ignored) {
            r.setDetail("{}");
        }
        recordRepo.save(r);

        SpeakingResult res = new SpeakingResult();
        res.setScore(score);
        res.setFeedback(feedback);
        res.setTranscript(req.getTranscript());
        res.setTargetText(req.getTargetText());
        return res;
    }

    private int score(String target, String transcript) {
        if (target == null || transcript == null || transcript.isBlank()) {
            return 0;
        }
        int dist = levenshtein(target, transcript);
        int max = Math.max(target.length(), transcript.length());
        int sim = max == 0 ? 100 : (int) Math.round((1 - (double) dist / max) * 100);
        return Math.max(0, Math.min(100, sim));
    }

    private String feedback(String target, String transcript, int score) {
        String prompt = String.format(
                "你是英语口语老师。目标句子：%s。学生朗读：%s。相似度得分 %d/100。"
                        + "请用 50 字以内中文给出发音/流利度改进建议。",
                target == null ? "" : target,
                transcript == null ? "" : transcript,
                score);
        try {
            return llmPort.chat(prompt);
        } catch (Exception e) {
            return score >= 80
                    ? "朗读很棒，继续保持节奏与重音！"
                    : "可以再放慢语速，注意单词之间的停顿与目标句保持一致。";
        }
    }

    private static int levenshtein(String a, String b) {
        a = a == null ? "" : a;
        b = b == null ? "" : b;
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}

package com.neteacher.assessment.service;

import com.neteacher.assessment.dto.*;
import com.neteacher.common.ai.SpeechEvaluationPort;
import com.neteacher.common.ai.dto.SpeechScoreRequest;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReadAloudService {

    private final SpeechEvaluationPort speechPort;

    public ReadAloudService(SpeechEvaluationPort speechPort) {
        this.speechPort = speechPort;
    }

    private static final Map<Integer, String[]> BANK = Map.of(
            1, new String[]{"Hello, I am Tom.", "你好，我是汤姆。"},
            2, new String[]{"I like apples and bananas.", "我喜欢苹果和香蕉。"},
            3, new String[]{"We read books in the library.", "我们在图书馆读书。"},
            4, new String[]{"My favorite season is spring.", "我最喜欢的季节是春天。"},
            5, new String[]{"She writes a letter to her friend.", "她给朋友写了一封信。"},
            6, new String[]{"Learning English is fun and useful.", "学英语既有趣又有用。"});

    public SpeakingTask getTask(int level) {
        return getTask(level, null);
    }

    /**
     * 获取跟读任务。
     * @param level    难度等级 1~6
     * @param courseId 来源课程（学习地图关卡节点直达时携带），仅用于任务标识与文案
     */
    public SpeakingTask getTask(int level, Long courseId) {
        int lv = Math.max(1, Math.min(6, level));
        String[] pair = BANK.get(lv);
        SpeakingTask t = new SpeakingTask();
        // 带课程时用课程 ID 作为任务 ID，便于前端区分不同关卡来源
        t.setId(courseId != null ? courseId : (long) lv);
        t.setLevel(lv);
        t.setRefText(pair[0]);
        t.setTranslation(pair[1]);
        t.setTip("跟着读，注意连读和重音～");
        t.setWords(Arrays.asList(pair[0].replaceAll("[.,]", "").split(" ")));
        return t;
    }

    public SpeakingEvalResult evaluate(SpeakingEvalRequest req) {
        SpeechScoreRequest sreq = new SpeechScoreRequest();
        sreq.setRefText(req.getRefText());
        sreq.setMode(2);
        var resp = speechPort.evaluate(sreq);

        SpeakingEvalResult r = new SpeakingEvalResult();
        r.setScore((int) Math.round(resp.getOverall()));
        r.setAccuracy((int) Math.round(resp.getAccuracy()));
        r.setFluency((int) Math.round(resp.getFluency()));
        r.setIntegrity((int) Math.round(resp.getIntegrity()));

        List<String> refWords = Arrays.asList(
                req.getRefText().replaceAll("[.,]", "").toLowerCase().split(" "));
        Set<String> said = new HashSet<>(Arrays.asList(
                (req.getTranscript() == null ? "" : req.getTranscript())
                        .replaceAll("[.,?!]", "").toLowerCase().split(" ")));

        List<SpeakingEvalResult.PhonemeMark> marks = new ArrayList<>();
        Random rnd = new Random();
        for (String w : refWords) {
            SpeakingEvalResult.PhonemeMark m = new SpeakingEvalResult.PhonemeMark();
            m.setText(w);
            if (said.contains(w)) {
                m.setStatus("good");
                m.setScore(88 + rnd.nextInt(12));
            } else {
                m.setStatus("weak");
                m.setScore(38 + rnd.nextInt(22));
            }
            marks.add(m);
        }
        r.setPhonemes(marks);
        r.setWaveformRef(fakeWave(45, 55, 80));
        r.setWaveformUser(fakeWave(35, 65, 80));
        r.setFeedback(feedbackFor(r.getScore()));
        return r;
    }

    private List<Integer> fakeWave(int base, int amp, int n) {
        List<Integer> out = new ArrayList<>();
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            out.add(base + rnd.nextInt(amp));
        }
        return out;
    }

    private String feedbackFor(int score) {
        if (score >= 85) return "太棒了！发音很标准，继续保持～";
        if (score >= 70) return "不错哦，注意个别单词的重音会更自然。";
        return "再试一次，慢慢说清楚每个词～";
    }
}

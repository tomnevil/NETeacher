package com.neteacher.learning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.common.ai.LlmPort;
import com.neteacher.learning.dto.*;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.model.DialogueMessage;
import com.neteacher.learning.model.DialogueSession;
import com.neteacher.learning.repository.LearningRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 口语对话编排：
 *  - start：系统按年级与学习单元发起开场白，激发孩子开口。
 *  - turn ：学生发言 -> 即时小分（流利度/准确度/相关性）+ 系统追问。
 *  - end  ：整段对话结束 -> 总评 + 优势/薄弱点 + 下一步强化建议，并落库学习记录。
 *
 * 支持离线运行：当大模型为 mock 时，使用内置友好话术与基于文本长度的启发式评分。
 */
@Slf4j
@Service
public class DialogueService {

    private final LlmPort llm;
    private final LearningRecordRepository recordRepo;
    private final ObjectMapper mapper = new ObjectMapper();
    private final boolean mock;
    private final Map<String, DialogueSession> sessions = new ConcurrentHashMap<>();

    public DialogueService(LlmPort llm, LearningRecordRepository recordRepo) {
        this.llm = llm;
        this.recordRepo = recordRepo;
        this.mock = "mock".equalsIgnoreCase(llm.vendor());
    }

    public DialogueStartResult start(Long userId, DialogueStartRequest req) {
        int grade = req.getGrade() != null ? req.getGrade() : 3;
        String unit = (req.getUnit() != null && !req.getUnit().isBlank()) ? req.getUnit() : "日常英语会话";
        String sessionId = UUID.randomUUID().toString();
        DialogueSession s = new DialogueSession();
        s.setSessionId(sessionId);
        s.setUserId(userId);
        s.setGrade(grade);
        s.setUnit(unit);
        s.setStartedAt(System.currentTimeMillis());
        s.setMessages(new ArrayList<>());
        s.setTurnScores(new ArrayList<>());

        String opening = mock ? mockOpening(grade, unit) : safeChat(openOpening(grade, unit));
        s.getMessages().add(new DialogueMessage("system", opening));
        sessions.put(sessionId, s);

        DialogueStartResult r = new DialogueStartResult();
        r.setSessionId(sessionId);
        r.setOpening(opening);
        r.setGrade(grade);
        r.setUnit(unit);
        return r;
    }

    public DialogueTurnResult turn(Long userId, DialogueTurnRequest req) {
        DialogueSession s = requireSession(req.getSessionId());
        s.getMessages().add(new DialogueMessage("student", req.getTranscript() == null ? "" : req.getTranscript()));

        DialogueTurnResult score = mock ? mockScore(req.getTranscript())
                : parseJson(safeChat(scorePrompt(s, req.getTranscript())), DialogueTurnResult.class, mockScore(req.getTranscript()));
        s.getTurnScores().add(score.getScore());

        String reply = mock ? mockReply(req.getTranscript(), s)
                : safeChat(replyPrompt(s));
        s.getMessages().add(new DialogueMessage("system", reply));

        score.setReply(reply);
        score.setTurn(s.getTurnScores().size());
        return score;
    }

    public DialogueEndResult end(Long userId, DialogueEndRequest req) {
        DialogueSession s = requireSession(req.getSessionId());
        DialogueEndResult r = mock ? mockSummary(s) : parseJson(safeChat(summaryPrompt(s)), DialogueEndResult.class, mockSummary(s));
        r.setTurns(s.getTurnScores().size());

        LearningRecord rec = new LearningRecord();
        rec.setUserId(s.getUserId() != null ? s.getUserId() : userId);
        rec.setModule("dialogue");
        rec.setScore(r.getOverallScore());
        rec.setFinished(true);
        rec.setDetail(buildDetail(s, r));
        recordRepo.save(rec);

        sessions.remove(req.getSessionId());
        return r;
    }

    // ---------- 提示词 ----------

    private String openOpening(int grade, String unit) {
        return "You are NETeacher, a warm and encouraging English speaking tutor for Chinese primary/middle school students. "
                + "The student is in grade " + grade + ". The current learning unit is '" + unit + "'. "
                + "Start a fun, lively spoken-English conversation in simple English (a little Chinese to encourage is OK). "
                + "Keep it to 1-3 sentences and end with an inviting question to get the student talking. "
                + "Output ONLY the opening line, no extra text.";
    }

    private String replyPrompt(DialogueSession s) {
        return "Continue the spoken-English conversation with the student (grade " + s.getGrade() + ", unit '" + s.getUnit() + "').\n"
                + "Conversation so far:\n" + historyText(s) + "\n"
                + "Respond to the student's last message naturally and warmly in simple English (a little Chinese OK), "
                + "and ask a follow-up question to keep them talking. 1-3 sentences. Output ONLY your reply.";
    }

    private String scorePrompt(DialogueSession s, String transcript) {
        return "You are an English speaking evaluator. Student grade " + s.getGrade() + ".\n"
                + "Conversation so far:\n" + historyText(s) + "\n"
                + "The student's latest utterance: \"" + transcript + "\".\n"
                + "Evaluate this single utterance on three dimensions (0-100 each): fluency(流利度), accuracy(准确度/语法词汇), "
                + "relevance(内容相关性/是否回应话题). Also give an overall turn score (0-100) and a short Chinese comment (<=40 chars) with encouragement.\n"
                + "Respond with ONLY a JSON object: {\"fluency\":int,\"accuracy\":int,\"relevance\":int,\"score\":int,\"comment\":\"...\"}.";
    }

    private String summaryPrompt(DialogueSession s) {
        return "You are an English speaking tutor. Grade " + s.getGrade() + ", unit '" + s.getUnit() + "'.\n"
                + "Full conversation:\n" + historyText(s) + "\n"
                + "Provide an overall assessment. Respond with ONLY a JSON object: "
                + "{\"overallScore\":int(0-100),\"strengths\":[string,...],\"weaknesses\":[string,...],"
                + "\"summary\":\"中文总评<=80字\",\"suggestions\":[string,... 2-4条下一步强化练习建议(中文)]}.";
    }

    private String historyText(DialogueSession s) {
        StringBuilder sb = new StringBuilder();
        for (DialogueMessage m : s.getMessages()) {
            sb.append(m.getRole().equals("system") ? "[系统]" : "[学生]").append(" ").append(m.getContent()).append("\n");
        }
        return sb.toString().trim();
    }

    // ---------- 离线兜底 ----------

    private String mockOpening(int grade, String unit) {
        return "Hi! I'm your English buddy NETeacher 🤖 Let's chat in English about " + unit
                + " — it's perfect for grade " + grade + "! What did you do last weekend? Tell me in English!";
    }

    private String mockReply(String transcript, DialogueSession s) {
        if (transcript == null || transcript.isBlank()) {
            return "No worries! Try saying one sentence in English — I'm listening! 😊";
        }
        return "Nice! You said: \"" + transcript + "\". Great job talking! Can you tell me one more thing about it in English? 🌟";
    }

    private DialogueTurnResult mockScore(String transcript) {
        int len = transcript == null ? 0 : transcript.trim().length();
        int base = Math.min(95, 55 + len / 2);
        DialogueTurnResult r = new DialogueTurnResult();
        r.setFluency(clamp(base + (len % 7)));
        r.setAccuracy(clamp(base - (len % 5)));
        r.setRelevance(clamp(base + 3));
        r.setScore((r.getFluency() + r.getAccuracy() + r.getRelevance()) / 3);
        r.setComment(len == 0 ? "开口就是进步，下次试着多说几句吧！" : "说得很棒，继续保持，多练会更流利！");
        return r;
    }

    private DialogueEndResult mockSummary(DialogueSession s) {
        int avg = s.getTurnScores().isEmpty() ? 70
                : s.getTurnScores().stream().mapToInt(Integer::intValue).sum() / s.getTurnScores().size();
        DialogueEndResult r = new DialogueEndResult();
        r.setOverallScore(avg);
        r.setStrengths(List.of("敢于开口、对话参与度好", "能围绕话题用英语表达想法"));
        r.setWeaknesses(List.of("部分句子语法与用词可更规范", "长句流利度仍有提升空间"));
        r.setSummary("你在这段对话中表现积极、乐于表达，整体达到" + avg + "分。继续坚持每天开口，英语会越来越自然！");
        r.setSuggestions(List.of(
                "每天用英语描述一件身边小事（3-5 句），锻炼流利度",
                "针对薄弱语法点做 5 分钟专项跟读练习",
                "把本周单元重点句型代入对话中多说几轮"));
        return r;
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    // ---------- 工具 ----------

    private String safeChat(String prompt) {
        try {
            String out = llm.chat(prompt);
            return (out == null || out.isBlank()) ? "" : out.trim();
        } catch (Exception e) {
            log.warn("LLM 调用失败，降级处理: {}", e.getMessage());
            return "";
        }
    }

    private <T> T parseJson(String raw, Class<T> type, T fallback) {
        try {
            String json = extractJson(raw);
            if (json == null) {
                return fallback;
            }
            return mapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("LLM JSON 解析失败，使用兜底: {}", e.getMessage());
            return fallback;
        }
    }

    private String extractJson(String raw) {
        if (raw == null) {
            return null;
        }
        int s = raw.indexOf('{');
        int e = raw.lastIndexOf('}');
        if (s >= 0 && e > s) {
            return raw.substring(s, e + 1);
        }
        return null;
    }

    private DialogueSession requireSession(String sessionId) {
        DialogueSession s = sessions.get(sessionId);
        if (s == null) {
            throw new com.neteacher.common.exception.BizException(
                    com.neteacher.common.exception.ErrorCode.NOT_FOUND, "会话不存在或已结束，请重新发起对话");
        }
        return s;
    }

    private String buildDetail(DialogueSession s, DialogueEndResult r) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("grade", s.getGrade());
        detail.put("unit", s.getUnit());
        List<Map<String, String>> conv = new ArrayList<>();
        for (DialogueMessage m : s.getMessages()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("role", m.getRole());
            item.put("content", m.getContent());
            conv.add(item);
        }
        detail.put("conversation", conv);
        detail.put("overallScore", r.getOverallScore());
        detail.put("strengths", r.getStrengths());
        detail.put("weaknesses", r.getWeaknesses());
        detail.put("summary", r.getSummary());
        detail.put("suggestions", r.getSuggestions());
        try {
            return mapper.writeValueAsString(detail);
        } catch (Exception e) {
            return "";
        }
    }
}

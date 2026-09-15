package com.neteacher.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.dto.AssessmentResult;
import com.neteacher.assessment.dto.QuizQuestion;
import com.neteacher.assessment.dto.QuizSubmitRequest;
import com.neteacher.assessment.dto.WrongQuestion;
import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.assessment.repository.QuestionRepository;
import com.neteacher.common.ai.LlmPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 测评服务（M4）：题库抽取、自动判分、报告与 AI 评语。
 */
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final QuestionRepository questionRepo;
    private final AssessmentRepository assessmentRepo;
    private final LlmPort llmPort;
    private final ObjectMapper objectMapper;

    public List<QuizQuestion> getQuiz(Integer level, String subject, int size) {
        List<Question> all;
        if (level != null && subject != null) {
            all = questionRepo.findByLevelAndSubject(level, subject);
        } else if (level != null) {
            all = questionRepo.findByLevel(level);
        } else if (subject != null) {
            all = questionRepo.findBySubject(subject);
        } else {
            all = questionRepo.findAll();
        }
        Collections.shuffle(all);
        int take = size <= 0 ? 10 : Math.min(size, all.size());
        return all.stream().limit(take).map(this::toQuiz).collect(Collectors.toList());
    }

    public AssessmentResult submit(Long userId, QuizSubmitRequest req) {
        Map<Long, String> ans = req.getAnswers().stream()
                .filter(a -> a.getQuestionId() != null)
                .collect(Collectors.toMap(QuizSubmitRequest.AnswerItem::getQuestionId,
                        a -> a.getAnswer() == null ? "" : a.getAnswer(),
                        (a, b) -> b));

        List<Question> qs = questionRepo.findAllById(ans.keySet());
        int total = qs.size();
        int correct = 0;
        List<Map<String, Object>> detail = new ArrayList<>();
        List<WrongQuestion> wrongQuestions = new ArrayList<>();
        for (Question q : qs) {
            boolean ok = Objects.equals(q.getAnswer(), ans.get(q.getId()));
            if (ok) {
                correct++;
            } else {
                WrongQuestion w = new WrongQuestion();
                w.setQuestionId(q.getId());
                w.setSubject(q.getSubject());
                w.setLevel(q.getLevel());
                w.setStem(q.getStem());
                w.setOptions(parseOptions(q.getOptions()));
                w.setAnswer(q.getAnswer());
                w.setUserAnswer(ans.get(q.getId()));
                w.setExplanation(q.getAnalysis());
                wrongQuestions.add(w);
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("questionId", q.getId());
            row.put("subject", q.getSubject());
            row.put("level", q.getLevel());
            row.put("stem", q.getStem());
            row.put("options", parseOptions(q.getOptions()));
            row.put("answer", q.getAnswer());
            row.put("userAnswer", ans.get(q.getId()));
            row.put("correct", ok);
            row.put("explanation", q.getAnalysis());
            detail.add(row);
        }
        int score = total == 0 ? 0 : (int) Math.round(correct * 100.0 / total);

        Assessment a = new Assessment();
        a.setUserId(userId);
        a.setType(req.getType());
        a.setSubject(req.getSubject());
        a.setLevel(req.getLevel());
        a.setScore(score);
        a.setTotalScore(total);
        a.setFinished(true);
        try {
            a.setDetail(objectMapper.writeValueAsString(detail));
        } catch (Exception e) {
            a.setDetail("[]");
        }
        a = assessmentRepo.save(a);

        String comment = llmPort.chat(buildCommentPrompt(req.getSubject(), score, correct, total));

        AssessmentResult r = new AssessmentResult();
        r.setId(a.getId());
        r.setLevel(req.getLevel());
        r.setSubject(req.getSubject());
        r.setType(a.getType());
        r.setScore(score);
        r.setTotalScore(total);
        r.setCorrectCount(correct);
        r.setTotalCount(total);
        r.setComment(comment);
        r.setDetail(a.getDetail());
        r.setWrongQuestions(wrongQuestions);
        return r;
    }

    public List<AssessmentResult> history(Long userId) {
        return assessmentRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    /** 错题本：聚合该用户所有测评中答错的题目（同题保留最新一次） */
    public List<WrongQuestion> wrongBook(Long userId) {
        LinkedHashMap<Long, WrongQuestion> map = new LinkedHashMap<>();
        for (Assessment a : assessmentRepo.findByUserIdOrderByCreatedAtDesc(userId)) {
            if (a.getDetail() == null) continue;
            try {
                List<Map<String, Object>> rows = objectMapper.readValue(a.getDetail(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                for (Map<String, Object> row : rows) {
                    if (Boolean.FALSE.equals(row.get("correct")) && row.get("stem") != null) {
                        WrongQuestion w = new WrongQuestion();
                        w.setQuestionId(((Number) row.get("questionId")).longValue());
                        w.setSubject((String) row.get("subject"));
                        w.setLevel(row.get("level") == null ? null : ((Number) row.get("level")).intValue());
                        w.setStem((String) row.get("stem"));
                        w.setOptions(parseOptions(row.get("options")));
                        w.setAnswer((String) row.get("answer"));
                        w.setUserAnswer((String) row.get("userAnswer"));
                        w.setExplanation((String) row.get("explanation"));
                        map.put(w.getQuestionId(), w);
                    }
                }
            } catch (Exception ignored) {
                // 跳过无法解析的明细
            }
        }
        return new ArrayList<>(map.values());
    }

    private AssessmentResult toResult(Assessment a) {
        AssessmentResult r = new AssessmentResult();
        r.setId(a.getId());
        r.setLevel(a.getLevel());
        r.setSubject(a.getSubject());
        r.setType(a.getType());
        r.setScore(a.getScore());
        r.setTotalScore(a.getTotalScore());
        List<WrongQuestion> wrong = new ArrayList<>();
        int correct = 0;
        int total = 0;
        if (a.getDetail() != null) {
            try {
                List<Map<String, Object>> rows = objectMapper.readValue(a.getDetail(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                total = rows.size();
                for (Map<String, Object> row : rows) {
                    if (Boolean.TRUE.equals(row.get("correct"))) correct++;
                    else if (row.get("stem") != null) {
                        WrongQuestion w = new WrongQuestion();
                        w.setQuestionId(((Number) row.get("questionId")).longValue());
                        w.setSubject((String) row.get("subject"));
                        w.setLevel(row.get("level") == null ? null : ((Number) row.get("level")).intValue());
                        w.setStem((String) row.get("stem"));
                        w.setOptions(parseOptions(row.get("options")));
                        w.setAnswer((String) row.get("answer"));
                        w.setUserAnswer((String) row.get("userAnswer"));
                        w.setExplanation((String) row.get("explanation"));
                        wrong.add(w);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        r.setCorrectCount(correct);
        r.setTotalCount(total);
        r.setWrongQuestions(wrong);
        r.setDetail(a.getDetail());
        return r;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseOptions(Object raw) {
        if (raw instanceof List) return (List<String>) raw;
        if (raw instanceof String s) {
            try {
                return objectMapper.readValue(s, objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, String.class));
            } catch (Exception e) {
                return List.of();
            }
        }
        return List.of();
    }

    private QuizQuestion toQuiz(Question q) {
        QuizQuestion dto = new QuizQuestion();
        dto.setId(q.getId());
        dto.setLevel(q.getLevel());
        dto.setSubject(q.getSubject());
        dto.setType(q.getType());
        dto.setStem(q.getStem());
        try {
            dto.setOptions(objectMapper.readValue(q.getOptions() == null ? "[]" : q.getOptions(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
        } catch (Exception e) {
            dto.setOptions(List.of());
        }
        return dto;
    }

    private String buildCommentPrompt(String subject, int score, int correct, int total) {
        String s = subject == null ? "综合" : subject;
        return String.format(
                "你是一位中小学英语老师。学生在一次%s测评中答对 %d/%d 题，得分 %d。请用一句鼓励性中文点评，并给出一条改进建议（不超过 60 字）。",
                s, correct, total, score);
    }
}

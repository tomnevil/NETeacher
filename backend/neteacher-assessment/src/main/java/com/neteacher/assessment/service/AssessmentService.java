package com.neteacher.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.dto.AssessmentResult;
import com.neteacher.assessment.dto.DimensionScore;
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
        return getQuiz(level, subject, size, null, null);
    }

    /**
     * 抽题（P3）：
     * <ul>
     *   <li>仅抽取已发布题目——草稿（如 AI 生成的待复核题）不会进入练习与测评；</li>
     *   <li>支持按场景 usage 过滤（Question.usage 为竖线分隔标签，命中任一即可）；</li>
     *   <li>支持按知识点 knowledgePoint 过滤，用于针对性补弱。</li>
     * </ul>
     */
    public List<QuizQuestion> getQuiz(Integer level, String subject, int size,
                                      String usage, String knowledgePoint) {
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
        List<Question> pool = all.stream()
                .filter(q -> "published".equals(q.getStatus()))
                .filter(q -> usage == null || usage.isBlank() || matchesUsage(q.getUsage(), usage))
                .filter(q -> knowledgePoint == null || knowledgePoint.isBlank()
                        || knowledgePoint.equalsIgnoreCase(q.getKnowledgePoint()))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(pool);
        int take = size <= 0 ? 10 : Math.min(size, pool.size());
        return pool.stream().limit(take).map(this::toQuiz).collect(Collectors.toList());
    }

    /** 场景匹配：题目未标注场景视为通用；标注时命中任一竖线分隔标签即可 */
    private boolean matchesUsage(String questionUsage, String wanted) {
        if (questionUsage == null || questionUsage.isBlank()) {
            return true;
        }
        for (String part : questionUsage.split("\\|")) {
            if (part.trim().equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
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
            // P3：记录知识点，供弱项分析下沉到知识点粒度
            row.put("knowledgePoint", q.getKnowledgePoint());
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

    /** 能力雷达图：聚合各维度最新一次测评得分（0-100），未测维度记 0 */
    public List<DimensionScore> abilityRadar(Long userId) {
        List<Assessment> list = assessmentRepo.findByUserIdOrderByCreatedAtDesc(userId);
        Map<String, Integer> latest = new LinkedHashMap<>();
        for (Assessment a : list) {
            if (a.getSubject() != null && !latest.containsKey(a.getSubject())) {
                latest.put(a.getSubject(), a.getScore() == null ? 0 : a.getScore());
            }
        }
        String[][] dims = {
            {"listening", "听力"}, {"speaking", "口语"}, {"reading", "阅读"},
            {"writing", "写作"}, {"word", "单词"}, {"grammar", "语法"}
        };
        List<DimensionScore> res = new ArrayList<>();
        for (String[] d : dims) {
            DimensionScore ds = new DimensionScore();
            ds.setDimension(d[0]);
            ds.setLabel(d[1]);
            ds.setScore(latest.getOrDefault(d[0], 0));
            res.add(ds);
        }
        return res;
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
        dto.setKnowledgePoint(q.getKnowledgePoint());
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

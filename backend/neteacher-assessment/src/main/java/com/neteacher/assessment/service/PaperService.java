package com.neteacher.assessment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.dto.PaperDTO;
import com.neteacher.assessment.dto.PaperSpec;
import com.neteacher.assessment.dto.QuizQuestion;
import com.neteacher.assessment.entity.Paper;
import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.PaperRepository;
import com.neteacher.assessment.repository.QuestionRepository;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 组卷服务（P3）：按「等级 + 场景 + 学科配比 + 知识点」从题库抽题并固化为试卷。
 *
 * <p>与 {@link AssessmentService#getQuiz} 的区别：组卷支持多维配比与知识点定向，且结果会持久化
 * （{@link Paper}），便于复用与后续作业布置；抽题只取已发布题目，草稿不会进入试卷。</p>
 */
@Service
@RequiredArgsConstructor
public class PaperService {

    private static final int DEFAULT_SIZE = 10;

    private final QuestionRepository questionRepo;
    private final PaperRepository paperRepo;
    private final ObjectMapper objectMapper;

    /** 组卷：按条件抽题并持久化 */
    public PaperDTO compose(Long uid, PaperSpec spec) {
        PaperSpec safe = spec == null ? new PaperSpec() : spec;
        Integer level = safe.getLevel() == null ? 1 : safe.getLevel();
        String usage = (safe.getUsage() == null || safe.getUsage().isBlank())
                ? "unit_test" : safe.getUsage().trim();
        List<String> kps = safe.getKnowledgePoints() == null ? List.of()
                : safe.getKnowledgePoints().stream()
                        .filter(Objects::nonNull).filter(s -> !s.isBlank()).toList();

        List<Question> pool = questionRepo.findByLevel(level).stream()
                .filter(q -> "published".equals(q.getStatus()))
                .filter(q -> matchesUsage(q.getUsage(), usage))
                .filter(q -> kps.isEmpty()
                        || (q.getKnowledgePoint() != null && kps.contains(q.getKnowledgePoint())))
                .collect(Collectors.toCollection(ArrayList::new));

        List<String> shortfalls = new ArrayList<>();
        if (pool.isEmpty()) {
            // 条件过窄时降级为该等级全部已发布题目，并明确告知缺口
            pool = questionRepo.findByLevel(level).stream()
                    .filter(q -> "published".equals(q.getStatus()))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (!pool.isEmpty()) {
                shortfalls.add("按 usage=" + usage + " 与知识点筛选后无题，已放宽为该等级已发布题目");
            }
        }
        if (pool.isEmpty()) {
            throw new BizException(ErrorCode.NOT_FOUND, "L" + level + " 暂无已发布题目，无法组卷");
        }

        Map<String, List<Question>> bySubject = new LinkedHashMap<>();
        for (Question q : pool) {
            bySubject.computeIfAbsent(q.getSubject() == null ? "?" : q.getSubject(), k -> new ArrayList<>()).add(q);
        }

        List<Question> picked = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        List<PaperSpec.PaperItem> items = safe.getItems() == null ? List.of() : safe.getItems();

        if (items.isEmpty()) {
            List<Question> shuffled = new ArrayList<>(pool);
            Collections.shuffle(shuffled);
            for (Question q : shuffled) {
                if (picked.size() >= DEFAULT_SIZE) {
                    break;
                }
                if (seen.add(q.getId())) {
                    picked.add(q);
                }
            }
        } else {
            for (PaperSpec.PaperItem item : items) {
                if (item == null || item.getSubject() == null || item.getSubject().isBlank()) {
                    continue;
                }
                List<Question> cand = new ArrayList<>(bySubject.getOrDefault(item.getSubject().trim(), List.of()));
                Collections.shuffle(cand);
                int want = Math.max(0, item.getCount());
                int got = 0;
                for (Question q : cand) {
                    if (got >= want) {
                        break;
                    }
                    if (seen.add(q.getId())) {
                        picked.add(q);
                        got++;
                    }
                }
                if (got < want) {
                    shortfalls.add(item.getSubject() + " 缺 " + (want - got) + " 题");
                }
            }
        }

        String title = (safe.getTitle() == null || safe.getTitle().isBlank())
                ? "L" + level + " · " + usage + " · " + picked.size() + "题" : safe.getTitle().trim();

        Paper paper = new Paper();
        paper.setTitle(title);
        paper.setLevel(level);
        paper.setUsage(usage);
        paper.setCreatedBy(uid);
        paper.setQuestionIds(writeJson(picked.stream().map(Question::getId).toList()));
        paper.setKnowledgePoints(writeJson(kps));
        Paper saved = paperRepo.save(paper);

        PaperDTO dto = new PaperDTO();
        dto.setId(saved.getId());
        dto.setTitle(saved.getTitle());
        dto.setLevel(level);
        dto.setUsage(usage);
        dto.setCreatedBy(uid);
        dto.setQuestionIds(picked.stream().map(Question::getId).collect(Collectors.toList()));
        dto.setQuestions(picked.stream().map(this::toQuizQuestion).collect(Collectors.toList()));
        dto.setShortfalls(shortfalls);
        return dto;
    }

    /** 读取已固化的试卷（含题目明细） */
    public PaperDTO get(Long id) {
        Paper p = paperRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "试卷不存在: " + id));
        List<Long> ids = readIds(p.getQuestionIds());
        Map<Long, Question> map = questionRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a, LinkedHashMap::new));
        List<Question> ordered = ids.stream().map(map::get).filter(Objects::nonNull).toList();

        PaperDTO dto = new PaperDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setLevel(p.getLevel());
        dto.setUsage(p.getUsage());
        dto.setCreatedBy(p.getCreatedBy());
        dto.setQuestionIds(ids);
        dto.setQuestions(ordered.stream().map(this::toQuizQuestion).collect(Collectors.toList()));
        return dto;
    }

    /** 我组过的卷子 */
    public List<PaperDTO> myPapers(Long uid) {
        return paperRepo.findByCreatedByOrderByCreatedAtDesc(uid).stream().map(p -> {
            PaperDTO dto = new PaperDTO();
            dto.setId(p.getId());
            dto.setTitle(p.getTitle());
            dto.setLevel(p.getLevel());
            dto.setUsage(p.getUsage());
            dto.setCreatedBy(p.getCreatedBy());
            dto.setQuestionIds(readIds(p.getQuestionIds()));
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 场景匹配：Question.usage 为竖线分隔的场景标签（如 practice|unit_test），
     * 命中任一标签即视为可用；题目未标注场景时视为通用，允许入选。
     */
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

    private QuizQuestion toQuizQuestion(Question q) {
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

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Long> readIds(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Object> raw = objectMapper.readValue(json, new TypeReference<List<Object>>() {
            });
            List<Long> ids = new ArrayList<>();
            for (Object o : raw) {
                if (o instanceof Number n) {
                    ids.add(n.longValue());
                }
            }
            return ids;
        } catch (Exception e) {
            return List.of();
        }
    }
}

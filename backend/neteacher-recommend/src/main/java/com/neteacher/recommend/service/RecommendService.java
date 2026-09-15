package com.neteacher.recommend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.common.ai.LlmPort;
import com.neteacher.course.entity.Course;
import com.neteacher.course.repository.CourseRepository;
import com.neteacher.learning.repository.LearningRecordRepository;
import com.neteacher.recommend.dto.RecommendItem;
import com.neteacher.recommend.dto.RecommendPath;
import com.neteacher.recommend.entity.LearningPath;
import com.neteacher.recommend.repository.LearningPathRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 推荐与自适应路径（M5）：基于测评成绩与学习记录计算当前等级、薄弱项与推荐内容。
 */
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final CourseRepository courseRepo;
    private final AssessmentRepository assessmentRepo;
    private final LearningRecordRepository recordRepo;
    private final LearningPathRepository pathRepo;
    private final ObjectMapper objectMapper;
    private final LlmPort llmPort;

    public RecommendPath build(Long uid) {
        List<Assessment> asms = assessmentRepo.findByUserIdOrderByCreatedAtDesc(uid);
        Map<String, Integer> mastery = computeMastery(asms);
        int avg = mastery.isEmpty()
                ? 60
                : (int) Math.round(mastery.values().stream().mapToInt(Integer::intValue).average().orElse(60));
        int currentLevel = levelFromScore(avg);
        int nextLevel = Math.min(currentLevel + 1, 6);

        List<RecommendItem> items = new ArrayList<>();
        courseRepo.findByLevel(currentLevel).stream().limit(3)
                .forEach(c -> items.add(item(c, "匹配当前等级 L" + currentLevel)));
        courseRepo.findByLevel(nextLevel).stream().limit(2)
                .forEach(c -> items.add(item(c, "冲刺下一等级 L" + nextLevel)));

        // 针对最薄弱学科推荐专项包
        mastery.entrySet().stream().min(Comparator.comparingInt(Map.Entry::getValue)).ifPresent(e -> {
            if (e.getValue() < 75) {
                courseRepo.findByCategory("SPECIAL").stream().limit(2)
                        .forEach(c -> items.add(item(c, "针对薄弱项「" + e.getKey() + "」的专项突破")));
            }
        });

        // 持久化路径
        LearningPath lp = pathRepo.findFirstByUserIdOrderByCreatedAtDesc(uid).orElse(new LearningPath());
        lp.setUserId(uid);
        lp.setCurrentLevel(currentLevel);
        lp.setNextLevel(nextLevel);
        try {
            lp.setRecommendItems(objectMapper.writeValueAsString(items));
            lp.setSubjectScores(objectMapper.writeValueAsString(mastery));
        } catch (Exception ex) {
            lp.setRecommendItems("[]");
            lp.setSubjectScores("{}");
        }
        pathRepo.save(lp);

        RecommendPath rp = new RecommendPath();
        rp.setCurrentLevel(currentLevel);
        rp.setNextLevel(nextLevel);
        rp.setMastery(mastery);
        rp.setItems(items);
        return rp;
    }

    public String explain(Long uid) {
        RecommendPath rp = build(uid);
        String prompt = String.format(
                "你是英语学习计划师。学生当前等级 L%d，目标 L%d。请给家长/学生一段 60 字以内的中文学习建议。",
                rp.getCurrentLevel(), rp.getNextLevel());
        return llmPort.chat(prompt);
    }

    /**
     * 将对话中的薄弱点转化为可执行的强化练习任务（推荐课程）。
     * 通过技能关键词（口语/听力/语法/词汇…）匹配课程 tags/category/level，离线可用。
     */
    public List<RecommendItem> recommendForWeaknesses(Long uid, List<String> weaknesses, Integer grade, String unit) {
        int level = levelFromGrade(grade);
        List<Course> pool = new ArrayList<>();
        pool.addAll(courseRepo.findByLevel(level));
        pool.addAll(courseRepo.findByLevel(Math.min(level + 1, 6)));
        pool.addAll(courseRepo.findByCategory("SPECIAL"));
        // 去重
        Map<Long, Course> byId = new LinkedHashMap<>();
        for (Course c : pool) {
            byId.put(c.getId(), c);
        }

        List<RecommendItem> items = new ArrayList<>();
        List<String> weaks = (weaknesses == null) ? List.of() : weaknesses.stream()
                .filter(w -> w != null && !w.isBlank()).collect(Collectors.toList());

        if (weaks.isEmpty()) {
            // 无薄弱点：给出当前等级巩固任务
            byId.values().stream().limit(4)
                    .forEach(c -> items.add(item(c, "继续巩固 L" + level + " 课程")));
        } else {
            for (String w : weaks) {
                List<String> skills = detectSkills(w);
                if (skills.isEmpty()) {
                    skills = List.of("speaking");
                }
                for (String skill : skills) {
                    List<Course> matched = byId.values().stream()
                            .filter(c -> matchSkill(c, skill))
                            .limit(2)
                            .collect(Collectors.toList());
                    if (matched.isEmpty()) {
                        byId.values().stream().findFirst().ifPresent(matched::add);
                    }
                    String skillName = SKILL_NAMES.getOrDefault(skill, skill);
                    for (Course c : matched) {
                        items.add(item(c, "针对薄弱点「" + w.trim() + "」的强化练习（侧重" + skillName + "）"));
                    }
                }
            }
        }

        // 去重并按 courseId 保序，最多 6 个
        Map<Long, RecommendItem> dedup = new LinkedHashMap<>();
        for (RecommendItem it : items) {
            dedup.putIfAbsent(it.getCourseId(), it);
        }
        return dedup.values().stream().limit(6).collect(Collectors.toList());
    }

    private static final Map<String, String> SKILL_KEYWORDS = Map.of(
            "speaking", "口语|说|表达|流利|开口|对话|发音|互动|speaking|pronunciation",
            "listening", "听力|听|listening",
            "reading", "阅读|读|reading",
            "writing", "写作|写|writing",
            "grammar", "语法|句子|结构|时态|grammar",
            "vocabulary", "词汇|用词|单词|词|vocabulary|word"
    );

    private static final Map<String, String> SKILL_NAMES = Map.of(
            "speaking", "口语表达",
            "listening", "听力",
            "reading", "阅读",
            "writing", "写作",
            "grammar", "语法",
            "vocabulary", "词汇"
    );

    private List<String> detectSkills(String weakness) {
        List<String> skills = new ArrayList<>();
        for (Map.Entry<String, String> e : SKILL_KEYWORDS.entrySet()) {
            if (Pattern.compile(e.getValue()).matcher(weakness).find()) {
                skills.add(e.getKey());
            }
        }
        return skills;
    }

    private boolean matchSkill(Course c, String skill) {
        String hay = ((c.getTags() == null ? "" : c.getTags()) + " "
                + (c.getTitle() == null ? "" : c.getTitle()) + " "
                + (c.getCategory() == null ? "" : c.getCategory()) + " "
                + (c.getDescription() == null ? "" : c.getDescription())).toLowerCase();
        return Pattern.compile(SKILL_KEYWORDS.get(skill)).matcher(hay).find();
    }

    private int levelFromGrade(Integer grade) {
        if (grade == null) {
            return 2;
        }
        if (grade <= 2) return 1;
        if (grade <= 4) return 2;
        if (grade <= 6) return 3;
        if (grade <= 8) return 4;
        if (grade <= 9) return 5;
        return 6;
    }


    private Map<String, Integer> computeMastery(List<Assessment> asms) {
        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        for (Assessment a : asms) {
            if (a.getSubject() == null || a.getScore() == null) {
                continue;
            }
            grouped.computeIfAbsent(a.getSubject(), k -> new ArrayList<>()).add(a.getScore());
        }
        Map<String, Integer> mastery = new LinkedHashMap<>();
        grouped.forEach((k, v) -> mastery.put(k, (int) Math.round(v.stream().mapToInt(Integer::intValue).average().orElse(0))));
        return mastery;
    }

    private int levelFromScore(int score) {
        if (score >= 94) return 6;
        if (score >= 88) return 5;
        if (score >= 80) return 4;
        if (score >= 70) return 3;
        if (score >= 60) return 2;
        return 1;
    }

    private RecommendItem item(Course c, String reason) {
        RecommendItem it = new RecommendItem();
        it.setCourseId(c.getId());
        it.setTitle(c.getTitle());
        it.setCategory(c.getCategory());
        it.setLevel(c.getLevel());
        it.setReason(reason);
        return it;
    }
}

package com.neteacher.ops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.repository.LearningRecordRepository;
import com.neteacher.ops.dto.ClassOverview;
import com.neteacher.ops.dto.StudentProgress;
import com.neteacher.user.entity.ClassGroup;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.ClassGroupRepository;
import com.neteacher.user.repository.SchoolRepository;
import com.neteacher.user.repository.TeacherClassRepository;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师端学情看板：按班级聚合学生进度、口语、打卡与薄弱点。
 */
@Service
@RequiredArgsConstructor
public class TeacherDashboardService {

    private final ClassGroupRepository classRepo;
    private final UserAccountRepository userRepo;
    private final SchoolRepository schoolRepo;
    private final TeacherClassRepository teacherClassRepo;
    private final LearningRecordRepository recordRepo;
    private final AssessmentRepository assessmentRepo;
    private final ObjectMapper objectMapper;

    /** 教师负责的班级学情概览 */
    public List<ClassOverview> overviewByTeacher(Long teacherId) {
        List<Long> classIds = teacherClassRepo.findByTeacherId(teacherId).stream()
                .map(tc -> tc.getClassId()).toList();
        List<ClassOverview> out = new ArrayList<>();
        for (Long cid : classIds) {
            classRepo.findById(cid).ifPresent(c -> out.add(build(c)));
        }
        if (out.isEmpty()) {
            // 兜底：教师直接作为班主任的班级
            for (ClassGroup c : classRepo.findByHeadTeacherId(teacherId)) {
                out.add(build(c));
            }
        }
        return out;
    }

    public ClassOverview overviewByClass(Long classId) {
        ClassGroup c = classRepo.findById(classId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "班级不存在"));
        return build(c);
    }

    /**
     * 带权限校验的班级详情：仅当该教师确实是此班的任课教师或班主任时才允许访问。
     */
    public ClassOverview overviewByClassForTeacher(Long teacherId, Long classId) {
        if (!teachesClass(teacherId, classId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该班级学情");
        }
        return overviewByClass(classId);
    }

    /** 教师是否任教该班级（任课关联 或 班主任） */
    public boolean teachesClass(Long teacherId, Long classId) {
        if (teacherClassRepo.existsByTeacherIdAndClassId(teacherId, classId)) {
            return true;
        }
        return classRepo.findById(classId)
                .map(c -> Objects.equals(c.getHeadTeacherId(), teacherId))
                .orElse(false);
    }

    private ClassOverview build(ClassGroup c) {
        List<UserAccount> students = userRepo.findByClassId(c.getId()).stream()
                .filter(u -> "STUDENT".equals(u.getRole()))
                .toList();

        List<StudentProgress> details = new ArrayList<>();
        Map<String, Integer> weakCount = new LinkedHashMap<>();
        Map<String, Integer> weakKpCount = new LinkedHashMap<>();
        int checkedToday = 0;

        for (UserAccount s : students) {
            StudentProgress sp = buildStudent(s);
            details.add(sp);
            if (sp.isCheckedToday()) checkedToday++;
            for (String w : sp.getWeakSubjects()) {
                weakCount.merge(w, 1, Integer::sum);
            }
            for (String kp : sp.getWeakKnowledgePoints()) {
                weakKpCount.merge(kp, 1, Integer::sum);
            }
        }
        details.sort(Comparator.comparingDouble(StudentProgress::getOverallLevel).reversed());

        ClassOverview ov = new ClassOverview();
        ov.setClassId(c.getId());
        ov.setClassName(c.getName());
        ov.setGrade(c.getGrade());
        if (c.getSchoolId() != null) {
            schoolRepo.findById(c.getSchoolId()).ifPresent(s -> ov.setSchoolName(s.getName()));
        }
        if (c.getHeadTeacherId() != null) {
            userRepo.findById(c.getHeadTeacherId()).ifPresent(t -> ov.setHeadTeacherName(t.getNickname()));
        }
        ov.setStudentCount(students.size());
        ov.setAvgSpeaking(details.stream().mapToDouble(StudentProgress::getSpeakingAvg).average().orElse(0));
        ov.setAvgMinutes(details.stream().mapToInt(StudentProgress::getTotalMinutes).average().orElse(0));
        ov.setCheckedTodayCount(checkedToday);
        ov.setWeakTopics(weakCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList());
        // P3：班级共性薄弱「知识点」（按出现人数降序）
        ov.setWeakKnowledgePoints(weakKpCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList());
        ov.setStudents(details);
        return ov;
    }

    /**
     * P3 弱项下沉：从测评明细中按知识点聚合正确率，
     * 取「样本不少于 2 题且正确率低于 60%」的知识点，正确率升序（最弱在前）。
     *
     * <p>说明：明细中的 knowledgePoint 自本次改动起才写入，历史测评记录不含该字段会被跳过，
     * 因此该列表会随新测评逐步丰富。</p>
     */
    private List<String> weakKnowledgePoints(List<Assessment> asms) {
        Map<String, int[]> stat = new LinkedHashMap<>();
        for (Assessment a : asms) {
            if (a.getDetail() == null || a.getDetail().isBlank()) {
                continue;
            }
            try {
                List<Map<String, Object>> rows = objectMapper.readValue(a.getDetail(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                for (Map<String, Object> row : rows) {
                    Object kp = row.get("knowledgePoint");
                    if (!(kp instanceof String s) || s.isBlank()) {
                        continue;
                    }
                    int[] st = stat.computeIfAbsent(s, k -> new int[2]);
                    st[0]++;
                    if (Boolean.TRUE.equals(row.get("correct"))) {
                        st[1]++;
                    }
                }
            } catch (Exception ignored) {
                // 跳过无法解析的明细
            }
        }
        return stat.entrySet().stream()
                .filter(e -> e.getValue()[0] >= 2)
                .filter(e -> e.getValue()[1] * 100.0 / e.getValue()[0] < 60)
                .sorted(Comparator.comparingDouble(e -> e.getValue()[1] * 100.0 / e.getValue()[0]))
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();
    }

    private StudentProgress buildStudent(UserAccount s) {
        List<LearningRecord> records = recordRepo.findByUserId(s.getId());
        List<Assessment> asms = assessmentRepo.findByUserIdOrderByCreatedAtDesc(s.getId());

        int totalMinutes = records.stream()
                .mapToInt(r -> r.getDurationSec() == null ? 0 : r.getDurationSec()).sum() / 60;
        double speakingAvg = records.stream()
                .filter(r -> "speaking".equals(r.getModule()) && r.getScore() != null)
                .mapToInt(LearningRecord::getScore).average().orElse(0);
        int completed = (int) records.stream()
                .filter(r -> Boolean.TRUE.equals(r.getFinished()) && r.getCourseId() != null)
                .map(LearningRecord::getCourseId).distinct().count();

        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        for (Assessment a : asms) {
            if (a.getSubject() == null || a.getScore() == null) continue;
            grouped.computeIfAbsent(a.getSubject(), k -> new ArrayList<>()).add(a.getScore());
        }
        List<String> weak = grouped.entrySet().stream()
                .filter(e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(100) < 60)
                .map(Map.Entry::getKey).toList();

        int avgScore = grouped.isEmpty() ? 60 : (int) Math.round(grouped.values().stream()
                .flatMap(List::stream).mapToInt(Integer::intValue).average().orElse(60));

        StudentProgress sp = new StudentProgress();
        sp.setStudentId(s.getId());
        sp.setNickname(s.getNickname());
        sp.setPhone(maskPhone(s.getPhone()));
        sp.setGrade(s.getGrade());
        sp.setOverallLevel(levelFromScore(avgScore));
        sp.setSpeakingAvg(Math.round(speakingAvg * 10.0) / 10.0);
        sp.setTotalMinutes(totalMinutes);
        sp.setStreakDays(streakDays(records));
        sp.setCompletedCourses(completed);
        sp.setWeakSubjects(weak);
        sp.setWeakKnowledgePoints(weakKnowledgePoints(asms));
        sp.setCheckedToday(hasRecordToday(records));
        return sp;
    }

    private boolean hasRecordToday(List<LearningRecord> records) {
        LocalDate today = LocalDate.now();
        return records.stream().anyMatch(r ->
                r.getCreatedAt() != null && r.getCreatedAt().toLocalDate().equals(today));
    }

    private int streakDays(List<LearningRecord> records) {
        if (records.isEmpty()) return 0;
        List<LocalDate> days = records.stream()
                .map(r -> r.getCreatedAt() == null ? null : r.getCreatedAt().toLocalDate())
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        LocalDate today = LocalDate.now();
        if (days.isEmpty() || days.get(0).isBefore(today.minusDays(1))) return 0;
        int streak = 1;
        LocalDate prev = days.get(0);
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).equals(prev.minusDays(1))) {
                streak++;
                prev = days.get(i);
            } else if (!days.get(i).equals(prev)) {
                break;
            }
        }
        return streak;
    }

    private int levelFromScore(int score) {
        if (score >= 94) return 6;
        if (score >= 88) return 5;
        if (score >= 80) return 4;
        if (score >= 70) return 3;
        if (score >= 60) return 2;
        return 1;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}

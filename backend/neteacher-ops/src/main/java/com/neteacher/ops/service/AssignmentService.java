package com.neteacher.ops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.assessment.repository.PaperRepository;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.ops.dto.AssignmentCreateDTO;
import com.neteacher.ops.dto.AssignmentItemDTO;
import com.neteacher.ops.dto.AssignmentStatsDTO;
import com.neteacher.ops.entity.Assignment;
import com.neteacher.ops.repository.AssignmentRepository;
import com.neteacher.user.entity.ClassGroup;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.ClassGroupRepository;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 作业布置与统计（FR-TRK-010）。
 *
 * <p>以试卷为载体积压给班级；学生提交测评时带上 assignmentId，据此统计完成率、均分与薄弱知识点。</p>
 */
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private static final String ROLE_STUDENT = "STUDENT";

    /** 截止前提醒窗口：距截止时间不足该小时数且未完成时提醒 */
    private static final long DUE_SOON_HOURS = 24;

    private final AssignmentRepository assignmentRepo;
    private final PaperRepository paperRepo;
    private final AssessmentRepository assessmentRepo;
    private final ClassGroupRepository classGroupRepo;
    private final UserAccountRepository userRepo;
    private final ObjectMapper objectMapper;

    /** 布置作业 */
    public Assignment create(Long teacherId, AssignmentCreateDTO req) {
        AssignmentCreateDTO safe = req == null ? new AssignmentCreateDTO() : req;
        if (safe.getPaperId() == null || safe.getClassId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "试卷 id 与班级 id 不能为空");
        }
        String paperTitle = paperRepo.findById(safe.getPaperId())
                .map(p -> p.getTitle())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "试卷不存在: " + safe.getPaperId()));
        ClassGroup cg = classGroupRepo.findById(safe.getClassId())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "班级不存在: " + safe.getClassId()));

        Assignment a = new Assignment();
        a.setPaperId(safe.getPaperId());
        a.setClassId(safe.getClassId());
        a.setTeacherId(teacherId);
        a.setTitle(safe.getTitle() == null || safe.getTitle().isBlank() ? paperTitle : safe.getTitle().trim());
        a.setDueAt(safe.getDueAt());
        a.setStatus(1);
        return assignmentRepo.save(a);
    }

    /** 教师布置的作业列表 */
    public List<Assignment> listByTeacher(Long teacherId) {
        return assignmentRepo.findByTeacherIdOrderByCreatedAtDesc(teacherId);
    }

    /** 学生所在班级的作业（我的作业） */
    public List<Assignment> listForStudent(Long studentId) {
        UserAccount me = userRepo.findById(studentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在: " + studentId));
        if (me.getClassId() == null) {
            return List.of();
        }
        return assignmentRepo.findByClassIdOrderByCreatedAtDesc(me.getClassId());
    }

    /** 作业统计：完成率 / 均分 / 薄弱知识点 / 学生明细 */
    public AssignmentStatsDTO stats(Long assignmentId) {
        Assignment a = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "作业不存在: " + assignmentId));

        List<UserAccount> students = userRepo.findByClassId(a.getClassId()).stream()
                .filter(u -> ROLE_STUDENT.equals(u.getRole()))
                .toList();

        List<Assessment> submissions = assessmentRepo.findAll().stream()
                .filter(x -> assignmentId.equals(x.getAssignmentId()))
                .toList();

        // 每位学生取最近一次提交
        Map<Long, Assessment> latest = new LinkedHashMap<>();
        for (Assessment s : submissions) {
            Assessment cur = latest.get(s.getUserId());
            if (cur == null || isAfter(s.getCreatedAt(), cur.getCreatedAt())) {
                latest.put(s.getUserId(), s);
            }
        }

        AssignmentStatsDTO dto = new AssignmentStatsDTO();
        dto.setAssignmentId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setPaperId(a.getPaperId());
        dto.setPaperTitle(paperRepo.findById(a.getPaperId()).map(p -> p.getTitle()).orElse(""));
        dto.setClassId(a.getClassId());
        dto.setClassName(classGroupRepo.findById(a.getClassId()).map(ClassGroup::getName).orElse(""));

        long completed = 0;
        int scoreSum = 0;
        List<AssignmentStatsDTO.StudentStat> stats = new ArrayList<>();
        for (UserAccount s : students) {
            Assessment sub = latest.get(s.getId());
            AssignmentStatsDTO.StudentStat st = new AssignmentStatsDTO.StudentStat();
            st.setStudentId(s.getId());
            st.setNickname(s.getNickname());
            st.setFinished(sub != null && Boolean.TRUE.equals(sub.getFinished()));
            st.setScore(sub == null ? null : sub.getScore());
            stats.add(st);
            if (st.isFinished()) {
                completed++;
                scoreSum += (sub.getScore() == null ? 0 : sub.getScore());
            }
        }
        dto.setStudents(stats);
        dto.setAssignedCount(students.size());
        dto.setCompletedCount(completed);
        dto.setCompletionRate(students.isEmpty() ? 0 : round1(completed * 100.0 / students.size()));
        dto.setAvgScore(completed == 0 ? 0 : round1(scoreSum * 1.0 / completed));

        // 逾期提醒：已过截止时间且仍有学生未完成
        long unfinished = students.size() - completed;
        dto.setDueAt(a.getDueAt());
        dto.setUnfinishedCount(unfinished);
        dto.setOverdue(a.getDueAt() != null && LocalDateTime.now().isAfter(a.getDueAt()) && unfinished > 0);
        dto.setDueSoon(unfinished > 0 && dueSoon(a.getDueAt(), false));

        dto.setWeakKnowledgePoints(weakKnowledgePoints(submissions));
        return dto;
    }

    /**
     * 学生作业列表：附带该学生自己的完成情况与逾期状态，逾期的排在最前。
     */
    public List<AssignmentItemDTO> listForStudentItems(Long studentId) {
        UserAccount me = userRepo.findById(studentId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在: " + studentId));
        if (me.getClassId() == null) {
            return List.of();
        }
        List<Assignment> list = assignmentRepo.findByClassIdOrderByCreatedAtDesc(me.getClassId());

        // 该学生已提交的作业 → 最近一次提交
        Map<Long, Assessment> myLatest = new LinkedHashMap<>();
        for (Assessment s : assessmentRepo.findAll()) {
            if (!studentId.equals(s.getUserId()) || s.getAssignmentId() == null) {
                continue;
            }
            Assessment cur = myLatest.get(s.getAssignmentId());
            if (cur == null || isAfter(s.getCreatedAt(), cur.getCreatedAt())) {
                myLatest.put(s.getAssignmentId(), s);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<AssignmentItemDTO> items = new ArrayList<>();
        for (Assignment a : list) {
            if (a.getStatus() != null && a.getStatus() == 0) {
                continue;
            }
            Assessment sub = myLatest.get(a.getId());
            boolean finished = sub != null && Boolean.TRUE.equals(sub.getFinished());
            AssignmentItemDTO it = new AssignmentItemDTO();
            it.setId(a.getId());
            it.setTitle(a.getTitle());
            it.setPaperId(a.getPaperId());
            it.setClassId(a.getClassId());
            it.setClassName(classGroupRepo.findById(a.getClassId()).map(ClassGroup::getName).orElse(""));
            it.setDueAt(a.getDueAt());
            it.setStatus(a.getStatus());
            it.setCreatedAt(a.getCreatedAt());
            it.setFinished(finished);
            it.setScore(sub == null ? null : sub.getScore());
            it.setOverdue(!finished && a.getDueAt() != null && now.isAfter(a.getDueAt()));
            it.setDueSoon(dueSoon(a.getDueAt(), finished));
            items.add(it);
        }
        // 已逾期 → 即将截止（24h 内）→ 未完成 → 已完成，同组内按截止时间升序
        items.sort(Comparator
                .comparing((AssignmentItemDTO x) ->
                        x.isOverdue() ? 0 : (x.isDueSoon() ? 1 : (x.isFinished() ? 3 : 2)))
                .thenComparing(x -> x.getDueAt() == null ? LocalDateTime.MAX : x.getDueAt()));
        return items;
    }

    /** 未完成，且截止时间落在未来 24 小时之内 */
    private boolean dueSoon(LocalDateTime dueAt, boolean finished) {
        if (finished || dueAt == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return dueAt.isAfter(now) && !dueAt.isAfter(now.plusHours(DUE_SOON_HOURS));
    }

    /**
     * 从本次作业的提交明细中按知识点聚合，取正确率低于 60% 的知识点，按错误数降序。
     */
    private List<String> weakKnowledgePoints(List<Assessment> submissions) {
        Map<String, int[]> stat = new LinkedHashMap<>();
        for (Assessment s : submissions) {
            if (s.getDetail() == null || s.getDetail().isBlank()) {
                continue;
            }
            try {
                List<Map<String, Object>> rows = objectMapper.readValue(s.getDetail(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                for (Map<String, Object> row : rows) {
                    Object kp = row.get("knowledgePoint");
                    if (!(kp instanceof String name) || name.isBlank()) {
                        continue;
                    }
                    int[] st = stat.computeIfAbsent(name, k -> new int[2]);
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
                .filter(e -> e.getValue()[0] >= 1)
                .filter(e -> e.getValue()[1] * 100.0 / e.getValue()[0] < 60)
                .sorted(Comparator.comparingInt((Map.Entry<String, int[]> e) -> e.getValue()[0] - e.getValue()[1]).reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();
    }

    private boolean isAfter(java.time.LocalDateTime a, java.time.LocalDateTime b) {
        if (a == null) {
            return false;
        }
        return b == null || a.isAfter(b);
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}

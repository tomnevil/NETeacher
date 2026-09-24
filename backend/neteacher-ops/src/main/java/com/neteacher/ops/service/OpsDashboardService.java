package com.neteacher.ops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.assessment.repository.QuestionRepository;
import com.neteacher.course.repository.CourseRepository;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.repository.LearningRecordRepository;
import com.neteacher.ops.dto.OpsDashboardDTO;
import com.neteacher.ops.entity.Membership;
import com.neteacher.ops.repository.MembershipRepository;
import com.neteacher.user.entity.ClassGroup;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.ClassGroupRepository;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 运营/质量看板（FR-OPS-008）：按 PRD §5 的北极星与核心指标聚合运营数据。
 *
 * <p>说明：当前数据量级较小，采用「全量载入 + 内存聚合」的方式实现，口径清晰且易于核对；
 * 后续规模上来后可改为按时间窗的 JPQL/原生 SQL 聚合或接入离线报表。</p>
 */
@Service
@RequiredArgsConstructor
public class OpsDashboardService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String ROLE_TEACHER = "TEACHER";
    private static final String PAID_PLAN = "PRO";

    private final UserAccountRepository userRepo;
    private final LearningRecordRepository recordRepo;
    private final AssessmentRepository assessmentRepo;
    private final MembershipRepository membershipRepo;
    private final QuestionRepository questionRepo;
    private final CourseRepository courseRepo;
    private final ClassGroupRepository classGroupRepo;
    private final ObjectMapper objectMapper;

    public OpsDashboardDTO dashboard() {
        OpsDashboardDTO d = new OpsDashboardDTO();

        List<UserAccount> students = userRepo.findByRole(ROLE_STUDENT);
        List<LearningRecord> records = recordRepo.findAll();
        List<Assessment> assessments = assessmentRepo.findAll();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = todayStart.minusDays(7);
        LocalDateTime prevWeekStart = todayStart.minusDays(14);

        // ---------- 规模 ----------
        d.setTotalStudents(students.size());
        d.setTotalTeachers(userRepo.findByRole(ROLE_TEACHER).size());

        // ---------- 活跃 ----------
        Set<Long> activeToday = distinctUsers(records, todayStart, now);
        Set<Long> activeThisWeek = distinctUsers(records, weekStart, now);
        Set<Long> activeLastWeek = distinctUsers(records, prevWeekStart, weekStart);
        d.setDau(activeToday.size());
        d.setWau(activeThisWeek.size());

        long weeklySeconds = records.stream()
                .filter(r -> inRange(r.getCreatedAt(), weekStart, now))
                .mapToLong(r -> r.getDurationSec() == null ? 0 : r.getDurationSec())
                .sum();
        double weeklyMinutes = weeklySeconds / 60.0;
        d.setWeeklyAvgMinutes(activeThisWeek.isEmpty() ? 0 : round1(weeklyMinutes / activeThisWeek.size()));

        // ---------- 留存（次周） ----------
        Set<Long> retained = new HashSet<>(activeLastWeek);
        retained.retainAll(activeThisWeek);
        d.setRetentionRate(activeLastWeek.isEmpty() ? 0 : round1(retained.size() * 100.0 / activeLastWeek.size()));

        // ---------- 单元测完成率 ----------
        List<Assessment> unitTests = assessments.stream()
                .filter(a -> a.getType() != null && a.getType().toLowerCase().contains("unit"))
                .toList();
        d.setUnitTestTotal(unitTests.size());
        long finished = unitTests.stream().filter(a -> Boolean.TRUE.equals(a.getFinished())).count();
        d.setUnitTestCompletionRate(unitTests.isEmpty() ? 0 : round1(finished * 100.0 / unitTests.size()));

        // ---------- 口语月度 ----------
        List<LearningRecord> speaking = records.stream()
                .filter(r -> "speaking".equals(r.getModule()) && r.getScore() != null && r.getCreatedAt() != null)
                .toList();
        LocalDateTime thisMonthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastMonthStart = thisMonthStart.minusMonths(1);
        double thisMonth = avgScore(speaking, thisMonthStart, now);
        double lastMonth = avgScore(speaking, lastMonthStart, thisMonthStart);
        d.setSpeakingAvgThisMonth(round1(thisMonth));
        d.setSpeakingAvgLastMonth(round1(lastMonth));
        d.setSpeakingMonthlyDelta(round1(thisMonth - lastMonth));

        // ---------- 商业化 ----------
        Set<Long> paidUsers = membershipRepo.findAll().stream()
                .filter(m -> PAID_PLAN.equalsIgnoreCase(m.getPlan()))
                .map(Membership::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        d.setPaidMembers(paidUsers.size());
        d.setMembershipConversionRate(students.isEmpty() ? 0 : round1(paidUsers.size() * 100.0 / students.size()));

        // ---------- 合规：家长绑定 ----------
        long unbound = students.stream().filter(s -> s.getParentId() == null).count();
        d.setUnboundParentStudents(unbound);
        d.setUnboundParentRate(students.isEmpty() ? 0 : round1(unbound * 100.0 / students.size()));

        // ---------- 内容使用率 ----------
        List<Question> questions = questionRepo.findAll();
        Set<Long> publishedIds = questions.stream()
                .filter(q -> "published".equals(q.getStatus()))
                .map(Question::getId)
                .collect(Collectors.toSet());
        d.setPublishedQuestions(publishedIds.size());
        Set<Long> usedIds = usedQuestionIds(assessments);
        usedIds.retainAll(publishedIds);
        d.setQuestionUsageRate(publishedIds.isEmpty() ? 0 : round1(usedIds.size() * 100.0 / publishedIds.size()));

        long totalCourses = courseRepo.count();
        long usedCourses = records.stream()
                .map(LearningRecord::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        d.setTotalCourses(totalCourses);
        d.setCourseUsageRate(totalCourses == 0 ? 0 : round1(usedCourses * 100.0 / totalCourses));

        // ---------- 近 14 日趋势 ----------
        List<OpsDashboardDTO.TrendPoint> trend = new ArrayList<>();
        for (int i = 13; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            LocalDateTime from = day.atStartOfDay();
            LocalDateTime to = from.plusDays(1);
            long dayDau = distinctUsers(records, from, to).size();
            long daySecs = records.stream()
                    .filter(r -> inRange(r.getCreatedAt(), from, to))
                    .mapToLong(r -> r.getDurationSec() == null ? 0 : r.getDurationSec())
                    .sum();
            OpsDashboardDTO.TrendPoint p = new OpsDashboardDTO.TrendPoint();
            p.setDate(day.toString());
            p.setDau(dayDau);
            p.setMinutes(round1(daySecs / 60.0));
            trend.add(p);
        }
        d.setTrend(trend);

        // ---------- 按班级下钻 ----------
        List<OpsDashboardDTO.ClassStat> breakdown = new ArrayList<>();
        for (ClassGroup cg : classGroupRepo.findAll()) {
            List<UserAccount> members = students.stream()
                    .filter(s -> cg.getId().equals(s.getClassId()))
                    .toList();
            if (members.isEmpty()) {
                continue;
            }
            Set<Long> memberIds = members.stream().map(UserAccount::getId).collect(Collectors.toSet());
            long classDau = userIdsInRange(records, todayStart, now, memberIds).size();
            Set<Long> classActiveWeek = userIdsInRange(records, weekStart, now, memberIds);
            long classWeeklySecs = records.stream()
                    .filter(r -> inRange(r.getCreatedAt(), weekStart, now))
                    .filter(r -> r.getUserId() != null && memberIds.contains(r.getUserId()))
                    .mapToLong(r -> r.getDurationSec() == null ? 0 : r.getDurationSec())
                    .sum();
            long unboundInClass = members.stream().filter(s -> s.getParentId() == null).count();

            OpsDashboardDTO.ClassStat cs = new OpsDashboardDTO.ClassStat();
            cs.setClassId(cg.getId());
            cs.setClassName(cg.getName());
            cs.setStudents(members.size());
            cs.setDau(classDau);
            cs.setWeeklyAvgMinutes(classActiveWeek.isEmpty()
                    ? 0 : round1(classWeeklySecs / 60.0 / classActiveWeek.size()));
            cs.setUnboundParentRate(round1(unboundInClass * 100.0 / members.size()));
            breakdown.add(cs);
        }
        breakdown.sort(Comparator.comparingLong(OpsDashboardDTO.ClassStat::getStudents).reversed());
        d.setClassBreakdown(breakdown);

        return d;
    }

    /** 指定时间窗内、且属于给定用户集合的去重用户 */
    private Set<Long> userIdsInRange(List<LearningRecord> records, LocalDateTime from,
                                     LocalDateTime to, Set<Long> scope) {
        return records.stream()
                .filter(r -> inRange(r.getCreatedAt(), from, to))
                .map(LearningRecord::getUserId)
                .filter(Objects::nonNull)
                .filter(scope::contains)
                .collect(Collectors.toSet());
    }

    private Set<Long> distinctUsers(List<LearningRecord> records, LocalDateTime from, LocalDateTime to) {
        return records.stream()
                .filter(r -> inRange(r.getCreatedAt(), from, to))
                .map(LearningRecord::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private double avgScore(List<LearningRecord> records, LocalDateTime from, LocalDateTime to) {
        return records.stream()
                .filter(r -> inRange(r.getCreatedAt(), from, to))
                .mapToInt(LearningRecord::getScore)
                .average()
                .orElse(0);
    }

    private boolean inRange(LocalDateTime t, LocalDateTime from, LocalDateTime to) {
        return t != null && !t.isBefore(from) && t.isBefore(to);
    }

    /** 从测评明细（JSON 数组）中解析被引用过的题目 id */
    private Set<Long> usedQuestionIds(List<Assessment> assessments) {
        Set<Long> ids = new HashSet<>();
        for (Assessment a : assessments) {
            if (a.getDetail() == null || a.getDetail().isBlank()) {
                continue;
            }
            try {
                List<Map<String, Object>> rows = objectMapper.readValue(a.getDetail(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
                for (Map<String, Object> row : rows) {
                    Object qid = row.get("questionId");
                    if (qid instanceof Number n) {
                        ids.add(n.longValue());
                    }
                }
            } catch (Exception ignored) {
                // 跳过无法解析的明细
            }
        }
        return ids;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}

package com.neteacher.progress.service;

import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.repository.LearningRecordRepository;
import com.neteacher.progress.dto.ProgressDashboard;
import com.neteacher.user.entity.StudentProfile;
import com.neteacher.user.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 进度与激励（M6）：基于测评与学习记录聚合仪表盘数据。
 */
@Service
public class ProgressService {

    private final AssessmentRepository assessmentRepo;
    private final LearningRecordRepository recordRepo;
    private final JwtUtil jwtUtil;
    private final StudentProfileRepository profileRepo;

    public ProgressService(AssessmentRepository assessmentRepo, LearningRecordRepository recordRepo, JwtUtil jwtUtil, StudentProfileRepository profileRepo) {
        this.assessmentRepo = assessmentRepo;
        this.recordRepo = recordRepo;
        this.jwtUtil = jwtUtil;
        this.profileRepo = profileRepo;
    }

    public ProgressDashboard dashboard(HttpServletRequest request) {
        Long uid = currentUid(request);
        StudentProfile profile = profileRepo.findByStudentId(uid).orElse(null);
        List<Assessment> asms = assessmentRepo.findByUserIdOrderByCreatedAtDesc(uid);
        List<LearningRecord> records = recordRepo.findByUserId(uid);

        Map<String, Integer> mastery = computeMastery(asms);
        int avg = mastery.isEmpty()
                ? 0
                : (int) Math.round(mastery.values().stream().mapToInt(Integer::intValue).average().orElse(0));
        int overallLevel;
        if (mastery.isEmpty()) {
            // 无测评记录：以入学测评定级基线（init_level，已是级别 1-6）作为起始级别；未定级则回退默认 L2
            if (profile != null && profile.getInitLevel() != null) {
                int lv = profile.getInitLevel();
                overallLevel = Math.min(6, Math.max(1, lv));
            } else {
                overallLevel = levelFromScore(60);
            }
        } else {
            overallLevel = levelFromScore(avg);
        }

        int totalMinutes = records.stream()
                .mapToInt(r -> r.getDurationSec() == null ? 0 : r.getDurationSec())
                .sum() / 60;

        double speakingAvg = records.stream()
                .filter(r -> "speaking".equals(r.getModule()) && r.getScore() != null)
                .mapToInt(LearningRecord::getScore)
                .average()
                .orElse(0);

        List<String> weak = mastery.entrySet().stream()
                .filter(e -> e.getValue() < 60)
                .map(Map.Entry::getKey)
                .toList();

        ProgressDashboard d = new ProgressDashboard();
        d.setOverallLevel(overallLevel);
        d.setInitLevel(profile != null ? profile.getInitLevel() : null);
        d.setMastery(mastery);
        d.setStreakDays(streakDays(records));
        d.setTotalMinutes(totalMinutes);
        d.setAssessmentCount(asms.size());
        d.setRecordCount(records.size());
        d.setSpeakingAvg(Math.round(speakingAvg * 10.0) / 10.0);
        d.setWeakSubjects(weak);
        d.setRecentRecords(recent(records));
        return d;
    }

    private Map<String, Integer> computeMastery(List<Assessment> asms) {
        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        for (Assessment a : asms) {
            if (a.getSubject() == null || a.getScore() == null) continue;
            grouped.computeIfAbsent(a.getSubject(), k -> new ArrayList<>()).add(a.getScore());
        }
        Map<String, Integer> mastery = new LinkedHashMap<>();
        grouped.forEach((k, v) -> mastery.put(k,
                (int) Math.round(v.stream().mapToInt(Integer::intValue).average().orElse(0))));
        return mastery;
    }

    private int streakDays(List<LearningRecord> records) {
        if (records.isEmpty()) return 0;
        List<LocalDate> days = records.stream()
                .map(r -> r.getCreatedAt() == null ? null : r.getCreatedAt().toLocalDate())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted(java.util.Comparator.reverseOrder())
                .toList();
        int streak = 1;
        LocalDate prev = days.get(0);
        // 若今天或昨天没有记录，连续打卡中断
        LocalDate today = LocalDate.now();
        if (prev.isBefore(today.minusDays(1))) return 0;
        for (int i = 1; i < days.size(); i++) {
            if (days.get(i).equals(prev.minusDays(1))) {
                streak++;
                prev = days.get(i);
            } else if (days.get(i).equals(prev)) {
                // 同一天，跳过
            } else {
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

    private List<ProgressDashboard.RecentRecord> recent(List<LearningRecord> records) {
        List<ProgressDashboard.RecentRecord> list = new ArrayList<>();
        records.stream().limit(5).forEach(r -> {
            ProgressDashboard.RecentRecord rr = new ProgressDashboard.RecentRecord();
            rr.setId(r.getId());
            rr.setModule(r.getModule());
            rr.setScore(r.getScore());
            rr.setDurationSec(r.getDurationSec());
            rr.setCreatedAt(r.getCreatedAt() == null ? null : r.getCreatedAt().toString());
            list.add(rr);
        });
        return list;
    }

    private Long currentUid(HttpServletRequest request) {
        Object uid = request.getAttribute("uid");
        if (uid instanceof Long l) return l;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return jwtUtil.getUserId(auth.substring(7));
        throw new com.neteacher.common.exception.BizException(
                com.neteacher.common.exception.ErrorCode.UNAUTHORIZED, "未登录");
    }
}

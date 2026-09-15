package com.neteacher.progress.service;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.course.entity.Course;
import com.neteacher.course.repository.CourseRepository;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.repository.LearningRecordRepository;
import com.neteacher.progress.dto.*;
import com.neteacher.progress.entity.CheckIn;
import com.neteacher.progress.repository.CheckInRepository;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.UserAccountRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class GamificationService {

    private static final Map<Integer, String> LEVEL_NAMES = Map.of(
            1, "趣味启蒙", 2, "日常会话", 3, "校园生活",
            4, "阅读进阶", 5, "写作表达", 6, "综合运用");

    private final CheckInRepository checkInRepo;
    private final CourseRepository courseRepo;
    private final LearningRecordRepository recordRepo;
    private final ProgressService progressService;
    private final JwtUtil jwtUtil;
    private final UserAccountRepository userRepo;

    public GamificationService(CheckInRepository checkInRepo, CourseRepository courseRepo,
                               LearningRecordRepository recordRepo, ProgressService progressService,
                               JwtUtil jwtUtil, UserAccountRepository userRepo) {
        this.checkInRepo = checkInRepo;
        this.courseRepo = courseRepo;
        this.recordRepo = recordRepo;
        this.progressService = progressService;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    /** 学生注册年级（用于按年级下发课程），未知则返回 null 表示取通用课程 */
    private Integer gradeOf(Long uid) {
        return userRepo.findById(uid).map(UserAccount::getGrade).orElse(null);
    }

    /** 取某年级某等级的课程：优先该年级课程，无则回退到全量（兼容无年级数据） */
    private List<Course> coursesForGrade(Integer grade, int level) {
        if (grade != null) {
            List<Course> matched = courseRepo.findByGradeOrGradeIsNull(grade).stream()
                    .filter(c -> c.getLevel() != null && c.getLevel() == level)
                    .toList();
            if (!matched.isEmpty()) return matched;
        }
        return courseRepo.findByLevel(level);
    }

    public CheckInStatus getCheckInStatus(HttpServletRequest request) {
        Long uid = currentUid(request);
        LocalDate today = LocalDate.now();
        boolean checkedToday = checkInRepo.findByUserIdAndCheckDate(uid, today).isPresent();
        CheckInStatus s = new CheckInStatus();
        s.setCheckedToday(checkedToday);
        s.setStreakDays(computeStreak(uid, today, checkedToday));
        s.setTotalDays((int) checkInRepo.countByUserId(uid));
        List<Boolean> week = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            week.add(checkInRepo.findByUserIdAndCheckDate(uid, today.minusDays(i)).isPresent());
        }
        s.setWeek(week);
        return s;
    }

    public CheckInStatus doCheckIn(HttpServletRequest request) {
        Long uid = currentUid(request);
        LocalDate today = LocalDate.now();
        if (checkInRepo.findByUserIdAndCheckDate(uid, today).isEmpty()) {
            CheckIn c = new CheckIn();
            c.setUserId(uid);
            c.setCheckDate(today);
            checkInRepo.save(c);
        }
        return getCheckInStatus(request);
    }

    public List<LevelInfo> getLevels(HttpServletRequest request) {
        Long uid = currentUid(request);
        Integer grade = gradeOf(uid);
        ProgressDashboard dash = progressService.dashboard(request);
        int current = clampLevel(dash.getOverallLevel());
        Integer initLevel = dash.getInitLevel();
        Set<Long> completed = completedCourseIds(uid);
        List<LevelInfo> out = new ArrayList<>();
        for (int lv = 1; lv <= 6; lv++) {
            List<Course> courses = coursesForGrade(grade, lv);
            int total = courses.size();
            int done = 0;
            for (Course c : courses) {
                if (completed.contains(c.getId())) done++;
            }
            int stars = total == 0 ? 0 : (done >= total ? 3 : Math.max(1, (int) Math.round(done * 3.0 / total)));
            if (done == 0) stars = 0;
            LevelInfo info = new LevelInfo();
            info.setLv("L" + lv);
            info.setName(LEVEL_NAMES.get(lv));
            info.setUnlocked(lv <= current + 1);
            info.setCurrent(lv == Math.min(6, current + 1));
            info.setCourseCount(total);
            info.setCompletedCount(done);
            info.setStars(stars);
            info.setInitLevel(initLevel);
            firstUnfinishedCourse(courses, completed).ifPresent(c -> {
                info.setFirstCourseId(c.getId());
                info.setFirstCourseTitle(c.getTitle());
            });
            out.add(info);
        }
        return out;
    }

    public HomeToday getHome(HttpServletRequest request) {
        Long uid = currentUid(request);
        Integer grade = gradeOf(uid);
        int current = clampLevel(progressService.dashboard(request).getOverallLevel());
        Set<Long> completed = completedCourseIds(uid);
        List<TodayTask> tasks = new ArrayList<>();
        List<Course> courses = coursesForGrade(grade, current);
        int cnt = 0;
        for (Course c : courses) {
            if (cnt >= 3) break;
            TodayTask t = new TodayTask();
            t.setId("course-" + c.getId());
            t.setTitle(c.getTitle());
            t.setType("course");
            t.setLevel(c.getLevel());
            t.setProgress(completed.contains(c.getId()) ? 100 : 0);
            t.setStarsReward(3);
            t.setCategory(c.getCategory());
            t.setTo("/map");
            tasks.add(t);
            cnt++;
        }
        TodayTask sp = new TodayTask();
        sp.setId("speaking");
        sp.setTitle("跟读训练：今日一句");
        sp.setType("speaking");
        sp.setLevel(current);
        sp.setProgress(0);
        sp.setStarsReward(2);
        sp.setCategory("speaking");
        sp.setTo("/speaking");
        tasks.add(sp);

        List<LevelInfo> levels = getLevels(request);
        int totalStars = levels.stream().mapToInt(LevelInfo::getStars).sum();

        HomeToday.HomeStats stats = new HomeToday.HomeStats();
        ProgressDashboard dash = progressService.dashboard(request);
        stats.setSpeakingAvg(dash.getSpeakingAvg());
        stats.setStreakDays(getCheckInStatus(request).getStreakDays());
        stats.setTotalMinutes(dash.getTotalMinutes());
        stats.setTotalStars(totalStars);

        HomeToday home = new HomeToday();
        home.setTasks(tasks);
        home.setStats(stats);
        home.setCheckIn(getCheckInStatus(request));
        home.setTotalStars(totalStars);
        return home;
    }

    /** 取该关卡中尚未完成的第一门课程，用于点击节点直达练习页 */
    private Optional<Course> firstUnfinishedCourse(List<Course> courses, Set<Long> completed) {
        for (Course c : courses) {
            if (!completed.contains(c.getId())) return Optional.of(c);
        }
        return courses.isEmpty() ? Optional.empty() : Optional.of(courses.get(0));
    }

    private Set<Long> completedCourseIds(Long uid) {
        Set<Long> set = new HashSet<>();
        for (LearningRecord r : recordRepo.findByUserId(uid)) {
            if (Boolean.TRUE.equals(r.getFinished()) && r.getCourseId() != null) {
                set.add(r.getCourseId());
            }
        }
        return set;
    }

    private int computeStreak(Long uid, LocalDate today, boolean checkedToday) {
        int s = 0;
        LocalDate d = checkedToday ? today : today.minusDays(1);
        while (checkInRepo.findByUserIdAndCheckDate(uid, d).isPresent()) {
            s++;
            d = d.minusDays(1);
        }
        return s;
    }

    private int clampLevel(int lv) {
        if (lv < 1) return 1;
        if (lv > 6) return 6;
        return lv;
    }

    private Long currentUid(HttpServletRequest request) {
        Object uid = request.getAttribute("uid");
        if (uid instanceof Long l) {
            return l;
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return jwtUtil.getUserId(auth.substring(7));
        }
        throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
    }
}

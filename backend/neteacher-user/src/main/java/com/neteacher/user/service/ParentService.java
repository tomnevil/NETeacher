package com.neteacher.user.service;

import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.repository.AssessmentRepository;
import com.neteacher.common.ai.LlmPort;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.learning.repository.LearningRecordRepository;
import com.neteacher.recommend.dto.RecommendPath;
import com.neteacher.recommend.service.RecommendService;
import com.neteacher.user.dto.ParentReport;
import com.neteacher.user.dto.UserInfo;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 家长服务（M1）：孩子列表与学情报告聚合。
 */
@Service
@RequiredArgsConstructor
public class ParentService {

    private final UserAccountRepository userRepo;
    private final AssessmentRepository assessmentRepo;
    private final LearningRecordRepository recordRepo;
    private final RecommendService recommendService;
    private final AuthService authService;
    private final LlmPort llmPort;

    public List<UserInfo> children(Long parentUid) {
        return userRepo.findByParentId(parentUid).stream()
                .map(u -> authService.profile(u.getId()))
                .collect(Collectors.toList());
    }

    public ParentReport report(Long parentUid, Long childId) {
        if (childId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "缺少 childId");
        }
        UserAccount child = userRepo.findById(childId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "学生不存在"));
        if (!Objects.equals(child.getParentId(), parentUid)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权查看该学生报告");
        }
        List<Assessment> asms = assessmentRepo.findByUserIdOrderByCreatedAtDesc(childId);
        long recordCount = recordRepo.countByUserId(childId);
        Map<String, Integer> mastery = computeMastery(asms);
        RecommendPath path = recommendService.build(childId);
        String comment = llmPort.chat(buildPrompt(child.getNickname(), mastery, path));

        ParentReport r = new ParentReport();
        r.setChild(authService.profile(childId));
        r.setAssessmentCount(asms.size());
        r.setRecordCount((int) recordCount);
        r.setMastery(mastery);
        r.setPath(path);
        r.setComment(comment);
        return r;
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
        grouped.forEach((k, v) -> mastery.put(k,
                (int) Math.round(v.stream().mapToInt(Integer::intValue).average().orElse(0))));
        return mastery;
    }

    private String buildPrompt(String name, Map<String, Integer> mastery, RecommendPath path) {
        return String.format("你是英语老师，给家长%s写一段 60 字以内的学习情况小结，当前等级 L%d，目标 L%d。",
                name == null ? "的孩子" : name, path.getCurrentLevel(), path.getNextLevel());
    }
}

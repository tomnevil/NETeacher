package com.neteacher.assessment.controller;

import com.neteacher.assessment.dto.AssessmentResult;
import com.neteacher.assessment.dto.DimensionScore;
import com.neteacher.assessment.dto.QuizQuestion;
import com.neteacher.assessment.dto.QuizSubmitRequest;
import com.neteacher.assessment.dto.WrongQuestion;
import com.neteacher.assessment.entity.Assessment;
import com.neteacher.assessment.service.AssessmentService;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Tag(name = "评测", description = "题库抽题 / 自动判分 / 报告")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final JwtUtil jwtUtil;

    @GetMapping("/quiz")
    @Operation(summary = "抽取一套测评题（可按等级/学科筛选）")
    public Result<List<QuizQuestion>> quiz(
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(assessmentService.getQuiz(level, subject, size));
    }

    @PostMapping("/quiz/submit")
    @Operation(summary = "提交测评并自动判分")
    public Result<AssessmentResult> submit(@Valid @RequestBody QuizSubmitRequest req, HttpServletRequest request) {
        return Result.success(assessmentService.submit(currentUid(request), req));
    }

    @GetMapping
    @Operation(summary = "我的测评历史")
    public Result<List<AssessmentResult>> history(HttpServletRequest request) {
        return Result.success(assessmentService.history(currentUid(request)));
    }

    @GetMapping("/wrong")
    @Operation(summary = "错题本：聚合所有测评中答错的题目")
    public Result<List<WrongQuestion>> wrongBook(HttpServletRequest request) {
        return Result.success(assessmentService.wrongBook(currentUid(request)));
    }

    @GetMapping("/ability")
    @Operation(summary = "能力雷达图：各维度最新得分")
    public Result<List<DimensionScore>> ability(HttpServletRequest request) {
        return Result.success(assessmentService.abilityRadar(currentUid(request)));
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

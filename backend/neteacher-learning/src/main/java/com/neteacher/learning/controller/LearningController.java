package com.neteacher.learning.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.PageResult;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.learning.dto.LearningRecordRequest;
import com.neteacher.learning.dto.SpeakingRequest;
import com.neteacher.learning.dto.SpeakingResult;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.service.LearningService;
import com.neteacher.learning.service.SpeakingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学习引擎（M3 / M4）：五模块清单、学习记录提交与查询、口语/听力评测。
 */
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
@Tag(name = "学习引擎", description = "单词/语法/口语/听力/对话")
public class LearningController {

    private final LearningService learningService;
    private final SpeakingService speakingService;
    private final JwtUtil jwtUtil;

    @GetMapping("/modules")
    @Operation(summary = "学习模块清单")
    public Result<List<String>> modules() {
        return Result.success(List.of("word", "grammar", "speaking", "listening", "dialogue"));
    }

    @PostMapping("/records")
    @Operation(summary = "提交一条学习记录")
    public Result<LearningRecord> submit(@Valid @RequestBody LearningRecordRequest req, HttpServletRequest request) {
        return Result.success(learningService.create(currentUid(request), req));
    }

    @GetMapping("/records")
    @Operation(summary = "我的学习记录（分页）")
    public Result<PageResult<LearningRecord>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        return Result.success(learningService.listMine(currentUid(request), page, size));
    }

    @PostMapping("/speaking")
    @Operation(summary = "口语/听力评测（提交转写文本，返回评分与 AI 反馈）")
    public Result<SpeakingResult> speaking(@Valid @RequestBody SpeakingRequest req, HttpServletRequest request) {
        return Result.success(speakingService.evaluate(currentUid(request), req));
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

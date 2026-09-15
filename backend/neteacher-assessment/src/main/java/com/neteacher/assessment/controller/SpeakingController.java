package com.neteacher.assessment.controller;

import com.neteacher.assessment.dto.*;
import com.neteacher.assessment.service.ReadAloudService;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessments/speaking")
public class SpeakingController {

    private final ReadAloudService speakingService;
    private final JwtUtil jwtUtil;

    public SpeakingController(ReadAloudService speakingService, JwtUtil jwtUtil) {
        this.speakingService = speakingService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/task")
    public Result<SpeakingTask> task(@RequestParam(defaultValue = "2") int level,
                                     @RequestParam(required = false) Long courseId) {
        return Result.success(speakingService.getTask(level, courseId));
    }

    @PostMapping("/evaluate")
    public Result<SpeakingEvalResult> evaluate(@RequestBody SpeakingEvalRequest req, HttpServletRequest request) {
        currentUid(request);
        return Result.success(speakingService.evaluate(req));
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

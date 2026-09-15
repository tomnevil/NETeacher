package com.neteacher.learning.controller;

import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.learning.dto.*;
import com.neteacher.learning.service.DialogueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 口语对话：系统发起 -> 学生发言 -> 即时小分 -> 结束总评与强化建议。
 */
@RestController
@RequestMapping("/api/learning/dialogue")
@RequiredArgsConstructor
@Tag(name = "口语对话", description = "系统发起多轮 AI 对话，逐轮评分并给出总评与强化建议")
public class DialogueController {

    private final DialogueService dialogueService;
    private final JwtUtil jwtUtil;

    @PostMapping("/start")
    @Operation(summary = "发起一段 AI 口语对话（系统开场白）")
    public Result<DialogueStartResult> start(@RequestBody DialogueStartRequest req, HttpServletRequest request) {
        return Result.success(dialogueService.start(currentUid(request), req));
    }

    @PostMapping("/turn")
    @Operation(summary = "学生发言一轮：返回系统追问 + 本轮即时评分")
    public Result<DialogueTurnResult> turn(@RequestBody DialogueTurnRequest req, HttpServletRequest request) {
        return Result.success(dialogueService.turn(currentUid(request), req));
    }

    @PostMapping("/end")
    @Operation(summary = "结束对话：返回总评 + 优势/薄弱点 + 强化建议")
    public Result<DialogueEndResult> end(@RequestBody DialogueEndRequest req, HttpServletRequest request) {
        return Result.success(dialogueService.end(currentUid(request), req));
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
        throw new com.neteacher.common.exception.BizException(
                com.neteacher.common.exception.ErrorCode.UNAUTHORIZED, "未登录");
    }
}

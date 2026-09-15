package com.neteacher.user.controller;

import com.neteacher.common.result.Result;
import com.neteacher.user.dto.*;
import com.neteacher.user.service.PlacementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 入学测评定级（API-CRS-001）。
 * GET  /api/placement      获取测评定级题目（不含答案）
 * POST /api/placement      提交作答，判分并返回初始级别 L1-L6，写入 student_profile
 */
@RestController
@RequestMapping("/api/placement")
@RequiredArgsConstructor
public class PlacementController {

    private final PlacementService placementService;

    @GetMapping
    public Result<List<PlacementQuestionDTO>> questions() {
        return Result.success(placementService.getQuestions());
    }

    @PostMapping
    public Result<PlacementResultDTO> submit(@RequestBody PlacementSubmitDTO body, HttpServletRequest request) {
        Object uidAttr = request.getAttribute("uid");
        Long uid = uidAttr == null ? null : Long.valueOf(String.valueOf(uidAttr));
        List<PlacementAnswerDTO> answers = body.getAnswers() == null ? List.of() : body.getAnswers();
        return Result.success(placementService.evaluate(uid, answers));
    }
}

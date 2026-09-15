package com.neteacher.recommend.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.recommend.dto.RecommendByDialogueRequest;
import com.neteacher.recommend.dto.RecommendPath;
import com.neteacher.recommend.dto.RecommendItem;
import com.neteacher.recommend.service.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 推荐与路径（M5）。
 */
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
@Tag(name = "推荐与路径", description = "三维推荐 / 自适应学习地图")
public class RecommendController {

    private final RecommendService recommendService;
    private final JwtUtil jwtUtil;

    @GetMapping("/path")
    @Operation(summary = "获取自适应学习路径（按测评与学习记录计算）")
    public Result<RecommendPath> path(HttpServletRequest request) {
        return Result.success(recommendService.build(currentUid(request)));
    }

    @GetMapping("/explain")
    @Operation(summary = "生成学习路径 AI 解读")
    public Result<String> explain(HttpServletRequest request) {
        return Result.success(recommendService.explain(currentUid(request)));
    }

    @PostMapping("/from-dialogue")
    @Operation(summary = "根据对话薄弱点生成强化练习任务（推荐课程）")
    public Result<List<RecommendItem>> fromDialogue(@RequestBody RecommendByDialogueRequest req, HttpServletRequest request) {
        return Result.success(recommendService.recommendForWeaknesses(currentUid(request), req.getWeaknesses(), req.getGrade(), req.getUnit()));
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

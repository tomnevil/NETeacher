package com.neteacher.progress.controller;

import com.neteacher.common.result.Result;
import com.neteacher.progress.dto.ProgressDashboard;
import com.neteacher.progress.service.ProgressService;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 进度与激励（M6）：仪表盘 / 勋章 / 积分 / 防沉迷。
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Tag(name = "进度与激励", description = "仪表盘 / 勋章 / 积分")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/dashboard")
    @Operation(summary = "学习仪表盘（基于测评与学习记录实时聚合）")
    public Result<ProgressDashboard> dashboard(HttpServletRequest request) {
        return Result.success(progressService.dashboard(request));
    }

    @GetMapping("/forbidden-hours")
    @Operation(summary = "防沉迷禁止时段（该时段不可学习/测评）")
    public Result<Map<String, Integer>> forbiddenHours() {
        return Result.success(Map.of("forbiddenStart", 21, "forbiddenEnd", 8));
    }
}

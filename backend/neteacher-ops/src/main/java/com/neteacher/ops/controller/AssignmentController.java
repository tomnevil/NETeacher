package com.neteacher.ops.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.ops.dto.AssignmentCreateDTO;
import com.neteacher.ops.dto.AssignmentStatsDTO;
import com.neteacher.ops.entity.Assignment;
import com.neteacher.ops.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作业布置与统计（FR-TRK-010）。
 */
@Tag(name = "assignment", description = "作业")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final JwtUtil jwtUtil;

    @PostMapping("/teacher/assignments")
    @Operation(summary = "布置作业：以试卷为载体下发给班级")
    public Result<Assignment> create(@RequestBody AssignmentCreateDTO req, HttpServletRequest request) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(assignmentService.create(currentUid(request), req));
    }

    @GetMapping("/teacher/assignments")
    @Operation(summary = "我布置的作业")
    public Result<List<Assignment>> myAssignments(HttpServletRequest request) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(assignmentService.listByTeacher(currentUid(request)));
    }

    @GetMapping("/teacher/assignments/{id}/stats")
    @Operation(summary = "作业统计：完成率 / 均分 / 薄弱知识点 / 学生明细")
    public Result<AssignmentStatsDTO> stats(@PathVariable Long id, HttpServletRequest request) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(assignmentService.stats(id));
    }

    @GetMapping("/assignments/mine")
    @Operation(summary = "我的作业（学生端）")
    public Result<List<Assignment>> mine(HttpServletRequest request) {
        requireRole(request, "STUDENT");
        return Result.success(assignmentService.listForStudent(currentUid(request)));
    }

    private void requireRole(HttpServletRequest req, String... allowed) {
        Object roleAttr = req.getAttribute("role");
        String role = roleAttr == null ? null : String.valueOf(roleAttr);
        for (String a : allowed) {
            if (a.equalsIgnoreCase(role)) {
                return;
            }
        }
        throw new BizException(ErrorCode.FORBIDDEN, "需要 " + String.join("/", allowed) + " 角色");
    }

    private Long currentUid(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            auth = auth.substring(7);
        }
        if (auth == null || auth.isBlank()) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return jwtUtil.getUserId(auth);
    }
}

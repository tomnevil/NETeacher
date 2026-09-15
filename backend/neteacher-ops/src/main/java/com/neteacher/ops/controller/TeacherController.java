package com.neteacher.ops.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.ops.dto.ClassOverview;
import com.neteacher.ops.service.TeacherDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 老师端接口（M7）：班级学情看板。
 * 访问控制：必须为 TEACHER 角色；查看单个班级时还需校验「该教师确实任教此班」。
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "老师端", description = "班级学情看板（需教师角色）")
public class TeacherController {

    private final TeacherDashboardService dashboardService;
    private final JwtUtil jwtUtil;

    @GetMapping("/classes")
    @Operation(summary = "我任教的班级学情概览")
    public Result<List<ClassOverview>> myClasses(HttpServletRequest request) {
        Long teacherId = requireTeacher(request);
        return Result.success(dashboardService.overviewByTeacher(teacherId));
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "指定班级的学情详情（仅任教该班的教师可访问）")
    public Result<ClassOverview> classDetail(@PathVariable Long classId, HttpServletRequest request) {
        Long teacherId = requireTeacher(request);
        return Result.success(dashboardService.overviewByClassForTeacher(teacherId, classId));
    }

    /** 强制校验当前登录者为教师角色，返回其 uid */
    private Long requireTeacher(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        if (!(role instanceof String r) || !"TEACHER".equals(r)) {
            throw new BizException(ErrorCode.FORBIDDEN, "仅教师可访问该功能");
        }
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

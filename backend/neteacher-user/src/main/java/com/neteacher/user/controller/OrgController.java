package com.neteacher.user.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.user.dto.CreateClassRequest;
import com.neteacher.user.dto.OrgClass;
import com.neteacher.user.dto.OrgMember;
import com.neteacher.user.dto.OrgSchool;
import com.neteacher.user.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织模型接口（M1 扩展）：学校 / 班级 / 教师-班级绑定。
 * 访问控制：管理类（写）操作需 ADMIN 或 TEACHER 角色；只读查询需 TEACHER 角色。
 * 当前演示数据未播种 ADMIN 角色，因此这些接口实际由教师账号（如 13700000000）调用。
 */
@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
@Tag(name = "组织模型", description = "学校 / 班级 / 教师绑定")
public class OrgController {

    private final OrgService orgService;

    @GetMapping("/schools")
    @Operation(summary = "学校列表（含班级与师生数）")
    public Result<List<OrgSchool>> schools(HttpServletRequest request) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.listSchools());
    }

    @GetMapping("/classes")
    @Operation(summary = "班级列表，可按学校或年级过滤")
    public Result<List<OrgClass>> classes(HttpServletRequest request,
                                          @RequestParam(required = false) Long schoolId,
                                          @RequestParam(required = false) Integer grade) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.listClasses(schoolId, grade));
    }

    @GetMapping("/teachers/{teacherId}/classes")
    @Operation(summary = "某教师任教的班级列表")
    public Result<List<OrgClass>> teacherClasses(HttpServletRequest request, @PathVariable Long teacherId) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.classesOfTeacher(teacherId));
    }

    @PostMapping("/classes")
    @Operation(summary = "新建班级（可指定班主任，需 ADMIN/TEACHER）")
    public Result<OrgClass> createClass(HttpServletRequest request, @Valid @RequestBody CreateClassRequest req) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.createClass(req));
    }

    @PostMapping("/classes/{classId}/teachers/{teacherId}")
    @Operation(summary = "将教师绑定到班级（需 ADMIN/TEACHER）")
    public Result<Void> bindTeacher(HttpServletRequest request, @PathVariable Long classId, @PathVariable Long teacherId) {
        requireRole(request, "ADMIN", "TEACHER");
        orgService.bindTeacher(teacherId, classId);
        return Result.success(null);
    }

    @GetMapping("/classes/{classId}/students")
    @Operation(summary = "班级学生名单")
    public Result<List<OrgMember>> classStudents(HttpServletRequest request, @PathVariable Long classId) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.studentsOfClass(classId));
    }

    @PostMapping("/classes/{classId}/students/{studentId}")
    @Operation(summary = "将学生绑定到班级（需 ADMIN/TEACHER）")
    public Result<OrgMember> bindStudent(HttpServletRequest request, @PathVariable Long classId, @PathVariable Long studentId) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.bindStudent(studentId, classId));
    }

    @DeleteMapping("/students/{studentId}/class")
    @Operation(summary = "将学生移出班级（需 ADMIN/TEACHER）")
    public Result<Void> unbindStudent(HttpServletRequest request, @PathVariable Long studentId) {
        requireRole(request, "ADMIN", "TEACHER");
        orgService.unbindStudent(studentId);
        return Result.success(null);
    }

    @GetMapping("/students/unassigned")
    @Operation(summary = "未分配班级的学生列表")
    public Result<List<OrgMember>> unassignedStudents(HttpServletRequest request) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.unassignedStudents());
    }

    @GetMapping("/schools/{schoolId}/teachers")
    @Operation(summary = "学校教师列表")
    public Result<List<OrgMember>> schoolTeachers(HttpServletRequest request, @PathVariable Long schoolId) {
        requireRole(request, "ADMIN", "TEACHER");
        return Result.success(orgService.teachersOfSchool(schoolId));
    }

    /** 校验当前登录者角色是否在允许范围内，否则抛出 403 */
    private void requireRole(HttpServletRequest request, String... allowed) {
        Object role = request.getAttribute("role");
        String current = role instanceof String s ? s : null;
        boolean ok = false;
        if (current != null) {
            for (String a : allowed) {
                if (a.equals(current)) {
                    ok = true;
                    break;
                }
            }
        }
        if (!ok) {
            throw new BizException(ErrorCode.FORBIDDEN,
                    "需要 " + String.join(" 或 ", allowed) + " 角色才能执行该操作");
        }
    }
}

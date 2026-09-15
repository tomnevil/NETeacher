package com.neteacher.course.controller;

import com.neteacher.common.result.PageResult;
import com.neteacher.common.result.Result;
import com.neteacher.course.entity.Course;
import com.neteacher.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程接口（M2）。
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "课程", description = "分级课程体系")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "课程列表（支持按等级/分类/专题/年级筛选与分页）")
    public Result<PageResult<Course>> list(
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer grade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(courseService.list(level, category, topic, grade, page, size));
    }

    @GetMapping("/topics")
    @Operation(summary = "专项练习专题列表（单词/口语/听力/阅读/语法）")
    public Result<java.util.List<java.util.Map<String, Object>>> topics(
            @RequestParam(required = false) Integer grade) {
        return Result.success(courseService.topics(grade));
    }

    @GetMapping("/{id}")
    @Operation(summary = "课程详情")
    public Result<Course> detail(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新建课程")
    public Result<Course> create(@Valid @RequestBody Course course) {
        return Result.success(courseService.create(course));
    }
}

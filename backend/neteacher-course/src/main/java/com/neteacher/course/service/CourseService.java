package com.neteacher.course.service;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.PageResult;
import com.neteacher.course.entity.Course;
import com.neteacher.course.repository.CourseRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程服务（M2）。
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    /** 专项练习的展示顺序与中文名 */
    private static final Map<String, String> TOPICS = new LinkedHashMap<>(Map.of(
            "WORD", "单词",
            "SPEAKING", "口语",
            "LISTENING", "听力",
            "READING", "阅读",
            "GRAMMAR", "语法"));

    private final CourseRepository courseRepo;

    public PageResult<Course> list(Integer level, String category, int page, int size) {
        return list(level, category, null, null, page, size);
    }

    public PageResult<Course> list(Integer level, String category, String topic, Integer grade,
                                   int page, int size) {
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (level != null) {
                ps.add(cb.equal(root.get("level"), level));
            }
            if (category != null && !category.isBlank()) {
                ps.add(cb.equal(root.get("category"), category));
            }
            if (topic != null && !topic.isBlank()) {
                ps.add(cb.equal(root.get("topic"), topic));
            }
            if (grade != null) {
                // 该年级课程 + 全年级通用（grade 为空）
                ps.add(cb.or(cb.equal(root.get("grade"), grade), cb.isNull(root.get("grade"))));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Page<Course> pg = courseRepo.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "level").and(Sort.by("id"))));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), pg.getNumber(), pg.getSize());
    }

    /**
     * 专项练习专题列表：按 topic 聚合数量，供前端「专项练习」入口展示。
     */
    public List<Map<String, Object>> topics(Integer grade) {
        List<Course> specials = grade != null
                ? courseRepo.findByGradeAndCategory(grade, "SPECIAL")
                : courseRepo.findByCategory("SPECIAL");
        List<Map<String, Object>> out = new ArrayList<>();
        TOPICS.forEach((topic, label) -> {
            long count = specials.stream().filter(c -> topic.equals(c.getTopic())).count();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("topic", topic);
            item.put("label", label);
            item.put("count", count);
            out.add(item);
        });
        return out;
    }

    public List<Course> listByTopic(String topic, Integer grade) {
        List<Course> specials = grade != null
                ? courseRepo.findByGradeAndCategory(grade, "SPECIAL")
                : courseRepo.findByCategory("SPECIAL");
        if (topic == null || topic.isBlank()) {
            return specials;
        }
        return specials.stream().filter(c -> topic.equals(c.getTopic())).toList();
    }

    public Course getById(Long id) {
        return courseRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "课程不存在"));
    }

    public Course create(Course course) {
        return courseRepo.save(course);
    }
}

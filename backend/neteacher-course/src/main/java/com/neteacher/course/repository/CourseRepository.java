package com.neteacher.course.repository;

import com.neteacher.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    List<Course> findByLevel(Integer level);

    List<Course> findByCategory(String category);

    /** 按年级取课（含全年级通用 grade 为空的课程） */
    List<Course> findByGradeOrGradeIsNull(Integer grade);

    /** 某年级的专项课程 */
    List<Course> findByGradeAndCategory(Integer grade, String category);

    List<Course> findByCategoryAndTopic(String category, String topic);
}

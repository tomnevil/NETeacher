package com.neteacher.user.repository;

import com.neteacher.user.entity.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {

    List<TeacherClass> findByTeacherId(Long teacherId);

    List<TeacherClass> findByClassId(Long classId);

    boolean existsByTeacherIdAndClassId(Long teacherId, Long classId);
}

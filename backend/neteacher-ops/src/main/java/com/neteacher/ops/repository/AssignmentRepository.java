package com.neteacher.ops.repository;

import com.neteacher.ops.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<Assignment> findByClassIdOrderByCreatedAtDesc(Long classId);
}

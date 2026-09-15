package com.neteacher.assessment.repository;

import com.neteacher.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Assessment> findFirstByUserIdAndSubjectOrderByCreatedAtDesc(Long userId, String subject);
}

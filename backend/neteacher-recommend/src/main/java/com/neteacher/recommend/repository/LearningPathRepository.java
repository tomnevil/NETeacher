package com.neteacher.recommend.repository;

import com.neteacher.recommend.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    Optional<LearningPath> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}

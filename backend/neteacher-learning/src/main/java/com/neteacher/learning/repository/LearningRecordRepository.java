package com.neteacher.learning.repository;

import com.neteacher.learning.entity.LearningRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {

    Page<LearningRecord> findByUserId(Long userId, Pageable pageable);

    List<LearningRecord> findByUserId(Long userId);

    long countByUserId(Long userId);
}

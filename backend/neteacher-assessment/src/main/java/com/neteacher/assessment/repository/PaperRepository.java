package com.neteacher.assessment.repository;

import com.neteacher.assessment.entity.Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperRepository extends JpaRepository<Paper, Long> {

    List<Paper> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
}

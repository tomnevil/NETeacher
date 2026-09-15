package com.neteacher.ops.repository;

import com.neteacher.ops.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    MembershipPlan findByTier(String tier);
}

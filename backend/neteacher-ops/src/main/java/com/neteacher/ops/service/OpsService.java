package com.neteacher.ops.service;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.ops.entity.Membership;
import com.neteacher.ops.entity.MembershipPlan;
import com.neteacher.ops.repository.MembershipPlanRepository;
import com.neteacher.ops.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 运营与订单（M7）：会员套餐查询与报名。
 */
@Service
@RequiredArgsConstructor
public class OpsService {

    private final MembershipPlanRepository planRepo;
    private final MembershipRepository membershipRepo;

    public List<MembershipPlan> plans() {
        return planRepo.findAll();
    }

    public Membership enroll(String tier, Long uid) {
        MembershipPlan plan = planRepo.findByTier(tier);
        if (plan == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "套餐不存在");
        }
        Membership m = new Membership();
        m.setUserId(uid);
        m.setPlan(plan.getTier());
        if (!"FREE".equals(plan.getTier())) {
            m.setExpireAt(LocalDate.now().plusDays(30));
        }
        return membershipRepo.save(m);
    }

    public Membership mine(Long uid) {
        return membershipRepo.findByUserId(uid).stream()
                .reduce((a, b) -> b)
                .orElse(null);
    }
}

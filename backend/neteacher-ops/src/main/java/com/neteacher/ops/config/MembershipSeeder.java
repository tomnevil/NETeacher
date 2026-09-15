package com.neteacher.ops.config;

import com.neteacher.ops.entity.MembershipPlan;
import com.neteacher.ops.repository.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化会员套餐（FREE / PRO）。
 */
@Component
@RequiredArgsConstructor
public class MembershipSeeder implements CommandLineRunner {

    private final MembershipPlanRepository planRepo;

    @Override
    public void run(String... args) {
        if (planRepo.count() > 0) return;
        MembershipPlan free = new MembershipPlan();
        free.setTier("FREE");
        free.setName("免费版");
        free.setPriceMonths(0);
        free.setLevel(3);
        free.setBenefits("基础分级课程 + 每月 5 次能力测评 + 学习记录");
        planRepo.save(free);

        MembershipPlan pro = new MembershipPlan();
        pro.setTier("PRO");
        pro.setName("会员版");
        pro.setPriceMonths(2990);
        pro.setLevel(6);
        pro.setBenefits("全部等级课程 + 无限测评与 AI 学情报告 + 自适应路径 + 家长端报告 + 口语评测");
        planRepo.save(pro);
    }
}

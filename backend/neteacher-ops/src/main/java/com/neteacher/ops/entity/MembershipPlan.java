package com.neteacher.ops.entity;

import com.neteacher.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 会员套餐定义。tier 区分 FREE / PRO。
 */
@Getter
@Setter
@Entity
@Table(name = "membership_plan")
public class MembershipPlan extends BaseEntity {

    @Column(length = 16, nullable = false)
    private String tier;

    @Column(nullable = false)
    private String name;

    /** 月费（分），0 表示免费 */
    private Integer priceMonths;

    @Column(length = 512)
    private String benefits;

    /** 对应解锁等级上限 L1-L6 */
    private Integer level;
}

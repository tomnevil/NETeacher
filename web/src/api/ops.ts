import request from './request'
import type { Result, MembershipPlan, Membership } from './types'

/** 会员套餐列表 */
export function listPlans() {
  return request.get<Result<MembershipPlan[]>>('/ops/plans')
}

/** 报名会员套餐 */
export function enroll(tier: string) {
  return request.post<Result<Membership>>('/ops/enroll', { tier })
}

/** 我的会员状态 */
export function myMembership() {
  return request.get<Result<Membership>>('/ops/mine')
}

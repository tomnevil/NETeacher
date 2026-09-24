import request from './request'
import type {
  Result,
  MembershipPlan,
  Membership,
  OpsDashboard,
  Assignment,
  AssignmentStats,
  ClassOverview
} from './types'

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

/** 运营/质量看板（仅 ADMIN） */
export function opsDashboard() {
  return request.get<Result<OpsDashboard>>('/ops/dashboard')
}

/** 教师端班级列表（用于布置作业） */
export function listTeacherClasses() {
  return request.get<Result<ClassOverview[]>>('/teacher/classes')
}

/** 布置作业 */
export function createAssignment(dto: {
  paperId: number
  classId: number
  title?: string
  dueAt?: string
}) {
  return request.post<Result<Assignment>>('/teacher/assignments', dto)
}

/** 我布置的作业 */
export function listAssignments() {
  return request.get<Result<Assignment[]>>('/teacher/assignments')
}

/** 作业统计 */
export function assignmentStats(id: number) {
  return request.get<Result<AssignmentStats>>(`/teacher/assignments/${id}/stats`)
}

/** 我的作业（学生端） */
export function myHomework() {
  return request.get<Result<Assignment[]>>('/assignments/mine')
}

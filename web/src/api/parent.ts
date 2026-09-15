import request from './request'
import type { Result, ParentReport } from './types'
import type { UserInfo } from './user'

/** 绑定状态 */
export function getBindStatus() {
  return request.get<Result<{ bound: boolean; parent?: { phone: string; nickname: string } }>>(
    '/user/bind-status'
  )
}

/** 绑定家长（按手机号，家长不存在则自动创建） */
export function bindParent(parentPhone: string) {
  return request.post<Result<UserInfo>>('/user/bind-parent', { parentPhone })
}

/** 我绑定的孩子列表 */
export function listChildren() {
  return request.get<Result<UserInfo[]>>('/parent/children')
}

/** 孩子学情报告 */
export function getReport(childId: number) {
  return request.get<Result<ParentReport>>('/parent/report/' + childId)
}

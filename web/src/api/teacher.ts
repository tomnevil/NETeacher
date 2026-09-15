import request from './request'
import type { Result, ClassOverview } from './types'

/** 我任教的班级学情概览 */
export function myClasses() {
  return request.get<Result<ClassOverview[]>>('/teacher/classes')
}

/** 指定班级学情详情 */
export function classDetail(classId: number) {
  return request.get<Result<ClassOverview>>(`/teacher/classes/${classId}`)
}

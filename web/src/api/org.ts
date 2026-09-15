import request from './request'
import type { Result, OrgClass, OrgSchool, OrgMember } from './types'

/** 学校列表 */
export function listSchools() {
  return request.get<Result<OrgSchool[]>>('/org/schools')
}

/** 班级列表（可按学校 / 年级过滤） */
export function listClasses(params?: { schoolId?: number; grade?: number }) {
  return request.get<Result<OrgClass[]>>('/org/classes', { params })
}

/** 某教师任教的班级 */
export function listTeacherClasses(teacherId: number) {
  return request.get<Result<OrgClass[]>>(`/org/teachers/${teacherId}/classes`)
}

/** 新建班级 */
export function createClass(body: {
  name: string
  schoolId: number
  grade: number
  headTeacherId?: number
}) {
  return request.post<Result<OrgClass>>('/org/classes', body)
}

/** 班级学生名单 */
export function classStudents(classId: number) {
  return request.get<Result<OrgMember[]>>(`/org/classes/${classId}/students`)
}

/** 将学生绑定到班级 */
export function bindStudent(classId: number, studentId: number) {
  return request.post<Result<OrgMember>>(`/org/classes/${classId}/students/${studentId}`)
}

/** 将学生移出班级 */
export function unbindStudent(studentId: number) {
  return request.delete<Result<void>>(`/org/students/${studentId}/class`)
}

/** 未分配班级的学生 */
export function unassignedStudents() {
  return request.get<Result<OrgMember[]>>('/org/students/unassigned')
}

/** 学校教师列表 */
export function schoolTeachers(schoolId: number) {
  return request.get<Result<OrgMember[]>>(`/org/schools/${schoolId}/teachers`)
}

/** 将教师绑定到班级 */
export function bindTeacher(classId: number, teacherId: number) {
  return request.post<Result<void>>(`/org/classes/${classId}/teachers/${teacherId}`)
}

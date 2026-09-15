import request from './request'
import type { Result, PageResult, Course, TopicInfo } from './types'

export function listCourses(params?: {
  level?: number
  category?: string
  topic?: string
  grade?: number
  page?: number
  size?: number
}) {
  return request.get<Result<PageResult<Course>>>('/courses', { params })
}

/** 专项练习专题（单词/口语/听力/阅读/语法） */
export function listTopics(grade?: number) {
  return request.get<Result<TopicInfo[]>>('/courses/topics', { params: { grade } })
}

export function getCourse(id: number) {
  return request.get<Result<Course>>('/courses/' + id)
}

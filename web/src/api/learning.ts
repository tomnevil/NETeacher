import request from './request'
import type { Result, PageResult, LearningRecord, SpeakingResult, DialogueStartResult, DialogueTurnResult, DialogueEndResult } from './types'

/** 学习模块清单 */
export function getModules() {
  return request.get<Result<string[]>>('/learning/modules')
}

/** 提交一条学习记录 */
export function submitRecord(req: {
  courseId: number
  lessonId?: number
  module?: string
  score?: number
  durationSec?: number
  finished?: boolean
  detail?: string
}) {
  return request.post<Result<LearningRecord>>('/learning/records', req)
}

/** 我的学习记录（分页） */
export function listRecords(params?: { page?: number; size?: number }) {
  return request.get<Result<PageResult<LearningRecord>>>('/learning/records', { params })
}

/** 口语/听力评测：提交转写文本，返回评分与 AI 反馈 */
export function speaking(req: {
  courseId?: number
  module?: string
  targetText: string
  transcript: string
  durationSec?: number
}) {
  return request.post<Result<SpeakingResult>>('/learning/speaking', req)
}

/** 发起一段 AI 口语对话（系统开场白） */
export function startDialogue(req: { grade?: number; unit?: string }) {
  return request.post<Result<DialogueStartResult>>('/learning/dialogue/start', req)
}

/** 学生发言一轮：返回系统追问 + 本轮即时评分 */
export function dialogueTurn(req: { sessionId: string; transcript: string; durationSec?: number }) {
  return request.post<Result<DialogueTurnResult>>('/learning/dialogue/turn', req)
}

/** 结束对话：返回总评 + 优势/薄弱点 + 强化建议 */
export function endDialogue(req: { sessionId: string }) {
  return request.post<Result<DialogueEndResult>>('/learning/dialogue/end', req)
}

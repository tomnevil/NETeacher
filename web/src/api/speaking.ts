import request from './request'
import type { Result, SpeakingTask, SpeakingEvalResult } from './types'

/** 获取一句跟读任务（可指定课程，用于学习地图关卡直达） */
export function getSpeakingTask(level = 2, courseId?: number) {
  return request.get<Result<SpeakingTask>>('/assessments/speaking/task', {
    params: { level, ...(courseId ? { courseId } : {}) }
  })
}

/** 提交跟读评测（参考文本 + 学生转写） */
export function evaluateSpeaking(req: { taskId?: number; refText: string; transcript: string }) {
  return request.post<Result<SpeakingEvalResult>>('/assessments/speaking/evaluate', req)
}

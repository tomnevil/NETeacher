import request from './request'
import type { Result, QuizQuestion, AssessmentResult, WrongQuestion } from './types'

/** 抽取一组测评题目（含 subject / level 可选筛选） */
export function getQuiz(params?: { subject?: string; level?: number }) {
  return request.get<Result<QuizQuestion[]>>('/assessments/quiz', { params })
}

/** 提交测评答案，返回自动判分结果与 AI 评语 */
export function submitQuiz(req: {
  subject: string
  level: number
  type?: string
  answers: { questionId: number; answer: string }[]
}) {
  return request.post<Result<AssessmentResult>>('/assessments/quiz/submit', req)
}

/** 错题本：聚合所有测评中答错的题目 */
export function getWrongBook() {
  return request.get<Result<WrongQuestion[]>>('/assessments/wrong')
}

/** 我的测评历史 */
export function listAssessments() {
  return request.get<Result<AssessmentResult[]>>('/assessments')
}

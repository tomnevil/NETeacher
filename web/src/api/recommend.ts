import request from './request'
import type { Result, RecommendPath, RecommendItem } from './types'

/** 自适应学习路径（基于测评与学习记录计算） */
export function getPath() {
  return request.get<Result<RecommendPath>>('/recommend/path')
}

/** AI 学习建议（中文） */
export function getExplain() {
  return request.get<Result<string>>('/recommend/explain')
}

/** 根据对话薄弱点生成强化练习任务（推荐课程） */
export function recommendFromDialogue(req: { grade?: number; unit?: string; weaknesses?: string[] }) {
  return request.post<Result<RecommendItem[]>>('/recommend/from-dialogue', req)
}

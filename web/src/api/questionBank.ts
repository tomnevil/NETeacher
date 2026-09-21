import request from './request'
import type {
  Result,
  QuestionBankItem,
  QuestionUpsert,
  QuestionGenRequest,
  GenerateResult,
  ImportResult,
  CoverageStat,
  PageResult
} from './types'

export interface QuestionQuery {
  level?: number
  subject?: string
  type?: string
  status?: string
  usage?: string
  knowledgePoint?: string
  keyword?: string
  page?: number
  size?: number
}

export function listQuestions(params: QuestionQuery) {
  return request.get<Result<PageResult<QuestionBankItem>>>('/ops/questions', { params })
}

export function getQuestion(id: number) {
  return request.get<Result<QuestionBankItem>>(`/ops/questions/${id}`)
}

export function createQuestion(dto: QuestionUpsert) {
  return request.post<Result<QuestionBankItem>>('/ops/questions', dto)
}

export function updateQuestion(id: number, dto: QuestionUpsert) {
  return request.put<Result<QuestionBankItem>>(`/ops/questions/${id}`, dto)
}

export function deleteQuestion(id: number) {
  return request.delete<Result<unknown>>(`/ops/questions/${id}`)
}

export function importQuestions(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post<Result<ImportResult>>('/ops/questions/import', fd, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 覆盖度统计。by=subject（默认，等级 × 学科）或 knowledgePoint（等级 × 知识点） */
export function coverage(by: 'subject' | 'knowledgePoint' = 'subject') {
  return request.get<Result<CoverageStat[]>>('/ops/questions/coverage', { params: { by } })
}

/** AI 出题：生成草稿（status=draft, source=ai），需复核后发布 */
export function generateQuestionDrafts(dto: QuestionGenRequest) {
  return request.post<Result<GenerateResult>>('/ops/questions/generate', dto)
}

function downloadBlob(data: Blob, filename: string) {
  const url = window.URL.createObjectURL(data)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}

export async function exportQuestions() {
  const resp = await request.get('/ops/questions/export', { responseType: 'blob' })
  downloadBlob(resp.data as Blob, 'question-bank.csv')
}

export async function downloadTemplate() {
  const resp = await request.get('/ops/questions/template', { responseType: 'blob' })
  downloadBlob(resp.data as Blob, 'question-bank-template.csv')
}

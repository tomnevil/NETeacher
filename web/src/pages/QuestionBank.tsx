import { useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  listQuestions,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  importQuestions,
  coverage,
  exportQuestions,
  downloadTemplate
} from '../api/questionBank'
import type { QuestionBankItem, QuestionUpsert, QuestionStatus, QuestionSource } from '../api/types'

const SUBJECTS = ['listening', 'speaking', 'reading', 'writing', 'word', 'grammar']
const LEVELS = [1, 2, 3, 4, 5, 6]
const STATUSES: QuestionStatus[] = ['draft', 'reviewed', 'published']
const SOURCES: QuestionSource[] = ['teacher', 'ai', 'imported', 'seeded']

const STATUS_LABEL: Record<string, string> = {
  draft: '草稿',
  reviewed: '待发布',
  published: '已发布'
}

const emptyForm = (): QuestionUpsert => ({
  level: 1,
  subject: 'word',
  type: 'mcq',
  stem: '',
  options: ['', '', '', ''],
  answer: '',
  analysis: '',
  knowledgePoint: '',
  mediaUrl: '',
  usage: 'practice',
  status: 'published',
  source: 'teacher'
})

export default function QuestionBank() {
  const qc = useQueryClient()
  const [filters, setFilters] = useState<{ level?: number; subject?: string; status?: string; keyword?: string }>({})
  const [page, setPage] = useState(0)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<QuestionBankItem | null>(null)
  const [form, setForm] = useState<QuestionUpsert>(emptyForm())
  const [importMsg, setImportMsg] = useState<string>('')
  const fileRef = useRef<HTMLInputElement>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['questions', filters, page],
    queryFn: () => listQuestions({ ...filters, page, size: 15 }).then((r) => r.data.data)
  })

  const { data: cov } = useQuery({
    queryKey: ['coverage'],
    queryFn: () => coverage().then((r) => r.data.data)
  })

  const saveMutation = useMutation({
    mutationFn: (dto: QuestionUpsert) =>
      editing ? updateQuestion(editing.id, dto) : createQuestion(dto),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['questions'] })
      qc.invalidateQueries({ queryKey: ['coverage'] })
      setModalOpen(false)
    }
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => deleteQuestion(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['questions'] })
      qc.invalidateQueries({ queryKey: ['coverage'] })
    }
  })

  const importMutation = useMutation({
    mutationFn: (file: File) => importQuestions(file),
    onSuccess: (resp) => {
      qc.invalidateQueries({ queryKey: ['questions'] })
      qc.invalidateQueries({ queryKey: ['coverage'] })
      const d = resp.data.data
      setImportMsg(`导入完成：成功 ${d.imported} 条，跳过 ${d.skipped} 条。` + (d.errors.length ? '\n' + d.errors.slice(0, 5).join('\n') : ''))
    },
    onError: (e: any) => setImportMsg('导入失败：' + (e?.response?.data?.message || e.message))
  })

  const openCreate = () => {
    setEditing(null)
    setForm(emptyForm())
    setModalOpen(true)
  }

  const openEdit = (q: QuestionBankItem) => {
    setEditing(q)
    setForm({
      level: q.level,
      subject: q.subject,
      type: q.type,
      stem: q.stem,
      options: q.options && q.options.length ? q.options : [''],
      answer: q.answer,
      analysis: q.analysis || '',
      knowledgePoint: q.knowledgePoint || '',
      mediaUrl: q.mediaUrl || '',
      usage: q.usage || '',
      status: q.status,
      source: q.source
    })
    setModalOpen(true)
  }

  const setOpt = (i: number, v: string) => {
    const next = [...form.options]
    next[i] = v
    setForm({ ...form, options: next })
  }

  const pageData = data?.content || []
  const totalPages = data?.totalPages || 1

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-800">题库管理</h1>

      {/* 覆盖度概览 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-2 text-sm font-semibold text-gray-600">覆盖度（等级 × 学科）</div>
        <div className="grid grid-cols-2 gap-2 md:grid-cols-3 lg:grid-cols-4">
          {cov?.map((c) => (
            <div key={c.level + '-' + c.subject} className="rounded-2xl border border-gray-100 bg-gray-50 p-3">
              <div className="text-xs text-gray-500">L{c.level} · {c.subject}</div>
              <div className="mt-1 text-lg font-bold text-indigo-600">{c.total}</div>
              <div className="text-[11px] text-gray-400">已发布 {c.published} / 草稿 {c.draft}</div>
            </div>
          ))}
        </div>
      </div>

      {/* 过滤 + 操作 */}
      <div className="flex flex-wrap items-end gap-3 rounded-3xl bg-white p-4 shadow-sm">
        <label className="text-xs text-gray-500">
          等级
          <select
            className="mt-1 block w-24 rounded-xl border border-gray-200 px-2 py-1.5"
            value={filters.level ?? ''}
            onChange={(e) => setFilters({ ...filters, level: e.target.value ? Number(e.target.value) : undefined })}
          >
            <option value="">全部</option>
            {LEVELS.map((l) => (
              <option key={l} value={l}>L{l}</option>
            ))}
          </select>
        </label>
        <label className="text-xs text-gray-500">
          学科
          <select
            className="mt-1 block w-32 rounded-xl border border-gray-200 px-2 py-1.5"
            value={filters.subject ?? ''}
            onChange={(e) => setFilters({ ...filters, subject: e.target.value || undefined })}
          >
            <option value="">全部</option>
            {SUBJECTS.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </label>
        <label className="text-xs text-gray-500">
          状态
          <select
            className="mt-1 block w-28 rounded-xl border border-gray-200 px-2 py-1.5"
            value={filters.status ?? ''}
            onChange={(e) => setFilters({ ...filters, status: e.target.value || undefined })}
          >
            <option value="">全部</option>
            {STATUSES.map((s) => (
              <option key={s} value={s}>{STATUS_LABEL[s]}</option>
            ))}
          </select>
        </label>
        <label className="text-xs text-gray-500">
          关键字
          <input
            className="mt-1 block w-40 rounded-xl border border-gray-200 px-2 py-1.5"
            placeholder="题干/知识点"
            value={filters.keyword ?? ''}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value || undefined })}
            onKeyDown={(e) => e.key === 'Enter' && setPage(0)}
          />
        </label>
        <button
          className="rounded-xl bg-indigo-600 px-4 py-2 text-sm text-white"
          onClick={() => setPage(0)}
        >
          查询
        </button>

        <div className="ml-auto flex flex-wrap gap-2">
          <button className="rounded-xl bg-emerald-600 px-3 py-2 text-sm text-white" onClick={openCreate}>
            + 新增题目
          </button>
          <button
            className="rounded-xl border border-gray-300 px-3 py-2 text-sm"
            onClick={() => fileRef.current?.click()}
          >
            批量导入 CSV
          </button>
          <input
            ref={fileRef}
            type="file"
            accept=".csv"
            className="hidden"
            onChange={(e) => {
              const f = e.target.files?.[0]
              if (f) importMutation.mutate(f)
              e.target.value = ''
            }}
          />
          <button className="rounded-xl border border-gray-300 px-3 py-2 text-sm" onClick={() => exportQuestions()}>
            导出 CSV
          </button>
          <button className="rounded-xl border border-gray-300 px-3 py-2 text-sm" onClick={() => downloadTemplate()}>
            下载模板
          </button>
        </div>
      </div>

      {importMsg && (
        <div className="whitespace-pre-wrap rounded-2xl border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
          {importMsg}
        </div>
      )}

      {/* 列表 */}
      <div className="overflow-x-auto rounded-3xl bg-white p-2 shadow-sm">
        <table className="w-full text-left text-sm">
          <thead className="text-xs text-gray-400">
            <tr>
              <th className="px-3 py-2">ID</th>
              <th className="px-3 py-2">等级</th>
              <th className="px-3 py-2">学科</th>
              <th className="px-3 py-2">知识点</th>
              <th className="px-3 py-2">题干</th>
              <th className="px-3 py-2">状态</th>
              <th className="px-3 py-2">来源</th>
              <th className="px-3 py-2 text-right">操作</th>
            </tr>
          </thead>
          <tbody>
            {isLoading && (
              <tr>
                <td colSpan={8} className="px-3 py-6 text-center text-gray-400">加载中…</td>
              </tr>
            )}
            {!isLoading && pageData.length === 0 && (
              <tr>
                <td colSpan={8} className="px-3 py-6 text-center text-gray-400">暂无题目</td>
              </tr>
            )}
            {pageData.map((q) => (
              <tr key={q.id} className="border-t border-gray-100">
                <td className="px-3 py-2 text-gray-400">{q.id}</td>
                <td className="px-3 py-2">L{q.level}</td>
                <td className="px-3 py-2">{q.subject}</td>
                <td className="px-3 py-2 text-gray-500">{q.knowledgePoint || '-'}</td>
                <td className="max-w-xs truncate px-3 py-2">{q.stem}</td>
                <td className="px-3 py-2">
                  <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs">{STATUS_LABEL[q.status] || q.status}</span>
                </td>
                <td className="px-3 py-2 text-gray-500">{q.source}</td>
                <td className="px-3 py-2 text-right">
                  <button className="text-indigo-600 hover:underline" onClick={() => openEdit(q)}>编辑</button>
                  <button
                    className="ml-3 text-rose-500 hover:underline"
                    onClick={() => {
                      if (confirm('确认删除该题？')) deleteMutation.mutate(q.id)
                    }}
                  >
                    删除
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* 分页 */}
        <div className="flex items-center justify-end gap-2 px-3 py-3 text-xs text-gray-500">
          <button
            className="rounded-lg border border-gray-200 px-2 py-1 disabled:opacity-40"
            disabled={page <= 0}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            上一页
          </button>
          <span>第 {page + 1} / {totalPages} 页</span>
          <button
            className="rounded-lg border border-gray-200 px-2 py-1 disabled:opacity-40"
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((p) => p + 1)}
          >
            下一页
          </button>
        </div>
      </div>

      {/* 新增/编辑弹窗 */}
      {modalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-3xl bg-white p-6">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-bold">{editing ? '编辑题目' : '新增题目'}</h2>
              <button className="text-gray-400" onClick={() => setModalOpen(false)}>✕</button>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <label className="text-xs text-gray-500">
                等级
                <select className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.level ?? 1}
                  onChange={(e) => setForm({ ...form, level: Number(e.target.value) })}>
                  {LEVELS.map((l) => <option key={l} value={l}>L{l}</option>)}
                </select>
              </label>
              <label className="text-xs text-gray-500">
                学科
                <select className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.subject}
                  onChange={(e) => setForm({ ...form, subject: e.target.value })}>
                  {SUBJECTS.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </label>
              <label className="text-xs text-gray-500">
                题型
                <input className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value })} />
              </label>
              <label className="text-xs text-gray-500">
                知识点
                <input className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.knowledgePoint || ''}
                  onChange={(e) => setForm({ ...form, knowledgePoint: e.target.value })} />
              </label>
              <label className="text-xs text-gray-500">
                适用场景（竖线分隔）
                <input className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" placeholder="practice|unit_test"
                  value={form.usage || ''} onChange={(e) => setForm({ ...form, usage: e.target.value })} />
              </label>
              <label className="text-xs text-gray-500">
                媒体地址
                <input className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.mediaUrl || ''}
                  onChange={(e) => setForm({ ...form, mediaUrl: e.target.value })} />
              </label>
              <label className="text-xs text-gray-500">
                状态
                <select className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.status}
                  onChange={(e) => setForm({ ...form, status: e.target.value as QuestionStatus })}>
                  {STATUSES.map((s) => <option key={s} value={s}>{STATUS_LABEL[s]}</option>)}
                </select>
              </label>
              <label className="text-xs text-gray-500">
                来源
                <select className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.source}
                  onChange={(e) => setForm({ ...form, source: e.target.value as QuestionSource })}>
                  {SOURCES.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </label>
            </div>

            <label className="mt-3 block text-xs text-gray-500">
              题干
              <textarea className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" rows={3}
                value={form.stem} onChange={(e) => setForm({ ...form, stem: e.target.value })} />
            </label>

            <div className="mt-3">
              <div className="mb-1 flex items-center justify-between text-xs text-gray-500">
                <span>选项（每行一个，存储为 JSON）</span>
                <button className="text-indigo-600" onClick={() => setForm({ ...form, options: [...form.options, ''] })}>
                  + 添加选项
                </button>
              </div>
              {form.options.map((o, i) => (
                <div key={i} className="mb-1 flex items-center gap-2">
                  <input className="block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={o}
                    onChange={(e) => setOpt(i, e.target.value)} />
                  <button className="text-rose-500" onClick={() => setForm({ ...form, options: form.options.filter((_, j) => j !== i) })}>
                    ✕
                  </button>
                </div>
              ))}
            </div>

            <div className="mt-3 grid grid-cols-2 gap-3">
              <label className="text-xs text-gray-500">
                正确答案
                <input className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" value={form.answer}
                  onChange={(e) => setForm({ ...form, answer: e.target.value })} />
              </label>
            </div>

            <label className="mt-3 block text-xs text-gray-500">
              解析
              <textarea className="mt-1 block w-full rounded-xl border border-gray-200 px-2 py-1.5" rows={2}
                value={form.analysis || ''} onChange={(e) => setForm({ ...form, analysis: e.target.value })} />
            </label>

            <div className="mt-5 flex justify-end gap-2">
              <button className="rounded-xl border border-gray-300 px-4 py-2 text-sm" onClick={() => setModalOpen(false)}>
                取消
              </button>
              <button
                className="rounded-xl bg-indigo-600 px-4 py-2 text-sm text-white disabled:opacity-50"
                disabled={saveMutation.isPending || !form.stem.trim()}
                onClick={() => saveMutation.mutate(form)}
              >
                {saveMutation.isPending ? '保存中…' : '保存'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

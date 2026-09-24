import { useMutation, useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { composePaper, listPapers, getPaper } from '../api/assessment'
import type { PaperDTO, PaperSpec } from '../api/types'

const LEVELS = [1, 2, 3, 4, 5, 6]
const SUBJECTS = ['listening', 'speaking', 'reading', 'writing', 'word', 'grammar']

const SUBJECT_LABEL: Record<string, string> = {
  listening: '听力',
  speaking: '口语',
  reading: '阅读',
  writing: '写作',
  word: '词汇',
  grammar: '语法'
}

export default function PaperCompose() {
  const navigate = useNavigate()
  const [level, setLevel] = useState(2)
  const [usage, setUsage] = useState('unit_test')
  const [kpText, setKpText] = useState('')
  const [counts, setCounts] = useState<Record<string, number>>({
    word: 2,
    grammar: 2,
    reading: 1
  })
  const [result, setResult] = useState<PaperDTO | null>(null)
  const [msg, setMsg] = useState('')

  const { data: papers } = useQuery({
    queryKey: ['papers'],
    queryFn: () => listPapers().then((r) => r.data.data)
  })

  const composeMutation = useMutation({
    mutationFn: (spec: PaperSpec) => composePaper(spec),
    onSuccess: (resp) => {
      const d = resp.data.data
      setResult(d)
      setMsg(
        d.shortfalls.length > 0
          ? `已生成试卷「${d.title}」（${d.questions.length} 题），但有缺口：${d.shortfalls.join('、')}`
          : `已生成试卷「${d.title}」，共 ${d.questions.length} 题`
      )
    },
    onError: (e: any) => setMsg('组卷失败：' + (e?.response?.data?.message || e.message))
  })

  const doCompose = () => {
    const items = SUBJECTS.filter((s) => (counts[s] ?? 0) > 0).map((s) => ({
      subject: s,
      count: counts[s]
    }))
    const knowledgePoints = kpText
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean)
    const spec: PaperSpec = { level, usage, items, knowledgePoints }
    composeMutation.mutate(spec)
  }

  const openPaper = async (id: number) => {
    const r = await getPaper(id)
    setResult(r.data.data)
    setMsg('')
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800">组卷</h1>
        <p className="mt-1 text-xs text-gray-400">
          按「等级 + 场景 + 学科配比 + 知识点」从已发布题目中抽题并固化成卷；草稿题不会进入试卷。
        </p>
      </div>

      {/* 组卷条件 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-3 text-sm font-semibold text-gray-600">组卷条件</div>
        <div className="flex flex-wrap items-end gap-3">
          <label className="text-xs text-gray-500">
            等级
            <select
              className="mt-1 block w-24 rounded-xl border border-gray-200 px-2 py-1.5"
              value={level}
              onChange={(e) => setLevel(Number(e.target.value))}
            >
              {LEVELS.map((l) => (
                <option key={l} value={l}>
                  L{l}
                </option>
              ))}
            </select>
          </label>
          <label className="text-xs text-gray-500">
            场景
            <input
              className="mt-1 block w-40 rounded-xl border border-gray-200 px-2 py-1.5"
              value={usage}
              onChange={(e) => setUsage(e.target.value)}
            />
          </label>
          <label className="text-xs text-gray-500">
            知识点（逗号分隔，可留空）
            <input
              className="mt-1 block w-56 rounded-xl border border-gray-200 px-2 py-1.5"
              placeholder="如：一般过去时,名词复数"
              value={kpText}
              onChange={(e) => setKpText(e.target.value)}
            />
          </label>
        </div>

        <div className="mt-4 text-xs text-gray-500">学科配比（填 0 表示不要）</div>
        <div className="mt-2 flex flex-wrap gap-3">
          {SUBJECTS.map((s) => (
            <label key={s} className="text-xs text-gray-500">
              {SUBJECT_LABEL[s]}
              <input
                type="number"
                min={0}
                max={20}
                className="mt-1 block w-20 rounded-xl border border-gray-200 px-2 py-1.5"
                value={counts[s] ?? 0}
                onChange={(e) => setCounts({ ...counts, [s]: Number(e.target.value) })}
              />
            </label>
          ))}
        </div>

        <button
          className="mt-4 rounded-xl bg-violet-600 px-5 py-2 text-sm text-white disabled:opacity-50"
          disabled={composeMutation.isPending}
          onClick={doCompose}
        >
          {composeMutation.isPending ? '组卷中…' : '生成试卷'}
        </button>
      </div>

      {msg && (
        <div className="whitespace-pre-wrap rounded-3xl border border-indigo-200 bg-indigo-50 p-4 text-xs text-indigo-800">
          {msg}
        </div>
      )}

      {/* 本次组卷结果 */}
      {result && (
        <div className="rounded-3xl bg-white p-4 shadow-sm">
          <div className="mb-2 flex items-center justify-between">
            <div className="text-sm font-semibold text-gray-600">
              {result.title}（{result.questions.length} 题）
            </div>
            <div className="flex gap-2">
              <span className="rounded-full bg-gray-100 px-3 py-1 text-[11px] text-gray-500">
                {result.usage}
              </span>
              <button
                className="rounded-xl bg-emerald-600 px-3 py-1 text-xs text-white"
                onClick={() => navigate(`/teacher/assignments?paperId=${result.id}`)}
              >
                布置为作业
              </button>
            </div>
          </div>

          {result.shortfalls.length > 0 && (
            <div className="mb-3 rounded-2xl border border-amber-200 bg-amber-50 p-3 text-xs text-amber-700">
              缺口：{result.shortfalls.join('、')}
            </div>
          )}

          <ol className="space-y-3">
            {result.questions.map((q, i) => (
              <li key={q.id} className="rounded-2xl border border-gray-100 bg-gray-50 p-3">
                <div className="flex items-start gap-2">
                  <span className="mt-0.5 text-xs text-gray-400">{i + 1}.</span>
                  <div className="flex-1">
                    <div className="text-sm text-gray-700">{q.stem}</div>
                    <div className="mt-1 flex flex-wrap gap-2 text-[11px] text-gray-500">
                      {q.options.map((o) => (
                        <span key={o} className="rounded-full bg-white px-2 py-0.5">
                          {o}
                        </span>
                      ))}
                    </div>
                    {q.knowledgePoint && (
                      <div className="mt-1 text-[11px] text-indigo-500">知识点：{q.knowledgePoint}</div>
                    )}
                  </div>
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}

      {/* 历史卷子 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-2 text-sm font-semibold text-gray-600">我组过的卷子</div>
        {!papers?.length ? (
          <div className="text-xs text-gray-400">暂无</div>
        ) : (
          <div className="flex flex-wrap gap-2">
            {papers.map((p) => (
              <button
                key={p.id}
                className="rounded-2xl border border-gray-100 bg-gray-50 px-3 py-2 text-xs text-gray-600"
                onClick={() => openPaper(p.id)}
              >
                #{p.id} {p.title}
                <span className="ml-1 text-[11px] text-gray-400">
                  （{p.questionIds.length} 题）
                </span>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

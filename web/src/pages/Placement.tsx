import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getPlacementQuestions, submitPlacement } from '../api/placement'
import type { PlacementQuestion, PlacementResult } from '../api/types'

const BAND: Record<number, string> = {
  1: '启蒙级 (L1)',
  2: '基础级 (L2)',
  3: '进阶级 (L3)',
  4: '提高级 (L4)',
  5: '熟练级 (L5)',
  6: '精通级 (L6)'
}

export default function Placement() {
  const nav = useNavigate()
  const [selected, setSelected] = useState<Record<string, number>>({})
  const [result, setResult] = useState<PlacementResult | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['placement'],
    queryFn: () => getPlacementQuestions().then((r) => r.data.data)
  })
  const questions: PlacementQuestion[] = data ?? []

  const submit = useMutation({
    mutationFn: () =>
      submitPlacement(
        questions.map((q) => ({ questionId: q.id, selected: selected[q.id] ?? -1 }))
      ),
    onSuccess: (r) => setResult(r.data.data)
  })

  if (result) {
    return (
      <div className="mx-auto max-w-2xl p-6 text-center">
        <div className="rounded-3xl bg-white p-8 shadow-soft">
          <div className="text-5xl">🎉</div>
          <div className="mt-3 text-2xl font-extrabold text-brand-800">
            你的起点级别：{result.initLevel ? BAND[result.initLevel] : ''}
          </div>
          <div className="mt-2 text-sm text-ink-soft">
            得分 {result.score} 分（{result.correct}/{result.total} 题正确）
          </div>
          <p className="mt-4 text-sm text-ink-soft">
            学习地图已按此级别为你解锁起点关卡，开始闯关吧！
          </p>
          <button
            onClick={() => nav('/map')}
            className="mt-6 rounded-full bg-gradient-to-r from-sky-400 to-brand-600 px-6 py-3 font-semibold text-white shadow-soft"
          >
            进入学习地图
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl space-y-4 p-4">
      <div className="rounded-3xl bg-gradient-to-br from-sky-400 via-brand-500 to-brand-700 p-5 text-white shadow-soft">
        <div className="text-2xl font-extrabold">入学测评定级</div>
        <div className="mt-1 text-sm text-accent-200">
          做几道词汇与听力题，为你匹配最合适的起点级别（L1-L6）
        </div>
      </div>

      {isLoading && <div className="py-8 text-center text-sm text-ink-soft">加载中…</div>}

      {questions.map((q, i) => (
        <div key={q.id} className="rounded-3xl bg-white p-5 shadow-soft">
          <div className="flex items-center gap-2 text-xs text-ink-soft">
            <span className="rounded-full bg-brand-100 px-2 py-0.5 font-semibold text-brand-700">
              {q.type === 'VOCAB' ? '词汇' : '听力'}
            </span>
            <span>第 {i + 1} 题 · 目标 L{q.level}</span>
          </div>
          <div className="mt-2 font-bold text-brand-800">{q.prompt}</div>
          {q.audioHint && (
            <div className="mt-1 text-sm text-ink-soft">读音：{q.audioHint}</div>
          )}
          <div className="mt-3 grid gap-2">
            {q.options.map((opt, idx) => {
              const active = selected[q.id] === idx
              return (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setSelected((s) => ({ ...s, [q.id]: idx }))}
                  className={`rounded-2xl border px-4 py-3 text-left transition ${
                    active
                      ? 'border-brand-400 bg-brand-50 font-semibold text-brand-800'
                      : 'border-brand-100 bg-white text-ink-soft hover:border-brand-200'
                  }`}
                >
                  {String.fromCharCode(65 + idx)}. {opt}
                </button>
              )
            })}
          </div>
        </div>
      ))}

      <button
        disabled={submit.isPending || questions.length === 0}
        onClick={() => submit.mutate()}
        className="w-full rounded-full bg-gradient-to-r from-sky-400 to-brand-600 py-3.5 font-semibold text-white shadow-soft disabled:opacity-50"
      >
        {submit.isPending ? '判分中…' : '提交并定级'}
      </button>
    </div>
  )
}

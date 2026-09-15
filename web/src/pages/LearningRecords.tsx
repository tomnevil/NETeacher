import { useState } from 'react'
import Card from '../components/ui/Card'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getModules, listRecords, submitRecord } from '../api/learning'
import { listCourses } from '../api/course'
import type { LearningRecord } from '../api/types'

const MODULE_LABEL: Record<string, string> = {
  word: '单词',
  grammar: '语法',
  speaking: '口语',
  listening: '听力',
  dialogue: '对话'
}

export default function LearningRecords() {
  const qc = useQueryClient()
  const { data: records } = useQuery({
    queryKey: ['learning-records'],
    queryFn: () => listRecords({ page: 0, size: 20 }).then((r) => r.data.data)
  })
  const { data: modules } = useQuery({
    queryKey: ['learning-modules'],
    queryFn: () => getModules().then((r) => r.data.data)
  })
  const { data: courses } = useQuery({
    queryKey: ['courses-all'],
    queryFn: () => listCourses({ size: 100 }).then((r) => r.data.data?.records ?? [])
  })

  const [courseId, setCourseId] = useState<number | ''>('')
  const [module, setModule] = useState('word')
  const [score, setScore] = useState<number>(80)
  const [durationSec, setDurationSec] = useState<number>(300)
  const [detail, setDetail] = useState('')

  const mutation = useMutation({
    mutationFn: submitRecord,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['learning-records'] })
      setDetail('')
    }
  })

  const list: LearningRecord[] = records?.records ?? []

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">我的练习</h1>

      <Card>
        <div className="font-semibold text-brand-800 mb-3">记录一次练习</div>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-3">
          <select
            className="border border-brand-200 rounded-xl px-3 py-2"
            value={courseId}
            onChange={(e) => setCourseId(e.target.value === '' ? '' : Number(e.target.value))}
          >
            <option value="">选择课程</option>
            {courses?.map((c) => (
              <option key={c.id} value={c.id}>
                {c.title}
              </option>
            ))}
          </select>
          <select
            className="border border-brand-200 rounded-xl px-3 py-2"
            value={module}
            onChange={(e) => setModule(e.target.value)}
          >
            {(modules ?? ['word', 'grammar', 'speaking', 'listening', 'dialogue']).map((m) => (
              <option key={m} value={m}>
                {MODULE_LABEL[m] ?? m}
              </option>
            ))}
          </select>
          <label className="flex items-center gap-2 text-sm text-gray-600">
            得分
            <input
              type="number"
              className="border border-brand-200 rounded-xl px-2 py-1 w-20"
              value={score}
              onChange={(e) => setScore(Number(e.target.value))}
            />
          </label>
          <label className="flex items-center gap-2 text-sm text-gray-600">
            时长(秒)
            <input
              type="number"
              className="border border-brand-200 rounded-xl px-2 py-1 w-20"
              value={durationSec}
              onChange={(e) => setDurationSec(Number(e.target.value))}
            />
          </label>
        </div>
        <textarea
          className="w-full border border-brand-200 rounded-xl px-3 py-2"
          placeholder="备注 / 详情（可选 JSON）"
          value={detail}
          onChange={(e) => setDetail(e.target.value)}
        />
        <button
          disabled={mutation.isPending || courseId === ''}
          onClick={() =>
            mutation.mutate({
              courseId: Number(courseId),
              module,
              score,
              durationSec,
              detail: detail || undefined,
              finished: true
            })
          }
          className="mt-3 bg-brand-600 hover:bg-brand-700 text-white rounded-2xl px-5 py-2 disabled:opacity-50"
        >
          {mutation.isPending ? '保存中…' : '保存记录'}
        </button>
      </Card>

      <Card>
        <div className="font-semibold text-brand-800 mb-3">练习历史（{list.length}）</div>
        <div className="space-y-2">
          {list.map((r) => (
            <div key={r.id} className="border border-brand-50 rounded-xl p-3">
              <div className="flex justify-between">
                <span className="font-medium text-brand-800">
                  {MODULE_LABEL[r.module ?? ''] ?? r.module ?? '练习'}
                </span>
                <span className="text-gray-400 text-sm">{r.createdAt?.slice(0, 10)}</span>
              </div>
              <div className="text-sm text-gray-500 mt-1">
                {r.durationSec ?? 0} 秒 · 得分 {r.score ?? '-'}
              </div>
              {r.detail && <div className="text-sm text-gray-500 mt-1">备注：{r.detail}</div>}
            </div>
          ))}
          {list.length === 0 && <div className="text-sm text-gray-400">还没有练习记录。</div>}
        </div>
      </Card>
    </div>
  )
}

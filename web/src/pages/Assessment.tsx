import { useState } from 'react'
import Card from '../components/ui/Card'
import { useQuery } from '@tanstack/react-query'
import { getQuiz, submitQuiz, listAssessments, getAbility } from '../api/assessment'
import type { QuizQuestion, AssessmentResult, DimensionScore } from '../api/types'
import RadarChart from '../components/RadarChart'

const SUBJECTS: { value: string; label: string }[] = [
  { value: '', label: '全部（随机）' },
  { value: 'listening', label: '听力' },
  { value: 'reading', label: '阅读' },
  { value: 'speaking', label: '口语' },
  { value: 'writing', label: '写作' }
]

const LEVELS = [1, 2, 3, 4, 5, 6]

export default function Assessment() {
  const [subject, setSubject] = useState('')
  const [level, setLevel] = useState<number | ''>('')
  const [questions, setQuestions] = useState<QuizQuestion[]>([])
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [active, setActive] = useState<'config' | 'quiz'>('config')
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<AssessmentResult | null>(null)
  const [openWrong, setOpenWrong] = useState<number | null>(null)
  const [historyOpen, setHistoryOpen] = useState(false)

  const { data: history, refetch: refetchHistory } = useQuery({
    queryKey: ['assessments'],
    queryFn: () => listAssessments().then((r) => r.data.data)
  })
  const { data: ability } = useQuery({
    queryKey: ['ability'],
    queryFn: () => getAbility().then((r) => r.data.data)
  })

  const startQuiz = async () => {
    setSubmitting(true)
    try {
      const res = await getQuiz({
        subject: subject || undefined,
        level: level === '' ? undefined : level
      })
      const qs = res.data.data
      if (!qs || qs.length === 0) {
        alert('该学科/等级暂无数目，请换一个条件试试～')
        return
      }
      setQuestions(qs)
      setAnswers({})
      setResult(null)
      setActive('quiz')
    } finally {
      setSubmitting(false)
    }
  }

  const submit = async () => {
    if (questions.some((q) => !answers[q.id])) {
      alert('还有题目没作答哦')
      return
    }
    setSubmitting(true)
    try {
      const res = await submitQuiz({
        subject: questions[0].subject,
        level: questions[0].level,
        type: 'quiz',
        answers: questions.map((q) => ({ questionId: q.id, answer: answers[q.id] }))
      })
      setResult(res.data.data)
      setActive('config')
      refetchHistory()
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">能力测评</h1>

      {active === 'config' && (
        <Card>
          {!result ? (
            <>
              <div className="font-semibold text-brand-800 mb-3">选择测评范围</div>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-4">
                <select
                  className="border border-brand-200 rounded-xl px-3 py-2"
                  value={subject}
                  onChange={(e) => setSubject(e.target.value)}
                >
                  {SUBJECTS.map((s) => (
                    <option key={s.value} value={s.value}>
                      {s.label}
                    </option>
                  ))}
                </select>
                <select
                  className="border border-brand-200 rounded-xl px-3 py-2"
                  value={level}
                  onChange={(e) => setLevel(e.target.value === '' ? '' : Number(e.target.value))}
                >
                  <option value="">全部等级</option>
                  {LEVELS.map((l) => (
                    <option key={l} value={l}>
                      L{l}
                    </option>
                  ))}
                </select>
              </div>
                {ability && ability.length > 0 && (
                  <div className="mb-4 rounded-2xl border border-brand-100 bg-white p-4">
                    <div className="mb-2 text-sm font-semibold text-brand-800">
                      📊 能力雷达图（各维度最新得分）
                    </div>
                    <div className="flex justify-center">
                      <RadarChart
                        data={ability.map((d: DimensionScore) => ({ label: d.label, score: d.score }))}
                      />
                    </div>
                  </div>
                )}
                <button
                  onClick={startQuiz}
                  disabled={submitting}
                  className="bg-brand-600 hover:bg-brand-700 text-white rounded-2xl px-6 py-2.5 disabled:opacity-50"
                >
                  {submitting ? '加载中…' : '开始测评'}
                </button>
            </>
          ) : (
            <ResultView
              result={result}
              openWrong={openWrong}
              setOpenWrong={setOpenWrong}
              onAgain={() => setResult(null)}
              onHistory={() => setHistoryOpen(true)}
            />
          )}
        </Card>
      )}

      {active === 'quiz' && (
        <Card>
          <div className="flex justify-between items-center mb-4">
            <div className="font-semibold text-brand-800">
              {questions.length} 道题 · {questions[0]?.subject} · L{questions[0]?.level}
            </div>
            <button className="text-sm text-gray-400" onClick={() => setActive('config')}>
              退出
            </button>
          </div>
          <div className="space-y-5">
            {questions.map((q, i) => (
              <div key={q.id} className="border-b border-brand-50 pb-4">
                <div className="font-medium text-brand-800 mb-2">
                  {i + 1}. {q.stem}
                </div>
                <div className="space-y-1.5">
                  {q.options.map((opt) => (
                    <label
                      key={opt}
                      className={`flex items-center gap-2 rounded-xl border px-3 py-2 cursor-pointer ${
                        answers[q.id] === opt ? 'border-brand-500 bg-brand-50' : 'border-brand-100'
                      }`}
                    >
                      <input
                        type="radio"
                        name={'q' + q.id}
                        checked={answers[q.id] === opt}
                        onChange={() => setAnswers((a) => ({ ...a, [q.id]: opt }))}
                      />
                      <span>{opt}</span>
                    </label>
                  ))}
                </div>
              </div>
            ))}
          </div>
          <button
            onClick={submit}
            disabled={submitting}
            className="mt-5 bg-accent-500 hover:bg-accent-600 text-white rounded-2xl px-6 py-2.5 disabled:opacity-50"
          >
            {submitting ? '判分中…' : '提交并查看结果'}
          </button>
        </Card>
      )}

      <Card>
        <button
          className="font-semibold text-brand-800 w-full text-left"
          onClick={() => setHistoryOpen((v) => !v)}
        >
          📜 历史测评（{history?.length ?? 0}）{historyOpen ? '▲' : '▼'}
        </button>
        {historyOpen && (
          <div className="mt-3 space-y-3">
            {history && history.length > 0 ? (
              history.map((h) => (
                <div key={h.id} className="border border-brand-50 rounded-xl p-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-brand-700 font-medium">
                      {h.subject} · L{h.level}
                    </span>
                  </div>
                  <div className="text-sm text-gray-600 mt-1">
                    得分 {h.score} · 正确 {h.correctCount}/{h.totalScore}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-sm text-gray-400">还没有测评记录，快去做一次吧～</div>
            )}
          </div>
        )}
      </Card>
    </div>
  )
}

function ResultView({
  result,
  openWrong,
  setOpenWrong,
  onAgain,
  onHistory
}: {
  result: AssessmentResult
  openWrong: number | null
  setOpenWrong: (v: number | null) => void
  onAgain: () => void
  onHistory: () => void
}) {
  return (
    <div className="space-y-4">
      <div className="flex items-end gap-3">
        <div className="text-4xl font-bold text-brand-700">{result.score}</div>
        <div className="text-gray-500">分 · 正确 {result.correctCount}/{result.totalScore}</div>
        <div className="ml-auto text-sm text-brand-600 bg-brand-50 rounded-full px-3 py-1">
          当前等级 L{result.level}
        </div>
      </div>

      <div className="text-sm text-gray-600 bg-brand-50 rounded-2xl p-3">
        <div className="font-medium text-brand-700 mb-1">🤖 AI 学习建议</div>
        {result.comment}
      </div>

      {result.wrongQuestions && result.wrongQuestions.length > 0 && (
        <div>
          <div className="font-medium text-brand-800 mb-2">📕 错题回顾（{result.wrongQuestions.length}）</div>
          <div className="space-y-2">
            {result.wrongQuestions.map((w, i) => (
              <div key={w.questionId} className="border border-brand-50 rounded-xl p-3">
                <button
                  className="w-full text-left text-sm font-medium text-brand-800"
                  onClick={() => setOpenWrong(openWrong === w.questionId ? null : w.questionId)}
                >
                  {i + 1}. {w.stem} {openWrong === w.questionId ? '▲' : '▼'}
                </button>
                {openWrong === w.questionId && (
                  <div className="mt-2 text-sm space-y-1">
                    <div className="text-gray-500">你的答案：{w.userAnswer}</div>
                    <div className="text-brand-700">正确答案：{w.answer}</div>
                    {w.explanation && <div className="text-gray-500">解析：{w.explanation}</div>}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="flex gap-3 pt-1">
        <button
          onClick={onAgain}
          className="bg-brand-600 hover:bg-brand-700 text-white rounded-2xl px-5 py-2"
        >
          再做一次
        </button>
        <button
          onClick={onHistory}
          className="border border-brand-200 text-brand-700 rounded-2xl px-5 py-2"
        >
          查看历史
        </button>
      </div>
    </div>
  )
}

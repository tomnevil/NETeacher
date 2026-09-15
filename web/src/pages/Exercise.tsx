import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Card from '../components/ui/Card'
import { getQuiz, submitQuiz } from '../api/assessment'
import type { QuizQuestion, AssessmentResult } from '../api/types'

const LABEL: Record<string, string> = {
  word: '单词',
  grammar: '语法',
  listening: '听力',
  reading: '阅读',
  speaking: '口语',
  writing: '写作'
}
const LEVELS = [1, 2, 3, 4, 5, 6]

export default function Exercise() {
  const [params] = useSearchParams()
  const subject = (params.get('subject') || 'word').toLowerCase()
  const [level, setLevel] = useState<number>(3)
  const [questions, setQuestions] = useState<QuizQuestion[]>([])
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [active, setActive] = useState<'config' | 'quiz'>('config')
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState<AssessmentResult | null>(null)

  const start = async () => {
    setSubmitting(true)
    try {
      const res = await getQuiz({ subject, level })
      const qs = res.data.data
      if (!qs || qs.length === 0) {
        alert('该专项暂无数目，换个等级试试～')
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
        subject,
        level: questions[0].level,
        type: 'practice',
        answers: questions.map((q) => ({ questionId: q.id, answer: answers[q.id] }))
      })
      setResult(res.data.data)
      setActive('config')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">{LABEL[subject] ?? subject}专项练习</h1>

      {active === 'config' && !result && (
        <Card>
          <div className="font-semibold text-brand-800 mb-3">选择练习等级</div>
          <div className="flex flex-wrap gap-2 mb-4">
            {LEVELS.map((l) => (
              <button
                key={l}
                onClick={() => setLevel(l)}
                className={`rounded-full px-4 py-2 text-sm ${
                  level === l ? 'bg-brand-600 text-white' : 'border border-brand-200 text-brand-700'
                }`}
              >
                L{l}
              </button>
            ))}
          </div>
          <button
            onClick={start}
            disabled={submitting}
            className="bg-brand-600 hover:bg-brand-700 text-white rounded-2xl px-6 py-2.5 disabled:opacity-50"
          >
            {submitting ? '加载中…' : '开始练习'}
          </button>
        </Card>
      )}

      {active === 'quiz' && (
        <Card>
          <div className="flex justify-between items-center mb-4">
            <div className="font-semibold text-brand-800">
              {questions.length} 道题 · {LABEL[subject]} · L{questions[0]?.level}
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

      {result && (
        <Card>
          <div className="space-y-4">
            <div className="flex items-end gap-3">
              <div className="text-4xl font-bold text-brand-700">{result.score}</div>
              <div className="text-gray-500">
                分 · 正确 {result.correctCount}/{result.totalScore}
              </div>
            </div>
            <div className="text-sm text-gray-600 bg-brand-50 rounded-2xl p-3">
              <div className="font-medium text-brand-700 mb-1">🤖 AI 学习建议</div>
              {result.comment}
            </div>
            {result.wrongQuestions && result.wrongQuestions.length > 0 && (
              <div>
                <div className="font-medium text-brand-800 mb-2">
                  📕 错题解析（{result.wrongQuestions.length}）
                </div>
                <div className="space-y-2">
                  {result.wrongQuestions.map((w, i) => (
                    <div key={w.questionId} className="border border-brand-50 rounded-xl p-3">
                      <div className="text-sm font-medium text-brand-800">
                        {i + 1}. {w.stem}
                      </div>
                      <div className="mt-2 text-sm space-y-1">
                        <div className="text-gray-500">你的答案：{w.userAnswer}</div>
                        <div className="text-brand-700">正确答案：{w.answer}</div>
                        {w.explanation && (
                          <div className="text-gray-500">解析：{w.explanation}</div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
            <button
              onClick={() => setResult(null)}
              className="bg-brand-600 hover:bg-brand-700 text-white rounded-2xl px-5 py-2"
            >
              再做一组
            </button>
          </div>
        </Card>
      )}
    </div>
  )
}

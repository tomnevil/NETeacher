import { useState } from 'react'
import Card from '../components/ui/Card'
import { useQuery } from '@tanstack/react-query'
import { getWrongBook } from '../api/assessment'
import type { WrongQuestion } from '../api/types'

function subjectName(s?: string) {
  return (
    { listening: '听力', reading: '阅读', speaking: '口语', writing: '写作' }[s ?? ''] ?? s ?? ''
  )
}

export default function WrongBook() {
  const { data, isLoading } = useQuery({
    queryKey: ['wrong-book'],
    queryFn: () => getWrongBook().then((r) => r.data.data)
  })
  const [openId, setOpenId] = useState<number | null>(null)

  if (isLoading) return <div className="text-gray-400">加载错题本…</div>

  const list: WrongQuestion[] = data ?? []
  const grouped = new Map<string, WrongQuestion[]>()
  for (const w of list) {
    const key = w.subject ?? 'other'
    if (!grouped.has(key)) grouped.set(key, [])
    grouped.get(key)!.push(w)
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">错题本</h1>
      {list.length === 0 ? (
        <Card>
          <div className="text-brand-800 font-semibold mb-1">还没有错题 🎉</div>
          <div className="text-sm text-gray-500">
            去「测评」做一次测试，答错的题目会自动收集到这里。
          </div>
        </Card>
      ) : (
        <>
          <div className="text-sm text-gray-500">共 {list.length} 道错题，按学科分组</div>
          {Array.from(grouped.entries()).map(([subject, items]) => (
            <Card key={subject}>
              <div className="font-semibold text-brand-800 mb-2">
                {subjectName(subject)}（{items.length}）
              </div>
              <div className="space-y-2">
                {items.map((w, i) => (
                  <div key={w.questionId} className="border border-brand-50 rounded-xl p-3">
                    <button
                      className="w-full text-left text-sm font-medium text-brand-800"
                      onClick={() => setOpenId(openId === w.questionId ? null : w.questionId)}
                    >
                      {i + 1}. {w.stem} {openId === w.questionId ? '▲' : '▼'}
                    </button>
                    {openId === w.questionId && (
                      <div className="mt-2 text-sm space-y-1">
                        <div className="text-gray-500">你的答案：{w.userAnswer}</div>
                        <div className="text-brand-700">正确答案：{w.answer}</div>
                        {w.options && w.options.length > 0 && (
                          <div className="text-gray-400">
                            选项：{w.options.join(' / ')}
                          </div>
                        )}
                        {w.explanation && (
                          <div className="text-gray-500">解析：{w.explanation}</div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </Card>
          ))}
        </>
      )}
    </div>
  )
}

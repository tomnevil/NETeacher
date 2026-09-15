import { useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import Card from '../components/ui/Card'
import { useQuery } from '@tanstack/react-query'
import { getPath, getExplain } from '../api/recommend'
import type { RecommendPath } from '../api/types'

export default function LearningPath() {
  const [params] = useSearchParams()
  const focusCourseId = Number(params.get('courseId')) || 0
  const focusRef = useRef<HTMLDivElement>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['recommend-path'],
    queryFn: () => getPath().then((r) => r.data.data)
  })
  const { data: explain } = useQuery({
    queryKey: ['recommend-explain'],
    queryFn: () => getExplain().then((r) => r.data.data)
  })

  useEffect(() => {
    if (focusCourseId && data) {
      focusRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, [focusCourseId, data])

  if (isLoading) {
    return <div className="text-gray-400">加载自适应路径…</div>
  }

  if (!data) {
    return (
      <Card>
        <div className="text-brand-800 font-semibold mb-2">还没有学习路径</div>
        <div className="text-sm text-gray-500">
          先去「能力测评」做一次测试，系统会基于成绩为你生成个性化学习路径。
        </div>
      </Card>
    )
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">自适应学习路径</h1>

      <Card>
        <div className="flex items-center gap-3 mb-3">
          <div className="text-lg font-bold text-brand-700">
            当前 L{data.currentLevel} → 目标 L{data.nextLevel}
          </div>
          {explain && (
            <div className="ml-auto text-sm text-accent-500">🤖 {explain}</div>
          )}
        </div>

        {data.mastery && Object.keys(data.mastery).length > 0 && (
          <div className="mb-4">
            <div className="text-sm text-gray-500 mb-2">各科学情</div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              {Object.entries(data.mastery).map(([k, v]) => (
                <div key={k} className="bg-brand-50 rounded-2xl p-3 text-center">
                  <div className="text-2xl font-bold text-brand-700">{v}</div>
                  <div className="text-xs text-gray-500">{subjectName(k)}</div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="font-semibold text-brand-800 mb-2">为你推荐</div>
        <div className="space-y-2">
          {data.items.map((it, i) => {
            const focused = focusCourseId && it.courseId === focusCourseId
            return (
              <div
                key={i}
                ref={focused ? focusRef : undefined}
                className={`flex items-center gap-3 border rounded-2xl p-3 transition ${
                  focused
                    ? 'border-brand-500 bg-brand-50 ring-2 ring-brand-300'
                    : 'border-brand-50 hover:border-brand-300'
                }`}
              >
                <div className="w-10 h-10 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center font-bold">
                  {it.level}
                </div>
                <div className="flex-1">
                  <div className="font-medium text-brand-800">
                    {it.title}
                    {focused && (
                      <span className="ml-2 text-xs font-normal text-brand-600 bg-brand-100 px-2 py-0.5 rounded-full">
                        来自对话强化
                      </span>
                    )}
                  </div>
                  <div className="text-sm text-gray-500">{it.reason}</div>
                </div>
                <div className="text-xs text-gray-400">{categoryName(it.category)}</div>
              </div>
            )
          })}
          {data.items.length === 0 && (
            <div className="text-sm text-gray-400">暂无推荐，完成更多测评后会自动更新。</div>
          )}
        </div>
      </Card>
    </div>
  )
}

function subjectName(s: string) {
  return (
    { listening: '听力', reading: '阅读', speaking: '口语', writing: '写作' }[s] ?? s
  )
}

function categoryName(c: string | null) {
  if (!c) return ''
  return (
    { MAJOR: '主修', EXTENSION: '拓展', SPECIAL: '专项' }[c] ?? c
  )
}

import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { listTopics, listCourses } from '../api/course'
import type { TopicInfo } from '../api/types'

const TOPIC_ICON: Record<string, string> = {
  WORD: '📚',
  SPEAKING: '🎤',
  LISTENING: '🎧',
  READING: '📖',
  GRAMMAR: '✏️'
}

const TOPIC_COLOR: Record<string, string> = {
  WORD: 'from-sky-400 to-brand-600',
  SPEAKING: 'from-accent-400 to-accent-600',
  LISTENING: 'from-violet-400 to-purple-600',
  READING: 'from-emerald-400 to-teal-600',
  GRAMMAR: 'from-rose-400 to-pink-600'
}

export default function SpecialTraining() {
  const nav = useNavigate()
  const [picked, setPicked] = useState<string>('')

  const { data: topics } = useQuery({
    queryKey: ['topics'],
    queryFn: () => listTopics().then((r) => r.data.data)
  })

  const { data: courses, isLoading } = useQuery({
    queryKey: ['courses-by-topic', picked],
    queryFn: () =>
      listCourses({ category: 'SPECIAL', topic: picked || undefined, size: 50 }).then(
        (r) => r.data.data.records
      )
  })

  return (
    <div className="-mx-4 -mt-4 space-y-5 pb-6 md:-mx-8 md:-mt-8">
      <header className="relative overflow-hidden bg-gradient-to-br from-violet-500 via-brand-600 to-brand-700 px-5 pb-6 pt-7 text-white md:px-10">
        <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rounded-full bg-white/10" />
        <div className="relative flex items-center gap-4">
          <img
            src="/assets/mascot.png"
            alt="向导"
            className="h-16 w-16 rounded-2xl bg-white/20 p-1 shadow-soft animate-float"
          />
          <div>
            <div className="text-3xl font-extrabold tracking-wide">专项练习</div>
            <div className="mt-1 inline-block rounded-full bg-white/20 px-3 py-0.5 text-xs text-white/90">
              单词 · 口语 · 听力 · 阅读 · 语法，逐个突破 💪
            </div>
          </div>
        </div>
      </header>

      {/* 专题卡片 */}
      <div className="px-4 md:px-10">
        <div className="mx-auto flex max-w-5xl flex-wrap gap-3">
          <button
            onClick={() => setPicked('')}
            className={`rounded-2xl px-4 py-2 text-sm font-semibold shadow-soft transition active:scale-95 ${
              !picked
                ? 'bg-gradient-to-r from-sky-400 to-brand-600 text-white'
                : 'bg-white text-brand-700 hover:shadow-lg'
            }`}
          >
            全部专项
          </button>
          {topics?.map((t: TopicInfo) => (
            <button
              key={t.topic}
              onClick={() => setPicked(t.topic)}
              className={`flex items-center gap-2 rounded-2xl px-4 py-2 text-sm font-semibold shadow-soft transition active:scale-95 ${
                picked === t.topic
                  ? `bg-gradient-to-r ${TOPIC_COLOR[t.topic] ?? 'from-sky-400 to-brand-600'} text-white`
                  : 'bg-white text-brand-700 hover:shadow-lg'
              }`}
            >
              <span className="text-lg">{TOPIC_ICON[t.topic] ?? '⭐'}</span>
              {t.label}
              <span className="rounded-full bg-black/10 px-1.5 text-xs">{t.count}</span>
            </button>
          ))}
        </div>
      </div>

      {/* 课程列表 */}
      <div className="px-4 md:px-10">
        <div className="mx-auto grid max-w-5xl gap-3 sm:grid-cols-2">
          {courses?.map((c) => (
            <button
              key={c.id}
              onClick={() => nav(`/speaking?courseId=${c.id}`)}
              className="flex items-center gap-4 rounded-3xl border border-brand-100 bg-white p-4 text-left shadow-soft transition hover:shadow-lg active:scale-[0.99]"
            >
              <span
                className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br text-2xl text-white shadow ${
                  TOPIC_COLOR[c.topic ?? 'WORD'] ?? 'from-sky-400 to-brand-600'
                }`}
              >
                {TOPIC_ICON[c.topic ?? 'WORD'] ?? '⭐'}
              </span>
              <div className="min-w-0 flex-1">
                <div className="truncate font-bold text-brand-800">{c.title}</div>
                <div className="mt-0.5 line-clamp-1 text-xs text-ink-soft">
                  {c.description}
                </div>
                <div className="mt-1 flex items-center gap-2 text-xs text-brand-500">
                  <span>L{c.level}</span>
                  <span>·</span>
                  <span>{c.lessonCount} 课时</span>
                </div>
              </div>
              <span className="shrink-0 text-brand-400">›</span>
            </button>
          ))}
          {courses && courses.length === 0 && !isLoading && (
            <div className="col-span-full py-10 text-center text-sm text-ink-soft">
              该专题暂无课程
            </div>
          )}
          {isLoading && (
            <div className="col-span-full py-10 text-center text-sm text-ink-soft">加载中…</div>
          )}
        </div>
      </div>
    </div>
  )
}

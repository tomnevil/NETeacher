import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getLevels, getForbiddenHours } from '../api/progress'
import type { LevelInfo } from '../api/types'

/** 每个关卡的装饰 emoji（呼应概念图氛围） */
const DECOR = ['🍎', '🌈', '🎒', '🚀', '💡', '🏆']

/** 蜿蜒上升路径的关键点（从下往上：L1 在底部，L6 在山顶） */
const PATH_POINTS = [
  { x: 50, y: 92 },
  { x: 30, y: 76 },
  { x: 62, y: 60 },
  { x: 34, y: 44 },
  { x: 60, y: 28 },
  { x: 44, y: 12 }
]
const PATH_D =
  'M 50 92 C 50 85 30 82 30 76 C 30 70 62 66 62 60 C 62 54 34 50 34 44 C 34 38 60 34 60 28 C 60 22 44 18 44 12'

/** 关卡完成百分比（0-100） */
function lvPercent(lv: LevelInfo): number {
  return lv.courseCount ? Math.round((lv.completedCount / lv.courseCount) * 100) : 0
}

function Stars({ n, size = 16 }: { n: number; size?: number }) {
  return (
    <span className="inline-flex gap-0.5 text-accent-500" style={{ fontSize: size }}>
      {[1, 2, 3].map((i) => (
        <span
          key={i}
          className={i <= n ? 'animate-twinkle' : 'opacity-20'}
          style={{ animationDelay: `${i * 0.2}s` }}
        >
          ★
        </span>
      ))}
    </span>
  )
}

export default function LearningMap() {
  const nav = useNavigate()
  const [picked, setPicked] = useState<string | null>(null)
  const [drawn, setDrawn] = useState(false)

  const { data, isLoading } = useQuery({
    queryKey: ['levels'],
    queryFn: () => getLevels().then((r) => r.data.data)
  })

  const { data: fh } = useQuery({
    queryKey: ['forbidden-hours'],
    queryFn: () => getForbiddenHours().then((r) => r.data.data)
  })

  const levels = data ?? []
  const unlockedCount = levels.filter((l) => l.unlocked).length
  const totalStars = levels.reduce((s, l) => s + l.stars, 0)
  const currentIndex = Math.max(
    0,
    levels.findIndex((l) => l.current) === -1
      ? unlockedCount - 1
      : levels.findIndex((l) => l.current)
  )
  const active = levels.find((l) => l.lv === picked) ?? levels[currentIndex] ?? levels[0]

  // 上升进度（路径填充到当前关卡）
  const progress =
    PATH_POINTS.length > 1
      ? ((PATH_POINTS[0].y - PATH_POINTS[currentIndex].y) /
          (PATH_POINTS[0].y - PATH_POINTS[PATH_POINTS.length - 1].y)) *
        100
      : 0

  useEffect(() => {
    const t = setTimeout(() => setDrawn(true), 80)
    return () => clearTimeout(t)
  }, [])

  const go = (lv: LevelInfo) => {
    setPicked(lv.lv)
    if (!lv.unlocked) return
    nav(lv.firstCourseId ? `/speaking?courseId=${lv.firstCourseId}` : '/speaking')
  }

  return (
    <div className="-mx-4 -mt-4 space-y-5 pb-6 md:-mx-8 md:-mt-8">
      {/* 通栏头部 */}
      <header className="relative overflow-hidden bg-gradient-to-br from-sky-400 via-brand-500 to-brand-700 px-5 pb-6 pt-7 text-white md:px-10">
        <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rounded-full bg-white/10" />
        <div className="pointer-events-none absolute right-24 top-16 h-20 w-20 rounded-full bg-white/10" />
        <div className="relative flex items-center gap-4">
          <img
            src="/assets/mascot.png"
            alt="向导"
            className="h-16 w-16 rounded-2xl bg-white/20 p-1 shadow-soft animate-float"
          />
          <div>
            <div className="text-3xl font-extrabold tracking-wide">学习地图</div>
            <div className="mt-1 inline-block rounded-full bg-white/20 px-3 py-0.5 text-xs text-accent-200">
              从山脚出发，一路闯到山顶 ✨
            </div>
          </div>
        </div>
        <div className="relative mt-5 flex flex-wrap items-center gap-3">
          <div className="rounded-2xl bg-white/15 px-4 py-2 backdrop-blur">
            <div className="text-xs text-white/70">已解锁</div>
            <div className="text-lg font-bold">
              {unlockedCount}/{levels.length || 6}
            </div>
          </div>
          <div className="rounded-2xl bg-white/15 px-4 py-2 backdrop-blur">
            <div className="text-xs text-white/70">收集星星</div>
            <div className="text-lg font-bold">★ {totalStars}</div>
          </div>
          <div className="min-w-[140px] flex-1 rounded-2xl bg-white/15 px-4 py-2 backdrop-blur">
            <div className="flex items-center justify-between text-xs text-white/70">
              <span>攀登高度</span>
              <span>{Math.round(progress)}%</span>
            </div>
            <div className="mt-1 h-2 w-full overflow-hidden rounded-full bg-white/25">
              <div
                className="h-full rounded-full bg-accent-300 transition-all duration-700"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        </div>
      </header>

      {/* 入学定级引导 */}
      {levels.length > 0 && levels[0]?.initLevel == null && (
        <div className="px-4 md:px-10">
          <div className="mx-auto max-w-3xl rounded-3xl border border-dashed border-brand-300 bg-brand-50 p-4 text-center">
            <div className="text-sm font-bold text-brand-800">尚未完成入学测评定级</div>
            <div className="mt-1 text-xs text-ink-soft">
              完成几道词汇 / 听力题，为你匹配更准确的起点级别并解锁对应关卡。
            </div>
            <button
              onClick={() => nav('/placement')}
              className="mt-3 rounded-full bg-gradient-to-r from-sky-400 to-brand-600 px-5 py-2 text-sm font-semibold text-white shadow-soft"
            >
              去定级
            </button>
          </div>
        </div>
      )}

      {/* 防沉迷提示 */}
      {fh && (
        <div className="px-4 md:px-10">
          <div className="mx-auto flex max-w-3xl items-center gap-3 rounded-3xl border border-amber-200 bg-amber-50 p-4">
            <span className="text-2xl">🌙</span>
            <div className="min-w-0 flex-1">
              <div className="text-sm font-bold text-amber-800">健康护眼时段</div>
              <div className="mt-0.5 text-xs text-amber-700">
                每日 {fh.forbiddenStart}:00 至次日 {fh.forbiddenEnd}:00 为休息时段，系统将暂停练习以守护视力与作息。
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 上升式动态地图 */}
      <div className="relative px-4 md:px-10">
        <div className="relative mx-auto max-w-3xl overflow-hidden rounded-3xl bg-gradient-to-b from-sky-50 via-brand-50/60 to-white shadow-soft">
          {/* 装饰云朵 / 星星 */}
          <span className="pointer-events-none absolute left-6 top-10 text-2xl animate-float opacity-70">
            ☁️
          </span>
          <span
            className="pointer-events-none absolute right-8 top-24 text-2xl animate-float opacity-70"
            style={{ animationDelay: '0.6s' }}
          >
            ☁️
          </span>
          <span className="pointer-events-none absolute right-12 top-6 text-lg animate-twinkle opacity-80">
            ✨
          </span>
          <span
            className="pointer-events-none absolute left-10 top-40 text-lg animate-twinkle opacity-80"
            style={{ animationDelay: '0.9s' }}
          >
            ⭐
          </span>

          <div className="relative aspect-[4/5] w-full">
            <svg
              className="absolute inset-0 h-full w-full"
              viewBox="0 0 100 100"
              preserveAspectRatio="none"
            >
              {/* 路径底色（虚线流动） */}
              <path
                d={PATH_D}
                fill="none"
                stroke="#dbeafe"
                strokeWidth="3"
                strokeLinecap="round"
                strokeDasharray="3 3"
                className="animate-dash-flow"
                vectorEffect="non-scaling-stroke"
              />
              {/* 进度路径（从下往上绘制） */}
              <path
                d={PATH_D}
                fill="none"
                stroke="url(#mapGrad)"
                strokeWidth="3.4"
                strokeLinecap="round"
                pathLength={100}
                style={{
                  strokeDasharray: 100,
                  strokeDashoffset: drawn ? 100 - progress : 100,
                  transition: 'stroke-dashoffset 1.6s ease-out'
                }}
                vectorEffect="non-scaling-stroke"
              />
              <defs>
                <linearGradient id="mapGrad" x1="0" y1="1" x2="0" y2="0">
                  <stop offset="0%" stopColor="#38bdf8" />
                  <stop offset="100%" stopColor="#2563eb" />
                </linearGradient>
              </defs>
            </svg>

            {/* 节点 */}
            {levels.map((lv, i) => {
              const p = PATH_POINTS[i]
              if (!p) return null
              const isCurrent = lv.current
              const isDone = i < currentIndex
              return (
                <div
                  key={lv.lv}
                  className="absolute -translate-x-1/2 -translate-y-1/2 animate-pop-in"
                  style={{ left: `${p.x}%`, top: `${p.y}%`, animationDelay: `${i * 0.12}s` }}
                >
                  {isCurrent && lv.unlocked && (
                    <>
                      <span className="absolute inset-0 rounded-full bg-accent-400/60 animate-pulse-ring" />
                      <span
                        className="absolute inset-0 rounded-full bg-accent-400/40 animate-pulse-ring"
                        style={{ animationDelay: '0.8s' }}
                      />
                    </>
                  )}
                  <button
                    type="button"
                    onClick={() => go(lv)}
                    aria-label={`${lv.lv} ${lv.name}`}
                    className={`relative flex h-14 w-14 items-center justify-center rounded-full border-4 border-white shadow-soft transition-transform duration-200 active:scale-95 sm:h-16 sm:w-16 ${
                      lv.unlocked ? 'cursor-pointer hover:scale-110' : 'cursor-not-allowed'
                    } ${
                      lv.unlocked
                        ? isCurrent
                          ? 'bg-gradient-to-br from-accent-400 to-accent-500 text-white'
                          : 'bg-gradient-to-br from-sky-400 to-brand-600 text-white'
                        : 'bg-gray-200 text-gray-400'
                    }`}
                  >
                    {lv.unlocked ? (
                      <span className="text-base font-extrabold leading-none sm:text-lg">
                        {lv.lv}
                      </span>
                    ) : (
                      <span className="text-xl leading-none sm:text-2xl">🔒</span>
                    )}
                  </button>
                  {/* 星标 */}
                  {lv.unlocked && lv.stars > 0 && (
                    <span className="absolute -bottom-1.5 left-1/2 -translate-x-1/2 whitespace-nowrap rounded-full bg-white px-1.5 py-0.5 text-[9px] font-bold text-accent-600 shadow">
                      {'★'.repeat(lv.stars)}
                    </span>
                  )}
                  {/* 关卡名 */}
                  <div
                    className={`absolute left-1/2 top-full mt-1 w-20 -translate-x-1/2 text-center text-[11px] font-bold leading-tight ${
                      lv.unlocked ? 'text-brand-800' : 'text-gray-400'
                    }`}
                  >
                    {lv.name}
                  </div>
                </div>
              )
            })}

            {/* 向导站在当前关卡 */}
            {levels[currentIndex] && (
              <img
                src="/assets/mascot.png"
                alt="向导"
                className="absolute h-10 w-10 -translate-x-1/2 -translate-y-[140%] drop-shadow animate-bounce-slight"
                style={{
                  left: `${PATH_POINTS[currentIndex].x}%`,
                  top: `${PATH_POINTS[currentIndex].y}%`
                }}
              />
            )}
          </div>
        </div>
      </div>

      {/* 直线关卡列表（详情 + 入口） */}
      <div className="px-4 md:px-10">
        <div className="mx-auto max-w-3xl">
          <div className="mb-2 text-sm font-bold text-brand-800">关卡列表</div>
          <div className="flex flex-col gap-3">
            {levels.map((lv, i) => {
              const isActive = active?.lv === lv.lv
              return (
                <div
                  key={lv.lv}
                  className={`flex items-center gap-3 rounded-3xl border bg-white p-4 shadow-soft transition ${
                    lv.current ? 'border-accent-200 ring-2 ring-accent-100' : 'border-brand-100'
                  } ${i % 2 === 0 ? 'md:flex-row' : 'md:flex-row-reverse'}`}
                >
                  <button
                    type="button"
                    onClick={() => go(lv)}
                    aria-label={lv.lv}
                    className={`flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl text-base font-extrabold text-white shadow-soft transition active:scale-95 ${
                      lv.unlocked
                        ? lv.current
                          ? 'bg-gradient-to-br from-accent-400 to-accent-500'
                          : 'bg-gradient-to-br from-sky-400 to-brand-600'
                        : 'bg-gray-200 text-gray-400'
                    }`}
                  >
                    {lv.unlocked ? lv.lv : '🔒'}
                  </button>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center justify-between gap-2">
                      <div className="truncate font-bold text-brand-800">
                        {lv.name}
                        {lv.current && (
                          <span className="ml-2 rounded-full bg-accent-100 px-2 py-0.5 text-xs font-normal text-accent-600">
                            当前
                          </span>
                        )}
                      </div>
                      {lv.unlocked && <Stars n={lv.stars} size={15} />}
                    </div>
                    <div className="mt-1 text-xs text-ink-soft">
                      课程 {lv.completedCount}/{lv.courseCount}
                      {lv.firstCourseTitle && ` · 下一课：${lv.firstCourseTitle}`}
                      {!lv.unlocked && ' · 未解锁'}
                    </div>
                    <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-brand-100">
                      <div
                        className="h-full rounded-full bg-gradient-to-r from-sky-400 to-brand-500 transition-all duration-500"
                        style={{ width: lvPercent(lv) + '%' }}
                      />
                    </div>
                  </div>
                  <button
                    disabled={!lv.unlocked}
                    onClick={() => go(lv)}
                    className={`shrink-0 rounded-full px-4 py-2 text-sm font-semibold transition-all active:scale-95 ${
                      lv.unlocked
                        ? 'bg-gradient-to-r from-sky-400 to-brand-600 text-white shadow-soft hover:shadow-lg'
                        : 'cursor-not-allowed bg-gray-100 text-gray-400'
                    }`}
                  >
                    {lv.unlocked ? '去闯关' : '未解锁'}
                  </button>
                </div>
              )
            })}
            {isLoading && (
              <div className="py-8 text-center text-sm text-ink-soft">加载中…</div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

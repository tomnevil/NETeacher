import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { myClasses } from '../api/teacher'
import type { StudentProgress } from '../api/types'

function LevelBadge({ lv }: { lv: number }) {
  return (
    <span className="inline-flex h-7 w-7 items-center justify-center rounded-lg bg-gradient-to-br from-sky-400 to-brand-600 text-xs font-bold text-white">
      L{lv}
    </span>
  )
}

function StatBar({ value, max = 100, color }: { value: number; max?: number; color: string }) {
  const pct = Math.min(100, Math.round((value / max) * 100))
  return (
    <div className="h-2 w-full overflow-hidden rounded-full bg-brand-100">
      <div
        className="h-full rounded-full transition-all duration-500"
        style={{ width: pct + '%', background: color }}
      />
    </div>
  )
}

export default function TeacherDashboard() {
  const [activeId, setActiveId] = useState<number | null>(null)

  const { data, isLoading, isError } = useQuery({
    queryKey: ['teacher-classes'],
    queryFn: () => myClasses().then((r) => r.data.data)
  })

  const classes = data ?? []
  const active = classes.find((c) => c.classId === activeId) ?? classes[0]

  return (
    <div className="-mx-4 -mt-4 space-y-5 pb-6 md:-mx-8 md:-mt-8">
      <header className="relative overflow-hidden bg-gradient-to-br from-emerald-500 via-teal-600 to-brand-700 px-5 pb-6 pt-7 text-white md:px-10">
        <div className="pointer-events-none absolute -right-10 -top-10 h-40 w-40 rounded-full bg-white/10" />
        <div className="relative flex items-center gap-4">
          <img
            src="/assets/mascot.png"
            alt="老师"
            className="h-16 w-16 rounded-2xl bg-white/20 p-1 shadow-soft animate-float"
          />
          <div>
            <div className="text-3xl font-extrabold tracking-wide">班级学情</div>
            <div className="mt-1 inline-block rounded-full bg-white/20 px-3 py-0.5 text-xs text-white/90">
              一眼看清每个孩子的进度与薄弱点 📊
            </div>
          </div>
        </div>
      </header>

      {isLoading && <div className="py-10 text-center text-sm text-ink-soft">加载中…</div>}

      {isError && (
        <div className="mx-4 py-10 text-center text-sm text-ink-soft md:mx-10">
          暂无权限或加载失败（请使用教师账号 13700000000 / 123456）
        </div>
      )}

      {!isLoading && !isError && classes.length === 0 && (
        <div className="mx-4 py-10 text-center text-sm text-ink-soft md:mx-10">
          还没有关联班级，请联系管理员绑定。
        </div>
      )}

      {classes.length > 0 && (
        <>
          <div className="px-4 md:px-10">
            <div className="mx-auto flex max-w-5xl flex-wrap gap-3">
              {classes.map((c) => (
                <button
                  key={c.classId}
                  onClick={() => setActiveId(c.classId)}
                  className={`rounded-2xl px-4 py-2 text-sm font-semibold shadow-soft transition active:scale-95 ${
                    active?.classId === c.classId
                      ? 'bg-gradient-to-r from-emerald-400 to-teal-600 text-white'
                      : 'bg-white text-brand-700 hover:shadow-lg'
                  }`}
                >
                  {c.className} · {c.studentCount}人
                </button>
              ))}
            </div>
          </div>

          {active && (
            <div className="px-4 md:px-10">
              <div className="mx-auto grid max-w-5xl gap-3 sm:grid-cols-4">
                <div className="rounded-3xl bg-white p-4 shadow-soft">
                  <div className="text-xs text-ink-soft">学生数</div>
                  <div className="text-2xl font-bold text-brand-800">{active.studentCount}</div>
                </div>
                <div className="rounded-3xl bg-white p-4 shadow-soft">
                  <div className="text-xs text-ink-soft">平均口语</div>
                  <div className="text-2xl font-bold text-accent-600">
                    {Math.round(active.avgSpeaking)}
                  </div>
                </div>
                <div className="rounded-3xl bg-white p-4 shadow-soft">
                  <div className="text-xs text-ink-soft">平均时长</div>
                  <div className="text-2xl font-bold text-brand-800">
                    {Math.round(active.avgMinutes)}
                    <span className="text-sm font-normal text-ink-soft"> 分</span>
                  </div>
                </div>
                <div className="rounded-3xl bg-white p-4 shadow-soft">
                  <div className="text-xs text-ink-soft">今日打卡</div>
                  <div className="text-2xl font-bold text-emerald-600">
                    {active.checkedTodayCount}
                    <span className="text-sm font-normal text-ink-soft">
                      /{active.studentCount}
                    </span>
                  </div>
                </div>
              </div>

              {active.weakTopics.length > 0 && (
                <div className="mx-auto mt-3 max-w-5xl rounded-3xl border border-accent-200 bg-accent-50/50 p-4">
                  <div className="text-sm font-bold text-accent-600">班级共性薄弱点（按学科）</div>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {active.weakTopics.map((w) => (
                      <span
                        key={w}
                        className="rounded-full bg-accent-100 px-3 py-1 text-sm text-accent-700"
                      >
                        {w}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {!!active.weakKnowledgePoints?.length && (
                <div className="mx-auto mt-3 max-w-5xl rounded-3xl border border-rose-200 bg-rose-50/50 p-4">
                  <div className="text-sm font-bold text-rose-600">
                    班级共性薄弱知识点（更细粒度）
                  </div>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {active.weakKnowledgePoints!.map((w) => (
                      <span
                        key={w}
                        className="rounded-full bg-rose-100 px-3 py-1 text-sm text-rose-700"
                      >
                        {w}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {active && (
            <div className="px-4 md:px-10">
              <div className="mx-auto max-w-5xl space-y-3">
                <div className="text-sm font-bold text-brand-800">学生明细</div>
                {active.students.map((s: StudentProgress) => (
                  <div
                    key={s.studentId}
                    className="rounded-3xl border border-brand-100 bg-white p-4 shadow-soft"
                  >
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-sky-400 to-brand-600 font-bold text-white">
                        {s.nickname?.charAt(0) ?? '生'}
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-center gap-2">
                          <span className="truncate font-bold text-brand-800">{s.nickname}</span>
                          <LevelBadge lv={s.overallLevel} />
                          {s.checkedToday && (
                            <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs text-emerald-600">
                              今日已练
                            </span>
                          )}
                        </div>
                        <div className="mt-0.5 text-xs text-ink-soft">
                          {s.phone} · 连续 {s.streakDays} 天 · 完成 {s.completedCourses} 课
                        </div>
                      </div>
                      <div className="hidden shrink-0 text-right sm:block">
                        <div className="text-xs text-ink-soft">口语</div>
                        <div className="font-bold text-accent-600">
                          {Math.round(s.speakingAvg)}
                        </div>
                      </div>
                    </div>

                    <div className="mt-3 grid gap-3 sm:grid-cols-2">
                      <div>
                        <div className="mb-1 flex justify-between text-xs text-ink-soft">
                          <span>口语水平</span>
                          <span>{Math.round(s.speakingAvg)}</span>
                        </div>
                        <StatBar value={s.speakingAvg} color="#f59e0b" />
                      </div>
                      <div>
                        <div className="mb-1 flex justify-between text-xs text-ink-soft">
                          <span>学习时长（分）</span>
                          <span>{s.totalMinutes}</span>
                        </div>
                        <StatBar value={s.totalMinutes} max={300} color="#3b82f6" />
                      </div>
                    </div>

                    {s.weakSubjects.length > 0 && (
                      <div className="mt-3 flex flex-wrap items-center gap-2">
                        <span className="text-xs text-ink-soft">薄弱：</span>
                        {s.weakSubjects.map((w) => (
                          <span
                            key={w}
                            className="rounded-full bg-rose-50 px-2 py-0.5 text-xs text-rose-600"
                          >
                            {w}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
                {active.students.length === 0 && (
                  <div className="py-8 text-center text-sm text-ink-soft">该班级暂无学生</div>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}

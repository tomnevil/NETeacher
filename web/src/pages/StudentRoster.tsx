import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { listClasses, classStudents } from '../api/org'
import type { OrgClass } from '../api/types'

/**
 * 学生名单：按班级查看学生，含年级/联系方式，便于老师点名与管理。
 */
export default function StudentRoster() {
  const [activeId, setActiveId] = useState<number | null>(null)

  const { data: classes } = useQuery({
    queryKey: ['org-classes'],
    queryFn: () => listClasses().then((r) => r.data.data)
  })

  const active: OrgClass | undefined =
    classes?.find((c) => c.id === activeId) ?? classes?.[0]

  const { data: students, isLoading } = useQuery({
    queryKey: ['roster-students', active?.id],
    queryFn: () => classStudents(active!.id).then((r) => r.data.data),
    enabled: !!active
  })

  return (
    <div className="space-y-4 pb-6">
      <h1 className="text-2xl font-bold text-brand-800">学生名单</h1>

      <div className="flex flex-wrap gap-2">
        {classes?.map((c) => (
          <button
            key={c.id}
            onClick={() => setActiveId(c.id)}
            className={`rounded-2xl px-4 py-2 text-sm font-semibold shadow-soft transition active:scale-95 ${
              active?.id === c.id
                ? 'bg-gradient-to-r from-sky-400 to-brand-600 text-white'
                : 'bg-white text-brand-700 hover:shadow-lg'
            }`}
          >
            {c.name} · {c.studentCount}人
          </button>
        ))}
      </div>

      {active && (
        <div className="rounded-3xl border border-brand-100 bg-white p-4 shadow-soft">
          <div className="mb-3 flex items-center justify-between">
            <div className="font-bold text-brand-800">
              {active.schoolName} · {active.name}
            </div>
            <div className="text-xs text-ink-soft">
              年级 {active.grade} · 班主任 {active.headTeacherName ?? '—'}
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full min-w-[420px] text-left text-sm">
              <thead>
                <tr className="text-xs text-ink-soft">
                  <th className="px-3 py-2">#</th>
                  <th className="px-3 py-2">姓名</th>
                  <th className="px-3 py-2">手机号</th>
                  <th className="px-3 py-2">年级</th>
                </tr>
              </thead>
              <tbody>
                {students?.map((s, i) => (
                  <tr key={s.uid} className="border-t border-brand-50">
                    <td className="px-3 py-2 text-ink-soft">{i + 1}</td>
                    <td className="px-3 py-2 font-semibold text-brand-800">{s.nickname}</td>
                    <td className="px-3 py-2 text-ink-soft">{s.phone}</td>
                    <td className="px-3 py-2 text-ink-soft">{s.grade}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {isLoading && <div className="py-6 text-center text-sm text-ink-soft">加载中…</div>}
            {!isLoading && students?.length === 0 && (
              <div className="py-6 text-center text-sm text-ink-soft">该班级暂无学生</div>
            )}
          </div>
        </div>
      )}

      {classes?.length === 0 && (
        <div className="rounded-3xl bg-white p-8 text-center text-sm text-ink-soft shadow-soft">
          暂无班级
        </div>
      )}
    </div>
  )
}

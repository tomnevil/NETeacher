import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  listPapers,
  getPaper
} from '../api/assessment'
import {
  listTeacherClasses,
  createAssignment,
  listAssignments,
  assignmentStats
} from '../api/ops'
import type { AssignmentStats } from '../api/types'

export default function Assignments() {
  const [params] = useSearchParams()
  const qc = useQueryClient()
  const [paperId, setPaperId] = useState<number | ''>(
    params.get('paperId') ? Number(params.get('paperId')) : ''
  )
  const [classId, setClassId] = useState<number | ''>('')
  const [title, setTitle] = useState('')
  const [dueAt, setDueAt] = useState('')
  const [msg, setMsg] = useState('')
  const [openStats, setOpenStats] = useState<number | null>(null)

  const { data: papers } = useQuery({
    queryKey: ['papers'],
    queryFn: () => listPapers().then((r) => r.data.data)
  })
  const { data: classes } = useQuery({
    queryKey: ['teacherClasses'],
    queryFn: () => listTeacherClasses().then((r) => r.data.data)
  })
  const { data: assignments } = useQuery({
    queryKey: ['assignments'],
    queryFn: () => listAssignments().then((r) => r.data.data)
  })
  const { data: stats } = useQuery({
    queryKey: ['assignmentStats', openStats],
    enabled: openStats !== null,
    queryFn: () => assignmentStats(openStats as number).then((r) => r.data.data)
  })

  const createMutation = useMutation({
    mutationFn: () =>
      createAssignment({
        paperId: Number(paperId),
        classId: Number(classId),
        title: title || undefined,
        dueAt: dueAt ? new Date(dueAt).toISOString().slice(0, 19) : undefined
      }),
    onSuccess: () => {
      setMsg('作业已下发')
      qc.invalidateQueries({ queryKey: ['assignments'] })
    },
    onError: (e: any) => setMsg('下发失败：' + (e?.response?.data?.message || e.message))
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800">作业布置与统计</h1>
        <p className="mt-1 text-xs text-gray-400">
          以试卷为载体积压给班级（FR-TRK-010）；学生提交时带上作业 id，即可统计完成率、均分与薄弱知识点。
        </p>
      </div>

      {/* 下发作业 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-3 text-sm font-semibold text-gray-600">下发作业</div>
        <div className="flex flex-wrap items-end gap-3">
          <label className="text-xs text-gray-500">
            试卷
            <select
              className="mt-1 block w-64 rounded-xl border border-gray-200 px-2 py-1.5"
              value={paperId}
              onChange={(e) => setPaperId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">请选择</option>
              {(papers || []).map((p) => (
                <option key={p.id} value={p.id}>
                  #{p.id} {p.title}（{p.questionIds.length} 题）
                </option>
              ))}
            </select>
          </label>
          <label className="text-xs text-gray-500">
            班级
            <select
              className="mt-1 block w-48 rounded-xl border border-gray-200 px-2 py-1.5"
              value={classId}
              onChange={(e) => setClassId(e.target.value === '' ? '' : Number(e.target.value))}
            >
              <option value="">请选择</option>
              {(classes || []).map((c) => (
                <option key={c.classId} value={c.classId}>
                  {c.className}
                </option>
              ))}
            </select>
          </label>
          <label className="text-xs text-gray-500">
            标题（可留空）
            <input
              className="mt-1 block w-48 rounded-xl border border-gray-200 px-2 py-1.5"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </label>
          <label className="text-xs text-gray-500">
            截止时间
            <input
              type="datetime-local"
              className="mt-1 block w-56 rounded-xl border border-gray-200 px-2 py-1.5"
              value={dueAt}
              onChange={(e) => setDueAt(e.target.value)}
            />
          </label>
          <button
            className="rounded-xl bg-emerald-600 px-5 py-2 text-sm text-white disabled:opacity-50"
            disabled={!paperId || !classId || createMutation.isPending}
            onClick={() => createMutation.mutate()}
          >
            {createMutation.isPending ? '下发中…' : '下发作业'}
          </button>
        </div>
        {msg && <div className="mt-3 text-xs text-indigo-700">{msg}</div>}
      </div>

      {/* 作业列表 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-2 text-sm font-semibold text-gray-600">已布置的作业</div>
        {!assignments?.length ? (
          <div className="text-xs text-gray-400">暂无</div>
        ) : (
          <div className="space-y-2">
            {assignments.map((a) => (
              <div key={a.id} className="rounded-2xl border border-gray-100 bg-gray-50 p-3">
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-700">
                    {a.title}
                    <span className="ml-2 text-[11px] text-gray-400">
                      试卷 #{a.paperId} · 班级 #{a.classId}
                    </span>
                  </div>
                  <button
                    className="rounded-xl border border-gray-200 px-3 py-1 text-xs"
                    onClick={() => setOpenStats(openStats === a.id ? null : a.id)}
                  >
                    {openStats === a.id ? '收起' : '查看统计'}
                  </button>
                </div>
                {openStats === a.id && stats && stats.assignmentId === a.id && (
                  <StatsPanel s={stats} />
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function StatsPanel({ s }: { s: AssignmentStats }) {
  return (
    <div className="mt-3 space-y-3">
      <div className="grid grid-cols-2 gap-2 md:grid-cols-4">
        <Stat label="应完成" value={`${s.assignedCount} 人`} />
        <Stat label="已完成" value={`${s.completedCount} 人`} />
        <Stat label="完成率" value={`${s.completionRate.toFixed(1)}%`} />
        <Stat label="平均分" value={s.avgScore.toFixed(1)} />
      </div>

      {s.weakKnowledgePoints.length > 0 && (
        <div className="rounded-2xl border border-rose-200 bg-rose-50 p-3 text-xs text-rose-700">
          薄弱知识点：
          <div className="mt-1 flex flex-wrap gap-2">
            {s.weakKnowledgePoints.map((k) => (
              <span key={k} className="rounded-full bg-rose-100 px-2 py-0.5">
                {k}
              </span>
            ))}
          </div>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs">
          <thead className="text-gray-400">
            <tr>
              <th className="py-1">学生</th>
              <th className="py-1">状态</th>
              <th className="py-1">得分</th>
            </tr>
          </thead>
          <tbody>
            {(s.students || []).map((st) => (
              <tr key={st.studentId} className="border-t border-gray-100">
                <td className="py-1">{st.nickname}</td>
                <td className="py-1">
                  <span
                    className={
                      st.finished ? 'text-emerald-600' : 'text-amber-600'
                    }
                  >
                    {st.finished ? '已完成' : '未完成'}
                  </span>
                </td>
                <td className="py-1">{st.score ?? '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl bg-white p-3">
      <div className="text-[11px] text-gray-400">{label}</div>
      <div className="mt-0.5 text-sm font-semibold text-gray-700">{value}</div>
    </div>
  )
}

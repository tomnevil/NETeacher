import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { opsDashboard } from '../api/ops'
import type { OpsDashboard as OpsDashboardData } from '../api/types'

interface Metric {
  key: string
  label: string
  value: string
  target?: number
  /** 目标为「越低越好」（如未绑定家长占比） */
  lowerBetter?: boolean
  hint?: string
}

export default function OpsDashboard() {
  const [trendMetric, setTrendMetric] = useState<'dau' | 'minutes'>('dau')
  const { data, isLoading, error } = useQuery({
    queryKey: ['opsDashboard'],
    queryFn: () => opsDashboard().then((r) => r.data.data)
  })

  if (isLoading) {
    return <div className="p-8 text-sm text-gray-400">加载中…</div>
  }
  if (error || !data) {
    return (
      <div className="p-8 text-sm text-rose-500">
        加载失败：{(error as any)?.response?.data?.message || '无权限或接口异常（需 ADMIN 角色）'}
      </div>
    )
  }

  const d = data as OpsDashboardData
  const t = d.targets || {}

  const metrics: Metric[] = [
    {
      key: 'weeklyAvgMinutes',
      label: '周活跃人均学习时长（分钟）',
      value: d.weeklyAvgMinutes.toFixed(1),
      target: t.weeklyAvgMinutes,
      hint: '北极星指标'
    },
    {
      key: 'unitTestCompletionRate',
      label: '单元测完成率（%）',
      value: d.unitTestCompletionRate.toFixed(1),
      target: t.unitTestCompletionRate,
      hint: `样本 ${d.unitTestTotal} 次`
    },
    {
      key: 'speakingMonthlyDelta',
      label: '口语均分月度提升（分）',
      value: d.speakingMonthlyDelta.toFixed(1),
      target: t.speakingMonthlyDelta,
      hint: `本月 ${d.speakingAvgThisMonth.toFixed(1)} / 上月 ${d.speakingAvgLastMonth.toFixed(1)}`
    },
    {
      key: 'retentionRate',
      label: '次周留存率（%）',
      value: d.retentionRate.toFixed(1),
      target: t.retentionRate
    },
    {
      key: 'membershipConversionRate',
      label: '会员转化率（%）',
      value: d.membershipConversionRate.toFixed(1),
      target: t.membershipConversionRate,
      hint: `付费会员 ${d.paidMembers} 人`
    },
    {
      key: 'unboundParentRate',
      label: '未绑定家长占比（%）',
      value: d.unboundParentRate.toFixed(1),
      target: t.unboundParentRate,
      lowerBetter: true,
      hint: `${d.unboundParentStudents} 个学生未绑定`
    },
    { key: 'dau', label: '今日活跃（DAU）', value: String(d.dau) },
    { key: 'wau', label: '近 7 日活跃（WAU）', value: String(d.wau) },
    {
      key: 'questionUsageRate',
      label: '题库使用率（%）',
      value: d.questionUsageRate.toFixed(1),
      hint: `已发布 ${d.publishedQuestions} 题`
    },
    {
      key: 'courseUsageRate',
      label: '课程使用率（%）',
      value: d.courseUsageRate.toFixed(1),
      hint: `课程 ${d.totalCourses} 门`
    }
  ]

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-800">运营数据看板</h1>
        <p className="mt-1 text-xs text-gray-400">
          指标口径对齐《PRD-主文档》§5 北极星与核心指标；绿色表示达成目标，橙色表示未达成。
        </p>
      </div>

      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
        {metrics.map((m) => {
          const reached =
            m.target === undefined
              ? null
              : m.lowerBetter
                ? Number(m.value) <= m.target
                : Number(m.value) >= m.target
          return (
            <div
              key={m.key}
              className={`rounded-3xl border p-4 ${
                reached === null
                  ? 'border-gray-100 bg-white'
                  : reached
                    ? 'border-emerald-200 bg-emerald-50'
                    : 'border-amber-200 bg-amber-50'
              }`}
            >
              <div className="text-xs text-gray-500">{m.label}</div>
              <div className="mt-1 text-2xl font-bold text-gray-800">{m.value}</div>
              {m.target !== undefined && (
                <div
                  className={`mt-1 text-[11px] ${reached ? 'text-emerald-600' : 'text-amber-600'}`}
                >
                  目标 {m.target} · {reached ? '已达成' : '未达成'}
                </div>
              )}
              {m.hint && <div className="mt-1 text-[11px] text-gray-400">{m.hint}</div>}
            </div>
          )
        })}
      </div>

      {/* 近 14 日趋势 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-3 flex items-center justify-between">
          <div className="text-sm font-semibold text-gray-600">近 14 日趋势</div>
          <div className="flex gap-1">
            <button
              className={`rounded-lg px-2 py-1 text-xs ${
                trendMetric === 'dau' ? 'bg-indigo-600 text-white' : 'border border-gray-200'
              }`}
              onClick={() => setTrendMetric('dau')}
            >
              活跃人数
            </button>
            <button
              className={`rounded-lg px-2 py-1 text-xs ${
                trendMetric === 'minutes' ? 'bg-indigo-600 text-white' : 'border border-gray-200'
              }`}
              onClick={() => setTrendMetric('minutes')}
            >
              学习时长
            </button>
          </div>
        </div>
        <TrendChart data={d.trend || []} metric={trendMetric} />
        <div className="mt-1 flex justify-between text-[10px] text-gray-400">
          <span>{(d.trend || [])[0]?.date ?? ''}</span>
          <span>{(d.trend || [])[(d.trend || []).length - 1]?.date ?? ''}</span>
        </div>
      </div>

      {/* 按班级下钻 */}
      <div className="rounded-3xl bg-white p-4 shadow-sm">
        <div className="mb-3 text-sm font-semibold text-gray-600">按班级下钻</div>
        {!(d.classBreakdown || []).length ? (
          <div className="text-xs text-gray-400">暂无班级数据</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-gray-400">
                <tr>
                  <th className="py-1">班级</th>
                  <th className="py-1">学员数</th>
                  <th className="py-1">今日活跃</th>
                  <th className="py-1">周人均时长（分钟）</th>
                  <th className="py-1">未绑定家长</th>
                </tr>
              </thead>
              <tbody>
                {(d.classBreakdown || []).map((c) => (
                  <tr key={c.classId} className="border-t border-gray-100">
                    <td className="py-1">{c.className}</td>
                    <td className="py-1">{c.students}</td>
                    <td className="py-1">{c.dau}</td>
                    <td className="py-1">{c.weeklyAvgMinutes.toFixed(1)}</td>
                    <td className="py-1">
                      <span
                        className={
                          c.unboundParentRate > 0 ? 'text-amber-600' : 'text-emerald-600'
                        }
                      >
                        {c.unboundParentRate.toFixed(1)}%
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="rounded-3xl bg-white p-4 text-xs text-gray-500 shadow-sm">
        规模：学员 {d.totalStudents} 人 · 教师 {d.totalTeachers} 人
      </div>
    </div>
  )
}

function TrendChart({
  data,
  metric
}: {
  data: { date: string; dau: number; minutes: number }[]
  metric: 'dau' | 'minutes'
}) {
  const values = data.map((x) => (metric === 'dau' ? x.dau : x.minutes))
  const max = Math.max(1, ...values)
  const w = 640
  const h = 160
  const pad = 20
  const step = data.length > 1 ? (w - pad * 2) / (data.length - 1) : 0
  const y = (v: number) => h - pad - (v / max) * (h - pad * 2)
  const points = values.map((v, i) => `${pad + i * step},${y(v)}`).join(' ')

  return (
    <svg viewBox={`0 0 ${w} ${h}`} className="w-full">
      <line x1={pad} y1={h - pad} x2={w - pad} y2={h - pad} stroke="#e5e7eb" />
      <polyline points={points} fill="none" stroke="#6366f1" strokeWidth={2} />
      {values.map((v, i) => (
        <circle key={i} cx={pad + i * step} cy={y(v)} r={2.5} fill="#6366f1" />
      ))}
    </svg>
  )
}

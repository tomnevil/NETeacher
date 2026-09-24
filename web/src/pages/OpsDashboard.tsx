import { useQuery } from '@tanstack/react-query'
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

      <div className="rounded-3xl bg-white p-4 text-xs text-gray-500 shadow-sm">
        规模：学员 {d.totalStudents} 人 · 教师 {d.totalTeachers} 人
      </div>
    </div>
  )
}

import { useState } from 'react'
import Card from '../components/ui/Card'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '../store/auth'
import { getBindStatus, bindParent, listChildren, getReport } from '../api/parent'
import type { ParentReport } from '../api/types'

export default function Parent() {
  const role = useAuthStore((s) => s.role)
  if (role === 'parent') return <ParentView />
  return <StudentBindView />
}

function StudentBindView() {
  const qc = useQueryClient()
  const { data } = useQuery({
    queryKey: ['bind-status'],
    queryFn: () => getBindStatus().then((r) => r.data.data)
  })
  const [phone, setPhone] = useState('')
  const mutation = useMutation({
    mutationFn: (p: string) => bindParent(p),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['bind-status'] })
  })

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">家长绑定</h1>
      <Card>
        {data?.bound ? (
          <div className="text-brand-800">
            已绑定家长：<span className="font-semibold">{data.parent?.nickname ?? data.parent?.phone}</span>
            <span className="text-gray-400 ml-2">（{data.parent?.phone}）</span>
          </div>
        ) : (
          <>
            <div className="text-sm text-gray-500 mb-3">
              绑定家长后，家长可在「家长端」查看你的学情报告。输入家长手机号完成绑定（家长账号不存在将自动创建）。
            </div>
            <div className="flex gap-2">
              <input
                className="border border-brand-200 rounded-xl px-3 py-2 flex-1"
                placeholder="家长手机号"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
              <button
                disabled={mutation.isPending || phone.length < 6}
                onClick={() => mutation.mutate(phone)}
                className="bg-brand-600 hover:bg-brand-700 text-white rounded-2xl px-5 py-2 disabled:opacity-50"
              >
                {mutation.isPending ? '绑定中…' : '绑定'}
              </button>
            </div>
          </>
        )}
      </Card>
    </div>
  )
}

function ParentView() {
  const { data: children } = useQuery({
    queryKey: ['children'],
    queryFn: () => listChildren().then((r) => r.data.data)
  })
  const [childId, setChildId] = useState<number | null>(null)
  const { data: report } = useQuery({
    queryKey: ['report', childId],
    enabled: childId != null,
    queryFn: () => getReport(childId!).then((r) => r.data.data)
  })

  if (!children || children.length === 0) {
    return (
      <div className="space-y-5">
        <h1 className="text-2xl font-bold text-brand-800">家长端</h1>
        <Card>
          <div className="text-brand-800 font-semibold mb-1">还没有关联的孩子</div>
          <div className="text-sm text-gray-500">
            请让孩子在其账号的「家长绑定」页输入你的手机号完成绑定。
          </div>
        </Card>
      </div>
    )
  }

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">家长端 · 学情报告</h1>

      <div className="flex gap-2 flex-wrap">
        {children.map((c) => (
          <button
            key={c.uid}
            onClick={() => setChildId(c.uid)}
            className={`rounded-2xl px-4 py-2 border ${
              childId === c.uid
                ? 'border-brand-500 bg-brand-50 text-brand-800'
                : 'border-brand-200 text-brand-600'
            }`}
          >
            {c.nickname ?? c.phone}
          </button>
        ))}
      </div>

      {childId != null && report && <ReportCard report={report} />}
    </div>
  )
}

function ReportCard({ report }: { report: ParentReport }) {
  return (
    <Card>
      <div className="font-semibold text-brand-800 mb-2">
        孩子：{report.child.nickname ?? report.child.phone}
      </div>
      <div className="grid grid-cols-2 gap-3 mb-3">
        <Stat label="测评次数" value={report.assessmentCount} />
        <Stat label="练习次数" value={report.recordCount} />
      </div>

      {report.mastery && Object.keys(report.mastery).length > 0 && (
        <div className="mb-3">
          <div className="text-sm text-gray-500 mb-1">各科学情</div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
            {Object.entries(report.mastery).map(([k, v]) => (
              <div key={k} className="bg-brand-50 rounded-xl p-2 text-center">
                <div className="font-bold text-brand-700">{v}</div>
                <div className="text-xs text-gray-500">{k}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {report.path && report.path.items.length > 0 && (
        <div className="mb-3">
          <div className="text-sm text-gray-500 mb-1">
            推荐路径（L{report.path.currentLevel} → L{report.path.nextLevel}）
          </div>
          <ul className="text-sm text-gray-600 list-disc list-inside">
            {report.path.items.map((it, i) => (
              <li key={i}>
                {it.title} · {it.reason}
              </li>
            ))}
          </ul>
        </div>
      )}

      {report.comment && (
        <div className="text-sm text-gray-600 bg-brand-50 rounded-2xl p-3">
          <div className="font-medium text-brand-700 mb-1">🤖 给家长的话</div>
          {report.comment}
        </div>
      )}
    </Card>
  )
}

function Stat({ label, value }: { label: string; value: number }) {
  return (
    <div className="bg-brand-50 rounded-2xl p-3 text-center">
      <div className="text-2xl font-bold text-brand-700">{value}</div>
      <div className="text-xs text-gray-500">{label}</div>
    </div>
  )
}

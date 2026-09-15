import Card from '../components/ui/Card'
import { useQuery } from '@tanstack/react-query'
import { getDashboard } from '../api/progress'
import type { ProgressDashboard } from '../api/types'

function subjectName(s: string) {
  return (
    { listening: '听力', reading: '阅读', speaking: '口语', writing: '写作' }[s] ?? s
  )
}

export default function Progress() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: () => getDashboard().then((r) => r.data.data)
  })

  if (isLoading) return <div className="text-gray-400">加载仪表盘…</div>
  if (!data) return <div className="text-gray-400">暂无数据</div>

  const d: ProgressDashboard = data

  return (
    <div className="space-y-5">
      <h1 className="text-2xl font-bold text-brand-800">学习仪表盘</h1>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <Stat label="综合等级" value={'L' + d.overallLevel} />
        <Stat label="连续打卡" value={d.streakDays + ' 天'} />
        <Stat label="练习时长" value={d.totalMinutes + ' 分'} />
        <Stat label="口语均分" value={String(d.speakingAvg)} />
      </div>

      <Card>
        <div className="font-semibold text-brand-800 mb-2">各科学情</div>
        {Object.keys(d.mastery).length === 0 ? (
          <div className="text-sm text-gray-400">还没有测评数据，去做一次「测评」吧。</div>
        ) : (
          <div className="space-y-2">
            {Object.entries(d.mastery).map(([k, v]) => (
              <div key={k}>
                <div className="flex justify-between text-sm">
                  <span className="text-brand-700">{subjectName(k)}</span>
                  <span className="text-gray-500">{v}</span>
                </div>
                <div className="h-2 bg-brand-50 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-brand-500"
                    style={{ width: Math.min(100, Math.max(0, v)) + '%' }}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
        {d.weakSubjects.length > 0 && (
          <div className="mt-3 text-sm text-accent-500">
            薄弱项：{d.weakSubjects.map(subjectName).join('、')}
          </div>
        )}
      </Card>

      <Card>
        <div className="font-semibold text-brand-800 mb-2">
          最近练习（测评 {d.assessmentCount} · 练习 {d.recordCount}）
        </div>
        <div className="space-y-2">
          {d.recentRecords.map((r) => (
            <div key={r.id} className="border border-brand-50 rounded-xl p-2 text-sm">
              <span className="text-brand-700">{r.module}</span> · 得分 {r.score ?? '-'} ·{' '}
              {r.durationSec ?? 0} 秒
            </div>
          ))}
          {d.recentRecords.length === 0 && (
            <div className="text-sm text-gray-400">还没有练习记录。</div>
          )}
        </div>
      </Card>
    </div>
  )
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-brand-50 rounded-2xl p-3 text-center">
      <div className="text-xl font-bold text-brand-700">{value}</div>
      <div className="text-xs text-gray-500">{label}</div>
    </div>
  )
}

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import { getHome, doCheckIn } from '../api/progress'
import { useAuthStore } from '../store/auth'

function Stars({ n, size = 14 }: { n: number; size?: number }) {
  return (
    <span className="inline-flex gap-0.5 text-accent-500" style={{ fontSize: size }}>
      {[1, 2, 3].map((i) => (
        <span key={i} className={i <= n ? '' : 'opacity-20'}>
          ★
        </span>
      ))}
    </span>
  )
}

const TYPE_ICON: Record<string, string> = {
  course: '📘',
  speaking: '🎤',
  quiz: '📝'
}

export default function Home() {
  const nav = useNavigate()
  const nickname = useAuthStore((s) => s.nickname)
  const qc = useQueryClient()

  const { data } = useQuery({
    queryKey: ['home'],
    queryFn: () => getHome().then((r) => r.data.data)
  })

  const checkin = useMutation({
    mutationFn: doCheckIn,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['home'] })
  })

  const home = data
  const check = home?.checkIn

  return (
    <div className="max-w-2xl mx-auto space-y-4 pb-6">
      {/* 顶部引导 */}
      <div className="bg-gradient-to-br from-sky-400 to-brand-600 rounded-3xl p-5 text-white shadow-soft animate-pop-in">
        <div className="flex items-center gap-3">
          <img
            src="/assets/mascot.png"
            alt="学习伙伴"
            className="w-14 h-14 rounded-2xl bg-white/20 p-1 animate-float"
          />
          <div className="flex-1">
            <div className="text-lg font-bold">Hi，{nickname ?? '同学'} 👋</div>
            <div className="text-sm text-white/85">今天也要加油哦～</div>
          </div>
          <div className="text-right">
            <div className="text-2xl font-bold leading-none">⭐ {home?.totalStars ?? 0}</div>
            <div className="text-xs text-white/80">累计星星</div>
          </div>
        </div>
      </div>

      {/* 统计胶囊 */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <Stat icon="🗣️" value={home ? home.stats.speakingAvg.toFixed(1) : '—'} label="口语均分" />
        <Stat icon="🔥" value={check ? String(check.streakDays) : '—'} label="连续打卡" />
        <Stat icon="⏱️" value={home ? String(home.stats.totalMinutes) : '—'} label="学习(分)" />
        <Stat icon="⭐" value={home ? String(home.totalStars) : '—'} label="累计星星" />
      </div>

      {/* 今日任务 */}
      <Card>
        <div className="flex items-center justify-between mb-3">
          <div className="font-bold text-brand-800 text-lg">今日任务</div>
          <span className="text-xs text-brand-500">{home?.tasks.length ?? 0} 项</span>
        </div>
        <div className="space-y-2">
          {home?.tasks.map((t) => (
            <button
              key={t.id}
              onClick={() => nav(t.to)}
              className="w-full flex items-center gap-3 text-left bg-brand-50/50 rounded-2xl p-3 hover:bg-brand-50 transition"
            >
              <div className="w-11 h-11 rounded-2xl bg-white shadow-soft flex items-center justify-center text-xl">
                {TYPE_ICON[t.type] ?? '📚'}
              </div>
              <div className="flex-1 min-w-0">
                <div className="font-medium text-brand-800 truncate">{t.title}</div>
                <div className="mt-1 h-1.5 w-full bg-brand-100 rounded-full overflow-hidden">
                  <div
                    className="h-full bg-gradient-to-r from-sky-400 to-brand-500 rounded-full"
                    style={{ width: `${t.progress}%` }}
                  />
                </div>
              </div>
              <div className="text-right shrink-0">
                <Stars n={t.starsReward > 2 ? 3 : 2} size={12} />
                <div className="text-[10px] text-accent-600 mt-0.5">+{t.starsReward}</div>
              </div>
            </button>
          ))}
          {!home && <div className="text-sm text-gray-400 py-4 text-center">加载中…</div>}
        </div>
      </Card>

      {/* 学习地图入口 */}
      <Card className="!p-0 overflow-hidden">
        <button onClick={() => nav('/map')} className="w-full flex items-stretch">
          <div className="flex-1 p-5 text-left">
            <div className="font-bold text-brand-800 text-lg">学习地图</div>
            <div className="text-sm text-gray-500 mt-1">L1–L6 闯关路径，解锁更多课程</div>
            <div className="mt-3 inline-flex items-center gap-1 text-sm text-brand-600 font-medium">
              查看我的进度 →
            </div>
          </div>
          <img
            src="/assets/concept_map.png"
            alt="学习地图参考"
            className="w-28 object-cover opacity-90"
          />
        </button>
      </Card>

      {/* 连续打卡 */}
      <Card>
        <div className="flex items-center justify-between mb-3">
          <div className="font-bold text-brand-800 text-lg">连续打卡</div>
          <div className="text-sm text-accent-600 font-medium">🔥 {check?.streakDays ?? 0} 天</div>
        </div>
        <div className="flex justify-between gap-1.5 mb-4">
          {check?.week.map((on, i) => (
            <div key={i} className="flex-1 flex flex-col items-center gap-1">
              <div
                className={`w-9 h-9 rounded-full flex items-center justify-center text-sm transition ${
                  on
                    ? 'bg-gradient-to-br from-sky-400 to-brand-600 text-white shadow-soft'
                    : 'bg-brand-50 text-gray-300'
                } ${i === (check.week.length - 1) ? 'ring-2 ring-accent-400 ring-offset-1' : ''}`}
              >
                {on ? '✓' : ''}
              </div>
              <span className="text-[10px] text-gray-400">
                {['一', '二', '三', '四', '五', '六', '日'][i]}
              </span>
            </div>
          ))}
          {!check && (
            <div className="flex-1 text-center text-sm text-gray-400 py-4">加载中…</div>
          )}
        </div>
        <Button
          variant={check?.checkedToday ? 'ghost' : 'primary'}
          className="w-full"
          disabled={check?.checkedToday || checkin.isPending}
          onClick={() => checkin.mutate()}
        >
          {check?.checkedToday ? '今日已打卡 ✓' : checkin.isPending ? '打卡中…' : '今日打卡 +1⭐'}
        </Button>
      </Card>
    </div>
  )
}

function Stat({ icon, value, label }: { icon: string; value: string; label: string }) {
  return (
    <div className="bg-white rounded-2xl shadow-soft p-3 text-center">
      <div className="text-xl">{icon}</div>
      <div className="font-bold text-brand-700 text-lg leading-tight mt-0.5">{value}</div>
      <div className="text-[11px] text-gray-500">{label}</div>
    </div>
  )
}

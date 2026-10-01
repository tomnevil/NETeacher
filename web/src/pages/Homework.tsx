import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { myHomework } from '../api/ops'

/** 后端返回 LocalDateTime（无时区），展示到分钟 */
function fmt(v: string | null | undefined) {
  if (!v) return ''
  return String(v).slice(0, 16).replace('T', ' ')
}

export default function Homework() {
  const nav = useNavigate()
  const { data, isLoading } = useQuery({
    queryKey: ['myHomework'],
    queryFn: () => myHomework().then((r) => r.data.data)
  })

  const list = data || []
  const overdue = list.filter((a) => a.overdue)
  const dueSoon = list.filter((a) => a.dueSoon)

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-800">我的作业</h1>

      {/* 逾期提醒：置顶展示，避免学生漏做 */}
      {overdue.length > 0 && (
        <div className="rounded-3xl border border-amber-300 bg-amber-50 p-4">
          <div className="text-sm font-bold text-amber-700">
            ⚠️ 你有 {overdue.length} 项作业已逾期未完成
          </div>
          <div className="mt-1 text-xs text-amber-700">{overdue.map((a) => a.title).join('、')}</div>
        </div>
      )}

      {/* 截止前提醒：24 小时内到期且未提交 */}
      {dueSoon.length > 0 && (
        <div className="rounded-3xl border border-sky-300 bg-sky-50 p-4">
          <div className="text-sm font-bold text-sky-700">
            ⏰ 你有 {dueSoon.length} 项作业即将截止（24 小时内）
          </div>
          <div className="mt-1 text-xs text-sky-700">{dueSoon.map((a) => a.title).join('、')}</div>
        </div>
      )}

      {isLoading ? (
        <div className="text-sm text-gray-400">加载中…</div>
      ) : !list.length ? (
        <div className="rounded-3xl bg-white p-4 text-xs text-gray-400 shadow-sm">暂无作业</div>
      ) : (
        <div className="space-y-2">
          {list.map((a) => (
            <div
              key={a.id}
              className={`rounded-3xl bg-white p-4 shadow-sm ${
                a.overdue ? 'ring-1 ring-amber-300' : a.dueSoon ? 'ring-1 ring-sky-300' : ''
              }`}
            >
              <div className="flex items-center justify-between gap-3">
                <div className="min-w-0">
                  <div className="truncate text-sm font-semibold text-gray-800">{a.title}</div>
                  <div className="mt-0.5 text-[11px] text-gray-400">
                    试卷 #{a.paperId}
                    {a.dueAt ? ` · 截止 ${fmt(a.dueAt)}` : ''}
                    {a.score != null ? ` · 得分 ${a.score}` : ''}
                  </div>
                </div>
                <div className="flex shrink-0 items-center gap-2">
                  {a.overdue ? (
                    <span className="rounded-full bg-amber-100 px-3 py-1 text-xs text-amber-700">
                      已逾期
                    </span>
                  ) : a.dueSoon ? (
                    <span className="rounded-full bg-sky-100 px-3 py-1 text-xs text-sky-700">
                      即将截止
                    </span>
                  ) : a.finished ? (
                    <span className="rounded-full bg-emerald-100 px-3 py-1 text-xs text-emerald-700">
                      已完成
                    </span>
                  ) : (
                    <span className="rounded-full bg-gray-100 px-3 py-1 text-xs text-gray-500">
                      待完成
                    </span>
                  )}
                  {!a.finished && (
                    <button
                      className="rounded-xl bg-indigo-600 px-4 py-1.5 text-xs text-white"
                      onClick={() =>
                        nav(`/exercise?paperId=${a.paperId}&assignmentId=${a.id}`)
                      }
                    >
                      去完成
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

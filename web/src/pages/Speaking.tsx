import { useEffect, useRef, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router-dom'
import Card from '../components/ui/Card'
import Button from '../components/ui/Button'
import { getSpeakingTask, evaluateSpeaking } from '../api/speaking'
import type { SpeakingTask, SpeakingEvalResult } from '../api/types'

function ScoreRing({ score }: { score: number }) {
  const r = 52
  const c = 2 * Math.PI * r
  const color = score >= 85 ? '#22c55e' : score >= 70 ? '#f59e0b' : '#ef4444'
  return (
    <div className="relative w-32 h-32">
      <svg className="w-32 h-32 -rotate-90" viewBox="0 0 120 120">
        <circle cx="60" cy="60" r={r} fill="none" stroke="#e5e7eb" strokeWidth="10" />
        <circle
          cx="60"
          cy="60"
          r={r}
          fill="none"
          stroke={color}
          strokeWidth="10"
          strokeLinecap="round"
          strokeDasharray={c}
          strokeDashoffset={c * (1 - score / 100)}
          style={{ transition: 'stroke-dashoffset 0.6s ease' }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <div className="text-3xl font-bold" style={{ color }}>
          {score}
        </div>
        <div className="text-xs text-gray-400">总分</div>
      </div>
    </div>
  )
}

function Wave({ data, color }: { data: number[]; color: string }) {
  const max = Math.max(...data, 1)
  return (
    <div className="flex items-end gap-[2px] h-12">
      {data.map((v, i) => (
        <div
          key={i}
          className="flex-1 rounded-sm"
          style={{ height: `${(v / max) * 100}%`, background: color, opacity: 0.85 }}
        />
      ))}
    </div>
  )
}

export default function Speaking() {
  const qc = useQueryClient()
  const [searchParams] = useSearchParams()
  // 由学习地图关卡节点带参进入：/speaking?courseId=xx
  const courseId = Number(searchParams.get('courseId')) || undefined
  const [transcript, setTranscript] = useState('')
  const [listening, setListening] = useState(false)
  const [phase, setPhase] = useState<'idle' | 'result'>('idle')
  const [result, setResult] = useState<SpeakingEvalResult | null>(null)
  const recRef = useRef<any>(null)

  const { data: task } = useQuery({
    queryKey: ['speaking-task', courseId ?? 0],
    queryFn: () => getSpeakingTask(2, courseId).then((r) => r.data.data)
  })

  const evaluate = useMutation({
    mutationFn: (t: SpeakingTask) =>
      evaluateSpeaking({ taskId: t.id, refText: t.refText, transcript }).then((r) => r.data.data),
    onSuccess: (d) => {
      setResult(d)
      setPhase('result')
    }
  })

  useEffect(() => {
    const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    if (SR) {
      const rec = new SR()
      rec.lang = 'en-US'
      rec.interimResults = false
      rec.onresult = (e: any) => setTranscript(e.results[0][0].transcript)
      recRef.current = rec
    }
    return () => recRef.current?.stop?.()
  }, [])

  const start = () => {
    setTranscript('')
    setPhase('idle')
    setResult(null)
    if (recRef.current) {
      setListening(true)
      recRef.current.onend = () => setListening(false)
      recRef.current.start()
    } else {
      setTranscript('I like apples and bananas.')
    }
  }

  const submit = () => {
    if (task && transcript.trim()) evaluate.mutate(task)
  }

  const replay = () => {
    if (!task) return
    const u = new SpeechSynthesisUtterance(task.refText)
    u.lang = 'en-US'
    window.speechSynthesis.speak(u)
  }

  const next = () => {
    setTranscript('')
    setResult(null)
    setPhase('idle')
    qc.invalidateQueries({ queryKey: ['speaking-task'] })
  }

  const statusColor = (s: string) =>
    s === 'good' ? 'bg-green-100 text-green-700' : s === 'fair' ? 'bg-amber-100 text-amber-700' : 'bg-red-100 text-red-700'

  return (
    <div className="max-w-2xl mx-auto space-y-4 pb-6">
      <div className="flex items-center gap-3">
        <img src="/assets/mascot.png" alt="教练" className="w-12 h-12 rounded-2xl bg-gradient-to-br from-sky-400 to-brand-600 p-1 shadow-soft animate-float" />
        <div>
          <div className="text-2xl font-bold text-brand-700">跟读训练室</div>
          <div className="text-xs text-accent-500 font-medium">听一听，跟着读，看评分</div>
        </div>
      </div>

      <img src="/assets/concept_speaking.png" alt="训练室参考" className="w-full rounded-2xl shadow-card opacity-90" />

      {task && (
        <Card>
          <div className="text-xs text-brand-500 mb-1">参考句子（L{task.level}）</div>
          <div className="text-xl font-bold text-brand-800">{task.refText}</div>
          <div className="text-sm text-gray-500 mt-1">🇨🇳 {task.translation}</div>
          <div className="text-xs text-accent-600 mt-2">💡 {task.tip}</div>
          <div className="flex flex-wrap gap-1.5 mt-3">
            {task.words.map((w, i) => (
              <span key={i} className="px-2 py-0.5 rounded-full bg-brand-50 text-brand-700 text-sm">
                {w}
              </span>
            ))}
          </div>
        </Card>
      )}

      {/* 录音 / 转写 */}
      <Card>
        <div className="flex items-center gap-3">
          <button
            onClick={start}
            className={`w-14 h-14 rounded-full flex items-center justify-center text-2xl shadow-soft transition ${
              listening ? 'bg-red-500 text-white animate-pulse' : 'bg-gradient-to-br from-sky-400 to-brand-600 text-white'
            }`}
          >
            🎤
          </button>
          <div className="flex-1">
            <div className="text-sm font-medium text-brand-700">
              {listening ? '正在聆听…说完自动停止' : '点击话筒开始跟读'}
            </div>
            <div className="text-xs text-gray-400 mt-0.5">支持中英文环境浏览器麦克风</div>
          </div>
          <button onClick={replay} className="text-brand-600 text-sm px-2">
            🔊 原音
          </button>
        </div>
        <textarea
          value={transcript}
          onChange={(e) => setTranscript(e.target.value)}
          placeholder="你说的内容会显示在这里，也可手动输入…"
          className="mt-3 w-full h-20 rounded-2xl border border-brand-100 p-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-200"
        />
        <Button className="w-full mt-2" disabled={!transcript.trim() || evaluate.isPending} onClick={submit}>
          {evaluate.isPending ? '评分中…' : '提交评分'}
        </Button>
      </Card>

      {/* 结果 */}
      {phase === 'result' && result && (
        <Card className="animate-pop-in">
          <div className="flex items-center gap-4">
            <ScoreRing score={result.score} />
            <div className="flex-1 space-y-2">
              <Metric label="准确度" v={result.accuracy} />
              <Metric label="流利度" v={result.fluency} />
              <Metric label="完整度" v={result.integrity} />
            </div>
          </div>

          <div className="mt-4">
            <div className="text-sm font-medium text-brand-700 mb-2">音素高亮</div>
            <div className="flex flex-wrap gap-2">
              {result.phonemes.map((p, i) => (
                <span key={i} className={`px-2.5 py-1 rounded-full text-sm ${statusColor(p.status)}`}>
                  {p.text} {p.score}
                </span>
              ))}
            </div>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-3">
            <div>
              <div className="text-xs text-gray-400 mb-1">标准波形</div>
              <Wave data={result.waveformRef} color="#60a5fa" />
            </div>
            <div>
              <div className="text-xs text-gray-400 mb-1">我的波形</div>
              <Wave data={result.waveformUser} color="#f59e0b" />
            </div>
          </div>

          <div className="mt-4 flex items-start gap-2 bg-brand-50 rounded-2xl p-3">
            <img src="/assets/mascot.png" alt="教练" className="w-8 h-8 rounded-full" />
            <div className="text-sm text-brand-800">{result.feedback}</div>
          </div>

          <div className="flex gap-2 mt-4">
            <Button variant="ghost" className="flex-1" onClick={replay}>
              🔊 再听原音
            </Button>
            <Button className="flex-1" onClick={next}>
              下一句 →
            </Button>
          </div>
        </Card>
      )}

      {!task && <div className="text-sm text-gray-400 py-6 text-center">加载中…</div>}
    </div>
  )
}

function Metric({ label, v }: { label: string; v: number }) {
  const color = v >= 85 ? '#22c55e' : v >= 70 ? '#f59e0b' : '#ef4444'
  return (
    <div>
      <div className="flex justify-between text-xs mb-0.5">
        <span className="text-gray-500">{label}</span>
        <span className="font-medium" style={{ color }}>
          {v}
        </span>
      </div>
      <div className="h-1.5 w-full bg-brand-100 rounded-full overflow-hidden">
        <div className="h-full rounded-full" style={{ width: `${v}%`, background: color }} />
      </div>
    </div>
  )
}

import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import { startDialogue, dialogueTurn, endDialogue } from '../api/learning'
import { recommendFromDialogue } from '../api/recommend'
import type { DialogueEndResult, RecommendItem } from '../api/types'

type Role = 'system' | 'student'
interface Msg {
  role: Role
  content: string
  turn?: number
  score?: number
  fluency?: number
  accuracy?: number
  relevance?: number
  comment?: string
}

const SCENES = ['日常英语会话', '学校生活', '购物', '兴趣爱好', '周末计划', '我的家庭']

export default function Dialogue() {
  const grade = useAuthStore((s) => s.grade)
  const navigate = useNavigate()
  const [phase, setPhase] = useState<'idle' | 'active' | 'ended'>('idle')
  const [unit, setUnit] = useState(SCENES[0])
  const [sessionId, setSessionId] = useState('')
  const [messages, setMessages] = useState<Msg[]>([])
  const [transcript, setTranscript] = useState('')
  const [listening, setListening] = useState(false)
  const [busy, setBusy] = useState(false)
  const [report, setReport] = useState<DialogueEndResult | null>(null)
  const [tasks, setTasks] = useState<RecommendItem[]>([])
  const [error, setError] = useState('')
  const bottomRef = useRef<HTMLDivElement>(null)
  const recRef = useRef<any>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, phase])

  const start = async () => {
    setBusy(true)
    setError('')
    try {
      const { data } = await startDialogue({ grade: grade ?? undefined, unit })
      if (data.code === 0) {
        setSessionId(data.data.sessionId)
        setMessages([{ role: 'system', content: data.data.opening }])
        setPhase('active')
      } else {
        setError(data.message)
      }
    } catch {
      setError('网络异常，请确认后端已启动（默认 8080）')
    } finally {
      setBusy(false)
    }
  }

  const startListening = () => {
    const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    if (!SR) {
      setError('当前浏览器不支持语音识别，请直接在下方输入')
      return
    }
    const rec = new SR()
    rec.lang = 'en-US'
    rec.interimResults = false
    rec.onresult = (e: any) => setTranscript(e.results[0][0].transcript)
    rec.onerror = () => setListening(false)
    rec.onend = () => setListening(false)
    recRef.current = rec
    rec.start()
    setListening(true)
  }

  const stopListening = () => {
    recRef.current?.stop()
    setListening(false)
  }

  const send = async () => {
    const t = transcript.trim()
    if (!t || !sessionId || busy) return
    setTranscript('')
    setBusy(true)
    try {
      const { data } = await dialogueTurn({ sessionId, transcript: t })
      if (data.code === 0) {
        const r = data.data
        setMessages((m) => [
          ...m,
          { role: 'student', content: t, turn: r.turn, score: r.score, fluency: r.fluency, accuracy: r.accuracy, relevance: r.relevance, comment: r.comment },
          { role: 'system', content: r.reply }
        ])
      } else {
        setError(data.message)
      }
    } catch {
      setError('发言提交失败，请重试')
    } finally {
      setBusy(false)
    }
  }

  const end = async () => {
    if (!sessionId || busy) return
    setBusy(true)
    try {
      const { data } = await endDialogue({ sessionId })
      if (data.code === 0) {
        setReport(data.data)
        setPhase('ended')
        // 对话薄弱点 -> 强化练习任务
        recommendFromDialogue({ grade: grade ?? undefined, unit, weaknesses: data.data.weaknesses })
          .then((t) => { if (t.data.code === 0) setTasks(t.data.data) })
          .catch(() => {})
      } else {
        setError(data.message)
      }
    } catch {
      setError('生成总评失败，请重试')
    } finally {
      setBusy(false)
    }
  }

  const reset = () => {
    setPhase('idle')
    setSessionId('')
    setMessages([])
    setReport(null)
    setTasks([])
    setTranscript('')
    setError('')
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex items-center gap-3 mb-1">
        <img
          src="/assets/mascot.png"
          alt="学习伙伴"
          className="w-12 h-12 rounded-2xl bg-gradient-to-br from-sky-400 to-brand-600 p-1 shadow-soft animate-float"
        />
        <div>
          <div className="text-2xl font-bold text-brand-700">AI 口语对话</div>
          <div className="text-xs text-accent-500 font-medium">和兔耳猫头鹰一起练口语</div>
        </div>
      </div>
      <p className="text-ink-soft mb-4">
        系统会按{grade ? ` ${grade} 年级` : '你的年级'}和学习单元主动发起话题，点「发言」用英语聊起来，每轮即时评分，结束还有总评与强化建议。
      </p>

      {phase === 'idle' && (
        <Card className="space-y-4">
          <div>
            <div className="text-sm text-gray-500 mb-2">选择本次对话场景</div>
            <div className="flex flex-wrap gap-2">
              {SCENES.map((s) => (
                <button
                  key={s}
                  onClick={() => setUnit(s)}
                  className={`px-3 py-1.5 rounded-full text-sm ${
                    unit === s ? 'bg-brand-600 text-white' : 'bg-brand-50 text-brand-700'
                  }`}
                >
                  {s}
                </button>
              ))}
            </div>
          </div>
          <Button className="w-full" onClick={start} disabled={busy}>
            {busy ? '发起中…' : '开始对话'}
          </Button>
        </Card>
      )}

      {phase === 'active' && (
        <Card className="space-y-4">
          <div className="h-[52vh] overflow-y-auto space-y-3 rounded-2xl bg-brand-50/40 p-3">
            {messages.map((m, i) => (
              <div key={i} className={m.role === 'system' ? 'flex justify-start' : 'flex justify-end'}>
                <div className={`max-w-[80%] ${m.role === 'system' ? 'bg-white' : 'bg-brand-600 text-white'} rounded-2xl px-4 py-2`}>
                  <div className="text-sm whitespace-pre-wrap">{m.content}</div>
                  {m.role === 'student' && m.score != null && (
                    <div className="mt-1 text-xs opacity-90">
                      本轮得分 <b>{m.score}</b>
                      {m.comment ? ` · ${m.comment}` : ''}
                    </div>
                  )}
                </div>
              </div>
            ))}
            <div ref={bottomRef} />
          </div>

          {error && <div className="text-accent-600 text-sm">{error}</div>}

          <div className="flex items-center gap-2">
            <Button variant="ghost" onClick={listening ? stopListening : startListening} disabled={busy}>
              {listening ? '停止' : '🎤 发言'}
            </Button>
            <textarea
              className="flex-1 border border-brand-100 rounded-xl px-3 py-2 text-sm resize-none"
              rows={2}
              placeholder={listening ? '正在聆听…说完点「停止」' : '用英语说点什么，或点「发言」语音输入'}
              value={transcript}
              onChange={(e) => setTranscript(e.target.value)}
            />
            <Button onClick={send} disabled={busy || !transcript.trim()}>
              {busy ? '评分中…' : '发送'}
            </Button>
          </div>

          <div className="flex justify-end">
            <Button variant="ghost" onClick={end} disabled={busy}>
              结束对话并查看总评
            </Button>
          </div>
        </Card>
      )}

      {phase === 'ended' && report && (
        <Card className="space-y-4">
          <div className="text-center">
            <div className="text-sm text-gray-500">整体评分</div>
            <div className="text-5xl font-bold text-brand-700">{report.overallScore}</div>
            <div className="text-xs text-gray-400">共 {report.turns} 轮对话</div>
          </div>
          <p className="text-sm text-gray-700">{report.summary}</p>

          <div className="grid grid-cols-2 gap-3">
            <div className="rounded-2xl bg-green-50 p-3">
              <div className="text-sm font-semibold text-green-700 mb-1">亮点</div>
              <ul className="text-sm text-gray-700 list-disc list-inside space-y-1">
                {report.strengths.map((s, i) => (
                  <li key={i}>{s}</li>
                ))}
              </ul>
            </div>
            <div className="rounded-2xl bg-amber-50 p-3">
              <div className="text-sm font-semibold text-amber-700 mb-1">待加强</div>
              <ul className="text-sm text-gray-700 list-disc list-inside space-y-1">
                {report.weaknesses.map((s, i) => (
                  <li key={i}>{s}</li>
                ))}
              </ul>
            </div>
          </div>

          <div className="rounded-2xl bg-brand-50 p-3">
            <div className="text-sm font-semibold text-brand-700 mb-1">下一步强化建议</div>
            <ol className="text-sm text-gray-700 list-decimal list-inside space-y-1">
              {report.suggestions.map((s, i) => (
                <li key={i}>{s}</li>
              ))}
            </ol>
          </div>

          {tasks.length > 0 && (
            <div>
              <div className="text-sm font-semibold text-brand-700 mb-2">为你生成的强化练习任务</div>
              <div className="space-y-2">
                {tasks.map((t, i) => (
                  <div
                    key={i}
                    className="flex items-center gap-3 border border-brand-100 rounded-2xl p-3"
                  >
                    <div className="w-9 h-9 rounded-full bg-brand-100 text-brand-700 flex items-center justify-center font-bold">
                      {t.level}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="font-medium text-brand-800 truncate">{t.title}</div>
                      <div className="text-sm text-gray-500">{t.reason}</div>
                    </div>
                    <Button variant="ghost" onClick={() => navigate('/path?courseId=' + t.courseId)}>
                      去练习
                    </Button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <Button className="w-full" onClick={reset}>
            再聊一次
          </Button>
        </Card>
      )}
    </div>
  )
}

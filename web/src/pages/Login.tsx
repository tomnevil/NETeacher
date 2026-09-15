import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, wechatLogin } from '../api/user'
import { useAuthStore } from '../store/auth'
import Button from '../components/ui/Button'
import Input from '../components/ui/Input'
import Card from '../components/ui/Card'

export default function Login() {
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [wxLoading, setWxLoading] = useState(false)
  const [error, setError] = useState('')
  const nav = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)

  const onSubmit = async () => {
    setError('')
    if (!phone || !password) {
      setError('请填写手机号和密码')
      return
    }
    setLoading(true)
    try {
      const { data } = await login(phone, password)
      if (data.code === 0) {
        const u = data.data
        setAuth({ token: u.token, uid: u.uid, nickname: u.nickname, phone, role: u.role, grade: u.grade })
        nav(u.role === 'TEACHER' ? '/teacher' : '/home')
      } else {
        setError(data.message)
      }
    } catch {
      setError('网络异常，请确认后端已启动（默认 8080）')
    } finally {
      setLoading(false)
    }
  }

  const onWechat = async () => {
    setError('')
    setWxLoading(true)
    try {
      const { data } = await wechatLogin('demo')
      if (data.code === 0) {
        const u = data.data
        setAuth({ token: u.token, uid: u.uid, nickname: u.nickname, phone: '', role: u.role, grade: u.grade })
        nav('/home')
      } else {
        setError(data.message)
      }
    } catch {
      setError('微信登录失败，请稍后再试')
    } finally {
      setWxLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-b from-brand-100 to-brand-50">
      <Card className="w-full max-w-sm">
        <div className="text-3xl font-bold text-brand-700 mb-1">NETeacher</div>
        <p className="text-gray-500 mb-6">中小学英语 AI 学习平台 · 登录</p>
        <div className="space-y-3">
          <Input
            placeholder="手机号"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
          <Input
            type="password"
            placeholder="密码"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <p className="text-xs text-gray-400">
            学生演示：13800000000 / 123456
            <br />
            教师演示：13700000000 / 123456
          </p>
          {error && <div className="text-accent-600 text-sm">{error}</div>}
          <Button className="w-full" onClick={onSubmit} disabled={loading}>
            {loading ? '登录中…' : '登录'}
          </Button>
          <Button
            variant="ghost"
            className="w-full"
            onClick={onWechat}
            disabled={wxLoading}
          >
            {wxLoading ? '登录中…' : '微信一键登录（演示）'}
          </Button>
        </div>
      </Card>
    </div>
  )
}

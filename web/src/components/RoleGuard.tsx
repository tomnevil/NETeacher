import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

interface Props {
  /** 允许访问的角色；不传表示只要登录即可 */
  allow?: string[]
  children: React.ReactNode
}

/**
 * 角色路由守卫：未登录跳登录页；角色不符则跳回其应有的首页。
 */
export default function RoleGuard({ allow, children }: Props) {
  const token = useAuthStore((s) => s.token)
  const role = useAuthStore((s) => s.role)

  if (!token) {
    return <Navigate to="/login" replace />
  }
  if (allow && allow.length > 0 && !allow.includes(role ?? 'STUDENT')) {
    // 非授权角色 -> 送回各自首页
    return <Navigate to={role === 'TEACHER' ? '/teacher' : '/home'} replace />
  }
  return <>{children}</>
}

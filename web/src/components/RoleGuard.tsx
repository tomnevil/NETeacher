import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'
import { homePathOf } from '../utils/homePath'

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
    // 非授权角色 -> 送回其角色应有的首页（ADMIN 必须回 /admin，否则会与 /home 形成死循环）
    return <Navigate to={homePathOf(role)} replace />
  }
  return <>{children}</>
}

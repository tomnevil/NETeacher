import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

export interface ShellNavItem {
  to: string
  label: string
}

interface Props {
  /** 侧栏标题 */
  title: string
  navItems: ShellNavItem[]
  /** 移动端底部导航（取 navItems 中若干项） */
  bottomNav: string[]
  /** 侧栏渐变主题 */
  theme: 'student' | 'teacher'
}

const THEME = {
  student: {
    aside: 'from-sky-400 to-brand-600',
    active: 'bg-white/90 text-brand-700 shadow-soft',
    icon: '/assets/mascot.png'
  },
  teacher: {
    aside: 'from-emerald-500 to-teal-700',
    active: 'bg-white/90 text-teal-700 shadow-soft',
    icon: '/assets/mascot.png'
  }
}

/**
 * 通用外壳（AppShell）：学生端 / 老师端共用结构，通过 navItems + 主题区分。
 */
export default function AppShell({ title, navItems, bottomNav, theme }: Props) {
  const nickname = useAuthStore((s) => s.nickname)
  const role = useAuthStore((s) => s.role)
  const logout = useAuthStore((s) => s.logout)
  const nav = useNavigate()
  const t = THEME[theme]

  const onLogout = () => {
    logout()
    nav('/login')
  }

  return (
    <div className="min-h-screen flex flex-col md:flex-row bg-page">
      <aside
        className={`hidden md:flex md:flex-col w-60 bg-gradient-to-b ${t.aside} text-white shadow-soft p-5 gap-2`}
      >
        <div className="flex items-center gap-2 mb-6">
          <img
            src={t.icon}
            alt="吉祥物"
            className="w-10 h-10 rounded-full bg-white/20 p-1 animate-bounce-slight"
          />
          <div className="text-2xl font-bold">{title}</div>
        </div>
        {navItems.map((it) => (
          <NavLink
            key={it.to}
            to={it.to}
            className={({ isActive }) =>
              `px-4 py-2.5 rounded-2xl transition ${
                isActive ? t.active : 'text-white/85 hover:bg-white/15'
              }`
            }
          >
            {it.label}
          </NavLink>
        ))}
        <div className="mt-auto text-sm text-white/70">
          你好，{nickname ?? '同学'}
          {role === 'TEACHER' && <span className="ml-1 text-xs">（教师）</span>}
        </div>
        <button
          onClick={onLogout}
          className="text-left text-sm text-white/60 hover:text-white"
        >
          退出登录
        </button>
      </aside>

      <main className="flex-1 px-4 md:px-8 py-4 md:py-8 pb-24 md:pb-8 bg-page">
        <Outlet />
      </main>

      <nav className="md:hidden fixed bottom-0 inset-x-0 bg-white/95 backdrop-blur border-t border-brand-100 flex z-30 shadow-[0_-8px_24px_-12px_rgba(30,64,175,0.18)]">
        {navItems
          .filter((it) => bottomNav.includes(it.to))
          .map((it) => (
            <NavLink
              key={it.to}
              to={it.to}
              className={({ isActive }) =>
                `flex-1 py-3 text-center text-sm transition ${
                  isActive ? 'text-brand-600' : 'text-gray-500'
                }`
              }
            >
              {it.label}
            </NavLink>
          ))}
      </nav>
    </div>
  )
}

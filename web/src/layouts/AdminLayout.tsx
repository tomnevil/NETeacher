import AppShell from './AppShell'

const navItems = [{ to: '/admin', label: '运营看板' }]

const bottomNav = ['/admin']

/**
 * 运营后台布局：仅管理员可访问（路由层由 RoleGuard 的 ADMIN 限制）。
 */
export default function AdminLayout() {
  return <AppShell title="运营后台" navItems={navItems} bottomNav={bottomNav} theme="teacher" />
}

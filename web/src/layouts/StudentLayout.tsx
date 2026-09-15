import AppShell from './AppShell'

const navItems = [
  { to: '/home', label: '首页' },
  { to: '/map', label: '学习地图' },
  { to: '/special', label: '专项练习' },
  { to: '/assessment', label: '测评' },
  { to: '/wrong', label: '错题本' },
  { to: '/path', label: '学习路径' },
  { to: '/speaking', label: '口语' },
  { to: '/dialogue', label: '对话' },
  { to: '/records', label: '练习' },
  { to: '/progress', label: '仪表盘' },
  { to: '/parent', label: '家长' },
  { to: '/membership', label: '会员' }
]

const bottomNav = ['/home', '/map', '/special', '/dialogue', '/speaking']

export default function StudentLayout() {
  return (
    <AppShell title="NETeacher" navItems={navItems} bottomNav={bottomNav} theme="student" />
  )
}

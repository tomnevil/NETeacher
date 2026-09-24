import AppShell from './AppShell'

const navItems = [
  { to: '/teacher', label: '班级学情' },
  { to: '/teacher/admin', label: '班级管理' },
  { to: '/teacher/students', label: '学生名单' },
  { to: '/teacher/questions', label: '题库管理' },
  { to: '/teacher/papers', label: '组卷' },
  { to: '/teacher/assignments', label: '作业' }
]

const bottomNav = [
  '/teacher',
  '/teacher/admin',
  '/teacher/students',
  '/teacher/questions',
  '/teacher/papers',
  '/teacher/assignments'
]

/**
 * 老师端布局：独立的导航与主题色，避免与学生端混用。
 */
export default function TeacherLayout() {
  return (
    <AppShell title="教师工作台" navItems={navItems} bottomNav={bottomNav} theme="teacher" />
  )
}

/**
 * 各角色登录后的落地页。
 *
 * <p>必须有完整映射：若 ADMIN 落到学生端 `/home`，会被 StudentLayout 的
 * RoleGuard 判定为「非授权角色」并重定向回 `/home`，形成重定向死循环而渲染空白。</p>
 */
export function homePathOf(role?: string | null): string {
  if (role === 'ADMIN') return '/admin'
  if (role === 'TEACHER') return '/teacher'
  return '/home'
}

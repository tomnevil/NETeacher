import { create } from 'zustand'

interface AuthState {
  token: string | null
  uid: number | null
  nickname: string | null
  phone: string | null
  role: string | null
  grade: number | null
  setAuth: (a: { token: string; uid: number; nickname: string; phone?: string; role?: string; grade?: number }) => void
  logout: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  uid: localStorage.getItem('uid') ? Number(localStorage.getItem('uid')) : null,
  nickname: localStorage.getItem('nickname'),
  phone: localStorage.getItem('phone'),
  role: localStorage.getItem('role'),
  grade: localStorage.getItem('grade') ? Number(localStorage.getItem('grade')) : null,
  setAuth: (a) => {
    localStorage.setItem('token', a.token)
    localStorage.setItem('uid', String(a.uid))
    localStorage.setItem('nickname', a.nickname)
    if (a.phone) localStorage.setItem('phone', a.phone)
    if (a.role) localStorage.setItem('role', a.role)
    if (a.grade != null) localStorage.setItem('grade', String(a.grade))
    set({ token: a.token, uid: a.uid, nickname: a.nickname, phone: a.phone ?? null, role: a.role ?? null, grade: a.grade ?? null })
  },
  logout: () => {
    localStorage.removeItem('token')
    localStorage.removeItem('uid')
    localStorage.removeItem('nickname')
    localStorage.removeItem('phone')
    localStorage.removeItem('role')
    localStorage.removeItem('grade')
    set({ token: null, uid: null, nickname: null, phone: null, role: null, grade: null })
  }
}))

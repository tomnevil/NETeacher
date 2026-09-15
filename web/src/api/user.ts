import request from './request'
import type { Result } from './types'

export interface LoginResponse {
  token: string
  uid: number
  nickname: string
  role: string
  grade?: number
}

export interface UserInfo {
  uid: number
  phone?: string
  nickname?: string
  avatar?: string
  grade?: number
  role?: string
  status?: number
}

export function login(phone: string, password: string) {
  return request.post<Result<LoginResponse>>('/auth/login', { phone, password })
}

export function register(req: {
  phone: string
  password: string
  nickname?: string
  role?: string
  grade?: number
  parentPhone?: string
}) {
  return request.post<Result<UserInfo>>('/auth/register', req)
}

export function getMe() {
  return request.get<Result<UserInfo>>('/user/me')
}

export function wechatLogin(code: string) {
  return request.post<Result<LoginResponse>>('/auth/wechat', { code })
}

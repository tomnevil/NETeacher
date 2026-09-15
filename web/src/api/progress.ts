import request from './request'
import type { Result, ProgressDashboard, HomeToday, LevelInfo, CheckInStatus, ForbiddenHours } from './types'

/** 学习仪表盘 */
export function getDashboard() {
  return request.get<Result<ProgressDashboard>>('/progress/dashboard')
}

/** 首页聚合：今日任务 / 统计 / 打卡 / 星标 */
export function getHome() {
  return request.get<Result<HomeToday>>('/home/today')
}

/** 学习地图：L1-L6 关卡（星标 / 解锁 / 当前） */
export function getLevels() {
  return request.get<Result<LevelInfo[]>>('/home/map')
}

/** 打卡状态 */
export function getCheckIn() {
  return request.get<Result<CheckInStatus>>('/home/checkin')
}

/** 今日打卡 */
export function doCheckIn() {
  return request.post<Result<CheckInStatus>>('/home/checkin')
}

/** 防沉迷禁止时段（21:00 - 次日 08:00 不可学习） */
export function getForbiddenHours() {
  return request.get<Result<ForbiddenHours>>('/progress/forbidden-hours')
}

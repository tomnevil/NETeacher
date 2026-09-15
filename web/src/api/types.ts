export interface Result<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

export interface Course {
  id: number
  title: string
  cover?: string
  level?: number
  grade?: number | null
  category?: string
  topic?: string | null
  durationSec?: number
  description?: string
  lessonCount?: number
  tags?: string
  createdAt?: string
  updatedAt?: string
}

export interface QuizQuestion {
  id: number
  subject: string
  level: number
  type: string
  stem: string
  options: string[]
  analysis?: string
}

export interface WrongQuestion {
  questionId: number
  subject: string
  level: number
  stem: string
  options: string[]
  answer: string
  userAnswer: string
  explanation?: string
}

export interface AssessmentResult {
  id: number
  level: number
  subject: string
  type?: string
  score: number
  totalScore: number
  correctCount: number
  totalCount: number
  comment: string
  detail?: string
  wrongQuestions: WrongQuestion[]
}

export interface RecommendItem {
  courseId: number
  title: string
  category: string | null
  level: number
  reason: string
}

export interface RecommendPath {
  currentLevel: number
  nextLevel: number
  mastery: Record<string, number>
  items: RecommendItem[]
}

export interface ParentReport {
  child: {
    uid: number
    phone?: string
    nickname?: string
    avatar?: string
    grade?: number
    role?: string
    status?: number
  }
  assessmentCount: number
  recordCount: number
  mastery: Record<string, number>
  path: RecommendPath
  comment: string
}

export interface LearningRecord {
  id: number
  userId: number
  courseId: number | null
  lessonId?: number | null
  module?: string
  score?: number | null
  durationSec?: number | null
  finished?: boolean
  detail?: string | null
  createdAt: string
}

export interface ProgressDashboard {
  overallLevel: number
  mastery: Record<string, number>
  streakDays: number
  totalMinutes: number
  assessmentCount: number
  recordCount: number
  speakingAvg: number
  weakSubjects: string[]
  recentRecords: {
    id: number
    module: string
    score: number | null
    durationSec: number | null
    createdAt: string | null
  }[]
}

export interface MembershipPlan {
  id: number
  tier: string
  name: string
  priceMonths: number
  benefits: string
  level: number
}

export interface Membership {
  id: number
  userId: number
  plan: string
  expireAt?: string | null
  createdAt?: string
}

export interface SpeakingResult {
  score: number
  feedback: string
  transcript: string
  targetText: string
}

export interface DialogueStartResult {
  sessionId: string
  opening: string
  grade: number
  unit: string
}

export interface DialogueTurnResult {
  reply: string
  score: number
  fluency: number
  accuracy: number
  relevance: number
  comment: string
  turn: number
}

export interface DialogueEndResult {
  overallScore: number
  strengths: string[]
  weaknesses: string[]
  summary: string
  suggestions: string[]
  turns: number
}

export interface CheckInStatus {
  checkedToday: boolean
  streakDays: number
  totalDays: number
  week: boolean[]
}

export interface LevelInfo {
  lv: string
  name: string
  unlocked: boolean
  stars: number
  current: boolean
  courseCount: number
  completedCount: number
  firstCourseId?: number | null
  firstCourseTitle?: string | null
  /** 入学测评定级得到的初始级别 L1-L6（null 表示尚未定级） */
  initLevel?: number | null
}

export interface PlacementQuestion {
  id: string
  type: 'VOCAB' | 'LISTENING'
  prompt: string
  audioHint?: string | null
  options: string[]
  level: number
}

export interface PlacementAnswer {
  questionId: string
  selected: number
}

export interface PlacementSubmit {
  answers: PlacementAnswer[]
}

export interface PlacementResult {
  initLevel: number
  score: number
  total: number
  correct: number
  band: string
}

export interface TopicInfo {
  topic: string
  label: string
  count: number
}

export interface OrgClass {
  id: number
  name: string
  schoolId: number
  schoolName?: string | null
  grade: number
  headTeacherId?: number | null
  headTeacherName?: string | null
  studentCount: number
}

export interface OrgSchool {
  id: number
  name: string
  stage?: string | null
  city?: string | null
  classCount: number
  teacherCount: number
  studentCount: number
}

export interface OrgMember {
  uid: number
  nickname: string
  phone: string
  role: string
  grade?: number | null
  classId?: number | null
  className?: string | null
}

export interface StudentProgress {
  studentId: number
  nickname: string
  phone: string
  grade: number
  overallLevel: number
  speakingAvg: number
  totalMinutes: number
  streakDays: number
  completedCourses: number
  weakSubjects: string[]
  checkedToday: boolean
}

export interface ClassOverview {
  classId: number
  className: string
  schoolName?: string | null
  grade: number
  headTeacherName?: string | null
  studentCount: number
  avgSpeaking: number
  avgMinutes: number
  checkedTodayCount: number
  weakTopics: string[]
  students: StudentProgress[]
}

export interface TodayTask {
  id: string
  title: string
  type: 'course' | 'speaking' | 'quiz'
  level?: number
  progress: number
  starsReward: number
  category?: string
  to: string
}

export interface HomeToday {
  tasks: TodayTask[]
  stats: {
    speakingAvg: number
    streakDays: number
    totalMinutes: number
    totalStars: number
  }
  checkIn: CheckInStatus
  totalStars: number
}

export interface SpeakingTask {
  id: number
  refText: string
  translation: string
  tip: string
  words: string[]
  level: number
}

export interface PhonemeMark {
  text: string
  score: number
  status: 'good' | 'fair' | 'weak'
}

export interface SpeakingEvalResult {
  score: number
  accuracy: number
  fluency: number
  integrity: number
  feedback: string
  phonemes: PhonemeMark[]
  waveformRef: number[]
  waveformUser: number[]
}

export interface DimensionScore {
  dimension: string
  label: string
  score: number
}

export interface ForbiddenHours {
  forbiddenStart: number
  forbiddenEnd: number
}

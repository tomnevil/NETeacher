import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import {
  listSchools,
  listClasses,
  createClass,
  classStudents,
  bindStudent,
  unbindStudent,
  unassignedStudents,
  schoolTeachers,
  bindTeacher
} from '../api/org'
import type { OrgClass } from '../api/types'

export default function ClassAdmin() {
  const qc = useQueryClient()
  const [schoolId, setSchoolId] = useState<number | null>(null)
  const [activeClassId, setActiveClassId] = useState<number | null>(null)
  const [msg, setMsg] = useState('')
  const [form, setForm] = useState({ name: '', grade: 3, headTeacherId: '' })

  const { data: schools } = useQuery({
    queryKey: ['org-schools'],
    queryFn: () => listSchools().then((r) => r.data.data)
  })

  const { data: classes } = useQuery({
    queryKey: ['org-classes', schoolId],
    queryFn: () => listClasses(schoolId ? { schoolId } : undefined).then((r) => r.data.data)
  })

  const activeClass: OrgClass | undefined =
    classes?.find((c) => c.id === activeClassId) ?? classes?.[0]

  const { data: students } = useQuery({
    queryKey: ['class-students', activeClass?.id],
    queryFn: () => classStudents(activeClass!.id).then((r) => r.data.data),
    enabled: !!activeClass
  })

  const { data: unassigned } = useQuery({
    queryKey: ['unassigned-students'],
    queryFn: () => unassignedStudents().then((r) => r.data.data)
  })

  const { data: teachers } = useQuery({
    queryKey: ['school-teachers', activeClass?.schoolId ?? schoolId],
    queryFn: () =>
      schoolTeachers((activeClass?.schoolId ?? schoolId)!).then((r) => r.data.data),
    enabled: !!(activeClass?.schoolId ?? schoolId)
  })

  const flash = (m: string) => {
    setMsg(m)
    setTimeout(() => setMsg(''), 2500)
  }

  const refreshAll = () => {
    qc.invalidateQueries({ queryKey: ['org-classes'] })
    qc.invalidateQueries({ queryKey: ['class-students'] })
    qc.invalidateQueries({ queryKey: ['unassigned-students'] })
  }

  const onCreate = async () => {
    const targetSchool = activeClass?.schoolId ?? schoolId ?? schools?.[0]?.id
    if (!form.name.trim() || !targetSchool) {
      flash('请填写班级名并选择学校')
      return
    }
    await createClass({
      name: form.name.trim(),
      schoolId: targetSchool,
      grade: Number(form.grade),
      headTeacherId: form.headTeacherId ? Number(form.headTeacherId) : undefined
    })
    setForm({ name: '', grade: 3, headTeacherId: '' })
    flash('班级创建成功')
    refreshAll()
  }

  const onBindStudent = async (studentId: number) => {
    if (!activeClass) return
    await bindStudent(activeClass.id, studentId)
    flash('已绑定学生')
    refreshAll()
  }

  const onUnbind = async (studentId: number) => {
    await unbindStudent(studentId)
    flash('已移出班级')
    refreshAll()
  }

  const onBindTeacher = async (teacherId: number) => {
    if (!activeClass) return
    await bindTeacher(activeClass.id, teacherId)
    flash('已绑定教师')
    refreshAll()
  }

  return (
    <div className="space-y-4 pb-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-brand-800">班级管理</h1>
        {msg && (
          <span className="rounded-full bg-emerald-100 px-3 py-1 text-sm text-emerald-700">
            {msg}
          </span>
        )}
      </div>

      {/* 新建班级 */}
      <div className="rounded-3xl border border-brand-100 bg-white p-4 shadow-soft">
        <div className="mb-3 font-bold text-brand-800">新建班级</div>
        <div className="grid gap-3 sm:grid-cols-4">
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="班级名，如 三年级2班"
            className="rounded-2xl border border-brand-100 px-3 py-2 text-sm outline-none focus:border-brand-400"
          />
          <select
            value={schoolId ?? schools?.[0]?.id ?? ''}
            onChange={(e) => setSchoolId(Number(e.target.value))}
            className="rounded-2xl border border-brand-100 px-3 py-2 text-sm outline-none focus:border-brand-400"
          >
            {schools?.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <input
            type="number"
            min={1}
            max={12}
            value={form.grade}
            onChange={(e) => setForm({ ...form, grade: Number(e.target.value) })}
            placeholder="年级"
            className="rounded-2xl border border-brand-100 px-3 py-2 text-sm outline-none focus:border-brand-400"
          />
          <button
            onClick={onCreate}
            className="rounded-2xl bg-gradient-to-r from-emerald-400 to-teal-600 px-4 py-2 text-sm font-semibold text-white shadow-soft transition hover:shadow-lg active:scale-95"
          >
            创建
          </button>
        </div>
      </div>

      {/* 班级切换 */}
      <div className="flex flex-wrap gap-2">
        {classes?.map((c) => (
          <button
            key={c.id}
            onClick={() => setActiveClassId(c.id)}
            className={`rounded-2xl px-4 py-2 text-sm font-semibold shadow-soft transition active:scale-95 ${
              activeClass?.id === c.id
                ? 'bg-gradient-to-r from-sky-400 to-brand-600 text-white'
                : 'bg-white text-brand-700 hover:shadow-lg'
            }`}
          >
            {c.name} · {c.studentCount}人
          </button>
        ))}
      </div>

      {activeClass && (
        <div className="grid gap-4 md:grid-cols-2">
          {/* 班级学生 */}
          <div className="rounded-3xl border border-brand-100 bg-white p-4 shadow-soft">
            <div className="mb-3 font-bold text-brand-800">
              班级学生（{students?.length ?? 0}）
            </div>
            <div className="space-y-2">
              {students?.map((s) => (
                <div
                  key={s.uid}
                  className="flex items-center justify-between rounded-2xl bg-brand-50/60 px-3 py-2"
                >
                  <div>
                    <div className="font-semibold text-brand-800">{s.nickname}</div>
                    <div className="text-xs text-ink-soft">{s.phone}</div>
                  </div>
                  <button
                    onClick={() => onUnbind(s.uid)}
                    className="rounded-full bg-white px-3 py-1 text-xs text-rose-600 shadow transition hover:shadow-md"
                  >
                    移出
                  </button>
                </div>
              ))}
              {students?.length === 0 && (
                <div className="py-4 text-center text-sm text-ink-soft">暂无学生</div>
              )}
            </div>
          </div>

          {/* 未分配学生 */}
          <div className="rounded-3xl border border-brand-100 bg-white p-4 shadow-soft">
            <div className="mb-3 font-bold text-brand-800">待分配学生</div>
            <div className="space-y-2">
              {unassigned?.map((s) => (
                <div
                  key={s.uid}
                  className="flex items-center justify-between rounded-2xl bg-amber-50 px-3 py-2"
                >
                  <div>
                    <div className="font-semibold text-brand-800">{s.nickname}</div>
                    <div className="text-xs text-ink-soft">{s.phone}</div>
                  </div>
                  <button
                    onClick={() => onBindStudent(s.uid)}
                    className="rounded-full bg-gradient-to-r from-sky-400 to-brand-600 px-3 py-1 text-xs text-white shadow transition hover:shadow-md"
                  >
                    加入本班
                  </button>
                </div>
              ))}
              {unassigned?.length === 0 && (
                <div className="py-4 text-center text-sm text-ink-soft">没有待分配学生</div>
              )}
            </div>
          </div>

          {/* 任课教师 */}
          <div className="rounded-3xl border border-brand-100 bg-white p-4 shadow-soft md:col-span-2">
            <div className="mb-3 font-bold text-brand-800">任课教师</div>
            <div className="flex flex-wrap gap-2">
              {teachers?.map((t) => (
                <button
                  key={t.uid}
                  onClick={() => onBindTeacher(t.uid)}
                  className="rounded-2xl bg-teal-50 px-3 py-2 text-sm text-teal-700 transition hover:bg-teal-100"
                >
                  {t.nickname}
                  <span className="ml-2 text-xs text-teal-500">点击绑定到本班</span>
                </button>
              ))}
              {teachers?.length === 0 && (
                <div className="text-sm text-ink-soft">该校暂无教师</div>
              )}
            </div>
          </div>
        </div>
      )}

      {classes?.length === 0 && (
        <div className="rounded-3xl bg-white p-8 text-center text-sm text-ink-soft shadow-soft">
          暂无班级，请先在上方创建。
        </div>
      )}
    </div>
  )
}

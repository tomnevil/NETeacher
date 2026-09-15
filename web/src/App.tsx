import { Routes, Route, Navigate } from 'react-router-dom'
import StudentLayout from './layouts/StudentLayout'
import TeacherLayout from './layouts/TeacherLayout'
import RoleGuard from './components/RoleGuard'
import Login from './pages/Login'
import Home from './pages/Home'
import LearningMap from './pages/LearningMap'
import Assessment from './pages/Assessment'
import LearningPath from './pages/LearningPath'
import LearningRecords from './pages/LearningRecords'
import Parent from './pages/Parent'
import WrongBook from './pages/WrongBook'
import Speaking from './pages/Speaking'
import Dialogue from './pages/Dialogue'
import Progress from './pages/Progress'
import Membership from './pages/Membership'
import SpecialTraining from './pages/SpecialTraining'
import TeacherDashboard from './pages/TeacherDashboard'
import ClassAdmin from './pages/ClassAdmin'
import StudentRoster from './pages/StudentRoster'
import NotFound from './pages/NotFound'
import Placement from './pages/Placement'
import Exercise from './pages/Exercise'
import QuestionBank from './pages/QuestionBank'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      {/* 老师端：需 TEACHER 角色 */}
      <Route
        path="/teacher"
        element={
          <RoleGuard allow={['TEACHER']}>
            <TeacherLayout />
          </RoleGuard>
        }
      >
        <Route index element={<TeacherDashboard />} />
        <Route path="admin" element={<ClassAdmin />} />
        <Route path="students" element={<StudentRoster />} />
        <Route path="questions" element={<QuestionBank />} />
      </Route>

      {/* 学生端 */}
      <Route
        path="/"
        element={
          <RoleGuard allow={['STUDENT', 'PARENT', 'TEACHER']}>
            <StudentLayout />
          </RoleGuard>
        }
      >
        <Route index element={<Navigate to="/home" replace />} />
        <Route path="home" element={<Home />} />
        <Route path="map" element={<LearningMap />} />
        <Route path="placement" element={<Placement />} />
        <Route path="exercise" element={<Exercise />} />
        <Route path="assessment" element={<Assessment />} />
        <Route path="wrong" element={<WrongBook />} />
        <Route path="path" element={<LearningPath />} />
        <Route path="records" element={<LearningRecords />} />
        <Route path="parent" element={<Parent />} />
        <Route path="speaking" element={<Speaking />} />
        <Route path="dialogue" element={<Dialogue />} />
        <Route path="progress" element={<Progress />} />
        <Route path="membership" element={<Membership />} />
        <Route path="special" element={<SpecialTraining />} />
      </Route>

      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

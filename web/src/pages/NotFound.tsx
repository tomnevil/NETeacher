import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-3">
      <div className="text-4xl font-bold text-brand-700">404</div>
      <Link to="/home" className="text-accent-500">
        返回首页
      </Link>
    </div>
  )
}

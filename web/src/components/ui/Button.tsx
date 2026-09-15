import { ButtonHTMLAttributes } from 'react'

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'ghost' | 'accent'
}

export default function Button({ variant = 'primary', className = '', ...rest }: Props) {
  const base =
    'inline-flex items-center justify-center rounded-full px-5 min-h-[44px] font-medium transition-all duration-200 disabled:opacity-50 disabled:pointer-events-none active:scale-95'
  const styles =
    variant === 'primary'
      ? 'bg-gradient-to-br from-sky-400 to-brand-600 text-white shadow-soft hover:shadow-lg hover:-translate-y-0.5'
      : variant === 'accent'
        ? 'bg-gradient-to-br from-accent-400 to-accent-500 text-white shadow-soft hover:shadow-lg hover:-translate-y-0.5'
        : 'bg-white text-brand-700 border border-brand-200 hover:bg-brand-50 hover:border-brand-300'
  return (
    <button className={`${base} ${styles} ${className}`} {...rest} />
  )
}

import { InputHTMLAttributes } from 'react'

interface Props extends InputHTMLAttributes<HTMLInputElement> {}

export default function Input({ className = '', ...rest }: Props) {
  return (
    <input
      className={`w-full rounded-2xl border border-brand-200 px-4 py-2.5 outline-none focus:border-brand-400 focus:ring-2 focus:ring-brand-100 ${className}`}
      {...rest}
    />
  )
}

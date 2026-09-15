import { HTMLAttributes, ReactNode } from 'react'

interface Props extends HTMLAttributes<HTMLDivElement> {
  children: ReactNode
}

export default function Card({ className = '', children, ...rest }: Props) {
  return (
    <div
      className={`bg-white rounded-3xl shadow-soft border border-brand-100/70 p-5 animate-pop-in ${className}`}
      {...rest}
    >
      {children}
    </div>
  )
}

interface RadarItem {
  label: string
  score: number
}

export default function RadarChart({ data, max = 100 }: { data: RadarItem[]; max?: number }) {
  const size = 260
  const cx = size / 2
  const cy = size / 2
  const r = size / 2 - 38
  const n = data.length
  const angle = (i: number) => (Math.PI * 2 * i) / n - Math.PI / 2
  const point = (i: number, val: number): [number, number] => {
    const rr = (val / max) * r
    return [cx + rr * Math.cos(angle(i)), cy + rr * Math.sin(angle(i))]
  }
  const grid = (f: number) => data.map((_, i) => point(i, f * max).join(',')).join(' ')

  return (
    <svg width={size} height={size} className="max-w-full">
      {[0.25, 0.5, 0.75, 1].map((f) => (
        <polygon key={f} points={grid(f)} fill="none" stroke="#e5e7eb" />
      ))}
      {data.map((_, i) => {
        const [x, y] = point(i, max)
        return <line key={i} x1={cx} y1={cy} x2={x} y2={y} stroke="#e5e7eb" />
      })}
      <polygon
        points={data.map((d, i) => point(i, d.score).join(',')).join(' ')}
        fill="rgba(59,130,246,0.35)"
        stroke="#3b82f6"
        strokeWidth={2}
      />
      {data.map((d, i) => {
        const [x, y] = point(i, d.score)
        return (
          <text key={'v' + i} x={x} y={y - 4} textAnchor="middle" fontSize={10} fill="#1d4ed8">
            {d.score}
          </text>
        )
      })}
      {data.map((d, i) => {
        const [x, y] = point(i, max * 1.2)
        return (
          <text key={'l' + i} x={x} y={y} textAnchor="middle" fontSize={11} fill="#475569">
            {d.label}
          </text>
        )
      })}
    </svg>
  )
}

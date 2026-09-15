/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // 主色 Primary：蓝 #3B82F6 / 天蓝 #0EA5E9（规范 §2）
        brand: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a'
        },
        // 天蓝（主色第二色），用于渐变
        sky: {
          400: '#0ea5e9',
          500: '#0284c7'
        },
        // 强调 Accent：琥珀 #F59E0B（积分/激励/关键 CTA）
        accent: {
          300: '#fcd34d',
          400: '#fbbf24',
          500: '#f59e0b',
          600: '#d97706'
        },
        // 页面背景 / 文本（规范 §2）
        page: '#F7F9FC',
        ink: {
          DEFAULT: '#1F2937',
          soft: '#6B7280'
        }
      },
      fontFamily: {
        rounded: ['"Baloo 2"', '"PingFang SC"', 'sans-serif']
      },
      boxShadow: {
        // 柔和投影（规范 §1）
        soft: '0 12px 32px -12px rgba(30, 64, 175, 0.18)',
        card: '0 10px 30px -10px rgba(15, 23, 42, 0.12)'
      },
      keyframes: {
        // 微动效（规范 §1 / §5）
        'pop-in': {
          '0%': { opacity: '0', transform: 'scale(0.96) translateY(6px)' },
          '100%': { opacity: '1', transform: 'scale(1) translateY(0)' }
        },
        float: {
          '0%,100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-6px)' }
        },
        'bounce-slight': {
          '0%,100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-3px)' }
        },
        // 关卡节点光晕呼吸（当前关卡）
        'pulse-ring': {
          '0%': { transform: 'scale(0.9)', opacity: '0.7' },
          '70%': { transform: 'scale(1.35)', opacity: '0' },
          '100%': { transform: 'scale(1.35)', opacity: '0' }
        },
        // 点击节点弹出
        'node-pop': {
          '0%': { transform: 'scale(1)' },
          '45%': { transform: 'scale(1.12)' },
          '100%': { transform: 'scale(1)' }
        },
        // 星星闪耀
        twinkle: {
          '0%,100%': { opacity: '1', transform: 'scale(1)' },
          '50%': { opacity: '0.5', transform: 'scale(0.85)' }
        },
        // 路径虚线流动
        'dash-flow': {
          to: { strokeDashoffset: '-24' }
        }
      },
      animation: {
        'pop-in': 'pop-in 0.35s ease-out both',
        float: 'float 3s ease-in-out infinite',
        'bounce-slight': 'bounce-slight 1.6s ease-in-out infinite',
        'pulse-ring': 'pulse-ring 2s ease-out infinite',
        'node-pop': 'node-pop 0.4s ease-out',
        twinkle: 'twinkle 1.8s ease-in-out infinite',
        'dash-flow': 'dash-flow 1s linear infinite'
      }
    }
  },
  plugins: []
}

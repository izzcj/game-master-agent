import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{vue,ts}'],
  theme: {
    extend: {
      colors: {
        canvas: '#020617',
        panel: '#0f172a',
        accent: '#f97316',
        ink: '#e2e8f0',
      },
      boxShadow: {
        glow: '0 0 0 1px rgba(249, 115, 22, 0.15), 0 12px 40px rgba(15, 23, 42, 0.55)',
      },
      fontFamily: {
        sans: ['"Segoe UI"', '"PingFang SC"', '"Hiragino Sans GB"', 'sans-serif'],
      },
    },
  },
  plugins: [],
} satisfies Config

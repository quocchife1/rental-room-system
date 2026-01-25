// tailwind.config.js
module.exports = {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        'summer-primary': '#2DD4BF',   // Turquoise
        'summer-secondary': '#F97316', // Coral
        'summer-accent': '#FACC15',    // Lemon
        'summer-bg': '#F6FFFE',        // Cool near-white
      },
      keyframes: {
        float: {
          '0%, 100%': { transform: 'translate3d(0, 0, 0)' },
          '50%': { transform: 'translate3d(0, -10px, 0)' },
        },
        sway: {
          '0%, 100%': { transform: 'rotate(-4deg)' },
          '50%': { transform: 'rotate(4deg)' },
        },
        sunrays: {
          '0%': { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(360deg)' },
        },
        swing: {
          '0%, 100%': { transform: 'rotate(-10deg)' },
          '50%': { transform: 'rotate(10deg)' },
        },
        'wave-drift': {
          '0%': { transform: 'translate3d(0, 0, 0)' },
          '100%': { transform: 'translate3d(-50%, 0, 0)' },
        },
        'wave-drift-rev': {
          '0%': { transform: 'translate3d(-50%, 0, 0)' },
          '100%': { transform: 'translate3d(0, 0, 0)' },
        },
        'fly-across': {
          '0%': { transform: 'translate3d(-10vw, 10px, 0)', opacity: 0 },
          '10%': { opacity: 1 },
          '90%': { opacity: 1 },
          '100%': { transform: 'translate3d(100vw, -20px, 0)', opacity: 0 },
        },
        scanline: {
          '0%': { transform: 'translate3d(-70vw, 0, 0)' },
          '100%': { transform: 'translate3d(170vw, 0, 0)' },
        },
        twinkle: {
          '0%, 100%': { opacity: 0.65, transform: 'translate3d(0, 0, 0)' },
          '50%': { opacity: 1, transform: 'translate3d(0, -1px, 0)' },
        },
      },
      animation: {
        'float-slow': 'float 6s ease-in-out infinite',
        'float-slower': 'float 9s ease-in-out infinite',
        'sway': 'sway 2.5s ease-in-out infinite',
        'sway-slow': 'sway 4s ease-in-out infinite',
        'sunrays': 'sunrays 14s linear infinite',
        'swing': 'swing 3s ease-in-out infinite',
        'wave-drift': 'wave-drift 14s linear infinite', 
        'wave-drift-rev': 'wave-drift-rev 20s linear infinite', 
        'fly-across': 'fly-across 25s linear infinite', 
        'scanline': 'scanline 900ms ease-in-out 1 both',
        'twinkle': 'twinkle 1.8s ease-in-out infinite',
      },
    },
  },
  plugins: []
}
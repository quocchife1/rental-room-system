import React from 'react';

// Cành lá rủ xuống (Dùng cho góc trên trái và phải)
export const HangingBranch = ({ className, flip = false }) => (
  <svg
    viewBox="0 0 200 150"
    className={className}
    fill="none"
    style={flip ? { transform: 'scaleX(-1)' } : {}}
    xmlns="http://www.w3.org/2000/svg"
  >
    {/* Cuống lá chính */}
    <path
      d="M0 0 C 40 10, 80 40, 120 100"
      stroke="#15803d"
      strokeWidth="4"
      fill="none"
      strokeLinecap="round"
    />

    {/* Các lá con */}
    <path d="M20 5 C 30 20, 30 40, 25 50" stroke="#16a34a" strokeWidth="6" strokeLinecap="round" />
    <path d="M40 15 C 55 35, 60 60, 50 70" stroke="#22c55e" strokeWidth="6" strokeLinecap="round" />
    <path d="M60 25 C 80 50, 90 80, 80 90" stroke="#16a34a" strokeWidth="6" strokeLinecap="round" />
    <path d="M80 40 C 100 60, 110 90, 100 110" stroke="#22c55e" strokeWidth="6" strokeLinecap="round" />
    <path d="M100 60 C 115 80, 120 100, 115 110" stroke="#15803d" strokeWidth="5" strokeLinecap="round" />
  </svg>
);

// Giữ lại Chim hải âu
export const Seagull = ({ className }) => (
  <svg
    viewBox="0 0 100 50"
    className={className}
    fill="none"
    stroke="currentColor"
    strokeWidth="3"
    strokeLinecap="round"
  >
    <path d="M10 20 Q 30 5 50 20 Q 70 5 90 20" />
  </svg>
);

// Giữ lại Mặt trời
export const SunGraphic = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className}>
    <circle cx="50" cy="50" r="22" fill="#FCD34D" />
    <g stroke="#FCD34D" strokeWidth="4" strokeLinecap="round">
      <line x1="50" y1="10" x2="50" y2="18" />
      <line x1="50" y1="82" x2="50" y2="90" />
      <line x1="10" y1="50" x2="18" y2="50" />
      <line x1="82" y1="50" x2="90" y2="50" />
      <line x1="22" y1="22" x2="28" y2="28" />
      <line x1="72" y1="72" x2="78" y2="78" />
      <line x1="22" y1="78" x2="28" y2="72" />
      <line x1="72" y1="28" x2="78" y2="22" />
    </g>
  </svg>
);

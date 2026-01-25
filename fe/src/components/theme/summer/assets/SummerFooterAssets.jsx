import React, { useId } from 'react';

// Existing beach icons
export const Starfish = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className} fill="currentColor" stroke="none" aria-hidden="true">
    <path
      d="M50 0L63 35L100 38L71 60L80 95L50 75L20 95L29 60L0 38L37 35L50 0Z"
      strokeLinejoin="round"
      strokeWidth="5"
      style={{ filter: 'drop-shadow(2px 4px 6px rgba(0,0,0,0.1))' }}
    />
    <circle cx="50" cy="50" r="3" fill="rgba(0,0,0,0.1)" />
    <circle cx="50" cy="30" r="2" fill="rgba(0,0,0,0.1)" />
    <circle cx="70" cy="50" r="2" fill="rgba(0,0,0,0.1)" />
    <circle cx="50" cy="70" r="2" fill="rgba(0,0,0,0.1)" />
    <circle cx="30" cy="50" r="2" fill="rgba(0,0,0,0.1)" />
  </svg>
);

export const Shell = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className} fill="currentColor" stroke="none" aria-hidden="true">
    <path
      d="M20 80C20 80 50 100 80 80C90 70 95 50 80 30C70 15 50 10 35 25C25 35 30 50 45 55C55 60 65 50 60 40"
      stroke="currentColor"
      strokeWidth="6"
      strokeLinecap="round"
      fill="none"
      style={{ filter: 'drop-shadow(2px 4px 4px rgba(0,0,0,0.1))' }}
    />
  </svg>
);

export const Footprints = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className} fill="currentColor" aria-hidden="true">
    <ellipse cx="30" cy="60" rx="10" ry="18" transform="rotate(-10 30 60)" opacity="0.6" />
    <circle cx="20" cy="38" r="2.5" opacity="0.6" />
    <circle cx="26" cy="35" r="2.5" opacity="0.6" />
    <circle cx="32" cy="36" r="2.5" opacity="0.6" />
    <circle cx="38" cy="39" r="2.5" opacity="0.6" />

    <ellipse cx="70" cy="30" rx="10" ry="18" transform="rotate(10 70 30)" opacity="0.6" />
    <circle cx="80" cy="8" r="2.5" opacity="0.6" />
    <circle cx="74" cy="5" r="2.5" opacity="0.6" />
    <circle cx="68" cy="6" r="2.5" opacity="0.6" />
    <circle cx="62" cy="9" r="2.5" opacity="0.6" />
  </svg>
);

export const Crab = ({ className }) => (
  <svg viewBox="0 0 100 60" className={className} fill="currentColor" aria-hidden="true">
    <path d="M20 30 Q 10 20 15 10 Q 25 15 30 25" stroke="currentColor" strokeWidth="3" fill="none" />
    <path d="M80 30 Q 90 20 85 10 Q 75 15 70 25" stroke="currentColor" strokeWidth="3" fill="none" />
    <ellipse cx="50" cy="35" rx="30" ry="18" />
    <circle cx="40" cy="25" r="3" fill="white" />
    <circle cx="60" cy="25" r="3" fill="white" />
    <circle cx="40" cy="25" r="1" fill="black" />
    <circle cx="60" cy="25" r="1" fill="black" />
    <path d="M25 40 L10 45 M25 45 L10 50 M25 50 L15 55" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    <path d="M75 40 L90 45 M75 45 L90 50 M75 50 L85 55" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

// New Summer footer objects
export const BeachUmbrella = ({ className }) => (
  <svg
    viewBox="0 0 100 100"
    className={className}
    fill="none"
    aria-hidden="true"
    style={{ filter: 'drop-shadow(2px 4px 6px rgba(0,0,0,0.15))' }}
  >
    <path d="M50 95 L50 40" stroke="#92400E" strokeWidth="3" strokeLinecap="round" />
    <path d="M10 45 Q 50 10 90 45" fill="#EF4444" />
    <path d="M30 45 Q 50 10 70 45" fill="#F0F9FF" />
    <path d="M42 45 Q 50 10 58 45" fill="#EF4444" />
    <circle cx="50" cy="28" r="1.5" fill="#FCD34D" />
  </svg>
);

export const Surfboard = ({ className }) => (
  <svg
    viewBox="0 0 40 100"
    className={className}
    fill="none"
    aria-hidden="true"
    style={{ filter: 'drop-shadow(3px 5px 4px rgba(0,0,0,0.2))' }}
  >
    <path
      d="M20 5 C 35 20, 35 80, 20 95 C 5 80, 5 20, 20 5"
      fill="#06B6D4"
      stroke="#155E75"
      strokeWidth="1"
    />
    <path d="M20 5 L 20 95" stroke="#ECFEFF" strokeWidth="3" opacity="0.5" />
    <path d="M15 30 L 25 30" stroke="#ECFEFF" strokeWidth="2" opacity="0.8" />
    <circle cx="20" cy="20" r="3" fill="#FACC15" />
  </svg>
);

// Summer Vibes collection (creative, detailed, chill)

// 1. Surf Van
export const SurfVan = ({ className }) => {
  const rawId = useId();
  const uid = String(rawId).replace(/:/g, '');
  const vanGradId = `vanGrad-${uid}`;

  return (
    <svg viewBox="0 0 120 100" className={className} fill="none" aria-hidden="true">
      <defs>
        <linearGradient id={vanGradId} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#22D3EE" />
          <stop offset="50%" stopColor="#22D3EE" />
          <stop offset="50%" stopColor="#ECFEFF" />
          <stop offset="100%" stopColor="#ECFEFF" />
        </linearGradient>
      </defs>

      {/* Surfboard on roof */}
      <path d="M20 35 L 100 35" stroke="#FCD34D" strokeWidth="6" strokeLinecap="round" />

      {/* Van body */}
      <path
        d="M20 40 L 100 40 Q 110 40 110 60 L 110 80 L 10 80 L 10 60 Q 10 40 20 40"
        fill={`url(#${vanGradId})`}
        stroke="#0E7490"
        strokeWidth="2"
      />

      {/* Windows */}
      <rect x="25" y="45" width="25" height="15" fill="#3B82F6" rx="2" opacity="0.6" />
      <rect x="55" y="45" width="25" height="15" fill="#3B82F6" rx="2" opacity="0.6" />

      {/* Wheels */}
      <circle cx="35" cy="80" r="10" fill="#1E293B" />
      <circle cx="35" cy="80" r="5" fill="#94A3B8" />
      <circle cx="85" cy="80" r="10" fill="#1E293B" />
      <circle cx="85" cy="80" r="5" fill="#94A3B8" />
    </svg>
  );
};

// 2. Chill Coconut (with animated umbrella)
export const ChillCoconut = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className} fill="none" aria-hidden="true">
    <style>
      {`
        .umbrella { animation: sway 3s infinite ease-in-out; transform-origin: 50% 100%; }
        @keyframes sway {
          0%, 100% { transform: rotate(-5deg); }
          50% { transform: rotate(5deg); }
        }
      `}
    </style>

    {/* Coconut */}
    <path
      d="M25 40 Q 10 60 25 80 Q 50 100 75 80 Q 90 60 75 40 Q 50 50 25 40"
      fill="#5D4037"
    />
    <ellipse cx="50" cy="45" rx="25" ry="10" fill="#FFF7ED" />

    {/* Coconut water */}
    <path d="M35 45 Q 50 55 65 45" stroke="#38BDF8" strokeWidth="3" fill="none" opacity="0.5" />

    {/* Straw */}
    <path d="M45 50 L 30 10" stroke="#F472B6" strokeWidth="4" strokeLinecap="round" />

    {/* Umbrella (animated) */}
    <g transform="translate(60, 20)" className="umbrella">
      <path d="M0 30 L 0 0" stroke="#FCD34D" strokeWidth="2" />
      <path d="M-15 10 Q 0 -5 15 10 Z" fill="#EF4444" />
      <path d="M-10 10 L 0 0 L 10 10" stroke="white" strokeWidth="1" fill="none" />
    </g>

    {/* Plumeria */}
    <circle cx="70" cy="75" r="8" fill="#FEF08A" />
    <circle cx="70" cy="75" r="3" fill="#F59E0B" />
  </svg>
);

// 3. Message in a Bottle (floating)
export const MessageBottle = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className} fill="none" aria-hidden="true">
    <style>
      {`
        .bottle { animation: float 4s infinite ease-in-out; }
        @keyframes float {
          0%, 100% { transform: translateY(0) rotate(5deg); }
          50% { transform: translateY(-5px) rotate(-5deg); }
        }
      `}
    </style>

    <g className="bottle" style={{ transformOrigin: '50% 50%' }}>
      {/* Glass bottle */}
      <path
        d="M35 30 L 35 80 Q 35 95 50 95 Q 65 95 65 80 L 65 30 Q 65 20 50 20 Q 35 20 35 30"
        fill="#A5F3FC"
        stroke="#0891B2"
        strokeWidth="2"
        opacity="0.6"
      />

      {/* Cork */}
      <rect x="42" y="12" width="16" height="8" fill="#78350F" rx="1" />

      {/* Note */}
      <path d="M42 40 L 58 40 L 58 70 L 42 70 Z" fill="#FEF3C7" />
      <path d="M44 45 L 56 45 M 44 50 L 54 50 M 44 55 L 56 55" stroke="#92400E" strokeWidth="1" />
      <circle cx="50" cy="65" r="3" fill="#DC2626" opacity="0.5" />

      {/* Highlight */}
      <path d="M40 35 L 40 80" stroke="white" strokeWidth="2" strokeLinecap="round" opacity="0.4" />
    </g>

    {/* Water wave */}
    <path
      d="M10 85 Q 30 80 50 90 T 90 85"
      stroke="#3B82F6"
      strokeWidth="2"
      strokeLinecap="round"
      opacity="0.3"
    />
  </svg>
);

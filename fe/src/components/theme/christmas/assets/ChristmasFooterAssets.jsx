import React, { useId } from 'react';

export const Snowman = ({ className }) => (
  <svg
    viewBox="0 0 100 120"
    className={className}
    fill="none"
    aria-hidden="true"
    style={{ filter: 'drop-shadow(2px 4px 6px rgba(0,0,0,0.15))' }}
  >
    <circle cx="50" cy="90" r="25" fill="#F8FAFC" stroke="#E2E8F0" strokeWidth="1" />
    <circle cx="50" cy="55" r="18" fill="#F8FAFC" stroke="#E2E8F0" strokeWidth="1" />
    <circle cx="50" cy="28" r="14" fill="#F8FAFC" stroke="#E2E8F0" strokeWidth="1" />

    <path d="M40 38 Q 50 45 60 38" stroke="#DC2626" strokeWidth="6" strokeLinecap="round" />
    <path d="M55 40 L 60 60" stroke="#DC2626" strokeWidth="6" strokeLinecap="round" />

    <rect x="35" y="14" width="30" height="4" fill="#1E293B" rx="1" />
    <rect x="40" y="2" width="20" height="14" fill="#1E293B" rx="1" />
    <rect x="40" y="12" width="20" height="2" fill="#DC2626" />

    <circle cx="45" cy="25" r="1.5" fill="#1E293B" />
    <circle cx="55" cy="25" r="1.5" fill="#1E293B" />
    <path d="M50 28 L 60 30 L 50 32" fill="#F97316" />

    <path d="M32 55 L 15 45" stroke="#78350F" strokeWidth="2" strokeLinecap="round" />
    <path d="M68 55 L 85 45" stroke="#78350F" strokeWidth="2" strokeLinecap="round" />
  </svg>
);

export const ChristmasTree = ({ className }) => (
  <svg
    viewBox="0 0 100 120"
    className={className}
    fill="none"
    aria-hidden="true"
    style={{ filter: 'drop-shadow(2px 4px 6px rgba(0,0,0,0.2))' }}
  >
    <rect x="44" y="100" width="12" height="15" fill="#78350F" />

    <path d="M20 100 L 50 60 L 80 100 Z" fill="#15803D" />
    <path d="M25 75 L 50 40 L 75 75 Z" fill="#16A34A" />
    <path d="M30 50 L 50 20 L 70 50 Z" fill="#22C55E" />

    <path d="M50 10 L 53 18 L 62 18 L 55 24 L 58 32 L 50 27 L 42 32 L 45 24 L 38 18 L 47 18 Z" fill="#FACC15" />

    <circle cx="40" cy="85" r="3" fill="#DC2626" />
    <circle cx="60" cy="70" r="3" fill="#FACC15" />
    <circle cx="45" cy="55" r="3" fill="#3B82F6" />
    <circle cx="55" cy="35" r="2.5" fill="#DC2626" />
  </svg>
);

export const GiftBox = ({ className }) => (
  <svg
    viewBox="0 0 60 60"
    className={className}
    fill="none"
    aria-hidden="true"
    style={{ filter: 'drop-shadow(2px 2px 4px rgba(0,0,0,0.15))' }}
  >
    <rect x="10" y="20" width="40" height="35" fill="#DC2626" rx="2" />
    <rect x="26" y="20" width="8" height="35" fill="#FACC15" />
    <rect x="8" y="15" width="44" height="10" fill="#B91C1C" rx="1" />
    <path
      d="M30 15 C 30 5, 10 5, 30 15 C 50 5, 30 5, 30 15"
      stroke="#FACC15"
      strokeWidth="4"
      fill="none"
    />
  </svg>
);

// Winter Magic collection (cute, cozy, animated)

// 1. Snow Globe - animated snow inside
export const MagicSnowGlobe = ({ className }) => {
  const rawId = useId();
  const uid = String(rawId).replace(/:/g, '');
  const glassGradId = `glassGrad-${uid}`;
  const baseGradId = `baseGrad-${uid}`;

  return (
    <svg viewBox="0 0 100 120" className={className} fill="none" aria-hidden="true">
      <defs>
        <linearGradient id={glassGradId} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#E0F2FE" stopOpacity="0.4" />
          <stop offset="50%" stopColor="#BAE6FD" stopOpacity="0.1" />
          <stop offset="100%" stopColor="#7DD3FC" stopOpacity="0.3" />
        </linearGradient>
        <radialGradient id={baseGradId} cx="0.5" cy="0.5" r="0.5">
          <stop offset="0%" stopColor="#B45309" />
          <stop offset="100%" stopColor="#78350F" />
        </radialGradient>
        <style>
          {`
            .flake { animation: fall infinite linear; }
            @keyframes fall {
              0% { transform: translateY(-10px); opacity: 0; }
              50% { opacity: 1; }
              100% { transform: translateY(20px); opacity: 0; }
            }
          `}
        </style>
      </defs>

      {/* Wooden base */}
      <path d="M20 90 L 80 90 L 85 110 L 15 110 Z" fill={`url(#${baseGradId})`} />
      <rect x="15" y="110" width="70" height="5" fill="#451A03" rx="2" />

      {/* Tree inside */}
      <path d="M50 35 L 35 60 L 65 60 Z" fill="#16A34A" />
      <path d="M50 50 L 30 80 L 70 80 Z" fill="#15803D" />
      <rect x="47" y="80" width="6" height="10" fill="#451A03" />

      {/* Glass globe */}
      <circle cx="50" cy="55" r="40" fill={`url(#${glassGradId})`} stroke="#E0F2FE" strokeWidth="2" />

      {/* Snow falling (animated) */}
      <circle
        cx="40"
        cy="40"
        r="1.5"
        fill="white"
        className="flake"
        style={{ animationDuration: '2s', animationDelay: '0s' }}
      />
      <circle
        cx="60"
        cy="30"
        r="1.5"
        fill="white"
        className="flake"
        style={{ animationDuration: '2.5s', animationDelay: '1s' }}
      />
      <circle
        cx="30"
        cy="60"
        r="1.5"
        fill="white"
        className="flake"
        style={{ animationDuration: '3s', animationDelay: '0.5s' }}
      />

      {/* Highlight on glass */}
      <ellipse
        cx="70"
        cy="35"
        rx="8"
        ry="4"
        fill="white"
        opacity="0.4"
        transform="rotate(-45 70 35)"
      />
    </svg>
  );
};

// 2. Hot Cocoa - cozy with steam animation
export const CozyCocoa = ({ className }) => (
  <svg viewBox="0 0 100 100" className={className} fill="none" aria-hidden="true">
    <style>
      {`
        .steam { animation: rise 2s infinite ease-in-out; opacity: 0; }
        @keyframes rise {
          0% { transform: translateY(0); opacity: 0; }
          50% { opacity: 0.6; }
          100% { transform: translateY(-15px); opacity: 0; }
        }
      `}
    </style>

    {/* Steam */}
    <path
      d="M40 20 Q 30 15 40 10"
      stroke="#CBD5E1"
      strokeWidth="3"
      strokeLinecap="round"
      className="steam"
      style={{ animationDelay: '0s' }}
    />
    <path
      d="M55 20 Q 45 15 55 10"
      stroke="#CBD5E1"
      strokeWidth="3"
      strokeLinecap="round"
      className="steam"
      style={{ animationDelay: '0.5s' }}
    />

    {/* Cup */}
    <path d="M25 30 L 30 80 Q 32 90 50 90 Q 68 90 70 80 L 75 30 Z" fill="#DC2626" />
    <rect x="25" y="25" width="50" height="5" fill="#B91C1C" rx="2" />

    {/* Handle */}
    <path
      d="M72 40 Q 90 40 90 55 Q 90 70 72 70"
      stroke="#DC2626"
      strokeWidth="6"
      strokeLinecap="round"
      fill="none"
    />

    {/* Snowflake pattern */}
    <path
      d="M50 45 L 50 75 M 35 60 L 65 60 M 40 50 L 60 70 M 40 70 L 60 50"
      stroke="white"
      strokeWidth="2"
      strokeLinecap="round"
      opacity="0.8"
    />

    {/* Marshmallows */}
    <rect x="35" y="22" width="10" height="8" fill="#FDE68A" rx="2" transform="rotate(-10)" />
    <rect x="50" y="20" width="10" height="8" fill="#FDE68A" rx="2" transform="rotate(15)" />
  </svg>
);

// 3. Cool Snowman - stylish beanie + scarf
export const CoolSnowman = ({ className }) => {
  const rawId = useId();
  const uid = String(rawId).replace(/:/g, '');
  const snowGradId = `snowGrad-${uid}`;

  return (
    <svg
      viewBox="0 0 100 120"
      className={className}
      fill="none"
      aria-hidden="true"
      style={{ filter: 'drop-shadow(2px 4px 6px rgba(0,0,0,0.1))' }}
    >
      <defs>
        <linearGradient id={snowGradId} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#FFFFFF" />
          <stop offset="100%" stopColor="#E2E8F0" />
        </linearGradient>
      </defs>

      {/* Body */}
      <circle cx="50" cy="90" r="28" fill={`url(#${snowGradId})`} />
      <circle cx="50" cy="50" r="22" fill={`url(#${snowGradId})`} />

      {/* Scarf */}
      <path d="M35 55 Q 50 65 65 55" stroke="#F43F5E" strokeWidth="6" strokeLinecap="round" />
      <path d="M60 55 Q 80 55 90 45" stroke="#F43F5E" strokeWidth="6" strokeLinecap="round" />

      {/* Beanie */}
      <path d="M28 40 Q 50 10 72 40" fill="#1E293B" />
      <rect x="25" y="38" width="50" height="8" rx="2" fill="#0F172A" />
      <circle cx="50" cy="20" r="6" fill="#F43F5E" />

      {/* Face */}
      <circle cx="42" cy="45" r="2.5" fill="#1E293B" />
      <circle cx="58" cy="45" r="2.5" fill="#1E293B" />
      <path d="M50 48 L 65 52 L 50 54" fill="#F97316" />
    </svg>
  );
};

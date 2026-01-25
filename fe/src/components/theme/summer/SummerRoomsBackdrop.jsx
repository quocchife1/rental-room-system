import React from 'react';

export default function SummerRoomsBackdrop({ enabled }) {
  if (!enabled) return null;

  return (
    <div
      className="pointer-events-none absolute inset-0 -z-10 overflow-hidden"
      aria-hidden="true"
      style={{ contain: 'paint' }}
    >
      {/* Soft gradient blobs (no blur/backdrop-filter for perf) */}
      <div
        className="absolute -top-24 -left-24 h-72 w-72 rounded-full opacity-60"
        style={{
          background:
            'radial-gradient(circle at 30% 30%, var(--app-primary-soft) 0%, rgba(0,0,0,0) 70%)',
        }}
      />
      <div
        className="absolute -top-16 -right-28 h-80 w-80 rounded-full opacity-50"
        style={{
          background:
            'radial-gradient(circle at 60% 40%, var(--app-wave) 0%, rgba(0,0,0,0) 72%)',
        }}
      />
      <div
        className="absolute -bottom-28 left-1/3 h-[420px] w-[420px] -translate-x-1/2 rounded-full opacity-40"
        style={{
          background:
            'radial-gradient(circle at 50% 60%, var(--app-primary) 0%, rgba(0,0,0,0) 70%)',
        }}
      />

      {/* Subtle wave bands */}
      <div className="absolute left-0 right-0 top-0 h-10 opacity-25">
        <svg viewBox="0 0 1200 120" preserveAspectRatio="none" className="h-full w-full">
          <path
            d="M0,80 C120,70 240,90 360,80 C480,70 600,40 720,48 C840,56 960,96 1080,88 C1140,84 1200,72 1200,72 L1200,0 L0,0 Z"
            fill="var(--app-wave)"
          />
        </svg>
      </div>
      <div className="absolute left-0 right-0 bottom-0 h-12 opacity-20 rotate-180">
        <svg viewBox="0 0 1200 120" preserveAspectRatio="none" className="h-full w-full">
          <path
            d="M0,80 C120,70 240,90 360,80 C480,70 600,40 720,48 C840,56 960,96 1080,88 C1140,84 1200,72 1200,72 L1200,0 L0,0 Z"
            fill="var(--app-wave)"
          />
        </svg>
      </div>

      {/* Small sparkles */}
      <div
        className="absolute left-10 top-20 h-1.5 w-1.5 rounded-full opacity-60"
        style={{ backgroundColor: 'var(--app-primary)' }}
      />
      <div
        className="absolute right-14 top-32 h-2 w-2 rounded-full opacity-40"
        style={{ backgroundColor: 'var(--app-primary)' }}
      />
      <div
        className="absolute right-24 bottom-24 h-1.5 w-1.5 rounded-full opacity-55"
        style={{ backgroundColor: 'var(--app-hero-from)' }}
      />
    </div>
  );
}

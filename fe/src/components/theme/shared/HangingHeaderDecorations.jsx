import React, { memo } from 'react';

const CHRISTMAS_BULBS = [
  { left: 8, color: '#ef4444' },
  { left: 18, color: '#22c55e' },
  { left: 28, color: '#f59e0b' },
  { left: 38, color: '#3b82f6' },
  { left: 48, color: '#a855f7' },
  { left: 58, color: '#ef4444' },
  { left: 68, color: '#22c55e' },
  { left: 78, color: '#f59e0b' },
  { left: 88, color: '#3b82f6' },
];

const SUMMER_FLAGS = [
  { left: 14, fill: 'var(--app-primary)' },
  { left: 24, fill: 'var(--app-secondary, var(--app-primary-hover))' },
  { left: 34, fill: 'var(--app-accent)' },
  { left: 44, fill: 'var(--app-primary)' },
  { left: 54, fill: 'var(--app-secondary, var(--app-primary-hover))' },
  { left: 64, fill: 'var(--app-accent)' },
  { left: 74, fill: 'var(--app-primary)' },
  { left: 84, fill: 'var(--app-secondary, var(--app-primary-hover))' },
];

function ChristmasLights({ paused }) {
  return (
    <div className="absolute inset-x-0 top-0 h-12 overflow-hidden">
      {/* Cable */}
      <svg
        className="absolute inset-x-0 top-0 h-12 w-full"
        viewBox="0 0 1200 80"
        preserveAspectRatio="none"
        aria-hidden="true"
      >
        <path
          d="M0,22 C180,46 360,8 600,22 C840,36 1020,10 1200,22"
          fill="none"
          stroke="rgba(0,0,0,0.25)"
          strokeWidth="3"
          strokeLinecap="round"
        />
      </svg>

      {/* Bulbs */}
      <div className="relative h-12">
        {CHRISTMAS_BULBS.map((b, idx) => (
          <div
            // eslint-disable-next-line react/no-array-index-key
            key={idx}
            className={paused ? 'absolute top-3' : 'absolute top-3 animate-twinkle will-change-transform'}
            style={{ left: `${b.left}%`, animationDelay: `${idx * 120}ms` }}
          >
            <div className="h-2 w-2 rounded-full bg-zinc-200/70" />
            <div
              className="mt-0.5 h-4 w-3 rounded-full"
              style={{
                backgroundColor: b.color,
                boxShadow: `0 0 10px ${b.color}55, 0 0 18px ${b.color}33`,
              }}
            />
          </div>
        ))}
      </div>
    </div>
  );
}

function SummerBunting({ paused }) {
  return (
    <div className="absolute inset-x-0 top-0 h-10 overflow-hidden">
      {/* Rope */}
      <svg
        className="absolute inset-x-0 top-0 h-10 w-full"
        viewBox="0 0 1200 80"
        preserveAspectRatio="none"
        aria-hidden="true"
      >
        <path
          d="M0,18 C200,36 400,10 600,20 C800,30 1000,10 1200,18"
          fill="none"
          stroke="var(--app-muted-2)"
          strokeWidth="2.5"
          strokeLinecap="round"
        />
      </svg>

      {/* Flags */}
      <div className="relative h-10">
        {SUMMER_FLAGS.map((f, idx) => (
          <div
            // eslint-disable-next-line react/no-array-index-key
            key={idx}
            className={paused ? 'absolute top-3 origin-top' : 'absolute top-3 origin-top animate-sway-slow will-change-transform'}
            style={{ left: `${f.left}%`, animationDelay: `${idx * 90}ms` }}
          >
            <svg width="11" height="12" viewBox="0 0 14 16" aria-hidden="true">
              <path d="M7 16L0 0h14L7 16z" fill={f.fill} fillOpacity="0.62" />
            </svg>
          </div>
        ))}
      </div>
    </div>
  );
}

function HangingHeaderDecorations({ variant, paused = false }) {
  if (!variant) return null;

  return (
    <div
      className="pointer-events-none absolute inset-x-0 bottom-0 z-20 hidden sm:block translate-y-[35%] drop-shadow-sm"
      aria-hidden="true"
    >
      {variant === 'christmas' ? <ChristmasLights paused={paused} /> : null}
      {variant === 'summer' ? <SummerBunting paused={paused} /> : null}
    </div>
  );
}

export default memo(HangingHeaderDecorations);

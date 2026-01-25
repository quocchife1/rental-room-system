import React, { useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';

export default function ThemeTransitionOverlay({
  active,
  durationMs = 900,
  onMidpoint,
  onDone,
}) {
  const { isTransitioning } = useSelector((state) => state.theme);
  const timeoutMidRef = useRef(null);
  const timeoutDoneRef = useRef(null);

  const shouldRun = Boolean(active ?? isTransitioning);

  useEffect(() => {
    if (!shouldRun) return;

    // Fire midpoint at ~50% to "rewrite" theme
    timeoutMidRef.current = window.setTimeout(() => {
      onMidpoint?.();
    }, Math.floor(durationMs * 0.5));

    // Finish
    timeoutDoneRef.current = window.setTimeout(() => {
      onDone?.();
    }, durationMs);

    return () => {
      if (timeoutMidRef.current) window.clearTimeout(timeoutMidRef.current);
      if (timeoutDoneRef.current) window.clearTimeout(timeoutDoneRef.current);
      timeoutMidRef.current = null;
      timeoutDoneRef.current = null;
    };
  }, [shouldRun, durationMs, onMidpoint, onDone]);

  if (!shouldRun) return null;

  // Performance notes:
  // - fixed overlay, no layout shift
  // - transform-only animation (GPU-friendly)
  return (
    <div
      className="pointer-events-none fixed inset-0 z-[10000] overflow-hidden"
      aria-hidden="true"
    >
      {/* Subtle dim + noise/pattern to sell the "rewrite" vibe */}
      <div className="absolute inset-0 bg-black/5" />

      {/* The scanline "wipe" bar */}
      <div
        className="absolute inset-y-0 left-0 w-[60vw] max-w-none animate-scanline will-change-transform"
        style={{
          animationDuration: `${durationMs}ms`,
          background:
            'linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.26) 35%, rgba(255,255,255,0.70) 50%, rgba(255,255,255,0.26) 65%, rgba(255,255,255,0) 100%)',
          boxShadow: '0 0 18px rgba(255,255,255,0.16)',
        }}
      >
        {/* Inner core line */}
        <div className="absolute inset-y-0 left-1/2 w-px -translate-x-1/2 bg-white/90" />
      </div>
    </div>
  );
}

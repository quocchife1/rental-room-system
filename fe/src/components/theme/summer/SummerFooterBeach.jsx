import React from 'react';
import { Starfish, Shell, Footprints, Crab } from './assets/SummerFooterAssets';

export default function SummerFooterBeach() {
  return (
    <div
      className="pointer-events-none absolute inset-0 z-0 overflow-hidden"
      aria-hidden="true"
      style={{ contain: 'paint' }}
    >
      {/* --- LỚP 1: NỀN CÁT (SAND BASE) --- */}
      <div
        className="absolute inset-0 top-10"
        style={{
          background:
            'linear-gradient(to top, var(--app-sand-2) 0%, var(--app-sand) 55%, rgba(255,247,237,0) 100%)',
        }}
      />

      {/* Hạt cát (Noise texture) */}
      <div
        className="absolute inset-0 opacity-20 mix-blend-multiply"
        style={{
          backgroundImage:
            "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E\")",
        }}
      />

      {/* --- LỚP 2: BỌT BIỂN (FOAM LINE) --- */}
      <div className="absolute top-[-2px] inset-x-0 h-12 md:h-20 text-[color:var(--app-sand)]">
        <svg viewBox="0 0 1440 320" preserveAspectRatio="none" className="w-full h-full fill-current">
          <path d="M0,160L48,176C96,192,192,224,288,224C384,224,480,192,576,165.3C672,139,768,117,864,128C960,139,1056,181,1152,197.3C1248,213,1344,203,1392,197.3L1440,192L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z" />
        </svg>
      </div>

      {/* --- LỚP 3: TRANG TRÍ (DECORATIONS) --- */}
      <div className="absolute bottom-20 left-[10%] opacity-20 text-[color:var(--app-sand-shadow)] rotate-12">
        <Footprints className="h-24 w-24" />
      </div>
      <div className="absolute bottom-32 left-[15%] opacity-15 text-[color:var(--app-sand-shadow)] rotate-[25deg] scale-90">
        <Footprints className="h-24 w-24" />
      </div>

      <div className="absolute bottom-8 right-[10%] text-orange-400 drop-shadow-lg opacity-90">
        <div className="animate-pulse">
          <Starfish className="h-16 w-16 md:h-20 md:w-20" />
        </div>
      </div>

      <div className="absolute bottom-4 left-[25%] text-orange-300/80 rotate-[-15deg] opacity-80">
        <Shell className="h-12 w-12 md:h-14 md:w-14" />
      </div>

      <div className="absolute bottom-6 right-[25%] md:right-[30%] text-red-400/90 opacity-80">
        <div className="animate-sway-slow origin-bottom">
          <Crab className="h-8 w-12 md:h-10 md:w-16" />
        </div>
      </div>

      <div className="absolute bottom-12 right-6 text-yellow-200/60 rotate-45">
        <Shell className="h-6 w-6" />
      </div>
    </div>
  );
}

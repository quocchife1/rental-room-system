import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useSelector } from 'react-redux';
import { Sun, Snowflake, CircleDot, ChevronUp } from 'lucide-react';

export default function ThemeSwitcher({ onRequestTheme }) {
  const { currentTheme, isTransitioning } = useSelector((state) => state.theme);
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  const options = [
    {
      key: 'summer',
      label: 'Mặc định (Summer)',
      icon: Sun,
      className:
        'bg-summer-primary text-white hover:bg-summer-primary/90 shadow-[0_10px_30px_rgba(20,184,166,0.35)]',
    },
    {
      key: 'christmas',
      label: 'Giáng sinh',
      icon: Snowflake,
      className:
        'bg-red-600 text-white hover:bg-red-700 shadow-[0_10px_30px_rgba(220,38,38,0.32)]',
    },
    {
      key: 'default',
      label: 'Cơ bản',
      icon: CircleDot,
      className:
        'bg-gray-900 text-white hover:bg-gray-800 shadow-[0_10px_30px_rgba(17,24,39,0.22)]',
    },
  ];

  const current = useMemo(
    () => options.find((o) => o.key === currentTheme) ?? options[0],
    [options, currentTheme]
  );

  useEffect(() => {
    const onDocDown = (e) => {
      if (!open) return;
      if (!rootRef.current) return;
      if (rootRef.current.contains(e.target)) return;
      setOpen(false);
    };
    document.addEventListener('mousedown', onDocDown);
    return () => document.removeEventListener('mousedown', onDocDown);
  }, [open]);

  return (
    <div ref={rootRef} className="fixed bottom-6 right-6 z-[9000]">
      <div className="relative">
        {/* Popover */}
        {open && (
          <div className="absolute bottom-14 right-0 w-[240px] rounded-2xl bg-[color:var(--app-surface-solid)] border border-[color:var(--app-border)] shadow-2xl p-2">
            <div className="px-2 pt-1 pb-2">
              <div className="text-[11px] font-extrabold uppercase tracking-wider text-[color:var(--app-muted-2)]">
                Chủ đề
              </div>
              <div className="text-sm font-bold text-[color:var(--app-text)]">
                Đang dùng: {current.label}
              </div>
            </div>

            <div className="flex flex-col gap-2">
              {options.map((opt) => {
                const Icon = opt.icon;
                const isActive = currentTheme === opt.key;
                return (
                  <button
                    key={opt.key}
                    type="button"
                    onClick={() => {
                      setOpen(false);
                      onRequestTheme?.(opt.key);
                    }}
                    disabled={isTransitioning || isActive}
                    className={
                      'group flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all ' +
                      (isActive
                        ? 'opacity-60 cursor-default'
                        : isTransitioning
                          ? 'opacity-40 cursor-not-allowed'
                          : 'hover:-translate-y-[1px] active:translate-y-0 ') +
                      opt.className
                    }
                    title={opt.label}
                  >
                    <Icon className="h-4 w-4" />
                    <span className="whitespace-nowrap">{opt.label}</span>
                    {isActive && (
                      <span className="ml-auto text-[10px] font-bold bg-white/20 px-2 py-0.5 rounded-full">
                        ON
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          </div>
        )}

        {/* Trigger pill */}
        <button
          type="button"
          onClick={() => setOpen((v) => !v)}
          aria-expanded={open}
          className="flex items-center gap-2 rounded-full bg-[color:var(--app-surface-solid)] border border-[color:var(--app-border-strong)] shadow-xl px-3 py-2 backdrop-blur-md"
        >
          <current.icon className="h-4 w-4 text-[color:var(--app-primary)]" />
          <span className="text-sm font-bold text-[color:var(--app-text)] whitespace-nowrap">
            {current.label}
          </span>
          <ChevronUp
            className={`h-4 w-4 text-[color:var(--app-muted-2)] transition-transform ${open ? 'rotate-180' : ''}`}
          />
        </button>
      </div>
    </div>
  );
}

import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useSelector } from 'react-redux';
import { Sun, Snowflake, CircleDot, ChevronDown } from 'lucide-react';
import { useThemeRequest } from './ThemeRequestContext';

export default function HeaderThemeMenu() {
  const requestTheme = useThemeRequest();
  const { currentTheme, isTransitioning } = useSelector((state) => state.theme);

  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  const options = useMemo(
    () => [
      {
        key: 'summer',
        label: 'Mặc định (Summer)',
        icon: Sun,
        className: 'bg-summer-bg/80 text-summer-primary border border-summer-primary/20',
      },
      {
        key: 'christmas',
        label: 'Giáng sinh',
        icon: Snowflake,
        className: 'bg-red-50 text-red-600 border border-red-200',
      },
      {
        key: 'default',
        label: 'Cơ bản',
        icon: CircleDot,
        className: 'bg-indigo-50 text-indigo-700 border border-indigo-200',
      },
    ],
    []
  );

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

  const disabled = !requestTheme || isTransitioning;

  return (
    <div ref={rootRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        disabled={disabled}
        aria-expanded={open}
        className={
          'inline-flex items-center gap-2 rounded-full px-3 py-2 border shadow-sm transition-all ' +
          'w-11 sm:w-[210px] justify-center sm:justify-between ' +
          'bg-[color:var(--app-surface-solid)] border-[color:var(--app-border-strong)] ' +
          (disabled ? 'opacity-60 cursor-not-allowed' : 'hover:bg-[color:var(--app-primary-soft)]')
        }
        title="Đổi chủ đề"
      >
        <span className="inline-flex items-center gap-2 min-w-0">
          <current.icon className="h-4 w-4 text-[color:var(--app-primary)] flex-none" />
          <span className="hidden sm:inline text-sm font-bold text-[color:var(--app-text)] whitespace-nowrap truncate">
            {current.label}
          </span>
        </span>
        <ChevronDown
          className={
            'h-4 w-4 text-[color:var(--app-muted-2)] transition-transform flex-none ' +
            (open ? 'rotate-180' : '')
          }
        />
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-[240px] rounded-2xl bg-[color:var(--app-surface-solid)] border border-[color:var(--app-border)] shadow-2xl p-2 z-[60]">
          <div className="px-2 pt-1 pb-2">
            <div className="text-[11px] font-extrabold uppercase tracking-wider text-[color:var(--app-muted-2)]">
              Chủ đề
            </div>
            <div className="text-sm font-bold text-[color:var(--app-text)]">
              Chọn giao diện
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
                  disabled={isTransitioning || isActive}
                  onClick={() => {
                    setOpen(false);
                    requestTheme?.(opt.key);
                  }}
                  className={
                    'flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-semibold transition-all ' +
                    (isActive
                      ? 'opacity-60 cursor-default'
                      : isTransitioning
                        ? 'opacity-40 cursor-not-allowed'
                        : 'hover:-translate-y-[1px] active:translate-y-0 ') +
                    opt.className
                  }
                >
                  <Icon className="h-4 w-4" />
                  <span className="whitespace-nowrap">{opt.label}</span>
                  {isActive && (
                    <span className="ml-auto text-[10px] font-bold bg-white/30 px-2 py-0.5 rounded-full">
                      ON
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

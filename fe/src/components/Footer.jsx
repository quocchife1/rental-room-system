import React from 'react';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import SummerFooterBeach from './theme/summer/SummerFooterBeach';
import { ChillCoconut, MessageBottle, SurfVan } from './theme/summer/assets/SummerFooterAssets';
import { CoolSnowman, CozyCocoa, MagicSnowGlobe } from './theme/christmas/assets/ChristmasFooterAssets';

export default function Footer() {
  const { currentTheme } = useSelector((state) => state.theme);
  const isChristmas = currentTheme === 'christmas';
  const isSummer = currentTheme === 'summer';

  return (
    <footer className="bg-[color:var(--app-surface-solid)] border-t border-[color:var(--app-border)] pt-16 pb-8 relative mt-auto">

      {/* Summer sand beach (bottom) */}
      {isSummer && <SummerFooterBeach />}

      {/* Summer wave band (top) */}
      {isSummer && (
        <div className="pointer-events-none absolute top-0 left-0 w-full h-10 overflow-hidden z-0">
          <div className="absolute inset-0 opacity-70">
            <div className="flex w-[200%] h-full animate-wave-drift will-change-transform">
              <svg
                className="h-full w-1/2 flex-none fill-[color:var(--app-wave)]"
                viewBox="0 0 1440 90"
                preserveAspectRatio="none"
                aria-hidden="true"
              >
                <path d="M0,55 C120,20 240,90 360,55 C480,20 600,90 720,55 C840,20 960,90 1080,55 C1200,20 1320,90 1440,55 L1440,0 L0,0 Z" />
              </svg>
              <svg
                className="h-full w-1/2 flex-none fill-[color:var(--app-wave)]"
                viewBox="0 0 1440 90"
                preserveAspectRatio="none"
                aria-hidden="true"
              >
                <path d="M0,55 C120,20 240,90 360,55 C480,20 600,90 720,55 C840,20 960,90 1080,55 C1200,20 1320,90 1440,55 L1440,0 L0,0 Z" />
              </svg>
            </div>
          </div>

          <div className="absolute inset-0 opacity-35 -translate-y-[1px]">
            <div className="flex w-[200%] h-full animate-wave-drift-rev will-change-transform">
              <svg
                className="h-full w-1/2 flex-none fill-[color:var(--app-wave)]"
                viewBox="0 0 1440 90"
                preserveAspectRatio="none"
                aria-hidden="true"
              >
                <path d="M0,52 C120,88 240,15 360,52 C480,88 600,15 720,52 C840,88 960,15 1080,52 C1200,88 1320,15 1440,52 L1440,0 L0,0 Z" />
              </svg>
              <svg
                className="h-full w-1/2 flex-none fill-[color:var(--app-wave)]"
                viewBox="0 0 1440 90"
                preserveAspectRatio="none"
                aria-hidden="true"
              >
                <path d="M0,52 C120,88 240,15 360,52 C480,88 600,15 720,52 C840,88 960,15 1080,52 C1200,88 1320,15 1440,52 L1440,0 L0,0 Z" />
              </svg>
            </div>
          </div>
        </div>
      )}

      {/* Summer watery glow */}
      {isSummer && (
        <div
          className="pointer-events-none absolute inset-x-0 top-0 h-40 opacity-60 z-0"
          style={{
            background:
              'radial-gradient(900px 120px at 20% 0%, var(--app-wave), transparent 60%), radial-gradient(700px 120px at 70% 0%, var(--app-primary-soft), transparent 62%)',
          }}
        />
      )}

      {/* Summer footer objects (anchored to footer edge) */}
      {isSummer && (
        <div className="absolute top-0 left-0 w-full pointer-events-none h-12 z-20">
          <div className="container mx-auto px-6 relative h-full">
            <div className="absolute left-6 -top-8 flex items-end gap-3">
              <SurfVan className="h-11 w-14" />
              <ChillCoconut className="h-11 w-11" />
              <MessageBottle className="h-10 w-10" />
            </div>
          </div>
        </div>
      )}
      
      {/* --- TRANG TRÍ GIÁNG SINH TRÊN FOOTER --- */}
      {isChristmas && (
        <div className="absolute top-0 left-0 w-full pointer-events-none h-12 z-20">
          <div className="container mx-auto px-6 relative h-full">
            <div className="absolute left-6 -top-9 flex items-end gap-3">
              <MagicSnowGlobe className="h-12 w-12" />
              <CozyCocoa className="h-11 w-11" />
            </div>
            <div className="absolute right-6 -top-10">
              <CoolSnowman className="h-12 w-12" />
            </div>
          </div>
        </div>
      )}

      <div className="container mx-auto px-6 relative z-10">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
          
          <div className="md:col-span-2">
            <Link to="/" className="inline-flex items-center gap-3 mb-4">
              <img
                src="/logo/logo_1.png"
                alt="Alpha"
                className="h-9 w-9 md:hidden"
                draggable="false"
                loading="lazy"
              />
              <img
                src="/logo/logo_2.png"
                alt="Alpha"
                className="hidden md:block h-9 w-auto"
                draggable="false"
                loading="lazy"
              />
              <span className="sr-only">Alpha</span>
            </Link>
            <p className="text-[color:var(--app-muted)] leading-relaxed max-w-sm">
              Nền tảng kết nối phòng trọ ký túc xá hàng đầu. Chúng tôi mang đến không gian sống tiện nghi, an toàn và minh bạch cho sinh viên và người đi làm.
            </p>
          </div>

          <div>
            <h4 className="font-bold text-[color:var(--app-text)] mb-6">Khám phá</h4>
            <ul className="space-y-4 text-[color:var(--app-muted)]">
              <li><Link to="/" className="hover:text-[color:var(--app-primary)] transition-colors">Tìm phòng ngay</Link></li>
              <li><Link to="/about" className="hover:text-[color:var(--app-primary)] transition-colors">Về chúng tôi</Link></li>
              <li><span className="cursor-pointer hover:text-[color:var(--app-primary)] transition-colors">Tin tức & Ưu đãi</span></li>
            </ul>
          </div>

          <div>
            <h4 className="font-bold text-[color:var(--app-text)] mb-6">Hỗ trợ</h4>
            <ul className="space-y-4 text-[color:var(--app-muted)]">
              <li>1900 1234 (8:00 - 21:00)</li>
              <li>support@rental.vn</li>
              <li>123 Đường Nguyễn Văn Cừ, Q.5, TP.HCM</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-[color:var(--app-border)] pt-8 flex flex-col md:flex-row justify-between items-center text-sm text-[color:var(--app-muted-2)]">
          <p>&copy; {new Date().getFullYear()} Alpha. All rights reserved.</p>
          <div className="flex gap-6 mt-4 md:mt-0">
            <span>Điều khoản sử dụng</span>
            <span>Chính sách bảo mật</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
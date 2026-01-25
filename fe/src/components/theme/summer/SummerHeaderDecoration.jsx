import React, { memo } from 'react';
// Đảm bảo bạn đã export HangingBranch từ file SummerAssets
import { HangingBranch, Seagull, SunGraphic } from './SummerAssets';

// Sóng nền (Đã tối ưu hóa chiều cao cho header nhỏ)
const WaveSVG = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 1440 90"
    preserveAspectRatio="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path d="M0,45 C240,70 480,20 720,45 C960,70 1200,20 1440,45 L1440,90 L0,90 Z" />
  </svg>
);

function SummerHeaderDecoration({ isSummer = true, paused = false }) {
  if (!isSummer) return null;

  return (
    <div
      className="pointer-events-none absolute inset-0 overflow-hidden z-0 select-none"
      style={{ contain: 'paint' }}
      aria-hidden="true"
    >
      {/* --- LỚP 1: BẦU TRỜI & MẶT TRỜI --- */}

      {/* Mặt trời: Đặt chìm vào nền (mix-blend-screen) để không làm rối nút bấm */}
      <div className="absolute top-[-15px] right-[10%] md:right-[18%] opacity-60 mix-blend-screen">
        <div className={paused ? 'origin-center' : 'animate-sunrays origin-center will-change-transform'}>
          {/* Kích thước vừa phải cho header nhỏ */}
          <SunGraphic className="h-20 w-20 md:h-28 md:w-28 text-yellow-300" />
        </div>
      </div>

      {/* Chim hải âu: Bay ngang, giảm opacity để không gây chú ý quá mức */}
      <div className="absolute top-3 left-[-20px] opacity-50 text-white">
        <div className={paused ? 'translate-x-10' : 'animate-fly-across will-change-transform'}>
          <Seagull className="h-4 w-8 md:h-5 md:w-10" />
        </div>
      </div>

      {/* Chim thứ 2 bay chậm hơn */}
      <div className="absolute top-8 left-[-50px] opacity-30 text-white" style={{ animationDelay: '3s' }}>
        <div
          className={paused ? 'translate-x-20' : 'animate-fly-across will-change-transform'}
          style={{ animationDuration: '25s' }}
        >
          <Seagull className="h-3 w-6 md:h-4 md:w-8" />
        </div>
      </div>

      {/* --- LỚP 2: TÁN LÁ RỦ (QUAN TRỌNG: Thay thế cây dừa to) --- */}

      {/* Góc TRÁI: Lá rủ xuống nhẹ nhàng */}
      <div className="absolute -top-3 -left-4 text-green-700/20 md:text-green-600/15 z-10">
        <div className={paused ? 'origin-top-left' : 'animate-sway origin-top-left will-change-transform'}>
          {/* Kích thước nhỏ gọn, không che chữ */}
          <HangingBranch className="h-24 w-24 md:h-32 md:w-32 drop-shadow-sm" />
        </div>
      </div>

      {/* Góc PHẢI: Lá rủ xuống (Lật ngược lại) */}
      <div className="absolute -top-3 -right-6 text-green-700/20 md:text-green-600/15 z-10">
        <div className={paused ? 'origin-top-right' : 'animate-sway-slow origin-top-right will-change-transform'}>
          {/* Flip = true để lật lá lại */}
          <HangingBranch className="h-28 w-28 md:h-36 md:w-36 drop-shadow-sm" flip={true} />
        </div>
      </div>

      {/* --- LỚP 3: SÓNG BIỂN (Đường viền đáy) --- */}
      <div className="absolute inset-x-0 bottom-0 z-0 h-full flex flex-col justify-end">
        {/* Sóng lớp sau (Mờ và chậm hơn) */}
        <div className="absolute bottom-0 w-[200%] h-10 md:h-14 opacity-40">
          <div className={paused ? 'w-full h-full' : 'w-full h-full animate-wave-drift will-change-transform'}>
            <WaveSVG className="h-full w-1/2 float-left fill-[color:var(--app-primary)]" />
            <WaveSVG className="h-full w-1/2 float-left fill-[color:var(--app-primary)]" />
          </div>
        </div>

        {/* Sóng lớp trước (Rõ và nhanh hơn) - Chiều cao thấp (h-8 = 32px) */}
        <div className="absolute bottom-[-2px] w-[200%] h-8 md:h-10">
          <div className={paused ? 'w-full h-full' : 'w-full h-full animate-wave-drift-rev will-change-transform'}>
            <WaveSVG className="h-full w-1/2 float-left fill-[color:var(--app-wave)]" />
            <WaveSVG className="h-full w-1/2 float-left fill-[color:var(--app-wave)]" />
          </div>
        </div>
      </div>
    </div>
  );
}

export default memo(SummerHeaderDecoration);

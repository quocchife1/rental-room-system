import React, { useMemo } from 'react';

// Cấu hình mùa hiện tại: 'NONE', 'WINTER', 'CHRISTMAS', 'TET'
export const CURRENT_SEASON = 'CHRISTMAS'; 

export default function SeasonalEffects() {
  if (CURRENT_SEASON === 'NONE') return null;

  // Tối ưu hóa tuyết rơi bằng useMemo
  const snowflakes = useMemo(() => {
    return [...Array(50)].map((_, i) => {
      const left = Math.random() * 100 + '%';
      const delay = -(Math.random() * 20) + 's'; // Delay âm để tuyết xuất hiện ngay lập tức
      const duration = Math.random() * 10 + 10 + 's'; 
      const size = Math.random() * 0.5 + 0.2 + 'rem'; 
      const opacity = Math.random() * 0.5 + 0.3; 

      return { i, left, delay, duration, size, opacity };
    });
  }, []);

  return (
    <div className="pointer-events-none fixed inset-0 z-[9999] overflow-hidden font-sans">
      
      {/* --- PHẦN 1: CSS ANIMATIONS --- */}
      <style>{`
        @keyframes snowfall {
          0% { transform: translate3d(0, -10vh, 0) rotate(0deg); opacity: 1; }
          100% { transform: translate3d(20px, 110vh, 0) rotate(360deg); opacity: 0.2; }
        }
        @keyframes twinkle {
          0%, 100% { opacity: 1; transform: scale(1); filter: brightness(1.2); }
          50% { opacity: 0.5; transform: scale(0.8); filter: brightness(0.8); }
        }
        /* Đã chỉnh sửa: Santa bắt đầu từ rất xa bên trái (-50vw) */
        @keyframes fly-santa {
          0% { transform: translateX(-50vw) translateY(0) scale(0.8); opacity: 0; }
          10% { opacity: 0.8; }
          25% { transform: translateX(25vw) translateY(20px) scale(0.9); }
          50% { transform: translateX(50vw) translateY(0) scale(1); }
          75% { transform: translateX(75vw) translateY(-20px) scale(0.9); }
          100% { transform: translateX(120vw) translateY(0) scale(0.8); opacity: 0; }
        }
        .snowflake {
          position: absolute;
          top: -20px;
          color: white;
          border-radius: 50%;
          background: white;
          filter: blur(1px);
          animation-name: snowfall;
          animation-timing-function: linear;
          animation-iteration-count: infinite;
          will-change: transform;
        }
      `}</style>

      {/* --- PHẦN 2: HIỆU ỨNG TUYẾT --- */}
      {(CURRENT_SEASON === 'WINTER' || CURRENT_SEASON === 'CHRISTMAS') && (
        <>
          <div className="absolute top-0 left-0 w-full h-24 bg-gradient-to-b from-white/30 to-transparent opacity-50"></div>
          {snowflakes.map((flake) => (
            <div 
              key={flake.i} 
              className="snowflake"
              style={{ 
                left: flake.left, 
                width: flake.size,
                height: flake.size,
                animationDelay: flake.delay, 
                animationDuration: flake.duration,
                opacity: flake.opacity
              }}
            />
          ))}
        </>
      )}

      {/* --- PHẦN 3: HIỆU ỨNG TOÀN MÀN HÌNH GIÁNG SINH --- */}
      {CURRENT_SEASON === 'CHRISTMAS' && (
        <>
           {/* 1. Dây đèn nhấp nháy trên cùng (Giữ nguyên vì đẹp) */}
           <div className="absolute top-0 left-0 w-full flex justify-between px-2 overflow-hidden">
              {[...Array(25)].map((_, i) => (
                <div 
                  key={i}
                  className={`w-2 h-2 rounded-full mt-[-4px] shadow-md ${i % 2 === 0 ? 'bg-red-500 shadow-red-500/50' : i % 3 === 0 ? 'bg-yellow-400 shadow-yellow-400/50' : 'bg-green-500 shadow-green-500/50'}`}
                  style={{
                    animation: `twinkle ${Math.random() * 2 + 1}s infinite`,
                    animationDelay: `${Math.random()}s`
                  }}
                ></div>
              ))}
           </div>

           {/* 2. Ông già Noel bay qua màn hình (Global) */}
           <div 
             className="absolute top-24 left-0 text-6xl opacity-40 filter drop-shadow-lg z-0"
             style={{ animation: 'fly-santa 45s linear infinite' }} // 45s để bay chậm rãi
           >
             🦌🎅🛷
           </div>
        </>
      )}
    </div>
  );
}
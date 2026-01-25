import React from 'react';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';

export default function AuthLayout({ children, title, subtitle }) {
  const { currentTheme } = useSelector((state) => state.theme);
  const isChristmas = currentTheme === 'christmas';

  return (
    <div className="min-h-screen bg-[color:var(--app-bg)] text-[color:var(--app-text)] flex flex-col justify-center items-center relative overflow-hidden font-sans">
      {/* Background Decor */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden">
         {/* Blob Background */}
         <div className="absolute -top-[10%] -left-[10%] w-[50%] h-[50%] rounded-full bg-indigo-100 blur-[100px] opacity-60"></div>
         <div className="absolute top-[20%] -right-[10%] w-[40%] h-[60%] rounded-full bg-teal-100 blur-[100px] opacity-60"></div>
         
         {/* Cây thông trang trí nền (Nếu là Giáng sinh) */}
        {isChristmas && (
            <div className="absolute bottom-0 left-10 text-[10rem] opacity-10 filter blur-sm transform -rotate-12">🎄</div>
         )}
      </div>

      {/* Logo & Header */}
      <div className="sm:mx-auto sm:w-full sm:max-w-md z-10 text-center mb-8">
        <Link to="/" className="inline-flex items-center gap-3 group" aria-label="Alpha">
          {isChristmas && <span className="text-3xl animate-bounce">🎅</span>}
          <img
            src="/logo/logo_1.png"
            alt="Alpha"
            className="h-10 w-10 sm:hidden"
            draggable="false"
            loading="eager"
          />
          <img
            src="/logo/logo_2.png"
            alt="Alpha"
            className="hidden sm:block h-10 w-auto"
            draggable="false"
            loading="eager"
          />
          <span className="sr-only">Alpha</span>
        </Link>
        <h2 className="mt-6 text-3xl font-extrabold text-[color:var(--app-text)]">
          {title}
        </h2>
        {subtitle && (
          <p className="mt-2 text-sm text-[color:var(--app-muted)]">
            {subtitle}
          </p>
        )}
      </div>

      {/* Main Content Box */}
      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md z-10">
        <div className="bg-[color:var(--app-surface-solid)] py-8 px-4 shadow-2xl sm:rounded-2xl sm:px-10 border border-[color:var(--app-border)] relative">
          {/* Trang trí góc hộp */}
          {isChristmas && (
             <div className="absolute -top-3 -right-3 text-2xl transform rotate-12">🎁</div>
          )}
          
          {children}
        </div>
      </div>

      {/* Footer nhỏ */}
      <div className="mt-8 text-center text-xs text-[color:var(--app-muted-2)] z-10">
        &copy; {new Date().getFullYear()} Alpha. All rights reserved.
      </div>
    </div>
  );
}
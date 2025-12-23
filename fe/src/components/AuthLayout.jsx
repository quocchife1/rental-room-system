import React from 'react';
import { Link } from 'react-router-dom';
import SeasonalEffects, { CURRENT_SEASON } from './SeasonalEffects';

export default function AuthLayout({ children, title, subtitle }) {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center items-center relative overflow-hidden font-sans">
      {/* Hiệu ứng mùa lễ hội */}
      <SeasonalEffects />

      {/* Background Decor */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden">
         {/* Blob Background */}
         <div className="absolute -top-[10%] -left-[10%] w-[50%] h-[50%] rounded-full bg-indigo-100 blur-[100px] opacity-60"></div>
         <div className="absolute top-[20%] -right-[10%] w-[40%] h-[60%] rounded-full bg-teal-100 blur-[100px] opacity-60"></div>
         
         {/* Cây thông trang trí nền (Nếu là Giáng sinh) */}
         {CURRENT_SEASON === 'CHRISTMAS' && (
            <div className="absolute bottom-0 left-10 text-[10rem] opacity-10 filter blur-sm transform -rotate-12">🎄</div>
         )}
      </div>

      {/* Logo & Header */}
      <div className="sm:mx-auto sm:w-full sm:max-w-md z-10 text-center mb-8">
        <Link to="/" className="inline-flex items-center gap-2 text-3xl font-extrabold text-indigo-600 tracking-tight group">
          {CURRENT_SEASON === 'CHRISTMAS' && <span className="text-3xl animate-bounce">🎅</span>}
          <span>UML Rental</span>
        </Link>
        <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
          {title}
        </h2>
        {subtitle && (
          <p className="mt-2 text-sm text-gray-600">
            {subtitle}
          </p>
        )}
      </div>

      {/* Main Content Box */}
      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md z-10">
        <div className="bg-white py-8 px-4 shadow-2xl shadow-indigo-100 sm:rounded-2xl sm:px-10 border border-gray-100 relative">
          {/* Trang trí góc hộp */}
          {CURRENT_SEASON === 'CHRISTMAS' && (
             <div className="absolute -top-3 -right-3 text-2xl transform rotate-12">🎁</div>
          )}
          
          {children}
        </div>
      </div>

      {/* Footer nhỏ */}
      <div className="mt-8 text-center text-xs text-gray-400 z-10">
        &copy; {new Date().getFullYear()} UML Rental. All rights reserved.
      </div>
    </div>
  );
}
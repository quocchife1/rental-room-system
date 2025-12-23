import React from 'react';
import { Link } from 'react-router-dom';
import { CURRENT_SEASON } from './SeasonalEffects';

export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-100 pt-16 pb-8 relative mt-auto">
      
      {/* --- TRANG TRÍ GIÁNG SINH TRÊN FOOTER --- */}
      {CURRENT_SEASON === 'CHRISTMAS' && (
        <div className="absolute -top-8 left-0 w-full pointer-events-none overflow-hidden h-10 z-10">
           <div className="container mx-auto px-6 relative h-full">
              {/* Người tuyết ngồi bên trái */}
              <div className="absolute left-6 bottom-0 text-4xl transform translate-y-1 drop-shadow-sm">
                ⛄
              </div>
              {/* Cây thông ngồi bên phải */}
              <div className="absolute right-6 bottom-0 flex gap-2">
                 <div className="text-2xl transform translate-y-2 animate-bounce" style={{animationDuration: '3s'}}>🎁</div>
                 <div className="text-4xl transform translate-y-1">🎄</div>
              </div>
           </div>
        </div>
      )}

      <div className="container mx-auto px-6 relative z-0">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-12 mb-12">
          
          <div className="md:col-span-2">
            <Link to="/" className="text-2xl font-bold text-indigo-600 mb-4 block">
              RentalSystem
            </Link>
            <p className="text-gray-500 leading-relaxed max-w-sm">
              Nền tảng kết nối phòng trọ ký túc xá hàng đầu. Chúng tôi mang đến không gian sống tiện nghi, an toàn và minh bạch cho sinh viên và người đi làm.
            </p>
          </div>

          <div>
            <h4 className="font-bold text-gray-900 mb-6">Khám phá</h4>
            <ul className="space-y-4 text-gray-500">
              <li><Link to="/" className="hover:text-indigo-600 transition-colors">Tìm phòng ngay</Link></li>
              <li><Link to="/about" className="hover:text-indigo-600 transition-colors">Về chúng tôi</Link></li>
              <li><span className="cursor-pointer hover:text-indigo-600 transition-colors">Tin tức & Ưu đãi</span></li>
            </ul>
          </div>

          <div>
            <h4 className="font-bold text-gray-900 mb-6">Hỗ trợ</h4>
            <ul className="space-y-4 text-gray-500">
              <li>1900 1234 (8:00 - 21:00)</li>
              <li>support@rental.vn</li>
              <li>123 Đường Nguyễn Văn Cừ, Q.5, TP.HCM</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-100 pt-8 flex flex-col md:flex-row justify-between items-center text-sm text-gray-400">
          <p>&copy; {new Date().getFullYear()} RentalSystem. All rights reserved.</p>
          <div className="flex gap-6 mt-4 md:mt-0">
            <span>Điều khoản sử dụng</span>
            <span>Chính sách bảo mật</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
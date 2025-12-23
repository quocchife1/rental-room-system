import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom'; // Import hook lấy địa chỉ URL
import Header from './Header';
import Footer from './Footer';
import SeasonalEffects from './SeasonalEffects';

export default function MainLayout({ children }) {
  const { pathname } = useLocation();

  // Tự động cuộn lên đầu trang mỗi khi đường dẫn (pathname) thay đổi
  useEffect(() => {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'instant' // 'smooth' nếu muốn cuộn mượt, 'instant' để về ngay lập tức
    });
  }, [pathname]);

  return (
    <div className="flex flex-col min-h-screen font-sans text-gray-800 bg-stone-50 relative">
      {/* Hiệu ứng mùa lễ hội */}
      <SeasonalEffects />
      
      <Header />
      <main className="flex-grow z-10">
        {children}
      </main>
      <Footer />
    </div>
  );
}
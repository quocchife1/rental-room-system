import React from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
// Header sẽ được cung cấp bởi MainLayout để tránh nhân đôi

export default function PartnerLayout() {
  const location = useLocation();
  
  const menuItems = [
    { path: '/partner/dashboard', label: 'Tổng quan', icon: '📊' },
    { path: '/partner/my-listings', label: 'Quản lý tin đăng', icon: '📝' },
    { path: '/partner/create-listing', label: 'Đăng tin mới', icon: '➕' },
    { path: '/partner/packages', label: 'Mua gói dịch vụ', icon: '💎' },
  ];

  return (
    <div className="min-h-screen bg-[color:var(--app-bg)] text-[color:var(--app-text)] flex flex-col">
      <div className="flex flex-1 container mx-auto px-4 py-8 gap-6">
        {/* Sidebar */}
        <aside className="w-64 hidden md:block">
          <div className="bg-[color:var(--app-surface-solid)] rounded-xl shadow-sm border border-[color:var(--app-border)] p-4 sticky top-24">
            <div className="mb-6 px-4">
               <h2 className="text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider">Kênh Đối tác</h2>
               <p className="text-sm text-[color:var(--app-muted)] mt-1">Quản lý nhà trọ & Tin đăng</p>
            </div>
            <nav className="space-y-1">
              {menuItems.map((item) => (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium text-sm
                  ${location.pathname === item.path
                    ? 'bg-[color:var(--app-primary-soft)] text-[color:var(--app-primary)] shadow-sm'
                    : 'text-[color:var(--app-muted)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)]'}`}
                >
                  <span>{item.icon}</span>
                  {item.label}
                </Link>
              ))}
            </nav>
            
            {/* Widget số dư đã tạm ẩn vì là dữ liệu mẫu */}
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
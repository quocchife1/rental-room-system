import React from 'react';
import { Outlet, Link, useLocation } from 'react-router-dom';
import Header from './Header'; 

export default function TenantLayout() {
  const location = useLocation();
  const menuItems = [
    { path: '/tenant/dashboard', label: 'Tổng quan', icon: '📊' },
    { path: '/tenant/reservations', label: 'Lịch sử giữ chỗ', icon: '📅' },
    { path: '/tenant/contracts', label: 'Hợp đồng của tôi', icon: '📝' },
    { path: '/tenant/services', label: 'Dịch vụ phát sinh', icon: '🧾' },
    { path: '/tenant/invoices', label: 'Hóa đơn & Thanh toán', icon: '💳' },
    { path: '/tenant/maintenance', label: 'Yêu cầu bảo trì', icon: '🛠️' },
  ];

  return (
    <div className="min-h-screen bg-[color:var(--app-bg)] text-[color:var(--app-text)] flex flex-col">
      <Header />
      <div className="flex flex-1 container mx-auto px-4 py-8 gap-6">
        <aside className="w-64 hidden md:block">
          <div className="bg-[color:var(--app-surface-solid)] rounded-xl shadow-sm border border-[color:var(--app-border)] p-4 sticky top-24">
            <h2 className="text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider mb-4 px-4">Menu Sinh viên</h2>
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
          </div>
        </aside>

        <main className="flex-1">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
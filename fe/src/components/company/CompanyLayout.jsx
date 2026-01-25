import React, { useMemo, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import authApi from '../../api/authApi';
import { logout } from '../../features/auth/authSlice';

const EMPLOYEE_ROLES = ['ADMIN', 'DIRECTOR', 'MANAGER', 'ACCOUNTANT', 'RECEPTIONIST', 'MAINTENANCE', 'SECURITY'];

function Icon({ path, className }) {
  return (
    <svg viewBox="0 0 24 24" className={className} fill="none" aria-hidden="true">
      <path d={path} stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

const Icons = {
  Home: (props) => (
    <Icon
      {...props}
      path="M3 10.5L12 3l9 7.5V21a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V10.5z"
    />
  ),
  Rooms: (props) => (
    <Icon
      {...props}
      path="M4 21V5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v16M7 8h3M7 12h3M7 16h3M14 8h3M14 12h3M14 16h3"
    />
  ),
  Booking: (props) => (
    <Icon
      {...props}
      path="M8 7V3m8 4V3M4 9h16M6 21h12a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2H6a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2z"
    />
  ),
  Contract: (props) => (
    <Icon
      {...props}
      path="M8 3h8l4 4v14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM8 11h8M8 15h8M8 19h6"
    />
  ),
  Finance: (props) => (
    <Icon
      {...props}
      path="M12 1v22M17 5H9.5a3.5 3.5 0 0 0 0 7H14a3.5 3.5 0 0 1 0 7H6"
    />
  ),
  Tools: (props) => (
    <Icon
      {...props}
      path="M14.7 6.3a4 4 0 1 0 3 3L22 13.6l-2.4 2.4-4.3-4.3a4 4 0 0 1-3.3 3.3L7.6 20.4 4 17l5.4-4.4a4 4 0 0 1 3.3-3.3L14.7 6.3z"
    />
  ),
  Clipboard: (props) => (
    <Icon
      {...props}
      path="M9 3h6a2 2 0 0 1 2 2v2H7V5a2 2 0 0 1 2-2zM7 7h10v14a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2V7z"
    />
  ),
  Shield: (props) => (
    <Icon
      {...props}
      path="M12 2l8 4v6c0 5-3.5 9.5-8 10-4.5-.5-8-5-8-10V6l8-4z"
    />
  ),
  Menu: (props) => (
    <Icon {...props} path="M4 6h16M4 12h16M4 18h16" />
  ),
  ChevronLeft: (props) => (
    <Icon {...props} path="M15 18l-6-6 6-6" />
  ),
  ChevronRight: (props) => (
    <Icon {...props} path="M9 18l6-6-6-6" />
  ),
  Logout: (props) => (
    <Icon {...props} path="M10 16l-4-4 4-4M6 12h11M17 4h2a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-2" />
  ),
};

function cn(...parts) {
  return parts.filter(Boolean).join(' ');
}

function isEmployee(role) {
  return EMPLOYEE_ROLES.includes(String(role || '').toUpperCase());
}

export default function CompanyLayout() {
  const { user } = useSelector((s) => s.auth);
  const role = String(user?.role || '').toUpperCase();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const location = useLocation();

  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const canSeeFinance = ['ADMIN', 'DIRECTOR', 'MANAGER', 'ACCOUNTANT'].includes(role);
  const canSeeMaintenanceBoard = ['ADMIN', 'MAINTENANCE'].includes(role);
  const canSeeAdminDirector = ['ADMIN', 'DIRECTOR'].includes(role);

  const staffHomePath = role === 'ACCOUNTANT'
    ? '/staff/finance/invoices'
    : role === 'MAINTENANCE'
      ? '/staff/maintenance/board'
      : '/staff/rooms';

  const navItems = useMemo(() => {
    const items = [];

    items.push({
      type: 'link',
      to: staffHomePath,
      label: 'Dashboard',
      icon: Icons.Home,
      visible: true,
    });

    items.push({
      type: 'link',
      to: '/staff/rooms',
      label: 'Quản lý phòng',
      icon: Icons.Rooms,
      visible: role !== 'ACCOUNTANT',
    });

    items.push({
      type: 'link',
      to: '/staff/bookings',
      label: 'Quản lý đặt chỗ',
      icon: Icons.Booking,
      visible: ['ADMIN', 'RECEPTIONIST'].includes(role),
    });

    items.push({
      type: 'submenu',
      id: 'contracts',
      to: '/staff/contracts',
      label: 'Hợp đồng',
      icon: Icons.Contract,
      visible: ['ADMIN', 'MANAGER', 'RECEPTIONIST'].includes(role),
      children: [
        {
          to: '/staff/contracts/create',
          label: 'Tạo hợp đồng',
          icon: Icons.Clipboard,
          visible: ['ADMIN', 'RECEPTIONIST'].includes(role),
        },
      ],
      activePrefix: '/staff/contracts',
    });

    items.push({
      type: 'link',
      to: '/staff/posts/moderation',
      label: 'Duyệt tin',
      icon: Icons.Clipboard,
      visible: ['ADMIN', 'RECEPTIONIST'].includes(role),
    });

    items.push({
      type: 'link',
      to: '/staff/cleaning-bookings',
      label: 'Lịch vệ sinh',
      icon: Icons.Tools,
      visible: ['ADMIN', 'MANAGER'].includes(role),
    });

    items.push({
      type: 'link',
      to: '/staff/inspection',
      label: 'Kiểm tra phòng',
      icon: Icons.Shield,
      visible: role === 'MANAGER',
    });

    items.push({
      type: 'link',
      to: '/staff/maintenance/board',
      label: 'Bảo trì',
      icon: Icons.Tools,
      visible: canSeeMaintenanceBoard,
    });

    items.push({
      type: 'submenu',
      id: 'finance',
      to: '/staff/finance/invoices',
      label: 'Tài chính',
      icon: Icons.Finance,
      visible: canSeeFinance,
      children: [
        {
          to: '/staff/finance/invoices',
          label: 'Hóa đơn',
          icon: Icons.Finance,
          visible: true,
        },
        {
          to: '/staff/finance/generate',
          label: 'Tạo hóa đơn',
          icon: Icons.Finance,
          visible: ['ADMIN', 'ACCOUNTANT'].includes(role),
        },
        {
          to: '/staff/finance/meter-readings',
          label: 'Chỉ số điện nước',
          icon: Icons.Finance,
          visible: ['ADMIN', 'MANAGER'].includes(role),
        },
      ],
      activePrefix: '/staff/finance',
    });

    items.push({
      type: 'link',
      to: '/profile',
      label: 'Hồ sơ cá nhân',
      icon: Icons.Clipboard,
      visible: true,
    });

    items.push({
      type: 'submenu',
      id: 'admin',
      to: '/admin/dashboard',
      label: 'Quản trị',
      icon: Icons.Shield,
      visible: canSeeAdminDirector,
      activePrefix: '/admin',
      children: [
        {
          to: '/admin/dashboard',
          label: 'Dashboard Admin',
          icon: Icons.Shield,
          visible: true,
        },
        {
          to: '/admin/branches',
          label: 'Chi nhánh',
          icon: Icons.Rooms,
          visible: true,
        },
        {
          to: '/admin/users',
          label: 'Người dùng & NV',
          icon: Icons.Clipboard,
          visible: role === 'ADMIN',
        },
        {
          to: '/admin/config',
          label: 'Cấu hình hệ thống',
          icon: Icons.Tools,
          visible: true,
        },
        {
          to: '/admin/audit-logs',
          label: 'Audit logs',
          icon: Icons.Clipboard,
          visible: true,
        },
      ],
    });

    return items
      .map((it) => {
        if (it.type === 'submenu') {
          const children = (it.children || []).filter((c) => c.visible);
          return { ...it, children };
        }
        return it;
      })
      .filter((it) => {
        if (!it.visible) return false;
        if (it.type === 'submenu') return true;
        return true;
      });
  }, [role, canSeeFinance, canSeeMaintenanceBoard, canSeeAdminDirector, staffHomePath]);

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore
    } finally {
      dispatch(logout());
      navigate('/login', { replace: true });
    }
  };

  if (!isEmployee(role)) {
    // Safety: internal layout should only ever be used by employee roles.
    return <Outlet />;
  }

  const sidebarWidth = collapsed ? 'w-[72px]' : 'w-[280px]';

  const linkBase =
    'flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-medium transition-colors';
  const linkInactive = 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-soft)]';
  const linkActive = 'text-[color:var(--app-primary)] bg-[color:var(--app-primary-soft)]';

  const contentTitle = location.pathname.startsWith('/admin')
    ? 'Khu vực Admin'
    : 'Khu vực Nhân viên';

  return (
    // Force internal pages to use Default theme tokens (no seasonal palette)
    <div data-theme="default" className="h-screen overflow-hidden bg-[color:var(--app-bg)] text-[color:var(--app-text)]">
      <div className="flex h-screen">
        {/* Mobile overlay */}
        {mobileOpen && (
          <div
            className="fixed inset-0 z-40 bg-black/30 md:hidden"
            onClick={() => setMobileOpen(false)}
            aria-hidden="true"
          />
        )}

        {/* Sidebar */}
        <aside
          className={cn(
            'fixed md:sticky md:top-0 inset-y-0 left-0 z-50 border-r border-[color:var(--app-border)] bg-[color:var(--app-surface-solid)] transition-[width,transform] duration-200 md:h-screen',
            sidebarWidth,
            mobileOpen ? 'translate-x-0' : 'max-md:-translate-x-full'
          )}
        >
          <div className="h-full flex flex-col">
          <div className="h-16 flex items-center justify-between px-3 border-b border-[color:var(--app-border)] shrink-0">
            <button
              type="button"
              className="md:hidden inline-flex items-center justify-center h-10 w-10 rounded-xl hover:bg-[color:var(--app-primary-soft)] text-[color:var(--app-muted)]"
              onClick={() => setMobileOpen(false)}
              aria-label="Đóng menu"
            >
              <Icons.ChevronLeft className="h-5 w-5" />
            </button>

            <div className={cn('flex items-center gap-2 min-w-0', collapsed && 'justify-center w-full')}
            >
              <img
                src="/logo/logo_1.png"
                alt="Alpha"
                className="h-8 w-8"
                draggable="false"
              />
              {!collapsed && (
                <div className="min-w-0">
                  <div className="text-sm font-extrabold tracking-tight text-[color:var(--app-primary)] truncate">Alpha Work</div>
                  <div className="text-[11px] text-[color:var(--app-muted-2)] truncate">{user?.fullName || user?.username || 'Tài khoản nội bộ'}</div>
                </div>
              )}
            </div>

            <button
              type="button"
              className="hidden md:inline-flex items-center justify-center h-10 w-10 rounded-xl hover:bg-[color:var(--app-primary-soft)] text-[color:var(--app-muted)]"
              onClick={() => setCollapsed((v) => !v)}
              aria-label={collapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'}
              title={collapsed ? 'Mở rộng sidebar' : 'Thu gọn sidebar'}
            >
              {collapsed ? <Icons.ChevronRight className="h-5 w-5" /> : <Icons.ChevronLeft className="h-5 w-5" />}
            </button>
          </div>

          <nav className="flex-1 overflow-y-auto p-3">
            <div className="space-y-1">
              {navItems.map((it) => {
                if (it.type === 'submenu') {
                  const isActive = it.activePrefix ? location.pathname.startsWith(it.activePrefix) : false;
                  const IconCmp = it.icon;
                  return (
                    <div key={it.id}>
                      <NavLink
                        to={it.to}
                        onClick={() => setMobileOpen(false)}
                        className={({ isActive: linkIsActive }) => cn(linkBase, (linkIsActive || isActive) ? linkActive : linkInactive)}
                        title={collapsed ? it.label : undefined}
                      >
                        <IconCmp className="h-5 w-5 shrink-0" />
                        {!collapsed && <span className="truncate">{it.label}</span>}
                      </NavLink>

                      {!collapsed && (it.children || []).length > 0 && (
                        <div className="mt-1 ml-3 pl-3 border-l border-[color:var(--app-border)] space-y-1">
                          {it.children.map((c) => {
                            const ChildIcon = c.icon;
                            return (
                              <NavLink
                                key={c.to}
                                to={c.to}
                                onClick={() => setMobileOpen(false)}
                                className={({ isActive: childActive }) => cn(
                                  'flex items-center gap-3 rounded-xl px-3 py-2 text-sm font-medium transition-colors',
                                  childActive ? linkActive : linkInactive
                                )}
                              >
                                <ChildIcon className="h-4 w-4 shrink-0" />
                                <span className="truncate">{c.label}</span>
                              </NavLink>
                            );
                          })}
                        </div>
                      )}
                    </div>
                  );
                }

                const IconCmp = it.icon;
                return (
                  <NavLink
                    key={it.to}
                    to={it.to}
                    onClick={() => setMobileOpen(false)}
                    className={({ isActive }) => cn(linkBase, isActive ? linkActive : linkInactive)}
                    title={collapsed ? it.label : undefined}
                  >
                    <IconCmp className="h-5 w-5 shrink-0" />
                    {!collapsed && <span className="truncate">{it.label}</span>}
                  </NavLink>
                );
              })}
            </div>

            <div className="pt-3 mt-3 border-t border-[color:var(--app-border)]" />

            <button
              type="button"
              onClick={handleLogout}
              className={cn(linkBase, linkInactive, 'w-full')}
              title={collapsed ? 'Đăng xuất' : undefined}
            >
              <Icons.Logout className="h-5 w-5 shrink-0" />
              {!collapsed && <span className="truncate">Đăng xuất</span>}
            </button>
          </nav>
          </div>
        </aside>

        {/* Main content */}
        <div className={cn('flex-1 min-w-0 flex flex-col h-screen')}
        >
          {/* Top bar */}
          <div className="h-16 shrink-0 flex items-center gap-3 px-4 md:px-6 border-b border-[color:var(--app-border)] bg-[color:var(--app-surface)] backdrop-blur-md">
            <button
              type="button"
              className="md:hidden inline-flex items-center justify-center h-10 w-10 rounded-xl hover:bg-[color:var(--app-primary-soft)] text-[color:var(--app-muted)]"
              onClick={() => setMobileOpen(true)}
              aria-label="Mở menu"
            >
              <Icons.Menu className="h-5 w-5" />
            </button>
            <div className="min-w-0">
              <div className="text-sm font-bold text-[color:var(--app-text)] truncate">{contentTitle}</div>
              <div className="text-xs text-[color:var(--app-muted-2)] truncate">Vai trò: {role || '-'}</div>
            </div>
          </div>

          <main className="flex-1 overflow-y-auto px-4 py-6 md:px-6">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}

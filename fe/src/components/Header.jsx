import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../features/auth/authSlice';
import authApi from '../api/authApi';
import HeaderThemeMenu from './theme/shared/HeaderThemeMenu';
import SummerHeaderDecoration from './theme/summer/SummerHeaderDecoration';
import HangingHeaderDecorations from './theme/shared/HangingHeaderDecorations';

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const { currentTheme, isTransitioning } = useSelector((state) => state.theme);
  const [showDropdown, setShowDropdown] = useState(false);

  const isChristmas = currentTheme === 'christmas';
  const isSummer = currentTheme === 'summer';

  const role = user?.role;
  const isEmployeeRole = ['ADMIN', 'DIRECTOR', 'MANAGER', 'ACCOUNTANT', 'RECEPTIONIST', 'MAINTENANCE', 'SECURITY'].includes(role);
  const canSeeFinance = ['ADMIN', 'DIRECTOR', 'MANAGER', 'ACCOUNTANT'].includes(role);
  const canSeeMaintenanceBoard = ['ADMIN', 'MAINTENANCE'].includes(role);
  const canSeeAdminDirector = ['ADMIN', 'DIRECTOR'].includes(role);

  const staffHomePath = role === 'ACCOUNTANT'
    ? '/staff/finance/invoices'
    : role === 'MAINTENANCE'
      ? '/staff/maintenance/board'
      : '/staff/rooms';

  const isActive = (path) => location.pathname === path;
  const isPartnerPortalActive = location.pathname === '/partner' || location.pathname.startsWith('/partner/');

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch (e) {
      // Ignore network/logout API errors; still clear local session
    } finally {
      dispatch(logout());
      setShowDropdown(false);
      navigate('/login');
    }
  };

  const displayUsername = user?.username || 'User';
  const displayChar = displayUsername.charAt(0).toUpperCase();

  return (
    <header className="bg-[color:var(--app-surface)] backdrop-blur-md sticky top-0 z-50 border-b border-[color:var(--app-border)] relative transition-all duration-300">
      <HangingHeaderDecorations
        variant={isChristmas ? 'christmas' : isSummer ? 'summer' : null}
        paused={isTransitioning}
      />
      <div className="container mx-auto px-6 h-20 grid grid-cols-[1fr_auto_1fr] items-center relative z-10">

        {/* Summer header decoration (sun + clouds + birds + parallax waves) */}
        <SummerHeaderDecoration isSummer={isSummer} paused={isTransitioning} />

        {/* Logo */}
        <Link
          to="/"
          className="justify-self-start relative text-[color:var(--app-primary)] tracking-tight flex items-center gap-2 group"
          aria-label="Alpha"
        >
          {isChristmas && (
            <span className="absolute -top-3 -left-2 text-2xl transform -rotate-12 filter drop-shadow-sm group-hover:rotate-0 transition-transform cursor-default">
              🎅
            </span>
          )}
          <img
            src="/logo/logo_1.png"
            alt="Alpha"
            className="h-9 w-9 sm:hidden"
            draggable="false"
            loading="eager"
          />
          <img
            src="/logo/logo_2.png"
            alt="Alpha"
            className="hidden sm:block h-9 w-auto"
            draggable="false"
            loading="eager"
          />
          <span className="sr-only">Alpha</span>
        </Link>

        {/* Menu Desktop */}
        <nav className="hidden md:flex items-center space-x-10 justify-self-center">
          <Link
            to="/"
            className={`relative font-medium transition-colors duration-200 ${isActive('/') ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
          >
            Trang chủ
            {isChristmas && isActive('/') && <span className="absolute -top-3 -right-3 text-xs animate-bounce">🎄</span>}
          </Link>
          <Link
            to="/partner-posts"
            className={`relative font-medium transition-colors duration-200 ${isActive('/partner-posts') ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
          >
            Các đơn vị khác
          </Link>
          <Link
            to="/about"
            className={`relative font-medium transition-colors duration-200 ${isActive('/about') ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
          >
            Về chúng tôi
            {isChristmas && isActive('/about') && <span className="absolute -top-3 -right-3 text-xs animate-bounce">🎁</span>}
          </Link>

          {/* Quick portal link */}
          {user?.role === 'TENANT' && (
            <Link
              to="/tenant/dashboard"
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/tenant') ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
            >
              Cổng Sinh viên
            </Link>
          )}
          {user?.role === 'PARTNER' && (
            <Link
              to="/partner/dashboard"
              className={`relative font-medium transition-colors duration-200 ${isPartnerPortalActive ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
            >
              Kênh Đối tác
            </Link>
          )}
          {isEmployeeRole && (
            <Link
              to={staffHomePath}
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/staff') ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
            >
              Nội bộ
            </Link>
          )}
          {canSeeAdminDirector && (
            <Link
              to="/admin/dashboard"
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/admin') ? 'text-[color:var(--app-primary)]' : 'text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)]'}`}
            >
              Admin
            </Link>
          )}
        </nav>

        {/* Auth Actions */}
        <div className="flex items-center space-x-4 justify-self-end">
          {user && (
            <div className="hidden md:block">
              <HeaderThemeMenu />
            </div>
          )}
          {user ? (
            // --- ĐÃ ĐĂNG NHẬP ---
            <div className="relative">
              <button
                onClick={() => setShowDropdown(!showDropdown)}
                className="flex items-center gap-2 focus:outline-none bg-[color:var(--app-surface-solid)] hover:bg-[color:var(--app-primary-soft)] px-3 py-1.5 rounded-full border border-[color:var(--app-border-strong)] transition-all shadow-sm"
              >
                <div className="w-8 h-8 rounded-full bg-[color:var(--app-primary)] text-white flex items-center justify-center font-bold text-sm">
                  {displayChar}
                </div>
                <div className="hidden md:flex flex-col items-start text-left">
                  <span className="font-bold text-[color:var(--app-text)] text-sm leading-tight max-w-[150px] truncate">
                    {displayUsername}
                  </span>
                </div>
                <svg className={`w-4 h-4 text-[color:var(--app-muted-2)] transition-transform ${showDropdown ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
              </button>

              {/* Dropdown Menu */}
              {showDropdown && (
                <div className="absolute right-0 mt-2 w-64 bg-[color:var(--app-surface-solid)] rounded-xl shadow-xl border border-[color:var(--app-border)] z-50 animate-fade-in-up origin-top-right max-h-[70vh] flex flex-col">
                  <div className="px-4 py-3 border-b border-[color:var(--app-border)] bg-[color:var(--app-primary-soft)] flex-shrink-0">
                    <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Tài khoản</p>
                    <p className="text-sm font-bold text-[color:var(--app-primary)] truncate mt-1">{displayUsername}</p>
                    <p className="text-xs text-[color:var(--app-muted)] mt-0.5">{user.role}</p>
                  </div>

                  <div className="py-1 overflow-y-auto flex-1">
                    {/* Mục chung */}
                    <Link
                      to="/profile"
                      className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                      onClick={() => setShowDropdown(false)}
                    >
                      <span>👤</span> Hồ sơ cá nhân
                    </Link>

                    {/* ===================== */}
                    {/* TENANT PORTAL */}
                    {/* ===================== */}
                    {role === 'TENANT' && (
                      <>
                        <div className="px-4 pt-3 pb-1">
                          <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Sinh viên</p>
                        </div>
                        <Link
                          to="/tenant/dashboard"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📊</span> Tổng quan
                        </Link>
                        <Link
                          to="/tenant/reservations"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📅</span> Lịch sử giữ chỗ
                        </Link>
                        <Link
                          to="/tenant/contracts"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📝</span> Hợp đồng của tôi
                        </Link>
                        <Link
                          to="/tenant/invoices"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>💳</span> Hóa đơn & Thanh toán
                        </Link>
                        <Link
                          to="/tenant/maintenance"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>🛠️</span> Yêu cầu bảo trì
                        </Link>
                      </>
                    )}

                    {/* ===================== */}
                    {/* PARTNER PORTAL */}
                    {/* ===================== */}
                    {role === 'PARTNER' && (
                      <>
                        <div className="px-4 pt-3 pb-1">
                          <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Đối tác</p>
                        </div>
                        <Link
                          to="/partner/dashboard"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📊</span> Tổng quan
                        </Link>
                        <Link
                          to="/partner/my-listings"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📝</span> Quản lý tin đăng
                        </Link>
                        <Link
                          to="/partner/create-listing"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>➕</span> Đăng tin mới
                        </Link>
                        <Link
                          to="/partner/packages"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>💎</span> Mua gói dịch vụ
                        </Link>
                      </>
                    )}

                    {/* ===================== */}
                    {/* GUEST PORTAL */}
                    {/* ===================== */}
                    {role === 'GUEST' && (
                      <>
                        <div className="px-4 pt-3 pb-1">
                          <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Khách</p>
                        </div>
                        <Link
                          to="/guest/my-reservations"
                          className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📅</span> Lịch sử đặt phòng
                        </Link>
                      </>
                    )}

                    {/* ===================== */}
                    {/* STAFF PORTAL */}
                    {/* ===================== */}
                    {isEmployeeRole && (
                      <>
                        <div className="px-4 pt-3 pb-1">
                          <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Nội bộ</p>
                        </div>
                        {role === 'MAINTENANCE' ? (
                          canSeeMaintenanceBoard && (
                            <Link
                              to="/staff/maintenance/board"
                              className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>🛠️</span> Bảng bảo trì
                            </Link>
                          )
                        ) : (
                          <>
                            {role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/rooms"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>🏢</span> Quản lý phòng
                              </Link>
                            )}

                            {['ADMIN', 'MANAGER'].includes(role) && (
                              <Link
                                to="/staff/cleaning-bookings"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>🧹</span> Lịch vệ sinh
                              </Link>
                            )}

                            {['ADMIN', 'MANAGER'].includes(role) && (
                              <Link
                                to="/staff/finance/meter-readings"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>⚡</span> Nhập chỉ số điện/nước
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'MANAGER' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/bookings"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>📅</span> Đặt chỗ
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'MANAGER' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/contracts/create"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>📝</span> Tạo hợp đồng
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/inspection"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>🧰</span> Biên bản bàn giao
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'MANAGER' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/posts/moderation"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>📰</span> Duyệt tin đối tác
                              </Link>
                            )}

                            {canSeeFinance && (
                              <>
                                <div className="px-4 pt-3 pb-1">
                                  <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Tài chính</p>
                                </div>
                                <Link
                                  to="/staff/finance/invoices"
                                  className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                  onClick={() => setShowDropdown(false)}
                                >
                                  <span>🧾</span> Hóa đơn & Doanh thu
                                </Link>
                                {role === 'ACCOUNTANT' && (
                                  <Link
                                    to="/staff/finance/generate"
                                    className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                    onClick={() => setShowDropdown(false)}
                                  >
                                    <span>➕</span> Tạo hóa đơn tháng
                                  </Link>
                                )}
                              </>
                            )}

                            {canSeeMaintenanceBoard && (
                              <>
                                <div className="px-4 pt-3 pb-1">
                                  <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Kỹ thuật</p>
                                </div>
                                <Link
                                  to="/staff/maintenance/board"
                                  className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                  onClick={() => setShowDropdown(false)}
                                >
                                  <span>🛠️</span> Bảng bảo trì
                                </Link>
                              </>
                            )}
                          </>
                        )}

                        {canSeeAdminDirector && (
                          <>
                            <div className="px-4 pt-3 pb-1">
                              <p className="text-xs text-[color:var(--app-muted-2)] uppercase font-bold tracking-wider">Admin & Giám đốc</p>
                            </div>
                            <Link
                              to="/admin/dashboard"
                              className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>📊</span> Dashboard giám đốc
                            </Link>
                            <Link
                              to="/admin/branches"
                              className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>🏢</span> Quản lý chi nhánh
                            </Link>
                            {role === 'ADMIN' && (
                              <Link
                                to="/admin/users"
                                className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>👥</span> Quản lý người dùng
                              </Link>
                            )}
                            <Link
                              to="/admin/config"
                              className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>⚙️</span> Cấu hình hệ thống
                            </Link>
                            <Link
                              to="/admin/audit-logs"
                              className="flex items-center px-4 py-2 text-sm text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] hover:text-[color:var(--app-primary)] transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>🧾</span> Audit logs
                            </Link>
                          </>
                        )}
                      </>
                    )}
                  </div>

                  <div className="border-t border-[color:var(--app-border)] my-1 flex-shrink-0"></div>
                  <button
                    onClick={handleLogout}
                    className="flex w-full items-center px-4 py-2 text-sm text-red-600 hover:bg-red-50 font-medium transition-colors gap-2 flex-shrink-0"
                  >
                    <span>🚪</span> Đăng xuất
                  </button>
                </div>
              )}
            </div>
          ) : (
            // --- CHƯA ĐĂNG NHẬP ---
            <>
              <Link to="/login" className="hidden md:block text-[color:var(--app-muted)] hover:text-[color:var(--app-primary)] font-medium transition-colors">
                Đăng nhập
              </Link>
              <Link to="/register" className="relative bg-[color:var(--app-primary)] text-white px-5 py-2.5 rounded-full font-semibold hover:bg-[color:var(--app-primary-hover)] transition-all shadow-lg text-sm">
                Đăng ký ngay
              </Link>
            </>
          )}
        </div>

        {/* Decoration */}
        {isChristmas && (
          <div className="absolute top-0 right-10 text-3xl transform origin-top animate-swing hidden lg:block opacity-80 pointer-events-none">
            🔔
          </div>
        )}
      </div>
    </header>
  );
}
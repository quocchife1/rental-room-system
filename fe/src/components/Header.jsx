import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../features/auth/authSlice';
import authApi from '../api/authApi';
import { CURRENT_SEASON } from './SeasonalEffects';

export default function Header() {
  const location = useLocation();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { user } = useSelector((state) => state.auth);
  const [showDropdown, setShowDropdown] = useState(false);

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
    <header className="bg-white/90 backdrop-blur-md sticky top-0 z-50 border-b border-gray-100 relative transition-all duration-300">
      <div className="container mx-auto px-6 h-20 flex items-center justify-between">

        {/* Logo */}
        <Link to="/" className="relative text-2xl font-extrabold text-indigo-600 tracking-tight flex items-center gap-2 group">
          {CURRENT_SEASON === 'CHRISTMAS' && (
            <span className="absolute -top-3 -left-2 text-2xl transform -rotate-12 filter drop-shadow-sm group-hover:rotate-0 transition-transform cursor-default">🎅</span>
          )}
          <span className="text-3xl">🏠</span>
          <span>UML Rental</span>
        </Link>

        {/* Menu Desktop */}
        <nav className="hidden md:flex items-center space-x-10">
          <Link
            to="/"
            className={`relative font-medium transition-colors duration-200 ${isActive('/') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
          >
            Trang chủ
            {CURRENT_SEASON === 'CHRISTMAS' && isActive('/') && <span className="absolute -top-3 -right-3 text-xs animate-bounce">🎄</span>}
          </Link>
          <Link
            to="/partner-posts"
            className={`relative font-medium transition-colors duration-200 ${isActive('/partner-posts') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
          >
            Các đơn vị khác
          </Link>
          <Link
            to="/about"
            className={`relative font-medium transition-colors duration-200 ${isActive('/about') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
          >
            Về chúng tôi
            {CURRENT_SEASON === 'CHRISTMAS' && isActive('/about') && <span className="absolute -top-3 -right-3 text-xs animate-bounce">🎁</span>}
          </Link>

          {/* Quick portal link */}
          {user?.role === 'TENANT' && (
            <Link
              to="/tenant/dashboard"
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/tenant') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
            >
              Cổng Sinh viên
            </Link>
          )}
          {user?.role === 'PARTNER' && (
            <Link
              to="/partner/dashboard"
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/partner') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
            >
              Kênh Đối tác
            </Link>
          )}
          {isEmployeeRole && (
            <Link
              to={staffHomePath}
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/staff') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
            >
              Nội bộ
            </Link>
          )}
          {canSeeAdminDirector && (
            <Link
              to="/admin/dashboard"
              className={`relative font-medium transition-colors duration-200 ${location.pathname.startsWith('/admin') ? 'text-indigo-600' : 'text-gray-500 hover:text-indigo-600'}`}
            >
              Admin
            </Link>
          )}
        </nav>

        {/* Auth Actions */}
        <div className="flex items-center space-x-4">
          {user ? (
            // --- ĐÃ ĐĂNG NHẬP ---
            <div className="relative">
              <button
                onClick={() => setShowDropdown(!showDropdown)}
                className="flex items-center gap-2 focus:outline-none bg-white hover:bg-indigo-50 px-3 py-1.5 rounded-full border border-gray-200 transition-all shadow-sm"
              >
                <div className="w-8 h-8 rounded-full bg-indigo-600 text-white flex items-center justify-center font-bold text-sm">
                  {displayChar}
                </div>
                <div className="hidden md:flex flex-col items-start text-left">
                  <span className="font-bold text-gray-700 text-sm leading-tight max-w-[150px] truncate">
                    {displayUsername}
                  </span>
                </div>
                <svg className={`w-4 h-4 text-gray-400 transition-transform ${showDropdown ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
              </button>

              {/* Dropdown Menu */}
              {showDropdown && (
                <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-xl border border-gray-100 z-50 animate-fade-in-up origin-top-right max-h-[70vh] flex flex-col">
                  <div className="px-4 py-3 border-b border-gray-50 bg-gray-50/50 flex-shrink-0">
                    <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Tài khoản</p>
                    <p className="text-sm font-bold text-indigo-600 truncate mt-1">{displayUsername}</p>
                    <p className="text-xs text-gray-500 mt-0.5">{user.role}</p>
                  </div>

                  <div className="py-1 overflow-y-auto flex-1">
                    {/* Mục chung */}
                    <Link
                      to="/profile"
                      className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                          <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Sinh viên</p>
                        </div>
                        <Link
                          to="/tenant/dashboard"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📊</span> Tổng quan
                        </Link>
                        <Link
                          to="/tenant/reservations"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📅</span> Lịch sử giữ chỗ
                        </Link>
                        <Link
                          to="/tenant/contracts"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📝</span> Hợp đồng của tôi
                        </Link>
                        <Link
                          to="/tenant/invoices"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>💳</span> Hóa đơn & Thanh toán
                        </Link>
                        <Link
                          to="/tenant/maintenance"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                          <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Đối tác</p>
                        </div>
                        <Link
                          to="/partner/dashboard"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📊</span> Tổng quan
                        </Link>
                        <Link
                          to="/partner/my-listings"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>📝</span> Quản lý tin đăng
                        </Link>
                        <Link
                          to="/partner/create-listing"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                          onClick={() => setShowDropdown(false)}
                        >
                          <span>➕</span> Đăng tin mới
                        </Link>
                        <Link
                          to="/partner/packages"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                          <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Khách</p>
                        </div>
                        <Link
                          to="/guest/my-reservations"
                          className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                          <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Nội bộ</p>
                        </div>
                        {role === 'MAINTENANCE' ? (
                          canSeeMaintenanceBoard && (
                            <Link
                              to="/staff/maintenance/board"
                              className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>🏢</span> Quản lý phòng
                              </Link>
                            )}

                            {['ADMIN', 'MANAGER'].includes(role) && (
                              <Link
                                to="/staff/cleaning-bookings"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>🧹</span> Lịch vệ sinh
                              </Link>
                            )}

                            {['ADMIN', 'MANAGER'].includes(role) && (
                              <Link
                                to="/staff/finance/meter-readings"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>⚡</span> Nhập chỉ số điện/nước
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'MANAGER' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/bookings"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>📅</span> Đặt chỗ
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'MANAGER' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/contracts/create"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>📝</span> Tạo hợp đồng
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/inspection"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>🧰</span> Biên bản bàn giao
                              </Link>
                            )}
                            {role !== 'DIRECTOR' && role !== 'MANAGER' && role !== 'ACCOUNTANT' && (
                              <Link
                                to="/staff/posts/moderation"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>📰</span> Duyệt tin đối tác
                              </Link>
                            )}

                            {canSeeFinance && (
                              <>
                                <div className="px-4 pt-3 pb-1">
                                  <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Tài chính</p>
                                </div>
                                <Link
                                  to="/staff/finance/invoices"
                                  className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                  onClick={() => setShowDropdown(false)}
                                >
                                  <span>🧾</span> Hóa đơn & Doanh thu
                                </Link>
                                {role === 'ACCOUNTANT' && (
                                  <Link
                                    to="/staff/finance/generate"
                                    className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                                  <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Kỹ thuật</p>
                                </div>
                                <Link
                                  to="/staff/maintenance/board"
                                  className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
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
                              <p className="text-xs text-gray-400 uppercase font-bold tracking-wider">Admin & Giám đốc</p>
                            </div>
                            <Link
                              to="/admin/dashboard"
                              className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>📊</span> Dashboard giám đốc
                            </Link>
                            <Link
                              to="/admin/branches"
                              className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>🏢</span> Quản lý chi nhánh
                            </Link>
                            {role === 'ADMIN' && (
                              <Link
                                to="/admin/users"
                                className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                                onClick={() => setShowDropdown(false)}
                              >
                                <span>👥</span> Quản lý người dùng
                              </Link>
                            )}
                            <Link
                              to="/admin/config"
                              className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>⚙️</span> Cấu hình hệ thống
                            </Link>
                            <Link
                              to="/admin/audit-logs"
                              className="flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors gap-2"
                              onClick={() => setShowDropdown(false)}
                            >
                              <span>🧾</span> Audit logs
                            </Link>
                          </>
                        )}
                      </>
                    )}
                  </div>

                  <div className="border-t border-gray-100 my-1 flex-shrink-0"></div>
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
              <Link to="/login" className="hidden md:block text-gray-500 hover:text-indigo-600 font-medium transition-colors">
                Đăng nhập
              </Link>
              <Link to="/register" className="relative bg-indigo-600 text-white px-5 py-2.5 rounded-full font-semibold hover:bg-indigo-700 transition-all shadow-lg text-sm">
                Đăng ký ngay
              </Link>
            </>
          )}
        </div>

        {/* Decoration */}
        {CURRENT_SEASON === 'CHRISTMAS' && (
          <div className="absolute top-0 right-10 text-3xl transform origin-top animate-[swing_3s_ease-in-out_infinite] hidden lg:block opacity-80 pointer-events-none">
            🔔
          </div>
        )}
      </div>
    </header>
  );
}
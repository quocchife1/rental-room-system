import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import userApi from '../../api/userApi';
import authApi from '../../api/authApi';
import { logout } from '../../features/auth/authSlice';

function formatDate(value) {
  if (!value) return '---';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '---';
  return date.toLocaleDateString('vi-VN');
}

export default function EmployeeProfilePage() {
  const { user } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let alive = true;

    const loadProfile = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await userApi.getCurrentEmployeeProfile();
        const data = res?.data?.result || res?.data || res || null;
        if (alive) setProfile(data || null);
      } catch (err) {
        if (alive) {
          setProfile(null);
          setError(err?.response?.data?.message || err?.message || 'Không tải được hồ sơ nhân viên');
        }
      } finally {
        if (alive) setLoading(false);
      }
    };

    if (user?.id) {
      loadProfile();
    } else {
      setLoading(false);
    }

    return () => {
      alive = false;
    };
  }, [user?.id]);

  const handleLogout = async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore logout API failure and still clear local session
    } finally {
      dispatch(logout());
      navigate('/login', { replace: true });
    }
  };

  const branchCode = profile?.branch?.branchCode || '---';
  const branchName = profile?.branch?.branchName || '---';
  const employeeName = profile?.fullName || user?.fullName || user?.username || 'Nhân viên';
  const initials = employeeName.charAt(0).toUpperCase();

  return (
    <div className="min-h-screen bg-[linear-gradient(135deg,var(--app-bg)_0%,var(--app-surface-solid)_45%,var(--app-primary-soft)_100%)] text-[color:var(--app-text)] p-4 md:p-8">
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-[color:var(--app-border)] bg-[color:var(--app-surface-solid)] text-xs font-bold uppercase tracking-wider text-[color:var(--app-primary)]">
              Hồ sơ nhân viên nội bộ
            </div>
            <h1 className="mt-3 text-3xl md:text-4xl font-extrabold tracking-tight">{employeeName}</h1>
            <p className="mt-2 text-[color:var(--app-muted)] max-w-2xl">
              Trang hồ sơ riêng cho nhân viên nội bộ. Thông tin chi nhánh và vị trí làm việc được lấy trực tiếp từ hệ thống nhân sự.
            </p>
          </div>

          <div className="flex gap-3 flex-wrap">
            <Link
              to="/staff/rooms"
              className="inline-flex items-center justify-center px-5 py-2.5 rounded-xl border border-[color:var(--app-border)] bg-[color:var(--app-surface-solid)] hover:bg-[color:var(--app-primary-soft)] font-semibold transition-colors"
            >
              Về khu vực làm việc
            </Link>
            <button
              onClick={handleLogout}
              className="inline-flex items-center justify-center px-5 py-2.5 rounded-xl bg-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-hover)] text-white font-bold transition-colors shadow-md"
            >
              Đăng xuất
            </button>
          </div>
        </div>

        {error && (
          <div className="rounded-xl border border-rose-200 bg-rose-50 text-rose-700 px-4 py-3 text-sm font-medium">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-[320px_1fr] gap-6">
          <div className="bg-[color:var(--app-surface-solid)] rounded-3xl border border-[color:var(--app-border)] shadow-sm overflow-hidden">
            <div className="p-8 bg-[linear-gradient(180deg,var(--app-primary-soft)_0%,transparent_100%)]">
              <div className="w-28 h-28 rounded-full bg-[color:var(--app-primary)] text-white flex items-center justify-center text-4xl font-extrabold shadow-lg">
                {initials}
              </div>
              <div className="mt-5">
                <div className="text-2xl font-bold leading-tight">{employeeName}</div>
                <div className="text-sm text-[color:var(--app-muted)] mt-1">{profile?.employeeCode || 'Chưa có mã nhân viên'}</div>
              </div>
              <div className="mt-4 inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-[color:var(--app-border)] bg-white text-xs font-bold uppercase tracking-wider">
                {profile?.position || user?.role || 'EMPLOYEE'}
              </div>
            </div>

            <div className="px-8 pb-8 space-y-4">
              <div>
                <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Chi nhánh</div>
                <div className="mt-1 text-base font-semibold">{branchName}</div>
                <div className="text-sm text-[color:var(--app-muted)]">Mã chi nhánh: {branchCode}</div>
              </div>
              <div>
                <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Trạng thái</div>
                <div className="mt-1 text-base font-semibold">{profile?.status || '---'}</div>
              </div>
              <div>
                <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Ngày vào làm</div>
                <div className="mt-1 text-base font-semibold">{formatDate(profile?.hireDate)}</div>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
              {[
                { label: 'Tên đăng nhập', value: profile?.username || user?.username || '-' },
                { label: 'Họ và tên', value: profile?.fullName || user?.fullName || '-' },
                { label: 'Email', value: profile?.email || user?.email || '-' },
                { label: 'Số điện thoại', value: profile?.phoneNumber || user?.phoneNumber || '-' },
              ].map((item) => (
                <div key={item.label} className="bg-[color:var(--app-surface-solid)] rounded-2xl border border-[color:var(--app-border)] p-5 shadow-sm">
                  <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">{item.label}</div>
                  <div className="mt-2 text-base font-semibold break-words">{item.value}</div>
                </div>
              ))}
            </div>

            <div className="bg-[color:var(--app-surface-solid)] rounded-3xl border border-[color:var(--app-border)] shadow-sm p-6 md:p-8">
              <div className="flex items-center justify-between gap-4 border-b border-[color:var(--app-border)] pb-4 mb-6">
                <div>
                  <h2 className="text-xl font-bold">Thông tin làm việc</h2>
                  <p className="text-sm text-[color:var(--app-muted)] mt-1">Dữ liệu này được lấy từ hồ sơ nhân sự nội bộ.</p>
                </div>
              </div>

              {loading ? (
                <div className="py-12 text-center text-[color:var(--app-muted)]">Đang tải hồ sơ...</div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-5 text-sm">
                  <div className="rounded-2xl border border-[color:var(--app-border)] bg-[color:var(--app-bg)] p-4">
                    <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Mã nhân viên</div>
                    <div className="mt-2 text-lg font-semibold">{profile?.employeeCode || '---'}</div>
                  </div>
                  <div className="rounded-2xl border border-[color:var(--app-border)] bg-[color:var(--app-bg)] p-4">
                    <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Chức vụ</div>
                    <div className="mt-2 text-lg font-semibold">{profile?.position || '---'}</div>
                  </div>
                  <div className="rounded-2xl border border-[color:var(--app-border)] bg-[color:var(--app-bg)] p-4">
                    <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Chi nhánh</div>
                    <div className="mt-2 text-lg font-semibold">{branchName}</div>
                    <div className="text-xs text-[color:var(--app-muted)] mt-1">{branchCode}</div>
                  </div>
                  <div className="rounded-2xl border border-[color:var(--app-border)] bg-[color:var(--app-bg)] p-4">
                    <div className="text-xs font-bold uppercase tracking-wider text-[color:var(--app-muted-2)]">Ngày vào làm</div>
                    <div className="mt-2 text-lg font-semibold">{formatDate(profile?.hireDate)}</div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { loginUser } from '../features/auth/authSlice';
import AuthLayout from '../components/AuthLayout';

export default function LoginPage() {
  const [formData, setFormData] = useState({ username: '', password: '' });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, user } = useSelector((state) => state.auth);

  const employeeRoles = ['ADMIN', 'DIRECTOR', 'MANAGER', 'ACCOUNTANT', 'RECEPTIONIST', 'MAINTENANCE', 'SECURITY'];

  useEffect(() => {
    if (!user) return;

    const role = String(user?.role || '').toUpperCase();
    if (employeeRoles.includes(role)) {
      navigate('/staff', { replace: true });
      return;
    }

    if (role === 'TENANT') {
      navigate('/tenant/dashboard', { replace: true });
      return;
    }

    if (role === 'PARTNER') {
      navigate('/partner/dashboard', { replace: true });
      return;
    }

    if (role === 'GUEST') {
      navigate('/', { replace: true });
      return;
    }

    navigate('/', { replace: true });
  }, [user, navigate]);

  const handleSubmit = (e) => {
    e.preventDefault();
    dispatch(loginUser(formData));
  };

  return (
    <AuthLayout 
      title="Đăng nhập tài khoản" 
      subtitle={
        <>
          Hoặc <Link to="/register" className="font-medium text-[color:var(--app-primary)] hover:text-[color:var(--app-primary-hover)]">đăng ký tài khoản mới</Link>
        </>
      }
    >
      {error && (
        <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 text-sm text-red-700 rounded-r-md">
          {error}
        </div>
      )}

      <form className="space-y-6" onSubmit={handleSubmit}>
        <div>
          <label className="block text-sm font-medium text-[color:var(--app-muted)]">Tên đăng nhập</label>
          <div className="mt-1">
            <input
              type="text"
              required
              className="appearance-none block w-full px-3 py-3 border border-[color:var(--app-border-strong)] bg-[color:var(--app-surface-solid)] text-[color:var(--app-text)] rounded-xl shadow-sm placeholder:text-[color:var(--app-muted-2)] focus:outline-none focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] sm:text-sm transition-all"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-[color:var(--app-muted)]">Mật khẩu</label>
          <div className="mt-1">
            <input
              type="password"
              required
              className="appearance-none block w-full px-3 py-3 border border-[color:var(--app-border-strong)] bg-[color:var(--app-surface-solid)] text-[color:var(--app-text)] rounded-xl shadow-sm placeholder:text-[color:var(--app-muted-2)] focus:outline-none focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] sm:text-sm transition-all"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            />
          </div>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <input id="remember-me" type="checkbox" className="h-4 w-4 text-[color:var(--app-primary)] focus:ring-2 focus:ring-[color:var(--app-primary-soft)] border-[color:var(--app-border-strong)] rounded" />
            <label htmlFor="remember-me" className="ml-2 block text-sm text-[color:var(--app-text)]">
              Ghi nhớ
            </label>
          </div>
          <div className="text-sm">
            <a href="#" className="font-medium text-[color:var(--app-primary)] hover:text-[color:var(--app-primary-hover)]">
              Quên mật khẩu?
            </a>
          </div>
        </div>

        <div>
          <button
            type="submit"
            disabled={loading}
            className="w-full flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-hover)] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-[color:var(--app-primary-soft)] transition-all transform active:scale-95"
          >
            {loading ? 'Đang xử lý...' : 'Đăng nhập'}
          </button>
        </div>
      </form>
    </AuthLayout>
  );
}
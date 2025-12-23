import React, { useEffect, useState } from 'react';
import adminUsersApi from '../../api/adminUsersApi';

export default function UserEmployeeManagementPage() {
  const [tab, setTab] = useState('employees');
  const [employees, setEmployees] = useState([]);
  const [tenants, setTenants] = useState([]);
  const [partners, setPartners] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const [e, t, p] = await Promise.all([
        adminUsersApi.listEmployees(),
        adminUsersApi.listTenants(),
        adminUsersApi.listPartners(),
      ]);
      setEmployees(Array.isArray(e) ? e : (e?.content ?? []));
      setTenants(Array.isArray(t) ? t : (t?.content ?? []));
      setPartners(Array.isArray(p) ? p : (p?.content ?? []));
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Load failed');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function setTenantStatus(id, status) {
    try {
      await adminUsersApi.setTenantStatus(id, status);
      await load();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || 'Update failed');
    }
  }

  const current = tab === 'employees' ? employees : tab === 'tenants' ? tenants : partners;

  return (
    <div className="container mx-auto px-6 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold">Quản lý người dùng & nhân viên</h1>
        <button className="border rounded px-3 py-2" onClick={load} disabled={loading}>
          Làm mới
        </button>
      </div>

      {error ? <div className="mb-4 text-red-600">{error}</div> : null}

      <div className="flex gap-2 mb-4">
        <button className={`border rounded px-3 py-2 ${tab === 'employees' ? 'bg-gray-100' : ''}`} onClick={() => setTab('employees')}>
          Nhân viên
        </button>
        <button className={`border rounded px-3 py-2 ${tab === 'tenants' ? 'bg-gray-100' : ''}`} onClick={() => setTab('tenants')}>
          Sinh viên
        </button>
        <button className={`border rounded px-3 py-2 ${tab === 'partners' ? 'bg-gray-100' : ''}`} onClick={() => setTab('partners')}>
          Đối tác
        </button>
      </div>

      <div className="overflow-x-auto border rounded">
        <table className="min-w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="text-left px-3 py-2">ID</th>
              <th className="text-left px-3 py-2">Tài khoản</th>
              <th className="text-left px-3 py-2">Tên</th>
              <th className="text-left px-3 py-2">Email</th>
              <th className="text-left px-3 py-2">Trạng thái</th>
              <th className="text-left px-3 py-2">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={6} className="px-3 py-6 text-center">
                  Đang tải...
                </td>
              </tr>
            ) : (current || []).length === 0 ? (
              <tr>
                <td colSpan={6} className="px-3 py-6 text-center text-gray-500">
                  Không có dữ liệu
                </td>
              </tr>
            ) : (
              (current || []).map((u) => (
                <tr key={u.id} className="border-t">
                  <td className="px-3 py-2">{u.id}</td>
                  <td className="px-3 py-2">{u.username ?? '-'}</td>
                  <td className="px-3 py-2">{u.fullName ?? u.contactPerson ?? u.companyName ?? '-'}</td>
                  <td className="px-3 py-2">{u.email ?? '-'}</td>
                  <td className="px-3 py-2">{String(u.status ?? u.enabled ?? u.active ?? '-')}
                  </td>
                  <td className="px-3 py-2">
                    {tab === 'tenants' ? (
                      <div className="flex gap-2">
                        <button className="border rounded px-2 py-1" onClick={() => setTenantStatus(u.id, 'ACTIVE')}>
                          Kích hoạt
                        </button>
                        <button className="border rounded px-2 py-1" onClick={() => setTenantStatus(u.id, 'BANNED')}>
                          Khóa
                        </button>
                      </div>
                    ) : (
                      <span className="text-gray-500">—</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

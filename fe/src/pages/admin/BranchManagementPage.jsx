import React, { useEffect, useMemo, useState } from 'react';
import branchApi from '../../api/branchApi';

export default function BranchManagementPage() {
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [items, setItems] = useState([]);

  const [editingId, setEditingId] = useState(null);
  const isEditing = useMemo(() => editingId !== null && editingId !== undefined, [editingId]);

  const [form, setForm] = useState({
    branchCode: 'AUTO',
    branchName: '',
    address: '',
    phoneNumber: '',
  });

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await branchApi.getAll();
      const payload = res;
      const list = Array.isArray(payload) ? payload : [];
      list.sort((a, b) => (a?.id || 0) - (b?.id || 0));
      setItems(list);
    } catch (e) {
      console.error(e);
      setError(e?.response?.data?.message || 'Không thể tải danh sách chi nhánh');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const resetForm = () => {
    setEditingId(null);
    setForm({
      branchCode: 'AUTO',
      branchName: '',
      address: '',
      phoneNumber: '',
    });
  };

  const startEdit = (b) => {
    setError('');
    setEditingId(b?.id ?? null);
    setForm({
      branchCode: b?.branchCode || 'AUTO',
      branchName: b?.branchName || '',
      address: b?.address || '',
      phoneNumber: b?.phoneNumber || '',
    });
  };

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        branchCode: (form.branchCode || 'AUTO').trim(),
        branchName: (form.branchName || '').trim(),
        address: (form.address || '').trim(),
        phoneNumber: (form.phoneNumber || '').trim(),
      };

      if (!payload.branchName || !payload.address) {
        setError('Vui lòng nhập đầy đủ tên chi nhánh và địa chỉ');
        return;
      }

      if (isEditing) {
        await branchApi.update(editingId, payload);
      } else {
        // branchCode is validated on BE but will be overwritten after create
        await branchApi.create(payload);
      }

      await load();
      resetForm();
    } catch (e2) {
      console.error(e2);
      const msg =
        e2?.response?.data?.message ||
        e2?.response?.data?.error ||
        'Không thể lưu chi nhánh';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  const remove = async (b) => {
    if (!b?.id) return;
    const ok = window.confirm(
      `Xóa chi nhánh ${b.branchCode || ''} - ${b.branchName || ''}?\n\nLưu ý: Khi xóa chi nhánh, tất cả phòng thuộc chi nhánh và dữ liệu liên quan của phòng sẽ bị xóa theo.`
    );
    if (!ok) return;

    setSaving(true);
    setError('');
    try {
      await branchApi.remove(b.id);
      await load();
      if (editingId === b.id) resetForm();
    } catch (e2) {
      console.error(e2);
      const msg =
        e2?.response?.data?.message ||
        e2?.response?.data?.error ||
        'Không thể xóa chi nhánh';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="container mx-auto px-6 py-10">
      <div className="flex items-start justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-800">Quản lý chi nhánh</h1>
          <p className="text-slate-500 mt-1">
            Tạo / cập nhật / xóa chi nhánh. Khi xóa chi nhánh sẽ xóa các phòng thuộc chi nhánh và dữ liệu liên quan.
          </p>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 rounded-xl border border-red-200 bg-red-50 text-red-700 text-sm">{error}</div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm p-5">
            <div className="flex items-center justify-between mb-4">
              <div className="font-bold text-slate-800">{isEditing ? 'Cập nhật chi nhánh' : 'Tạo chi nhánh'}</div>
              {isEditing && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="text-sm font-semibold text-slate-500 hover:text-slate-700"
                  disabled={saving}
                >
                  Hủy
                </button>
              )}
            </div>

            <form onSubmit={submit} className="space-y-3">
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Mã chi nhánh</label>
                <input
                  value={form.branchCode}
                  onChange={(e) => setForm((p) => ({ ...p, branchCode: e.target.value }))}
                  disabled={true}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 bg-slate-50 text-slate-600"
                />
                <div className="text-xs text-slate-400 mt-1">Hệ thống tự sinh mã (CNxx)</div>
              </div>

              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Tên chi nhánh</label>
                <input
                  value={form.branchName}
                  onChange={(e) => setForm((p) => ({ ...p, branchName: e.target.value }))}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                  placeholder="Ví dụ: Chi nhánh Quận 1"
                />
              </div>

              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Địa chỉ</label>
                <input
                  value={form.address}
                  onChange={(e) => setForm((p) => ({ ...p, address: e.target.value }))}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                  placeholder="Ví dụ: 123 Nguyễn Văn Cừ, Q5"
                />
              </div>

              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Số điện thoại</label>
                <input
                  value={form.phoneNumber}
                  onChange={(e) => setForm((p) => ({ ...p, phoneNumber: e.target.value }))}
                  className="w-full rounded-xl border border-slate-200 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                  placeholder="Ví dụ: 0281234567"
                />
              </div>

              <button
                type="submit"
                disabled={saving}
                className="w-full rounded-xl bg-indigo-600 text-white font-bold py-2.5 hover:bg-indigo-700 disabled:opacity-60"
              >
                {saving ? 'Đang lưu...' : isEditing ? 'Cập nhật' : 'Tạo chi nhánh'}
              </button>
            </form>
          </div>
        </div>

        <div className="lg:col-span-2">
          <div className="bg-white rounded-2xl border border-slate-100 shadow-sm">
            <div className="p-5 border-b border-slate-100 flex items-center justify-between">
              <div className="font-bold text-slate-800">Danh sách chi nhánh</div>
              <button
                type="button"
                onClick={load}
                disabled={loading || saving}
                className="text-sm font-semibold text-slate-600 hover:text-slate-800"
              >
                Tải lại
              </button>
            </div>

            {loading ? (
              <div className="p-6 text-slate-500">Đang tải...</div>
            ) : items.length === 0 ? (
              <div className="p-6 text-slate-500">Chưa có chi nhánh.</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead className="bg-slate-50 text-slate-500">
                    <tr>
                      <th className="text-left px-5 py-3 font-bold">Mã</th>
                      <th className="text-left px-5 py-3 font-bold">Tên</th>
                      <th className="text-left px-5 py-3 font-bold">Địa chỉ</th>
                      <th className="text-left px-5 py-3 font-bold">SĐT</th>
                      <th className="text-right px-5 py-3 font-bold">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody>
                    {items.map((b) => (
                      <tr key={b.id} className="border-t border-slate-100">
                        <td className="px-5 py-3 font-bold text-slate-700 whitespace-nowrap">{b.branchCode}</td>
                        <td className="px-5 py-3 text-slate-700">{b.branchName}</td>
                        <td className="px-5 py-3 text-slate-600">{b.address}</td>
                        <td className="px-5 py-3 text-slate-600 whitespace-nowrap">{b.phoneNumber || '-'}</td>
                        <td className="px-5 py-3">
                          <div className="flex items-center justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => startEdit(b)}
                              disabled={saving}
                              className="px-3 py-1.5 rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50 font-semibold"
                            >
                              Sửa
                            </button>
                            <button
                              type="button"
                              onClick={() => remove(b)}
                              disabled={saving}
                              className="px-3 py-1.5 rounded-lg border border-red-200 text-red-600 hover:bg-red-50 font-semibold"
                            >
                              Xóa
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

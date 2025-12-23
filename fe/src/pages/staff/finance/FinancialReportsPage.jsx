import React, { useEffect, useState } from 'react';
import reportsApi from '../../../api/reportsApi';

export default function FinancialReportsPage() {
  const [from, setFrom] = useState(() => {
    const d = new Date();
    const start = new Date(d.getFullYear(), d.getMonth(), 1);
    return start.toISOString().slice(0, 10);
  });
  const [to, setTo] = useState(() => {
    const d = new Date();
    return d.toISOString().slice(0, 10);
  });
  const [branchId, setBranchId] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function load() {
    setLoading(true);
    setError('');
    try {
      const res = await reportsApi.summary({ from, to, branchId: branchId ? Number(branchId) : undefined });
      setData(res);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tải dữ liệu thất bại');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const payload = data;

  return (
    <div className="container mx-auto px-6 py-8">
      <h1 className="text-2xl font-bold mb-6">Báo cáo tài chính</h1>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div>
          <label className="block text-sm font-medium mb-1">Từ ngày</label>
          <input type="date" className="w-full border rounded px-3 py-2" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Đến ngày</label>
          <input type="date" className="w-full border rounded px-3 py-2" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Mã chi nhánh (không bắt buộc)</label>
          <input
            className="w-full border rounded px-3 py-2"
            value={branchId}
            onChange={(e) => setBranchId(e.target.value)}
            placeholder="VD: 1"
          />
        </div>
        <div className="flex items-end">
          <button className="w-full border rounded px-3 py-2" onClick={load} disabled={loading}>
            Chạy báo cáo
          </button>
        </div>
      </div>

      {error ? <div className="mb-4 text-red-600">{error}</div> : null}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="border rounded p-4">
          <div className="text-sm text-gray-600">Doanh thu</div>
          <div className="text-xl font-semibold">{loading ? '...' : payload?.revenue ?? '-'}</div>
        </div>
        <div className="border rounded p-4">
          <div className="text-sm text-gray-600">Đã thu</div>
          <div className="text-xl font-semibold">{loading ? '...' : payload?.paid ?? '-'}</div>
        </div>
        <div className="border rounded p-4">
          <div className="text-sm text-gray-600">Công nợ</div>
          <div className="text-xl font-semibold">{loading ? '...' : payload?.outstanding ?? '-'}</div>
        </div>
      </div>

      <div className="border rounded p-4 mt-6">
        <div className="text-sm text-gray-600 mb-2">Chi tiết</div>
        <pre className="text-xs whitespace-pre-wrap">{JSON.stringify(payload, null, 2)}</pre>
      </div>
    </div>
  );
}

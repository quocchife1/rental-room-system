import React, { useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import invoiceApi from '../../../api/invoiceApi';

function toISODate(date) {
  try {
    return date.toISOString().slice(0, 10);
  } catch {
    return '';
  }
}

function formatMoneyVnd(value) {
  if (value === null || value === undefined) return '0 ₫';
  const num = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(num)) return String(value);
  return num.toLocaleString('vi-VN') + ' ₫';
}

export default function InvoiceGenerationPage() {
  const role = useSelector((s) => s?.auth?.user?.role);

  const [month, setMonth] = useState(() => {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    return `${d.getFullYear()}-${m}`;
  });

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [busyContractId, setBusyContractId] = useState(null);

  const [expandedByContractId, setExpandedByContractId] = useState({});

  const ym = useMemo(() => {
    const [y, m] = String(month || '').split('-');
    const year = y ? Number(y) : undefined;
    const monthNum = m ? Number(m) : undefined;
    if (!year || !monthNum) return null;
    return { year, month: monthNum };
  }, [month]);

  async function load() {
    if (!ym) return;
    setLoading(true);
    setError('');
    try {
      const res = await invoiceApi.monthlyPreviews(ym.year, ym.month);
      setRows(Array.isArray(res) ? res : []);
    } catch (e) {
      setRows([]);
      setError(e?.response?.data?.message || e?.message || 'Tải danh sách hợp đồng thất bại');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [ym?.year, ym?.month]);

  async function generateBulk() {
    if (!ym) return;
    setLoading(true);
    setError('');
    try {
      const payload = { year: ym.year, month: ym.month };
      const resp = await invoiceApi.generateMonthly(payload);
      alert(`Đã tạo ${resp?.createdCount ?? 0} hóa đơn (bỏ qua ${resp?.skippedExistingCount ?? 0} đã tồn tại)`);
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tạo hàng loạt hóa đơn thất bại');
    } finally {
      setLoading(false);
    }
  }

  async function generateForContract(contractId) {
    if (!ym || !contractId) return;
    setBusyContractId(contractId);
    setError('');
    try {
      const payload = { year: ym.year, month: ym.month };
      await invoiceApi.generateMonthlyForContract(contractId, payload);
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tạo hóa đơn thất bại');
    } finally {
      setBusyContractId(null);
    }
  }

  function isExpanded(contractId) {
    return expandedByContractId?.[contractId] !== false;
  }

  function toggleExpanded(contractId) {
    if (!contractId) return;
    setExpandedByContractId((prev) => ({
      ...(prev || {}),
      [contractId]: !isExpanded(contractId),
    }));
  }

  // Safety: page is only routed for ADMIN/ACCOUNTANT. Keep a tiny guard to avoid rendering for other roles.
  const canUse = role === 'ADMIN' || role === 'ACCOUNTANT';
  if (!canUse) return null;

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-7xl mx-auto space-y-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Tạo hóa đơn tháng</h1>
            <p className="text-slate-500 mt-1">Danh sách hợp đồng ACTIVE kèm chi tiết các khoản cần thanh toán.</p>
          </div>

          <button
            onClick={generateBulk}
            disabled={loading || !ym}
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-lg shadow-lg shadow-indigo-200 font-bold transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
            title="Tạo hàng loạt hóa đơn gửi đến người thuê"
          >
            Tạo hàng loạt hóa đơn
          </button>
        </div>

        <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-col md:flex-row gap-4 items-end md:items-center justify-between">
          <div className="w-full md:w-56">
            <label className="block text-xs font-bold text-slate-500 mb-1">Tháng làm việc</label>
            <input
              type="month"
              className="w-full bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-sm font-medium focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
              value={month}
              onChange={(e) => setMonth(e.target.value)}
            />
          </div>
          <div className="text-xs text-slate-400">
            Hạn thanh toán mặc định: 5 ngày kể từ ngày tạo
          </div>
        </div>

        {error && (
          <div className="bg-rose-50 text-rose-700 px-4 py-3 rounded-xl border border-rose-200 text-sm font-medium">
            {error}
          </div>
        )}

        {loading ? (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 text-slate-500">Đang tải dữ liệu...</div>
        ) : rows.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 text-slate-500">Không có hợp đồng để hiển thị.</div>
        ) : (
          <div className="space-y-6">
            {rows.map((r) => (
              <div key={r.contractId} className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <div className="px-6 py-4 border-b border-slate-200 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                  <div className="space-y-1">
                    <div className="text-sm font-extrabold text-slate-900">Hợp đồng #{r.contractId}</div>
                    <div className="text-sm text-slate-600">
                      <span className="font-bold">Phòng:</span> {r.roomNumber || '-'} &nbsp;|&nbsp; <span className="font-bold">Chi nhánh:</span> {r.branchCode || '-'}
                    </div>
                    <div className="text-sm text-slate-600">
                      <span className="font-bold">Người thuê:</span> {r.tenantName || r.tenantUsername || '-'}
                    </div>
                  </div>

                  <div className="flex flex-col sm:flex-row items-start sm:items-center gap-2 sm:gap-3">
                    <button
                      onClick={() => toggleExpanded(r.contractId)}
                      className="border border-slate-200 hover:bg-slate-50 text-slate-700 px-3 py-2 rounded-lg font-bold text-sm transition-colors"
                      title={isExpanded(r.contractId) ? 'Thu gọn chi tiết' : 'Mở rộng chi tiết'}
                      type="button"
                    >
                      {isExpanded(r.contractId) ? 'Thu gọn' : 'Xem chi tiết'}
                    </button>

                    {!isExpanded(r.contractId) && (
                      <div className="text-sm font-extrabold text-slate-700 whitespace-nowrap">
                        Tổng cộng:&nbsp;<span className="text-indigo-700">{formatMoneyVnd(r.amount)}</span>
                      </div>
                    )}

                    <button
                      onClick={() => generateForContract(r.contractId)}
                      disabled={busyContractId === r.contractId || !ym}
                      className="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-lg font-extrabold text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                      title="Tạo hóa đơn tháng cho hợp đồng này"
                      type="button"
                    >
                      {busyContractId === r.contractId ? 'Đang tạo...' : 'Tạo hóa đơn tháng'}
                    </button>
                  </div>
                </div>

                {r.error ? (
                  <div className="px-6 py-4 text-sm bg-rose-50 text-rose-700 border-b border-rose-200">
                    {r.error}
                  </div>
                ) : isExpanded(r.contractId) ? (
                  <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-bold tracking-wider">
                          <th className="px-6 py-4">Nội dung</th>
                          <th className="px-6 py-4 text-right">Đơn giá</th>
                          <th className="px-6 py-4 text-right">SL</th>
                          <th className="px-6 py-4 text-right">Thành tiền</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 text-sm">
                        {(r.details || []).map((d, idx) => (
                          <tr key={d.id ?? idx}>
                            <td className="px-6 py-4 font-semibold text-slate-800">{d.description}</td>
                            <td className="px-6 py-4 text-right text-slate-600">{formatMoneyVnd(d.unitPrice)}</td>
                            <td className="px-6 py-4 text-right text-slate-600">{d.quantity ?? 0}</td>
                            <td className="px-6 py-4 text-right font-extrabold text-slate-900">{formatMoneyVnd(d.amount)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : null}

                {(!r.error && isExpanded(r.contractId)) && (
                  <div className="px-6 py-4 bg-slate-50 border-t border-slate-200 flex items-center justify-end">
                    <div className="text-sm font-extrabold text-slate-700">
                      TỔNG CỘNG:&nbsp;
                      <span className="text-indigo-700">{formatMoneyVnd(r.amount)}</span>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

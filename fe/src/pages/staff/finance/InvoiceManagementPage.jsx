import React, { useEffect, useMemo, useState } from 'react';
import { useSelector } from 'react-redux';
import invoiceApi from '../../../api/invoiceApi';
import reportsApi from '../../../api/reportsApi';

const Icons = {
  Money: () => (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
  ),
  Wallet: () => (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
  ),
  Chart: () => (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 002 2h2a2 2 0 002-2z" /></svg>
  ),
  Building: () => (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" /></svg>
  ),
  Filter: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" /></svg>
  ),
  Refresh: ({ spinning }) => (
    <svg className={`w-4 h-4 ${spinning ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
  ),
  Cash: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
  ),
  Bank: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
  ),
  Plus: () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
  )
};

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

function formatDate(dateStr) {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('vi-VN');
}

export default function InvoiceManagementPage() {
  const role = useSelector((s) => s?.auth?.user?.role);
  const isManager = role === 'MANAGER';
  const canConfirmPayments = role === 'ADMIN' || role === 'ACCOUNTANT';
  const canGenerateMonthly = role === 'ADMIN' || role === 'ACCOUNTANT';
  const isAccountant = role === 'ACCOUNTANT';

  const [month, setMonth] = useState(() => {
    const d = new Date();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    return `${d.getFullYear()}-${m}`;
  });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [status, setStatus] = useState('');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [reportLoading, setReportLoading] = useState(false);
  const [reportError, setReportError] = useState('');
  const [report, setReport] = useState(null);
  const [branchId, setBranchId] = useState('');

  const [expandedInvoiceId, setExpandedInvoiceId] = useState(null);
  const [invoiceDetailById, setInvoiceDetailById] = useState({});
  const [detailLoadingId, setDetailLoadingId] = useState(null);

  const [cashLookupId, setCashLookupId] = useState('');
  const [cashInvoice, setCashInvoice] = useState(null);
  const [cashLoading, setCashLoading] = useState(false);
  const [cashError, setCashError] = useState('');

  const reportParams = useMemo(() => {
    const [y, m] = month.split('-');
    const year = y ? Number(y) : undefined;
    const monthNum = m ? Number(m) : undefined;
    if (!year || !monthNum) return null;
    const from = new Date(year, monthNum - 1, 1);
    const to = new Date(year, monthNum, 0);
    return {
      from: toISODate(from),
      to: toISODate(to),
      branchId: !isManager && branchId ? Number(branchId) : undefined,
    };
  }, [month, branchId, isManager]);

  const params = useMemo(() => {
    const [y, m] = month.split('-');
    return {
      year: y ? Number(y) : undefined,
      month: m ? Number(m) : undefined,
      page,
      size,
      status: status || undefined,
      sort: 'id,desc',
    };
  }, [month, page, size, status]);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const res = await invoiceApi.listPaged(params);
      setData(res);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tải dữ liệu thất bại');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, [month, page, size, status]);

  useEffect(() => {
    let alive = true;
    async function loadReport() {
      if (!reportParams?.from || !reportParams?.to) return;
      setReportLoading(true);
      setReportError('');
      try {
        const res = await reportsApi.summary(reportParams);
        if (alive) setReport(res);
      } catch (e) {
        if (alive) setReportError(e?.response?.data?.message || e?.message || 'Tải doanh thu thất bại');
      } finally {
        if (alive) setReportLoading(false);
      }
    }
    loadReport();
    return () => {
      alive = false;
    };
  }, [reportParams]);

  async function markPaid(invoiceId, direct) {
    if (!canConfirmPayments) return;
    try {
      await invoiceApi.payInvoiceAsStaff(invoiceId, direct);
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Cập nhật thanh toán thất bại');
    }
  }

  async function lookupCashInvoice() {
    const id = Number(String(cashLookupId || '').trim());
    if (!Number.isFinite(id) || id <= 0) {
      setCashError('Vui lòng nhập mã hóa đơn hợp lệ');
      setCashInvoice(null);
      return;
    }
    setCashLoading(true);
    setCashError('');
    try {
      const inv = await invoiceApi.getById(id);
      setCashInvoice(inv);
    } catch (e) {
      setCashInvoice(null);
      setCashError(e?.response?.data?.message || e?.message || 'Không tìm thấy hóa đơn');
    } finally {
      setCashLoading(false);
    }
  }

  async function confirmCashCollection() {
    const id = cashInvoice?.id;
    if (!id) return;
    setCashLoading(true);
    setCashError('');
    try {
      await invoiceApi.payInvoiceAsStaff(id, true);
      await load();
      const refreshed = await invoiceApi.getById(id);
      setCashInvoice(refreshed);
    } catch (e) {
      setCashError(e?.response?.data?.message || e?.message || 'Xác nhận thu tiền mặt thất bại');
    } finally {
      setCashLoading(false);
    }
  }

  async function generateMonthlyInvoices() {
    if (isManager) {
      setError('Quản lý không có quyền tạo hóa đơn tiền nhà.');
      return;
    }
    if (!canGenerateMonthly) {
      setError('Bạn không có quyền tạo hóa đơn tiền nhà.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const [y, m] = month.split('-');
      const payload = { year: y ? Number(y) : undefined, month: m ? Number(m) : undefined };
      const resp = await invoiceApi.generateMonthly(payload);
      alert(`Đã tạo ${resp?.createdCount ?? resp?.created_count ?? 0} hóa đơn (bỏ qua ${resp?.skippedExistingCount ?? 0} đã tồn tại)`);
      await load();
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tạo hóa đơn tháng thất bại');
    } finally {
      setLoading(false);
    }
  }

  const pageObj = data;
  const itemsRaw = Array.isArray(pageObj?.content) ? pageObj.content : [];
  const items = [...itemsRaw].sort((a, b) => (b?.id || 0) - (a?.id || 0));
  const totalPages = pageObj?.totalPages ?? 0;

  async function toggleInvoiceDetails(id) {
    if (!id) return;
    const next = expandedInvoiceId === id ? null : id;
    setExpandedInvoiceId(next);
    if (!next) return;

    if (invoiceDetailById[next]) return;
    setDetailLoadingId(next);
    try {
      const res = await invoiceApi.getById(next);
      setInvoiceDetailById((prev) => ({ ...prev, [next]: res }));
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tải chi tiết hóa đơn thất bại');
    } finally {
      setDetailLoadingId(null);
    }
  }

  const getStatusColor = (st) => {
    switch (st) {
      case 'PAID': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
      case 'OVERDUE': return 'bg-rose-100 text-rose-700 border-rose-200';
      default: return 'bg-amber-100 text-amber-700 border-amber-200';
    }
  };

  const getStatusLabel = (st) => {
    switch (st) {
      case 'PAID': return 'Đã thanh toán';
      case 'OVERDUE': return 'Quá hạn';
      case 'UNPAID': return 'Chưa thanh toán';
      default: return st;
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
             <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Quản lý Tài chính</h1>
             <p className="text-slate-500 mt-1">Theo dõi doanh thu, công nợ và quản lý hóa đơn tiền nhà.</p>
          </div>
          
          {canGenerateMonthly && (
             <button 
               onClick={generateMonthlyInvoices} 
               disabled={loading}
               className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-lg shadow-lg shadow-indigo-200 font-bold transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
             >
               <Icons.Plus />
               Tạo hóa đơn tháng này
             </button>
          )}
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
           <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-4">
                 <div className="p-3 bg-blue-50 text-blue-600 rounded-xl"><Icons.Chart /></div>
                 <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Tổng doanh thu</span>
              </div>
              <div>
                 <div className="text-2xl font-bold text-slate-800">{reportLoading ? '...' : formatMoneyVnd(report?.revenue)}</div>
                 <div className="text-sm text-slate-500 mt-1">Phát sinh trong tháng</div>
              </div>
           </div>

           <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-4">
                 <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl"><Icons.Wallet /></div>
                 <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Đã thu</span>
              </div>
              <div>
                 <div className="text-2xl font-bold text-emerald-600">{reportLoading ? '...' : formatMoneyVnd(report?.paid)}</div>
                 <div className="text-sm text-slate-500 mt-1">Thực thu</div>
              </div>
           </div>

           <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-4">
                 <div className="p-3 bg-rose-50 text-rose-600 rounded-xl"><Icons.Money /></div>
                 <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Công nợ</span>
              </div>
              <div>
                 <div className="text-2xl font-bold text-rose-600">{reportLoading ? '...' : formatMoneyVnd(report?.outstanding)}</div>
                 <div className="text-sm text-slate-500 mt-1">Chưa thanh toán</div>
              </div>
           </div>

           <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200 flex flex-col justify-between hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-4">
                 <div className="p-3 bg-indigo-50 text-indigo-600 rounded-xl"><Icons.Building /></div>
                 <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Chi nhánh</span>
              </div>
              <div className="w-full">
                 {!isManager ? (
                   <input
                     className="w-full border-b border-slate-300 focus:border-indigo-500 outline-none py-1 text-slate-800 font-medium placeholder:font-normal"
                     value={branchId}
                     onChange={(e) => setBranchId(e.target.value)}
                     placeholder="Nhập ID chi nhánh..."
                   />
                 ) : (
                   <div className="font-bold text-slate-700">Chi nhánh quản lý</div>
                 )}
                 {reportError && <div className="text-xs text-rose-500 mt-1 truncate" title={reportError}>{reportError}</div>}
              </div>
           </div>
        </div>

        {/* Filter Toolbar */}
        <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-col md:flex-row gap-4 items-end md:items-center justify-between">
           <div className="flex flex-col md:flex-row gap-4 w-full md:w-auto">
              <div className="w-full md:w-48">
                <label className="block text-xs font-bold text-slate-500 mb-1">Tháng làm việc</label>
                <input
                  type="month"
                  className="w-full bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-sm font-medium focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                  value={month}
                  onChange={(e) => { setMonth(e.target.value); setPage(0); }}
                />
              </div>
              <div className="w-full md:w-48">
                <label className="block text-xs font-bold text-slate-500 mb-1">Trạng thái</label>
                <select
                  className="w-full bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-sm font-medium focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                  value={status}
                  onChange={(e) => { setStatus(e.target.value); setPage(0); }}
                >
                  <option value="">Tất cả trạng thái</option>
                  <option value="UNPAID">Chưa thanh toán</option>
                  <option value="PAID">Đã thanh toán</option>
                  <option value="OVERDUE">Quá hạn</option>
                </select>
              </div>
           </div>

           <div className="flex gap-3 w-full md:w-auto">
             <div className="w-24">
                <label className="block text-xs font-bold text-slate-500 mb-1">Hiển thị</label>
                <select className="w-full bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-sm font-medium focus:outline-none" value={size} onChange={(e) => setSize(Number(e.target.value))}>
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={50}>50</option>
                </select>
             </div>
             <div className="flex-1 md:flex-none flex items-end">
                <button 
                  className="w-full md:w-auto flex items-center justify-center gap-2 border border-slate-200 hover:bg-slate-50 text-slate-700 px-4 py-2 rounded-lg font-bold text-sm transition-colors h-[38px]"
                  onClick={load}
                  disabled={loading}
                >
                  <Icons.Refresh spinning={loading} />
                  Làm mới
                </button>
             </div>
           </div>
        </div>

        {error && (
          <div className="bg-rose-50 text-rose-700 px-4 py-3 rounded-xl border border-rose-200 text-sm font-medium">
            Error: {error}
          </div>
        )}

        {isAccountant && (
          <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
              <div>
                <div className="text-sm font-extrabold text-slate-900">Thu tiền mặt</div>
                <div className="text-xs text-slate-500">Nhập mã hóa đơn để xem chi tiết và xác nhận đã thu (tiền mặt).</div>
              </div>
            </div>

            <div className="mt-3 flex flex-col md:flex-row gap-3">
              <input
                className="flex-1 bg-slate-50 border border-slate-200 rounded-lg px-3 py-2 text-sm font-medium focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none"
                value={cashLookupId}
                onChange={(e) => setCashLookupId(e.target.value)}
                placeholder="Nhập mã hóa đơn (VD: 123)"
                inputMode="numeric"
              />
              <button
                className="px-4 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                onClick={lookupCashInvoice}
                disabled={cashLoading}
              >
                {cashLoading ? 'Đang tìm...' : 'Tìm hóa đơn'}
              </button>
            </div>

            {cashError && (
              <div className="mt-3 bg-rose-50 text-rose-700 px-4 py-3 rounded-xl border border-rose-200 text-sm font-medium">
                {cashError}
              </div>
            )}

            {cashInvoice && (
              <div className="mt-4 space-y-3">
                <div className="grid grid-cols-1 md:grid-cols-6 gap-3 text-sm">
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div className="text-xs text-slate-500 font-bold uppercase">Mã HĐ</div>
                    <div className="font-mono font-bold text-slate-800">#{cashInvoice.id}</div>
                  </div>
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div className="text-xs text-slate-500 font-bold uppercase">Hợp đồng</div>
                    <div className="font-bold text-slate-800">HĐ #{cashInvoice.contractId ?? '-'}</div>
                  </div>
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div className="text-xs text-slate-500 font-bold uppercase">Chi nhánh</div>
                    <div className="font-semibold text-slate-800">
                      {cashInvoice.branchName || cashInvoice.branchCode || '-'}
                    </div>
                    {(cashInvoice.branchName && cashInvoice.branchCode) ? (
                      <div className="text-xs text-slate-500 mt-0.5">{cashInvoice.branchCode}</div>
                    ) : null}
                  </div>
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div className="text-xs text-slate-500 font-bold uppercase">Phòng</div>
                    <div className="font-semibold text-slate-800">
                      {cashInvoice.roomCode || (cashInvoice.roomNumber ? `Phòng ${cashInvoice.roomNumber}` : '-')}
                    </div>
                    {cashInvoice.roomCode && cashInvoice.roomNumber ? (
                      <div className="text-xs text-slate-500 mt-0.5">Phòng {cashInvoice.roomNumber}</div>
                    ) : null}
                  </div>
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div className="text-xs text-slate-500 font-bold uppercase">Số tiền</div>
                    <div className="font-extrabold text-indigo-700">{formatMoneyVnd(cashInvoice.amount)}</div>
                  </div>
                  <div className="bg-slate-50 border border-slate-200 rounded-lg p-3">
                    <div className="text-xs text-slate-500 font-bold uppercase">Trạng thái</div>
                    <div>
                      <span className={`inline-block px-3 py-1 rounded-full text-xs font-bold border ${getStatusColor(cashInvoice.status)}`}>
                        {getStatusLabel(cashInvoice.status)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="bg-white border border-slate-200 rounded-lg p-3 text-sm">
                  <div className="text-xs text-slate-500 font-bold uppercase">Người thuê</div>
                  <div className="mt-1 flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                    <div className="font-semibold text-slate-800">
                      {cashInvoice.tenantFullName || cashInvoice.tenantUsername || '-'}
                    </div>
                    <div className="text-slate-600 text-xs md:text-sm">
                      {cashInvoice.tenantPhoneNumber ? `SĐT: ${cashInvoice.tenantPhoneNumber}` : null}
                      {cashInvoice.tenantPhoneNumber && cashInvoice.tenantCccd ? ' • ' : null}
                      {cashInvoice.tenantCccd ? `CCCD: ${cashInvoice.tenantCccd}` : null}
                      {(!cashInvoice.tenantPhoneNumber && !cashInvoice.tenantCccd) ? '-' : null}
                    </div>
                  </div>
                  {(cashInvoice.tenantStudentId || cashInvoice.tenantUniversity || cashInvoice.tenantEmail) ? (
                    <div className="mt-1 text-xs text-slate-500">
                      {cashInvoice.tenantStudentId ? `MSSV: ${cashInvoice.tenantStudentId}` : null}
                      {cashInvoice.tenantStudentId && cashInvoice.tenantUniversity ? ' • ' : null}
                      {cashInvoice.tenantUniversity ? cashInvoice.tenantUniversity : null}
                      {(cashInvoice.tenantStudentId || cashInvoice.tenantUniversity) && cashInvoice.tenantEmail ? ' • ' : null}
                      {cashInvoice.tenantEmail ? cashInvoice.tenantEmail : null}
                    </div>
                  ) : null}
                </div>

                <div className="overflow-x-auto">
                  <table className="min-w-full text-sm border border-slate-200 rounded-lg overflow-hidden">
                    <thead className="bg-white">
                      <tr className="text-slate-500 text-xs uppercase">
                        <th className="text-left px-3 py-2">Mô tả</th>
                        <th className="text-right px-3 py-2">Đơn giá</th>
                        <th className="text-right px-3 py-2">Số lượng</th>
                        <th className="text-right px-3 py-2">Thành tiền</th>
                      </tr>
                    </thead>
                    <tbody className="bg-white">
                      {(cashInvoice.details || []).length === 0 ? (
                        <tr>
                          <td colSpan={4} className="px-3 py-3 text-slate-500">Không có chi tiết.</td>
                        </tr>
                      ) : (
                        (cashInvoice.details || []).map((d) => (
                          <tr key={d.id} className="border-t border-slate-100">
                            <td className="px-3 py-2">{d.description}</td>
                            <td className="px-3 py-2 text-right">{formatMoneyVnd(d.unitPrice)}</td>
                            <td className="px-3 py-2 text-right">{d.quantity}</td>
                            <td className="px-3 py-2 text-right font-semibold">{formatMoneyVnd(d.amount)}</td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>

                <div className="flex items-center justify-end">
                  {cashInvoice.status !== 'PAID' ? (
                    <button
                      className="flex items-center gap-2 px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                      onClick={confirmCashCollection}
                      disabled={cashLoading}
                      title="Xác nhận đã thu tiền mặt"
                    >
                      <Icons.Cash />
                      Xác nhận đã thu tiền mặt
                    </button>
                  ) : (
                    <div className="text-emerald-700 font-bold text-sm">Hóa đơn đã thanh toán</div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}

        {/* Data Table */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-bold tracking-wider">
                  <th className="px-6 py-4">Mã HĐ</th>
                  <th className="px-6 py-4">Hợp đồng</th>
                  <th className="px-6 py-4">Người thuê</th>
                  <th className="px-6 py-4">Chi nhánh</th>
                  <th className="px-6 py-4">Phòng</th>
                  <th className="px-6 py-4">Số tiền</th>
                  <th className="px-6 py-4">Hạn thanh toán</th>
                  <th className="px-6 py-4 text-center">Trạng thái</th>
                  <th className="px-6 py-4">Ngày thực thu</th>
                  {canConfirmPayments && <th className="px-6 py-4 text-right">Xác nhận thu</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm">
                {loading ? (
                  <tr><td colSpan={canConfirmPayments ? 10 : 9} className="px-6 py-10 text-center text-slate-500">Đang tải dữ liệu...</td></tr>
                ) : items.length === 0 ? (
                  <tr><td colSpan={canConfirmPayments ? 10 : 9} className="px-6 py-10 text-center text-slate-400 italic">Không tìm thấy hóa đơn nào trong tháng này.</td></tr>
                ) : (
                  items.map((inv) => (
                    <React.Fragment key={inv.id ?? inv.invoiceId}>
                    <tr
                      className="hover:bg-slate-50 transition-colors cursor-pointer"
                      onClick={() => toggleInvoiceDetails(inv.id ?? inv.invoiceId)}
                      title="Bấm để xem chi tiết hóa đơn"
                    >
                      <td className="px-6 py-4 font-mono text-slate-600">#{inv.id ?? inv.invoiceId}</td>
                      <td className="px-6 py-4 font-semibold text-slate-800">HĐ #{inv.contractId ?? '-'}</td>
                      <td className="px-6 py-4">
                        <div className="font-semibold text-slate-800">{inv.tenantFullName || inv.tenantUsername || '-'}</div>
                        {(inv.tenantPhoneNumber || inv.tenantCccd) ? (
                          <div className="text-xs text-slate-500 mt-0.5">
                            {inv.tenantPhoneNumber ? inv.tenantPhoneNumber : null}
                            {inv.tenantPhoneNumber && inv.tenantCccd ? ' • ' : null}
                            {inv.tenantCccd ? `CCCD: ${inv.tenantCccd}` : null}
                          </div>
                        ) : null}
                      </td>
                      <td className="px-6 py-4 text-slate-700">
                        <div className="font-semibold">{inv.branchName || inv.branchCode || '-'}</div>
                        {inv.branchName && inv.branchCode ? <div className="text-xs text-slate-500 mt-0.5">{inv.branchCode}</div> : null}
                      </td>
                      <td className="px-6 py-4 text-slate-700">
                        <div className="font-semibold">{inv.roomCode || (inv.roomNumber ? `Phòng ${inv.roomNumber}` : '-')}</div>
                        {inv.roomCode && inv.roomNumber ? <div className="text-xs text-slate-500 mt-0.5">Phòng {inv.roomNumber}</div> : null}
                      </td>
                      <td className="px-6 py-4 font-bold text-indigo-700 text-base">{formatMoneyVnd(inv.amount)}</td>
                      <td className="px-6 py-4 text-slate-600">{formatDate(inv.dueDate)}</td>
                      <td className="px-6 py-4 text-center">
                        <span className={`inline-block px-3 py-1 rounded-full text-xs font-bold border ${getStatusColor(inv.status)}`}>
                          {getStatusLabel(inv.status)}
                        </span>
                        {inv.status === 'PAID' && (
                          <div className="text-xs text-slate-400 mt-1 font-medium">
                            {inv.paidDirect ? 'Tiền mặt' : 'Chuyển khoản'}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 text-slate-600">{formatDate(inv.paidDate)}</td>
                      
                      {canConfirmPayments && (
                        <td className="px-6 py-4 text-right">
                          {inv.status !== 'PAID' ? (
                            <div className="flex justify-end gap-2">
                               <button 
                                 onClick={(e) => { e.stopPropagation(); markPaid(inv.id ?? inv.invoiceId, true); }}
                                 className="p-2 rounded-lg bg-emerald-50 text-emerald-600 border border-emerald-200 hover:bg-emerald-100 transition-colors"
                                 title="Thu tiền mặt"
                               >
                                 <Icons.Cash />
                               </button>
                               {!isAccountant && (
                                 <button 
                                   onClick={(e) => { e.stopPropagation(); markPaid(inv.id ?? inv.invoiceId, false); }}
                                   className="p-2 rounded-lg bg-blue-50 text-blue-600 border border-blue-200 hover:bg-blue-100 transition-colors"
                                   title="Thu chuyển khoản"
                                 >
                                   <Icons.Bank />
                                 </button>
                               )}
                            </div>
                          ) : (
                            <span className="text-emerald-600 font-bold text-xs flex items-center justify-end gap-1">
                               <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                               Hoàn tất
                            </span>
                          )}
                        </td>
                      )}
                    </tr>
                    {expandedInvoiceId === (inv.id ?? inv.invoiceId) && (
                      <tr className="bg-slate-50/50">
                        <td colSpan={canConfirmPayments ? 10 : 9} className="px-6 py-4">
                          {detailLoadingId === (inv.id ?? inv.invoiceId) ? (
                            <div className="text-sm text-slate-500">Đang tải chi tiết…</div>
                          ) : (
                            <div className="space-y-3">
                              <div className="text-sm font-bold text-slate-700">Chi tiết hóa đơn</div>
                              <div className="overflow-x-auto">
                                <table className="min-w-full text-sm border border-slate-200 rounded-lg overflow-hidden">
                                  <thead className="bg-white">
                                    <tr className="text-slate-500 text-xs uppercase">
                                      <th className="text-left px-3 py-2">Mô tả</th>
                                      <th className="text-right px-3 py-2">Đơn giá</th>
                                      <th className="text-right px-3 py-2">Số lượng</th>
                                      <th className="text-right px-3 py-2">Thành tiền</th>
                                    </tr>
                                  </thead>
                                  <tbody className="bg-white">
                                    {(invoiceDetailById[inv.id ?? inv.invoiceId]?.details || []).length === 0 ? (
                                      <tr>
                                        <td colSpan={4} className="px-3 py-3 text-slate-500">Không có chi tiết.</td>
                                      </tr>
                                    ) : (
                                      (invoiceDetailById[inv.id ?? inv.invoiceId]?.details || []).map((d) => (
                                        <tr key={d.id} className="border-t border-slate-100">
                                          <td className="px-3 py-2">{d.description}</td>
                                          <td className="px-3 py-2 text-right">{formatMoneyVnd(d.unitPrice)}</td>
                                          <td className="px-3 py-2 text-right">{d.quantity}</td>
                                          <td className="px-3 py-2 text-right font-semibold">{formatMoneyVnd(d.amount)}</td>
                                        </tr>
                                      ))
                                    )}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          )}
                        </td>
                      </tr>
                    )}
                    </React.Fragment>
                  ))
                )}
              </tbody>
            </table>
          </div>
          
          {/* Pagination Footer */}
          <div className="px-6 py-4 border-t border-slate-200 bg-slate-50 flex items-center justify-between">
             <div className="text-sm text-slate-500">
                Hiển thị trang <span className="font-bold text-slate-800">{page + 1}</span> trên <span className="font-bold text-slate-800">{Math.max(1, totalPages)}</span>
             </div>
             <div className="flex gap-2">
                <button 
                  className="px-4 py-2 bg-white border border-slate-300 rounded-lg text-sm font-medium hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  onClick={() => setPage((p) => Math.max(0, p - 1))} 
                  disabled={page <= 0}
                >
                  Trước
                </button>
                <button 
                  className="px-4 py-2 bg-white border border-slate-300 rounded-lg text-sm font-medium hover:bg-slate-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  onClick={() => setPage((p) => p + 1)}
                  disabled={totalPages ? page + 1 >= totalPages : items.length < size}
                >
                  Sau
                </button>
             </div>
          </div>
        </div>

      </div>
    </div>
  );
}
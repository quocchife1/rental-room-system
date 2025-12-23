import React, { useEffect, useState } from 'react';
import dashboardApi from '../../api/dashboardApi';

// --- Icons (Inline SVG) ---
const Icons = {
  Money: () => <svg className="w-6 h-6 text-emerald-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
  Chart: () => <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 002 2h2a2 2 0 002-2z" /></svg>,
  Alert: () => <svg className="w-6 h-6 text-rose-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>,
  Tool: () => <svg className="w-6 h-6 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>,
  Refresh: ({ spin }) => <svg className={`w-4 h-4 ${spin ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>,
};

export default function DirectorDashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [revenueChartMode, setRevenueChartMode] = useState('month'); // 'month' | 'branch'

  const formatCurrency = (v) => {
    if (v === null || v === undefined) return '-';
    const n = typeof v === 'number' ? v : Number(v);
    if (Number.isNaN(n)) return String(v);
    return new Intl.NumberFormat('vi-VN').format(n) + ' đ';
  };

  const formatPercent = (v) => {
    if (v === null || v === undefined) return '-';
    const n = typeof v === 'number' ? v : Number(v);
    if (Number.isNaN(n)) return String(v);
    const pct = n <= 1 ? n * 100 : n;
    return `${pct.toFixed(1)}%`;
  };

  async function load() {
    setLoading(true);
    setError('');
    try {
      const res = await dashboardApi.getDirectorDashboard();
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
  const monthlyRevenueHistory = Array.isArray(payload?.monthlyRevenueHistory) ? payload.monthlyRevenueHistory : [];
  const revenueByBranchThisMonth = Array.isArray(payload?.revenueByBranchThisMonth) ? payload.revenueByBranchThisMonth : [];
  const roomOccupancyByBranch = Array.isArray(payload?.roomOccupancyByBranch) ? payload.roomOccupancyByBranch : [];
  const topOverdueInvoices = Array.isArray(payload?.topOverdueInvoices) ? payload.topOverdueInvoices : [];
  const expiringContracts = Array.isArray(payload?.expiringContracts) ? payload.expiringContracts : [];

  const pieColors = ['text-indigo-500', 'text-emerald-500', 'text-amber-500', 'text-rose-500', 'text-blue-500', 'text-slate-500'];

  const pieData = revenueByBranchThisMonth
    .filter(i => i && (i.revenue ?? 0) !== 0)
    .map(i => ({
      branchId: i.branchId,
      branchName: i.branchName || i.branchCode || 'Không xác định',
      revenue: typeof i.revenue === 'number' ? i.revenue : Number(i.revenue) || 0,
    }))
    .sort((a, b) => (b.revenue || 0) - (a.revenue || 0));

  const pieTotal = pieData.reduce((sum, i) => sum + (i.revenue || 0), 0);

  // Helper for KPI Cards
  const KpiCard = ({ title, value, icon, subtext, colorClass }) => (
    <div className="bg-white rounded-2xl p-6 shadow-sm border border-slate-200 flex flex-col justify-between hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-4">
        <div className={`p-3 rounded-xl ${colorClass}`}>
          {icon}
        </div>
      </div>
      <div>
        <div className="text-3xl font-extrabold text-slate-800 tracking-tight">{loading ? '...' : value}</div>
        <div className="text-sm font-medium text-slate-500 mt-1">{title}</div>
        {subtext && <div className="text-xs text-slate-400 mt-2">{subtext}</div>}
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Tổng quan Giám đốc</h1>
            <p className="text-slate-500 mt-1">Theo dõi hiệu suất kinh doanh và các chỉ số quan trọng.</p>
          </div>
          <button
            onClick={load}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-300 rounded-lg text-slate-700 font-medium hover:bg-slate-50 transition-all shadow-sm active:scale-95 disabled:opacity-70"
          >
            <Icons.Refresh spin={loading} />
            {loading ? 'Đang cập nhật...' : 'Làm mới dữ liệu'}
          </button>
        </div>

        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-r shadow-sm flex items-center">
            <span className="font-bold mr-2">Lỗi:</span> {error}
          </div>
        )}

        {/* KPI Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <KpiCard 
            title="Doanh thu tháng này" 
            value={formatCurrency(payload?.totalRevenueThisMonth)} 
            icon={<Icons.Money />}
            colorClass="bg-emerald-50"
            subtext="Tổng thu thực tế"
          />
          <KpiCard 
            title="Tỷ lệ lấp đầy" 
            value={formatPercent(payload?.occupancyRateThisMonth)} 
            icon={<Icons.Chart />}
            colorClass="bg-blue-50"
            subtext="Trung bình toàn hệ thống"
          />
          <KpiCard 
            title="Tổng công nợ" 
            value={formatCurrency(payload?.totalOutstandingDebt)} 
            icon={<Icons.Alert />}
            colorClass="bg-rose-50"
            subtext="Cần thu hồi gấp"
          />
          <KpiCard 
            title="Bảo trì chờ xử lý" 
            value={payload?.pendingMaintenanceCount ?? 0} 
            icon={<Icons.Tool />}
            colorClass="bg-amber-50"
            subtext="Yêu cầu đang pending"
          />
        </div>

        {/* Charts & Tables Section 1 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Revenue Chart (Simulated) */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-full">
            <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex items-center justify-between gap-3">
              <h3 className="font-bold text-lg text-slate-800">Biểu đồ doanh thu</h3>
              <div className="inline-flex rounded-lg border border-slate-200 bg-white overflow-hidden">
                <button
                  type="button"
                  onClick={() => setRevenueChartMode('month')}
                  className={`px-3 py-1.5 text-sm font-semibold transition-colors ${revenueChartMode === 'month' ? 'bg-slate-100 text-slate-800' : 'bg-white text-slate-600 hover:bg-slate-50'}`}
                >
                  Theo tháng
                </button>
                <button
                  type="button"
                  onClick={() => setRevenueChartMode('branch')}
                  className={`px-3 py-1.5 text-sm font-semibold transition-colors border-l border-slate-200 ${revenueChartMode === 'branch' ? 'bg-slate-100 text-slate-800' : 'bg-white text-slate-600 hover:bg-slate-50'}`}
                >
                  Theo chi nhánh
                </button>
              </div>
            </div>
            <div className="p-6 flex-1">
              {revenueChartMode === 'month' ? (
                monthlyRevenueHistory.length === 0 ? (
                  <div className="h-48 flex items-center justify-center text-slate-400 italic">Chưa có dữ liệu.</div>
                ) : (
                  <div className="space-y-4">
                    {monthlyRevenueHistory
                      .slice()
                      .sort((a, b) => (b?.year || 0) - (a?.year || 0) || (b?.month || 0) - (a?.month || 0))
                      .slice(0, 6) // Show last 6 months
                      .map((r, idx) => {
                         const maxRev = Math.max(...monthlyRevenueHistory.map(i => i.revenue || 0));
                         const widthPct = maxRev > 0 ? ((r.revenue || 0) / maxRev) * 100 : 0;
                         return (
                           <div key={`${r?.year}-${r?.month}-${idx}`} className="flex items-center gap-4">
                             <div className="w-16 text-sm font-bold text-slate-500 text-right">T{r.month}/{r.year}</div>
                             <div className="flex-1 h-8 bg-slate-100 rounded-full overflow-hidden relative group">
                                <div 
                                  className="h-full bg-indigo-500 rounded-full transition-all duration-1000 ease-out flex items-center justify-end px-2"
                                  style={{ width: `${Math.max(widthPct, 5)}%` }}
                                >
                                  <span className="text-xs text-white font-bold opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                                     {formatCurrency(r.revenue)}
                                  </span>
                                </div>
                             </div>
                           </div>
                         )
                      })}
                  </div>
                )
              ) : (
                pieTotal <= 0 ? (
                  <div className="h-48 flex items-center justify-center text-slate-400 italic">Chưa có dữ liệu.</div>
                ) : (
                  <div className="flex flex-col md:flex-row gap-6 items-center md:items-start">
                    <div className="shrink-0">
                      <svg width="220" height="220" viewBox="0 0 220 220" className="block">
                        <circle cx="110" cy="110" r="78" className="text-slate-100" fill="none" stroke="currentColor" strokeWidth="18" />
                        {(() => {
                          const r = 78;
                          const c = 2 * Math.PI * r;
                          let offset = 0;
                          return pieData.map((item, idx) => {
                            const frac = pieTotal > 0 ? (item.revenue || 0) / pieTotal : 0;
                            const dash = frac * c;
                            const gap = Math.max(0, c - dash);
                            const el = (
                              <circle
                                key={`${item.branchId ?? item.branchName}-${idx}`}
                                cx="110"
                                cy="110"
                                r={r}
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="18"
                                strokeLinecap="butt"
                                className={pieColors[idx % pieColors.length]}
                                strokeDasharray={`${dash} ${gap}`}
                                strokeDashoffset={-offset}
                                transform="rotate(-90 110 110)"
                              />
                            );
                            offset += dash;
                            return el;
                          });
                        })()}
                        <text x="110" y="106" textAnchor="middle" className="fill-slate-800" style={{ fontSize: 14, fontWeight: 800 }}>
                          Tổng
                        </text>
                        <text x="110" y="128" textAnchor="middle" className="fill-slate-500" style={{ fontSize: 12, fontWeight: 700 }}>
                          {formatCurrency(pieTotal)}
                        </text>
                      </svg>
                    </div>

                    <div className="w-full">
                      <div className="text-sm font-semibold text-slate-700 mb-2">Doanh thu theo chi nhánh (tháng này)</div>
                      <div className="overflow-x-auto">
                        <table className="w-full text-left text-sm">
                          <thead className="bg-slate-50 text-slate-500 font-semibold border border-slate-200">
                            <tr>
                              <th className="px-4 py-2">Chi nhánh</th>
                              <th className="px-4 py-2 text-right">Doanh thu</th>
                              <th className="px-4 py-2 text-right">Tỷ trọng</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100 border border-slate-200 border-t-0">
                            {pieData.map((item, idx) => {
                              const pct = pieTotal > 0 ? ((item.revenue || 0) / pieTotal) * 100 : 0;
                              return (
                                <tr key={`${item.branchId ?? item.branchName}-${idx}`} className="hover:bg-slate-50 transition-colors">
                                  <td className="px-4 py-2 font-medium text-slate-700">
                                    <span className={`inline-block w-2.5 h-2.5 rounded-sm mr-2 align-middle ${pieColors[idx % pieColors.length].replace('text-', 'bg-')}`} />
                                    {item.branchName}
                                  </td>
                                  <td className="px-4 py-2 text-right font-semibold text-slate-800">{formatCurrency(item.revenue)}</td>
                                  <td className="px-4 py-2 text-right text-slate-600">{pct.toFixed(1)}%</td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  </div>
                )
              )}
            </div>
          </div>

          {/* Occupancy Table */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-full">
            <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50">
               <h3 className="font-bold text-lg text-slate-800">Hiệu suất chi nhánh</h3>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-3">Chi nhánh</th>
                    <th className="px-6 py-3 text-right">Tổng phòng</th>
                    <th className="px-6 py-3 text-right">Đã thuê</th>
                    <th className="px-6 py-3 text-right">Lấp đầy</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {roomOccupancyByBranch.length === 0 ? (
                    <tr><td colSpan="4" className="px-6 py-8 text-center text-slate-400">Chưa có dữ liệu</td></tr>
                  ) : (
                    roomOccupancyByBranch
                      .sort((a, b) => (b?.occupancyRate || 0) - (a?.occupancyRate || 0))
                      .map((r, idx) => (
                        <tr key={idx} className="hover:bg-slate-50 transition-colors">
                          <td className="px-6 py-3 font-medium text-slate-700">{r.branchName}</td>
                          <td className="px-6 py-3 text-right text-slate-600">{r.totalRooms}</td>
                          <td className="px-6 py-3 text-right text-slate-600">{r.occupiedRooms}</td>
                          <td className="px-6 py-3 text-right">
                            <span className={`inline-block px-2 py-1 rounded-md text-xs font-bold ${
                              (r.occupancyRate || 0) >= 0.8 ? 'bg-emerald-100 text-emerald-700' :
                              (r.occupancyRate || 0) >= 0.5 ? 'bg-amber-100 text-amber-700' :
                              'bg-rose-100 text-rose-700'
                            }`}>
                              {formatPercent(r.occupancyRate)}
                            </span>
                          </td>
                        </tr>
                      ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

        </div>

        {/* Tables Section 2 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Overdue Invoices */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-full">
            <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex justify-between items-center">
               <h3 className="font-bold text-lg text-slate-800">Cảnh báo công nợ</h3>
               <span className="text-xs font-bold text-rose-600 bg-rose-50 px-2 py-1 rounded uppercase">Top quá hạn</span>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-3">Mã HĐ</th>
                    <th className="px-6 py-3">Khách thuê</th>
                    <th className="px-6 py-3 text-right">Quá hạn</th>
                    <th className="px-6 py-3 text-right">Số tiền</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {topOverdueInvoices.length === 0 ? (
                    <tr><td colSpan="4" className="px-6 py-8 text-center text-slate-400">Không có hóa đơn quá hạn</td></tr>
                  ) : (
                    topOverdueInvoices.map((r, idx) => (
                      <tr key={idx} className="hover:bg-slate-50 transition-colors">
                        <td className="px-6 py-3 font-mono text-slate-600 text-xs">#{r.invoiceId}</td>
                        <td className="px-6 py-3 font-medium text-slate-700">{r.tenantName}</td>
                        <td className="px-6 py-3 text-right">
                          <span className="text-rose-600 font-bold">{r.daysOverdue} ngày</span>
                        </td>
                        <td className="px-6 py-3 text-right font-bold text-slate-800">{formatCurrency(r.amount)}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Expiring Contracts */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-full">
            <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex justify-between items-center">
               <h3 className="font-bold text-lg text-slate-800">Sắp hết hạn hợp đồng</h3>
               <span className="text-xs font-bold text-amber-600 bg-amber-50 px-2 py-1 rounded uppercase">Cần gia hạn</span>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200">
                  <tr>
                    <th className="px-6 py-3">Hợp đồng</th>
                    <th className="px-6 py-3">Phòng</th>
                    <th className="px-6 py-3">Hết hạn</th>
                    <th className="px-6 py-3 text-right">Còn lại</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {expiringContracts.length === 0 ? (
                    <tr><td colSpan="4" className="px-6 py-8 text-center text-slate-400">Không có hợp đồng sắp hết hạn</td></tr>
                  ) : (
                    expiringContracts
                      .sort((a, b) => (a?.daysRemaining || 0) - (b?.daysRemaining || 0))
                      .map((r, idx) => (
                        <tr key={idx} className="hover:bg-slate-50 transition-colors">
                          <td className="px-6 py-3 font-medium text-slate-700">
                            #{r.contractId}
                            <div className="text-xs text-slate-400 font-normal">{r.tenantName}</div>
                          </td>
                          <td className="px-6 py-3 text-slate-600">{r.roomInfo}</td>
                          <td className="px-6 py-3 text-slate-600">{r.endDate}</td>
                          <td className="px-6 py-3 text-right">
                            <span className="text-amber-600 font-bold">{r.daysRemaining} ngày</span>
                          </td>
                        </tr>
                      ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

        </div>

      </div>
    </div>
  );
}
import React, { useEffect, useMemo, useState } from 'react';
import checkoutRequestsApi from '../../api/checkoutRequestsApi';
import contractServicesApi from '../../api/contractServicesApi';
import resolveImageUrl from '../../utils/resolveImageUrl';

const SYSTEM_DEDUCTION_KEYS = new Set(['electricity_settlement', 'water_settlement', 'settle_all_fees']);

const DEFAULT_ITEMS = [
  { key: 'wall', label: 'Tường, sơn và vết bẩn', amount: 0, note: '' },
  { key: 'floor', label: 'Sàn nhà và gạch lát', amount: 0, note: '' },
  { key: 'sanitary', label: 'Thiết bị vệ sinh', amount: 0, note: '' },
  { key: 'furniture', label: 'Đồ nội thất', amount: 0, note: '' },
  { key: 'electrical', label: 'Thiết bị điện', amount: 0, note: '' },
  { key: 'keys', label: 'Khóa cửa / Chìa khóa', amount: 0, note: '' },
  { key: 'other', label: 'Khác', amount: 0, note: '' },
];

function safeParseJson(value) {
  if (!value) return null;
  try { return JSON.parse(value); } catch { return null; }
}

function formatCurrencyVnd(value) {
  const n = Number(value || 0);
  return new Intl.NumberFormat('vi-VN').format(Number.isFinite(n) ? n : 0);
}

function normalizeServiceName(name) {
  return (name || '').trim().toLowerCase();
}

function normalizeInspectionItems(items) {
  const rawItems = Array.isArray(items) ? items : [];
  const normalized = DEFAULT_ITEMS.map((base) => {
    const found = rawItems.find((item) => (item?.key || item?.itemKey) === base.key);
    return {
      ...base,
      amount: Number(found?.amount ?? base.amount ?? 0),
      note: (found?.note ?? base.note ?? '').toString(),
    };
  });

  const extras = rawItems
    .filter((item) => item?.key && !SYSTEM_DEDUCTION_KEYS.has(item.key) && !DEFAULT_ITEMS.some((base) => base.key === item.key))
    .map((item) => ({
      key: item.key,
      label: item.label || item.key,
      amount: Number(item.amount ?? 0),
      note: (item.note ?? '').toString(),
    }));

  return [...normalized, ...extras];
}

const Icons = {
  Refresh: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>,
  Check: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>,
  Save: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" /></svg>,
  Invoice: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>,
  Camera: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" /></svg>,
  Search: () => <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>,
  User: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>,
  Home: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>,
  ArrowLeft: () => <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" /></svg>
};

export default function Inspection() {
  const [requests, setRequests] = useState([]);
  const [loadingList, setLoadingList] = useState(false);
  const [listError, setListError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  const [selected, setSelected] = useState(null);
  const [report, setReport] = useState(null);
  const [utility, setUtility] = useState({
    electricityServiceId: null, electricityPrev: null, electricityCurr: null, electricityUnitPrice: null,
    waterServiceId: null, waterPrev: null, waterCurr: null, waterUnitPrice: null,
  });
  const [reportJson, setReportJson] = useState({
    electricityPrev: '', electricityCurr: '', waterPrev: '', waterCurr: '', items: DEFAULT_ITEMS,
  });

  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState({ message: '', type: '' });
  const [error, setError] = useState('');

  const confirmAction = (message) => { try { return window.confirm(message); } catch { return true; } };

  // --- LOGIC GIỮ NGUYÊN ---
  const electricityUsage = useMemo(() => {
    const prev = Number(reportJson.electricityPrev); const curr = Number(reportJson.electricityCurr);
    if (!Number.isFinite(prev) || !Number.isFinite(curr)) return null; return curr - prev;
  }, [reportJson.electricityPrev, reportJson.electricityCurr]);

  const waterUsage = useMemo(() => {
    const prev = Number(reportJson.waterPrev); const curr = Number(reportJson.waterCurr);
    if (!Number.isFinite(prev) || !Number.isFinite(curr)) return null; return curr - prev;
  }, [reportJson.waterPrev, reportJson.waterCurr]);

  const electricityAmount = useMemo(() => {
    const usage = electricityUsage; const unitPrice = Number(utility.electricityUnitPrice);
    if (!Number.isFinite(usage) || !Number.isFinite(unitPrice) || usage < 0) return null; return usage * unitPrice;
  }, [electricityUsage, utility.electricityUnitPrice]);

  const waterAmount = useMemo(() => {
    const usage = waterUsage; const unitPrice = Number(utility.waterUnitPrice);
    if (!Number.isFinite(usage) || !Number.isFinite(unitPrice) || usage < 0) return null; return usage * unitPrice;
  }, [waterUsage, utility.waterUnitPrice]);

  const totalAmount = useMemo(() => {
    const damageSum = (reportJson?.items || []).reduce((acc, it) => acc + Number(it.amount || 0), 0);
    const utilitySum = Number.isFinite(electricityAmount || 0) ? Number(electricityAmount || 0) : 0;
    const waterSum = Number.isFinite(waterAmount || 0) ? Number(waterAmount || 0) : 0;
    const sum = damageSum + utilitySum + waterSum;
    return Number.isFinite(sum) ? sum : 0;
  }, [reportJson, electricityAmount, waterAmount]);

  const upsertItem = (key, patch) => {
    setReportJson((prev) => {
      const items = Array.isArray(prev.items) ? prev.items : [];
      const idx = items.findIndex((x) => x?.key === key);
      if (idx >= 0) {
        const next = items.slice(); next[idx] = { ...next[idx], ...patch }; return { ...prev, items: next };
      }
      return { ...prev, items: [...items, { key, label: key, amount: 0, note: '', ...patch }] };
    });
  };

  useEffect(() => { if (electricityAmount != null) upsertItem('electricity_settlement', { amount: Math.max(0, Math.round(electricityAmount)) }); }, [electricityAmount]);
  useEffect(() => { if (waterAmount != null) upsertItem('water_settlement', { amount: Math.max(0, Math.round(waterAmount)) }); }, [waterAmount]);

  const itemImages = useMemo(() => {
    const images = report?.images || []; const byKey = {};
    images.forEach((img) => {
      const desc = img.description || ''; const m = desc.match(/^itemKey:([^;]+);/); const key = m ? m[1] : null;
      if (!key) return; byKey[key] = byKey[key] || []; byKey[key].push(img);
    });
    return byKey;
  }, [report]);

  const filteredRequests = useMemo(() => {
    const q = searchTerm.trim().toLowerCase(); if (!q) return requests;
    return requests.filter((row) => [row?.roomNumber, row?.roomCode, row?.tenantName, row?.contractId, row?.status, row?.id].filter(Boolean).join(' ').toLowerCase().includes(q));
  }, [requests, searchTerm]);

  const loadList = async () => {
    setLoadingList(true); setListError('');
    try {
      const page = await checkoutRequestsApi.listMyBranch({ status: ['PENDING', 'APPROVED'], page: 0, size: 50, sort: 'createdAt,desc' });
      setRequests(page?.content || []);
    } catch (e) { setListError(e?.message || 'Không tải được danh sách yêu cầu trả phòng'); } 
    finally { setLoadingList(false); }
  };

  useEffect(() => { loadList(); }, []);
  useEffect(() => { if (toast.message) { const timer = setTimeout(() => setToast({ message: '', type: '' }), 3000); return () => clearTimeout(timer); } }, [toast]);

  const loadReport = async (row) => {
    setSelected(row); setReport(null); setError(''); setToast({ message: '', type: '' }); setBusy(true);
    try {
      const [resp, services] = await Promise.all([ checkoutRequestsApi.getOrCreateReport(row.id), row?.contractId ? contractServicesApi.list(row.contractId) : Promise.resolve([]) ]);
      setReport(resp);
      const serviceList = Array.isArray(services) ? services : services?.data ?? [];
      const electricity = serviceList.find((s) => normalizeServiceName(s?.serviceName) === 'điện');
      const water = serviceList.find((s) => normalizeServiceName(s?.serviceName) === 'nước');
      
      setUtility({
        electricityServiceId: electricity?.id ?? null, electricityPrev: electricity?.previousReading ?? null, electricityCurr: electricity?.currentReading ?? null, electricityUnitPrice: electricity?.price ?? null,
        waterServiceId: water?.id ?? null, waterPrev: water?.previousReading ?? null, waterCurr: water?.currentReading ?? null, waterUnitPrice: water?.price ?? null,
      });

      const parsed = safeParseJson(resp?.damageDetails);
      const parsedItems = Array.isArray(parsed) ? parsed : Array.isArray(parsed?.items) ? parsed.items : [];
      setReportJson({
        electricityPrev: parsed?.electricityPrev ?? (electricity?.previousReading ?? ''), electricityCurr: parsed?.electricityCurr ?? parsed?.finalElectric ?? (electricity?.currentReading ?? ''),
        waterPrev: parsed?.waterPrev ?? (water?.previousReading ?? ''), waterCurr: parsed?.waterCurr ?? parsed?.finalWater ?? (water?.currentReading ?? ''),
        items: normalizeInspectionItems(parsedItems),
      });
    } catch (e) { setError(e?.message || 'Không tải được biên bản'); } 
    finally { setBusy(false); }
  };

  const approveSelected = async () => {
    if (!selected || busy) return;
    if (!confirmAction(`Duyệt yêu cầu trả phòng của phòng ${selected.roomNumber || selected.roomCode || 'N/A'}?`)) return;
    setBusy(true);
    setError('');
    try {
      const updated = await checkoutRequestsApi.approve(selected.id);
      setSelected(updated);
      setToast({ message: 'Đã duyệt yêu cầu trả phòng', type: 'success' });
      await loadList();
    } catch (e) {
      setError(e?.message || 'Không thể duyệt yêu cầu trả phòng');
    } finally {
      setBusy(false);
    }
  };

  const saveReport = async () => {
    if (!selected || busy) return;
    setBusy(true);
    setError('');
    try {
      const payload = {
        contractId: selected.contractId,
        description: report?.description || `Biên bản kiểm tra trả phòng - Phòng ${selected.roomNumber || selected.roomCode || 'N/A'}`,
        damageDetails: JSON.stringify({
          electricityPrev: reportJson.electricityPrev,
          electricityCurr: reportJson.electricityCurr,
          waterPrev: reportJson.waterPrev,
          waterCurr: reportJson.waterCurr,
          items: reportJson.items || [],
        }),
        totalDamageCost: totalAmount,
      };
      const updated = await checkoutRequestsApi.saveReport(selected.id, payload);
      setReport(updated);
      setToast({ message: 'Đã lưu biên bản kiểm tra', type: 'success' });
    } catch (e) {
      setError(e?.message || 'Không thể lưu biên bản');
    } finally {
      setBusy(false);
    }
  };

  const uploadImages = async (itemKey, fileList) => {
    if (!selected || busy) return;
    const files = Array.from(fileList || []);
    if (files.length === 0) return;
    setBusy(true);
    setError('');
    try {
      const updated = await checkoutRequestsApi.uploadItemImages(selected.id, itemKey, files);
      setReport(updated);
      setToast({ message: 'Đã tải ảnh minh chứng', type: 'success' });
    } catch (e) {
      setError(e?.message || 'Không thể tải ảnh minh chứng');
    } finally {
      setBusy(false);
    }
  };

  const createInvoice = async () => {
    if (!selected || busy) return;
    if (report?.settlementInvoiceId) {
      setToast({ message: 'Biên bản này đã có hóa đơn tất toán', type: 'success' });
      return;
    }
    if (!confirmAction(`Tạo hóa đơn tất toán cho phòng ${selected.roomNumber || selected.roomCode || 'N/A'}?`)) return;
    setBusy(true);
    setError('');
    try {
      const created = await checkoutRequestsApi.createInvoice(selected.id);
      setReport((prev) => (prev ? { ...prev, settlementInvoiceId: created?.id ?? prev.settlementInvoiceId } : prev));
      setToast({ message: `Đã tạo hóa đơn tất toán #${created?.id || ''}`.trim(), type: 'success' });
    } catch (e) {
      setError(e?.message || 'Không thể tạo hóa đơn tất toán');
    } finally {
      setBusy(false);
    }
  };

  // --- PHẦN UI MỚI ---
  return (
    <div className="bg-[color:var(--app-bg)] h-screen w-full flex flex-col overflow-hidden text-[color:var(--app-text)]">
      {/* Toast Notification */}
      {toast.message && (
        <div className={`fixed top-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg border-l-4 transform transition-all duration-300 ease-in-out translate-y-0 ${toast.type === 'success' ? 'bg-[color:var(--app-surface)] border-emerald-500 text-emerald-700' : 'bg-[color:var(--app-surface)] border-[color:var(--app-primary)] text-[color:var(--app-primary)]'}`}>
          <div className="flex items-center gap-2">
            {toast.type === 'success' && <Icons.Check />}
            <span className="font-medium">{toast.message}</span>
          </div>
        </div>
      )}

      {/* RENDER MÀN HÌNH DANH SÁCH (KHI CHƯA CHỌN PHÒNG) */}
      {!selected ? (
        <div className="flex flex-col h-full">
          <header className="bg-[color:var(--app-surface)] border-b border-[color:var(--app-border)] px-6 py-4 flex-shrink-0 flex items-center justify-between shadow-sm z-10">
            <div>
              <h1 className="text-xl font-bold text-[color:var(--app-text)]">Yêu cầu trả phòng</h1>
              <p className="text-sm text-[color:var(--app-muted)]">Quản lý và kiểm tra tình trạng phòng</p>
            </div>
            <div className="flex items-center gap-4">
              <div className="relative">
                <span className="absolute left-3 top-2.5 text-[color:var(--app-muted)]"><Icons.Search /></span>
                <input 
                  type="text" 
                  placeholder="Tìm phòng, mã HĐ..." 
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-64 pl-10 pr-4 py-2 rounded-lg border border-[color:var(--app-border)] focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] text-sm bg-[color:var(--app-surface)] text-[color:var(--app-text)]"
                />
              </div>
              <button onClick={loadList} disabled={loadingList} className="flex items-center gap-2 px-4 py-2 bg-[color:var(--app-surface)] border border-[color:var(--app-border)] text-[color:var(--app-text)] rounded-lg hover:bg-[color:var(--app-bg)] transition-colors font-medium text-sm">
                <Icons.Refresh /> {loadingList ? 'Đang tải...' : 'Làm mới'}
              </button>
            </div>
          </header>

          <main className="flex-1 overflow-y-auto p-6">
            {listError && <div className="p-4 mb-4 bg-rose-50 text-rose-700 rounded-lg border border-rose-200">{listError}</div>}
            
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {filteredRequests.map((r) => (
                <div key={r.id} onClick={() => loadReport(r)} className="bg-[color:var(--app-surface)] p-5 rounded-xl border border-[color:var(--app-border)] hover:border-[color:var(--app-primary)] hover:shadow-md transition-all cursor-pointer group">
                  <div className="flex justify-between items-start mb-3">
                    <div className="flex items-center gap-2">
                      <span className="p-2 rounded-lg bg-[color:var(--app-primary-soft)] text-[color:var(--app-primary)] group-hover:bg-[color:var(--app-primary)] group-hover:text-white transition-colors"><Icons.Home /></span>
                      <span className="font-bold text-lg text-[color:var(--app-text)]">{r.roomNumber || r.roomCode || 'N/A'}</span>
                    </div>
                    <span className={`px-2.5 py-1 rounded-md text-xs font-semibold border ${r.status === 'APPROVED' ? 'bg-[color:var(--app-primary-soft)] text-[color:var(--app-primary)] border-[color:var(--app-border)]' : 'bg-[color:var(--app-bg)] text-[color:var(--app-muted)] border-[color:var(--app-border)]'}`}>
                      {r.status}
                    </span>
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center gap-2 text-sm text-[color:var(--app-muted)]">
                      <Icons.User /> <span className="font-medium text-[color:var(--app-text)]">{r.tenantName || 'Không tên'}</span>
                    </div>
                    <div className="text-sm text-[color:var(--app-muted)]">HĐ #{r.contractId}</div>
                  </div>
                </div>
              ))}
              {filteredRequests.length === 0 && !loadingList && (
                <div className="col-span-full text-center py-12 text-[color:var(--app-muted)] bg-[color:var(--app-surface)] rounded-xl border border-dashed border-[color:var(--app-border)]">Không có yêu cầu nào phù hợp</div>
              )}
            </div>
          </main>
        </div>
      ) : (
        /* RENDER MÀN HÌNH CHI TIẾT LÀM VIỆC (KHI ĐÃ CHỌN PHÒNG) */
        <div className="flex flex-col h-full bg-[color:var(--app-bg)]">
          <header className="bg-[color:var(--app-surface)] border-b border-[color:var(--app-border)] px-6 py-3 flex-shrink-0 flex items-center justify-between shadow-sm z-10 sticky top-0">
            <div className="flex items-center gap-4">
              <button onClick={() => setSelected(null)} className="p-2 -ml-2 text-[color:var(--app-muted)] hover:text-[color:var(--app-text)] hover:bg-[color:var(--app-primary-soft)] rounded-lg transition-colors flex items-center gap-2 font-medium">
                <Icons.ArrowLeft /> <span>Quay lại</span>
              </button>
              <div className="h-6 w-px bg-[color:var(--app-border)]"></div>
              <div>
                <h1 className="text-lg font-bold text-[color:var(--app-text)]">Biên bản #{selected.id} - Phòng {selected.roomNumber || selected.roomCode || 'N/A'}</h1>
                <p className="text-xs text-[color:var(--app-muted)]">Khách thuê: {selected.tenantName} · HĐ #{selected.contractId}</p>
              </div>
            </div>
            <div className="flex gap-2">
              <span className={`px-3 py-1.5 rounded-lg text-sm font-semibold border ${selected.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-amber-50 text-amber-700 border-amber-200'}`}>{selected.status}</span>
              <span className="px-3 py-1.5 rounded-lg text-sm font-semibold border bg-[color:var(--app-surface)] text-[color:var(--app-text)] border-[color:var(--app-border)]">{report?.settlementInvoiceId ? `Hóa đơn #${report.settlementInvoiceId}` : 'Chưa xuất HĐ'}</span>
            </div>
          </header>

          <div className="flex-1 overflow-y-auto p-6 custom-scrollbar">
            <div className="max-w-4xl mx-auto space-y-6 pb-32">
              {error && (
                <div className="bg-rose-50 border border-rose-200 text-rose-700 px-4 py-3 rounded-lg flex items-start gap-3">
                  <span className="mt-0.5"><Icons.Check /> {/* Replace with alert icon if needed */}</span>
                  <span>{error}</span>
                </div>
              )}

              {/* Khối Điện / Nước làm dạng lưới gọn gàng hơn */}
              <div className="bg-[color:var(--app-surface)] rounded-xl shadow-sm border border-[color:var(--app-border)] overflow-hidden">
                <div className="bg-[color:var(--app-bg)] px-5 py-4 border-b border-[color:var(--app-border)]">
                  <h3 className="font-semibold text-[color:var(--app-text)]">Chỉ số Điện / Nước chốt sổ</h3>
                </div>
                <div className="p-5 grid grid-cols-1 md:grid-cols-2 gap-6 divide-y md:divide-y-0 md:divide-x divide-[color:var(--app-border)]">
                  {/* Điện */}
                      <div className="space-y-4 md:pr-6">
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-[color:var(--app-text)]">Điện năng tiêu thụ (kWh)</span>
                      <span className="text-sm font-semibold text-amber-600">{electricityAmount != null ? `${formatCurrencyVnd(Math.round(electricityAmount))}đ` : '-'}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs text-[color:var(--app-muted)] mb-1">Số đầu</label>
                        <input className="w-full border border-[color:var(--app-border)] rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] outline-none bg-[color:var(--app-surface)] text-[color:var(--app-text)]" value={reportJson.electricityPrev} onChange={(e) => setReportJson((p) => ({ ...p, electricityPrev: e.target.value }))} placeholder={utility.electricityPrev ?? ''} />
                      </div>
                      <div>
                        <label className="block text-xs text-[color:var(--app-muted)] mb-1">Số cuối</label>
                        <input className="w-full border border-[color:var(--app-border)] rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] outline-none bg-[color:var(--app-surface)] text-[color:var(--app-text)]" value={reportJson.electricityCurr} onChange={(e) => setReportJson((p) => ({ ...p, electricityCurr: e.target.value }))} placeholder={utility.electricityCurr ?? ''} />
                      </div>
                    </div>
                    <div className="text-xs text-[color:var(--app-muted)] flex justify-between">
                      <span>Sử dụng: <strong>{Number.isFinite(electricityUsage) ? electricityUsage : '-'}</strong></span>
                      <span>Đơn giá: <strong>{utility.electricityUnitPrice != null ? `${formatCurrencyVnd(utility.electricityUnitPrice)}đ` : '-'}</strong></span>
                    </div>
                  </div>

                  {/* Nước */}
                    <div className="space-y-4 pt-4 md:pt-0 md:pl-6">
                     <div className="flex items-center justify-between">
                      <span className="font-medium text-[color:var(--app-text)]">Nước sinh hoạt (m³)</span>
                      <span className="text-sm font-semibold text-amber-600">{waterAmount != null ? `${formatCurrencyVnd(Math.round(waterAmount))}đ` : '-'}</span>
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs text-[color:var(--app-muted)] mb-1">Số đầu</label>
                        <input className="w-full border border-[color:var(--app-border)] rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] outline-none bg-[color:var(--app-surface)] text-[color:var(--app-text)]" value={reportJson.waterPrev} onChange={(e) => setReportJson((p) => ({ ...p, waterPrev: e.target.value }))} placeholder={utility.waterPrev ?? ''} />
                      </div>
                      <div>
                        <label className="block text-xs text-[color:var(--app-muted)] mb-1">Số cuối</label>
                        <input className="w-full border border-[color:var(--app-border)] rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] outline-none bg-[color:var(--app-surface)] text-[color:var(--app-text)]" value={reportJson.waterCurr} onChange={(e) => setReportJson((p) => ({ ...p, waterCurr: e.target.value }))} placeholder={utility.waterCurr ?? ''} />
                      </div>
                    </div>
                    <div className="text-xs text-[color:var(--app-muted)] flex justify-between">
                      <span>Sử dụng: <strong>{Number.isFinite(waterUsage) ? waterUsage : '-'}</strong></span>
                      <span>Đơn giá: <strong>{utility.waterUnitPrice != null ? `${formatCurrencyVnd(utility.waterUnitPrice)}đ` : '-'}</strong></span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Danh sách hạng mục */}
              <div className="bg-[color:var(--app-surface)] rounded-xl shadow-sm border border-[color:var(--app-border)] overflow-hidden">
                <div className="bg-[color:var(--app-bg)] px-5 py-4 border-b border-[color:var(--app-border)]">
                  <h3 className="font-semibold text-[color:var(--app-text)]">Đánh giá hiện trạng & Bồi thường</h3>
                </div>
                <div className="divide-y divide-[color:var(--app-border)]">
                  {(reportJson.items || []).map((it) => (
                    <div key={it.key} className="p-5 flex flex-col sm:flex-row gap-4 hover:bg-[color:var(--app-primary-soft)]/20 transition-colors">
                      <div className="sm:w-1/3 flex-shrink-0">
                        <div className="font-semibold text-[color:var(--app-text)]">{it.label}</div>
                        <label className="mt-3 cursor-pointer inline-flex items-center gap-1.5 text-xs font-medium text-[color:var(--app-primary)] hover:text-[color:var(--app-primary-hover)] bg-[color:var(--app-primary-soft)] px-2.5 py-1.5 rounded-lg border border-[color:var(--app-border)] w-fit">
                          <Icons.Camera /> Thêm ảnh
                          <input type="file" multiple accept="image/*" className="hidden" onChange={(e) => uploadImages(it.key, e.target.files)} disabled={busy} />
                        </label>
                        
                        {/* Hiển thị ảnh nhỏ gọn bên dưới nút */}
                        {itemImages[it.key]?.length > 0 && (
                          <div className="flex flex-wrap gap-1.5 mt-2">
                            {itemImages[it.key].map((img) => (
                              <a key={img.id} href={resolveImageUrl(img.imageUrl)} target="_blank" rel="noreferrer" className="block w-10 h-10 border border-[color:var(--app-border)] rounded-md overflow-hidden">
                                <img src={resolveImageUrl(img.imageUrl)} alt="" className="w-full h-full object-cover" />
                              </a>
                            ))}
                          </div>
                        )}
                      </div>
                      
                      <div className="sm:w-2/3 flex flex-col sm:flex-row gap-3">
                        <textarea
                          className="flex-1 border border-[color:var(--app-border)] rounded-lg px-3 py-2 text-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] outline-none resize-none bg-[color:var(--app-surface)] text-[color:var(--app-text)]"
                          value={it.note} onChange={(e) => setReportJson((p) => ({ ...p, items: (p.items || []).map((x) => (x.key === it.key ? { ...x, note: e.target.value } : x)) }))}
                          placeholder="Mô tả tình trạng..." rows={2}
                        />
                        <div className="sm:w-32 relative flex-shrink-0">
                          <input
                            className="w-full border border-[color:var(--app-border)] rounded-lg pl-3 pr-8 py-2 text-sm text-right font-medium focus:ring-2 focus:ring-[color:var(--app-primary-soft)] outline-none bg-[color:var(--app-surface)] text-[color:var(--app-text)]"
                            type="number" min={0} value={Number(it.amount || 0)}
                            onChange={(e) => setReportJson((p) => ({ ...p, items: (p.items || []).map((x) => x.key === it.key ? { ...x, amount: Number(e.target.value || 0) } : x) }))}
                          />
                          <span className="absolute right-3 top-2 text-sm text-slate-400">đ</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* CHÂN TRANG CỐ ĐỊNH CHỨA ACTION - STICKY FOOTER */}
          <div className="fixed bottom-0 right-0 w-full bg-[color:var(--app-surface)] border-t border-[color:var(--app-border)] p-4 shadow-[0_-10px_20px_rgba(0,0,0,0.03)] z-20 flex justify-center">
            <div className="w-full max-w-4xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <div className="text-sm text-[color:var(--app-muted)] uppercase tracking-wide font-medium">Tổng bồi thường & Tất toán</div>
                <div className="text-2xl font-bold text-[color:var(--app-primary)]">{formatCurrencyVnd(totalAmount)} VNĐ</div>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                {selected.status === 'PENDING' && (
                  <button onClick={approveSelected} disabled={busy} className="px-5 py-2.5 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-lg hover:bg-emerald-100 font-medium transition-colors flex items-center gap-2">
                    <Icons.Check /> Duyệt yêu cầu
                  </button>
                )}
                <button onClick={saveReport} disabled={busy} className="px-5 py-2.5 bg-[color:var(--app-primary)] text-white rounded-lg hover:bg-[color:var(--app-primary-hover)] font-medium shadow-sm transition-colors flex items-center gap-2">
                  <Icons.Save /> Lưu biên bản
                </button>
                {!report?.settlementInvoiceId && (
                  <button onClick={createInvoice} disabled={busy} className="px-5 py-2.5 bg-[color:var(--app-text)] text-white rounded-lg hover:opacity-90 font-medium shadow-sm transition-colors flex items-center gap-2">
                    <Icons.Invoice /> Tạo HĐ Tất toán
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
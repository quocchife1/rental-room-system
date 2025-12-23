import React, { useEffect, useMemo, useState } from 'react';
import checkoutRequestsApi from '../../api/checkoutRequestsApi';
import contractServicesApi from '../../api/contractServicesApi';
import resolveImageUrl from '../../utils/resolveImageUrl';

const DEFAULT_ITEMS = [
  { key: 'electricity_settlement', label: 'Tiền điện (tất toán)', amount: 0, note: '' },
  { key: 'water_settlement', label: 'Tiền nước (tất toán)', amount: 0, note: '' },
  { key: 'settle_all_fees', label: 'Tất toán tất cả chi phí', amount: 0, note: '' },
  { key: 'wall', label: 'Tường (sơn/tường dơ/nứt)', amount: 0, note: '' },
  { key: 'floor', label: 'Sàn nhà (gạch/trầy/xước)', amount: 0, note: '' },
  { key: 'sanitary', label: 'Thiết bị vệ sinh', amount: 0, note: '' },
  { key: 'furniture', label: 'Đồ nội thất', amount: 0, note: '' },
  { key: 'electrical', label: 'Thiết bị điện', amount: 0, note: '' },
  { key: 'keys', label: 'Khóa cửa / Chìa khóa', amount: 0, note: '' },
  { key: 'other', label: 'Khác', amount: 0, note: '' },
];

function safeParseJson(value) {
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function formatCurrencyVnd(value) {
  const n = Number(value || 0);
  return new Intl.NumberFormat('vi-VN').format(Number.isFinite(n) ? n : 0);
}

function normalizeServiceName(name) {
  return (name || '').trim().toLowerCase();
}

const Icons = {
  Refresh: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
  ),
  Check: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
  ),
  Save: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" /></svg>
  ),
  Invoice: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
  ),
  Camera: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
  ),
  Search: () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
  ),
  User: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
  ),
  Home: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>
  )
};

export default function Inspection() {
  const [requests, setRequests] = useState([]);
  const [loadingList, setLoadingList] = useState(false);
  const [listError, setListError] = useState('');

  const [selected, setSelected] = useState(null);
  const [report, setReport] = useState(null);
  const [utility, setUtility] = useState({
    electricityServiceId: null,
    electricityPrev: null,
    electricityCurr: null,
    electricityUnitPrice: null,
    waterServiceId: null,
    waterPrev: null,
    waterCurr: null,
    waterUnitPrice: null,
  });
  const [reportJson, setReportJson] = useState({
    electricityPrev: '',
    electricityCurr: '',
    waterPrev: '',
    waterCurr: '',
    items: DEFAULT_ITEMS,
  });

  const [busy, setBusy] = useState(false);
  const [toast, setToast] = useState({ message: '', type: '' });
  const [error, setError] = useState('');

  const confirmAction = (message) => {
    try {
      return window.confirm(message);
    } catch {
      return true;
    }
  };

  const totalAmount = useMemo(() => {
    const sum = (reportJson?.items || []).reduce((acc, it) => acc + Number(it.amount || 0), 0);
    return Number.isFinite(sum) ? sum : 0;
  }, [reportJson]);

  const electricityUsage = useMemo(() => {
    const prev = Number(reportJson.electricityPrev);
    const curr = Number(reportJson.electricityCurr);
    if (!Number.isFinite(prev) || !Number.isFinite(curr)) return null;
    return curr - prev;
  }, [reportJson.electricityPrev, reportJson.electricityCurr]);

  const waterUsage = useMemo(() => {
    const prev = Number(reportJson.waterPrev);
    const curr = Number(reportJson.waterCurr);
    if (!Number.isFinite(prev) || !Number.isFinite(curr)) return null;
    return curr - prev;
  }, [reportJson.waterPrev, reportJson.waterCurr]);

  const electricityAmount = useMemo(() => {
    const usage = electricityUsage;
    const unitPrice = Number(utility.electricityUnitPrice);
    if (!Number.isFinite(usage) || !Number.isFinite(unitPrice) || usage < 0) return null;
    return usage * unitPrice;
  }, [electricityUsage, utility.electricityUnitPrice]);

  const waterAmount = useMemo(() => {
    const usage = waterUsage;
    const unitPrice = Number(utility.waterUnitPrice);
    if (!Number.isFinite(usage) || !Number.isFinite(unitPrice) || usage < 0) return null;
    return usage * unitPrice;
  }, [waterUsage, utility.waterUnitPrice]);

  const upsertItem = (key, patch) => {
    setReportJson((prev) => {
      const items = Array.isArray(prev.items) ? prev.items : [];
      const idx = items.findIndex((x) => x?.key === key);
      if (idx >= 0) {
        const next = items.slice();
        next[idx] = { ...next[idx], ...patch };
        return { ...prev, items: next };
      }
      return { ...prev, items: [...items, { key, label: key, amount: 0, note: '', ...patch }] };
    });
  };

  useEffect(() => {
    if (electricityAmount !== null && electricityAmount !== undefined) {
      upsertItem('electricity_settlement', { amount: Math.max(0, Math.round(electricityAmount)) });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [electricityAmount]);

  useEffect(() => {
    if (waterAmount !== null && waterAmount !== undefined) {
      upsertItem('water_settlement', { amount: Math.max(0, Math.round(waterAmount)) });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [waterAmount]);

  const itemImages = useMemo(() => {
    const images = report?.images || [];
    const byKey = {};
    images.forEach((img) => {
      const desc = img.description || '';
      const m = desc.match(/^itemKey:([^;]+);/);
      const key = m ? m[1] : null;
      if (!key) return;
      byKey[key] = byKey[key] || [];
      byKey[key].push(img);
    });
    return byKey;
  }, [report]);

  const loadList = async () => {
    setLoadingList(true);
    setListError('');
    try {
      const page = await checkoutRequestsApi.listMyBranch({
        status: ['PENDING', 'APPROVED'],
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
      });
      setRequests(page?.content || []);
    } catch (e) {
      setListError(e?.message || 'Không tải được danh sách yêu cầu trả phòng');
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    loadList();
  }, []);

  useEffect(() => {
    if (toast.message) {
      const timer = setTimeout(() => setToast({ message: '', type: '' }), 3000);
      return () => clearTimeout(timer);
    }
  }, [toast]);

  const loadReport = async (row) => {
    setSelected(row);
    setReport(null);
    setUtility({
      electricityServiceId: null,
      electricityPrev: null,
      electricityCurr: null,
      electricityUnitPrice: null,
      waterServiceId: null,
      waterPrev: null,
      waterCurr: null,
      waterUnitPrice: null,
    });
    setError('');
    setToast({ message: '', type: '' });
    setBusy(true);
    try {
      const [resp, services] = await Promise.all([
        checkoutRequestsApi.getOrCreateReport(row.id),
        row?.contractId ? contractServicesApi.list(row.contractId) : Promise.resolve([]),
      ]);

      setReport(resp);

      const serviceList = Array.isArray(services) ? services : services?.data ?? [];
      const electricity = serviceList.find((s) => normalizeServiceName(s?.serviceName) === 'điện');
      const water = serviceList.find((s) => normalizeServiceName(s?.serviceName) === 'nước');
      setUtility({
        electricityServiceId: electricity?.id ?? null,
        electricityPrev: electricity?.previousReading ?? null,
        electricityCurr: electricity?.currentReading ?? null,
        electricityUnitPrice: electricity?.price ?? null,
        waterServiceId: water?.id ?? null,
        waterPrev: water?.previousReading ?? null,
        waterCurr: water?.currentReading ?? null,
        waterUnitPrice: water?.price ?? null,
      });

      const parsed = safeParseJson(resp?.damageDetails);
      const parsedItems = Array.isArray(parsed)
        ? parsed
        : Array.isArray(parsed?.items)
          ? parsed.items
          : null;

      const mergedItems = DEFAULT_ITEMS.map((base) => {
        const found = (parsedItems || []).find((x) => (x.key || x.itemKey) === base.key);
        return {
          ...base,
          amount: Number(found?.amount ?? base.amount ?? 0),
          note: (found?.note ?? base.note ?? '').toString(),
        };
      });

      const defaultElecPrev = parsed?.electricityPrev ?? (electricity?.previousReading ?? '');
      const defaultElecCurr = parsed?.electricityCurr ?? parsed?.finalElectric ?? (electricity?.currentReading ?? '');
      const defaultWaterPrev = parsed?.waterPrev ?? (water?.previousReading ?? '');
      const defaultWaterCurr = parsed?.waterCurr ?? parsed?.finalWater ?? (water?.currentReading ?? '');

      setReportJson({
        electricityPrev: defaultElecPrev ?? '',
        electricityCurr: defaultElecCurr ?? '',
        waterPrev: defaultWaterPrev ?? '',
        waterCurr: defaultWaterCurr ?? '',
        items: mergedItems,
      });
    } catch (e) {
      setError(e?.message || 'Không tải được biên bản');
    } finally {
      setBusy(false);
    }
  };

  const approveSelected = async () => {
    if (!selected) return;

    const ok = confirmAction(
      `Xác nhận duyệt yêu cầu trả phòng?\n\n- Phòng: ${selected.roomNumber || selected.roomCode || 'N/A'}\n- HĐ: #${selected.contractId}\n- Yêu cầu: #${selected.id}`
    );
    if (!ok) return;

    setBusy(true);
    setError('');
    try {
      const updated = await checkoutRequestsApi.approve(selected.id);
      setSelected(updated);
      setToast({ message: 'Đã duyệt yêu cầu trả phòng thành công', type: 'success' });
      await loadList();
    } catch (e) {
      setError(e?.message || 'Duyệt yêu cầu thất bại');
    } finally {
      setBusy(false);
    }
  };

  const saveReport = async () => {
    if (!selected) return;

    // Basic validation: if previous reading exists, final reading must be >= previous.
    const pe = Number(reportJson.electricityPrev);
    const ce = Number(reportJson.electricityCurr);
    const pw = Number(reportJson.waterPrev);
    const cw = Number(reportJson.waterCurr);

    if (reportJson.electricityPrev !== '' && reportJson.electricityCurr !== '') {
      if (!Number.isFinite(pe) || !Number.isFinite(ce)) {
        setError('Chỉ số điện không hợp lệ');
        return;
      }
      if (ce < pe) {
        setError('Chỉ số điện không hợp lệ (Số cuối < Số đầu)');
        return;
      }
    }

    if (reportJson.waterPrev !== '' && reportJson.waterCurr !== '') {
      if (!Number.isFinite(pw) || !Number.isFinite(cw)) {
        setError('Chỉ số nước không hợp lệ');
        return;
      }
      if (cw < pw) {
        setError('Chỉ số nước không hợp lệ (Số cuối < Số đầu)');
        return;
      }
    }

    const ok = confirmAction(
      `Xác nhận lưu biên bản kiểm tra?\n\n- Phòng: ${selected.roomNumber || selected.roomCode || 'N/A'}\n- HĐ: #${selected.contractId}\n- Tổng bồi thường: ${formatCurrencyVnd(totalAmount)} VNĐ`
    );
    if (!ok) return;

    setBusy(true);
    setError('');
    try {
      const payload = {
        description: `Biên bản kiểm tra trả phòng - HĐ #${selected.contractId}`,
        damageDetails: JSON.stringify({
          electricityPrev: reportJson.electricityPrev,
          electricityCurr: reportJson.electricityCurr,
          waterPrev: reportJson.waterPrev,
          waterCurr: reportJson.waterCurr,
          items: reportJson.items,
        }),
        totalDamageCost: totalAmount,
      };
      const resp = await checkoutRequestsApi.saveReport(selected.id, payload);
      setReport(resp);
      setToast({ message: 'Đã lưu biên bản thành công', type: 'success' });
    } catch (e) {
      setError(e?.message || 'Lưu biên bản thất bại');
    } finally {
      setBusy(false);
    }
  };

  const uploadImages = async (itemKey, fileList) => {
    if (!selected) return;
    const files = Array.from(fileList || []);
    if (files.length === 0) return;
    setBusy(true);
    setError('');
    try {
      const resp = await checkoutRequestsApi.uploadItemImages(selected.id, itemKey, files);
      setReport(resp);
      setToast({ message: 'Tải ảnh lên thành công', type: 'success' });
    } catch (e) {
      setError(e?.message || 'Tải ảnh thất bại');
    } finally {
      setBusy(false);
    }
  };

  const createInvoice = async () => {
    if (!selected) return;
    if (selected.status !== 'APPROVED') {
      setError('Cần duyệt yêu cầu trả phòng trước khi tạo hóa đơn');
      return;
    }

    const ok = confirmAction(
      `Xác nhận tạo hóa đơn tất toán từ biên bản này?\n\n- Phòng: ${selected.roomNumber || selected.roomCode || 'N/A'}\n- HĐ: #${selected.contractId}\n- Tổng bồi thường: ${formatCurrencyVnd(totalAmount)} VNĐ\n\nSau khi hóa đơn được thanh toán, hệ thống sẽ hoàn tất trả phòng.`
    );
    if (!ok) return;

    setBusy(true);
    setError('');
    try {
      const invoice = await checkoutRequestsApi.createInvoice(selected.id);
      setToast({ message: `Đã tạo hóa đơn #${invoice?.id}`, type: 'success' });
      const refreshed = await checkoutRequestsApi.getOrCreateReport(selected.id);
      setReport(refreshed);
    } catch (e) {
      setError(e?.message || 'Tạo hóa đơn thất bại');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="bg-gray-50 h-screen w-full flex flex-col overflow-hidden text-slate-800">
      {/* Toast Notification */}
      {toast.message && (
        <div className={`fixed top-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg border-l-4 transform transition-all duration-300 ease-in-out translate-y-0 ${toast.type === 'success' ? 'bg-white border-emerald-500 text-emerald-700' : 'bg-white border-blue-500 text-blue-700'}`}>
          <div className="flex items-center gap-2">
            {toast.type === 'success' && <Icons.Check />}
            <span className="font-medium">{toast.message}</span>
          </div>
        </div>
      )}

      {/* Header */}
      <header className="bg-white border-b px-6 py-4 flex-shrink-0 flex items-center justify-between shadow-sm z-10">
        <div>
          <h1 className="text-xl font-bold text-slate-800">Quản lý Trả phòng & Kiểm tra</h1>
          <p className="text-sm text-slate-500">Xử lý yêu cầu, lập biên bản và tất toán hợp đồng</p>
        </div>
        <button
          onClick={loadList}
          disabled={loadingList}
          className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 text-slate-700 rounded-lg hover:bg-slate-50 transition-colors focus:ring-2 focus:ring-slate-200 font-medium text-sm"
        >
          <Icons.Refresh />
          {loadingList ? 'Đang tải...' : 'Làm mới'}
        </button>
      </header>

      <main className="flex-1 flex overflow-hidden">
        {/* Left Sidebar - Request List */}
        <aside className="w-1/3 min-w-[350px] max-w-md bg-white border-r flex flex-col z-0">
          <div className="p-4 border-b bg-slate-50/50">
            <div className="relative">
              <span className="absolute left-3 top-2.5 text-slate-400"><Icons.Search /></span>
              <input 
                type="text" 
                placeholder="Tìm theo phòng hoặc tên..." 
                className="w-full pl-10 pr-4 py-2 rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm"
              />
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto p-2 space-y-2 custom-scrollbar">
            {listError && <div className="p-3 bg-red-50 text-red-600 rounded-lg text-sm">{listError}</div>}
            
            {requests.length === 0 && !loadingList ? (
              <div className="text-center py-10 text-slate-400">Không có yêu cầu nào</div>
            ) : (
              requests.map((r) => (
                <div
                  key={r.id}
                  onClick={() => loadReport(r)}
                  className={`group p-4 rounded-xl border cursor-pointer transition-all duration-200 hover:shadow-md ${
                    selected?.id === r.id 
                      ? 'bg-indigo-50 border-indigo-200 ring-1 ring-indigo-200' 
                      : 'bg-white border-slate-100 hover:border-slate-300'
                  }`}
                >
                  <div className="flex justify-between items-start mb-2">
                    <div className="flex items-center gap-2">
                      <span className="p-1.5 rounded-md bg-indigo-100 text-indigo-700"><Icons.Home /></span>
                      <span className="font-bold text-slate-800">{r.roomNumber || r.roomCode || 'N/A'}</span>
                    </div>
                    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                      r.status === 'APPROVED' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                    }`}>
                      {r.status}
                    </span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-slate-600 mb-1">
                    <Icons.User />
                    <span className="font-medium">{r.tenantName || 'Không tên'}</span>
                  </div>
                  <div className="flex justify-between items-end mt-2 text-xs text-slate-400">
                    <span>HĐ #{r.contractId}</span>
                    <span>ID: {r.id}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </aside>

        {/* Right Content - Report Detail */}
        <section className="flex-1 flex flex-col bg-slate-50 overflow-hidden relative">
          {!selected ? (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-400">
              <svg className="w-20 h-20 mb-4 text-slate-200" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
              <p className="text-lg font-medium">Chọn một yêu cầu để bắt đầu làm việc</p>
            </div>
          ) : (
            <>
              {/* Vùng cuộn: Chỉ chứa nội dung form */}
              <div className="flex-1 overflow-y-auto custom-scrollbar p-6">
                <div className="max-w-5xl mx-auto">
                  
                  {/* Error Banner */}
                  {error && (
                    <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg shadow-sm flex items-start gap-3">
                      <svg className="w-5 h-5 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                      <span>{error}</span>
                    </div>
                  )}

                  {/* Toolbar / Header */}
                  <div className="flex items-center justify-between mb-8 bg-white p-4 rounded-xl shadow-sm border border-slate-100">
                     <div>
                       <h2 className="text-lg font-bold text-slate-800">Biên bản kiểm tra</h2>
                       <div className="text-sm text-slate-500 mt-0.5">Yêu cầu #{selected.id} • {selected.roomNumber || selected.roomCode}</div>
                     </div>
                     <div className="flex items-center gap-3">
                        {selected.status === 'PENDING' && (
                          <button onClick={approveSelected} disabled={busy} className="flex items-center gap-2 px-4 py-2 bg-white border border-emerald-500 text-emerald-600 rounded-lg hover:bg-emerald-50 font-medium transition-colors">
                            <Icons.Check /> Duyệt
                          </button>
                        )}
                        <button onClick={saveReport} disabled={busy} className="flex items-center gap-2 px-5 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 font-medium shadow-md shadow-indigo-200 transition-all active:scale-95">
                          <Icons.Save /> Lưu biên bản
                        </button>
                     </div>
                  </div>

                  {/* Meter Readings Card */}
                  <div className="bg-white rounded-xl shadow-sm border border-slate-100 p-6 mb-6">
                    <h3 className="font-semibold text-slate-800 mb-4 flex items-center gap-2">
                      <span className="w-1 h-5 bg-indigo-500 rounded-full block"></span>
                      Chỉ số điện nước
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div className="bg-slate-50 p-4 rounded-lg border border-slate-100">
                        <label className="block text-sm font-medium text-slate-700 mb-3">Điện (kWh)</label>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <div>
                            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Số đầu</label>
                            <input
                              className="w-full bg-white border border-slate-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none font-mono"
                              value={reportJson.electricityPrev}
                              onChange={(e) => setReportJson((p) => ({ ...p, electricityPrev: e.target.value }))}
                              inputMode="numeric"
                              placeholder={utility.electricityPrev ?? 'Nhập số đầu...'}
                            />
                          </div>
                          <div>
                            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Số cuối</label>
                            <input
                              className="w-full bg-white border border-slate-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none font-mono"
                              value={reportJson.electricityCurr}
                              onChange={(e) => setReportJson((p) => ({ ...p, electricityCurr: e.target.value }))}
                              inputMode="numeric"
                              placeholder={utility.electricityCurr ?? 'Nhập số cuối...'}
                            />
                          </div>
                        </div>
                        <div className="mt-3 text-sm text-slate-600 flex items-center justify-between">
                          <div>
                            <span className="text-slate-500">Số dùng:</span>{' '}
                            <span className="font-semibold">{Number.isFinite(electricityUsage) ? electricityUsage : '-'}</span>
                          </div>
                          <div className="text-right">
                            <div>
                              <span className="text-slate-500">Đơn giá:</span>{' '}
                              <span className="font-semibold">{utility.electricityUnitPrice != null ? `${formatCurrencyVnd(utility.electricityUnitPrice)}đ/kWh` : '-'}</span>
                            </div>
                            <div>
                              <span className="text-slate-500">Thành tiền:</span>{' '}
                              <span className="font-bold text-amber-700">{electricityAmount != null ? `${formatCurrencyVnd(Math.round(electricityAmount))}đ` : '-'}</span>
                            </div>
                          </div>
                        </div>
                      </div>

                      <div className="bg-slate-50 p-4 rounded-lg border border-slate-100">
                        <label className="block text-sm font-medium text-slate-700 mb-3">Nước (m³)</label>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          <div>
                            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Số đầu</label>
                            <input
                              className="w-full bg-white border border-slate-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none font-mono"
                              value={reportJson.waterPrev}
                              onChange={(e) => setReportJson((p) => ({ ...p, waterPrev: e.target.value }))}
                              inputMode="numeric"
                              placeholder={utility.waterPrev ?? 'Nhập số đầu...'}
                            />
                          </div>
                          <div>
                            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Số cuối</label>
                            <input
                              className="w-full bg-white border border-slate-300 rounded-lg px-4 py-2.5 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none font-mono"
                              value={reportJson.waterCurr}
                              onChange={(e) => setReportJson((p) => ({ ...p, waterCurr: e.target.value }))}
                              inputMode="numeric"
                              placeholder={utility.waterCurr ?? 'Nhập số cuối...'}
                            />
                          </div>
                        </div>
                        <div className="mt-3 text-sm text-slate-600 flex items-center justify-between">
                          <div>
                            <span className="text-slate-500">Số dùng:</span>{' '}
                            <span className="font-semibold">{Number.isFinite(waterUsage) ? waterUsage : '-'}</span>
                          </div>
                          <div className="text-right">
                            <div>
                              <span className="text-slate-500">Đơn giá:</span>{' '}
                              <span className="font-semibold">{utility.waterUnitPrice != null ? `${formatCurrencyVnd(utility.waterUnitPrice)}đ/m³` : '-'}</span>
                            </div>
                            <div>
                              <span className="text-slate-500">Thành tiền:</span>{' '}
                              <span className="font-bold text-amber-700">{waterAmount != null ? `${formatCurrencyVnd(Math.round(waterAmount))}đ` : '-'}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Items List */}
                  <div className="bg-white rounded-xl shadow-sm border border-slate-100 overflow-hidden mb-4">
                    <div className="p-6 border-b border-slate-100">
                      <h3 className="font-semibold text-slate-800 flex items-center gap-2">
                        <span className="w-1 h-5 bg-amber-500 rounded-full block"></span>
                        Chi tiết hư hại & Bồi thường
                      </h3>
                    </div>
                    
                    <div className="divide-y divide-slate-100">
                      {(reportJson.items || []).map((it) => (
                        <div key={it.key} className="p-6 hover:bg-slate-50 transition-colors">
                          <div className="flex flex-col lg:flex-row gap-6">
                            {['electricity_settlement', 'water_settlement'].includes(it.key) && (
                              <div className="w-full bg-amber-50 border border-amber-200 text-amber-800 px-4 py-2 rounded-lg text-sm">
                                Mục này được tự tính từ chỉ số điện/nước.
                              </div>
                            )}
                            
                            {/* Item Info & Note */}
                            <div className="flex-1">
                              <div className="flex items-center justify-between mb-2">
                                <span className="font-bold text-slate-700 text-base">{it.label}</span>
                                <span className="text-xs font-mono text-slate-400 bg-slate-100 px-2 py-0.5 rounded">{it.key}</span>
                              </div>
                              <textarea
                                className="w-full border border-slate-300 rounded-lg px-4 py-3 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none text-sm min-h-[80px] resize-y"
                                value={it.note}
                                onChange={(e) =>
                                  setReportJson((p) => ({
                                    ...p,
                                    items: (p.items || []).map((x) => (x.key === it.key ? { ...x, note: e.target.value } : x)),
                                  }))
                                }
                                placeholder={`Mô tả tình trạng ${it.label.toLowerCase()}...`}
                                rows={3}
                              />
                            </div>

                            {/* Controls: Amount & Upload */}
                            <div className="w-full lg:w-72 flex flex-col gap-4">
                              <div>
                                 <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Chi phí bồi thường</label>
                                 <div className="relative">
                                   <input
                                     className="w-full border border-slate-300 rounded-lg pl-4 pr-10 py-2.5 text-right font-semibold text-slate-800 focus:ring-2 focus:ring-amber-500 focus:border-amber-500 outline-none"
                                     type="number"
                                     min={0}
                                     value={Number(it.amount || 0)}
                                       disabled={['electricity_settlement', 'water_settlement'].includes(it.key)}
                                     onChange={(e) =>
                                       setReportJson((p) => ({
                                         ...p,
                                         items: (p.items || []).map((x) =>
                                           x.key === it.key ? { ...x, amount: Number(e.target.value || 0) } : x
                                         ),
                                       }))
                                     }
                                   />
                                   <span className="absolute right-4 top-2.5 text-slate-400 font-medium">đ</span>
                                 </div>
                                 <div className="text-right text-xs text-slate-500 mt-1">
                                   {formatCurrencyVnd(it.amount)} VNĐ
                                 </div>
                              </div>

                              <div>
                                <div className="flex items-center justify-between mb-2">
                                  <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider">Hình ảnh</label>
                                  <label className="cursor-pointer text-xs font-medium text-indigo-600 hover:text-indigo-800 flex items-center gap-1">
                                    <Icons.Camera /> Thêm ảnh
                                    <input
                                      type="file"
                                      multiple
                                      accept="image/*"
                                      className="hidden"
                                      onChange={(e) => uploadImages(it.key, e.target.files)}
                                      disabled={busy || ['electricity_settlement', 'water_settlement'].includes(it.key)}
                                    />
                                  </label>
                                </div>
                                
                                <div className="flex flex-wrap gap-2">
                                  {itemImages[it.key]?.length > 0 ? (
                                    itemImages[it.key].map((img) => (
                                      <a
                                        key={img.id}
                                        href={resolveImageUrl(img.imageUrl)}
                                        target="_blank"
                                        rel="noreferrer"
                                        className="block relative group overflow-hidden rounded-lg border border-slate-200 w-16 h-16"
                                      >
                                        <img
                                          src={resolveImageUrl(img.imageUrl)}
                                          alt=""
                                          className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-110"
                                        />
                                      </a>
                                    ))
                                  ) : (
                                    <div className="w-full py-3 border border-dashed border-slate-300 rounded-lg text-center text-xs text-slate-400 bg-slate-50">
                                      Chưa có ảnh
                                    </div>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              {/* Footer Actions - Nằm ngoài vùng cuộn, cố định ở dưới */}
              <div className="flex-shrink-0 border-t bg-white p-6 shadow-[0_-5px_15px_rgba(0,0,0,0.05)] z-20">
                  <div className="max-w-5xl mx-auto flex items-center justify-between">
                    <div>
                      <div className="text-sm font-medium text-slate-500 mb-1 uppercase tracking-wide">Tổng cộng bồi thường</div>
                      <div className="text-3xl font-bold text-amber-600 tracking-tight">{formatCurrencyVnd(totalAmount)} <span className="text-lg text-amber-500 font-medium">VNĐ</span></div>
                    </div>
                    
                    <div className="flex items-center gap-4">
                      {report?.settlementInvoiceId ? (
                          <div className="bg-green-50 text-green-700 px-6 py-3 rounded-xl border border-green-200 font-medium flex items-center gap-3">
                            <span className="p-1 bg-white rounded-full"><Icons.Invoice /></span>
                            <span>Đã có hóa đơn <strong>#{report.settlementInvoiceId}</strong></span>
                          </div>
                      ) : (
                        <button
                          onClick={createInvoice}
                          disabled={busy}
                          className="flex items-center gap-3 px-8 py-3.5 bg-slate-800 hover:bg-slate-900 text-white rounded-xl font-semibold shadow-lg shadow-slate-200 transition-all transform active:scale-95"
                        >
                          <Icons.Invoice />
                          <span>Tạo hóa đơn tất toán</span>
                        </button>
                      )}
                    </div>
                  </div>
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  );
}
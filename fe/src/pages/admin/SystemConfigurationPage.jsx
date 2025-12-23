import React, { useEffect, useState } from 'react';
import systemConfigApi from '../../api/systemConfigApi';

// --- Icons (Inline SVG) ---
const Icons = {
  Lightning: () => <svg className="w-5 h-5 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" /></svg>,
  Water: () => <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" /></svg>,
  Clock: () => <svg className="w-5 h-5 text-rose-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>,
  Save: () => <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" /></svg>,
  Refresh: ({ spin }) => <svg className={`w-5 h-5 ${spin ? 'animate-spin' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>,
  User: () => <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>,
  Phone: () => <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" /></svg>,
  Qr: () => <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v1m6 11h2m-6 0h-2v4h2v-4zM6 8V4h6v4H6zm14 12V8h-6v12h6z" /></svg>,
  Check: () => <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>,
  Code: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" /></svg>
};

export default function SystemConfigurationPage() {
  const [config, setConfig] = useState(null);
  const [electricPrice, setElectricPrice] = useState('');
  const [waterPrice, setWaterPrice] = useState('');
  const [lateFeePerDay, setLateFeePerDay] = useState('');
  const [momoReceiverName, setMomoReceiverName] = useState('');
  const [momoReceiverPhone, setMomoReceiverPhone] = useState('');
  const [momoReceiverQrUrl, setMomoReceiverQrUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);
  const [showRaw, setShowRaw] = useState(false);

  async function load() {
    setLoading(true);
    setError('');
    setSaved(false);
    try {
      const res = await systemConfigApi.get();
      const payload = res;
      setConfig(payload);
      setElectricPrice(payload?.electricPricePerUnit ?? '');
      setWaterPrice(payload?.waterPricePerUnit ?? '');
      setLateFeePerDay(payload?.lateFeePerDay ?? '');
      setMomoReceiverName(payload?.momoReceiverName ?? '');
      setMomoReceiverPhone(payload?.momoReceiverPhone ?? '');
      setMomoReceiverQrUrl(payload?.momoReceiverQrUrl ?? '');
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

  useEffect(() => {
    if (saved) {
      const timer = setTimeout(() => setSaved(false), 3000);
      return () => clearTimeout(timer);
    }
  }, [saved]);

  async function save() {
    setLoading(true);
    setError('');
    setSaved(false);
    try {
      const res = await systemConfigApi.upsert({
        electricPricePerUnit: electricPrice === '' ? null : Number(electricPrice),
        waterPricePerUnit: waterPrice === '' ? null : Number(waterPrice),
        lateFeePerDay: lateFeePerDay === '' ? null : Number(lateFeePerDay),
        momoReceiverName: momoReceiverName === '' ? null : momoReceiverName,
        momoReceiverPhone: momoReceiverPhone === '' ? null : momoReceiverPhone,
        momoReceiverQrUrl: momoReceiverQrUrl === '' ? null : momoReceiverQrUrl,
      });
      const payload = res;
      setConfig(payload);
      setSaved(true);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Lưu thất bại');
    } finally {
      setLoading(false);
    }
  }

  // --- Helper Component for Inputs ---
  const InputGroup = ({ label, icon, value, onChange, placeholder, type = "text", unit }) => (
    <div className="mb-4">
      <label className="block text-sm font-medium text-slate-700 mb-1.5">{label}</label>
      <div className="relative rounded-md shadow-sm">
        <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
          {icon}
        </div>
        <input
          type={type}
          className="block w-full rounded-lg border-slate-300 pl-10 pr-12 py-2.5 focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm transition-shadow"
          placeholder={placeholder}
          value={value}
          onChange={onChange}
        />
        {unit && (
          <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-3">
            <span className="text-gray-500 sm:text-sm font-medium bg-slate-50 px-2 py-1 rounded">{unit}</span>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-5xl mx-auto space-y-6">
        
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Cấu hình hệ thống</h1>
            <p className="text-slate-500 mt-1">Thiết lập đơn giá dịch vụ và thông tin thanh toán toàn hệ thống.</p>
          </div>
          <div className="flex gap-3">
            <button
              onClick={load}
              disabled={loading}
              className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-300 rounded-lg text-slate-700 font-medium hover:bg-slate-50 transition-all shadow-sm"
            >
              <Icons.Refresh spin={loading} />
              Tải lại
            </button>
            <button
              onClick={save}
              disabled={loading}
              className="flex items-center gap-2 px-6 py-2 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 transition-all shadow-md active:scale-95 disabled:opacity-70"
            >
              <Icons.Save />
              Lưu thay đổi
            </button>
          </div>
        </div>

        {/* Notifications */}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 rounded-r shadow-sm flex items-center justify-between animate-fade-in">
            <span>{error}</span>
          </div>
        )}
        {saved && (
          <div className="bg-emerald-50 border-l-4 border-emerald-500 text-emerald-700 p-4 rounded-r shadow-sm flex items-center gap-2 animate-fade-in">
            <Icons.Check />
            <span className="font-medium">Cấu hình đã được cập nhật thành công!</span>
          </div>
        )}

        {/* Main Content Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          
          {/* Card 1: Service Fees */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex items-center gap-2">
              <span className="p-2 bg-indigo-100 text-indigo-600 rounded-lg"><Icons.Lightning /></span>
              <h2 className="font-bold text-lg text-slate-800">Chi phí dịch vụ & Phạt</h2>
            </div>
            <div className="p-6">
              <InputGroup
                label="Giá điện"
                icon={<Icons.Lightning />}
                value={electricPrice}
                onChange={(e) => setElectricPrice(e.target.value)}
                placeholder="0"
                type="number"
                unit="đ/kWh"
              />
              <InputGroup
                label="Giá nước"
                icon={<Icons.Water />}
                value={waterPrice}
                onChange={(e) => setWaterPrice(e.target.value)}
                placeholder="0"
                type="number"
                unit="đ/m³"
              />
               <hr className="my-6 border-slate-100" />
              <InputGroup
                label="Phí trễ hạn thanh toán"
                icon={<Icons.Clock />}
                value={lateFeePerDay}
                onChange={(e) => setLateFeePerDay(e.target.value)}
                placeholder="0"
                type="number"
                unit="đ/ngày"
              />
            </div>
          </div>

          {/* Card 2: Payment Config */}
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-full">
            <div className="px-6 py-4 border-b border-slate-100 bg-slate-50/50 flex items-center gap-2">
              <span className="p-2 bg-pink-100 text-pink-600 rounded-lg">
                <svg className="w-5 h-5" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z" /><path d="M15.5 11h-7c-.28 0-.5.22-.5.5v5c0 .28.22.5.5.5h7c.28 0 .5-.22.5-.5v-5c0-.28-.22-.5-.5-.5zm-1.5 4.5h-4v-3h4v3z" /></svg>
              </span>
              <div>
                <h2 className="font-bold text-lg text-slate-800">Cấu hình ví MoMo</h2>
                <p className="text-xs text-slate-500">Thông tin nhận cọc & thanh toán online</p>
              </div>
            </div>
            
            <div className="p-6 flex-1 flex flex-col">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                 <InputGroup
                  label="Tên người nhận"
                  icon={<Icons.User />}
                  value={momoReceiverName}
                  onChange={(e) => setMomoReceiverName(e.target.value)}
                  placeholder="Nguyễn Văn A"
                />
                <InputGroup
                  label="Số điện thoại ví"
                  icon={<Icons.Phone />}
                  value={momoReceiverPhone}
                  onChange={(e) => setMomoReceiverPhone(e.target.value)}
                  placeholder="09xxxxxxxx"
                />
              </div>

              <div className="mb-4">
                <label className="block text-sm font-medium text-slate-700 mb-1.5">Link ảnh QR Code (URL)</label>
                <div className="flex gap-2 items-start">
                   <div className="relative rounded-md shadow-sm flex-1">
                    <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
                      <Icons.Qr />
                    </div>
                    <input
                      type="text"
                      className="block w-full rounded-lg border-slate-300 pl-10 py-2.5 focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm"
                      placeholder="https://example.com/my-qr.png"
                      value={momoReceiverQrUrl}
                      onChange={(e) => setMomoReceiverQrUrl(e.target.value)}
                    />
                  </div>
                </div>
              </div>

              {/* QR Preview Area */}
              <div className="mt-auto pt-4 border-t border-slate-100 flex items-center justify-center">
                 {momoReceiverQrUrl ? (
                   <div className="text-center">
                      <p className="text-xs text-slate-400 mb-2 uppercase font-bold tracking-wide">Xem trước mã QR</p>
                      
                      <img 
                        src={momoReceiverQrUrl} 
                        alt="QR Preview" 
                        className="h-32 w-32 object-contain border rounded-lg p-1 bg-white shadow-sm mx-auto"
                        onError={(e) => {e.target.onerror = null; e.target.src='https://via.placeholder.com/150?text=Invalid+Image'}} 
                      />
                   </div>
                 ) : (
                   <div className="h-32 w-full border-2 border-dashed border-slate-200 rounded-lg flex flex-col items-center justify-center text-slate-400">
                      <Icons.Qr />
                      <span className="text-xs mt-1">Chưa có mã QR</span>
                   </div>
                 )}
              </div>
            </div>
          </div>
        </div>

        {/* Technical Details (Collapsible) */}
        <div className="mt-8">
           <button 
             onClick={() => setShowRaw(!showRaw)} 
             className="flex items-center gap-2 text-xs font-bold text-slate-500 hover:text-indigo-600 transition-colors uppercase tracking-wider"
           >
             <Icons.Code />
             {showRaw ? 'Ẩn chi tiết kỹ thuật' : 'Xem chi tiết kỹ thuật (JSON)'}
           </button>
           
           {showRaw && (
             <div className="mt-3 bg-slate-900 rounded-xl p-4 shadow-inner overflow-hidden animate-fade-in-down">
                <pre className="text-xs text-green-400 font-mono whitespace-pre-wrap overflow-x-auto">
                  {JSON.stringify(config, null, 2)}
                </pre>
             </div>
           )}
        </div>

      </div>
    </div>
  );
}
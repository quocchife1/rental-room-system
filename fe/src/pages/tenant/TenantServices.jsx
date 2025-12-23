import React, { useEffect, useMemo, useState } from 'react';
import contractApi from '../../api/contractApi';
import contractServicesApi from '../../api/contractServicesApi';
import serviceBookingApi from '../../api/serviceBookingApi';
import axiosClient from '../../api/axiosClient';

export default function TenantServices() {
  const [contracts, setContracts] = useState([]);
  const [contractId, setContractId] = useState('');

  const [availableServices, setAvailableServices] = useState([]);
  const [contractServices, setContractServices] = useState([]);
  const [bookings, setBookings] = useState([]);

  const [addServiceId, setAddServiceId] = useState('');
  const [addQty, setAddQty] = useState(1);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const selectedContract = useMemo(
    () => contracts.find((c) => String(c.id) === String(contractId)),
    [contracts, contractId]
  );

  async function loadContracts() {
    const res = await contractApi.getMyContracts();
    let data = [];
    if (Array.isArray(res)) data = res;
    else if (res && res.content) data = res.content;
    else if (res && res.data) data = res.data.result || res.data || [];
    else data = res || [];
    setContracts(data);

    const active = data.find((c) => c.status === 'ACTIVE') || data[0];
    if (active && active.id) setContractId(String(active.id));
  }

  async function loadAvailableServices() {
    const res = await axiosClient.get('/api/services');
    const list = Array.isArray(res) ? res : res?.data ?? [];
    setAvailableServices(list);
  }

  async function loadContractData(cid) {
    if (!cid) return;
    const [sv, bk] = await Promise.all([
      contractServicesApi.list(cid),
      serviceBookingApi.listByContract(cid),
    ]);
    const services = Array.isArray(sv) ? sv : sv?.data ?? [];
    const bookingList = Array.isArray(bk) ? bk : bk?.data ?? [];
    setContractServices(services);
    setBookings(bookingList);
  }

  useEffect(() => {
    (async () => {
      setLoading(true);
      setError('');
      try {
        await Promise.all([loadContracts(), loadAvailableServices()]);
      } catch (e) {
        setError(e?.response?.data?.message || e?.message || 'Tải dữ liệu thất bại');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    (async () => {
      setError('');
      try {
        await loadContractData(contractId);
      } catch (e) {
        setError(e?.response?.data?.message || e?.message || 'Tải dữ liệu hợp đồng thất bại');
      }
    })();
  }, [contractId]);

  const selectableServices = useMemo(() => {
    return availableServices.filter((s) => {
      const name = (s.serviceName || '').toLowerCase();
      if (name === 'điện' || name === 'nước') return false;
      if (name.includes('bảo vệ') || name.includes('bao ve')) return false;
      if (name.includes('vệ sinh') || name.includes('ve sinh')) return false;
      return true;
    });
  }, [availableServices]);

  const selectedAddService = useMemo(
    () => selectableServices.find((s) => String(s.id) === String(addServiceId)),
    [selectableServices, addServiceId]
  );

  const isParkingSelected = useMemo(() => {
    const name = (selectedAddService?.serviceName || '').toLowerCase();
    return name === 'giữ xe máy' || name.includes('giữ xe') || name.includes('giu xe');
  }, [selectedAddService]);

  useEffect(() => {
    if (!isParkingSelected) setAddQty(1);
  }, [isParkingSelected, addServiceId]);

  async function addService() {
    if (!contractId || !addServiceId) return;
    setLoading(true);
    setError('');
    try {
      await contractServicesApi.add(contractId, {
        serviceId: Number(addServiceId),
        quantity: Number(addQty) || 1,
        startDate: new Date().toISOString().slice(0, 10),
      });
      await loadContractData(contractId);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Đăng ký dịch vụ thất bại');
    } finally {
      setLoading(false);
    }
  }

  async function cancelService(csId) {
    setLoading(true);
    setError('');
    try {
      await contractServicesApi.cancel(contractId, csId);
      await loadContractData(contractId);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Hủy dịch vụ thất bại');
    } finally {
      setLoading(false);
    }
  }

  async function bookCleaning() {
    setLoading(true);
    setError('');
    try {
      await serviceBookingApi.bookCleaning(contractId);
      await loadContractData(contractId);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Đăng ký vệ sinh thất bại');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-6xl mx-auto space-y-8">
        
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Quản lý dịch vụ</h1>
          <p className="text-slate-500 mt-2">Đăng ký thêm tiện ích và đặt lịch vệ sinh phòng cho hợp đồng của bạn.</p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
            <div>
              <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
                Chọn hợp đồng đang thuê
              </label>
              <select
                className="block w-full bg-slate-50 border border-slate-200 text-slate-800 py-3 px-4 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
                value={contractId}
                onChange={(e) => setContractId(e.target.value)}
              >
                <option value="">-- Vui lòng chọn hợp đồng --</option>
                {contracts.map((c) => (
                  <option key={c.id} value={String(c.id)}>
                    Mã HĐ: {c.contractCode || c.id} — Phòng {c.roomCode || c.roomNumber || 'N/A'}
                  </option>
                ))}
              </select>
            </div>
            
            <div className="bg-slate-50 rounded-xl p-4 border border-slate-100 h-full flex flex-col justify-center">
              {selectedContract ? (
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-slate-600">Trạng thái hợp đồng</span>
                  <span className={`px-3 py-1 rounded-full text-xs font-bold ${selectedContract.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                    {selectedContract.status}
                  </span>
                </div>
              ) : (
                <span className="text-sm text-slate-400 italic">Chưa chọn hợp đồng nào</span>
              )}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          <div className="lg:col-span-2 space-y-8">
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
              <div className="bg-slate-900 px-6 py-4 flex items-center justify-between">
                 <h3 className="text-white font-bold text-lg">Dịch vụ đang sử dụng</h3>
                 <span className="text-slate-400 text-xs uppercase font-bold tracking-wider">Thuê bao tháng</span>
              </div>
              
              <div className="p-6 bg-slate-50 border-b border-slate-200">
                <div className="text-sm font-bold text-slate-700 mb-3 uppercase tracking-wide">Đăng ký thêm dịch vụ</div>
                <div className="flex flex-col md:flex-row gap-3">
                  <div className="flex-grow">
                    <select
                      className="w-full bg-white border border-slate-300 rounded-lg py-2.5 px-3 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      value={addServiceId}
                      onChange={(e) => setAddServiceId(e.target.value)}
                    >
                      <option value="">Chọn dịch vụ muốn thêm...</option>
                      {selectableServices.map((s) => (
                        <option key={s.id} value={String(s.id)}>
                          {s.serviceName} — {new Intl.NumberFormat('vi-VN').format(s.price)} đ / {s.unit}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="w-full md:w-24">
                    <input
                      type="number"
                      placeholder="SL"
                      className="w-full bg-white border border-slate-300 rounded-lg py-2.5 px-3 text-sm text-center focus:ring-2 focus:ring-indigo-500 disabled:bg-slate-100 disabled:text-slate-400"
                      value={addQty}
                      min={1}
                      disabled={!isParkingSelected}
                      onChange={(e) => setAddQty(e.target.value)}
                    />
                  </div>
                  <button 
                    onClick={addService} 
                    disabled={loading || !contractId || !addServiceId}
                    className="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-5 py-2.5 rounded-lg text-sm font-bold transition-all shadow-md active:scale-95 whitespace-nowrap"
                  >
                    Xác nhận thêm
                  </button>
                </div>
                {!isParkingSelected && addServiceId && (
                  <p className="text-xs text-slate-500 mt-2 ml-1">
                    * Số lượng mặc định là 1 cho dịch vụ này.
                  </p>
                )}
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                  <thead className="bg-white border-b border-slate-100 text-slate-500">
                    <tr>
                      <th className="px-6 py-4 font-semibold">Tên dịch vụ</th>
                      <th className="px-6 py-4 font-semibold text-center">Số lượng</th>
                      <th className="px-6 py-4 font-semibold">Ngày bắt đầu</th>
                      <th className="px-6 py-4 font-semibold text-right">Hành động</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-50">
                    {contractServices.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-6 py-8 text-center text-slate-400 italic">
                          Chưa có dịch vụ nào được đăng ký
                        </td>
                      </tr>
                    ) : (
                      contractServices.map((cs) => {
                        const name = (cs.serviceName || '').toLowerCase();
                        const canCancel =
                          name !== 'điện' &&
                          name !== 'nước' &&
                          !name.includes('bảo vệ') &&
                          !name.includes('bao ve');

                        return (
                          <tr key={cs.id} className="hover:bg-slate-50 transition-colors">
                            <td className="px-6 py-4 font-medium text-slate-800">{cs.serviceName}</td>
                            <td className="px-6 py-4 text-center text-slate-600">{cs.quantity}</td>
                            <td className="px-6 py-4 text-slate-600">{cs.startDate || '---'}</td>
                            <td className="px-6 py-4 text-right">
                              {canCancel ? (
                                <button
                                  className="text-rose-600 hover:text-rose-800 text-xs font-bold border border-rose-200 hover:bg-rose-50 px-3 py-1.5 rounded transition-all"
                                  onClick={() => cancelService(cs.id)}
                                >
                                  Hủy đăng ký
                                </button>
                              ) : (
                                <span className="text-xs font-bold text-slate-400 bg-slate-100 px-2 py-1 rounded">Cố định</span>
                              )}
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 relative overflow-hidden">
               <div className="absolute top-0 right-0 w-20 h-20 bg-indigo-50 rounded-bl-full -mr-10 -mt-10 z-0"></div>
               <div className="relative z-10">
                 <h3 className="font-bold text-lg text-slate-800 mb-1">Dọn vệ sinh phòng</h3>
                 <p className="text-sm text-slate-500 mb-4">Lịch định kỳ: <span className="font-semibold text-indigo-600">Thứ 5 hàng tuần</span> (08:00 - 11:00)</p>
                 
                 <button
                   onClick={bookCleaning}
                   disabled={loading || !contractId}
                   className="w-full bg-slate-800 hover:bg-slate-900 disabled:opacity-50 disabled:cursor-not-allowed text-white py-3 rounded-xl font-bold text-sm shadow-lg shadow-slate-200 transition-all active:scale-95"
                 >
                   Đăng ký cho tuần tới
                 </button>
               </div>
            </div>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
               <div className="px-6 py-4 border-b border-slate-100 bg-slate-50">
                 <h4 className="font-bold text-sm text-slate-700 uppercase tracking-wide">Lịch sử đặt dọn</h4>
               </div>
               <div className="max-h-[400px] overflow-y-auto custom-scrollbar">
                  <table className="w-full text-sm text-left">
                    <tbody className="divide-y divide-slate-100">
                      {bookings.length === 0 ? (
                        <tr>
                          <td className="px-6 py-8 text-center text-slate-400 text-xs">
                            Chưa có lịch sử đặt dịch vụ
                          </td>
                        </tr>
                      ) : (
                        bookings
                          .filter((b) => (b.serviceName || '').toLowerCase().includes('vệ sinh'))
                          .sort((a, b) => String(b.bookingDate).localeCompare(String(a.bookingDate)))
                          .map((b) => (
                            <tr key={b.id} className="hover:bg-slate-50">
                              <td className="px-6 py-3">
                                <div className="font-medium text-slate-800">{b.bookingDate}</div>
                                <div className="text-xs text-slate-500 mt-0.5">{b.startTime} - {b.endTime}</div>
                              </td>
                              <td className="px-6 py-3 text-right">
                                <span className={`inline-block px-2 py-1 rounded text-xs font-bold ${
                                  b.status === 'CONFIRMED' || b.status === 'COMPLETED' 
                                    ? 'bg-emerald-50 text-emerald-700' 
                                    : b.status === 'CANCELLED' 
                                      ? 'bg-rose-50 text-rose-700' 
                                      : 'bg-amber-50 text-amber-700'
                                }`}>
                                  {b.status}
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

        </div>

        {loading && (
          <div className="fixed inset-0 bg-white/60 backdrop-blur-sm z-50 flex items-center justify-center">
             <div className="bg-white px-6 py-4 rounded-xl shadow-2xl border border-slate-100 flex flex-col items-center">
                <div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mb-3"></div>
                <span className="font-bold text-slate-700 text-sm">Đang xử lý...</span>
             </div>
          </div>
        )}

      </div>
    </div>
  );
}
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
    <div className="min-h-screen bg-[color:var(--app-bg)] p-6 md:p-10 font-sans text-[color:var(--app-text)]">
      <div className="max-w-6xl mx-auto space-y-8">
        
        <div>
          <h1 className="text-3xl font-extrabold text-[color:var(--app-text)] tracking-tight">Quản lý dịch vụ</h1>
          <p className="text-[color:var(--app-muted)] mt-2">Đăng ký thêm tiện ích và đặt lịch vệ sinh phòng cho hợp đồng của bạn.</p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
          </div>
        )}

        <div className="bg-[color:var(--app-surface-solid)] rounded-2xl shadow-sm border border-[color:var(--app-border)] p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
            <div>
              <label className="block text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider mb-2">
                Chọn hợp đồng đang thuê
              </label>
              <select
                className="block w-full bg-[color:var(--app-bg)] border border-[color:var(--app-border-strong)] text-[color:var(--app-text)] py-3 px-4 rounded-xl focus:outline-none focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] transition-all"
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
            
            <div className="bg-[color:var(--app-bg)] rounded-xl p-4 border border-[color:var(--app-border)] h-full flex flex-col justify-center">
              {selectedContract ? (
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-[color:var(--app-muted)]">Trạng thái hợp đồng</span>
                  <span className={`px-3 py-1 rounded-full text-xs font-bold ${selectedContract.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-[color:var(--app-bg)] text-[color:var(--app-muted)] border border-[color:var(--app-border)]'}`}>
                    {selectedContract.status}
                  </span>
                </div>
              ) : (
                <span className="text-sm text-[color:var(--app-muted-2)] italic">Chưa chọn hợp đồng nào</span>
              )}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          <div className="lg:col-span-2 space-y-8">
            <div className="bg-[color:var(--app-surface-solid)] rounded-2xl shadow-sm border border-[color:var(--app-border)] overflow-hidden">
              <div className="bg-[color:var(--app-text)] px-6 py-4 flex items-center justify-between">
                 <h3 className="text-white font-bold text-lg">Dịch vụ đang sử dụng</h3>
                 <span className="text-white/70 text-xs uppercase font-bold tracking-wider">Thuê bao tháng</span>
              </div>
              
              <div className="p-6 bg-[color:var(--app-bg)] border-b border-[color:var(--app-border)]">
                <div className="text-sm font-bold text-[color:var(--app-text)] mb-3 uppercase tracking-wide">Đăng ký thêm dịch vụ</div>
                <div className="flex flex-col md:flex-row gap-3">
                  <div className="flex-grow">
                    <select
                      className="w-full bg-[color:var(--app-surface-solid)] border border-[color:var(--app-border-strong)] text-[color:var(--app-text)] rounded-lg py-2.5 px-3 text-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)]"
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
                      className="w-full bg-[color:var(--app-surface-solid)] border border-[color:var(--app-border-strong)] text-[color:var(--app-text)] rounded-lg py-2.5 px-3 text-sm text-center focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] disabled:bg-[color:var(--app-border)] disabled:text-[color:var(--app-muted-2)]"
                      value={addQty}
                      min={1}
                      disabled={!isParkingSelected}
                      onChange={(e) => setAddQty(e.target.value)}
                    />
                  </div>
                  <button 
                    onClick={addService} 
                    disabled={loading || !contractId || !addServiceId}
                    className="bg-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-hover)] disabled:opacity-50 disabled:cursor-not-allowed text-white px-5 py-2.5 rounded-lg text-sm font-bold transition-all shadow-md active:scale-95 whitespace-nowrap"
                  >
                    Xác nhận thêm
                  </button>
                </div>
                {!isParkingSelected && addServiceId && (
                  <p className="text-xs text-[color:var(--app-muted)] mt-2 ml-1">
                    * Số lượng mặc định là 1 cho dịch vụ này.
                  </p>
                )}
              </div>

              <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                  <thead className="bg-[color:var(--app-surface-solid)] border-b border-[color:var(--app-border)] text-[color:var(--app-muted)]">
                    <tr>
                      <th className="px-6 py-4 font-semibold">Tên dịch vụ</th>
                      <th className="px-6 py-4 font-semibold text-center">Số lượng</th>
                      <th className="px-6 py-4 font-semibold">Ngày bắt đầu</th>
                      <th className="px-6 py-4 font-semibold text-right">Hành động</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[color:var(--app-border)]">
                    {contractServices.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-6 py-8 text-center text-[color:var(--app-muted-2)] italic">
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
                          <tr key={cs.id} className="hover:bg-[color:var(--app-bg)] transition-colors">
                            <td className="px-6 py-4 font-medium text-[color:var(--app-text)]">{cs.serviceName}</td>
                            <td className="px-6 py-4 text-center text-[color:var(--app-muted)]">{cs.quantity}</td>
                            <td className="px-6 py-4 text-[color:var(--app-muted)]">{cs.startDate || '---'}</td>
                            <td className="px-6 py-4 text-right">
                              {canCancel ? (
                                <button
                                  className="text-rose-600 hover:text-rose-800 text-xs font-bold border border-rose-200 hover:bg-rose-50 px-3 py-1.5 rounded transition-all"
                                  onClick={() => cancelService(cs.id)}
                                >
                                  Hủy đăng ký
                                </button>
                              ) : (
                                <span className="text-xs font-bold text-[color:var(--app-muted-2)] bg-[color:var(--app-bg)] border border-[color:var(--app-border)] px-2 py-1 rounded">Cố định</span>
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
            <div className="bg-[color:var(--app-surface-solid)] rounded-2xl shadow-sm border border-[color:var(--app-border)] p-6 relative overflow-hidden">
               <div className="absolute top-0 right-0 w-20 h-20 bg-[color:var(--app-primary-soft)] rounded-bl-full -mr-10 -mt-10 z-0"></div>
               <div className="relative z-10">
                 <h3 className="font-bold text-lg text-[color:var(--app-text)] mb-1">Dọn vệ sinh phòng</h3>
                 <p className="text-sm text-[color:var(--app-muted)] mb-4">Lịch định kỳ: <span className="font-semibold text-[color:var(--app-primary)]">Thứ 5 hàng tuần</span> (08:00 - 11:00)</p>
                 
                 <button
                   onClick={bookCleaning}
                   disabled={loading || !contractId}
                   className="w-full bg-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-hover)] disabled:opacity-50 disabled:cursor-not-allowed text-white py-3 rounded-xl font-bold text-sm shadow-lg transition-all active:scale-95"
                 >
                   Đăng ký cho tuần tới
                 </button>
               </div>
            </div>

            <div className="bg-[color:var(--app-surface-solid)] rounded-2xl shadow-sm border border-[color:var(--app-border)] overflow-hidden">
               <div className="px-6 py-4 border-b border-[color:var(--app-border)] bg-[color:var(--app-bg)]">
                 <h4 className="font-bold text-sm text-[color:var(--app-text)] uppercase tracking-wide">Lịch sử đặt dọn</h4>
               </div>
               <div className="max-h-[400px] overflow-y-auto custom-scrollbar">
                  <table className="w-full text-sm text-left">
                    <tbody className="divide-y divide-[color:var(--app-border)]">
                      {bookings.length === 0 ? (
                        <tr>
                          <td className="px-6 py-8 text-center text-[color:var(--app-muted-2)] text-xs">
                            Chưa có lịch sử đặt dịch vụ
                          </td>
                        </tr>
                      ) : (
                        bookings
                          .filter((b) => (b.serviceName || '').toLowerCase().includes('vệ sinh'))
                          .sort((a, b) => String(b.bookingDate).localeCompare(String(a.bookingDate)))
                          .map((b) => (
                            <tr key={b.id} className="hover:bg-[color:var(--app-bg)]">
                              <td className="px-6 py-3">
                                <div className="font-medium text-[color:var(--app-text)]">{b.bookingDate}</div>
                                <div className="text-xs text-[color:var(--app-muted)] mt-0.5">{b.startTime} - {b.endTime}</div>
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
           <div className="fixed inset-0 bg-black/25 backdrop-blur-sm z-50 flex items-center justify-center">
             <div className="bg-[color:var(--app-surface-solid)] px-6 py-4 rounded-xl shadow-2xl border border-[color:var(--app-border)] flex flex-col items-center">
               <div className="w-8 h-8 border-4 border-[color:var(--app-primary)] border-t-transparent rounded-full animate-spin mb-3"></div>
               <span className="font-bold text-[color:var(--app-text)] text-sm">Đang xử lý...</span>
             </div>
          </div>
        )}

      </div>
    </div>
  );
}
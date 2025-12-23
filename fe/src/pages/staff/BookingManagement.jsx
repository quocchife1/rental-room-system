import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import reservationApi from '../../api/reservationApi';

// Component hiển thị trạng thái đẹp mắt
const StatusBadge = ({ status }) => {
  const styles = {
    PENDING_CONFIRMATION: { bg: 'bg-yellow-50', text: 'text-yellow-700', label: 'Chờ xác nhận', border: 'border-yellow-200' },
    RESERVED: { bg: 'bg-indigo-50', text: 'text-indigo-700', label: 'Đã giữ chỗ', border: 'border-indigo-200' },
    COMPLETED: { bg: 'bg-emerald-50', text: 'text-emerald-700', label: 'Hoàn tất', border: 'border-emerald-200' },
    NO_SHOW: { bg: 'bg-gray-100', text: 'text-gray-600', label: 'Không đến', border: 'border-gray-200' },
    CANCELLED: { bg: 'bg-red-50', text: 'text-red-600', label: 'Đã hủy', border: 'border-red-200' },
    ALL: { bg: 'bg-gray-100', text: 'text-gray-800', label: 'Tất cả', border: 'border-gray-200' }
  };

  const currentStyle = styles[status] || styles.ALL;

  return (
    <span className={`px-2.5 py-1 rounded-full text-xs font-semibold border ${currentStyle.bg} ${currentStyle.text} ${currentStyle.border} whitespace-nowrap`}>
      {currentStyle.label}
    </span>
  );
};

// Icons SVG components để không cần cài thư viện ngoài
const Icons = {
  Search: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>,
  Filter: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" /></svg>,
  Check: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" /></svg>,
  X: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" /></svg>,
  Contract: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>,
  User: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>,
  Calendar: () => <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>,
};

export default function BookingManagement() {
  const navigate = useNavigate();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const res = await reservationApi.getMyBranchReservations({
        page: 0,
        size: 50,
        sort: 'reservationDate,desc',
        status: statusFilter
      });
      const data = Array.isArray(res) ? res : (res?.content || []);
      setRequests(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error('Lỗi tải yêu cầu đặt', e);
      setRequests([]);
    } finally { setLoading(false); }
  };

  useEffect(() => { fetchRequests(); }, [statusFilter]);

  const runSearch = async () => {
    const q = (searchQuery || '').trim();
    if (!q) {
      fetchRequests();
      return;
    }

    setLoading(true);
    try {
      const res = await reservationApi.getMyBranchReservations({
        page: 0,
        size: 50,
        sort: 'reservationDate,desc',
        status: statusFilter,
        q
      });
      const data = Array.isArray(res) ? res : (res?.content || []);
      setRequests(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error('Lỗi tra cứu đặt lịch', e);
      setRequests([]);
    } finally {
      setLoading(false);
    }
  };

  const approve = async (id) => {
    try {
      await reservationApi.confirmReservation(id);
      setRequests(prev => prev.map(r => r.id === id ? { ...r, status: 'RESERVED' } : r));
    } catch (e) { alert('Không thể duyệt'); }
  };

  const cancel = async (id) => {
    if (!window.confirm('Bạn chắc chắn muốn hủy phiếu này?')) return;
    try {
      await reservationApi.cancelReservation(id);
      setRequests(prev => prev.map(r => r.id === id ? { ...r, status: 'CANCELLED' } : r));
    } catch (e) { alert('Không thể hủy'); }
  };

  const markCompleted = async (id) => {
    try {
      await reservationApi.markCompleted(id);
      setRequests(prev => prev.map(r => r.id === id ? { ...r, status: 'COMPLETED' } : r));
    } catch (e) { alert('Không thể cập nhật hoàn tất'); }
  };

  const markNoShow = async (id) => {
    try {
      await reservationApi.markNoShow(id);
      setRequests(prev => prev.map(r => r.id === id ? { ...r, status: 'NO_SHOW' } : r));
    } catch (e) { alert('Không thể cập nhật không đến'); }
  };

  const goCreateContract = (id) => {
    navigate(`/staff/contracts/create?reservationId=${id}`);
  };

  const formatSlot = (slot) => {
    if (slot === 'MORNING') return 'Sáng (08:00 - 11:00)';
    if (slot === 'AFTERNOON') return 'Chiều (13:30 - 16:00)';
    return slot || '-';
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 font-sans text-gray-800">
      <div className="container mx-auto px-4 md:px-6">
        
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-end justify-between mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Quản lý đặt lịch</h1>
            <p className="text-gray-500 mt-2 text-sm">Theo dõi và xử lý các yêu cầu xem phòng từ khách hàng.</p>
          </div>
        </div>

        {/* Filters & Search Card */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 mb-6 transition-all hover:shadow-md">
          <div className="flex flex-col lg:flex-row gap-4 items-center justify-between">
            {/* Filter Group */}
            <div className="flex items-center gap-3 w-full lg:w-auto bg-gray-50 p-1 rounded-lg border border-gray-200">
              <div className="pl-3 text-gray-400"><Icons.Filter/></div>
              <select 
                className="bg-transparent text-sm font-medium text-gray-700 py-2 pr-8 pl-2 focus:outline-none w-full cursor-pointer"
                value={statusFilter} 
                onChange={(e) => setStatusFilter(e.target.value)}
              >
                <option value="ALL">Tất cả trạng thái</option>
                <option value="PENDING_CONFIRMATION">Chờ xác nhận</option>
                <option value="RESERVED">Đã giữ chỗ</option>
                <option value="COMPLETED">Hoàn tất</option>
                <option value="NO_SHOW">Không đến</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
            </div>

            {/* Search Group */}
            <div className="flex w-full lg:w-auto gap-2">
              <div className="relative w-full lg:w-96">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                  <Icons.Search />
                </div>
                <input
                  className="block w-full pl-10 pr-3 py-2.5 border border-gray-200 rounded-lg text-sm bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-colors shadow-sm"
                  placeholder="Tìm theo tên, SĐT, email hoặc mã..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter') runSearch(); }}
                />
              </div>
              <button 
                className="px-6 py-2.5 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium shadow-sm transition-all active:scale-95" 
                onClick={runSearch}
              >
                Tìm kiếm
              </button>
            </div>
          </div>
        </div>

        {/* Data Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          {loading ? (
            <div className="p-12 text-center text-gray-500 flex flex-col items-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mb-4"></div>
              <span>Đang tải dữ liệu...</span>
            </div>
          ) : requests.length === 0 ? (
            <div className="p-16 text-center text-gray-400 flex flex-col items-center">
               <svg className="w-16 h-16 mb-4 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
               </svg>
               <span className="text-lg font-medium text-gray-500">Không tìm thấy dữ liệu</span>
               <p className="text-sm mt-1">Vui lòng thử lại với bộ lọc khác</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm text-left">
                <thead className="bg-gray-50 border-b border-gray-100 text-gray-500 uppercase text-xs tracking-wider font-semibold">
                  <tr>
                    <th className="px-6 py-4">Mã / Thông tin phòng</th>
                    <th className="px-6 py-4">Khách hàng</th>
                    <th className="px-6 py-4">Lịch hẹn</th>
                    <th className="px-6 py-4">Trạng thái</th>
                    <th className="px-6 py-4 text-right">Hành động</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {requests.map(r => (
                    <tr key={r.id} className="hover:bg-gray-50/50 transition-colors group">
                      <td className="px-6 py-4 align-top">
                        <div className="flex items-center gap-2">
                           <span className="font-mono text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded text-xs font-bold">
                             {r.reservationCode || `#${r.id}`}
                           </span>
                        </div>
                        <div className="mt-2 font-bold text-gray-800 text-base">
                          {r.roomNumber || r.roomCode || 'Chưa gán phòng'}
                        </div>
                      </td>
                      
                      <td className="px-6 py-4 align-top">
                        <div className="flex items-start gap-3">
                           <div className="mt-1 p-1.5 bg-gray-100 rounded-full text-gray-400">
                             <Icons.User />
                           </div>
                           <div>
                              <div className="font-semibold text-gray-900">{r.tenantName || 'Khách vãng lai'}</div>
                              <div className="text-gray-500 text-xs mt-0.5 font-mono">
                                {r.tenantPhoneNumber || '---'}
                              </div>
                              <div className="text-gray-400 text-xs mt-0.5 truncate max-w-[150px]" title={r.tenantEmail}>
                                {r.tenantEmail}
                              </div>
                           </div>
                        </div>
                      </td>

                      <td className="px-6 py-4 align-top">
                        <div className="flex items-center gap-2 text-gray-700">
                           <Icons.Calendar />
                           <span>{r.visitDate ? new Date(r.visitDate).toLocaleDateString('vi-VN') : '-'}</span>
                        </div>
                        <div className="mt-1 ml-6 text-gray-500 text-xs bg-gray-100 inline-block px-2 py-0.5 rounded">
                           {formatSlot(r.visitSlot)}
                        </div>
                      </td>

                      <td className="px-6 py-4 align-top">
                        <StatusBadge status={r.status} />
                      </td>

                      <td className="px-6 py-4 align-top text-right">
                        <div className="flex flex-col gap-2 items-end">
                          
                          {/* Trạng thái: CHỜ XÁC NHẬN */}
                          {r.status === 'PENDING_CONFIRMATION' && (
                            <>
                              <button 
                                onClick={() => approve(r.id)}
                                className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-medium hover:bg-emerald-700 shadow-sm w-fit"
                              >
                                <Icons.Check /> Xác nhận
                              </button>
                              <button 
                                onClick={() => cancel(r.id)}
                                className="flex items-center gap-1.5 px-3 py-1.5 text-red-600 bg-red-50 hover:bg-red-100 rounded-lg text-xs font-medium w-fit transition-colors"
                              >
                                <Icons.X /> Từ chối
                              </button>
                            </>
                          )}

                          {/* Trạng thái: ĐÃ ĐẶT (RESERVED) */}
                          {r.status === 'RESERVED' && (
                            <>
                              <button 
                                onClick={() => goCreateContract(r.id)}
                                className="flex items-center gap-1.5 px-4 py-1.5 bg-indigo-600 text-white rounded-lg text-xs font-bold hover:bg-indigo-700 shadow-sm w-fit transition-all"
                              >
                                <Icons.Contract /> Lập hợp đồng
                              </button>
                              
                              <div className="flex gap-2">
                                <button 
                                  onClick={() => markCompleted(r.id)}
                                  className="px-3 py-1.5 border border-emerald-200 text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-lg text-xs font-medium transition-colors"
                                  title="Đánh dấu đã xem phòng xong"
                                >
                                  Hoàn tất
                                </button>
                                <button 
                                  onClick={() => markNoShow(r.id)}
                                  className="px-3 py-1.5 border border-amber-200 text-amber-700 bg-amber-50 hover:bg-amber-100 rounded-lg text-xs font-medium transition-colors"
                                  title="Khách không đến"
                                >
                                  Không đến
                                </button>
                                <button 
                                  onClick={() => cancel(r.id)}
                                  className="px-3 py-1.5 border border-gray-200 text-gray-500 hover:text-red-600 hover:border-red-200 hover:bg-red-50 rounded-lg text-xs font-medium transition-colors"
                                  title="Hủy đặt lịch"
                                >
                                  Hủy
                                </button>
                              </div>
                            </>
                          )}

                          {/* Các trạng thái đã kết thúc: Chỉ hiển thị text hoặc nút ẩn */}
                          {['COMPLETED', 'CANCELLED', 'NO_SHOW'].includes(r.status) && (
                            <span className="text-xs text-gray-400 italic">Đã đóng</span>
                          )}

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
  );
}
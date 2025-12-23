import { useEffect, useMemo, useState } from 'react';
import managerCleaningApi from '../../api/managerCleaningApi';

const Icons = {
  Refresh: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" /></svg>
  ),
  Calendar: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
  ),
  Clock: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
  ),
  Home: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>
  ),
  User: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" /></svg>
  ),
  Trash: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
  ),
  Close: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
  ),
  Empty: ({ className }) => (
    <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" /></svg>
  )
};

function formatDate(dateStr) {
  if (!dateStr) return '-';
  try {
    return new Date(dateStr).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch {
    return String(dateStr);
  }
}

function formatTime(timeStr) {
  if (!timeStr) return '-';
  return String(timeStr).slice(0, 5);
}

function getStatusStyle(status) {
  const s = (status || '').toUpperCase();
  if (s === 'COMPLETED' || s === 'DONE') return 'bg-emerald-100 text-emerald-700 border-emerald-200';
  if (s === 'CANCELED' || s === 'CANCELLED') return 'bg-gray-100 text-gray-500 border-gray-200 line-through';
  if (s === 'CONFIRMED' || s === 'APPROVED') return 'bg-blue-100 text-blue-700 border-blue-200';
  return 'bg-amber-100 text-amber-700 border-amber-200';
}

function getStatusLabel(status) {
  const s = (status || '').toUpperCase();
  if (s === 'COMPLETED') return 'Hoàn thành';
  if (s === 'CANCELED' || s === 'CANCELLED') return 'Đã hủy';
  if (s === 'CONFIRMED') return 'Đã duyệt';
  if (s === 'PENDING') return 'Chờ duyệt';
  return status;
}

export default function ManagerCleaningBookingsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [cancelReason, setCancelReason] = useState('');
  const [cancelId, setCancelId] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const sortedItems = useMemo(() => {
    const arr = Array.isArray(items) ? [...items] : [];
    arr.sort((a, b) => {
      const ad = a?.bookingDate ? new Date(a.bookingDate).getTime() : 0;
      const bd = b?.bookingDate ? new Date(b.bookingDate).getTime() : 0;
      if (ad !== bd) return bd - ad; 
      const at = (a?.startTime ?? '').toString();
      const bt = (b?.startTime ?? '').toString();
      return at.localeCompare(bt);
    });
    return arr;
  }, [items]);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const resp = await managerCleaningApi.getMyBranchCleaningBookings();
      const data = resp?.data ?? resp;
      setItems(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Không tải được danh sách.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const beginCancel = (row) => {
    setCancelId(row?.id ?? null);
    setCancelReason('');
  };

  const submitCancel = async () => {
    if (!cancelId) return;
    const reason = cancelReason.trim();
    if (!reason) {
      alert('Vui lòng nhập lý do hủy.');
      return;
    }

    setSubmitting(true);
    try {
      await managerCleaningApi.cancelBooking(cancelId, reason);
      setCancelId(null);
      setCancelReason('');
      await load();
    } catch (e) {
      alert(e?.response?.data?.message || e?.message || 'Hủy lịch thất bại.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-6 font-sans text-slate-800">
      <div className="max-w-7xl mx-auto space-y-6">
        
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Quản lý Lịch vệ sinh</h1>
            <p className="text-slate-500 text-sm mt-1">Theo dõi và xử lý các yêu cầu dọn phòng tại chi nhánh.</p>
          </div>
          <button
            className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 text-slate-700 font-medium shadow-sm transition-all active:scale-95"
            onClick={load}
            disabled={loading}
          >
            <Icons.Refresh className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            {loading ? 'Đang cập nhật...' : 'Làm mới dữ liệu'}
          </button>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl flex items-center gap-3">
             <span className="font-bold">Lỗi:</span> {error}
          </div>
        )}

        <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-xs uppercase text-slate-500 font-bold tracking-wider">
                  <th className="px-6 py-4">Thời gian</th>
                  <th className="px-6 py-4">Thông tin phòng</th>
                  <th className="px-6 py-4">Người yêu cầu</th>
                  <th className="px-6 py-4 text-center">Trạng thái</th>
                  <th className="px-6 py-4 text-right">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {loading && items.length === 0 ? (
                   <tr>
                     <td colSpan="5" className="px-6 py-12 text-center text-slate-500">
                       <div className="flex justify-center mb-2"><div className="w-6 h-6 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin"></div></div>
                       Đang tải danh sách...
                     </td>
                   </tr>
                ) : sortedItems.length === 0 ? (
                  <tr>
                    <td colSpan="5" className="px-6 py-16 text-center text-slate-400">
                      <div className="flex justify-center mb-4 text-slate-300">
                        <Icons.Empty className="w-16 h-16" />
                      </div>
                      <p className="text-lg font-medium text-slate-600">Chưa có lịch vệ sinh nào</p>
                      <p className="text-sm">Hiện tại không có yêu cầu dọn phòng nào sắp tới.</p>
                    </td>
                  </tr>
                ) : (
                  sortedItems.map((row) => (
                    <tr key={row.id} className="hover:bg-slate-50 transition-colors group">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2 font-semibold text-slate-800">
                           <Icons.Calendar className="w-4 h-4 text-slate-400" />
                           {formatDate(row.bookingDate)}
                        </div>
                        <div className="flex items-center gap-2 text-sm text-slate-500 mt-1">
                           <Icons.Clock className="w-4 h-4 text-slate-400" />
                           {formatTime(row.startTime)} - {formatTime(row.endTime)}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2 font-bold text-indigo-900">
                          <Icons.Home className="w-4 h-4 text-indigo-400" />
                          {row.roomNumber || row.roomCode || 'N/A'}
                        </div>
                        <div className="text-xs text-slate-400 mt-1 pl-6">
                          HĐ: #{row.contractCode || row.contractId || '---'}
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2 text-slate-700">
                          <Icons.User className="w-4 h-4 text-slate-400" />
                          <span className="font-medium">{row.tenantName || row.tenantUsername || 'Khách vãng lai'}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold border ${getStatusStyle(row.status)}`}>
                          {getStatusLabel(row.status)}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button
                          className={`inline-flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-bold transition-all ${
                             row.status === 'COMPLETED' || row.status === 'CANCELED' 
                             ? 'bg-slate-100 text-slate-400 cursor-not-allowed' 
                             : 'bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200 hover:border-rose-300'
                          }`}
                          onClick={() => beginCancel(row)}
                          disabled={submitting || row.status === 'COMPLETED' || row.status === 'CANCELED'}
                        >
                          {row.status === 'CANCELED' ? 'Đã hủy' : row.status === 'COMPLETED' ? 'Hoàn tất' : (
                            <>
                              <Icons.Trash className="w-3.5 h-3.5" /> Hủy lịch
                            </>
                          )}
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {cancelId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm transition-opacity">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden transform transition-all scale-100">
            <div className="bg-white px-6 py-4 border-b border-slate-100 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-800">Xác nhận hủy lịch</h3>
              <button 
                onClick={() => { setCancelId(null); setCancelReason(''); }}
                className="text-slate-400 hover:text-slate-600 transition-colors"
              >
                <Icons.Close className="w-5 h-5" />
              </button>
            </div>
            
            <div className="p-6">
              <div className="bg-amber-50 border border-amber-100 rounded-lg p-3 mb-4 text-sm text-amber-800">
                Bạn đang thực hiện hủy lịch dọn vệ sinh <strong>#{cancelId}</strong>. Hành động này không thể hoàn tác.
              </div>

              <label className="block text-sm font-bold text-slate-700 mb-2">
                Lý do hủy <span className="text-rose-500">*</span>
              </label>
              <textarea
                className="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 text-slate-800 focus:ring-2 focus:ring-rose-500 focus:border-rose-500 transition-all outline-none resize-none"
                rows="3"
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                placeholder="Nhập lý do hủy lịch (VD: Khách báo bận, thay đổi nhân sự...)"
              />

              <div className="flex gap-3 mt-6">
                <button
                  className="flex-1 px-4 py-2.5 bg-white border border-slate-300 text-slate-700 rounded-xl font-bold hover:bg-slate-50 transition-colors"
                  onClick={() => { setCancelId(null); setCancelReason(''); }}
                  disabled={submitting}
                >
                  Đóng
                </button>
                <button
                  className="flex-1 px-4 py-2.5 bg-rose-600 text-white rounded-xl font-bold hover:bg-rose-700 shadow-lg shadow-rose-200 transition-all active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed flex justify-center items-center gap-2"
                  onClick={submitCancel}
                  disabled={submitting}
                >
                  {submitting ? 'Đang xử lý...' : 'Xác nhận Hủy'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
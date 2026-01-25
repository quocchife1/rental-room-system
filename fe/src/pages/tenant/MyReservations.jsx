import React, { useEffect, useState } from 'react';
import reservationApi from '../../api/reservationApi';
import { Link } from 'react-router-dom';

export default function MyReservations({ isGuestView = false }) {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchReservations = async () => {
    setLoading(true);
    try {
      // FIX: Đảm bảo Sort field đúng
      const response = await reservationApi.getMyReservations({ 
          page: 0, 
          size: 50, 
          sort: 'reservationDate,desc' 
      });
      
      // Xử lý response (Page object)
      const data = response.content || response.result?.content || response || [];
      
      if (Array.isArray(data)) {
          setReservations(data);
      } else if (data.content && Array.isArray(data.content)) { // Nếu response là Page object
          setReservations(data.content);
      } else {
          setReservations([]);
      }
    } catch (error) {
      console.error("Lỗi tải lịch sử:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReservations();
  }, []);

  const handleCancel = async (id) => {
    if (window.confirm('Bạn chắc chắn muốn hủy yêu cầu này?')) {
      try {
        await reservationApi.cancelReservation(id);
        alert('Đã hủy thành công');
        fetchReservations();
      } catch (error) {
        alert('Lỗi khi hủy: ' + (error.response?.data?.message || 'Lỗi server'));
      }
    }
  };

  const getStatusBadge = (status) => {
    const config = {
      'PENDING_CONFIRMATION': { color: 'bg-yellow-100 text-yellow-800', text: 'Chờ xác nhận' },
      'RESERVED': { color: 'bg-green-100 text-green-800', text: 'Đã giữ chỗ' },
      'CANCELLED': { color: 'bg-red-100 text-red-800', text: 'Đã hủy' },
      'COMPLETED': { color: 'bg-blue-100 text-blue-800', text: 'Hoàn tất' },
      'NO_SHOW': { color: 'bg-amber-100 text-amber-800', text: 'Không đến' }
    };
    const item = config[status] || { color: 'bg-[color:var(--app-bg)] text-[color:var(--app-text)]', text: status };
    return <span className={`px-3 py-1 rounded-full text-xs font-bold ${item.color}`}>{item.text}</span>;
  };

  if (loading) return <div className="p-10 text-center text-[color:var(--app-muted)]">Đang tải dữ liệu...</div>;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-end border-b border-[color:var(--app-border)] pb-4">
        <div>
            <h2 className="text-2xl font-bold text-[color:var(--app-text)]">
                {isGuestView ? "Lịch sử đặt phòng của bạn" : "Quản lý giữ chỗ"}
            </h2>
            <p className="text-[color:var(--app-muted)] text-sm mt-1">Danh sách các phòng bạn đã gửi yêu cầu.</p>
        </div>
        {isGuestView && (
            <Link to="/" className="text-[color:var(--app-primary)] hover:underline text-sm font-medium">← Quay lại trang chủ</Link>
        )}
      </div>

      {reservations.length === 0 ? (
        <div className="text-center py-16 bg-[color:var(--app-bg)] rounded-2xl border border-dashed border-[color:var(--app-border-strong)]">
            <span className="text-4xl block mb-4">📭</span>
          <p className="text-[color:var(--app-muted)] font-medium">Bạn chưa có yêu cầu đặt phòng nào.</p>
          <Link to="/" className="mt-4 inline-block bg-[color:var(--app-primary)] text-white px-6 py-2.5 rounded-xl font-bold hover:bg-[color:var(--app-primary-hover)] transition-all shadow-sm">
                Tìm phòng ngay
            </Link>
        </div>
      ) : (
        <div className="grid gap-4">
            {reservations.map((item) => (
            <div key={item.id} className="bg-[color:var(--app-surface-solid)] p-5 rounded-2xl shadow-sm border border-[color:var(--app-border)] hover:shadow-md transition-all flex flex-col md:flex-row gap-6">
                    {/* Icon */}
              <div className="w-full md:w-32 h-32 bg-[color:var(--app-primary-soft)] rounded-xl flex items-center justify-center flex-shrink-0 text-[color:var(--app-primary)] opacity-30">
                        <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"></path></svg>
                    </div>

                    <div className="flex-1 space-y-3">
                        <div className="flex justify-between items-start">
                            <div>
                          <h3 className="text-lg font-bold text-[color:var(--app-text)]">Phòng {item.roomNumber} <span className="text-[color:var(--app-muted-2)] font-normal text-sm">({item.roomCode})</span></h3>
                          <p className="text-xs text-[color:var(--app-muted)] mt-1">Mã đơn: <span className="font-mono text-[color:var(--app-text)]">{item.reservationCode}</span></p>
                            </div>
                            {getStatusBadge(item.status)}
                        </div>

                      <div className="grid grid-cols-2 gap-4 text-sm text-[color:var(--app-muted)] bg-[color:var(--app-bg)] p-3 rounded-lg border border-[color:var(--app-border)]">
                            <div>
                          <p className="text-[color:var(--app-muted-2)] text-xs uppercase font-bold">Ngày gửi</p>
                                <p className="font-medium">{item.reservationDate ? new Date(item.reservationDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                            </div>
                            <div>
                        <p className="text-[color:var(--app-muted-2)] text-xs uppercase font-bold">Ngày tham khảo</p>
                            <p className="font-medium">{item.visitDate ? new Date(item.visitDate).toLocaleDateString('vi-VN') : 'N/A'}</p>
                            </div>
                          <div>
                        <p className="text-[color:var(--app-muted-2)] text-xs uppercase font-bold">Khung giờ</p>
                            <p className="font-medium">
                              {item.visitSlot === 'MORNING' ? 'Sáng (08:00 - 11:00)' : item.visitSlot === 'AFTERNOON' ? 'Chiều (13:30 - 16:00)' : (item.visitSlot || 'N/A')}
                            </p>
                          </div>
                        </div>
                        
                        {item.notes && (
                        <p className="text-sm text-[color:var(--app-muted)] italic">" {item.notes} "</p>
                        )}
                    </div>

                    <div className="flex flex-col justify-between items-end min-w-[140px]">
                      <Link to={`/rooms/${item.roomId}`} className="text-[color:var(--app-primary)] hover:text-[color:var(--app-primary-hover)] text-sm font-bold mb-2">
                            Xem chi tiết phòng
                        </Link>
                        
                        {item.status === 'PENDING_CONFIRMATION' && (
                            <button 
                                onClick={() => handleCancel(item.id)}
                            className="px-4 py-2 bg-[color:var(--app-surface-solid)] border border-red-200 text-red-600 rounded-lg text-sm font-bold hover:bg-red-50 transition-colors w-full md:w-auto"
                            >
                                Hủy yêu cầu
                            </button>
                        )}
                    </div>
                </div>
            ))}
        </div>
      )}
    </div>
  );
}
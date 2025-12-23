import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import roomApi from '../api/roomApi';
import reservationApi from '../api/reservationApi';
import MainLayout from '../components/MainLayout';
import resolveImageUrl from '../utils/resolveImageUrl';

export default function RoomDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);

  const [room, setRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedImage, setSelectedImage] = useState('');
  
  // Modal Booking State
  const [showBookingModal, setShowBookingModal] = useState(false);
  const [bookingLoading, setBookingLoading] = useState(false);

  const computeAllowedVisitDates = () => {
    const today = new Date();
    const toISODate = (d) => d.toISOString().split('T')[0];

    const unique = new Map();
    for (let offset = 1; offset <= 3; offset++) {
      const d = new Date(today);
      d.setDate(d.getDate() + offset);
      const day = d.getDay(); // 0=Sun,6=Sat
      if (day === 6) d.setDate(d.getDate() + 2);
      if (day === 0) d.setDate(d.getDate() + 1);
      unique.set(toISODate(d), toISODate(d));
    }

    return Array.from(unique.values()).sort();
  };
  
  // Form Data
  const [bookingData, setBookingData] = useState({
    visitDate: '',
    visitSlot: 'MORNING',
    notes: ''
  });

  useEffect(() => {
    const fetchRoom = async () => {
      try {
        const data = await roomApi.getById(id);
        setRoom(data);
        if (data.images && data.images.length > 0) {
          setSelectedImage(resolveImageUrl(data.images[0].imageUrl));
        }
      } catch (error) {
        console.error("Lỗi tải thông tin phòng:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchRoom();
  }, [id]);

  // Handle click "Đặt phòng"
  const handleBookClick = () => {
    if (!user) {
      if (window.confirm("Bạn cần đăng nhập để đặt giữ chỗ. Chuyển đến trang đăng nhập?")) {
        navigate('/login');
      }
      return;
    }
    const allowed = computeAllowedVisitDates();
    setBookingData(prev => ({ ...prev, visitDate: allowed[0] || '', visitSlot: 'MORNING' }));
    setShowBookingModal(true);
  };

  // Submit Booking
  const handleConfirmBooking = async () => {
    if (!bookingData.visitDate) {
      alert('Vui lòng chọn ngày đến tham khảo!');
      return;
    }

    setBookingLoading(true);
    try {
      const payload = {
        roomId: room.id,
        visitDate: bookingData.visitDate,
        visitSlot: bookingData.visitSlot,
        notes: bookingData.notes
      };

      await reservationApi.createReservation(payload);
      
      alert("🎉 Đặt lịch tham khảo thành công! Bạn có thể xem trạng thái trong trang cá nhân.");
      setShowBookingModal(false);
      
      // Điều hướng thông minh: Nếu là Guest -> Tenant Dashboard (nếu đã có quyền) hoặc trang Profile
      navigate('/tenant/reservations'); 
      
    } catch (error) {
      console.error(error);
      const msg = error.response?.data?.message || "Có lỗi xảy ra khi đặt lịch.";
      alert("❌ " + msg);
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) return (
    <MainLayout>
       <div className="min-h-[60vh] flex items-center justify-center">
         <div className="text-gray-400 font-medium animate-pulse">Đang tải dữ liệu...</div>
       </div>
    </MainLayout>
  );
  
  if (!room) return (
    <MainLayout>
       <div className="min-h-[60vh] flex items-center justify-center">
         <div className="text-gray-900 font-bold text-xl">Không tìm thấy thông tin phòng!</div>
       </div>
    </MainLayout>
  );

  return (
    <MainLayout>
      <div className="container mx-auto px-6 py-10 relative">
        {/* Breadcrumb */}
        <div className="flex items-center text-sm font-medium text-gray-400 mb-8">
            <Link to="/" className="hover:text-indigo-600 transition-colors">Trang chủ</Link>
            <span className="mx-3">/</span>
            <span className="text-gray-900">Chi tiết phòng {room.roomNumber}</span>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-12">
          {/* Cột trái: Hình ảnh & Mô tả */}
          <div className="lg:col-span-8 space-y-6">
            <div className="bg-gray-100 rounded-3xl overflow-hidden h-[500px] shadow-sm relative group">
              <img src={selectedImage || 'https://placehold.co/1200x800?text=No+Image'} alt="Main View" className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"/>
            </div>
            {room.images && room.images.length > 0 && (
              <div className="flex gap-4 overflow-x-auto pb-2 scrollbar-hide">
                {room.images.map((img) => {
                  const fullUrl = resolveImageUrl(img.imageUrl);
                  return (
                    <button key={img.id} onClick={() => setSelectedImage(fullUrl)} className={`relative w-24 h-24 rounded-2xl overflow-hidden flex-shrink-0 transition-all duration-200 border-2 ${selectedImage === fullUrl ? 'border-indigo-600 opacity-100 scale-105' : 'border-transparent opacity-70 hover:opacity-100'}`}>
                      <img src={fullUrl} alt="Thumbnail" className="w-full h-full object-cover" onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/120?text=Error'; }} />
                    </button>
                  );
                })}
              </div>
            )}
            <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-100">
              <h3 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2"><span>📝</span> Mô tả chi tiết</h3>
              <div className="prose max-w-none text-gray-600 leading-relaxed whitespace-pre-line">{room.description || 'Chưa có mô tả chi tiết.'}</div>
            </div>
          </div>

          {/* Cột phải: Thông tin & Nút đặt */}
          <div className="lg:col-span-4">
            <div className="sticky top-24 bg-white rounded-3xl p-8 shadow-xl shadow-indigo-100/50 border border-gray-100">
                <div className="flex justify-between items-start mb-2">
                  <p className="text-sm font-bold text-indigo-500 uppercase tracking-wider bg-indigo-50 px-2 py-1 rounded-md">{room.branchCode}</p>
                  <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide flex items-center gap-1 ${room.status === 'AVAILABLE' ? 'bg-emerald-100 text-emerald-700' : 'bg-gray-100 text-gray-500'}`}>
                    {room.status === 'AVAILABLE' ? (<><span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span> Còn trống</>) : 'Đã thuê'}
                  </span>
                </div>
                <h1 className="text-3xl font-extrabold text-gray-900 mb-6 mt-4">Phòng {room.roomNumber}</h1>
                <div className="flex items-baseline gap-1 mb-8 pb-8 border-b border-gray-100">
                  <span className="text-4xl font-extrabold text-indigo-600">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(room.price)}</span>
                  <span className="text-gray-400 font-medium">/ tháng</span>
                </div>
                <div className="space-y-4 mb-8">
                   <div className="flex justify-between items-center p-3 bg-gray-50 rounded-xl"><span className="text-gray-500 text-sm">Diện tích</span><span className="font-bold text-gray-900">{room.area} m²</span></div>
                   <div className="flex justify-between items-center p-3 bg-gray-50 rounded-xl"><span className="text-gray-500 text-sm">Loại phòng</span><span className="font-bold text-gray-900">Standard</span></div>
                   <div className="flex justify-between items-center p-3 bg-gray-50 rounded-xl"><span className="text-gray-500 text-sm">Đặt cọc</span><span className="font-bold text-gray-900">1 tháng</span></div>
                </div>
                
                <button 
                  disabled={room.status !== 'AVAILABLE'}
                  className={`w-full py-4 rounded-2xl font-bold text-lg transition-all duration-200 shadow-lg transform active:scale-[0.98] ${room.status === 'AVAILABLE' ? 'bg-indigo-600 text-white hover:bg-indigo-700 shadow-indigo-200' : 'bg-gray-100 text-gray-400 cursor-not-allowed'}`}
                  onClick={handleBookClick}
                >
                  {room.status === 'AVAILABLE' ? 'Đặt Lịch Xem Phòng 📅' : 'Đã Được Thuê 🔒'}
                </button>
            </div>
          </div>
        </div>

        {/* --- MODAL XÁC NHẬN ĐẶT PHÒNG --- */}
        {showBookingModal && (
            <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
                <div className="bg-white rounded-2xl w-full max-w-md p-6 shadow-2xl animate-fade-in-up">
              <h3 className="text-xl font-bold text-gray-900 mb-4 border-b pb-2">Xác nhận lịch tham khảo</h3>
                    
                    <div className="space-y-4 mb-6 text-sm text-gray-600">
                        <div className="bg-gray-50 p-3 rounded-lg border border-gray-100">
                            <div className="flex justify-between mb-1"><span>Phòng:</span><span className="font-bold text-gray-900">{room.roomNumber} ({room.branchCode})</span></div>
                            <div className="flex justify-between"><span>Giá thuê:</span><span className="font-medium text-indigo-600">{room.price.toLocaleString()} đ/tháng</span></div>
                        </div>

                        {/* Ngày đến tham khảo */}
                        <div>
                            <label className="block text-gray-700 font-medium mb-1">Ngày đến tham khảo (*)</label>
                            <select
                              required
                              className="w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-indigo-500 outline-none"
                              value={bookingData.visitDate}
                              onChange={(e) => setBookingData({ ...bookingData, visitDate: e.target.value })}
                            >
                              {computeAllowedVisitDates().map((d) => (
                                <option key={d} value={d}>
                                  {new Date(d).toLocaleDateString('vi-VN')}
                                </option>
                              ))}
                            </select>
                            <p className="text-xs text-gray-500 mt-1">Chỉ cho phép đặt lịch trong 1–3 ngày tới (nếu rơi T7/CN sẽ tự dời sang T2).</p>
                        </div>

                        {/* Khung giờ */}
                        <div>
                            <label className="block text-gray-700 font-medium mb-1">Giờ muốn đến (*)</label>
                            <select
                              className="w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-indigo-500 outline-none"
                              value={bookingData.visitSlot}
                              onChange={(e) => setBookingData({ ...bookingData, visitSlot: e.target.value })}
                            >
                              <option value="MORNING">Sáng (08:00 - 11:00)</option>
                              <option value="AFTERNOON">Chiều (13:30 - 16:00)</option>
                            </select>
                        </div>

                        {/* Ghi chú */}
                        <div>
                            <label className="block text-gray-700 font-medium mb-1">Ghi chú (Tùy chọn)</label>
                            <textarea rows="2" className="w-full border border-gray-300 rounded-lg p-2 focus:ring-2 focus:ring-indigo-500 outline-none" placeholder="Ví dụ: Tôi muốn dọn vào cuối tuần..." value={bookingData.notes} onChange={(e) => setBookingData({...bookingData, notes: e.target.value})}></textarea>
                        </div>
                    </div>

                    <div className="flex gap-3">
                        <button onClick={() => setShowBookingModal(false)} className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 font-bold rounded-xl hover:bg-gray-200 transition-colors">Hủy</button>
                        <button onClick={handleConfirmBooking} disabled={bookingLoading} className="flex-1 px-4 py-2.5 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200 disabled:opacity-50 flex justify-center items-center">
                            {bookingLoading ? <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></span> : "Gửi yêu cầu"}
                        </button>
                    </div>
                </div>
            </div>
        )}
      </div>
    </MainLayout>
  );
}
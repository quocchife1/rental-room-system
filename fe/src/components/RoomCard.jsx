import React from 'react';
import { Link } from 'react-router-dom';
import resolveImageUrl from '../utils/resolveImageUrl';

const formatCurrency = (value) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

export default function RoomCard({ room }) {
  const thumbnail = room.images && room.images.length > 0 
    ? resolveImageUrl(room.images[0].imageUrl)
    : 'https://placehold.co/600x400?text=No+Image';

  // Màu sắc trạng thái nhẹ nhàng hơn (Pastel)
  const statusConfig = {
    AVAILABLE: { bg: 'bg-emerald-50', text: 'text-emerald-600', label: 'Còn trống' },
    RENTED: { bg: 'bg-rose-50', text: 'text-rose-600', label: 'Đã thuê' },
    MAINTENANCE: { bg: 'bg-amber-50', text: 'text-amber-600', label: 'Bảo trì' }
  };

  const status = statusConfig[room.status] || { bg: 'bg-gray-50', text: 'text-gray-600', label: room.status };

  return (
    // Thẻ Link bao bọc toàn bộ card
    <Link 
      to={`/rooms/${room.id}`} 
      className="group block bg-white rounded-3xl overflow-hidden shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 border border-gray-100"
    >
      {/* Image Container */}
      <div className="relative h-60 overflow-hidden">
        <img 
          src={thumbnail} 
          alt={room.roomCode} 
          className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
          onError={(e) => { e.target.onerror = null; e.target.src = 'https://placehold.co/600x400?text=Error'; }}
        />
        {/* Badge trạng thái */}
        <div className="absolute top-4 left-4">
          <span className={`px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wide shadow-sm ${status.bg} ${status.text}`}>
            {status.label}
          </span>
        </div>
        
        {/* Overlay gradient khi hover */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
      </div>
      
      {/* Content */}
      <div className="p-6">
        <div className="flex justify-between items-start mb-2">
          <div>
            <p className="text-xs font-bold text-indigo-500 uppercase tracking-wider mb-1">
              {room.branchCode}
            </p>
            <h3 className="text-xl font-bold text-gray-900 group-hover:text-indigo-600 transition-colors">
              Phòng {room.roomNumber}
            </h3>
          </div>
          <div className="text-right">
            <span className="block text-lg font-extrabold text-gray-900">
              {formatCurrency(room.price)}
            </span>
            <span className="text-xs text-gray-400">/ tháng</span>
          </div>
        </div>
        
        <div className="h-px bg-gray-100 my-4"></div>

        <div className="flex items-center justify-between text-sm text-gray-500">
          <div className="flex items-center gap-2">
            <span>📐</span>
            <span>{room.area} m²</span>
          </div>
          <div className="flex items-center gap-2">
            <span>🛏️</span>
            <span>Standard</span>
          </div>
          <div className="group-hover:translate-x-1 transition-transform duration-300 text-indigo-600 font-medium text-xs uppercase tracking-wide">
            Xem ngay &rarr;
          </div>
        </div>
      </div>
    </Link>
  );
}
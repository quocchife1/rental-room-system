import React, { useEffect, useState, useMemo } from 'react';
import { 
  RefreshCcw, 
  Search, 
  CheckCircle, 
  Loader2, 
  FileText,
  DollarSign,
  Eye,
  MapPin,
  Calendar,
  X,
  Maximize2
} from 'lucide-react';
import maintenanceApi from '../../../api/maintenanceApi';

// --- Constants & Helpers ---
const TABS = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'PENDING', label: 'Chờ xử lý' },
  { key: 'IN_PROGRESS', label: 'Đang xử lý' },
  { key: 'COMPLETED', label: 'Hoàn tất' },
];

const formatCurrency = (amount) => {
  if (!amount && amount !== 0) return '-';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
};

const formatDateTime = (v) => {
  if (!v) return null;
  try {
    return new Date(v).toLocaleString('vi-VN');
  } catch {
    return String(v);
  }
};

// --- Component Lightbox (Xem ảnh phóng to) ---
const ImageLightbox = ({ src, onClose }) => {
  if (!src) return null;
  return (
    <div 
      className="fixed inset-0 z-[10002] bg-black/90 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in duration-200"
      onClick={onClose} // Bấm ra ngoài để đóng
    >
      {/* Nút đóng */}
      <button 
        onClick={onClose} 
        className="absolute top-4 right-4 p-2 bg-white/10 hover:bg-white/20 text-white rounded-full transition"
      >
        <X className="w-6 h-6" />
      </button>
      
      {/* Ảnh phóng to */}
      <img 
        src={src} 
        alt="Enlarged" 
        className="max-w-full max-h-[90vh] object-contain rounded shadow-2xl animate-in zoom-in-95 duration-200"
        onClick={(e) => e.stopPropagation()} // Ngăn việc bấm vào ảnh thì bị đóng
      />
    </div>
  );
};

// --- Modal Chi Tiết Yêu Cầu ---
const DetailModal = ({ isOpen, onClose, item }) => {
  const [previewImage, setPreviewImage] = useState(null);

  if (!isOpen || !item) return null;

  return (
    <>
      {/* Overlay Modal chính: z-index rất cao (9999) để đè lên Header */}
      <div className="fixed inset-0 z-[10001] flex items-start justify-center bg-black/60 backdrop-blur-sm pt-24 pb-6 px-4">
        
        {/* Container Modal */}
        <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[calc(100vh-7rem)] flex flex-col animate-in fade-in zoom-in duration-200 relative">
          
          {/* Header Modal */}
          <div className="flex justify-between items-start p-5 border-b shrink-0 gap-4">
            <div className="min-w-0">
              <h3 className="text-xl font-bold text-gray-800">Chi tiết yêu cầu</h3>
              <div className="text-sm text-gray-500 mt-1 flex flex-wrap items-center gap-x-4 gap-y-1">
                <span>Mã: <span className="font-mono">#{item.requestCode || item.id}</span></span>
                {item.createdAt ? <span>Tạo: {formatDateTime(item.createdAt)}</span> : null}
                {item.updatedAt ? <span>Cập nhật: {formatDateTime(item.updatedAt)}</span> : null}
              </div>
            </div>
            <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full text-gray-500 transition shrink-0">
              <X className="w-5 h-5"/>
            </button>
          </div>
          
          {/* Body Modal (Có cuộn) */}
          <div className="p-6 overflow-y-auto custom-scrollbar">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-5">
              <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Người thuê</label>
                <div className="font-semibold text-gray-800 text-base mt-1 truncate">{item.tenantName || item.tenant?.fullName || 'N/A'}</div>
              </div>

              <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Vị trí</label>
                <div className="font-medium text-gray-800 flex items-center gap-2 mt-1">
                  <MapPin className="w-4 h-4 text-blue-600"/>
                  <span className="truncate">{item.branchCode ? `${item.branchCode} - ` : ''}Phòng {item.roomNumber || 'N/A'}</span>
                </div>
                {item.branchName ? <div className="text-xs text-gray-500 mt-1 truncate">{item.branchName}</div> : null}
              </div>

              <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Trạng thái</label>
                <div className="mt-2 flex items-center justify-between gap-3">
                  <span className={`px-3 py-1.5 rounded-full text-xs font-bold border ${
                    item.status === 'COMPLETED' ? 'bg-green-50 text-green-700 border-green-200' :
                    item.status === 'IN_PROGRESS' ? 'bg-blue-50 text-blue-700 border-blue-200' :
                    'bg-yellow-50 text-yellow-700 border-yellow-200'
                  }`}>
                    {item.status === 'COMPLETED' ? 'Hoàn tất' : item.status === 'IN_PROGRESS' ? 'Đang xử lý' : 'Chờ xử lý'}
                  </span>
                  <div className="text-sm font-semibold text-gray-800">{formatCurrency(item.cost)}</div>
                </div>
                {item.invoiceId ? (
                  <div className="text-xs text-green-700 mt-2 flex items-center gap-1">
                    <CheckCircle className="w-3.5 h-3.5" /> Hóa đơn: #{item.invoiceId}
                  </div>
                ) : null}
              </div>
            </div>

            {/* Phần Mô tả */}
            <div className="mb-6">
              <label className="text-xs font-bold text-gray-400 uppercase tracking-wider block mb-2">Mô tả sự cố</label>
              <div className="bg-gray-50 p-4 rounded-lg text-gray-700 border border-gray-200 text-sm leading-relaxed">
                {item.description || 'Không có mô tả chi tiết.'}
              </div>
            </div>

            {/* Phần Hình ảnh (Grid) */}
            <div>
               <label className="text-xs font-bold text-gray-400 uppercase tracking-wider block mb-2">
                 Hình ảnh đính kèm ({Array.isArray(item.images) ? item.images.length : 0})
               </label>
               {Array.isArray(item.images) && item.images.length > 0 ? (
                 <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
                   {item.images.map((url, idx) => (
                     <div 
                        key={idx} 
                        className="group relative aspect-square rounded-lg overflow-hidden border border-gray-200 cursor-zoom-in shadow-sm hover:shadow-md transition"
                        onClick={() => setPreviewImage(url)} // Bấm vào để xem to
                     >
                        <img 
                          src={url} 
                          alt={`Evidence ${idx}`} 
                          className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                        />
                        {/* Overlay icon hover */}
                        <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors flex items-center justify-center">
                          <Maximize2 className="w-6 h-6 text-white opacity-0 group-hover:opacity-100 transition-opacity drop-shadow-md" />
                        </div>
                     </div>
                   ))}
                 </div>
               ) : (
                 <div className="text-gray-400 italic text-sm py-4 text-center bg-gray-50 rounded-lg border border-dashed">
                   Không có hình ảnh đính kèm.
                 </div>
               )}
            </div>
          </div>
          
          {/* Footer Modal */}
          <div className="p-4 border-t bg-gray-50 flex justify-end shrink-0 rounded-b-xl">
            <button 
              onClick={onClose} 
              className="px-6 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-100 hover:text-gray-900 transition font-medium shadow-sm"
            >
              Đóng lại
            </button>
          </div>
        </div>
      </div>

      {/* Render Lightbox nếu đang chọn ảnh */}
      <ImageLightbox src={previewImage} onClose={() => setPreviewImage(null)} />
    </>
  );
}

// --- Modal Tạo Hóa Đơn ---
const InvoiceModal = ({ isOpen, onClose, onSubmit, defaultAmount, item }) => {
  const [amount, setAmount] = useState(defaultAmount || '');
  const [note, setNote] = useState('');

  useEffect(() => {
    if (isOpen && item) {
      setAmount(item.cost || '');
      setNote(`Phí bảo trì (${item.requestCode || item.id})`);
    }
  }, [isOpen, item]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[10001] flex items-center justify-center bg-black/60 backdrop-blur-sm animate-in fade-in duration-150 p-4">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-md p-6 transform transition-all">
        <h3 className="text-lg font-bold mb-5 flex items-center gap-2 text-gray-800 border-b pb-3">
          <DollarSign className="w-5 h-5 text-green-600" />
          Lập hóa đơn lỗi người thuê
        </h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Số tiền thu (VND)</label>
            <div className="relative">
              <input
                type="number"
                className="w-full border border-gray-300 rounded-lg pl-3 pr-10 py-2.5 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                max={9999999999}
                placeholder="Ví dụ: 200000"
              />
              <span className="absolute right-3 top-2.5 text-gray-400 text-sm font-medium">đ</span>
            </div>
          </div>
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Ghi chú hóa đơn</label>
            <textarea
              className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition resize-none"
              rows={3}
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Nhập nội dung chi tiết..."
            />
          </div>
        </div>
        <div className="flex justify-end gap-3 mt-8">
          <button onClick={onClose} className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg transition font-medium">Hủy bỏ</button>
          <button 
            onClick={() => onSubmit(amount, note)}
            className="px-5 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 shadow-lg shadow-blue-500/30 transition font-medium"
          >
            Xác nhận tạo
          </button>
        </div>
      </div>
    </div>
  );
};

// --- Main Page ---
export default function MaintenanceListPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentTab, setCurrentTab] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  
  // Modals state
  const [detailItem, setDetailItem] = useState(null);
  const [invoiceItem, setInvoiceItem] = useState(null);

  async function load() {
    setLoading(true);
    try {
      const res = await maintenanceApi.listAllForBoard();
      const arr = Array.isArray(res) ? res : res?.content ?? [];
      setItems(arr);
    } catch (e) {
      alert('Tải dữ liệu thất bại: ' + (e?.message || 'Lỗi không xác định'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  // Update Status Logic
  async function updateStatus(itemId, nextStatus) {
    try {
      // Optimistic update
      setItems(prev => prev.map(item => item.id === itemId ? { ...item, status: nextStatus } : item));
      await maintenanceApi.updateStatus(itemId, nextStatus);
      load(); 
    } catch (e) {
      alert('Cập nhật thất bại');
      load(); 
    }
  }

  // Create Invoice Logic
  const handleCreateInvoice = async (amountStr, note) => {
    if (!invoiceItem) return;
    const amount = Number(String(amountStr).replace(/[^0-9]/g, ''));
    if (!amount || amount <= 0) return alert('Số tiền không hợp lệ');
    if (amount > 9999999999) return alert('Số tiền vượt quá giới hạn (tối đa 9,999,999,999)');

    try {
      await maintenanceApi.createTenantFaultInvoice(invoiceItem.id, { amount, note });
      alert('Đã lập hóa đơn thành công!');
      setInvoiceItem(null);
      load();
    } catch (e) {
      alert('Lỗi: ' + (e?.response?.data?.message || e.message));
    }
  };

  // Filter & Search Logic
  const filteredItems = useMemo(() => {
    return items.filter(it => {
      const matchesTab = currentTab === 'ALL' || (it.status || 'PENDING') === currentTab;
      
      const searchLower = searchTerm.toLowerCase();
      const matchesSearch = !searchTerm || 
         (it.requestCode && it.requestCode.toLowerCase().includes(searchLower)) ||
         (it.tenantName && it.tenantName.toLowerCase().includes(searchLower)) ||
        (it.branchCode && it.branchCode.toLowerCase().includes(searchLower)) ||
         (it.roomNumber && it.roomNumber.toLowerCase().includes(searchLower)) ||
         (it.description && it.description.toLowerCase().includes(searchLower));

      return matchesTab && matchesSearch;
    });
  }, [items, currentTab, searchTerm]);

  return (
    <div className="min-h-screen bg-gray-50 p-6 font-sans text-gray-800">
      
      {/* Header & Title */}
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-6 gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản Lý Bảo Trì</h1>
          <p className="text-sm text-gray-500 mt-1">Danh sách yêu cầu sửa chữa từ cư dân</p>
        </div>
        <div className="flex gap-3">
          <button 
            onClick={load} 
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-lg hover:bg-gray-50 transition shadow-sm"
          >
            <RefreshCcw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            <span className="hidden md:inline">Làm mới</span>
          </button>
        </div>
      </div>

      {/* Controls: Stats & Filters */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 mb-6">
        {/* Tabs */}
        <div className="flex overflow-x-auto border-b border-gray-100 px-2 scrollbar-hide">
          {TABS.map(tab => {
            const isActive = currentTab === tab.key;
            const count = items.filter(i => tab.key === 'ALL' ? true : (i.status || 'PENDING') === tab.key).length;
            
            return (
              <button
                key={tab.key}
                onClick={() => setCurrentTab(tab.key)}
                className={`flex items-center gap-2 px-4 py-4 text-sm font-medium border-b-2 transition-colors whitespace-nowrap outline-none
                  ${isActive 
                    ? 'border-blue-600 text-blue-600 bg-blue-50/50' 
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50'
                  }`}
              >
                {tab.label}
                <span className={`text-xs px-2 py-0.5 rounded-full ${isActive ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-600'}`}>
                  {count}
                </span>
              </button>
            )
          })}
        </div>
        
        {/* Search Bar */}
        <div className="p-4 flex items-center gap-3">
          <div className="relative flex-1 max-w-md">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input 
              type="text"
              placeholder="Tìm kiếm theo phòng, tên cư dân, mã yêu cầu..."
              className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
          </div>
          <div className="text-sm text-gray-500 ml-auto hidden md:block">
            Hiển thị {filteredItems.length} kết quả
          </div>
        </div>
      </div>

      {/* Data Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/80 text-xs font-semibold text-gray-500 uppercase tracking-wider border-b border-gray-200">
                <th className="px-6 py-4">Mã / Ngày</th>
                <th className="px-6 py-4">Phòng & Cư dân</th>
                <th className="px-6 py-4 w-1/3">Mô tả sự cố</th>
                <th className="px-6 py-4">Chi phí</th>
                <th className="px-6 py-4">Trạng thái</th>
                <th className="px-6 py-4 text-right">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {loading && filteredItems.length === 0 ? (
                 <tr>
                   <td colSpan="6" className="px-6 py-12 text-center text-gray-400">
                      <div className="flex flex-col items-center gap-2">
                        <Loader2 className="w-8 h-8 animate-spin text-blue-500"/>
                        <span>Đang tải dữ liệu...</span>
                      </div>
                   </td>
                 </tr>
              ) : filteredItems.length === 0 ? (
                <tr>
                   <td colSpan="6" className="px-6 py-12 text-center text-gray-400 italic">
                      Không tìm thấy yêu cầu nào phù hợp.
                   </td>
                 </tr>
              ) : (
                filteredItems.map((it) => (
                  <tr key={it.id} className="hover:bg-gray-50/80 transition-colors group">
                    <td className="px-6 py-4 align-top">
                      <div className="flex flex-col">
                        <span className="text-sm font-bold text-gray-900 hover:text-blue-600 cursor-pointer" onClick={() => setDetailItem(it)}>
                          #{it.requestCode || it.id}
                        </span>
                        {it.createdAt && (
                          <span className="text-xs text-gray-400 mt-1 flex items-center gap-1">
                             <Calendar className="w-3 h-3"/> {formatDateTime(it.createdAt)}
                          </span>
                        )}
                      </div>
                    </td>
                    
                    <td className="px-6 py-4 align-top">
                      <div className="flex flex-col">
                         <div className="text-sm font-medium text-gray-900 flex items-center gap-1">
                           <MapPin className="w-3.5 h-3.5 text-gray-400"/> {it.branchCode ? `${it.branchCode} - ` : ''}{it.roomNumber || '---'}
                         </div>
                         <div className="text-sm text-gray-500 mt-0.5">{it.tenantName || it.tenant?.fullName || 'Khách vãng lai'}</div>
                      </div>
                    </td>

                    <td className="px-6 py-4 align-top">
                      <div className="text-sm text-gray-700 line-clamp-2" title={it.description}>
                        {it.description || 'Không có mô tả'}
                      </div>
                      {Array.isArray(it.images) && it.images.length > 0 && (
                        <div className="flex items-center gap-1 mt-2 text-xs text-blue-600 font-medium cursor-pointer hover:underline" onClick={() => setDetailItem(it)}>
                          <FileText className="w-3 h-3"/> {it.images.length} hình ảnh
                        </div>
                      )}
                    </td>

                    <td className="px-6 py-4 align-top">
                       <div className="text-sm font-medium text-gray-900">{formatCurrency(it.cost)}</div>
                       {it.invoiceId && (
                         <div className="text-[10px] text-green-600 font-bold bg-green-50 px-1.5 py-0.5 rounded-full inline-block mt-1 border border-green-100">
                           Đã xuất HĐ
                         </div>
                       )}
                    </td>

                    <td className="px-6 py-4 align-top">
                       <select 
                         className={`text-xs font-bold px-2 py-1.5 rounded border-0 ring-1 ring-inset cursor-pointer outline-none focus:ring-2 focus:ring-blue-500 transition shadow-sm
                           ${it.status === 'COMPLETED' ? 'bg-green-50 text-green-700 ring-green-600/20' : 
                             it.status === 'IN_PROGRESS' ? 'bg-blue-50 text-blue-700 ring-blue-600/20' : 
                             'bg-yellow-50 text-yellow-700 ring-yellow-600/20'}`}
                         value={it.status || 'PENDING'}
                         onChange={(e) => updateStatus(it.id, e.target.value)}
                       >
                         <option value="PENDING">Chờ xử lý</option>
                         <option value="IN_PROGRESS">Đang xử lý</option>
                         <option value="COMPLETED">Hoàn tất</option>
                       </select>
                    </td>

                    <td className="px-6 py-4 align-top text-right">
                       <div className="flex items-center justify-end gap-2">
                          <button 
                            onClick={() => setDetailItem(it)}
                            className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition"
                            title="Xem chi tiết"
                          >
                            <Eye className="w-4 h-4" />
                          </button>
                          
                          {it.status === 'COMPLETED' && !it.invoiceId && (
                            <button 
                              onClick={() => setInvoiceItem(it)}
                              className="p-2 text-orange-400 hover:text-orange-700 hover:bg-orange-50 rounded-lg transition"
                              title="Lập hóa đơn"
                            >
                              <DollarSign className="w-4 h-4" />
                            </button>
                          )}
                       </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Render Modals */}
      <DetailModal isOpen={!!detailItem} item={detailItem} onClose={() => setDetailItem(null)} />
      
      <InvoiceModal 
        isOpen={!!invoiceItem} 
        item={invoiceItem}
        onClose={() => setInvoiceItem(null)}
        onSubmit={handleCreateInvoice}
      />

    </div>
  );
}
import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import contractApi from '../../api/contractApi';
import maintenanceApi from '../../api/maintenanceApi';
import resolveImageUrl from '../../utils/resolveImageUrl';

export default function MaintenanceRequests() {
  const { user } = useSelector((state) => state.auth);
  const [requests, setRequests] = useState([]);
  const [isFormOpen, setIsFormOpen] = useState(false);

  const [activeContract, setActiveContract] = useState(null);
  
  // State form
  const [formData, setFormData] = useState({
    branchCode: '',
    roomNumber: '',
    description: ''
  });

  const normalizeContracts = (res) => {
    let data = [];
    if (Array.isArray(res)) data = res;
    else if (res && res.content) data = res.content;
    else if (res && res.data) data = res.data.result || res.data || [];
    else data = res || [];
    return Array.isArray(data) ? data : [];
  };

  const prefillFromContract = (contract) => {
    if (!contract) return;
    setFormData((prev) => ({
      ...prev,
      branchCode: contract.branchCode || prev.branchCode || '',
      roomNumber: contract.roomNumber || contract.roomCode || prev.roomNumber || '',
    }));
  };

  // Quản lý file ảnh
  const [selectedFiles, setSelectedFiles] = useState([]); // Mảng các file thực tế
  const [previewUrls, setPreviewUrls] = useState([]);     // Mảng URL để hiển thị preview
  
  const [zoomImage, setZoomImage] = useState(null); // State phóng to ảnh

  // --- 1. Load danh sách yêu cầu ---
  const fetchRequests = async () => {
    if (user?.id) {
      try {
        // Gọi API mới: /api/maintenance/my-requests (đã có trong Controller bạn gửi)
        // Hoặc dùng getRequestsByTenant nếu API kia chưa có trong file api js
        const res = await maintenanceApi.getRequestsByTenant(user.id);
        // axiosClient interceptor unwraps ApiResponseDto and returns the inner `data`.
        // Support both shapes: unwrapped array OR full axios response.
        let data = [];
        if (Array.isArray(res)) data = res;
        else if (res && Array.isArray(res.data)) data = res.data;
        else if (res && Array.isArray(res.result)) data = res.result;
        else if (res && res.content) data = res.content; // pagination shape
        else data = res || [];
        // Sắp xếp mới nhất lên đầu
        setRequests(Array.isArray(data) ? data.reverse() : []);
      } catch (error) {
        console.error("Lỗi tải dữ liệu:", error);
      }
    }
  };

  useEffect(() => {
    fetchRequests();
  }, [user]);

  useEffect(() => {
    (async () => {
      try {
        const res = await contractApi.getMyContracts();
        const list = normalizeContracts(res);
        const active = list.find((c) => c.status === 'ACTIVE') || list[0] || null;
        setActiveContract(active);
        prefillFromContract(active);
      } catch (e) {
        // Keep form usable even if contract lookup fails
        setActiveContract(null);
      }
    })();
  }, [user?.id]);

  useEffect(() => {
    if (isFormOpen) {
      prefillFromContract(activeContract);
    }
  }, [isFormOpen, activeContract]);

  // --- 2. Xử lý chọn ảnh (Cho phép chọn thêm, tối đa 5) ---
  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    
    if (selectedFiles.length + files.length > 5) {
        alert("Bạn chỉ được gửi tối đa 5 hình ảnh.");
        return;
    }

    // Gộp file mới vào danh sách cũ
    const newFiles = [...selectedFiles, ...files];
    setSelectedFiles(newFiles);

    // Tạo URL preview cho các file mới và gộp vào
    const newPreviews = files.map(file => URL.createObjectURL(file));
    setPreviewUrls([...previewUrls, ...newPreviews]);
    
    // Reset input để có thể chọn lại cùng 1 file nếu muốn
    e.target.value = null; 
  };

  // --- 3. Xóa ảnh khỏi danh sách chờ gửi ---
  const removeImage = (index) => {
    const newFiles = selectedFiles.filter((_, i) => i !== index);
    const newPreviews = previewUrls.filter((_, i) => i !== index);
    
    setSelectedFiles(newFiles);
    setPreviewUrls(newPreviews);
  };

  // --- 4. Gửi Form ---
  const handleSubmit = async (e) => {
    e.preventDefault();
    const data = new FormData();
    
    data.append('branchCode', formData.branchCode);
    data.append('roomNumber', formData.roomNumber);
    data.append('description', formData.description);
    
    // Append từng file vào FormData
    selectedFiles.forEach((file) => {
        data.append('images', file);
    });

    try {
      await maintenanceApi.createRequest(data);
      alert('✅ Gửi yêu cầu thành công! Ban quản lý sẽ sớm phản hồi.');
      
      // Reset form
      setIsFormOpen(false);
      setFormData({
        branchCode: activeContract?.branchCode || '',
        roomNumber: activeContract?.roomNumber || activeContract?.roomCode || '',
        description: '',
      });
      setSelectedFiles([]);
      setPreviewUrls([]);
      fetchRequests(); // Reload list
    } catch (err) {
      console.error(err);
      alert('❌ Gửi thất bại: ' + (err.response?.data?.message || 'Lỗi hệ thống'));
    }
  };

  // Helper: Xử lý URL ảnh từ Backend
  const getImageUrl = (path) => {
    if (!path) return 'https://placehold.co/100?text=NoImage';
    return resolveImageUrl(path);
  };
  const getStatusConfig = (status) => {
    switch (status) {
        case 'PENDING': return { bg: 'bg-yellow-100', text: 'text-yellow-800', label: '⏳ Đang chờ xử lý', border: 'border-yellow-200' };
        case 'IN_PROGRESS': return { bg: 'bg-blue-100', text: 'text-blue-800', label: '🛠️ Đang sửa chữa', border: 'border-blue-200' };
        case 'COMPLETED': return { bg: 'bg-green-100', text: 'text-green-800', label: '✅ Đã hoàn thành', border: 'border-green-200' };
        case 'REJECTED': return { bg: 'bg-red-100', text: 'text-red-800', label: '🚫 Bị từ chối', border: 'border-red-200' };
        default: return { bg: 'bg-[color:var(--app-bg)]', text: 'text-[color:var(--app-text)]', label: status, border: 'border-[color:var(--app-border)]' };
    }
  };

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      {/* Header & Toggle Button */}
      <div className="flex flex-col sm:flex-row justify-between items-center gap-4 bg-[color:var(--app-surface-solid)] p-6 rounded-2xl shadow-sm border border-[color:var(--app-border)]">
        <div>
            <h2 className="text-2xl font-bold text-[color:var(--app-text)]">🛠️ Yêu cầu bảo trì</h2>
            <p className="text-[color:var(--app-muted)] text-sm mt-1">Báo cáo các sự cố hỏng hóc để được hỗ trợ kịp thời.</p>
        </div>
        <button 
          onClick={() => setIsFormOpen(!isFormOpen)}
          className={`px-6 py-3 rounded-xl font-bold shadow-md transition-all flex items-center gap-2 transform active:scale-95
            ${isFormOpen ? 'bg-[color:var(--app-bg)] text-[color:var(--app-text)] hover:bg-[color:var(--app-border)] border border-[color:var(--app-border)]' : 'bg-[color:var(--app-primary)] text-white hover:bg-[color:var(--app-primary-hover)]'}`}
        >
          {isFormOpen ? '✖ Đóng biểu mẫu' : '＋ Tạo yêu cầu mới'}
        </button>
      </div>

      {/* Form Gửi Yêu Cầu */}
      {isFormOpen && (
        <div className="bg-[color:var(--app-surface-solid)] p-8 rounded-2xl border border-[color:var(--app-border)] shadow-xl animate-fade-in-down">
          <h3 className="font-bold text-[color:var(--app-text)] text-xl mb-6 flex items-center gap-2 border-b border-[color:var(--app-border)] pb-4">
            📝 Thông tin sự cố
          </h3>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-semibold text-[color:var(--app-text)] mb-2">Mã chi nhánh <span className="text-red-500">*</span></label>
                <input required className="w-full border border-[color:var(--app-border-strong)] bg-[color:var(--app-bg)] text-[color:var(--app-text)] rounded-xl p-3 shadow-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] outline-none transition-all" 
                    placeholder="VD: CN01" 
                    value={formData.branchCode} 
                    onChange={e => setFormData({...formData, branchCode: e.target.value})} 
                />
                {activeContract?.branchCode ? (
                  <div className="text-xs text-[color:var(--app-muted-2)] mt-1">Tự động lấy từ hợp đồng đang hiệu lực.</div>
                ) : null}
              </div>
              <div>
                <label className="block text-sm font-semibold text-[color:var(--app-text)] mb-2">Số phòng <span className="text-red-500">*</span></label>
                <input required className="w-full border border-[color:var(--app-border-strong)] bg-[color:var(--app-bg)] text-[color:var(--app-text)] rounded-xl p-3 shadow-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] outline-none transition-all" 
                    placeholder="VD: 101" 
                    value={formData.roomNumber} 
                    onChange={e => setFormData({...formData, roomNumber: e.target.value})} 
                />
                {activeContract?.roomNumber || activeContract?.roomCode ? (
                  <div className="text-xs text-[color:var(--app-muted-2)] mt-1">Tự động lấy từ hợp đồng đang hiệu lực.</div>
                ) : null}
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-[color:var(--app-text)] mb-2">Mô tả chi tiết <span className="text-red-500">*</span></label>
              <textarea required rows="4" className="w-full border border-[color:var(--app-border-strong)] bg-[color:var(--app-bg)] text-[color:var(--app-text)] rounded-xl p-3 shadow-sm focus:ring-2 focus:ring-[color:var(--app-primary-soft)] focus:border-[color:var(--app-primary)] outline-none transition-all" 
                placeholder="Mô tả chi tiết sự cố bạn đang gặp phải (vị trí, tình trạng...)" 
                value={formData.description} 
                onChange={e => setFormData({...formData, description: e.target.value})} 
              ></textarea>
            </div>
            
            {/* Upload Area */}
            <div>
              <label className="block text-sm font-semibold text-[color:var(--app-text)] mb-2">
                Hình ảnh đính kèm ({selectedFiles.length}/5)
              </label>
              
              <div className="flex flex-wrap gap-4">
                 {/* Nút chọn ảnh */}
                 {selectedFiles.length < 5 && (
                    <label className="w-24 h-24 flex flex-col items-center justify-center border-2 border-dashed border-[color:var(--app-border-strong)] bg-[color:var(--app-bg)] rounded-xl cursor-pointer hover:border-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-soft)] transition-colors">
                        <span className="text-2xl text-[color:var(--app-muted-2)]">+</span>
                        <span className="text-xs text-[color:var(--app-muted)] font-medium">Thêm ảnh</span>
                        <input type="file" multiple accept="image/*" className="hidden" onChange={handleImageChange} />
                    </label>
                 )}

                 {/* Danh sách ảnh Preview */}
                 {previewUrls.map((url, index) => (
                    <div key={index} className="relative w-24 h-24 group">
                        <img 
                            src={url} 
                            alt="Preview" 
                      className="w-full h-full object-cover rounded-xl border border-[color:var(--app-border)] shadow-sm"
                        />
                        {/* Nút xóa ảnh */}
                        <button 
                            type="button"
                            onClick={() => removeImage(index)}
                            className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center shadow-md hover:bg-red-600 transition-colors text-xs font-bold"
                        >
                            ✕
                        </button>
                    </div>
                 ))}
              </div>
            </div>
            
            <div className="flex justify-end gap-3 pt-4 border-t border-[color:var(--app-border)]">
              <button type="button" onClick={() => setIsFormOpen(false)} className="px-6 py-2.5 text-[color:var(--app-text)] hover:bg-[color:var(--app-bg)] rounded-xl font-bold transition-colors border border-[color:var(--app-border)]">Hủy bỏ</button>
              <button type="submit" className="px-8 py-2.5 bg-[color:var(--app-primary)] text-white hover:bg-[color:var(--app-primary-hover)] rounded-xl font-bold shadow-lg transition-transform active:scale-95">
                🚀 Gửi yêu cầu
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Danh sách yêu cầu */}
      <div className="space-y-4">
        <h3 className="text-lg font-bold text-[color:var(--app-text)] mb-4 px-2">Lịch sử yêu cầu ({requests.length})</h3>
        
        {requests.length === 0 ? (
          <div className="text-center py-12 bg-[color:var(--app-surface-solid)] rounded-2xl border border-dashed border-[color:var(--app-border-strong)]">
                <span className="text-4xl block mb-3">📭</span>
            <p className="text-[color:var(--app-muted)]">Bạn chưa có yêu cầu bảo trì nào.</p>
            </div>
        ) : (
            requests.map(req => {
                const status = getStatusConfig(req.status);
                return (
              <div key={req.id} className={`bg-[color:var(--app-surface-solid)] p-6 rounded-2xl shadow-sm border border-[color:var(--app-border)] border-l-4 ${status.border} flex flex-col md:flex-row gap-6 hover:shadow-md transition-all`}>
                        {/* Thông tin chính */}
                        <div className="flex-1 space-y-3">
                            <div className="flex items-center justify-between md:justify-start gap-3">
                    <h4 className="text-lg font-bold text-[color:var(--app-text)]">Phòng {req.roomNumber}</h4>
                                <span className={`px-3 py-1 rounded-full text-xs font-bold border ${status.bg} ${status.text} ${status.border}`}>
                                    {status.label}
                                </span>
                            </div>
                            
                  <p className="text-[color:var(--app-muted)] leading-relaxed bg-[color:var(--app-bg)] p-3 rounded-lg border border-[color:var(--app-border)]">
                                {req.description}
                            </p>

                  <div className="flex flex-wrap gap-4 text-xs text-[color:var(--app-muted)] mt-2">
                                <div className="flex items-center gap-1">
                                    <span>📅 Ngày gửi:</span>
                      <span className="font-medium text-[color:var(--app-text)]">{new Date(req.createdAt).toLocaleDateString('vi-VN')}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <span>🔖 Mã phiếu:</span>
                      <span className="font-mono bg-[color:var(--app-bg)] px-1 rounded border border-[color:var(--app-border)]">{req.requestCode}</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <span>🏢 Chi nhánh:</span>
                      <span className="font-medium text-[color:var(--app-text)]">{req.branchCode || 'N/A'}</span>
                                </div>
                            </div>
                        </div>

                        {/* Cột phải: Hình ảnh & Kỹ thuật viên */}
                        <div className="flex flex-col justify-between items-start md:items-end min-w-[200px] border-t md:border-t-0 md:border-l border-[color:var(--app-border)] pt-4 md:pt-0 md:pl-6">
                            {/* Hiển thị ảnh nhỏ */}
                            {req.images && req.images.length > 0 ? (
                                <div className="flex gap-2 mb-4">
                                    {req.images.map((img, idx) => (
                                        <img 
                                            key={idx} 
                                            src={getImageUrl(img)} 
                                            alt="Proof" 
                                  className="w-12 h-12 object-cover rounded-lg border border-[color:var(--app-border)] cursor-zoom-in hover:border-[color:var(--app-primary)] transition-colors"
                                            onClick={() => setZoomImage(getImageUrl(img))}
                                            title="Nhấn để phóng to"
                                        />
                                    ))}
                                </div>
                            ) : (
                            <span className="text-xs text-[color:var(--app-muted-2)] italic mb-4">Không có hình ảnh</span>
                            )}

                            {/* Thông tin xử lý */}
                            <div className="text-right w-full">
                              <p className="text-xs text-[color:var(--app-muted)]">Người xử lý:</p>
                              <p className="font-bold text-sm text-[color:var(--app-text)] mb-1">{req.technicianName || '---'}</p>
                                
                                {req.cost > 0 && (
                                    <div className="mt-2 bg-green-50 px-3 py-1 rounded-lg text-right inline-block">
                                        <p className="text-xs text-green-600 font-bold">Chi phí</p>
                                        <p className="text-sm font-extrabold text-green-700">{req.cost.toLocaleString()} đ</p>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                );
            })
        )}
      </div>

      {/* Modal Phóng To Ảnh */}
      {zoomImage && (
        <div 
            className="fixed inset-0 z-[60] flex items-center justify-center bg-black/90 backdrop-blur-sm p-4 cursor-zoom-out"
            onClick={() => setZoomImage(null)}
        >
            <div className="relative max-w-5xl max-h-screen">
                <img 
                    src={zoomImage} 
                    alt="Full View" 
                    className="max-w-full max-h-[90vh] rounded-lg shadow-2xl"
                    onClick={(e) => e.stopPropagation()} // Click vào ảnh không đóng
                />
                <button 
              className="absolute -top-4 -right-4 bg-[color:var(--app-surface-solid)] text-[color:var(--app-text)] rounded-full w-8 h-8 flex items-center justify-center font-bold shadow-lg border border-[color:var(--app-border)] hover:bg-[color:var(--app-bg)]"
                    onClick={() => setZoomImage(null)}
                >
                    ✕
                </button>
            </div>
        </div>
      )}
    </div>
  );
}
import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import roomApi from '../../api/roomApi';
import branchApi from '../../api/branchApi';
import resolveImageUrl from '../../utils/resolveImageUrl';
// Import icons để giao diện đẹp hơn
import { 
  Building2, Search, Filter, Edit3, Image as ImageIcon, 
  X, Star, UploadCloud, Ban 
} from 'lucide-react';

// Cấu hình Meta cho Status với màu sắc và Icon tương ứng
const STATUS_META = {
  AVAILABLE: { 
    label: 'Phòng trống', 
    color: 'text-emerald-700 bg-emerald-50 border-emerald-200'
  },
  OCCUPIED: { 
    label: 'Đang thuê', 
    color: 'text-blue-700 bg-blue-50 border-blue-200'
  },
  MAINTENANCE: { 
    label: 'Bảo trì', 
    color: 'text-amber-700 bg-amber-50 border-amber-200'
  },
  RESERVED: { 
    label: 'Đã đặt', 
    color: 'text-violet-700 bg-violet-50 border-violet-200'
  },
};

// Component Badge hiển thị trạng thái
const StatusBadge = ({ status }) => {
  const meta = STATUS_META[status] || { label: status, color: 'text-gray-700 bg-gray-50 border-gray-200' };
  return (
    <span className={`text-xs font-medium px-2.5 py-1 rounded-full border ${meta.color}`}>{meta.label}</span>
  );
};

export default function RoomManagement() {
  const role = useSelector((state) => state?.auth?.user?.role);
  const canCreateRoom = ['ADMIN', 'DIRECTOR'].includes(role);

  const [rooms, setRooms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');

  const [branches, setBranches] = useState([]);

  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');
  const [createForm, setCreateForm] = useState({
    branchCode: '',
    roomNumber: '',
    area: '',
    price: '',
    status: 'AVAILABLE',
    description: '',
  });

  const [selectedRoom, setSelectedRoom] = useState(null);
  const [editDescription, setEditDescription] = useState('');
  const [images, setImages] = useState([]);
  const [savingDesc, setSavingDesc] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState('');

  // -- LOGIC GIỮ NGUYÊN --
  const fetchRooms = async () => {
    setLoading(true);
    try {
      const res = await roomApi.getAllRooms();
      const data = Array.isArray(res) ? res : (res?.content || []);
      setRooms(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error('Lỗi tải danh sách phòng', e);
      setRooms([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const loadBranches = async () => {
      try {
        const res = await branchApi.getAll();
        const list = Array.isArray(res) ? res : [];
        setBranches(list);
        if (!createForm.branchCode && list.length > 0) {
          setCreateForm((p) => ({ ...p, branchCode: list[0]?.branchCode || '' }));
        }
      } catch (e) {
        setBranches([]);
      }
    };
    loadBranches();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openCreate = () => {
    setCreateError('');
    setCreateForm((p) => ({
      branchCode: p.branchCode || branches?.[0]?.branchCode || '',
      roomNumber: '',
      area: '',
      price: '',
      status: 'AVAILABLE',
      description: '',
    }));
    setShowCreate(true);
  };

  const closeCreate = () => {
    setShowCreate(false);
    setCreateError('');
    setCreating(false);
  };

  const submitCreate = async (e) => {
    e.preventDefault();
    setCreating(true);
    setCreateError('');
    try {
      const payload = {
        branchCode: (createForm.branchCode || '').trim(),
        roomNumber: (createForm.roomNumber || '').trim(),
        area: createForm.area === '' ? null : Number(createForm.area),
        price: createForm.price === '' ? null : Number(createForm.price),
        status: createForm.status,
        description: (createForm.description || '').trim(),
      };

      if (!payload.branchCode || !payload.roomNumber) {
        setCreateError('Vui lòng chọn chi nhánh và nhập số phòng');
        return;
      }
      if (payload.area !== null && Number.isNaN(payload.area)) {
        setCreateError('Diện tích không hợp lệ');
        return;
      }
      if (payload.price !== null && Number.isNaN(payload.price)) {
        setCreateError('Giá phòng không hợp lệ');
        return;
      }

      await roomApi.createRoom(payload);
      await fetchRooms();
      closeCreate();
    } catch (e2) {
      setCreateError(e2?.response?.data?.message || e2?.message || 'Không thể tạo phòng');
    } finally {
      setCreating(false);
    }
  };

  const openEdit = async (room) => {
    setSelectedRoom(room);
    setEditDescription(room?.description || '');
    setImages([]);
    try {
      const imgs = await roomApi.listImages(room.id);
      setImages(Array.isArray(imgs) ? imgs : []);
    } catch (e) {
      console.error('Không thể tải ảnh phòng', e);
      setImages([]);
    }
  };

  const closeEdit = () => {
    setSelectedRoom(null);
    setEditDescription('');
    setImages([]);
    setSavingDesc(false);
    setUploading(false);
    setPreviewUrl('');
  };

  useEffect(() => {
    if (!previewUrl) return;
    const onKeyDown = (e) => {
      if (e.key === 'Escape') setPreviewUrl('');
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [previewUrl]);

  const updateStatus = async (roomId, status) => {
    try {
      await roomApi.updateRoomStatus?.(roomId, status);
      setRooms(prev => prev.map(r => r.id === roomId ? { ...r, status } : r));
    } catch (e) {
      alert('Không thể cập nhật trạng thái phòng');
    }
  };

  const saveDescription = async () => {
    if (!selectedRoom) return;
    setSavingDesc(true);
    try {
      const updated = await roomApi.updateDescription(selectedRoom.id, editDescription);
      const newDesc = updated?.description ?? editDescription;
      setRooms(prev => prev.map(r => r.id === selectedRoom.id ? { ...r, description: newDesc } : r));
      setSelectedRoom(prev => prev ? { ...prev, description: newDesc } : prev);
    } catch (e) {
      alert('Không thể cập nhật mô tả phòng');
    } finally {
      setSavingDesc(false);
    }
  };

  const uploadRoomImages = async (files) => {
    if (!selectedRoom || !files || files.length === 0) return;
    setUploading(true);
    try {
      const formData = new FormData();
      Array.from(files).forEach((f) => formData.append('images', f));
      await roomApi.uploadImages(selectedRoom.id, formData);
      const imgs = await roomApi.listImages(selectedRoom.id);
      setImages(Array.isArray(imgs) ? imgs : []);
    } catch (e) {
      alert('Không thể upload ảnh phòng');
    } finally {
      setUploading(false);
    }
  };

  const setThumbnail = async (imageId) => {
    if (!selectedRoom) return;
    try {
      const updated = await roomApi.setThumbnail(selectedRoom.id, imageId);
      setImages(Array.isArray(updated) ? updated : []);
    } catch (e) {
      alert('Không thể đặt ảnh đại diện');
    }
  };

  const deleteImage = async (imageId) => {
    if (!selectedRoom) return;
    if (!window.confirm('Gỡ ảnh này khỏi phòng?')) return;
    try {
      const updated = await roomApi.deleteImage(selectedRoom.id, imageId);
      setImages(Array.isArray(updated) ? updated : []);
    } catch (e) {
      alert('Không thể gỡ ảnh phòng');
    }
  };

  const filtered = rooms.filter(r => statusFilter === 'ALL' || (r.status === statusFilter));
  
  const sortedImages = (images || []).slice().sort((a, b) => (b?.isThumbnail === true) - (a?.isThumbnail === true) || (b?.id || 0) - (a?.id || 0));

  return (
    <div className="min-h-screen bg-gray-50 text-slate-800 font-sans">
      <div className="container mx-auto px-4 py-8 max-w-7xl">
        
        {/* HEADER */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Quản lý phòng</h1>
            <p className="text-slate-500 mt-1">Quản lý danh sách, trạng thái và hình ảnh phòng trọ.</p>
          </div>
          
          <div className="flex items-center gap-3">
            {canCreateRoom && (
              <button
                type="button"
                onClick={openCreate}
                className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold px-4 py-2 rounded-lg shadow-sm"
              >
                Tạo phòng
              </button>
            )}

            <div className="flex items-center bg-white border rounded-lg p-1 shadow-sm">
            <div className="pl-3 pr-2 text-slate-400">
              <Filter size={18} />
            </div>
            <select 
              className="bg-transparent border-none text-sm font-medium focus:ring-0 text-slate-700 py-2 pr-8 cursor-pointer"
              value={statusFilter} 
              onChange={e => setStatusFilter(e.target.value)}
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value="AVAILABLE">Phòng trống</option>
              <option value="OCCUPIED">Đang thuê</option>
              <option value="MAINTENANCE">Bảo trì</option>
              <option value="RESERVED">Đã đặt</option>
            </select>
          </div>
          </div>
        </div>

        {/* LOADING STATE */}
        {loading && (
          <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
          </div>
        )}

        {/* ROOM LIST GRID */}
        {!loading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filtered.map(room => (
              <div key={room.id} className="group bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-lg hover:border-indigo-100 transition-all duration-200 flex flex-col overflow-hidden">
                
                {/* Card Header */}
                <div className="p-5 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white">
                  <div className="flex justify-between items-start mb-3">
                    <StatusBadge status={room.status} />
                    <button 
                      onClick={() => openEdit(room)}
                      className="text-gray-400 hover:text-indigo-600 p-1.5 rounded-full hover:bg-indigo-50 transition-colors"
                      title="Chỉnh sửa"
                    >
                      <Edit3 size={18} />
                    </button>
                  </div>
                  
                  <div className="flex items-baseline gap-1">
                    <h3 className="text-xl font-bold text-slate-800">
                      {room.roomNumber || '---'}
                    </h3>
                    <span className="text-sm text-slate-500 font-medium">{room.roomCode}</span>
                  </div>
                  <div className="text-xs text-slate-400 mt-1 font-medium flex items-center gap-1">
                    <Building2 size={12}/> Chi nhánh: {room.branchCode || '-'}
                  </div>
                </div>

                {/* Card Body */}
                <div className="p-5 flex-1 flex flex-col justify-between">
                  <div>
                    <div className="text-2xl font-bold text-indigo-600">
                      {new Intl.NumberFormat('vi-VN').format(room.price || 0)}
                      <span className="text-sm font-normal text-slate-500 ml-1">đ/tháng</span>
                    </div>
                    {room.description && (
                      <p className="text-sm text-slate-500 mt-3 line-clamp-2 leading-relaxed">
                        {room.description}
                      </p>
                    )}
                  </div>

                  {/* Actions Grid */}
                  <div className="mt-5 grid grid-cols-4 gap-2 pt-4 border-t border-dashed border-gray-200">
                    {[
                      { s: 'AVAILABLE', color: 'hover:bg-emerald-50 hover:text-emerald-700 hover:border-emerald-200' },
                      { s: 'OCCUPIED', color: 'hover:bg-blue-50 hover:text-blue-700 hover:border-blue-200' },
                      { s: 'MAINTENANCE', color: 'hover:bg-amber-50 hover:text-amber-700 hover:border-amber-200' },
                      { s: 'RESERVED', color: 'hover:bg-violet-50 hover:text-violet-700 hover:border-violet-200' }
                    ].map(({s, color}) => {
                       const active = room.status === s;
                       const statusLocked = room.status === 'OCCUPIED' || room.status === 'RESERVED';
                       return (
                        <button
                          key={s}
                          onClick={() => updateStatus(room.id, s)}
                          title={`Đặt trạng thái: ${STATUS_META[s].label}`}
                          className={`
                            flex items-center justify-center p-2 rounded-lg border transition-all duration-200 text-[11px] font-semibold
                            ${active 
                              ? 'bg-slate-800 text-white border-slate-800 shadow-md ring-2 ring-offset-1 ring-slate-200' 
                              : `bg-white border-gray-200 text-gray-600 ${color}`
                            }
                            ${statusLocked && !active ? 'opacity-50 cursor-not-allowed hover:bg-white hover:text-gray-600 hover:border-gray-200' : ''}
                          `}
                          disabled={statusLocked}
                        >
                          {STATUS_META[s].label}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
        
        {/* EMPTY STATE */}
        {!loading && filtered.length === 0 && (
          <div className="text-center py-20 bg-white rounded-2xl border border-dashed border-gray-300">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-50 mb-4 text-gray-400">
               <Ban size={32} />
            </div>
            <h3 className="text-lg font-medium text-gray-900">Không tìm thấy phòng</h3>
            <p className="text-gray-500 mt-1">Thử thay đổi bộ lọc trạng thái để xem kết quả khác.</p>
          </div>
        )}

        {/* MODAL EDIT */}
        {selectedRoom && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" onClick={closeEdit} />
            
            <div className="relative w-full max-w-5xl bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-200">
              {/* Modal Header */}
              <div className="flex items-center justify-between px-6 py-4 border-b bg-white z-10">
                <div>
                  <h2 className="text-xl font-bold text-slate-800 flex items-center gap-2">
                    {selectedRoom.roomCode}
                    <StatusBadge status={selectedRoom.status} />
                  </h2>
                  <p className="text-sm text-slate-500">
                    Chi nhánh {selectedRoom.branchCode} • Phòng {selectedRoom.roomNumber}
                  </p>
                </div>
                <button 
                  onClick={closeEdit}
                  className="p-2 bg-gray-100 hover:bg-gray-200 rounded-full text-gray-600 transition-colors"
                >
                  <X size={20} />
                </button>
              </div>

              {/* Modal Body */}
              <div className="flex-1 overflow-auto bg-gray-50/50 p-6">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 h-full">
                  
                  {/* Left Column: Description */}
                  <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm h-fit">
                    <label className="block text-sm font-semibold text-slate-700 mb-2">Mô tả phòng</label>
                    <p className="text-xs text-slate-500 mb-4">Thông tin tiện ích, đặc điểm nổi bật của phòng.</p>
                    
                    <textarea
                      className="w-full border-gray-200 rounded-lg px-4 py-3 min-h-[200px] text-sm focus:border-indigo-500 focus:ring-indigo-500 bg-gray-50 focus:bg-white transition-all resize-y"
                      value={editDescription}
                      onChange={(e) => setEditDescription(e.target.value)}
                      placeholder="Nhập mô tả chi tiết..."
                    />
                    
                    <div className="mt-4 flex justify-end">
                      <button
                        className={`flex items-center gap-2 text-sm font-medium px-5 py-2.5 rounded-lg text-white shadow-sm transition-all
                          ${savingDesc ? 'bg-indigo-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700 hover:shadow-md active:translate-y-0.5'}
                        `}
                        onClick={saveDescription}
                        disabled={savingDesc}
                      >
                         {savingDesc ? 'Đang lưu...' : 'Lưu thay đổi'}
                      </button>
                    </div>
                  </div>

                  {/* Right Column: Images */}
                  <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm flex flex-col h-fit min-h-[400px]">
                    <div className="flex items-center justify-between mb-4">
                      <div>
                        <h3 className="text-sm font-semibold text-slate-700">Thư viện ảnh</h3>
                        <p className="text-xs text-slate-500">{sortedImages.length} ảnh đã tải lên</p>
                      </div>
                      <label className={`
                        flex items-center gap-2 cursor-pointer bg-white border border-gray-300 hover:border-indigo-500 hover:text-indigo-600 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium transition-all shadow-sm
                        ${uploading ? 'opacity-50 cursor-wait' : ''}
                      `}>
                        <UploadCloud size={16} />
                        <span>Tải ảnh</span>
                        <input
                          type="file"
                          multiple
                          accept="image/*"
                          className="hidden"
                          disabled={uploading}
                          onChange={(e) => uploadRoomImages(e.target.files)}
                        />
                      </label>
                    </div>

                    {uploading && (
                      <div className="mb-4 w-full bg-gray-100 rounded-full h-1.5 overflow-hidden">
                        <div className="bg-indigo-600 h-1.5 rounded-full animate-progress w-2/3"></div>
                      </div>
                    )}

                    {sortedImages.length === 0 ? (
                      <div className="flex-1 flex flex-col items-center justify-center border-2 border-dashed border-gray-200 rounded-xl bg-gray-50 py-10">
                        <ImageIcon className="text-gray-300 mb-3" size={48} />
                        <p className="text-sm text-gray-500">Chưa có hình ảnh nào</p>
                        <p className="text-xs text-gray-400 mt-1">Tải ảnh lên để hiển thị cho khách xem</p>
                      </div>
                    ) : (
                      <div className="grid grid-cols-2 sm:grid-cols-3 gap-3 overflow-y-auto max-h-[400px] pr-1 custom-scrollbar">
                        {sortedImages.map((img) => (
                          <div key={img.id} className="group relative aspect-square bg-gray-100 rounded-lg overflow-hidden border border-gray-200">
                            <button
                              type="button"
                              className="block w-full h-full"
                              onClick={() => setPreviewUrl(resolveImageUrl(img.imageUrl))}
                              title="Bấm để phóng to"
                            >
                              <img 
                                className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110" 
                                src={resolveImageUrl(img.imageUrl)} 
                                alt="room" 
                              />
                            </button>
                            
                            {/* Overlay Gradient */}
                            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300" />

                            {/* Thumbnail Star (click to set) */}
                            <button
                              type="button"
                              onClick={() => setThumbnail(img.id)}
                              className={`absolute top-2 right-2 p-1.5 rounded-full shadow-sm backdrop-blur-sm transition-all
                                ${img.isThumbnail ? 'bg-amber-500/95 text-white' : 'bg-white/90 text-amber-600 hover:bg-white'}
                              `}
                              title={img.isThumbnail ? 'Đang là ảnh đại diện' : 'Đặt làm ảnh đại diện'}
                            >
                              <Star size={16} fill={img.isThumbnail ? 'currentColor' : 'none'} />
                            </button>

                            {/* Hover Actions */}
                            <div className="absolute bottom-2 right-2 opacity-0 group-hover:opacity-100 transition-all duration-300 translate-y-2 group-hover:translate-y-0">
                              <button 
                                onClick={() => deleteImage(img.id)}
                                className="px-2 py-1 text-xs font-medium bg-white/90 hover:bg-white text-red-600 rounded shadow-sm backdrop-blur-sm"
                                title="Gỡ ảnh"
                              >
                                Gỡ
                              </button>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* MODAL CREATE */}
        {showCreate && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" onClick={closeCreate} />

            <div className="relative w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-200">
              <div className="flex items-center justify-between px-6 py-4 border-b bg-white z-10">
                <div>
                  <h2 className="text-xl font-bold text-slate-800">Tạo phòng</h2>
                  <p className="text-sm text-slate-500">Nhập thông tin phòng mới</p>
                </div>
                <button
                  onClick={closeCreate}
                  className="p-2 bg-gray-100 hover:bg-gray-200 rounded-full text-gray-600 transition-colors"
                >
                  <X size={20} />
                </button>
              </div>

              <div className="flex-1 overflow-auto bg-gray-50/50 p-6">
                {createError && (
                  <div className="mb-4 p-3 rounded-lg border border-red-200 bg-red-50 text-red-700 text-sm">{createError}</div>
                )}

                <form onSubmit={submitCreate} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Chi nhánh</label>
                    <select
                      className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                      value={createForm.branchCode}
                      onChange={(e) => setCreateForm((p) => ({ ...p, branchCode: e.target.value }))}
                    >
                      {branches.map((b) => (
                        <option key={b.id} value={b.branchCode}>
                          {b.branchCode} - {b.branchName}
                        </option>
                      ))}
                      {branches.length === 0 && <option value="">(Chưa có chi nhánh)</option>}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Số phòng</label>
                    <input
                      className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                      value={createForm.roomNumber}
                      onChange={(e) => setCreateForm((p) => ({ ...p, roomNumber: e.target.value }))}
                      placeholder="Ví dụ: 101"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Diện tích (m²)</label>
                    <input
                      type="number"
                      min="0"
                      className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                      value={createForm.area}
                      onChange={(e) => setCreateForm((p) => ({ ...p, area: e.target.value }))}
                      placeholder="Ví dụ: 20"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Giá (đ/tháng)</label>
                    <input
                      type="number"
                      min="0"
                      className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                      value={createForm.price}
                      onChange={(e) => setCreateForm((p) => ({ ...p, price: e.target.value }))}
                      placeholder="Ví dụ: 2500000"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Trạng thái</label>
                    <select
                      className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:ring-indigo-500"
                      value={createForm.status}
                      onChange={(e) => setCreateForm((p) => ({ ...p, status: e.target.value }))}
                    >
                      <option value="AVAILABLE">Phòng trống</option>
                      <option value="OCCUPIED">Đang thuê</option>
                      <option value="MAINTENANCE">Bảo trì</option>
                      <option value="RESERVED">Đã đặt</option>
                    </select>
                  </div>

                  <div className="sm:col-span-2">
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Mô tả</label>
                    <textarea
                      className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm focus:border-indigo-500 focus:ring-indigo-500 min-h-[120px]"
                      value={createForm.description}
                      onChange={(e) => setCreateForm((p) => ({ ...p, description: e.target.value }))}
                      placeholder="Nhập mô tả (tuỳ chọn)"
                    />
                  </div>

                  <div className="sm:col-span-2 flex justify-end gap-2 pt-2">
                    <button
                      type="button"
                      onClick={closeCreate}
                      className="px-4 py-2 rounded-lg border border-gray-200 text-gray-700 hover:bg-gray-50 text-sm font-semibold"
                      disabled={creating}
                    >
                      Hủy
                    </button>
                    <button
                      type="submit"
                      className="px-5 py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold disabled:opacity-60"
                      disabled={creating}
                    >
                      {creating ? 'Đang tạo...' : 'Tạo phòng'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* LIGHTBOX PREVIEW */}
        {previewUrl && (
          <div className="fixed inset-0 z-[60] bg-black/90 backdrop-blur-md flex items-center justify-center p-4 animate-in fade-in duration-200" onClick={() => setPreviewUrl('')}>
            <button
              className="absolute top-6 right-6 text-white/70 hover:text-white bg-white/10 hover:bg-white/20 p-2 rounded-full transition-colors"
              onClick={() => setPreviewUrl('')}
            >
              <X size={24} />
            </button>
            <img 
              src={previewUrl} 
              alt="preview" 
              className="max-w-full max-h-[90vh] rounded-lg shadow-2xl object-contain animate-in zoom-in-95 duration-300" 
              onClick={(e) => e.stopPropagation()}
            />
          </div>
        )}
      </div>
    </div>
  );
}
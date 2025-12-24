import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import partnerApi from '../../api/partnerApi';

export default function CreateListing() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        title: '', description: '', price: '', area: '', address: '', postType: 'NORMAL'
    });
    const [images, setImages] = useState([]);
    const [imagePreviews, setImagePreviews] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleImageChange = (e) => {
        const files = Array.from(e.target.files);
        if (files.length > 5) { alert('Chỉ được tải lên tối đa 5 ảnh!'); return; }
        setImages(files);
        const previews = files.map(file => URL.createObjectURL(file));
        setImagePreviews(previews);
    };

    const removeImage = (index) => {
        const newImages = images.filter((_, i) => i !== index);
        const newPreviews = imagePreviews.filter((_, i) => i !== index);
        setImages(newImages);
        setImagePreviews(newPreviews);
        URL.revokeObjectURL(imagePreviews[index]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const payload = {
                title: formData.title,
                description: formData.description,
                price: parseFloat(formData.price),
                area: parseFloat(formData.area),
                address: formData.address,
                postType: formData.postType
            };
            const created = await partnerApi.createPost(payload, images);
            const createdId = created?.id;
            const createdStatus = created?.status;

            if (createdId && String(createdStatus).toUpperCase() === 'PENDING_PAYMENT') {
                alert('Đăng tin thành công! Vui lòng thanh toán để hoàn tất.');
                const payment = await partnerApi.initiatePostMomoPayment(createdId);
                const payUrl = payment?.payUrl || payment?.paymentUrl;
                if (payUrl) {
                    window.location.href = payUrl;
                    return;
                }
            }

            alert('Đăng tin thành công! Tin đang chờ duyệt.');
            navigate('/partner/my-listings');
        } catch (error) {
            console.error('Create post error:', error);
            const msg = error.response?.data?.message || error.message || 'Không thể đăng tin';
            alert('Lỗi: ' + msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto space-y-8 animate-fade-in-up">
            <div className="text-center">
                <h1 className="text-3xl font-extrabold text-gray-900 bg-clip-text text-transparent bg-gradient-to-r from-indigo-600 to-purple-600">
                    Đăng tin cho thuê mới
                </h1>
                <p className="text-gray-500 mt-2">Tiếp cận hàng ngàn sinh viên đang tìm kiếm nơi ở lý tưởng.</p>
            </div>

            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-2xl shadow-xl shadow-indigo-100/50 border border-gray-100 space-y-8 relative overflow-hidden">
                {/* Decorative background circle */}
                <div className="absolute -top-20 -right-20 w-64 h-64 bg-indigo-50 rounded-full blur-3xl opacity-50 pointer-events-none"></div>

                {/* Section 1 */}
                <div className="space-y-5 relative">
                    <h3 className="font-bold text-lg text-gray-800 flex items-center gap-2 border-b border-gray-100 pb-2">
                        <span className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center text-sm">1</span>
                        Thông tin cơ bản
                    </h3>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">Tiêu đề tin <span className="text-red-500">*</span></label>
                        <input required className="w-full border-gray-200 bg-gray-50 rounded-xl p-3 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all outline-none"
                            placeholder="VD: Phòng trọ cao cấp, full nội thất gần ĐH Bách Khoa..."
                            onChange={e => setFormData({ ...formData, title: e.target.value })} />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">Giá thuê (VNĐ/tháng) <span className="text-red-500">*</span></label>
                            <div className="relative">
                                <input type="number" required className="w-full border-gray-200 bg-gray-50 rounded-xl p-3 pl-10 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all outline-none"
                                    placeholder="3000000"
                                    onChange={e => setFormData({ ...formData, price: e.target.value })} />
                                <span className="absolute left-3 top-3 text-gray-400 font-bold">₫</span>
                            </div>
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">Diện tích (m²) <span className="text-red-500">*</span></label>
                            <div className="relative">
                                <input type="number" required className="w-full border-gray-200 bg-gray-50 rounded-xl p-3 pl-10 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all outline-none"
                                    placeholder="25"
                                    onChange={e => setFormData({ ...formData, area: e.target.value })} />
                                <span className="absolute left-3 top-3 text-gray-400">📐</span>
                            </div>
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">Địa chỉ chính xác <span className="text-red-500">*</span></label>
                        <div className="relative">
                            <input required className="w-full border-gray-200 bg-gray-50 rounded-xl p-3 pl-10 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all outline-none"
                                placeholder="Số nhà, tên đường, phường/xã, quận/huyện..."
                                onChange={e => setFormData({ ...formData, address: e.target.value })} />
                            <span className="absolute left-3 top-3 text-gray-400">📍</span>
                        </div>
                    </div>
                </div>

                {/* Section 2 */}
                <div className="space-y-5 relative">
                    <h3 className="font-bold text-lg text-gray-800 flex items-center gap-2 border-b border-gray-100 pb-2">
                        <span className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center text-sm">2</span>
                        Mô tả & Hình ảnh
                    </h3>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-2">Mô tả chi tiết</label>
                        <textarea required rows="5" className="w-full border-gray-200 bg-gray-50 rounded-xl p-3 focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all outline-none"
                            placeholder="Mô tả các tiện ích, nội thất, quy định giờ giấc..."
                            onChange={e => setFormData({ ...formData, description: e.target.value })}></textarea>
                    </div>

                    <div>
                        <label className="block text-sm font-semibold text-gray-700 mb-3">Hình ảnh (Tối đa 5)</label>

                        <div className="grid grid-cols-5 gap-4">
                            {/* Upload Button */}
                            {images.length < 5 && (
                                <label className="aspect-square flex flex-col items-center justify-center border-2 border-dashed border-indigo-200 rounded-xl cursor-pointer hover:border-indigo-500 hover:bg-indigo-50 transition-all group">
                                    <div className="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-2 group-hover:scale-110 transition-transform">
                                        <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v16m8-8H4"></path></svg>
                                    </div>
                                    <span className="text-xs font-semibold text-indigo-600">Thêm ảnh</span>
                                    <input type="file" multiple accept="image/*" className="hidden" onChange={handleImageChange} />
                                </label>
                            )}

                            {/* Previews */}
                            {imagePreviews.map((preview, index) => (
                                <div key={index} className="aspect-square relative group">
                                    <img src={preview} alt="Preview" className="w-full h-full object-cover rounded-xl border border-gray-200 shadow-sm" />
                                    <button type="button" onClick={() => removeImage(index)} className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center shadow-md opacity-0 group-hover:opacity-100 transition-all hover:bg-red-600 transform hover:scale-110">
                                        ×
                                    </button>
                                    {index === 0 && <span className="absolute bottom-1 left-1 bg-black/60 text-white text-[10px] px-1.5 rounded">Cover</span>}
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Section 3 */}
                <div className="space-y-5 relative">
                    <h3 className="font-bold text-lg text-gray-800 flex items-center gap-2 border-b border-gray-100 pb-2">
                        <span className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center text-sm">3</span>
                        Cấu hình hiển thị
                    </h3>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {/* Tin Thường */}
                        <label className={`relative border-2 rounded-2xl p-5 cursor-pointer transition-all ${formData.postType === 'NORMAL' ? 'border-gray-700 bg-gray-50' : 'border-gray-100 hover:border-gray-200'}`}>
                            <input type="radio" name="postType" className="hidden" checked={formData.postType === 'NORMAL'} onChange={() => setFormData({ ...formData, postType: 'NORMAL' })} />
                            <div className="flex items-center gap-3 mb-2">
                                <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${formData.postType === 'NORMAL' ? 'border-gray-800' : 'border-gray-300'}`}>
                                    {formData.postType === 'NORMAL' && <div className="w-3 h-3 rounded-full bg-gray-900"></div>}
                                </div>
                                <span className="font-bold text-gray-900">Tin Thường (Standard)</span>
                            </div>
                            <p className="text-sm text-gray-600 pl-8">Vị trí thấp, trôi nhanh. Giá 20.000đ/tin.</p>
                        </label>

                        {/* VIP 1 - Silver */}
                        <label className={`relative border-2 rounded-2xl p-5 cursor-pointer transition-all ${formData.postType === 'VIP1' ? 'border-blue-500 bg-blue-50/50 shadow-sm' : 'border-gray-100 hover:border-blue-200'}`}>
                            <input type="radio" name="postType" className="hidden" checked={formData.postType === 'VIP1'} onChange={() => setFormData({ ...formData, postType: 'VIP1' })} />
                            <div className="absolute top-0 right-0 bg-blue-600 text-white text-xs font-bold px-3 py-1 rounded-bl-xl rounded-tr-lg">VIP Bạc</div>
                            <div className="flex items-center gap-3 mb-2">
                                <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${formData.postType === 'VIP1' ? 'border-blue-600' : 'border-gray-300'}`}>
                                    {formData.postType === 'VIP1' && <div className="w-3 h-3 rounded-full bg-blue-600"></div>}
                                </div>
                                <span className="font-bold text-blue-700">VIP 1 (Silver)</span>
                            </div>
                            <p className="text-sm text-gray-600 pl-8">Nổi bật nhẹ, giá rẻ. Vị trí trên tin thường. Giá 50.000đ/tin.</p>
                        </label>

                        {/* VIP 2 - Gold */}
                        <label className={`relative border-2 rounded-2xl p-5 cursor-pointer transition-all ${formData.postType === 'VIP2' ? 'border-amber-400 bg-amber-50/60 shadow-md' : 'border-amber-100 hover:border-amber-200'}`}>
                            <input type="radio" name="postType" className="hidden" checked={formData.postType === 'VIP2'} onChange={() => setFormData({ ...formData, postType: 'VIP2' })} />
                            <div className="absolute top-0 right-0 bg-gradient-to-r from-amber-400 to-orange-500 text-white text-xs font-bold px-3 py-1 rounded-bl-xl rounded-tr-lg">VIP Vàng ⭐</div>
                            <div className="flex items-center gap-3 mb-2">
                                <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${formData.postType === 'VIP2' ? 'border-orange-500' : 'border-amber-300'}`}>
                                    {formData.postType === 'VIP2' && <div className="w-3 h-3 rounded-full bg-orange-500"></div>}
                                </div>
                                <span className="font-extrabold text-orange-600 uppercase flex items-center gap-2">VIP 2 (Gold) <span>⭐</span></span>
                            </div>
                            <p className="text-sm text-gray-700 pl-8">Thu hút mạnh. Tiêu đề cam/đỏ, khung viền vàng nhạt, icon sao. Giá 100.000đ/tin.</p>
                        </label>

                        {/* VIP 3 - Diamond */}
                        <label className={`relative border-2 rounded-2xl p-5 cursor-pointer transition-all ${formData.postType === 'VIP3' ? 'border-purple-500 bg-purple-50/60 shadow-lg' : 'border-purple-100 hover:border-purple-200'}`}>
                            <input type="radio" name="postType" className="hidden" checked={formData.postType === 'VIP3'} onChange={() => setFormData({ ...formData, postType: 'VIP3' })} />
                            <div className="absolute top-0 right-0 bg-gradient-to-r from-fuchsia-500 via-purple-600 to-pink-500 text-white text-xs font-bold px-3 py-1 rounded-bl-xl rounded-tr-lg">VIP Kim Cương</div>
                            <div className="flex items-center gap-3 mb-2">
                                <div className={`w-6 h-6 rounded-full border flex items-center justify-center ${formData.postType === 'VIP3' ? 'border-fuchsia-600' : 'border-purple-300'}`}>
                                    {formData.postType === 'VIP3' && <div className="w-4 h-4 rounded-full bg-fuchsia-600 animate-pulse"></div>}
                                </div>
                                <span className="font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-purple-600 via-pink-500 to-fuchsia-500 text-lg">VIP 3 (Diamond)</span>
                            </div>
                            <p className="text-sm text-gray-700 pl-8">Top 1-5 tìm kiếm, gợi ý hôm nay, tiêu đề tím/hồng chữ lớn, ảnh cover to hơn, nút "Gọi ngay" nổi bật. Giá 200.000đ/tin.</p>
                        </label>
                    </div>
                </div>

                <div className="pt-6 border-t border-gray-100">
                    <button type="submit" disabled={loading} className="w-full py-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-bold rounded-xl shadow-lg shadow-indigo-200 hover:shadow-indigo-300 transform active:scale-[0.98] transition-all disabled:opacity-70 disabled:cursor-not-allowed">
                        {loading ? (
                            <span className="flex items-center justify-center gap-2">
                                <svg className="animate-spin h-5 w-5 text-white" viewBox="0 0 24 24"><circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle><path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path></svg>
                                Đang xử lý...
                            </span>
                        ) : '🚀 Đăng tin ngay'}
                    </button>
                </div>
            </form>
        </div>
    );
}
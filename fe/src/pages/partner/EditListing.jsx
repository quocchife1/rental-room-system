import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import partnerApi from '../../api/partnerApi';
import resolveImageUrl from '../../utils/resolveImageUrl';

export default function EditListing() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    area: '',
    address: '',
    postType: 'NORMAL'
  });
  const [originalPostType, setOriginalPostType] = useState(null);
  const [postStatus, setPostStatus] = useState(null);
  const [images, setImages] = useState([]);
  const [imagePreviews, setImagePreviews] = useState([]);
  const [existingImages, setExistingImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fetchingPost, setFetchingPost] = useState(true);

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);
    if (files.length > 5) {
      alert('Chỉ được tải lên tối đa 5 ảnh!');
      return;
    }

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

  useEffect(() => {
    fetchPost();
  }, [id]);

  const fetchPost = async () => {
    try {
      const post = await partnerApi.getPostById(id);
      setFormData({
        title: post.title,
        description: post.description,
        price: post.price.toString(),
        area: post.area.toString(),
        address: post.address,
        postType: post.postType
      });
      setOriginalPostType(post.postType);
      setPostStatus(post.status);

      // Load existing images
      if (post.imageUrls && post.imageUrls.length > 0) {
        setExistingImages(post.imageUrls);
      }
    } catch (error) {
      console.error(error);
      alert('Không tìm thấy tin đăng hoặc bạn không có quyền chỉnh sửa');
      navigate('/partner/my-listings');
    } finally {
      setFetchingPost(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const canEdit = !(postStatus === 'APPROVED' || postStatus === 'REJECTED');
    if (!canEdit) {
      alert('Tin đã hiển thị hoặc bị từ chối, không thể sửa.');
      return;
    }
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

      const res = await partnerApi.updatePost(id, payload, images);
      const responseData = res?.data?.data || res?.data || res;
      const paymentUrl = responseData?.paymentUrl;
      const postTypeChanged = originalPostType && originalPostType !== formData.postType;

      if (postTypeChanged && paymentUrl) {
        alert('Đổi gói tin thành công. Đang chuyển đến trang thanh toán MoMo.');
        imagePreviews.forEach(url => URL.revokeObjectURL(url));
        window.location.href = paymentUrl;
        return;
      }

      alert('Cập nhật tin thành công!');

      // Clean up preview URLs
      imagePreviews.forEach(url => URL.revokeObjectURL(url));

      navigate('/partner/my-listings');
    } catch (error) {
      console.error(error);
      alert('Lỗi: ' + (error.response?.data?.message || 'Không thể cập nhật tin'));
    } finally {
      setLoading(false);
    }
  };

  if (fetchingPost) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-gray-500">Đang tải...</div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-extrabold text-gray-800">Chỉnh sửa tin đăng</h1>
        <p className="text-gray-500 mt-2">Cập nhật thông tin tin đăng của bạn</p>
      </div>
      {/* Notice when editing is locked */}
      {postStatus === 'APPROVED' || postStatus === 'REJECTED' ? (
        <div className="mb-4 p-4 bg-yellow-50 border border-yellow-200 rounded-xl text-sm text-yellow-800">
          Tin đang hiển thị hoặc đã bị từ chối, không thể chỉnh sửa.
        </div>
      ) : null}

      <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow-lg p-8 space-y-6">
        {/** compute canEdit for disabling fields */}
        {(() => { })()}
        {/* Thông tin cơ bản */}
        <div className="space-y-4">
          <h3 className="font-bold text-gray-700 border-b pb-2">1. Thông tin cơ bản</h3>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tiêu đề tin đăng</label>
            <input required type="text" className="w-full border-gray-300 rounded-lg p-2.5"
              placeholder="VD: Cho thuê phòng trọ giá rẻ gần ĐH Công Nghệ..."
              value={formData.title}
              onChange={e => setFormData({ ...formData, title: e.target.value })}
              disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Giá thuê (VNĐ/tháng)</label>
              <input required type="number" className="w-full border-gray-300 rounded-lg p-2.5"
                placeholder="2000000"
                value={formData.price}
                onChange={e => setFormData({ ...formData, price: e.target.value })}
                disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Diện tích (m²)</label>
              <input required type="number" className="w-full border-gray-300 rounded-lg p-2.5"
                placeholder="25"
                value={formData.area}
                onChange={e => setFormData({ ...formData, area: e.target.value })}
                disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Địa chỉ</label>
            <input required type="text" className="w-full border-gray-300 rounded-lg p-2.5"
              placeholder="123 Đường ABC, Quận XYZ, TP HCM"
              value={formData.address}
              onChange={e => setFormData({ ...formData, address: e.target.value })}
              disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
          </div>
        </div>

        {/* Mô tả */}
        <div className="space-y-4">
          <h3 className="font-bold text-gray-700 border-b pb-2">2. Mô tả chi tiết</h3>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả chi tiết</label>
            <textarea required rows="5" className="w-full border-gray-300 rounded-lg p-2.5"
              placeholder="Mô tả đầy đủ về phòng trọ..."
              value={formData.description}
              onChange={e => setFormData({ ...formData, description: e.target.value })}
              disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'}></textarea>
          </div>
        </div>

        {/* Hình ảnh */}
        <div className="space-y-4">
          <h3 className="font-bold text-gray-700 border-b pb-2">3. Hình ảnh</h3>

          {/* Existing images */}
          {existingImages.length > 0 && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Ảnh hiện tại</label>
              <div className="grid grid-cols-5 gap-2 mb-4">
                {existingImages.map((url, index) => (
                  <div key={index} className="relative">
                    <img
                      src={resolveImageUrl(url)}
                      alt={`Existing ${index + 1}`}
                      className="w-full h-24 object-cover rounded-lg border"
                      onError={(e) => { e.target.src = '/placeholder-image.png'; }}
                    />
                    {index === 0 && (
                      <span className="absolute bottom-1 left-1 bg-indigo-600 text-white text-xs px-2 py-0.5 rounded">Đại diện</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Upload new images */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              {existingImages.length > 0 ? 'Thay thế bằng ảnh mới (Tối đa 5)' : 'Tải lên ảnh (Tối đa 5)'}
            </label>
            <input
              type="file"
              multiple
              accept="image/*"
              onChange={handleImageChange}
              className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100"
              disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'}
            />
            <p className="text-xs text-gray-500 mt-1">
              {existingImages.length > 0
                ? 'Nếu tải ảnh mới, tất cả ảnh cũ sẽ bị thay thế. Ảnh đầu tiên sẽ là ảnh đại diện.'
                : 'Ảnh đầu tiên sẽ là ảnh đại diện'}
            </p>
          </div>

          {/* New image previews */}
          {imagePreviews.length > 0 && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Ảnh mới sẽ tải lên</label>
              <div className="grid grid-cols-5 gap-2">
                {imagePreviews.map((preview, index) => (
                  <div key={index} className="relative group">
                    <img src={preview} alt={`Preview ${index + 1}`} className="w-full h-24 object-cover rounded-lg border" />
                    <button
                      type="button"
                      onClick={() => removeImage(index)}
                      className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                    >
                      ×
                    </button>
                    {index === 0 && (
                      <span className="absolute bottom-1 left-1 bg-indigo-600 text-white text-xs px-2 py-0.5 rounded">Đại diện</span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Chọn gói tin */}
        <div className="space-y-4">
          <h3 className="font-bold text-gray-700 border-b pb-2">4. Loại tin đăng</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <label className={`relative border-2 rounded-2xl p-4 cursor-pointer transition-all ${formData.postType === 'NORMAL' ? 'border-gray-700 bg-gray-50' : 'border-gray-200 hover:border-gray-300'}`}>
              <input type="radio" name="pkg" className="hidden" checked={formData.postType === 'NORMAL'} onChange={() => setFormData({ ...formData, postType: 'NORMAL' })} disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
              <div className="flex items-center gap-2 mb-1">
                <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${formData.postType === 'NORMAL' ? 'border-gray-800' : 'border-gray-300'}`}>
                  {formData.postType === 'NORMAL' && <div className="w-3 h-3 rounded-full bg-gray-900"></div>}
                </div>
                <div className="font-bold text-gray-900">Tin Thường (Standard)</div>
              </div>
              <div className="text-xs text-gray-600 pl-7">Vị trí thấp, trôi nhanh. Giá 20.000đ/tin.</div>
            </label>

            <label className={`relative border-2 rounded-2xl p-4 cursor-pointer transition-all ${formData.postType === 'VIP1' ? 'border-blue-500 bg-blue-50/60' : 'border-gray-200 hover:border-blue-200'}`}>
              <input type="radio" name="pkg" className="hidden" checked={formData.postType === 'VIP1'} onChange={() => setFormData({ ...formData, postType: 'VIP1' })} disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
              <div className="absolute top-0 right-0 bg-blue-600 text-white text-[11px] font-bold px-3 py-1 rounded-bl-xl rounded-tr-lg">VIP Bạc</div>
              <div className="flex items-center gap-2 mb-1">
                <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${formData.postType === 'VIP1' ? 'border-blue-600' : 'border-gray-300'}`}>
                  {formData.postType === 'VIP1' && <div className="w-3 h-3 rounded-full bg-blue-600"></div>}
                </div>
                <div className="font-bold text-blue-700">VIP 1 (Silver)</div>
              </div>
              <div className="text-xs text-gray-600 pl-7">Nổi bật nhẹ, giá rẻ. Vị trí trên tin thường. Giá 50.000đ/tin.</div>
            </label>

            <label className={`relative border-2 rounded-2xl p-4 cursor-pointer transition-all ${formData.postType === 'VIP2' ? 'border-amber-400 bg-amber-50/70 shadow-sm' : 'border-amber-100 hover:border-amber-200'}`}>
              <input type="radio" name="pkg" className="hidden" checked={formData.postType === 'VIP2'} onChange={() => setFormData({ ...formData, postType: 'VIP2' })} disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
              <div className="absolute top-0 right-0 bg-gradient-to-r from-amber-400 to-orange-500 text-white text-[11px] font-bold px-3 py-1 rounded-bl-xl rounded-tr-lg">VIP Vàng ⭐</div>
              <div className="flex items-center gap-2 mb-1">
                <div className={`w-5 h-5 rounded-full border flex items-center justify-center ${formData.postType === 'VIP2' ? 'border-orange-500' : 'border-amber-300'}`}>
                  {formData.postType === 'VIP2' && <div className="w-3 h-3 rounded-full bg-orange-500"></div>}
                </div>
                <div className="font-extrabold text-orange-600 uppercase flex items-center gap-1">VIP 2 (Gold) <span>⭐</span></div>
              </div>
              <div className="text-xs text-gray-700 pl-7">Thu hút mạnh, viền vàng nhạt, tiêu đề cam/đỏ. Giá 100.000đ/tin.</div>
            </label>

            <label className={`relative border-2 rounded-2xl p-4 cursor-pointer transition-all ${formData.postType === 'VIP3' ? 'border-purple-500 bg-purple-50/70 shadow-md' : 'border-purple-100 hover:border-purple-200'}`}>
              <input type="radio" name="pkg" className="hidden" checked={formData.postType === 'VIP3'} onChange={() => setFormData({ ...formData, postType: 'VIP3' })} disabled={postStatus === 'APPROVED' || postStatus === 'REJECTED'} />
              <div className="absolute top-0 right-0 bg-gradient-to-r from-fuchsia-500 via-purple-600 to-pink-500 text-white text-[11px] font-bold px-3 py-1 rounded-bl-xl rounded-tr-lg">VIP Kim Cương</div>
              <div className="flex items-center gap-2 mb-1">
                <div className={`w-6 h-6 rounded-full border flex items-center justify-center ${formData.postType === 'VIP3' ? 'border-fuchsia-600' : 'border-purple-300'}`}>
                  {formData.postType === 'VIP3' && <div className="w-4 h-4 rounded-full bg-fuchsia-600 animate-pulse"></div>}
                </div>
                <div className="font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-purple-600 via-pink-500 to-fuchsia-500 text-lg">VIP 3 (Diamond)</div>
              </div>
              <div className="text-xs text-gray-700 pl-7">Top 1-5 tìm kiếm, gợi ý hôm nay, tiêu đề tím/hồng chữ lớn, ảnh cover to hơn, nút "Gọi ngay" nổi bật. Giá 200.000đ/tin.</div>
            </label>
          </div>
        </div>

        <div className="pt-4 flex gap-4">
          <button type="button" onClick={() => navigate('/partner/my-listings')} className="flex-1 py-3 bg-gray-200 text-gray-700 font-bold rounded-xl hover:bg-gray-300 transition">
            Hủy bỏ
          </button>
          <button type="submit" disabled={loading || postStatus === 'APPROVED' || postStatus === 'REJECTED'} className="flex-1 py-3 bg-indigo-600 text-white font-bold rounded-xl hover:bg-indigo-700 shadow-lg transition-transform active:scale-95 disabled:opacity-50">
            {loading ? 'Đang xử lý...' : 'Cập nhật tin'}
          </button>
        </div>
      </form>
    </div>
  );
}

import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import publicPartnerApi from '../../api/publicPartnerApi';
import userApi from '../../api/userApi';
import MainLayout from '../../components/MainLayout';
import resolveImageUrl from '../../utils/resolveImageUrl';

export default function PartnerPostDetail() {
  const { id } = useParams();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedImage, setSelectedImage] = useState('');

  useEffect(() => {
    const fetchDetail = async () => {
      setLoading(true);
      try {
        const res = await publicPartnerApi.getById(id);
        const data = res?.data?.result || res?.data || res;
                // Fetch partner phone via profile if not present
                let partnerPhone = data?.partnerPhone;
                if (!partnerPhone && data?.partnerId) {
                    try {
                        const prof = await userApi.getPartnerProfile(data.partnerId);
                        const profData = prof?.data?.result || prof?.data || prof;
                        partnerPhone = profData?.phoneNumber || profData?.phone || profData?.contactPhone;
                    } catch (e) {
                        console.warn('Không lấy được số điện thoại đối tác', e);
                    }
                }
                setPost({ ...data, partnerPhone });
        if (data.imageUrls && data.imageUrls.length > 0) {
            setSelectedImage(resolveImageUrl(data.imageUrls[0]));
        }
      } catch (e) {
        console.error('Lỗi tải chi tiết tin đối tác', e);
        setPost(null);
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [id]);

    const getImageFullUrl = (url) => resolveImageUrl(url);

    if (loading) return (
        <MainLayout>
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-[color:var(--app-primary)]"></div>
            </div>
        </MainLayout>
    );

    if (!post) return (
        <MainLayout>
            <div className="container mx-auto px-6 py-20 text-center">
                <h2 className="text-2xl font-bold text-[color:var(--app-text)]">Không tìm thấy bài đăng</h2>
                <Link to="/partner-posts" className="text-[color:var(--app-primary)] hover:underline mt-4 inline-block">Quay lại danh sách</Link>
            </div>
        </MainLayout>
    );

  return (
        <MainLayout>
            <div className="bg-[color:var(--app-bg)] text-[color:var(--app-text)] min-h-screen py-10">
        <div className="container mx-auto px-6 max-w-6xl">
            {/* Breadcrumb */}
            <div className="text-sm text-[color:var(--app-muted)] mb-6 flex gap-2">
                <Link to="/" className="hover:text-[color:var(--app-primary)]">Trang chủ</Link> / 
                <Link to="/partner-posts" className="hover:text-[color:var(--app-primary)]">Tin thuê nhà</Link> / 
                <span className="text-[color:var(--app-text)] font-medium truncate max-w-xs">{post.title}</span>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Cột trái: Hình ảnh & Thông tin chi tiết */}
                <div className="lg:col-span-2 space-y-8">
                    {/* Gallery */}
                    <div className="bg-[color:var(--app-surface-solid)] p-4 rounded-3xl shadow-sm border border-[color:var(--app-border)]">
                        <div className="relative h-[400px] rounded-2xl overflow-hidden mb-4 bg-[color:var(--app-border)]">
                            <img 
                                src={selectedImage || 'https://placehold.co/800x450?text=No+Image'} 
                                alt="Main View" 
                                className="w-full h-full object-cover"
                                onError={(e) => { e.target.src = 'https://placehold.co/800x450?text=Error'; }} 
                            />
                        </div>
                        <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
                            {(post.imageUrls || []).map((url, idx) => {
                                const fullUrl = getImageFullUrl(url);
                                return (
                                    <button 
                                        key={idx} 
                                        onClick={() => setSelectedImage(fullUrl)}
                                        className={`w-24 h-24 rounded-xl overflow-hidden flex-shrink-0 border-2 transition-all ${selectedImage === fullUrl ? 'border-[color:var(--app-primary)] ring-2 ring-[color:var(--app-primary-soft)]' : 'border-transparent opacity-70 hover:opacity-100'}`}
                                    >
                                        <img src={fullUrl} alt={`Thumbnail ${idx}`} className="w-full h-full object-cover" />
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* Nội dung */}
                    <div className="bg-[color:var(--app-surface-solid)] p-8 rounded-3xl shadow-sm border border-[color:var(--app-border)]">
                        <div className="border-b border-[color:var(--app-border)] pb-6 mb-6">
                            <h1 className="text-3xl font-extrabold text-[color:var(--app-text)] leading-tight mb-4">{post.title}</h1>
                            <div className="flex items-center gap-2 text-[color:var(--app-muted)] text-sm">
                                <span>📍</span>
                                <span>{post.address}</span>
                            </div>
                        </div>

                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-8">
                            <div className="bg-[color:var(--app-bg)] p-4 rounded-2xl text-center border border-[color:var(--app-border)]">
                                <p className="text-xs text-[color:var(--app-muted)] uppercase font-bold mb-1">Mức giá</p>
                                <p className="text-[color:var(--app-primary)] font-extrabold text-lg">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(post.price)}</p>
                            </div>
                            <div className="bg-[color:var(--app-bg)] p-4 rounded-2xl text-center border border-[color:var(--app-border)]">
                                <p className="text-xs text-[color:var(--app-muted)] uppercase font-bold mb-1">Diện tích</p>
                                <p className="text-[color:var(--app-text)] font-bold text-lg">{post.area} m²</p>
                            </div>
                            <div className="bg-[color:var(--app-bg)] p-4 rounded-2xl text-center border border-[color:var(--app-border)]">
                                <p className="text-xs text-[color:var(--app-muted)] uppercase font-bold mb-1">Ngày đăng</p>
                                <p className="text-[color:var(--app-text)] font-bold text-lg">{new Date(post.createdAt).toLocaleDateString('vi-VN')}</p>
                            </div>
                        </div>

                        <h3 className="text-xl font-bold text-[color:var(--app-text)] mb-4">Thông tin mô tả</h3>
                        <div className="prose max-w-none text-[color:var(--app-muted)] leading-relaxed whitespace-pre-line bg-[color:var(--app-bg)] p-6 rounded-2xl border border-[color:var(--app-border)]">
                            {post.description}
                        </div>
                    </div>
                </div>

                {/* Cột phải: Thông tin liên hệ */}
                <div className="lg:col-span-1 space-y-6">
                    <div className="bg-[color:var(--app-surface-solid)] p-6 rounded-3xl shadow-lg border border-[color:var(--app-border)] sticky top-24">
                        <div className="flex items-center gap-4 mb-6">
                            <div className="w-16 h-16 bg-gradient-to-br from-[color:var(--app-primary)] to-[color:var(--app-hero-to)] rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-md">
                                {post.partnerName ? post.partnerName.charAt(0).toUpperCase() : 'U'}
                            </div>
                            <div>
                                <p className="text-xs text-[color:var(--app-muted)] uppercase font-bold">Được đăng bởi</p>
                                <h3 className="text-lg font-bold text-[color:var(--app-text)] line-clamp-1">{post.partnerName || 'Đối tác'}</h3>
                                <div className="flex items-center gap-1 mt-1">
                                    <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                                    <span className="text-xs text-green-600 font-medium">Đang hoạt động</span>
                                </div>
                            </div>
                        </div>

                        <div className="space-y-3">
                            <button className="w-full py-4 bg-green-600 hover:bg-green-700 text-white font-bold rounded-xl shadow-lg shadow-green-200 transition-all flex items-center justify-center gap-2 transform active:scale-95">
                                <span>📞</span>
                                <span>{post.partnerPhone || 'Đang cập nhật'}</span>
                            </button>
                            <button className="w-full py-4 bg-[color:var(--app-surface-solid)] border-2 border-[color:var(--app-primary)] text-[color:var(--app-primary)] font-bold rounded-xl hover:bg-[color:var(--app-primary-soft)] transition-all flex items-center justify-center gap-2">
                                <span>💬</span> Nhắn tin Zalo
                            </button>
                        </div>
                        
                        <div className="mt-6 pt-6 border-t border-[color:var(--app-border)] text-center">
                            <p className="text-xs text-[color:var(--app-muted-2)] mb-2">An toàn khi giao dịch</p>
                            <div className="flex justify-center gap-4 text-2xl grayscale opacity-50">
                                🛡️ 🤝 📋
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
            </div>
        </MainLayout>
  );
}
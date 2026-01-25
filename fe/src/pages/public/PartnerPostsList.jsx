import React, { useEffect, useState } from 'react';
import publicPartnerApi from '../../api/publicPartnerApi';
import PartnerPostCard from '../../components/PartnerPostCard';

export default function PartnerPostsList() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const res = await publicPartnerApi.list({ page, size, sort: 'createdAt,desc' });
      const data = res?.data?.result || res?.data || res;
      const content = data?.content || [];
      setPosts(content);
      setTotalPages(data?.totalPages || 0);
      setTotalElements(data?.totalElements || content.length);
    } catch (e) {
      console.error('Lỗi tải tin đối tác', e);
      setPosts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [page, size]);

  return (
      <div className="bg-[color:var(--app-bg)] text-[color:var(--app-text)] min-h-screen pb-20">
        {/* Banner Header */}
        <div className="bg-gradient-to-r from-[color:var(--app-hero-from)] to-[color:var(--app-hero-to)] text-white py-16 mb-10">
            <div className="container mx-auto px-6 text-center">
                <h1 className="text-3xl md:text-4xl font-extrabold mb-4">Tin Thuê Nhà & Phòng Trọ</h1>
                <p className="text-white/85 text-lg max-w-2xl mx-auto">
                  Khám phá hàng trăm tin đăng cho thuê phòng trọ, căn hộ mini, nhà nguyên căn từ các đối tác uy tín của Alpha.
                </p>
            </div>
        </div>

        <div className="container mx-auto px-6">
          <div className="flex flex-col md:flex-row justify-between items-center mb-8 gap-4">
          <h2 className="text-xl font-bold text-[color:var(--app-text)] flex items-center gap-2">
                Danh sách tin đăng
            <span className="bg-[color:var(--app-primary-soft)] text-[color:var(--app-primary)] text-xs px-2 py-1 rounded-full">{totalElements} tin</span>
            </h2>
            
            <div className="flex items-center gap-2">
            <span className="text-sm text-[color:var(--app-muted)]">Hiển thị:</span>
                <select 
                    value={size} 
                    onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }} 
              className="border border-[color:var(--app-border-strong)] bg-[color:var(--app-surface-solid)] rounded-lg px-3 py-1.5 text-sm outline-none focus:ring-2 focus:ring-[color:var(--app-primary-soft)]"
                >
                    <option value={8}>8 tin</option>
                    <option value={12}>12 tin</option>
                    <option value={20}>20 tin</option>
                </select>
            </div>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {Array.from({ length: 8 }).map((_, i) => (
                <div key={i} className="bg-[color:var(--app-surface-solid)] rounded-2xl h-80 shadow-sm border border-[color:var(--app-border)] animate-pulse">
                    <div className="h-48 bg-[color:var(--app-border)] rounded-t-2xl"></div>
                    <div className="p-4 space-y-3">
                        <div className="h-4 bg-[color:var(--app-border)] rounded w-3/4"></div>
                        <div className="h-4 bg-[color:var(--app-border)] rounded w-1/2"></div>
                    </div>
                </div>
              ))}
            </div>
          ) : posts.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
              {posts.map((post) => (
                <PartnerPostCard key={post.id} post={post} />
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-[color:var(--app-surface-solid)] rounded-2xl border border-dashed border-[color:var(--app-border-strong)]">
              <div className="w-20 h-20 bg-[color:var(--app-primary-soft)] rounded-full flex items-center justify-center mx-auto mb-4 text-4xl grayscale opacity-50">📰</div>
              <h3 className="text-lg font-bold text-[color:var(--app-text)]">Chưa có tin đăng nào</h3>
              <p className="text-[color:var(--app-muted)]">Hiện tại chưa có đối tác nào đăng tin. Vui lòng quay lại sau.</p>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center mt-12 gap-2">
              <button 
                className="px-4 py-2 rounded-lg border border-[color:var(--app-border-strong)] text-sm font-medium hover:bg-[color:var(--app-primary-soft)] disabled:opacity-50" 
                disabled={page <= 0} 
                onClick={() => setPage((p) => Math.max(p - 1, 0))}
              >
                ← Trước
              </button>
              
              {Array.from({ length: totalPages }, (_, i) => i)
                .filter(i => i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 2)
                .map((i, idx, arr) => {
                  const prev = arr[idx - 1];
                  const needDots = prev !== undefined && i - prev > 1;
                  return (
                    <React.Fragment key={i}>
                      {needDots && <span className="px-2 py-2 text-[color:var(--app-muted-2)]">...</span>}
                      <button 
                        className={`w-10 h-10 rounded-lg text-sm font-bold transition-all ${i === page ? 'bg-[color:var(--app-primary)] text-white shadow-md' : 'bg-[color:var(--app-surface-solid)] border border-[color:var(--app-border-strong)] hover:bg-[color:var(--app-primary-soft)] text-[color:var(--app-text)]'}`} 
                        onClick={() => setPage(i)}
                      >
                        {i + 1}
                      </button>
                    </React.Fragment>
                  );
                })}

              <button 
                className="px-4 py-2 rounded-lg border border-[color:var(--app-border-strong)] text-sm font-medium hover:bg-[color:var(--app-primary-soft)] disabled:opacity-50" 
                disabled={page >= totalPages - 1} 
                onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
              >
                Sau →
              </button>
            </div>
          )}
        </div>
      </div>
  );
}
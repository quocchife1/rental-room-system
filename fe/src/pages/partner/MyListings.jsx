import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import partnerApi from '../../api/partnerApi';
import resolveImageUrl from '../../utils/resolveImageUrl';

export default function MyListings() {
  const [listings, setListings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    fetchListings(page, size);
  }, [page, size]);

  const fetchListings = async (pageParam = 0, sizeParam = 10) => {
    try {
      // Prefer paginated endpoint; fall back to old list if needed
      let res = await partnerApi.getMyPostsPaged({ page: pageParam, size: sizeParam, sort: 'createdAt,desc' });
      let data = res?.data || res?.result || res;

      if (data && Array.isArray(data.content)) {
        setListings(data.content);
        setTotalPages(typeof data.totalPages === 'number' ? data.totalPages : 0);
        setTotalElements(typeof data.totalElements === 'number' ? data.totalElements : data.content.length);
        // Normalize current page/size if backend returns them
        if (typeof data.number === 'number') setPage(data.number);
        if (typeof data.size === 'number') setSize(data.size);
      } else {
        // Fallback: non-paged array response
        const arr = Array.isArray(data) ? data : [];
        setListings(arr);
        setTotalPages(1);
        setTotalElements(arr.length);
        setPage(0);
      }
    } catch (err) {
      console.error('Lỗi tải danh sách tin:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa tin này? Lưu ý: không hỗ trợ hoàn tiền sau khi xóa tin.')) return;
    try {
      await partnerApi.deletePost(id);
      alert('Đã xóa tin thành công');
      fetchListings();
    } catch (err) {
      alert('Lỗi xóa tin: ' + (err.response?.data?.message || 'Lỗi server'));
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      'APPROVED': 'bg-emerald-100 text-emerald-700 border-emerald-200',
      'ACTIVE': 'bg-emerald-100 text-emerald-700 border-emerald-200',
      'PENDING_APPROVAL': 'bg-amber-100 text-amber-700 border-amber-200',
      'PENDING': 'bg-amber-100 text-amber-700 border-amber-200',
      'PENDING_PAYMENT': 'bg-rose-100 text-rose-700 border-rose-200',
      'REJECTED': 'bg-rose-100 text-rose-700 border-rose-200',
      'EXPIRED': 'bg-gray-100 text-gray-600 border-gray-200'
    };
    const labels = {
      'APPROVED': 'Đang hiển thị',
      'ACTIVE': 'Đang hiển thị',
      'PENDING_APPROVAL': 'Chờ duyệt',
      'PENDING': 'Chờ duyệt',
      'PENDING_PAYMENT': 'Chưa thanh toán',
      'REJECTED': 'Bị từ chối',
      'EXPIRED': 'Hết hạn'
    };
    const key = status ? status.toUpperCase() : 'PENDING';
    return (
      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${styles[key] || styles['EXPIRED']} inline-flex items-center gap-1.5`}>
        <span className={`w-1.5 h-1.5 rounded-full ${key === 'APPROVED' || key === 'ACTIVE' ? 'bg-emerald-500 animate-pulse' : 'bg-current'}`}></span>
        {labels[key] || status}
      </span>
    );
  };

  const getPackageBadge = (postType) => {
    const config = {
      'NORMAL': { label: 'Tin Thường', bg: 'bg-gray-100', text: 'text-gray-700', icon: '📄' },
      'VIP1': { label: 'VIP 1 (Silver)', bg: 'bg-blue-100', text: 'text-blue-700', icon: '🟦' },
      'VIP2': { label: 'VIP 2 (Gold)', bg: 'bg-amber-100', text: 'text-amber-700', icon: '⭐' },
      'VIP3': { label: 'VIP 3 (Diamond)', bg: 'bg-purple-100', text: 'text-purple-700', icon: '💎' }
    };
    const pkg = config[postType] || config['NORMAL'];
    return (
      <span className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold ${pkg.bg} ${pkg.text} border border-current border-opacity-20`}>
        <span>{pkg.icon}</span>
        {pkg.label}
      </span>
    );
  };

  // Helper để lấy ảnh đầu tiên hoặc ảnh placeholder
  const getThumbnail = (item) => {
    // API có thể trả về imageUrls (List<String>) hoặc images (List<Object>)
    if (item.imageUrls && item.imageUrls.length > 0) return `http://localhost:8080${item.imageUrls[0]}`;
    if (item.images && item.images.length > 0) return `http://localhost:8080${item.images[0].imageUrl || item.images[0]}`;
    return 'https://placehold.co/100?text=NoImage';
      // API có thể trả về imageUrls (List<String>) hoặc images (List<Object>)
      if (item.imageUrls && item.imageUrls.length > 0) return resolveImageUrl(item.imageUrls[0]);
      if (item.images && item.images.length > 0) return resolveImageUrl(item.images[0].imageUrl || item.images[0]);
      return 'https://placehold.co/100?text=NoImage';
  };

  if (loading) return <div className="p-10 text-center text-gray-500">Đang tải dữ liệu...</div>;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-end border-b border-gray-100 pb-5">
        <div>
          <h1 className="text-3xl font-bold text-gray-800 tracking-tight">Quản lý tin đăng</h1>
          <p className="text-gray-500 mt-1">Danh sách các phòng bạn đang cho thuê.</p>
        </div>
        <Link to="/partner/create-listing" className="bg-indigo-600 text-white px-5 py-2.5 rounded-xl font-bold hover:bg-indigo-700 shadow-lg shadow-indigo-200 transition-transform active:scale-95 flex items-center gap-2">
          <span>＋</span> Đăng tin mới
        </Link>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
        {listings.length > 0 ? (
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/50 border-b border-gray-100 text-xs uppercase text-gray-500 font-bold tracking-wider">
                <th className="px-6 py-4 w-24">Ảnh</th>
                <th className="px-6 py-4">Thông tin tin đăng</th>
                <th className="px-6 py-4 text-center">Gói tin</th>
                <th className="px-6 py-4 text-center">Trạng thái</th>
                <th className="px-6 py-4 text-center">Lượt xem</th>
                <th className="px-6 py-4 text-right">Hành động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {listings.map(item => (
                <tr key={item.id} className="hover:bg-indigo-50/30 transition-colors group">
                  <td className="px-6 py-4">
                    <div className="w-16 h-16 rounded-lg overflow-hidden border border-gray-200 shadow-sm relative bg-gray-100">
                      <img
                        src={getThumbnail(item)}
                        alt="Thumb"
                        className="w-full h-full object-cover"
                        onError={(e) => { e.target.src = 'https://placehold.co/100?text=Error' }}
                      />
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <h4 className="font-bold text-gray-900 group-hover:text-indigo-600 transition-colors line-clamp-1">{item.title}</h4>
                    <p className="text-indigo-600 font-bold text-sm mt-1">{item.price?.toLocaleString()} đ/tháng</p>
                    <p className="text-xs text-gray-400 mt-1">Ngày đăng: {new Date(item.createdAt || Date.now()).toLocaleDateString('vi-VN')}</p>
                  </td>
                  <td className="px-6 py-4 text-center">
                    {getPackageBadge(item.postType)}
                  </td>
                  <td className="px-6 py-4 text-center">
                    {getStatusBadge(item.status)}
                    {item.status === 'PENDING_PAYMENT' && item.paymentUrl && (
                      <a
                        href={item.paymentUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="mt-2 inline-block text-indigo-600 font-bold hover:underline"
                      >
                        Thanh toán ngay →
                      </a>
                    )}
                    {item.status === 'REJECTED' && item.rejectReason && (
                      <div className="mt-2 text-xs text-left bg-red-50 border border-red-100 text-red-700 rounded-lg px-3 py-2 inline-block max-w-xs">
                        <div className="font-semibold">Lý do bị từ chối</div>
                        <div className="line-clamp-3 whitespace-pre-line">{item.rejectReason}</div>
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 text-center">
                    <div className="inline-flex flex-col items-center">
                      <span className="text-lg font-bold text-gray-800">{item.views || 0}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex justify-end gap-2">
                      {(item.status !== 'APPROVED' && item.status !== 'REJECTED') ? (
                        <Link to={`/partner/edit-listing/${item.id}`} className="p-2 text-gray-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors" title="Chỉnh sửa">
                          ✎
                        </Link>
                      ) : (
                        <button className="p-2 text-gray-300 cursor-not-allowed rounded-lg" title="Không thể sửa tin đã hiển thị hoặc bị từ chối" disabled>
                          ✎
                        </button>
                      )}
                      <button onClick={() => handleDelete(item.id)} className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors" title="Xóa">
                        🗑
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="text-center py-20">
            <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4 text-4xl grayscale opacity-50">📭</div>
            <h3 className="text-gray-900 font-bold text-lg">Chưa có tin đăng nào</h3>
            <p className="text-gray-500 text-sm mt-1">Hãy đăng tin ngay để tiếp cận hàng ngàn khách hàng tiềm năng.</p>
            <Link to="/partner/create-listing" className="text-indigo-600 font-bold hover:underline mt-4 inline-block">Đăng tin ngay &rarr;</Link>
          </div>
        )}
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between bg-white p-4 rounded-xl border border-gray-100 shadow-sm">
          <div className="text-sm text-gray-500">
            Hiển thị {Math.min(page * size + 1, totalElements)}–{Math.min((page + 1) * size, totalElements)} trong {totalElements}
          </div>
          <div className="flex items-center gap-2">
            <button
              className="px-3 py-2 rounded-lg border text-sm font-medium disabled:opacity-50"
              onClick={() => setPage((p) => Math.max(p - 1, 0))}
              disabled={page <= 0}
            >
              ← Trước
            </button>
            {/* Page numbers (compact) */}
            {Array.from({ length: totalPages }, (_, i) => i)
              .filter(i => i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 2)
              .map((i, idx, arr) => {
                const prev = arr[idx - 1];
                const needDots = prev !== undefined && i - prev > 1;
                return (
                  <React.Fragment key={i}>
                    {needDots && <span className="px-1 text-gray-400">…</span>}
                    <button
                      className={`px-3 py-2 rounded-lg border text-sm font-bold ${i === page ? 'bg-indigo-600 text-white border-indigo-600' : 'hover:bg-gray-100'}`}
                      onClick={() => setPage(i)}
                    >
                      {i + 1}
                    </button>
                  </React.Fragment>
                );
              })}
            <button
              className="px-3 py-2 rounded-lg border text-sm font-medium disabled:opacity-50"
              onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
              disabled={page >= totalPages - 1}
            >
              Sau →
            </button>
            <select
              value={size}
              onChange={(e) => setSize(Number(e.target.value))}
              className="ml-2 border rounded-lg px-2 py-2 text-sm"
            >
              {[5, 10, 20, 50].map(opt => (
                <option key={opt} value={opt}>{opt}/trang</option>
              ))}
            </select>
          </div>
        </div>
      )}
    </div>
  );
}
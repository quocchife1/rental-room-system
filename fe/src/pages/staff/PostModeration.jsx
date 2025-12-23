import React, { useEffect, useState } from 'react';
import staffApi from '../../api/staffApi';
import resolveImageUrl from '../../utils/resolveImageUrl';

export default function PostModeration() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState(null); // detail
  const [detailLoading, setDetailLoading] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [status, setStatus] = useState('PENDING_APPROVAL');
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedIds, setSelectedIds] = useState([]);
  const [stats, setStats] = useState({ pending: 0, approved: 0, rejected: 0, approvedToday: 0 });

  // Normalize stats payload from API (handles axios response wrapping)
  const parseStats = (res) => {
    const data = res?.data?.result || res?.data || res || {};
    return {
      pending: data.pending || 0,
      approved: data.approved || 0,
      rejected: data.rejected || 0,
      approvedToday: data.approvedToday || 0
    };
  };

  const fetchPosts = async () => {
    setLoading(true);
    try {
      const res = await staffApi.getManagementPosts({ status, q, page, size: 12 });
      const data = res?.data?.content ? res.data : res?.data?.result || res?.data || res;
      setPosts(data?.content || []);
      setTotalPages(data?.totalPages || 0);

    } catch (e) {
      console.error('Lỗi tải tin chờ duyệt', e);
      setPosts([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { setPage(0); }, [status, q]);
  useEffect(() => { fetchPosts(); }, [status, page]);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await staffApi.getModerationStats?.();
        setStats(parseStats(res));
      } catch (e) { /* ignore */ }
    };
    fetchStats();
  }, [status, q, page]);

  // Helpers for nicer UI labels
  const getStatusMeta = (status) => {
    switch (status) {
      case 'APPROVED':
        return { label: 'Đã duyệt', cls: 'bg-emerald-50 text-emerald-700 border border-emerald-100' };
      case 'REJECTED':
        return { label: 'Từ chối', cls: 'bg-red-50 text-red-700 border border-red-100' };
      case 'PENDING_APPROVAL':
      default:
        return { label: 'Chờ duyệt', cls: 'bg-amber-50 text-amber-700 border border-amber-100' };
    }
  };

  const getTypeMeta = (type) => {
    switch (type) {
      case 'VIP1':
        return { label: 'VIP 1 (Bạc)', cls: 'bg-blue-50 text-blue-700 border border-blue-100' };
      case 'VIP2':
        return { label: 'VIP 2 (Vàng)', cls: 'bg-amber-50 text-amber-700 border border-amber-100' };
      case 'VIP3':
        return { label: 'VIP 3 (Kim cương)', cls: 'bg-purple-50 text-purple-700 border border-purple-100' };
      case 'NORMAL':
      default:
        return { label: 'Tin thường', cls: 'bg-gray-50 text-gray-700 border border-gray-100' };
    }
  };

  const approve = async (id) => {
    try {
      await staffApi.approvePost?.(id);
      // After action, refetch posts and stats
      await fetchPosts();
      try { const s = await staffApi.getModerationStats?.(); setStats(parseStats(s)); } catch { }
      setSelected(null);
    } catch (e) { alert('Không thể duyệt tin'); }
  };
  const reject = async (id) => {
    const reason = rejectReason || prompt('Nhập lý do từ chối:');
    try {
      await staffApi.rejectPost?.(id, reason);
      await fetchPosts();
      try { const s = await staffApi.getModerationStats?.(); setStats(parseStats(s)); } catch { }
      setSelected(null);
    } catch (e) { alert('Không thể từ chối'); }
  };

  const openDetail = async (id) => {
    setDetailLoading(true);
    setRejectReason('');
    try {
      const res = await staffApi.getManagementPostById(id);
      const data = res?.data?.result || res?.data || res;
      setSelected(data);
    } catch (e) { console.error('Không tải được chi tiết', e); }
    finally { setDetailLoading(false); }
  };

  const modalStatus = selected ? getStatusMeta(selected.status || status) : null;

  return (
    <div className="container mx-auto px-6 py-8">
      {/** Helper to show Vietnamese labels for post type */}
      {/** postTypes: NORMAL, VIP1, VIP2, VIP3 */}
      {/** Consider moving to a shared util if reused */}
      <h1 className="text-2xl font-bold mb-6">Duyệt tin đối tác</h1>
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
        <div className="bg-white border rounded-xl p-3 text-sm">
          <div className="text-gray-500">Chờ duyệt</div>
          <div className="text-xl font-semibold text-yellow-700">{stats.pending}</div>
        </div>
        <div className="bg-white border rounded-xl p-3 text-sm">
          <div className="text-gray-500">Đã duyệt</div>
          <div className="text-xl font-semibold text-emerald-700">{stats.approved}</div>
        </div>
        <div className="bg-white border rounded-xl p-3 text-sm">
          <div className="text-gray-500">Từ chối</div>
          <div className="text-xl font-semibold text-red-700">{stats.rejected}</div>
        </div>
        <div className="bg-white border rounded-xl p-3 text-sm">
          <div className="text-gray-500">Duyệt hôm nay</div>
          <div className="text-xl font-semibold text-indigo-700">{stats.approvedToday}</div>
        </div>
      </div>
      <div className="flex flex-col md:flex-row md:items-center gap-3 mb-6">
        <div className="inline-flex rounded-lg border overflow-hidden">
          {[
            { key: 'PENDING_APPROVAL', label: 'Chờ duyệt' },
            { key: 'APPROVED', label: 'Đã duyệt' },
            { key: 'REJECTED', label: 'Từ chối' }
          ].map(tab => (
            <button
              key={tab.key}
              className={`px-4 py-2 text-sm ${status === tab.key ? 'bg-indigo-600 text-white' : 'bg-white hover:bg-gray-50'}`}
              onClick={() => setStatus(tab.key)}
            >{tab.label}</button>
          ))}
        </div>
        <div className="flex items-center gap-2 md:ml-auto">
          <input
            type="text"
            value={q}
            onChange={e => setQ(e.target.value)}
            onKeyDown={e => { if (e.key === 'Enter') { fetchPosts(); } }}
            placeholder="Tìm theo tiêu đề..."
            className="w-full md:w-64 border rounded-lg px-3 py-2"
          />
          <button className="px-4 py-2 rounded bg-gray-800 text-white" onClick={fetchPosts}>Tìm</button>
          {(status === 'PENDING_APPROVAL' || status === 'REJECTED') && selectedIds.length > 0 && (
            <>
              {status === 'PENDING_APPROVAL' && (
                <button className="px-4 py-2 rounded bg-emerald-600 text-white" onClick={async () => {
                  try { await staffApi.approvePostsBatch(selectedIds); setSelectedIds([]); await fetchPosts(); try { const s = await staffApi.getModerationStats?.(); setStats(parseStats(s)); } catch { } } catch (e) { alert('Không thể duyệt hàng loạt'); }
                }}>Duyệt hàng loạt ({selectedIds.length})</button>
              )}
              {status !== 'APPROVED' && (
                <button className="px-4 py-2 rounded bg-red-600 text-white" onClick={async () => {
                  const reason = prompt('Lý do từ chối hàng loạt:') || '';
                  try { await staffApi.rejectPostsBatch(selectedIds, reason); setSelectedIds([]); await fetchPosts(); try { const s = await staffApi.getModerationStats?.(); setStats(parseStats(s)); } catch { } } catch (e) { alert('Không thể từ chối hàng loạt'); }
                }}>Từ chối hàng loạt ({selectedIds.length})</button>
              )}
            </>
          )}
        </div>
      </div>
      {loading ? (
        <div>Tải dữ liệu...</div>
      ) : posts.length === 0 ? (
        <div className="bg-white border rounded-xl p-6 text-center text-gray-600">Không có bản ghi phù hợp bộ lọc.</div>
      ) : (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {posts.map(post => {
              const statusMeta = getStatusMeta(post.status);
              const typeMeta = getTypeMeta(post.postType);
              const created = post.createdAt ? new Date(post.createdAt).toLocaleString('vi-VN') : '—';
              const approved = post.approvedAt ? new Date(post.approvedAt).toLocaleString('vi-VN') : null;

              return (
                <div key={post.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-all p-5 flex flex-col gap-3">
                  <div className="flex items-center justify-between">
                    <label className="flex items-center gap-2 text-xs text-gray-500">
                      <input type="checkbox" checked={selectedIds.includes(post.id)} onChange={(e) => {
                        const checked = e.target.checked;
                        setSelectedIds(prev => checked ? [...new Set([...prev, post.id])] : prev.filter(id => id !== post.id));
                      }} />
                      <span>Chọn</span>
                    </label>
                    <div className="flex flex-col items-end gap-1">
                      <span className={`text-xs px-2 py-1 rounded-full ${statusMeta.cls}`}>{statusMeta.label}</span>
                      <span className={`text-[11px] px-2 py-1 rounded-full ${typeMeta.cls}`}>{typeMeta.label}</span>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <h3 className="font-semibold text-gray-900 text-base leading-snug line-clamp-2">{post.title}</h3>
                    <p className="text-sm text-gray-600 line-clamp-3">{post.description}</p>
                  </div>

                  <div className="grid grid-cols-2 gap-2 text-xs text-gray-600">
                    <div className="bg-gray-50 rounded-lg px-3 py-2 border border-gray-100 flex items-center justify-between">
                      <span>Giá</span>
                      <b className="text-indigo-600">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(post.price)}</b>
                    </div>
                    <div className="bg-gray-50 rounded-lg px-3 py-2 border border-gray-100 flex items-center justify-between">
                      <span>Diện tích</span>
                      <b>{post.area} m²</b>
                    </div>
                  </div>

                  <div className="text-xs text-gray-600 bg-gray-50 rounded-lg px-3 py-2 border border-gray-100">
                    <div className="font-medium text-gray-800">{post.partnerName}</div>
                    <div className="flex justify-between"><span>SĐT</span><span>{post.partnerPhone || '—'}</span></div>
                  </div>

                  <div className="text-[11px] text-gray-500 flex flex-col gap-1">
                    <span>Tạo: {created}</span>
                    {approved && <span>Duyệt: {approved}</span>}
                  </div>

                  {post.status === 'REJECTED' && post.rejectReason && (
                    <div className="text-xs text-red-700 bg-red-50 border border-red-100 rounded-lg px-3 py-2">
                      <div className="font-semibold">Lý do bị từ chối</div>
                      <div className="line-clamp-3">{post.rejectReason}</div>
                    </div>
                  )}

                  <div className="flex items-center justify-between mt-auto pt-1">
                    <div className="text-indigo-600 font-bold text-sm">#{post.id}</div>
                    <div className="flex gap-2">
                      <button className="px-3 py-1.5 rounded-lg border border-gray-200 text-sm text-gray-700 hover:bg-gray-50" onClick={() => openDetail(post.id)}>Xem</button>
                      {status !== 'APPROVED' && (
                        <button className="px-3 py-1.5 rounded-lg bg-emerald-600 text-white text-sm hover:bg-emerald-700" onClick={() => approve(post.id)}>Duyệt</button>
                      )}
                      {status !== 'REJECTED' && (
                        <button className="px-3 py-1.5 rounded-lg bg-red-600 text-white text-sm hover:bg-red-700" onClick={() => reject(post.id)}>Từ chối</button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
          <div className="flex items-center justify-end gap-2 mt-6">
            <button
              className="px-3 py-1 rounded border"
              disabled={page <= 0}
              onClick={() => setPage(p => Math.max(0, p - 1))}
            >Trang trước</button>
            <span className="text-sm text-gray-600">Trang {page + 1}/{Math.max(1, totalPages || 1)}</span>
            <button
              className="px-3 py-1 rounded border"
              disabled={totalPages === 0 || page >= totalPages - 1}
              onClick={() => setPage(p => p + 1)}
            >Trang sau</button>
          </div>
        </>
      )}

      {selected && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-3xl w-full max-w-5xl shadow-2xl overflow-hidden">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <div>
                <p className="text-xs uppercase tracking-wide text-gray-400">Kiểm tra tin</p>
                <h2 className="text-xl font-bold text-gray-800">#{selected.id} — {selected.title}</h2>
              </div>
              <button className="text-gray-500 hover:text-gray-700 text-lg" onClick={() => setSelected(null)}>✕</button>
            </div>

            {detailLoading ? (
              <div className="p-10 text-center text-gray-500">Đang tải chi tiết...</div>
            ) : (
              <div className="max-h-[78vh] overflow-y-auto grid grid-cols-1 lg:grid-cols-3 gap-6 p-6 pr-4">
                <div className="lg:col-span-2 space-y-4">
                  <div className="h-72 bg-gray-100 rounded-2xl overflow-hidden border border-gray-100">
                    <img src={selected.imageUrls?.[0] ? `http://localhost:8080${selected.imageUrls[0]}` : 'https://placehold.co/1200x600?text=No+Image'} alt="" className="w-full h-full object-cover" />
                  </div>
                  <div className="flex gap-3 overflow-x-auto pb-1">
                    {(selected.imageUrls || []).map((u, i) => (
                      <img key={i} src={`http://localhost:8080${u}`} alt="thumb" className="w-24 h-24 object-cover rounded-lg border border-gray-200" />
                    ))}
                  </div>

                  <div className="space-y-2">
                    <div className="flex items-center justify-between flex-wrap gap-2">
                      <h3 className="text-lg font-semibold text-gray-900 leading-snug">{selected.title}</h3>
                      <div className="flex items-center gap-2 text-xs">
                        <span className="px-3 py-1 rounded-full bg-gray-50 border border-gray-200 text-gray-700">{selected.postType}</span>
                        {modalStatus && (
                          <span className={`px-3 py-1 rounded-full ${modalStatus.cls}`}>{modalStatus.label}</span>
                        )}
                      </div>
                    </div>
                    <p className="text-sm text-gray-600">{selected.address}</p>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-sm">
                    <div className="bg-gray-50 p-3 rounded-xl border border-gray-100 flex items-center justify-between">
                      <span className="text-gray-500">Giá</span>
                      <b className="text-indigo-700">{new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(selected.price)}</b>
                    </div>
                    <div className="bg-gray-50 p-3 rounded-xl border border-gray-100 flex items-center justify-between">
                      <span className="text-gray-500">Diện tích</span>
                      <b>{selected.area} m²</b>
                    </div>
                    <div className="bg-gray-50 p-3 rounded-xl border border-gray-100 flex items-center justify-between">
                      <span className="text-gray-500">Đối tác</span>
                      <b className="text-right">{selected.partnerName}</b>
                    </div>
                  </div>

                  <div className="bg-gray-50 p-3 rounded-xl border border-gray-100 text-sm">
                    <div className="flex justify-between text-gray-600">
                      <span>SĐT liên hệ</span>
                      <b>{selected.partnerPhone || '—'}</b>
                    </div>
                    <div className="flex justify-between text-gray-500 text-xs mt-1">
                      <span>Tạo: {selected.createdAt ? new Date(selected.createdAt).toLocaleString('vi-VN') : '—'}</span>
                      {selected.approvedAt && <span>Duyệt: {new Date(selected.approvedAt).toLocaleString('vi-VN')}</span>}
                    </div>
                  </div>

                  <div className="p-3 rounded-xl border border-gray-100 bg-white text-sm leading-relaxed max-h-48 overflow-auto whitespace-pre-line">
                    {selected.description}
                  </div>
                </div>

                <div className="lg:col-span-1 space-y-3">
                  {selected.status === 'REJECTED' && selected.rejectReason && (
                    <div className="bg-red-50 border border-red-100 text-red-800 rounded-xl p-3 text-sm">
                      <div className="font-semibold mb-1">Lý do bị từ chối</div>
                      <div className="whitespace-pre-line">{selected.rejectReason}</div>
                    </div>
                  )}
                  <label className="block text-sm font-semibold text-gray-800">Lý do từ chối (nếu có)</label>
                  <textarea className="w-full border border-gray-200 rounded-xl p-3 min-h-[140px] text-sm focus:ring-2 focus:ring-indigo-200 focus:border-indigo-300" value={rejectReason} onChange={e => setRejectReason(e.target.value)} placeholder="Ví dụ: ảnh không rõ ràng, thông tin giá chưa hợp lệ" />
                  <div className="grid grid-cols-1 gap-2 pt-2">
                    <button className="px-4 py-2.5 rounded-xl bg-emerald-600 text-white font-semibold hover:bg-emerald-700" onClick={() => approve(selected.id)}>Duyệt tin</button>
                    <button className="px-4 py-2.5 rounded-xl bg-red-600 text-white font-semibold hover:bg-red-700" onClick={() => reject(selected.id)}>Từ chối tin</button>
                    <button className="px-4 py-2.5 rounded-xl bg-gray-100 text-gray-700 font-semibold hover:bg-gray-200" onClick={() => setSelected(null)}>Đóng</button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

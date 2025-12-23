import React, { useEffect, useState } from 'react';
import auditApi from '../../api/auditApi';

const ACTION_OPTIONS = [
  { value: '', label: 'Tất cả hành động' },
  { value: 'LOGIN_SUCCESS', label: 'Đăng nhập thành công' },
  { value: 'LOGIN_FAILED', label: 'Đăng nhập thất bại' },
  { value: 'LOGOUT', label: 'Đăng xuất' },
  { value: 'REGISTER_GUEST', label: 'Đăng ký (khách)' },
  { value: 'REGISTER_TENANT', label: 'Đăng ký (người thuê)' },
  { value: 'REGISTER_PARTNER', label: 'Đăng ký (đối tác)' },
  { value: 'REGISTER_EMPLOYEE', label: 'Đăng ký (nhân viên)' },
  { value: 'CREATE_CONTRACT', label: 'Tạo hợp đồng' },
  { value: 'UPDATE_CONTRACT', label: 'Cập nhật hợp đồng' },
  { value: 'EXTEND_CONTRACT', label: 'Gia hạn hợp đồng' },
  { value: 'TERMINATE_CONTRACT', label: 'Chấm dứt hợp đồng' },
  { value: 'SIGN_CONTRACT', label: 'Ký hợp đồng' },
  { value: 'CREATE_INVOICE', label: 'Tạo hóa đơn' },
  { value: 'UPDATE_INVOICE', label: 'Cập nhật hóa đơn' },
  { value: 'CONFIRM_PAYMENT', label: 'Xác nhận thanh toán' },
  { value: 'REJECT_PAYMENT', label: 'Từ chối thanh toán' },
  { value: 'CANCEL_INVOICE', label: 'Hủy hóa đơn' },
  { value: 'UPDATE_PRICE', label: 'Cập nhật giá' },
  { value: 'ADD_SERVICE', label: 'Thêm dịch vụ' },
  { value: 'REMOVE_SERVICE', label: 'Xóa dịch vụ' },
  { value: 'CREATE_TENANT', label: 'Tạo người thuê' },
  { value: 'UPDATE_TENANT', label: 'Cập nhật người thuê' },
  { value: 'BAN_TENANT', label: 'Khóa/Ban người thuê' },
  { value: 'UNBAN_TENANT', label: 'Mở khóa/Unban người thuê' },
  { value: 'CREATE_EMPLOYEE', label: 'Tạo nhân viên' },
  { value: 'UPDATE_EMPLOYEE', label: 'Cập nhật nhân viên' },
  { value: 'CREATE_PARTNER_POST', label: 'Tạo bài đăng đối tác' },
  { value: 'APPROVE_PARTNER_POST', label: 'Duyệt bài đăng đối tác' },
  { value: 'REJECT_PARTNER_POST', label: 'Từ chối bài đăng đối tác' },
  { value: 'UPDATE_PARTNER_POST', label: 'Cập nhật bài đăng đối tác' },
  { value: 'DELETE_PARTNER_POST', label: 'Xóa bài đăng đối tác' },
  { value: 'CREATE_ROOM', label: 'Tạo phòng' },
  { value: 'UPDATE_ROOM', label: 'Cập nhật phòng' },
  { value: 'CHANGE_ROOM_STATUS', label: 'Đổi trạng thái phòng' },
  { value: 'DELETE_ROOM', label: 'Xóa phòng' },
  { value: 'CREATE_RESERVATION', label: 'Tạo đặt phòng' },
  { value: 'CONFIRM_RESERVATION', label: 'Xác nhận đặt phòng' },
  { value: 'CANCEL_RESERVATION', label: 'Hủy đặt phòng' },
  { value: 'CREATE_MAINTENANCE_REQUEST', label: 'Tạo yêu cầu bảo trì' },
  { value: 'UPDATE_MAINTENANCE_STATUS', label: 'Cập nhật trạng thái bảo trì' },
  { value: 'COMPLETE_MAINTENANCE', label: 'Hoàn tất bảo trì' },
  { value: 'ASSIGN_ROLE', label: 'Gán vai trò' },
  { value: 'REMOVE_ROLE', label: 'Gỡ vai trò' },
  { value: 'GRANT_PERMISSION', label: 'Cấp quyền' },
  { value: 'REVOKE_PERMISSION', label: 'Thu hồi quyền' },
  { value: 'MANUAL_ADJUSTMENT', label: 'Điều chỉnh thủ công' },
  { value: 'SYSTEM_AUTO_ACTION', label: 'Tác vụ tự động hệ thống' },
  { value: 'BACKUP_DATA', label: 'Sao lưu dữ liệu' },
  { value: 'DELETE_DATA', label: 'Xóa dữ liệu' },
  { value: 'SUBMIT_CHECKOUT_REQUEST', label: 'Gửi yêu cầu trả phòng' },
  { value: 'APPROVE_CHECKOUT', label: 'Duyệt trả phòng' },
  { value: 'DAMAGE_ASSESSMENT', label: 'Đánh giá hư hại' },
  { value: 'FINAL_SETTLEMENT', label: 'Quyết toán cuối' },
];

const ENTITY_TYPE_OPTIONS = [
  { value: '', label: 'Tất cả đối tượng' },
  { value: 'ROOM', label: 'Phòng' },
  { value: 'CONTRACT', label: 'Hợp đồng' },
  { value: 'INVOICE', label: 'Hóa đơn' },
  { value: 'TENANT', label: 'Người thuê' },
  { value: 'RESERVATION', label: 'Đặt phòng' },
  { value: 'PARTNER_POST', label: 'Bài đăng đối tác' },
  { value: 'MAINTENANCE_REQUEST', label: 'Yêu cầu bảo trì' },
  { value: 'SYSTEM_CONFIG', label: 'Cấu hình hệ thống' },
];

export default function AuditLogsPage() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [actor, setActor] = useState('');
  const [action, setAction] = useState('');
  const [entityType, setEntityType] = useState('');
  const [entityId, setEntityId] = useState('');
  const [branchId, setBranchId] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [expandedIds, setExpandedIds] = useState(() => new Set());

  const toggleExpanded = (id) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const normalizeDateString = (s) => {
    if (!s || typeof s !== 'string') return null;
    // Backend often returns LocalDateTime with microseconds: 2025-12-18T17:07:57.747370
    // JS Date parsing is more reliable with milliseconds.
    return s.replace(/\.(\d{3})\d+$/, '.$1');
  };

  const formatDateTime = (s) => {
    const normalized = normalizeDateString(s);
    if (!normalized) return '-';
    const d = new Date(normalized);
    if (Number.isNaN(d.getTime())) return s;
    return new Intl.DateTimeFormat('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }).format(d);
  };

  const prettyJson = (value) => {
    if (value == null) return '';
    if (typeof value !== 'string') {
      try {
        return JSON.stringify(value, null, 2);
      } catch (e) {
        return String(value);
      }
    }

    const trimmed = value.trim();
    if (!trimmed) return '';
    try {
      const parsed = JSON.parse(trimmed);
      return JSON.stringify(parsed, null, 2);
    } catch (e) {
      return value;
    }
  };

  async function search() {
    setLoading(true);
    setError('');
    try {
      const parsedEntityId = entityId ? Number(entityId) : undefined;
      const safeEntityId = Number.isFinite(parsedEntityId) ? parsedEntityId : undefined;
      const parsedBranchId = branchId ? Number(branchId) : undefined;
      const safeBranchId = Number.isFinite(parsedBranchId) ? parsedBranchId : undefined;

      const res = await auditApi.search({
        from: from || undefined,
        to: to || undefined,
        actor: actor || undefined,
        action: action || undefined,
        entityType: entityType || undefined,
        entityId: safeEntityId,
        branchId: safeBranchId,
        page,
        size,
        sort: 'id,desc',
      });
      setData(res);
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Tìm kiếm thất bại');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    search();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);

  const pageObj = data;
  const itemsRaw = Array.isArray(pageObj?.content) ? pageObj.content : [];
  const items = [...itemsRaw].sort((a, b) => (b?.id || 0) - (a?.id || 0));
  const totalPages = pageObj?.totalPages ?? 0;

  return (
    <div className="container mx-auto px-6 py-8">
      <h1 className="text-2xl font-bold mb-6">Nhật ký thao tác</h1>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
        <div>
          <label className="block text-sm font-medium mb-1">Từ thời điểm</label>
          <input type="datetime-local" className="w-full border rounded px-3 py-2" value={from} onChange={(e) => setFrom(e.target.value)} />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Đến thời điểm</label>
          <input type="datetime-local" className="w-full border rounded px-3 py-2" value={to} onChange={(e) => setTo(e.target.value)} />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Người thực hiện</label>
          <input className="w-full border rounded px-3 py-2" value={actor} onChange={(e) => setActor(e.target.value)} placeholder="tên đăng nhập" />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Hành động</label>
          <select className="w-full border rounded px-3 py-2" value={action} onChange={(e) => { setAction(e.target.value); setPage(0); }}>
            {ACTION_OPTIONS.map((opt) => (
              <option key={opt.value || 'ALL'} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div>
          <label className="block text-sm font-medium mb-1">Loại đối tượng</label>
          <select className="w-full border rounded px-3 py-2" value={entityType} onChange={(e) => { setEntityType(e.target.value); setPage(0); }}>
            {ENTITY_TYPE_OPTIONS.map((opt) => (
              <option key={opt.value || 'ALL'} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Mã đối tượng</label>
          <input className="w-full border rounded px-3 py-2" value={entityId} onChange={(e) => setEntityId(e.target.value)} placeholder="VD: 123" />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Mã chi nhánh</label>
          <input className="w-full border rounded px-3 py-2" value={branchId} onChange={(e) => setBranchId(e.target.value)} placeholder="VD: 1" />
        </div>
        <div className="flex items-end gap-2">
          <button className="border rounded px-3 py-2 w-full" onClick={() => { setPage(0); search(); }} disabled={loading}>
            Tìm kiếm
          </button>
          <select className="border rounded px-2 py-2" value={size} onChange={(e) => setSize(Number(e.target.value))}>
            {[10, 20, 50].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error ? <div className="mb-4 text-red-600">{error}</div> : null}

      <div className="overflow-x-auto border rounded">
        <table className="min-w-full text-sm">
          <thead className="bg-gray-50">
            <tr>
              <th className="text-left px-3 py-2">Thời gian</th>
              <th className="text-left px-3 py-2">Người thực hiện</th>
              <th className="text-left px-3 py-2">Hành động</th>
              <th className="text-left px-3 py-2">Đối tượng</th>
              <th className="text-left px-3 py-2">Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={5} className="px-3 py-6 text-center">Đang tải...</td>
              </tr>
            ) : items.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-3 py-6 text-center text-gray-500">Không có dữ liệu</td>
              </tr>
            ) : (
              items.map((it) => {
                const expanded = expandedIds.has(it.id);
                const oldText = prettyJson(it.oldValue);
                const newText = prettyJson(it.newValue);
                const hasDiff = Boolean(oldText || newText);

                return (
                  <React.Fragment key={it.id}>
                    <tr className="border-t align-top">
                      <td className="px-3 py-2 whitespace-nowrap">
                        <div className="font-medium text-gray-900">{formatDateTime(it.createdAt)}</div>
                        <div className="text-xs text-gray-500">#{it.id}</div>
                      </td>
                      <td className="px-3 py-2">
                        <div className="font-medium text-gray-900">{it.actorId ?? '-'}</div>
                        <div className="text-xs text-gray-500">{it.actorRole ?? '-'}</div>
                      </td>
                      <td className="px-3 py-2 whitespace-nowrap">
                        <div className="inline-flex items-center gap-2">
                          <span className="px-2 py-1 rounded bg-gray-100 text-gray-800 text-xs font-semibold">
                            {it.action ?? '-'}
                          </span>
                          <span className={`px-2 py-1 rounded text-xs font-semibold ${it.status === 'FAILURE' ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'}`}>
                            {it.status ?? 'SUCCESS'}
                          </span>
                        </div>
                        {it.errorMessage ? (
                          <div className="text-xs text-red-600 mt-1 truncate" title={it.errorMessage}>
                            {it.errorMessage}
                          </div>
                        ) : null}
                      </td>
                      <td className="px-3 py-2 whitespace-nowrap">
                        <div className="flex items-center gap-2">
                          <span className="px-2 py-1 rounded bg-indigo-50 text-indigo-700 text-xs font-semibold">
                            {it.targetType ?? '-'}
                          </span>
                          <span className="text-gray-700 font-medium">#{it.targetId ?? '-'}</span>
                        </div>
                        <div className="text-xs text-gray-500 mt-1">Branch: {it.branchId ?? '-'}</div>
                      </td>
                      <td className="px-3 py-2">
                        <div className="flex items-start justify-between gap-3">
                          <div className="min-w-0">
                            <div className="text-gray-900 truncate" title={it.description ?? ''}>
                              {it.description ?? '-'}
                            </div>
                            <div className="text-xs text-gray-500 mt-1 truncate" title={it.ipAddress || ''}>
                              IP: {it.ipAddress ?? '-'}
                            </div>
                          </div>
                          <button
                            className={`border rounded px-2 py-1 text-xs whitespace-nowrap ${hasDiff ? 'hover:bg-gray-50' : 'opacity-50 cursor-not-allowed'}`}
                            onClick={() => hasDiff && toggleExpanded(it.id)}
                            disabled={!hasDiff}
                            title={hasDiff ? (expanded ? 'Ẩn chi tiết' : 'Xem giá trị cũ/mới') : 'Không có giá trị cũ/mới'}
                          >
                            {expanded ? 'Ẩn' : 'Xem'}
                          </button>
                        </div>
                      </td>
                    </tr>

                    {expanded && (
                      <tr className="border-t bg-gray-50/50">
                        <td colSpan={5} className="px-3 py-3">
                          <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
                            <div className="bg-white border rounded p-3">
                              <div className="text-xs font-bold text-gray-600 mb-2">Giá trị cũ</div>
                              <pre className="text-xs whitespace-pre-wrap break-words text-gray-800 max-h-[240px] overflow-auto">
                                {oldText || '(trống)'}
                              </pre>
                            </div>
                            <div className="bg-white border rounded p-3">
                              <div className="text-xs font-bold text-gray-600 mb-2">Giá trị mới</div>
                              <pre className="text-xs whitespace-pre-wrap break-words text-gray-800 max-h-[240px] overflow-auto">
                                {newText || '(trống)'}
                              </pre>
                            </div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between mt-4">
        <div className="text-sm text-gray-600">Trang {page + 1} / {Math.max(1, totalPages || 1)}</div>
        <div className="flex gap-2">
          <button className="border rounded px-3 py-1" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page <= 0}>
            Trước
          </button>
          <button className="border rounded px-3 py-1" onClick={() => setPage((p) => p + 1)} disabled={totalPages ? page + 1 >= totalPages : items.length < size}>
            Sau
          </button>
        </div>
      </div>
    </div>
  );
}

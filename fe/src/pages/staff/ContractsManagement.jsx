import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import contractApi from '../../api/contractApi';

export default function ContractsManagement() {
  const navigate = useNavigate();
  const [contracts, setContracts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  const fetchContracts = async () => {
    setLoading(true);
    try {
      const res = await contractApi.getMyBranchContracts({
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
        status: statusFilter,
      });
      const data = Array.isArray(res) ? res : (res?.content || res?.data?.result?.content || res?.data?.content || []);
      setContracts(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error('Lỗi tải hợp đồng', e);
      setContracts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchContracts();
  }, [statusFilter]);

  const runSearch = async () => {
    const q = (searchQuery || '').trim();
    if (!q) {
      fetchContracts();
      return;
    }

    setLoading(true);
    try {
      const res = await contractApi.getMyBranchContracts({
        page: 0,
        size: 50,
        sort: 'createdAt,desc',
        status: statusFilter,
        q,
      });
      const data = Array.isArray(res) ? res : (res?.content || res?.data?.result?.content || res?.data?.content || []);
      setContracts(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error('Lỗi tra cứu hợp đồng', e);
      setContracts([]);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (v) => {
    if (!v) return '-';
    try {
      return new Date(v).toLocaleString('vi-VN');
    } catch {
      return v;
    }
  };

  const formatStatus = (status) => {
    const s = String(status || '').toUpperCase();
    if (!s) return '-';
    switch (s) {
      case 'PENDING':
        return 'Chờ ký';
      case 'SIGNED_PENDING_DEPOSIT':
        return 'Đã ký - chờ thanh toán cọc';
      case 'ACTIVE':
        return 'Đang hiệu lực';
      case 'ENDED':
        return 'Đã kết thúc';
      case 'CANCELLED':
        return 'Đã hủy';
      default:
        return status;
    }
  };

  const deletePending = async (id) => {
    if (!window.confirm('Xóa hợp đồng tạm này?')) return;
    try {
      await contractApi.delete(id);
      setContracts((prev) => prev.filter((c) => c.id !== id));
      alert('Đã xóa hợp đồng tạm');
    } catch (e) {
      console.error(e);
      alert('Không thể xóa hợp đồng');
    }
  };

  return (
    <div className="container mx-auto px-6 py-8">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6">
        <h1 className="text-2xl font-bold">Quản lý hợp đồng</h1>
        <button
          className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm"
          onClick={() => navigate('/staff/contracts/create')}
        >
          Tạo hợp đồng
        </button>
      </div>

      <div className="bg-white rounded-xl border p-4 mb-4 flex flex-col md:flex-row gap-3 md:items-center md:justify-between">
        <div className="flex gap-2 items-center">
          <span className="text-sm text-gray-600">Trạng thái</span>
          <select
            className="border rounded-lg px-3 py-2 text-sm"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="ALL">Tất cả</option>
            <option value="PENDING">Chờ ký</option>
            <option value="SIGNED_PENDING_DEPOSIT">Đã ký - chờ thanh toán cọc</option>
            <option value="ACTIVE">Đang hiệu lực</option>
            <option value="ENDED">Đã kết thúc</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>

        <div className="flex gap-2">
          <input
            className="border rounded-lg px-3 py-2 text-sm w-full md:w-[360px]"
            placeholder="Tra cứu: tên khách, SĐT, email, phòng..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') runSearch();
            }}
          />
          <button className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-sm" onClick={runSearch}>
            Tra cứu
          </button>
        </div>
      </div>

      {loading ? (
        <div>Tải dữ liệu...</div>
      ) : (
        <div className="bg-white rounded-xl border p-4 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left border-b">
                <th className="py-2">Mã</th>
                <th>Khách</th>
                <th>Chi nhánh</th>
                <th>Phòng</th>
                <th>Trạng thái</th>
                <th>Ngày tạo</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {contracts.map((c) => (
                <tr key={c.id} className="border-b">
                  <td className="py-2">#{c.id}</td>
                  <td>
                    <div className="font-medium">{c.tenantName || '-'}</div>
                  </td>
                  <td>{c.branchCode || '-'}</td>
                  <td>{c.roomNumber || c.roomCode || '-'}</td>
                  <td>
                    <span className="px-2 py-1 rounded bg-gray-100">{formatStatus(c.status)}</span>
                  </td>
                  <td>{formatDateTime(c.createdAt)}</td>
                  <td className="text-right">
                    {c.status === 'PENDING' && (
                      <>
                        <button
                          className="px-3 py-1 rounded bg-indigo-600 text-white mr-2"
                          onClick={() => navigate(`/staff/contracts/create?contractId=${c.id}`)}
                        >
                          Hoàn thiện
                        </button>
                        <button
                          className="px-3 py-1 rounded bg-red-600 text-white"
                          onClick={() => deletePending(c.id)}
                        >
                          Xóa
                        </button>
                      </>
                    )}

                    {c.status === 'SIGNED_PENDING_DEPOSIT' && (
                      <button
                        className="px-3 py-1 rounded bg-indigo-600 text-white"
                        onClick={() => navigate(`/staff/contracts/create?contractId=${c.id}`)}
                      >
                        Thanh toán cọc
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {contracts.length === 0 && (
                <tr>
                  <td className="py-6 text-center text-gray-500" colSpan={7}>
                    Không có hợp đồng
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

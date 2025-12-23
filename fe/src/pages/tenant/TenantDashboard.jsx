import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import invoiceApi from '../../api/invoiceApi';
import contractApi from '../../api/contractApi';
import reservationApi from '../../api/reservationApi';

export default function TenantDashboard() {
  const { user } = useSelector((state) => state.auth);
  
  const [invoices, setInvoices] = useState([]);
  const [stats, setStats] = useState({
    unpaidInvoices: 0,
    activeContracts: 0,
    pendingReservations: 0
  });
  const [recentInvoices, setRecentInvoices] = useState([]);

  const formatMoneyVnd = (value) => {
    const num = typeof value === 'number' ? value : Number(value);
    if (!Number.isFinite(num)) return '0 đ';
    return `${num.toLocaleString()} đ`;
  };

  const formatLocalDate = (value) => {
    if (!value) return '';
    const d = new Date(value);
    if (Number.isNaN(d.getTime())) return '';
    return d.toLocaleDateString();
  };

  const getInvoiceTitle = (invoice) => {
    const month = invoice?.billingMonth;
    const year = invoice?.billingYear;
    if (month != null && year != null) return `Tháng ${month}/${year}`;

    // Ad-hoc invoices (settlement/maintenance/others) may not have billingMonth/billingYear
    const due = invoice?.dueDate ? new Date(invoice.dueDate) : null;
    if (due && !Number.isNaN(due.getTime())) {
      return `Hóa đơn phát sinh (${due.getMonth() + 1}/${due.getFullYear()})`;
    }
    return 'Hóa đơn phát sinh';
  };

  const compareInvoicesDesc = (a, b) => {
    const aTime = a?.createdAt ? Date.parse(a.createdAt) : a?.dueDate ? Date.parse(a.dueDate) : 0;
    const bTime = b?.createdAt ? Date.parse(b.createdAt) : b?.dueDate ? Date.parse(b.dueDate) : 0;
    if (bTime !== aTime) return bTime - aTime;
    return (b?.id || 0) - (a?.id || 0);
  };

  useEffect(() => {
    // 1. Fetch Hóa đơn (Invoices)
    const fetchInvoices = async () => {
      try {
        const res = await invoiceApi.getMyInvoices();
        // axiosClient interceptor may return the inner data directly or a page/wrapper
        let data = [];
        if (Array.isArray(res)) {
          data = res;
        } else if (res && res.content) {
          data = res.content;
        } else if (res && res.data) {
          data = res.data.result || res.data || [];
        } else {
          data = res || [];
        }

        setInvoices(data);
        const sorted = [...(data || [])].sort(compareInvoicesDesc);
        setRecentInvoices(sorted.slice(0, 3));
        setStats(prev => ({ ...prev, unpaidInvoices: (data || []).filter(i => i.status === 'UNPAID').length }));
      } catch (err) {
        console.warn("Lỗi tải hóa đơn:", err.message || err);
      }
    };

    // 2. Fetch Hợp đồng (Contracts)
    const fetchContracts = async () => {
      try {
        const res = await contractApi.getMyContracts();
        let data = [];
        if (Array.isArray(res)) {
          data = res;
        } else if (res && res.content) {
          data = res.content;
        } else if (res && res.data) {
          data = res.data.result || res.data || [];
        } else {
          data = res || [];
        }
        setStats(prev => ({ ...prev, activeContracts: (data || []).filter(c => c.status === 'ACTIVE').length }));
      } catch (err) {
        console.warn("Lỗi tải hợp đồng:", err.message || err);
      }
    };

    // 3. Fetch Giữ chỗ (Reservations)
    const fetchReservations = async () => {
      try {
        const res = await reservationApi.getMyReservations({ size: 100 });
        let data = [];
        if (res && res.content) data = res.content;
        else if (res && Array.isArray(res)) data = res;
        else if (res && res.data) data = res.data.result?.content || res.data.result || [];
        else data = res || [];

        setStats(prev => ({ ...prev, pendingReservations: (data || []).filter(r => r.status === 'PENDING' || r.status === 'PENDING_CONFIRMATION').length }));
      } catch (err) {
        console.warn("Lỗi tải giữ chỗ:", err.message || err);
      }
    };

    // Chạy độc lập
    if (user) {
        fetchInvoices();
        fetchContracts();
        fetchReservations();
    }
  }, [user]);

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-r from-indigo-600 to-purple-600 rounded-2xl p-8 text-white shadow-lg">
        <h1 className="text-3xl font-bold mb-2">Xin chào, {user?.fullName || user?.username}! 👋</h1>
        <p className="opacity-90">Chào mừng bạn quay trở lại hệ thống quản lý phòng trọ.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Card Hóa đơn */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500 font-medium">Hóa đơn chưa thanh toán</p>
              <h3 className="text-3xl font-bold text-red-600 mt-2">{stats.unpaidInvoices}</h3>
            </div>
            <span className="text-4xl p-2 bg-red-50 rounded-lg">💳</span>
          </div>
          <Link to="/tenant/invoices" className="text-sm text-indigo-600 mt-4 inline-block hover:underline font-medium">Xem chi tiết &rarr;</Link>
        </div>

        {/* Card Hợp đồng */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500 font-medium">Hợp đồng hiệu lực</p>
              <h3 className="text-3xl font-bold text-green-600 mt-2">{stats.activeContracts}</h3>
            </div>
            <span className="text-4xl p-2 bg-green-50 rounded-lg">📝</span>
          </div>
          <Link to="/tenant/contracts" className="text-sm text-indigo-600 mt-4 inline-block hover:underline font-medium">Xem hợp đồng &rarr;</Link>
        </div>

        {/* Card Giữ chỗ */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500 font-medium">Giữ chỗ đang chờ</p>
              <h3 className="text-3xl font-bold text-yellow-600 mt-2">{stats.pendingReservations}</h3>
            </div>
            <span className="text-4xl p-2 bg-yellow-50 rounded-lg">⏳</span>
          </div>
          <Link to="/tenant/reservations" className="text-sm text-indigo-600 mt-4 inline-block hover:underline font-medium">Xem lịch sử &rarr;</Link>
        </div>
      </div>

      {/* Danh sách hóa đơn gần đây */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
          <h3 className="font-bold text-gray-800">Hóa đơn gần đây</h3>
          <Link to="/tenant/invoices" className="text-sm text-indigo-600 hover:text-indigo-800">Xem tất cả</Link>
        </div>
        <div className="divide-y divide-gray-100">
          {recentInvoices.length > 0 ? recentInvoices.map((invoice) => (
            <div key={invoice.id} className="p-6 flex items-center justify-between hover:bg-gray-50 transition-colors">
              <div className="flex items-center gap-4">
                <div className={`w-10 h-10 rounded-full flex items-center justify-center ${invoice.status === 'PAID' ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'}`}>
                  $
                </div>
                <div>
                  <p className="font-medium text-gray-900">{getInvoiceTitle(invoice)}</p>
                  <p className="text-sm text-gray-500">Hạn: {formatLocalDate(invoice.dueDate) || 'Đang cập nhật'}</p>
                </div>
              </div>
              <div className="text-right">
                <p className="font-bold text-gray-900">{formatMoneyVnd(invoice.amount)}</p>
                <span className={`text-xs font-bold px-2 py-1 rounded-full ${invoice.status === 'PAID' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                  {invoice.status}
                </span>
              </div>
            </div>
          )) : (
            <div className="p-8 text-center text-gray-500">Chưa có hóa đơn nào</div>
          )}
        </div>
      </div>
    </div>
  );
}
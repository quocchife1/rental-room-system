import React, { useEffect, useState } from 'react';
import partnerApi from '../../api/partnerApi';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

export default function PartnerDashboard() {
  const [stats, setStats] = useState({
    views: 0,
    activeListings: 0,
    pendingListings: 0,
    rejectedListings: 0
  });
  const [monthlyData, setMonthlyData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
    fetchMonthlyViews();
  }, []);

  const fetchMonthlyViews = async () => {
    try {
      const res = await partnerApi.getMonthlyViews();
      console.log('Monthly views response:', res);
      setMonthlyData(res);
    } catch (err) {
      console.error('Lỗi tải dữ liệu lượt xem theo tháng:', err);
    }
  };

  const fetchStats = async () => {
    try {
      const posts = await partnerApi.getMyPosts();
      // Check data structure from API response
      const data = Array.isArray(posts) ? posts : (posts.data || posts.result || []);
      const active = data.filter(p => p.status === 'APPROVED' || p.status === 'ACTIVE').length;
      const pending = data.filter(p => p.status === 'PENDING_APPROVAL' || p.status === 'PENDING').length;
      const rejected = data.filter(p => p.status === 'REJECTED').length;
      const totalViews = data.reduce((acc, curr) => acc + (curr.views || 0), 0); // Tính tổng view thật

      setStats({
        views: totalViews,
        activeListings: active,
        pendingListings: pending,
        rejectedListings: rejected
      });
    } catch (err) {
      console.error('Lỗi tải thống kê:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Tổng quan hoạt động</h1>
          <p className="text-gray-500 text-sm mt-1">Thống kê hiệu quả tin đăng của bạn.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl shadow-sm border border-indigo-50 hover:shadow-md transition-all">
          <div className="flex items-center gap-3 mb-2">
            <span className="p-2 bg-indigo-50 text-indigo-600 rounded-lg text-xl">👀</span>
            <p className="text-gray-500 text-sm font-medium">Lượt xem tin</p>
          </div>
          <h3 className="text-3xl font-extrabold text-indigo-600 ml-1">{stats.views}</h3>
        </div>

        <div className="bg-white p-5 rounded-2xl shadow-sm border border-green-50 hover:shadow-md transition-all">
          <div className="flex items-center gap-3 mb-2">
            <span className="p-2 bg-green-50 text-green-600 rounded-lg text-xl">✅</span>
            <p className="text-gray-500 text-sm font-medium">Tin đang hiển thị</p>
          </div>
          <h3 className="text-3xl font-extrabold text-green-600 ml-1">{stats.activeListings}</h3>
        </div>

        <div className="bg-white p-5 rounded-2xl shadow-sm border border-yellow-50 hover:shadow-md transition-all">
          <div className="flex items-center gap-3 mb-2">
            <span className="p-2 bg-yellow-50 text-yellow-600 rounded-lg text-xl">⏳</span>
            <p className="text-gray-500 text-sm font-medium">Tin chờ duyệt</p>
          </div>
          <h3 className="text-3xl font-extrabold text-yellow-600 ml-1">{stats.pendingListings}</h3>
        </div>

        <div className="bg-white p-5 rounded-2xl shadow-sm border border-red-50 hover:shadow-md transition-all">
          <div className="flex items-center gap-3 mb-2">
            <span className="p-2 bg-red-50 text-red-600 rounded-lg text-xl">❌</span>
            <p className="text-gray-500 text-sm font-medium">Tin bị từ chối</p>
          </div>
          <h3 className="text-3xl font-extrabold text-red-600 ml-1">{stats.rejectedListings}</h3>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-gray-800 mb-4 text-lg">Lượt xem theo tháng</h3>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={monthlyData}>
              <defs>
                <linearGradient id="colorViews" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis
                dataKey="month"
                tick={{ fontSize: 12, fill: '#6b7280' }}
                axisLine={{ stroke: '#e5e7eb' }}
              />
              <YAxis
                tick={{ fontSize: 12, fill: '#6b7280' }}
                axisLine={{ stroke: '#e5e7eb' }}
              />
              <Tooltip
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  fontSize: '12px'
                }}
              />
              <Area
                type="monotone"
                dataKey="views"
                stroke="#6366f1"
                strokeWidth={2}
                fillOpacity={1}
                fill="url(#colorViews)"
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 className="font-bold text-gray-800 mb-4 text-lg">Thông báo mới</h3>
          <div className="space-y-4">
            {stats.pendingListings > 0 && (
              <div className="flex gap-3 items-start p-3 bg-yellow-50 rounded-xl border border-yellow-100">
                <span className="text-xl">⚠️</span>
                <div>
                  <p className="text-sm font-bold text-yellow-800">Tin chờ duyệt</p>
                  <p className="text-xs text-yellow-700 mt-1">Bạn có {stats.pendingListings} tin đang chờ ban quản trị phê duyệt.</p>
                </div>
              </div>
            )}
            <div className="flex gap-3 items-start p-3 bg-gray-50 rounded-xl">
              <span className="text-xl">🎉</span>
              <div>
                <p className="text-sm font-bold text-gray-700">Chào mừng đối tác</p>
                <p className="text-xs text-gray-500 mt-1">Cảm ơn bạn đã đồng hành cùng UML Rental.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
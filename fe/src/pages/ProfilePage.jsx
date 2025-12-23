import React, { useEffect, useState } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import userApi from '../api/userApi';
import { updateUserInfo, logout } from '../features/auth/authSlice';

export default function ProfilePage() {
  const { user, token } = useSelector((state) => state.auth);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  
  // 1. KHỞI TẠO STATE TỪ REDUX STORE (Đảm bảo có dữ liệu ngay lập tức)
  const [profile, setProfile] = useState({
    username: user?.username || '',
    fullName: user?.fullName || '',
    email: user?.email || '',
    phoneNumber: user?.phoneNumber || '',
    address: user?.address || '',
    role: user?.role || ''
  });
  
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState(null);

  // Redirect nếu chưa đăng nhập
  useEffect(() => {
    if (!token || !user) {
      navigate('/login');
    }
  }, [token, user, navigate]);

  // 2. Fetch dữ liệu mới nhất (chỉ chạy ngầm để cập nhật, không block UI)
  useEffect(() => {
    if (!user) return;

    const fetchProfile = async () => {
      // Không set loading = true để tránh hiệu ứng "nháy" màn hình
      // Chỉ cập nhật nếu API trả về thành công
      try {
        let data = null;
        
        if (user.role === 'TENANT') {
          const res = await userApi.getTenantProfile(user.id);
          data = res.data || res;
        } else if (user.role === 'PARTNER') {
          const res = await userApi.getPartnerProfile(user.id);
          data = res.data || res;
        } else if (user.role === 'EMPLOYEE' || user.role === 'ADMIN') {
          try {
             const res = await userApi.getEmployeeProfile(user.id);
             data = res.data || res;
          } catch(e) {
             // Admin có thể không có profile, bỏ qua lỗi này
             console.log("Admin/Employee profile fetch skipped or failed.");
          }
        }
        
        const finalData = Array.isArray(data) ? data[0] : data;

        if (finalData) {
          setProfile(prev => ({
            ...prev,
            fullName: finalData.fullName || prev.fullName,
            email: finalData.email || prev.email,
            phoneNumber: finalData.phoneNumber || prev.phoneNumber,
            address: finalData.address || prev.address,
            username: finalData.username || prev.username,
            role: user.role
          }));
        }
      } catch (error) {
        console.error('Lỗi khi đồng bộ hồ sơ:', error);
      }
    };

    fetchProfile();
  }, [user]);

  const handleUpdate = async (e) => {
    e.preventDefault();
    setMsg(null);
    setLoading(true);

    try {
      const payload = {
        fullName: profile.fullName,
        email: profile.email,
        phoneNumber: profile.phoneNumber,
        address: profile.address
      };

      let updatedData = { ...user, ...payload }; 

      if (user.role === 'TENANT') {
        const res = await userApi.updateTenantProfile(user.id, payload);
        updatedData = { ...updatedData, ...(res.data || res) };
      } else if (user.role === 'PARTNER') {
        const res = await userApi.updatePartnerProfile(user.id, payload);
        updatedData = { ...updatedData, ...(res.data || res) };
      } else {
        setMsg({ type: 'info', text: 'Thông tin đã lưu tạm thời (Admin chưa hỗ trợ cập nhật DB).' });
      }
      
      dispatch(updateUserInfo(updatedData));
      if (user.role !== 'ADMIN' && user.role !== 'EMPLOYEE') {
          setMsg({ type: 'success', text: 'Cập nhật hồ sơ thành công!' });
      }
    } catch (error) {
      console.error(error);
      setMsg({ type: 'error', text: 'Cập nhật thất bại. Vui lòng thử lại.' });
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  if (!user) return null;

  return (
    <div className="bg-gray-50 min-h-screen py-12 px-4">
      <div className="max-w-5xl mx-auto">
          <div className="mb-8">
            <h1 className="text-3xl font-extrabold text-gray-900">Hồ sơ cá nhân</h1>
            <p className="text-gray-500 mt-2">Thông tin tài khoản và cài đặt.</p>
          </div>

          <div className="bg-white rounded-3xl shadow-xl shadow-indigo-100/50 border border-gray-100 overflow-hidden">
            <div className="md:flex">
              {/* Sidebar Info */}
              <div className="md:w-1/3 bg-gradient-to-b from-indigo-50 to-white p-8 border-r border-gray-100 flex flex-col items-center text-center">
                <div className="relative">
                  <div className="w-32 h-32 bg-white border-4 border-white shadow-lg rounded-full flex items-center justify-center text-5xl mb-4 overflow-hidden">
                    <img 
                      src={`https://ui-avatars.com/api/?name=${profile.username}&background=6366f1&color=fff&size=128`} 
                      alt="Avatar" 
                      className="w-full h-full object-cover"
                    />
                  </div>
                </div>
                
                {/* HIỂN THỊ USERNAME CHÍNH XÁC */}
                <h2 className="text-2xl font-bold text-gray-900 mt-2">{profile.username}</h2>
                <div className="mt-2 px-4 py-1.5 bg-indigo-100 text-indigo-700 text-xs font-bold uppercase tracking-wider rounded-full">
                  {profile.role}
                </div>
                
                <p className="text-gray-500 text-sm mt-4">{profile.email || 'Chưa cập nhật email'}</p>

                <div className="mt-auto w-full pt-8 space-y-3">
                  <button 
                    onClick={handleLogout}
                    className="w-full py-3 px-4 bg-white border border-red-100 text-red-600 rounded-xl hover:bg-red-50 font-bold transition-colors shadow-sm flex items-center justify-center gap-2"
                  >
                    <span>🚪</span> Đăng xuất
                  </button>
                </div>
              </div>

              {/* Form Update */}
              <div className="md:w-2/3 p-8 md:p-10">
                <div className="flex justify-between items-center mb-8 pb-4 border-b border-gray-100">
                    <h3 className="text-xl font-bold text-gray-800">Thông tin chi tiết</h3>
                </div>
                
                {msg && (
                  <div className={`p-4 mb-6 rounded-xl flex items-center gap-3 ${msg.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200' : (msg.type === 'info' ? 'bg-blue-50 text-blue-800 border border-blue-200' : 'bg-red-50 text-red-800 border border-red-200')}`}>
                    <span className="text-xl">{msg.type === 'success' ? '✅' : (msg.type === 'info' ? 'ℹ️' : '⚠️')}</span>
                    {msg.text}
                  </div>
                )}

                <form onSubmit={handleUpdate} className="space-y-6">
                  {/* USERNAME (READ ONLY) */}
                  <div className="space-y-2">
                      <label className="text-sm font-semibold text-gray-500">Tên đăng nhập</label>
                      <input
                        value={profile.username}
                        readOnly
                        className="w-full px-4 py-3 bg-gray-100 border border-gray-200 rounded-xl text-gray-500 cursor-not-allowed outline-none"
                      />
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <label className="text-sm font-semibold text-gray-700">Họ và tên</label>
                      <input
                        value={profile.fullName}
                        onChange={(e) => setProfile({...profile, fullName: e.target.value})}
                        className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                        placeholder="Nhập họ tên đầy đủ"
                      />
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-semibold text-gray-700">Số điện thoại</label>
                      <input
                        value={profile.phoneNumber}
                        onChange={(e) => setProfile({...profile, phoneNumber: e.target.value})}
                        className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                        placeholder="Nhập số điện thoại"
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-semibold text-gray-700">Email liên hệ</label>
                    <input
                      type="email"
                      value={profile.email}
                      onChange={(e) => setProfile({...profile, email: e.target.value})}
                      className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                      placeholder="email@example.com"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-semibold text-gray-700">Địa chỉ thường trú</label>
                    <input
                      value={profile.address}
                      onChange={(e) => setProfile({...profile, address: e.target.value})}
                      className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 outline-none transition-all"
                      placeholder="Nhập địa chỉ nhà riêng hoặc văn phòng"
                    />
                  </div>

                  <div className="pt-8 flex justify-end gap-4">
                    <button
                      type="button"
                      onClick={() => navigate('/')}
                      className="px-6 py-3 rounded-xl text-gray-600 font-bold hover:bg-gray-100 transition-colors"
                    >
                      Hủy bỏ
                    </button>
                    <button
                      type="submit"
                      disabled={loading}
                      className="bg-indigo-600 text-white px-8 py-3 rounded-xl hover:bg-indigo-700 font-bold transition-all shadow-lg shadow-indigo-200 transform active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed flex items-center gap-2"
                    >
                      {loading ? 'Đang lưu...' : 'Lưu thay đổi'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
    </div>
  );
}
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authApi from '../api/authApi';
import AuthLayout from '../components/AuthLayout';

export default function RegisterPage() {
  // Dùng 'GUEST' cho Sinh viên và 'PARTNER' cho Chủ trọ để khớp logic Backend
  const [activeTab, setActiveTab] = useState('GUEST');
  
  const [formData, setFormData] = useState({
    username: '', 
    password: '', 
    confirmPassword: '', 
    fullName: '', 
    email: '', 
    phoneNumber: '', // Frontend dùng phoneNumber cho rõ nghĩa
    address: '',
    dob: '',         // Thêm ngày sinh cho sinh viên
    companyName: ''  // Thêm tên công ty (nếu chủ trọ muốn nhập)
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 1. Kiểm tra mật khẩu
    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 2. Chuẩn bị dữ liệu gửi đi (Mapping dữ liệu)
      // Backend DTO dùng 'phone', Frontend đang dùng 'phoneNumber' -> Cần map lại
      const payload = {
        username: formData.username,
        password: formData.password,
        email: formData.email,
        fullName: formData.fullName,
        phone: formData.phoneNumber, // QUAN TRỌNG: Map field này để khớp DTO Backend
      };

      // 3. Thêm trường riêng biệt tùy theo Tab
      if (activeTab === 'GUEST') {
        // Sinh viên cần thêm ngày sinh
        payload.dob = formData.dob;
        
        // Gọi API đăng ký Guest
        await authApi.registerGuest(payload);
      } else {
        // Chủ trọ cần thêm địa chỉ và tên công ty
        payload.address = formData.address;
        payload.companyName = formData.companyName || formData.fullName; // Nếu không nhập tên cty thì lấy tên người
        
        // Gọi API đăng ký Partner
        await authApi.registerPartner(payload);
      }
      
      alert('Đăng ký thành công! Vui lòng đăng nhập.');
      navigate('/login');
    } catch (err) {
      console.error(err);
      // Lấy thông báo lỗi từ Backend trả về
      const errorMsg = err.response?.data?.message || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.';
      setError(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout 
      title="Tạo tài khoản mới" 
      subtitle={
        <>
          Đã có tài khoản? <Link to="/login" className="font-medium text-indigo-600 hover:text-indigo-500">Đăng nhập ngay</Link>
        </>
      }
    >
      {/* Tabs chuyển đổi Sinh viên / Chủ trọ */}
      <div className="flex p-1 mb-6 bg-gray-100 rounded-xl">
        <button
          type="button"
          className={`w-1/2 py-2.5 text-sm font-bold rounded-lg transition-all ${
            activeTab === 'GUEST' ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'
          }`}
          onClick={() => setActiveTab('GUEST')}
        >
          Sinh viên
        </button>
        <button
          type="button"
          className={`w-1/2 py-2.5 text-sm font-bold rounded-lg transition-all ${
            activeTab === 'PARTNER' ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-500 hover:text-gray-700'
          }`}
          onClick={() => setActiveTab('PARTNER')}
        >
          Chủ trọ
        </button>
      </div>

      {error && (
        <div className="mb-4 bg-red-50 border-l-4 border-red-500 p-4 text-sm text-red-700 rounded-r-md">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 gap-4">
          
          {/* --- Các trường chung --- */}
          <div>
            <label className="block text-sm font-medium text-gray-700">Họ và tên</label>
            <input 
              name="fullName" 
              required 
              placeholder={activeTab === 'PARTNER' ? "Tên người liên hệ" : "Nguyễn Văn A"}
              className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
              onChange={handleInputChange} 
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Email</label>
            <input name="email" type="email" required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" onChange={handleInputChange} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Số điện thoại</label>
            <input name="phoneNumber" required pattern="[0-9]{10}" title="Vui lòng nhập 10 chữ số" className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" onChange={handleInputChange} />
          </div>

          {/* --- Trường riêng cho Sinh viên: Ngày sinh --- */}
          {activeTab === 'GUEST' && (
            <div>
              <label className="block text-sm font-medium text-gray-700">Ngày sinh</label>
              <input 
                name="dob" 
                type="date" 
                required 
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                onChange={handleInputChange} 
              />
            </div>
          )}

          {/* --- Trường riêng cho Chủ trọ: Tên công ty & Địa chỉ --- */}
          {activeTab === 'PARTNER' && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700">Tên nhà trọ / Công ty (Tùy chọn)</label>
                <input 
                  name="companyName" 
                  placeholder="Nhà trọ Xanh..."
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  onChange={handleInputChange} 
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Địa chỉ</label>
                <input 
                  name="address" 
                  required 
                  placeholder="Địa chỉ kinh doanh..."
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" 
                  onChange={handleInputChange} 
                />
              </div>
            </>
          )}

          <div>
            <label className="block text-sm font-medium text-gray-700">Tên đăng nhập</label>
            <input name="username" required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" onChange={handleInputChange} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Mật khẩu</label>
              <input name="password" type="password" required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" onChange={handleInputChange} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Xác nhận</label>
              <input name="confirmPassword" type="password" required className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" onChange={handleInputChange} />
            </div>
          </div>
        </div>

        <button type="submit" disabled={loading} className="w-full mt-6 flex justify-center py-3 px-4 border border-transparent rounded-xl shadow-sm text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed">
          {loading ? 'Đang xử lý...' : 'Đăng ký ngay'}
        </button>
      </form>
    </AuthLayout>
  );
}
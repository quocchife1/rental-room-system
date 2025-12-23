import React, { useEffect, useState } from 'react';
import partnerApi from '../../api/partnerApi';
import { useNavigate } from 'react-router-dom';

export default function ServicePackages() {
  const navigate = useNavigate();
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [purchasing, setPurchasing] = useState(null);

  useEffect(() => {
    const defaultPackages = [
      {
        id: 1,
        name: 'Gói cơ bản',
        price: 0,
        priceDisplay: 'Miễn phí',
        durationDays: 30,
        target: 'Đối tượng khuyên dùng: Sinh viên pass phòng, chủ nhà trọ chỉ có 1-2 phòng trống',
        features: [
          'Đăng tối đa 3 tin/ngày',
          'Tổng tin hiển thị tối đa 5 tin',
          'Duyệt tin thủ công (chờ Admin và nhân viên duyệt tin 12-24h)',
        ]
      },
      {
        id: 2,
        name: 'Gói môi giới chuyên nghiệp',
        price: 199000,
        priceDisplay: '199.000đ/tháng',
        durationDays: 30,
        target: 'Đối tượng khuyên dùng: Môi giới tự do, người quản lý 2-3 căn nhà',
        features: [
          'Đăng tối đa 20 tin/ngày',
          'Tổng tin hiển thị 50 tin',
          'Huy hiệu "Đối tác Pro" (Màu bạc)',
          'Giảm 10% khi mua các gói tin',
          'Tặng 5 lượt "Đẩy tin" miễn phí',
          'Duyệt tin siêu tốc trong 1-3 giờ'
        ]
      },
      {
        id: 3,
        name: 'Gói doanh nghiệp',
        price: 999000,
        priceDisplay: '999.000đ/tháng',
        durationDays: 30,
        target: 'Đối tượng khuyên dùng: Công ty quản lý tòa nhà, ký túc xá, chuỗi phòng trọ',
        features: [
          'Đăng tin không giới hạn',
          'Huy hiệu "Đối tác Xác thực" (Verified)',
          'Trang hồ sơ riêng với banner thương hiệu',
          'Giảm 25% khi mua các gói tin',
          'Xem số điện thoại khách hàng tiềm năng',
          'Xem biểu đồ hiệu quả tin đăng chi tiết',
          'Duyệt tin "THE FLASH" trong 30 phút'
        ]
      }
    ];
    setPackages(defaultPackages);
    setLoading(false);
  }, []);

  const handlePurchase = async (pkg) => {
    // Simulate purchase: get a post to activate
    const postId = prompt('Nhập ID tin đăng cần kích hoạt (hoặc để trống để bỏ qua):');
    if (!postId) {
      alert('Bạn cần chọn một tin đăng để mua gói.');
      return;
    }
    setPurchasing(pkg.id);
    try {
      const res = await partnerApi.simulatePurchase(postId, pkg.id);
      alert('Mua gói thành công! Tin đã được kích hoạt.');
      navigate('/partner/my-listings');
    } catch (e) {
      console.error('Lỗi mua gói', e);
      alert('Lỗi: ' + (e.response?.data?.message || 'Không thể mua gói'));
    } finally {
      setPurchasing(null);
    }
  };

  if (loading) return <div className="text-center py-12">Đang tải...</div>;

  return (
    <div className="space-y-10 animate-fade-in">
      <div className="text-center max-w-2xl mx-auto space-y-4">
        <h1 className="text-4xl font-extrabold text-gray-900">Nâng cấp tài khoản</h1>
        <p className="text-gray-500 text-lg">Chọn gói dịch vụ phù hợp để tiếp cận khách hàng tiềm năng nhanh chóng hơn.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto px-4">
        {packages.map((pkg, idx) => (
          <div key={pkg.id} className={`relative bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden transform transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl ${idx === 1 ? 'ring-4 ring-indigo-100 scale-105 z-10' : ''}`}>
            {idx === 1 && (
              <div className="absolute top-0 left-0 w-full bg-gradient-to-r from-indigo-500 to-purple-600 text-white text-center text-xs font-bold py-1.5 uppercase tracking-widest">
                Khuyên dùng
              </div>
            )}

            <div className="p-8 text-center pt-12">
              <div className={`w-16 h-16 mx-auto rounded-2xl flex items-center justify-center text-3xl shadow-lg text-white mb-6 ${idx === 0 ? 'bg-gradient-to-br from-blue-400 to-blue-600' :
                idx === 1 ? 'bg-gradient-to-br from-indigo-500 to-purple-600' :
                  'bg-gradient-to-br from-amber-500 to-orange-600'
                }`}>
                {idx === 0 ? '📦' : idx === 1 ? '⭐' : '🏢'}
              </div>
              <h3 className="text-xl font-bold text-gray-900">{pkg.name}</h3>
              <p className="text-xs text-gray-500 mt-2 mb-4 min-h-10">{pkg.target}</p>
              <div className="my-6 flex items-center justify-center gap-2 text-gray-900">
                {pkg.price === 0 ? (
                  <span className="text-3xl font-extrabold text-green-600">Miễn phí</span>
                ) : (
                  <>
                    <span className="text-4xl font-extrabold">{pkg.price.toLocaleString()}</span>
                    <span className="text-gray-500 font-medium">đ/tháng</span>
                  </>
                )}
              </div>
              <button onClick={() => handlePurchase(pkg)} disabled={purchasing === pkg.id} className={`w-full py-3 rounded-xl font-bold text-white shadow-lg transition-transform active:scale-95 disabled:opacity-50 ${idx === 0 ? 'bg-gradient-to-r from-blue-500 to-blue-600' :
                idx === 1 ? 'bg-gradient-to-r from-indigo-500 to-purple-600' :
                  'bg-gradient-to-r from-amber-500 to-orange-600'
                }`}>
                {purchasing === pkg.id ? 'Đang xử lý...' : 'Mua ngay'}
              </button>
            </div>

            <div className="bg-gray-50 p-8 border-t border-gray-100">
              <ul className="space-y-3">
                {pkg.features.map((feature, fidx) => (
                  <li key={fidx} className="flex items-start gap-3 text-sm text-gray-700">
                    <div className="w-5 h-5 rounded-full bg-green-100 text-green-600 flex items-center justify-center text-xs flex-shrink-0 mt-0.5">✓</div>
                    <span>{feature}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
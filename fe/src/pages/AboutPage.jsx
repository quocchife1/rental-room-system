import React from 'react';
import MainLayout from '../components/MainLayout';
import { CURRENT_SEASON } from '../components/SeasonalEffects';

export default function AboutPage() {
  const stats = [
    { label: 'Cơ sở hiện đại', value: '3+' },
    { label: 'Phòng tiện nghi', value: '500+' },
    { label: 'Thành viên UML', value: '2.000+' },
    { label: 'Tỉ lệ hài lòng', value: '99%' },
  ];

  const features = [
    {
      title: 'An Tâm Tuyệt Đối',
      desc: 'Hệ thống an ninh 3 lớp với bảo vệ 24/7, camera giám sát và khóa vân tay hiện đại tại mọi cơ sở UML.',
      icon: '🛡️'
    },
    {
      title: 'Sống Tiện Nghi',
      desc: 'Không gian sống được trang bị đầy đủ nội thất cao cấp, wifi tốc độ cao và khu sinh hoạt chung sáng tạo.',
      icon: '🛋️'
    },
    {
      title: 'Kết Nối Dễ Dàng',
      desc: 'Vị trí đắc địa ngay trung tâm các khu đại học, thuận tiện di chuyển và kết nối cộng đồng tri thức trẻ.',
      icon: '📍'
    }
  ];

  // Thay đổi màu nền Hero section dựa theo mùa
  const heroClass = (CURRENT_SEASON === 'CHRISTMAS' || CURRENT_SEASON === 'WINTER')
    ? "bg-gradient-to-r from-red-700 to-green-800 text-white py-24" // Màu Giáng sinh
    : "bg-indigo-600 text-white py-24"; // Màu mặc định

  return (
    <MainLayout>
      {/* Hero Section */}
      <div className={heroClass}>
        <div className="container mx-auto px-6 text-center">
          <h1 className="text-4xl md:text-5xl font-extrabold mb-6">
            {CURRENT_SEASON === 'CHRISTMAS' ? '🎄 Giáng sinh ấm áp cùng UML Rental' : 'Câu chuyện của UML Rental'}
          </h1>
          <p className="text-indigo-100 text-lg max-w-2xl mx-auto leading-relaxed opacity-90">
            "UML - United, Modern, Living". Chúng tôi kiến tạo không gian sống nơi mọi sinh viên được kết nối, phát triển và tận hưởng tuổi trẻ.
          </p>
        </div>
      </div>

      {/* Stats */}
      <div className="container mx-auto px-6 -mt-16 relative z-10">
        <div className="bg-white rounded-3xl shadow-xl grid grid-cols-2 md:grid-cols-4 gap-8 p-10 text-center border border-gray-100">
          {stats.map((stat, idx) => (
            <div key={idx} className="space-y-2">
              <div className="text-4xl font-extrabold text-indigo-600">{stat.value}</div>
              <div className="text-sm font-bold text-gray-400 uppercase tracking-wider">{stat.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Mission */}
      <div className="container mx-auto px-6 py-24">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-16 items-center">
          <div>
            <span className="text-indigo-600 font-bold tracking-wide uppercase text-sm">Tầm nhìn & Sứ mệnh</span>
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mt-4 mb-6">
              Nâng tầm chất lượng sống sinh viên Việt
            </h2>
            <p className="text-gray-500 text-lg leading-relaxed mb-6">
              Tại <b>UML Rental</b>, chúng tôi tin rằng môi trường sống quyết định sự thành công. Sứ mệnh của chúng tôi là xóa bỏ nỗi lo về "nhà trọ tạm bợ", thay vào đó là những "ngôi nhà thứ hai" đầy cảm hứng.
            </p>
            <p className="text-gray-500 text-lg leading-relaxed">
              <b>UML</b> cam kết mang đến sự minh bạch về tài chính, sự chuyên nghiệp trong vận hành và sự tận tâm trong từng dịch vụ hỗ trợ.
            </p>
          </div>
          <div className="grid grid-cols-2 gap-4">
            {/* Ảnh 1: Không gian sinh hoạt chung */}
            <img 
              src="https://images.unsplash.com/photo-1555854877-bab0e564b8d5?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80" 
              alt="UML Living Space" 
              className="rounded-3xl shadow-lg w-full h-64 object-cover mt-8 transform hover:scale-105 transition-transform duration-500" 
            />
            {/* Ảnh 2: Phòng ngủ hiện đại */}
            <img 
              src="https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80" 
              alt="UML Bedroom" 
              className="rounded-3xl shadow-lg w-full h-64 object-cover transform hover:scale-105 transition-transform duration-500" 
            />
          </div>
        </div>
      </div>

      {/* Features */}
      <div className="bg-white py-24">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">Tại sao chọn UML Rental?</h2>
            <p className="text-gray-500 mt-4">Trải nghiệm sống khác biệt dành riêng cho cư dân UML</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {features.map((f, idx) => (
              <div key={idx} className="bg-stone-50 p-8 rounded-3xl border border-stone-100 hover:shadow-lg transition-all duration-300 group">
                <div className="text-4xl mb-6 transform group-hover:scale-110 transition-transform duration-300">{f.icon}</div>
                <h3 className="text-xl font-bold text-gray-900 mb-3 group-hover:text-indigo-600 transition-colors">{f.title}</h3>
                <p className="text-gray-500 leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
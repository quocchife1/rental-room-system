import React, { useEffect, useState } from 'react';
import roomApi from '../api/roomApi';
import publicPartnerApi from '../api/publicPartnerApi';
import resolveImageUrl from '../utils/resolveImageUrl';
import RoomCard from '../components/RoomCard';
import MainLayout from '../components/MainLayout';

export default function HomePage() {
  const [rooms, setRooms] = useState([]);
  const [displayRooms, setDisplayRooms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [partnerPosts, setPartnerPosts] = useState([]);

  // Chỉ còn lại bộ lọc chi nhánh
  const [filterBranch, setFilterBranch] = useState('ALL');

  const branches = [
    { code: 'ALL', name: 'Toàn bộ hệ thống' },
    { code: 'CN01', name: 'UML Quận 1 - Center' },
    { code: 'CN02', name: 'UML Quận 7 - Riverside' },
    { code: 'CN03', name: 'UML Thủ Đức - Campus' }
  ];

  const fetchRooms = async () => {
    setLoading(true);
    try {
      let data = [];
      
      // Logic mới: Luôn ưu tiên lấy phòng AVAILABLE trước
      // Nếu chọn chi nhánh cụ thể -> Gọi API Branch -> Lọc tiếp Available
      // Nếu chọn ALL -> Gọi API Status Available
      
      if (filterBranch !== 'ALL') {
        const res = await roomApi.getByBranch(filterBranch);
        // Chỉ giữ lại phòng AVAILABLE từ kết quả trả về
        data = (res || []).filter(r => r.status === 'AVAILABLE');
      } else {
        // Mặc định lấy tất cả phòng AVAILABLE
        const res = await roomApi.getByStatus('AVAILABLE');
        data = res || [];
      }

      setRooms(data);
      setDisplayRooms(data);
    } catch (error) {
      console.error("Lỗi tải dữ liệu", error);
      setRooms([]);
      setDisplayRooms([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRooms();
    fetchPartnerPosts();
  }, []);

  const fetchPartnerPosts = async () => {
    try {
      const res = await publicPartnerApi.list({ page: 0, size: 8, sort: 'createdAt,desc' });
      const data = res?.data?.result || res?.data || res;
      const content = data?.content || [];
      setPartnerPosts(content);
    } catch (e) {
      console.error('Lỗi tải tin đối tác', e);
      setPartnerPosts([]);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    fetchRooms();
  };

  // Tự động tìm kiếm khi đổi chi nhánh (UX tốt hơn, đỡ phải bấm nút)
  const handleBranchChange = (e) => {
    setFilterBranch(e.target.value);
  };

  return (
    <MainLayout>
      {/* --- HERO SECTION --- */}
      <div className="relative overflow-hidden bg-gradient-to-br from-indigo-50 via-white to-teal-50 pt-20 pb-28 border-b border-white/60">
        <div className="container mx-auto px-6 text-center relative z-10">
          <span className="inline-flex items-center gap-2 py-1.5 px-4 rounded-full bg-indigo-50 border border-indigo-100 text-indigo-600 text-xs font-bold uppercase tracking-wider mb-6 shadow-sm">
            <span className="w-2 h-2 rounded-full bg-indigo-500 animate-pulse"></span>
            UML Rental - Đồng hành cùng sinh viên Việt
          </span>
          
          <h1 className="text-4xl md:text-6xl font-extrabold text-gray-900 mb-6 leading-tight tracking-tight">
            Khởi đầu hành trình <br className="hidden md:block" />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-teal-500">
              Tại ngôi nhà thứ hai
            </span>
          </h1>
          
          <p className="text-lg text-gray-500 max-w-2xl mx-auto mb-8 font-light">
            **UML Rental** mang đến không gian sống hiện đại, an ninh và gắn kết. 
            Nơi bạn không chỉ ở, mà còn được sống trọn vẹn tuổi trẻ.
          </p>
        </div>
        
        {/* Background Decor */}
        <div className="absolute top-0 left-0 w-full h-full overflow-hidden pointer-events-none opacity-30">
            <div className="absolute -top-[10%] left-[20%] w-[40%] h-[40%] rounded-full bg-indigo-200 blur-[120px]"></div>
            <div className="absolute top-[20%] right-[10%] w-[30%] h-[50%] rounded-full bg-teal-100 blur-[100px]"></div>
        </div>
      </div>

      {/* --- CONTENT CONTAINER --- */}
      <div className="container mx-auto px-6 pb-24">
        
        {/* --- SEARCH BAR (Floating Glass Effect) --- */}
        <div className="-mt-10 relative z-20 mb-16">
          <div className="bg-white/80 backdrop-blur-xl rounded-2xl shadow-2xl shadow-indigo-100/40 p-3 max-w-3xl mx-auto border border-white/50 ring-1 ring-gray-100">
            <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
              
              {/* Dropdown Chi Nhánh - Thiết kế tối giản */}
              <div className="relative flex-grow group">
                <div className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-hover:text-indigo-500 transition-colors text-xl">
                  📍
                </div>
                <select 
                  className="w-full bg-gray-50 hover:bg-white focus:bg-white border-none rounded-xl py-4 pl-12 pr-10 text-gray-700 font-semibold text-base transition-all outline-none focus:ring-2 focus:ring-indigo-100 cursor-pointer appearance-none"
                  value={filterBranch}
                  onChange={handleBranchChange}
                >
                  {branches.map((b) => (
                    <option key={b.code} value={b.code}>{b.name}</option>
                  ))}
                </select>
                <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-gray-400">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path></svg>
                </div>
              </div>

              {/* Button Tìm Kiếm */}
              <button 
                type="submit" 
                className="sm:w-auto w-full bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl px-8 py-4 transition-all shadow-lg shadow-indigo-200 hover:shadow-indigo-300 transform active:scale-95 flex items-center justify-center gap-2 whitespace-nowrap"
              >
                <span>🔍</span>
                <span>Tìm Phòng Ngay</span>
              </button>
            </form>
          </div>
        </div>

        {/* --- LIST HEADER --- */}
        <div className="flex items-center justify-between mb-10 pb-4 border-b border-gray-100">
          <div>
            <h2 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
              Phòng đang sẵn sàng
              <span className="flex h-2 w-2 relative">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
              </span>
            </h2>
          </div>
          <span className="text-sm font-medium text-gray-500 bg-gray-50 px-4 py-2 rounded-full border border-gray-100">
            {displayRooms.length} lựa chọn tốt nhất
          </span>
        </div>

        {/* --- ROOM GRID --- */}
        {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="bg-white rounded-3xl h-80 shadow-sm border border-gray-50 animate-pulse">
                 <div className="h-48 bg-gray-100 rounded-t-3xl"></div>
                 <div className="p-4 space-y-3">
                    <div className="h-4 bg-gray-100 rounded w-2/3"></div>
                    <div className="h-4 bg-gray-100 rounded w-1/2"></div>
                 </div>
              </div>
            ))}
          </div>
        ) : displayRooms.length > 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {displayRooms.map((room) => (
              <RoomCard key={room.id} room={room} />
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-24 bg-white rounded-3xl border border-gray-100 shadow-sm">
            <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-4xl grayscale opacity-50">
              🏠
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">Chưa tìm thấy phòng trống</h3>
            <p className="text-gray-500 max-w-md text-center">
              Hiện tại khu vực <span className="font-semibold text-gray-700">{branches.find(b => b.code === filterBranch)?.name}</span> đang hết phòng trống. 
              Bạn hãy thử chọn khu vực khác xem sao nhé!
            </p>
            <button 
                onClick={() => { setFilterBranch('ALL'); fetchRooms(); }}
                className="mt-8 text-indigo-600 font-semibold hover:text-indigo-800 transition-colors flex items-center gap-2"
            >
                <span>↺</span> Xem tất cả khu vực
            </button>
          </div>
        )}

        {/* --- PARTNER POSTS SECTION --- */}
        <div className="mt-16">
          <div className="flex items-center justify-between mb-6 pb-4 border-b border-gray-100">
            <h2 className="text-2xl font-bold text-gray-900">Tin từ đối tác</h2>
            <span className="text-sm font-medium text-gray-500 bg-gray-50 px-4 py-2 rounded-full border border-gray-100">
              {partnerPosts.length} tin nổi bật
            </span>
          </div>

          {partnerPosts.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
              {partnerPosts.map((post) => (
                <a key={post.id} href={`/partner-posts/${post.id}`} className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden group">
                  <div className="h-40 bg-gray-100">
                    <img
                      src={(post.imageUrls && post.imageUrls.length > 0) ? (resolveImageUrl(post.imageUrls[0])) : 'https://placehold.co/600x300?text=No+Image'}
                      alt={post.title}
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform"
                      onError={(e) => { e.target.src = 'https://placehold.co/600x300?text=Error'; }}
                    />
                  </div>
                  <div className="p-4">
                    <h3 className="font-bold text-gray-900 line-clamp-1 group-hover:text-indigo-600 transition-colors">{post.title}</h3>
                    <p className="text-indigo-600 font-bold mt-1">{post.price?.toLocaleString()} đ/tháng</p>
                    <p className="text-sm text-gray-500 line-clamp-2 mt-1">{post.address}</p>
                  </div>
                </a>
              ))}
            </div>
          ) : (
            <div className="text-center py-12 bg-white rounded-3xl border border-gray-100 shadow-sm">
              <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-3 text-3xl grayscale opacity-50">📰</div>
              <p className="text-gray-600">Chưa có tin đối tác nào được duyệt</p>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
}
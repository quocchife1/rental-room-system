import React, { useEffect, useState } from 'react';
import contractApi from '../../api/contractApi';

// Icon Components (Inline SVGs)
const Icons = {
  Download: () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
  ),
  Checkout: () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
  ),
  Calendar: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" /></svg>
  ),
  Money: () => (
    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
  ),
  Home: () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>
  ),
  Close: () => (
    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
  )
};

export default function MyContracts() {
  const [contracts, setContracts] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [selectedContractId, setSelectedContractId] = useState(null);
  const [checkoutDate, setCheckoutDate] = useState('');
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchContracts = async () => {
      setLoading(true);
      try {
        const res = await contractApi.getMyContracts();
        let data = [];
        if (Array.isArray(res)) data = res;
        else if (res && res.content) data = res.content;
        else if (res && res.data) data = res.data.result || res.data || [];
        else data = res || [];
        setContracts(data);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    };
    fetchContracts();
  }, []);

  const handleDownload = async (id, code) => {
    try {
      const response = await contractApi.downloadContract(id);
      const blob = response && response.data ? response.data : response;
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `HopDong_${code}.docx`);
      document.body.appendChild(link);
      link.click();
      setTimeout(() => window.URL.revokeObjectURL(url), 100);
    } catch (error) {
      alert("Không thể tải file hợp đồng.");
    }
  };

  const openCheckoutModal = (id) => {
    setSelectedContractId(id);
    setShowModal(true);
  };

  const submitCheckout = async (e) => {
    e.preventDefault();
    try {
      await contractApi.requestCheckout(selectedContractId, {
        requestDate: checkoutDate, 
        reason: reason
      });
      alert('Gửi yêu cầu trả phòng thành công!');
      setShowModal(false);
      setReason('');
      setCheckoutDate('');
    } catch (error) {
      alert(error.response?.data?.message || 'Gửi yêu cầu thất bại');
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '---';
    return new Date(dateString).toLocaleDateString('vi-VN');
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN').format(amount || 0) + ' đ';
  };

  return (
    <div className="min-h-screen bg-slate-50 p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-6xl mx-auto space-y-8">
        
        {/* Header */}
        <div>
          <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight">Hợp đồng thuê phòng</h2>
          <p className="text-slate-500 mt-2">Quản lý thông tin hợp đồng và các yêu cầu liên quan.</p>
        </div>

        {/* Contract List */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {contracts.map(contract => (
            <div 
              key={contract.id} 
              className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden hover:shadow-lg transition-all duration-300 group"
            >
              {/* Card Header */}
              <div className="px-6 py-5 border-b border-slate-100 flex justify-between items-start bg-gradient-to-r from-white to-slate-50">
                <div className="flex gap-4">
                  <div className="p-3 bg-indigo-100 text-indigo-600 rounded-xl">
                    <Icons.Home />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-slate-800">Phòng {contract.roomCode || 'N/A'}</h3>
                    <p className="text-sm text-slate-500 font-mono">#{contract.contractCode}</p>
                  </div>
                </div>
                <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider ${
                  contract.status === 'ACTIVE' 
                    ? 'bg-emerald-100 text-emerald-700 border border-emerald-200' 
                    : 'bg-slate-100 text-slate-600 border border-slate-200'
                }`}>
                  {contract.status === 'ACTIVE' ? 'Đang thuê' : contract.status}
                </span>
              </div>
              
              {/* Card Body */}
              <div className="p-6 space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-100">
                    <div className="flex items-center gap-2 text-slate-500 text-xs uppercase font-bold mb-1">
                      <Icons.Calendar /> Ngày bắt đầu
                    </div>
                    <div className="text-slate-800 font-medium">
                      {formatDate(contract.startDate)}
                    </div>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-100">
                    <div className="flex items-center gap-2 text-slate-500 text-xs uppercase font-bold mb-1">
                      <Icons.Calendar /> Ngày kết thúc
                    </div>
                    <div className="text-slate-800 font-medium">
                      {formatDate(contract.endDate)}
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between p-3 rounded-lg bg-indigo-50/50 border border-indigo-100">
                  <div className="flex items-center gap-2 text-indigo-800 text-sm font-medium">
                    <Icons.Money /> Tiền cọc giữ chỗ
                  </div>
                  <span className="text-lg font-bold text-indigo-700">
                    {formatCurrency(contract.depositAmount)}
                  </span>
                </div>
              </div>

              {/* Card Footer / Actions */}
              <div className="px-6 py-4 bg-slate-50 border-t border-slate-100 flex gap-3">
                <button 
                  onClick={() => handleDownload(contract.id, contract.contractCode)}
                  className="flex-1 flex items-center justify-center gap-2 bg-white border border-slate-300 text-slate-700 py-2.5 rounded-xl font-semibold hover:bg-slate-100 hover:border-slate-400 transition-all shadow-sm active:scale-95 text-sm"
                >
                  <Icons.Download /> Tải hợp đồng
                </button>
                {contract.status === 'ACTIVE' && (
                  <button 
                    onClick={() => openCheckoutModal(contract.id)}
                    className="flex-1 flex items-center justify-center gap-2 bg-rose-50 border border-rose-200 text-rose-600 py-2.5 rounded-xl font-semibold hover:bg-rose-100 hover:border-rose-300 transition-all shadow-sm active:scale-95 text-sm"
                  >
                    <Icons.Checkout /> Trả phòng
                  </button>
                )}
              </div>
            </div>
          ))}

          {loading && (
             <div className="col-span-full py-20 text-center">
               <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-slate-200 border-t-indigo-600"></div>
               <p className="mt-2 text-slate-500 text-sm">Đang tải dữ liệu...</p>
             </div>
          )}

          {!loading && contracts.length === 0 && (
            <div className="col-span-full bg-white rounded-2xl p-12 text-center border border-dashed border-slate-300">
              <div className="mx-auto w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center text-slate-400 mb-4">
                <Icons.Home />
              </div>
              <h3 className="text-lg font-bold text-slate-800">Chưa có hợp đồng</h3>
              <p className="text-slate-500">Bạn hiện tại chưa có hợp đồng thuê phòng nào.</p>
            </div>
          )}
        </div>
      </div>

      {/* Checkout Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="absolute inset-0 bg-slate-900/60 backdrop-blur-sm transition-opacity" onClick={() => setShowModal(false)}></div>
          <div className="bg-white rounded-2xl w-full max-w-md shadow-2xl z-10 overflow-hidden transform transition-all scale-100">
            <div className="bg-white px-6 py-4 border-b border-slate-100 flex justify-between items-center">
              <h3 className="text-lg font-bold text-slate-800">Yêu cầu trả phòng</h3>
              <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600 transition-colors">
                <Icons.Close />
              </button>
            </div>
            
            <form onSubmit={submitCheckout} className="p-6 space-y-5">
              <div className="bg-blue-50 text-blue-800 text-sm p-3 rounded-lg border border-blue-100">
                Lưu ý: Yêu cầu trả phòng cần được gửi trước <strong>30 ngày</strong> theo quy định hợp đồng.
              </div>

              <div>
                <label className="block text-sm font-bold text-slate-700 mb-2">Ngày dự kiến trả</label>
                <input 
                  type="date" 
                  required 
                  className="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 text-slate-800 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none"
                  value={checkoutDate}
                  onChange={(e) => setCheckoutDate(e.target.value)}
                />
              </div>
              
              <div>
                <label className="block text-sm font-bold text-slate-700 mb-2">Lý do trả phòng</label>
                <textarea 
                  required 
                  rows="4" 
                  className="w-full bg-slate-50 border border-slate-300 rounded-xl px-4 py-3 text-slate-800 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all outline-none resize-none"
                  placeholder="Ví dụ: Chuyển công tác, hết hạn hợp đồng, về quê..."
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                ></textarea>
              </div>

              <div className="flex gap-3 pt-2">
                <button 
                  type="button" 
                  onClick={() => setShowModal(false)} 
                  className="flex-1 px-4 py-3 text-slate-700 bg-white border border-slate-300 hover:bg-slate-50 rounded-xl font-bold transition-all"
                >
                  Hủy bỏ
                </button>
                <button 
                  type="submit" 
                  className="flex-1 px-4 py-3 bg-rose-600 text-white hover:bg-rose-700 rounded-xl font-bold shadow-lg shadow-rose-200 transition-all transform active:scale-95"
                >
                  Gửi yêu cầu
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
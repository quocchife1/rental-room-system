import React, { useEffect, useState } from 'react';
import invoiceApi from '../../api/invoiceApi';

export default function MyInvoices() {
  const [invoices, setInvoices] = useState([]);
  const [processingId, setProcessingId] = useState(null);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    const fetchInvoices = async () => {
      try {
        const res = await invoiceApi.getMyInvoices();
        const arr = Array.isArray(res) ? res : [];
        // Sắp xếp ID giảm dần (mới nhất lên đầu)
        arr.sort((a, b) => (b?.id || 0) - (a?.id || 0));
        setInvoices(arr);
      } catch (error) {
        console.error(error);
      }
    };
    fetchInvoices();
  }, []);

  const handlePay = async (id, direct) => {
    setProcessingId(id);
    try {
      await invoiceApi.payInvoice(id, direct);
      alert('Thanh toán thành công!');

      const updateInvoiceState = (inv) => 
        inv.id === id ? { ...inv, status: 'PAID', paidDirect: direct } : inv;

      setInvoices((prev) => prev.map(updateInvoiceState));
      setSelected((prev) => (prev && prev.id === id ? { ...prev, status: 'PAID', paidDirect: direct } : prev));
    } catch (error) {
      alert('Lỗi thanh toán: ' + (error.response?.data?.message || error.message || 'Lỗi không xác định'));
    } finally {
      setProcessingId(null);
    }
  };

  // --- Helper Functions ---

  function formatPeriod(inv) {
    if (inv?.billingMonth && inv?.billingYear) {
      return `Kỳ tháng ${inv.billingMonth}/${inv.billingYear}`;
    }
    return 'Hóa đơn bổ sung';
  }

  function getStatusLabel(status) {
    switch (status) {
      case 'PAID': return 'Đã thanh toán';
      case 'OVERDUE': return 'Quá hạn';
      default: return 'Chờ thanh toán'; // UNPAID
    }
  }

  function getStatusStyle(status) {
    switch (status) {
      case 'PAID': return 'bg-emerald-50 text-emerald-700 border border-emerald-100';
      case 'OVERDUE': return 'bg-orange-50 text-orange-700 border border-orange-100';
      default: return 'bg-rose-50 text-rose-700 border border-rose-100';
    }
  }

  function formatPaymentMethod(inv) {
    if (inv?.status !== 'PAID') return '---';
    return inv?.paidDirect ? 'Tiền mặt' : 'Chuyển khoản';
  }

  function formatMoney(v) {
    const n = typeof v === 'number' ? v : Number(v || 0);
    return Number.isFinite(n) ? n.toLocaleString('vi-VN') : '0';
  }

  function formatDate(dateString) {
    if (!dateString) return '---';
    return new Date(dateString).toLocaleDateString('vi-VN');
  }

  return (
    <div className="bg-gray-50 min-h-screen p-6 md:p-10 font-sans text-slate-800">
      <div className="max-w-6xl mx-auto space-y-8">
        
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <h2 className="text-3xl font-extrabold tracking-tight text-slate-900">Quản lý hóa đơn</h2>
            <p className="text-slate-500 mt-2">Theo dõi và thanh toán các khoản phí hàng tháng của bạn.</p>
          </div>
          <div className="bg-white px-4 py-2 rounded-lg shadow-sm border border-gray-200">
            <span className="text-sm text-slate-500 mr-2">Tổng hóa đơn:</span>
            <span className="font-bold text-lg text-slate-800">{invoices.length}</span>
          </div>
        </div>

        {/* Detail View Section (Expanded Card) */}
        {selected && (
          <div className="bg-white rounded-2xl shadow-xl border border-indigo-100 overflow-hidden transition-all duration-300 ease-in-out">
            {/* Detail Header */}
            <div className="bg-slate-900 text-white px-8 py-5 flex items-center justify-between">
              <div>
                <div className="text-indigo-300 text-xs font-bold uppercase tracking-wider mb-1">Chi tiết hóa đơn</div>
                <div className="text-xl font-bold">Mã số #{selected.id}</div>
              </div>
              <button 
                onClick={() => setSelected(null)}
                className="bg-slate-700 hover:bg-slate-600 text-white text-xs px-4 py-2 rounded-md font-semibold transition-colors"
              >
                Đóng lại
              </button>
            </div>

            <div className="p-8">
              {/* Info Grid */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8 pb-8 border-b border-gray-100">
                <div>
                  <div className="text-xs text-slate-400 font-semibold uppercase mb-1">Kỳ thanh toán</div>
                  <div className="font-medium text-slate-800">{formatPeriod(selected)}</div>
                </div>
                <div>
                  <div className="text-xs text-slate-400 font-semibold uppercase mb-1">Hạn thanh toán</div>
                  <div className="font-medium text-slate-800">{formatDate(selected?.dueDate)}</div>
                </div>
                <div>
                  <div className="text-xs text-slate-400 font-semibold uppercase mb-1">Trạng thái</div>
                  <span className={`inline-block px-3 py-1 rounded-md text-xs font-bold ${getStatusStyle(selected.status)}`}>
                    {getStatusLabel(selected.status)}
                  </span>
                </div>
                <div>
                  <div className="text-xs text-slate-400 font-semibold uppercase mb-1">Hình thức</div>
                  <div className="font-medium text-slate-800">{formatPaymentMethod(selected)}</div>
                </div>
              </div>

              {/* Items Table */}
              <div className="mb-8">
                <h4 className="text-sm font-bold text-slate-900 mb-4 uppercase tracking-wide">Chi tiết khoản thu</h4>
                <div className="overflow-hidden rounded-lg border border-gray-200">
                  <table className="w-full text-sm text-left">
                    <thead className="bg-gray-50 text-slate-500 font-semibold border-b border-gray-200">
                      <tr>
                        <th className="px-6 py-3">Nội dung</th>
                        <th className="px-6 py-3 text-right">Đơn giá</th>
                        <th className="px-6 py-3 text-center">SL</th>
                        <th className="px-6 py-3 text-right">Thành tiền</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {Array.isArray(selected.details) && selected.details.length > 0 ? (
                        selected.details.map((d) => (
                          <tr key={d.id} className="hover:bg-gray-50/50">
                            <td className="px-6 py-4 font-medium text-slate-700">{d.description}</td>
                            <td className="px-6 py-4 text-right text-slate-500">{formatMoney(d.unitPrice)}</td>
                            <td className="px-6 py-4 text-center text-slate-500">{d.quantity}</td>
                            <td className="px-6 py-4 text-right font-bold text-slate-800">{formatMoney(d.amount)} ₫</td>
                          </tr>
                        ))
                      ) : (
                        <tr><td colSpan="4" className="px-6 py-4 text-center text-gray-400 italic">Không có chi tiết hiển thị</td></tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Footer / Actions */}
              <div className="flex flex-col md:flex-row items-center justify-between gap-6 bg-slate-50 p-6 rounded-xl border border-gray-100">
                 <div className="text-slate-600 text-sm max-w-lg">
                    {selected.status !== 'PAID' && (
                      <p>
                        Nếu bạn muốn thanh toán bằng <span className="font-bold text-slate-800">tiền mặt</span>, vui lòng liên hệ kế toán và cung cấp mã hóa đơn <span className="font-bold">#{selected.id}</span>.
                      </p>
                    )}
                 </div>
                 <div className="flex items-center gap-6 w-full md:w-auto justify-between md:justify-end">
                    <div className="text-right">
                      <div className="text-xs text-slate-500 font-semibold uppercase">Tổng cộng</div>
                      <div className="text-2xl font-bold text-indigo-600">{formatMoney(selected.amount)} ₫</div>
                    </div>
                    {selected.status !== 'PAID' && (
                      <button
                        onClick={() => handlePay(selected.id, false)}
                        disabled={processingId === selected.id}
                        className="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg text-sm font-bold shadow-lg shadow-indigo-200 transition-all transform active:scale-95"
                      >
                        {processingId === selected.id ? 'Đang xử lý...' : 'Thanh toán ngay'}
                      </button>
                    )}
                 </div>
              </div>
            </div>
          </div>
        )}

        {/* Main List Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200">
                  <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Kỳ thu phí</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider">Hạn thanh toán</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider text-right">Tổng tiền</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider text-center">Trạng thái</th>
                  <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase tracking-wider text-center">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {invoices.map((inv) => (
                  <tr key={inv.id} className={`group transition-colors ${selected?.id === inv.id ? 'bg-indigo-50/60' : 'hover:bg-gray-50'}`}>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-slate-800">{formatPeriod(inv)}</div>
                      <div className="text-xs text-slate-400 mt-1">ID: #{inv.id}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-slate-600">
                      {formatDate(inv.dueDate)}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span className="font-bold text-slate-800 text-sm">{formatMoney(inv.amount)} ₫</span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span className={`inline-flex items-center justify-center px-2.5 py-1 rounded-md text-xs font-bold border ${getStatusStyle(inv.status)}`}>
                        {getStatusLabel(inv.status)}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <div className="flex items-center justify-center gap-3">
                        <button
                          onClick={() => setSelected(inv)}
                          className="text-slate-600 hover:text-indigo-600 text-xs font-bold border border-slate-200 hover:border-indigo-200 px-3 py-1.5 rounded bg-white transition-all"
                        >
                          Xem chi tiết
                        </button>
                        {inv.status !== 'PAID' && (
                           <button
                             onClick={() => handlePay(inv.id, false)}
                             disabled={processingId === inv.id}
                             className="text-indigo-600 hover:text-indigo-800 text-xs font-bold hover:underline disabled:opacity-50 disabled:no-underline"
                           >
                             {processingId === inv.id ? '...' : 'Thanh toán'}
                           </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {invoices.length === 0 && (
                  <tr>
                    <td colSpan="5" className="px-6 py-12 text-center">
                      <div className="text-slate-400 text-sm">Hiện tại bạn chưa có hóa đơn nào cần thanh toán.</div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
}
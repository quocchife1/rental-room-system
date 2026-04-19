import React, { useEffect, useState } from 'react';
import invoiceApi from '../../api/invoiceApi';

export default function MyInvoices() {
  const [invoices, setInvoices] = useState([]);
  const [processingId, setProcessingId] = useState(null);
  const [selected, setSelected] = useState(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    const fetchInvoices = async () => {
      try {
        const res = await invoiceApi.getMyInvoicesPaged({ page, size, sort: 'id,desc' });
        const data = res?.content || res?.data?.result?.content || res?.data?.content || res?.result?.content || [];
        setInvoices(Array.isArray(data) ? data : []);
        const meta = res?.totalPages != null ? res : res?.data?.result || res?.data || res;
        setTotalPages(meta?.totalPages || 0);
        setTotalElements(meta?.totalElements || (Array.isArray(data) ? data.length : 0));
      } catch (error) {
        console.error(error);
      }
    };
    fetchInvoices();

    // If user was redirected back from MoMo (via backend return handler), refresh invoices from server.
    try {
      const params = new URLSearchParams(window.location.search);
      const orderId = params.get('orderId');
      const momo = params.get('momo');

      if (momo != null || orderId != null) {
        if (momo === 'success') {
          alert('Thanh toán MoMo thành công. Hệ thống đang cập nhật lại hóa đơn...');
        } else if (momo === 'failed') {
          alert('Thanh toán MoMo chưa thành công. Vui lòng thử lại.');
        }

        // Clear query params to avoid repeated alerts on refresh
        window.history.replaceState({}, document.title, window.location.pathname);

        // Give backend a short moment then refetch
        setTimeout(() => {
          fetchInvoices();
        }, 1200);
      }
    } catch (e) {
      // ignore
    }
  }, [page, size]);

  const handlePay = async (id, direct) => {
    setProcessingId(id);
    try {
      const resp = await invoiceApi.payInvoice(id, direct);

      // Online payment (MoMo): backend returns { payUrl, orderId }
      if (!direct && resp && resp.payUrl) {
        window.location.href = resp.payUrl;
        return;
      }

      // Direct/cash confirmation (staff only)
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
    <div className="bg-[color:var(--app-bg)] min-h-screen p-6 md:p-10 font-sans text-[color:var(--app-text)]">
      <div className="max-w-6xl mx-auto space-y-8">
        
        {/* Header Section */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <h2 className="text-3xl font-extrabold tracking-tight text-[color:var(--app-text)]">Quản lý hóa đơn</h2>
            <p className="text-[color:var(--app-muted)] mt-2">Theo dõi và thanh toán các khoản phí hàng tháng của bạn.</p>
          </div>
          <div className="bg-[color:var(--app-surface-solid)] px-4 py-2 rounded-lg shadow-sm border border-[color:var(--app-border)]">
            <span className="text-sm text-[color:var(--app-muted)] mr-2">Tổng hóa đơn:</span>
            <span className="font-bold text-lg text-[color:var(--app-text)]">{totalElements || invoices.length}</span>
          </div>
        </div>

        {/* Detail View Section (Expanded Card) */}
        {selected && (
          <div className="bg-[color:var(--app-surface-solid)] rounded-2xl shadow-xl border border-[color:var(--app-border)] overflow-hidden transition-all duration-300 ease-in-out">
            {/* Detail Header */}
            <div className="bg-[color:var(--app-primary)] text-white px-8 py-5 flex items-center justify-between">
              <div>

              {totalPages > 1 && (
                <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-[color:var(--app-surface-solid)] px-4 py-3 rounded-xl border border-[color:var(--app-border)] shadow-sm">
                  <div className="text-sm text-[color:var(--app-muted)]">
                    Hiển thị {Math.min(page * size + 1, totalElements || invoices.length)}-{Math.min((page + 1) * size, totalElements || invoices.length)} trong {totalElements || invoices.length} hóa đơn
                  </div>
                  <div className="flex items-center gap-2 flex-wrap justify-center">
                    <button
                      onClick={() => setPage((p) => Math.max(p - 1, 0))}
                      disabled={page <= 0}
                      className="px-4 py-2 rounded-lg border border-[color:var(--app-border-strong)] text-sm font-medium hover:bg-[color:var(--app-primary-soft)] disabled:opacity-50"
                    >
                      ← Trước
                    </button>
                    <span className="text-sm font-medium text-[color:var(--app-muted)]">
                      Trang {page + 1}/{totalPages}
                    </span>
                    <button
                      onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                      disabled={page >= totalPages - 1}
                      className="px-4 py-2 rounded-lg border border-[color:var(--app-border-strong)] text-sm font-medium hover:bg-[color:var(--app-primary-soft)] disabled:opacity-50"
                    >
                      Sau →
                    </button>
                    <select
                      value={size}
                      onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}
                      className="ml-1 border border-[color:var(--app-border-strong)] rounded-lg px-2 py-2 text-sm bg-[color:var(--app-surface-solid)]"
                    >
                      <option value={10}>10</option>
                      <option value={20}>20</option>
                      <option value={50}>50</option>
                    </select>
                  </div>
                </div>
              )}
                <div className="text-white/70 text-xs font-bold uppercase tracking-wider mb-1">Chi tiết hóa đơn</div>
                <div className="text-xl font-bold">Mã số #{selected.id}</div>
              </div>
              <button 
                onClick={() => setSelected(null)}
                className="bg-white/15 hover:bg-white/20 text-white text-xs px-4 py-2 rounded-md font-semibold transition-colors"
              >
                Đóng lại
              </button>
            </div>

            <div className="p-8">
              {/* Info Grid */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8 pb-8 border-b border-[color:var(--app-border)]">
                <div>
                  <div className="text-xs text-[color:var(--app-muted-2)] font-semibold uppercase mb-1">Kỳ thanh toán</div>
                  <div className="font-medium text-[color:var(--app-text)]">{formatPeriod(selected)}</div>
                </div>
                <div>
                  <div className="text-xs text-[color:var(--app-muted-2)] font-semibold uppercase mb-1">Hạn thanh toán</div>
                  <div className="font-medium text-[color:var(--app-text)]">{formatDate(selected?.dueDate)}</div>
                </div>
                <div>
                  <div className="text-xs text-[color:var(--app-muted-2)] font-semibold uppercase mb-1">Trạng thái</div>
                  <span className={`inline-block px-3 py-1 rounded-md text-xs font-bold ${getStatusStyle(selected.status)}`}>
                    {getStatusLabel(selected.status)}
                  </span>
                </div>
                <div>
                  <div className="text-xs text-[color:var(--app-muted-2)] font-semibold uppercase mb-1">Hình thức</div>
                  <div className="font-medium text-[color:var(--app-text)]">{formatPaymentMethod(selected)}</div>
                </div>
              </div>

              {/* Items Table */}
              <div className="mb-8">
                <h4 className="text-sm font-bold text-[color:var(--app-text)] mb-4 uppercase tracking-wide">Chi tiết khoản thu</h4>
                <div className="overflow-hidden rounded-lg border border-[color:var(--app-border)]">
                  <table className="w-full text-sm text-left">
                    <thead className="bg-[color:var(--app-bg)] text-[color:var(--app-muted)] font-semibold border-b border-[color:var(--app-border)]">
                      <tr>
                        <th className="px-6 py-3">Nội dung</th>
                        <th className="px-6 py-3 text-right">Đơn giá</th>
                        <th className="px-6 py-3 text-center">SL</th>
                        <th className="px-6 py-3 text-right">Thành tiền</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[color:var(--app-border)]">
                      {Array.isArray(selected.details) && selected.details.length > 0 ? (
                        selected.details.map((d) => (
                          <tr key={d.id} className="hover:bg-[color:var(--app-bg)]">
                            <td className="px-6 py-4 font-medium text-[color:var(--app-text)]">{d.description}</td>
                            <td className="px-6 py-4 text-right text-[color:var(--app-muted)]">{formatMoney(d.unitPrice)}</td>
                            <td className="px-6 py-4 text-center text-[color:var(--app-muted)]">{d.quantity}</td>
                            <td className="px-6 py-4 text-right font-bold text-[color:var(--app-text)]">{formatMoney(d.amount)} ₫</td>
                          </tr>
                        ))
                      ) : (
                        <tr><td colSpan="4" className="px-6 py-4 text-center text-[color:var(--app-muted-2)] italic">Không có chi tiết hiển thị</td></tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>

              {/* Footer / Actions */}
              <div className="flex flex-col md:flex-row items-center justify-between gap-6 bg-[color:var(--app-bg)] p-6 rounded-xl border border-[color:var(--app-border)]">
                 <div className="text-[color:var(--app-muted)] text-sm max-w-lg">
                    {selected.status !== 'PAID' && (
                      <p>
                        Nếu bạn muốn thanh toán bằng <span className="font-bold text-[color:var(--app-text)]">tiền mặt</span>, vui lòng liên hệ kế toán và cung cấp mã hóa đơn <span className="font-bold">#{selected.id}</span>.
                      </p>
                    )}
                 </div>
                 <div className="flex items-center gap-6 w-full md:w-auto justify-between md:justify-end">
                    <div className="text-right">
                      <div className="text-xs text-[color:var(--app-muted)] font-semibold uppercase">Tổng cộng</div>
                      <div className="text-2xl font-bold text-[color:var(--app-primary)]">{formatMoney(selected.amount)} ₫</div>
                    </div>
                    {selected.status !== 'PAID' && (
                      <button
                        onClick={() => handlePay(selected.id, false)}
                        disabled={processingId === selected.id}
                        className="bg-[color:var(--app-primary)] hover:bg-[color:var(--app-primary-hover)] disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg text-sm font-bold shadow-lg transition-all transform active:scale-95"
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
        <div className="bg-[color:var(--app-surface-solid)] rounded-xl shadow-sm border border-[color:var(--app-border)] overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-[color:var(--app-bg)] border-b border-[color:var(--app-border)]">
                  <th className="px-6 py-4 text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider">Kỳ thu phí</th>
                  <th className="px-6 py-4 text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider">Hạn thanh toán</th>
                  <th className="px-6 py-4 text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider text-right">Tổng tiền</th>
                  <th className="px-6 py-4 text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider text-center">Trạng thái</th>
                  <th className="px-6 py-4 text-xs font-bold text-[color:var(--app-muted-2)] uppercase tracking-wider text-center">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[color:var(--app-border)]">
                {invoices.map((inv) => (
                  <tr key={inv.id} className={`group transition-colors ${selected?.id === inv.id ? 'bg-[color:var(--app-primary-soft)]' : 'hover:bg-[color:var(--app-bg)]'}`}>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-[color:var(--app-text)]">{formatPeriod(inv)}</div>
                      <div className="text-xs text-[color:var(--app-muted-2)] mt-1">ID: #{inv.id}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-[color:var(--app-muted)]">
                      {formatDate(inv.dueDate)}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <span className="font-bold text-[color:var(--app-text)] text-sm">{formatMoney(inv.amount)} ₫</span>
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
                          className="text-[color:var(--app-text)] hover:text-[color:var(--app-primary)] text-xs font-bold border border-[color:var(--app-border)] px-3 py-1.5 rounded bg-[color:var(--app-surface-solid)] hover:bg-[color:var(--app-bg)] transition-all"
                        >
                          Xem chi tiết
                        </button>
                        {inv.status !== 'PAID' && (
                           <button
                             onClick={() => handlePay(inv.id, false)}
                             disabled={processingId === inv.id}
                             className="text-[color:var(--app-primary)] hover:text-[color:var(--app-primary-hover)] text-xs font-bold hover:underline disabled:opacity-50 disabled:no-underline"
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
                      <div className="text-[color:var(--app-muted-2)] text-sm">Hiện tại bạn chưa có hóa đơn nào cần thanh toán.</div>
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
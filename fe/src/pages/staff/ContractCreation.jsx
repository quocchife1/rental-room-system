import React, { useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import contractApi from '../../api/contractApi';
import reservationApi from '../../api/reservationApi';
import userApi from '../../api/userApi';
import systemConfigApi from '../../api/systemConfigApi';

export default function ContractCreation(){
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);

  const reservationId = useMemo(() => {
    const sp = new URLSearchParams(location.search);
    const raw = sp.get('reservationId');
    const n = raw ? Number(raw) : null;
    return Number.isFinite(n) ? n : null;
  }, [location.search]);

  const contractId = useMemo(() => {
    const sp = new URLSearchParams(location.search);
    const raw = sp.get('contractId');
    const n = raw ? Number(raw) : null;
    return Number.isFinite(n) ? n : null;
  }, [location.search]);

  const [fixedBranchCode, setFixedBranchCode] = useState('');
  const [form, setForm] = useState({
    branchCode:'', roomNumber:'',
    tenantId: null,
    tenantFullName:'', tenantPhoneNumber:'', tenantEmail:'', tenantAddress:'',
    tenantCccd:'', studentId:'', university:'', deposit:'', startDate:'', endDate:''
  });
  const [creating, setCreating] = useState(false);
  const [created, setCreated] = useState(null); // ContractResponse
  const [uploading, setUploading] = useState(false);
  const [prefillLoading, setPrefillLoading] = useState(false);
  const [contractLoading, setContractLoading] = useState(false);
  const [pendingSignedFile, setPendingSignedFile] = useState(null);
  const [pendingSignedPreviewUrl, setPendingSignedPreviewUrl] = useState('');
  const [uploadInputKey, setUploadInputKey] = useState(1);

  const [depositForm, setDepositForm] = useState({
    method: 'CASH',
    amount: '',
    reference: ''
  });

  const [systemConfig, setSystemConfig] = useState(null);

  const isCompletionMode = !!contractId;
  const canEditContract = created?.status === 'PENDING' || !created?.status;
  const canUploadSigned = created?.status === 'PENDING' || !created?.status;
  const canConfirmDeposit = created?.status === 'SIGNED_PENDING_DEPOSIT';

  const isStaffRole = useMemo(() => {
    const role = String(user?.role || '').toUpperCase();
    return ['MANAGER', 'ACCOUNTANT', 'RECEPTIONIST', 'MAINTENANCE', 'SECURITY', 'ADMIN'].includes(role);
  }, [user?.role]);

  const isBranchLocked = isStaffRole && !!fixedBranchCode;

  useEffect(() => {
    if (!isStaffRole) return;
    if (!user?.id) return;

    const loadMyBranch = async () => {
      try {
        const res = await userApi.getEmployeeProfile(user.id);
        const data = res?.data?.result || res?.data || res;
        const branchCode = data?.branch?.branchCode || '';
        if (!branchCode) return;
        setFixedBranchCode(branchCode);
      } catch (e) {
        console.error('Không thể tải chi nhánh nhân viên', e);
      }
    };

    loadMyBranch();
  }, [isStaffRole, user?.id]);

  useEffect(() => {
    if (!fixedBranchCode) return;
    setForm((prev) => ({
      ...prev,
      branchCode: fixedBranchCode,
    }));
  }, [fixedBranchCode]);

  useEffect(() => {
    if (!reservationId) return;
    if (contractId) return;
    const loadPrefill = async () => {
      setPrefillLoading(true);
      try {
        const res = await reservationApi.getContractPrefill(reservationId);
        const data = res?.data?.result || res?.data || res;
        if (!data) return;
        setForm((prev) => ({
          ...prev,
          branchCode: data.branchCode || prev.branchCode,
          roomNumber: data.roomNumber || prev.roomNumber,
          tenantId: data.tenantId ?? prev.tenantId,
          tenantFullName: data.tenantFullName || prev.tenantFullName,
          tenantPhoneNumber: data.tenantPhoneNumber || prev.tenantPhoneNumber,
          tenantEmail: data.tenantEmail || prev.tenantEmail,
          tenantAddress: data.tenantAddress || prev.tenantAddress,
          tenantCccd: data.tenantCccd || prev.tenantCccd,
          studentId: data.studentId || prev.studentId,
          university: data.university || prev.university,
          deposit: (data.deposit ?? prev.deposit) !== null ? String(data.deposit ?? prev.deposit ?? '') : prev.deposit,
          startDate: data.startDate || prev.startDate,
          endDate: data.endDate || prev.endDate,
        }));
      } catch (e) {
        console.error('Lỗi tải prefill hợp đồng', e);
        alert('Không thể tải dữ liệu prefill từ phiếu giữ phòng');
      } finally {
        setPrefillLoading(false);
      }
    };
    loadPrefill();
  }, [reservationId, contractId]);

  useEffect(() => {
    const loadCfg = async () => {
      try {
        const res = await systemConfigApi.get();
        const data = res?.data?.result || res?.data || res;
        setSystemConfig(data || null);
      } catch (e) {
        // best-effort: deposit step can still work without showing transfer instructions
        setSystemConfig(null);
      }
    };
    loadCfg();
  }, []);

  useEffect(() => {
    if (created?.status !== 'SIGNED_PENDING_DEPOSIT') return;
    setDepositForm((prev) => ({
      ...prev,
      amount: prev.amount || (created?.deposit != null ? String(created.deposit) : ''),
    }));
  }, [created?.status, created?.deposit]);

  useEffect(() => {
    if (!contractId) return;
    const loadContract = async () => {
      setContractLoading(true);
      try {
        const res = await contractApi.getById(contractId);
        const data = res?.data?.result || res?.data || res;
        if (data) {
          setCreated(data);
          setForm((prev) => ({
            ...prev,
            branchCode: data.branchCode || prev.branchCode,
            roomNumber: data.roomNumber || prev.roomNumber,
            tenantId: data.tenantId ?? prev.tenantId,
            tenantFullName: data.tenantName || prev.tenantFullName,
            tenantPhoneNumber: data.tenantPhoneNumber || prev.tenantPhoneNumber,
            tenantEmail: data.tenantEmail || prev.tenantEmail,
            tenantAddress: data.tenantAddress || prev.tenantAddress,
            tenantCccd: data.tenantCccd || prev.tenantCccd,
            studentId: data.studentId || prev.studentId,
            university: data.university || prev.university,
            deposit: (data.deposit ?? prev.deposit) !== null ? String(data.deposit ?? prev.deposit ?? '') : prev.deposit,
            startDate: data.startDate || prev.startDate,
            endDate: data.endDate || prev.endDate,
          }));
        }
      } catch (e) {
        console.error('Lỗi tải hợp đồng', e);
        alert('Không thể tải chi tiết hợp đồng');
      } finally {
        setContractLoading(false);
      }
    };
    loadContract();
  }, [contractId]);

  const validateBeforeCreate = () => {
    const required = [
      { key: 'branchCode', label: 'Chi nhánh' },
      { key: 'roomNumber', label: 'Số phòng' },
      { key: 'tenantFullName', label: 'Họ tên' },
      { key: 'tenantPhoneNumber', label: 'Số điện thoại' },
      { key: 'tenantEmail', label: 'Email' },
      { key: 'tenantCccd', label: 'CCCD' },
      { key: 'endDate', label: 'Ngày kết thúc' },
    ];

    const missing = required
      .filter((f) => !String(form[f.key] ?? '').trim())
      .map((f) => f.label);

    if (missing.length > 0) {
      alert(`Vui lòng nhập đầy đủ: ${missing.join(', ')}`);
      return false;
    }
    return true;
  };

  const handleUpdate = async () => {
    if (!contractId) return;
    if (!validateBeforeCreate()) return;
    if (created?.status && created.status !== 'PENDING') {
      alert('Chỉ có thể chỉnh sửa khi hợp đồng còn PENDING.');
      return;
    }

    setCreating(true);
    try {
      const payload = {
        tenantFullName: form.tenantFullName,
        tenantPhoneNumber: form.tenantPhoneNumber,
        tenantEmail: form.tenantEmail,
        tenantAddress: form.tenantAddress,
        tenantCccd: form.tenantCccd,
        studentId: form.studentId,
        university: form.university,
        deposit: form.deposit ? Number(form.deposit) : 0,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
      };
      const res = await contractApi.update(contractId, payload);
      const data = res?.data?.result || res?.data || res;
      setCreated(data);
      alert('Đã lưu thay đổi hợp đồng');
    } catch (e) {
      console.error(e);
      alert('Không thể cập nhật hợp đồng');
    } finally {
      setCreating(false);
    }
  };

  const renderSignedPreview = () => {
    const url = created?.signedContractUrl;
    if (!url) return null;
    const lower = String(url).toLowerCase();
    const isImage = lower.endsWith('.png') || lower.endsWith('.jpg') || lower.endsWith('.jpeg') || lower.endsWith('.webp') || lower.endsWith('.gif');
    const isPdf = lower.endsWith('.pdf');
    return (
      <div className="mt-6">
        <h3 className="font-semibold mb-2">Hợp đồng đã ký</h3>
        <div className="border rounded-lg p-4 bg-gray-50">
          <div className="text-sm mb-3">
            <a className="text-indigo-600 underline" href={url} target="_blank" rel="noreferrer">Mở file đã ký</a>
          </div>
          {isImage && (
            <img src={url} alt="signed-contract" className="max-h-[360px] w-auto rounded border bg-white" />
          )}
          {isPdf && (
            <div className="text-sm text-gray-600">File PDF (mở bằng link ở trên).</div>
          )}
          {!isImage && !isPdf && (
            <div className="text-sm text-gray-600">Đã tải lên file đã ký.</div>
          )}
        </div>
      </div>
    );
  };

  const handleCreate = async ()=>{
    if (!validateBeforeCreate()) return;
    setCreating(true);
    setCreated(null);
    try{
      const payload = {
        branchCode: form.branchCode,
        roomNumber: form.roomNumber,
        tenantId: form.tenantId || null,
        tenantFullName: form.tenantFullName,
        tenantPhoneNumber: form.tenantPhoneNumber,
        tenantEmail: form.tenantEmail,
        tenantAddress: form.tenantAddress,
        tenantCccd: form.tenantCccd,
        studentId: form.studentId,
        university: form.university,
        deposit: form.deposit ? Number(form.deposit) : 0,
        startDate: form.startDate || null,
        endDate: form.endDate || null
      };
      const res = await contractApi.createContract(payload);
      const data = res?.data?.result || res?.data || res;
      setCreated(data);
      alert('Tạo hợp đồng thành công');

      // Nếu lập hợp đồng từ phiếu giữ phòng: đánh dấu phiếu đã lập hợp đồng
      if (reservationId) {
        try {
          await reservationApi.markContracted(reservationId);
        } catch (e) {
          // best-effort; không chặn luồng tạo hợp đồng
          console.warn('Không thể đánh dấu phiếu đã lập hợp đồng', e);
        }
      }
    }catch(e){ console.error(e); alert('Không thể tạo hợp đồng'); }
    finally{ setCreating(false); }
  };

  const handleDownload = async ()=>{
    if(!created?.id){ return; }
    try{
      const res = await contractApi.downloadContract(created.id);
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `contract_${created.id}.docx`;
      a.click();
      window.URL.revokeObjectURL(url);
    }catch(e){ console.error(e); alert('Không thể tải hợp đồng'); }
  };

  const handleUploadSigned = async (e)=>{
    if(!created?.id){ return; }
    if (created?.status && created.status !== 'PENDING') {
      alert('Hợp đồng không còn ở trạng thái chờ ký.');
      return;
    }
    const file = e.target.files?.[0];
    if(!file) return;

    // allow selecting the same file again later
    try { e.target.value = ''; } catch {}

    // cleanup old preview
    if (pendingSignedPreviewUrl) {
      try { window.URL.revokeObjectURL(pendingSignedPreviewUrl); } catch {}
    }

    setPendingSignedFile(file);
    // only images can be previewed safely
    const isImage = file.type?.startsWith('image/');
    if (isImage) {
      setPendingSignedPreviewUrl(window.URL.createObjectURL(file));
    } else {
      setPendingSignedPreviewUrl('');
    }
  };

  const clearPendingSigned = () => {
    setPendingSignedFile(null);
    if (pendingSignedPreviewUrl) {
      try { window.URL.revokeObjectURL(pendingSignedPreviewUrl); } catch {}
    }
    setPendingSignedPreviewUrl('');
    setUploadInputKey((k) => k + 1);
  };

  const confirmUploadSigned = async () => {
    if (!created?.id) return;
    if (!pendingSignedFile) {
      alert('Vui lòng chọn file trước khi xác nhận.');
      return;
    }
    if (created?.status && created.status !== 'PENDING') {
      alert('Hợp đồng không còn ở trạng thái chờ ký.');
      return;
    }

    setUploading(true);
    try {
      const res = await contractApi.uploadSigned(created.id, pendingSignedFile);
      const data = res?.data?.result || res?.data || res;
      setCreated(data);
      setPendingSignedFile(null);
      if (pendingSignedPreviewUrl) {
        try { window.URL.revokeObjectURL(pendingSignedPreviewUrl); } catch {}
      }
      setPendingSignedPreviewUrl('');
      alert('Đã tải hợp đồng đã ký. Hợp đồng đang chờ thanh toán tiền cọc.');
    } catch (err) {
      console.error(err);
      alert('Không thể tải lên hợp đồng đã ký');
    } finally {
      setUploading(false);
    }
  };

  const confirmDepositPayment = async () => {
    if (!created?.id) return;
    if (created?.status !== 'SIGNED_PENDING_DEPOSIT') {
      alert('Hợp đồng chưa ở trạng thái chờ thanh toán tiền cọc.');
      return;
    }

    if (depositForm.method === 'MOMO') {
      alert('Vui lòng dùng nút “Thanh toán MoMo” để tạo link và thanh toán.');
      return;
    }

    const payload = {
      method: depositForm.method,
      amount: String(depositForm.amount ?? '').trim() ? Number(depositForm.amount) : null,
      reference: depositForm.reference || null,
    };

    setUploading(true);
    try {
      const res = await contractApi.confirmDepositPayment(created.id, payload);
      const data = res?.data?.result || res?.data || res;
      setCreated(data);
      alert('Đã xác nhận tiền cọc. Hợp đồng đã có hiệu lực.');
    } catch (e) {
      console.error(e);
      alert('Không thể xác nhận thanh toán tiền cọc');
    } finally {
      setUploading(false);
    }
  };

  const initiateMomoDeposit = async () => {
    if (!created?.id) return;
    if (created?.status !== 'SIGNED_PENDING_DEPOSIT') {
      alert('Hợp đồng chưa ở trạng thái chờ thanh toán tiền cọc.');
      return;
    }

    const payload = {
      amount: String(depositForm.amount ?? '').trim() ? Number(depositForm.amount) : null,
      returnPath: `/staff/contracts/create?contractId=${created.id}`,
    };

    setUploading(true);
    try {
      const res = await contractApi.initiateDepositMomo(created.id, payload);
      const data = res?.data?.result || res?.data || res;
      const payUrl = data?.payUrl;
      if (!payUrl) {
        alert('Không thể khởi tạo thanh toán MoMo');
        return;
      }
      window.open(payUrl, '_blank', 'noopener,noreferrer');
      alert('Đã mở trang thanh toán MoMo. Sau khi thanh toán thành công, hệ thống sẽ tự kích hoạt hợp đồng.');
    } catch (e) {
      console.error(e);
      alert('Không thể khởi tạo thanh toán MoMo');
    } finally {
      setUploading(false);
    }
  };

  return (
      <div className="container mx-auto px-6 py-8">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6">
          <h1 className="text-2xl font-bold">{isCompletionMode ? 'Hoàn thiện hợp đồng' : 'Tạo hợp đồng'}</h1>
          <div className="flex gap-2">
            <button className="px-4 py-2 rounded-lg border text-sm" onClick={() => navigate('/staff/contracts')}>Danh sách hợp đồng</button>
          </div>
        </div>

        <div className="bg-white rounded-xl border p-6 max-w-4xl mx-auto">
          {prefillLoading && (
            <div className="mb-4 text-sm text-gray-600">Đang tải dữ liệu từ phiếu giữ phòng...</div>
          )}
          {contractLoading && (
            <div className="mb-4 text-sm text-gray-600">Đang tải chi tiết hợp đồng...</div>
          )}
          {isCompletionMode && created?.status === 'SIGNED_PENDING_DEPOSIT' && (
            <div className="mb-4 text-sm text-gray-600">Hợp đồng đã ký và đang chờ thanh toán tiền cọc để kích hoạt.</div>
          )}
          {isCompletionMode && created?.status && created.status !== 'PENDING' && created.status !== 'SIGNED_PENDING_DEPOSIT' && (
            <div className="mb-4 text-sm text-gray-600">Hợp đồng đã {created.status === 'ACTIVE' ? 'có hiệu lực' : 'kết thúc/hủy'}; không cần tương tác thêm.</div>
          )}

          <h3 className="font-semibold mb-2">Thông tin phòng</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm mb-1">Chi nhánh</label>
              <input
                className="w-full border rounded px-3 py-2 disabled:bg-gray-50"
                placeholder="CN01"
                value={form.branchCode}
                disabled={isCompletionMode || isBranchLocked}
                readOnly={isBranchLocked}
                onChange={e=>setForm({ ...form, branchCode: e.target.value })}
              />
              {isBranchLocked && (
                <div className="mt-1 text-xs text-gray-500">Chi nhánh được cố định theo nhân viên.</div>
              )}
            </div>
            <div>
              <label className="block text-sm mb-1">Số phòng</label>
              <input
                className="w-full border rounded px-3 py-2 disabled:bg-gray-50"
                placeholder="P101"
                value={form.roomNumber}
                disabled={isCompletionMode || !!reservationId}
                onChange={e=>setForm({ ...form, roomNumber: e.target.value })}
              />
              {!!reservationId && (
                <div className="mt-1 text-xs text-gray-500">Lập hợp đồng từ phiếu: phòng được cố định theo phiếu.</div>
              )}
            </div>
          </div>

          <h3 className="font-semibold mt-6 mb-2">Thông tin khách hàng</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm mb-1">Họ tên</label>
              <input className="w-full border rounded px-3 py-2" value={form.tenantFullName} onChange={e=>setForm({ ...form, tenantFullName: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">Số điện thoại</label>
              <input className="w-full border rounded px-3 py-2" value={form.tenantPhoneNumber} onChange={e=>setForm({ ...form, tenantPhoneNumber: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">Email</label>
              <input className="w-full border rounded px-3 py-2" value={form.tenantEmail} onChange={e=>setForm({ ...form, tenantEmail: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">Địa chỉ</label>
              <input className="w-full border rounded px-3 py-2" value={form.tenantAddress} onChange={e=>setForm({ ...form, tenantAddress: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">CCCD</label>
              <input className="w-full border rounded px-3 py-2" value={form.tenantCccd} onChange={e=>setForm({ ...form, tenantCccd: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">Mã SV</label>
              <input className="w-full border rounded px-3 py-2" value={form.studentId} onChange={e=>setForm({ ...form, studentId: e.target.value })} />
            </div>
            <div className="col-span-2">
              <label className="block text-sm mb-1">Trường/Đơn vị</label>
              <input className="w-full border rounded px-3 py-2" value={form.university} onChange={e=>setForm({ ...form, university: e.target.value })} />
            </div>
          </div>

          <h3 className="font-semibold mt-6 mb-2">Thông tin hợp đồng</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm mb-1">Tiền cọc</label>
              <input type="number" className="w-full border rounded px-3 py-2" value={form.deposit} onChange={e=>setForm({ ...form, deposit: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">Ngày bắt đầu</label>
              <input type="date" className="w-full border rounded px-3 py-2" value={form.startDate} onChange={e=>setForm({ ...form, startDate: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm mb-1">Ngày kết thúc</label>
              <input type="date" className="w-full border rounded px-3 py-2" value={form.endDate} onChange={e=>setForm({ ...form, endDate: e.target.value })} />
            </div>
          </div>

          <div className="flex flex-wrap gap-3 justify-end mt-6">
            {!isCompletionMode && (
              <button disabled={creating} className="px-4 py-2 rounded bg-indigo-600 text-white" onClick={handleCreate}>Lưu & Tạo hợp đồng</button>
            )}
            {isCompletionMode && canEditContract && (
              <button disabled={creating} className="px-4 py-2 rounded bg-indigo-600 text-white" onClick={handleUpdate}>Lưu thay đổi</button>
            )}
            <button disabled={!created?.id} className="px-4 py-2 rounded bg-emerald-600 text-white disabled:opacity-50" onClick={handleDownload}>Tải hợp đồng (DOCX)</button>
            <label className={`px-4 py-2 rounded bg-purple-600 text-white cursor-pointer ${!created?.id || !canUploadSigned ? 'opacity-50 cursor-not-allowed' : ''}`}>
              Tải lên hợp đồng đã ký
              <input key={uploadInputKey} type="file" accept="image/*,application/pdf" className="hidden" disabled={!created?.id || uploading || !canUploadSigned} onChange={handleUploadSigned} />
            </label>
            <button
              disabled={!created?.id || !canUploadSigned || uploading || !pendingSignedFile}
              className="px-4 py-2 rounded bg-indigo-600 text-white disabled:opacity-50"
              onClick={confirmUploadSigned}
            >
              Xác nhận tải lên
            </button>
            <button
              disabled={!pendingSignedFile || uploading}
              className="px-4 py-2 rounded border text-sm disabled:opacity-50"
              onClick={clearPendingSigned}
            >
              Bỏ chọn file
            </button>
          </div>

          {pendingSignedFile && (
            <div className="mt-4">
              <h3 className="font-semibold mb-2">File đã chọn (chưa upload)</h3>
              <div className="border rounded-lg p-4 bg-gray-50">
                <div className="text-sm mb-2">{pendingSignedFile.name}</div>
                {pendingSignedPreviewUrl ? (
                  <img src={pendingSignedPreviewUrl} alt="preview" className="max-h-[360px] w-auto rounded border bg-white" />
                ) : (
                  <div className="text-sm text-gray-600">Không hỗ trợ preview định dạng này. Nhấn “Xác nhận tải lên” để upload.</div>
                )}
                <div className="mt-3 text-xs text-gray-600">Bạn có thể chọn file khác hoặc bấm “Bỏ chọn file” trước khi xác nhận.</div>
              </div>
            </div>
          )}

          {(created?.status === 'SIGNED_PENDING_DEPOSIT' || created?.depositInvoiceUrl || created?.depositReceiptUrl) && (
            <div className="mt-6">
              <h3 className="font-semibold mb-2">Thanh toán tiền cọc</h3>

              {created?.status === 'SIGNED_PENDING_DEPOSIT' && (
                <div className="border rounded-lg p-4 bg-gray-50">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm mb-1">Phương thức</label>
                      <select
                        className="w-full border rounded px-3 py-2"
                        value={depositForm.method}
                        onChange={(e) => setDepositForm((p) => ({ ...p, method: e.target.value }))}
                      >
                        <option value="CASH">Tiền mặt</option>
                        <option value="BANK_TRANSFER">Chuyển khoản</option>
                        <option value="MOMO">MoMo</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-sm mb-1">Số tiền cọc</label>
                      <input
                        type="number"
                        className="w-full border rounded px-3 py-2"
                        value={depositForm.amount}
                        onChange={(e) => setDepositForm((p) => ({ ...p, amount: e.target.value }))}
                      />
                    </div>
                    <div className="col-span-2">
                      <label className="block text-sm mb-1">Mã tham chiếu / ghi chú</label>
                      <input
                        className="w-full border rounded px-3 py-2"
                        placeholder="VD: CK MB 123456 / Thu tiền mặt"
                        value={depositForm.reference}
                        onChange={(e) => setDepositForm((p) => ({ ...p, reference: e.target.value }))}
                      />
                    </div>
                  </div>

                  {depositForm.method === 'BANK_TRANSFER' && (
                    <div className="mt-3 text-sm text-gray-700">
                      <div className="font-medium mb-1">Thông tin chuyển khoản (MoMo)</div>
                      <div>Tên người nhận: <span className="font-medium">{systemConfig?.momoReceiverName || '-'}</span></div>
                      <div>Số điện thoại: <span className="font-medium">{systemConfig?.momoReceiverPhone || '-'}</span></div>
                      {systemConfig?.momoReceiverQrUrl ? (
                        <div className="mt-2">
                          <a className="text-indigo-600 underline" href={systemConfig.momoReceiverQrUrl} target="_blank" rel="noreferrer">Mở QR MoMo</a>
                        </div>
                      ) : null}
                      <div className="mt-2 text-xs text-gray-600">Gợi ý nội dung chuyển khoản: mã hợp đồng hoặc mã tham chiếu.</div>
                    </div>
                  )}

                  {depositForm.method === 'MOMO' && (
                    <div className="mt-3 text-sm text-gray-700">
                      <div className="text-xs text-gray-600">Thanh toán MoMo sẽ tự động xác nhận khi MoMo gửi IPN về hệ thống.</div>
                    </div>
                  )}

                  <div className="flex gap-3 justify-end mt-4">
                    {depositForm.method === 'MOMO' ? (
                      <button
                        disabled={uploading}
                        className="px-4 py-2 rounded bg-indigo-600 text-white disabled:opacity-50"
                        onClick={initiateMomoDeposit}
                      >
                        Thanh toán MoMo
                      </button>
                    ) : (
                      <button
                        disabled={uploading}
                        className="px-4 py-2 rounded bg-indigo-600 text-white disabled:opacity-50"
                        onClick={confirmDepositPayment}
                      >
                        Xác nhận đã thu tiền cọc
                      </button>
                    )}
                  </div>
                </div>
              )}

              {(created?.depositInvoiceUrl || created?.depositReceiptUrl) && (
                <div className="mt-3 text-sm">
                  {created?.depositInvoiceUrl && (
                    <div>
                      <a className="text-indigo-600 underline" href={created.depositInvoiceUrl} target="_blank" rel="noreferrer">In hóa đơn tiền cọc</a>
                    </div>
                  )}
                  {created?.depositReceiptUrl && (
                    <div>
                      <a className="text-indigo-600 underline" href={created.depositReceiptUrl} target="_blank" rel="noreferrer">In biên bản nhận tiền cọc</a>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {renderSignedPreview()}
        </div>
      </div>
  );
}

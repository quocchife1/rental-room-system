import React, { useEffect, useMemo, useState } from 'react';
import contractApi from '../../../api/contractApi';
import contractServicesApi from '../../../api/contractServicesApi';

export default function MeterReadingsPage() {
  const PAGE_SIZE = 200;
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [savingAll, setSavingAll] = useState(false);
  const [saveAllProgress, setSaveAllProgress] = useState({ done: 0, total: 0 });
  const [error, setError] = useState('');
  
  // State cho tìm kiếm
  const [searchTerm, setSearchTerm] = useState('');

  // 1. Logic lọc danh sách theo từ khóa tìm kiếm
  const filteredRows = useMemo(() => {
    if (!searchTerm) return rows;
    const lowerTerm = searchTerm.toLowerCase().trim();
    return rows.filter(
      (r) =>
        r.roomNumber?.toLowerCase().includes(lowerTerm) ||
        r.tenantName?.toLowerCase().includes(lowerTerm)
    );
  }, [rows, searchTerm]);

  const dirtyCount = useMemo(() => rows.filter((r) => r.dirty).length, [rows]);

  function normalizeServiceName(name) {
    return (name || '').trim().toLowerCase();
  }

  function buildPayload(previousReading, currentReading) {
    const payload = {};
    if (previousReading !== null && previousReading !== undefined && previousReading !== '') {
      payload.previousReading = Number(previousReading);
    }
    if (currentReading !== null && currentReading !== undefined && currentReading !== '') {
      payload.currentReading = Number(currentReading);
    }
    return payload;
  }

  async function mapWithConcurrency(items, limit, mapper) {
    const results = new Array(items.length);
    let nextIndex = 0;

    async function worker() {
      while (true) {
        const i = nextIndex;
        nextIndex += 1;
        if (i >= items.length) return;
        results[i] = await mapper(items[i], i);
      }
    }

    const workers = Array.from({ length: Math.min(limit, items.length) }, () => worker());
    await Promise.all(workers);
    return results;
  }

  async function loadActiveContracts() {
    setLoading(true);
    setError('');
    try {
      const page = await contractApi.getMyBranchContracts({ status: 'ACTIVE', page: 0, size: PAGE_SIZE });
      const contracts = Array.isArray(page?.content) ? page.content : [];

      const rowList = await mapWithConcurrency(contracts, 8, async (c) => {
        const services = await contractServicesApi.list(c.id);
        const serviceList = Array.isArray(services) ? services : services?.data ?? [];
        const electricity = serviceList.find((s) => normalizeServiceName(s.serviceName) === 'điện');
        const water = serviceList.find((s) => normalizeServiceName(s.serviceName) === 'nước');

        return {
          contractId: c.id,
          roomNumber: c.roomNumber || c.roomCode || '-',
          tenantName: c.tenantName || '-',
          branchCode: c.branchCode || '-',
          electricityServiceId: electricity?.id ?? null,
          electricityPrev: electricity?.previousReading ?? null,
          electricityCurr: electricity?.currentReading ?? null,
          waterServiceId: water?.id ?? null,
          waterPrev: water?.previousReading ?? null,
          waterCurr: water?.currentReading ?? null,
          dirty: false,
          saving: false,
          lastSavedAt: null,
          rowError: '',
        };
      });

      setRows(rowList);
    } catch (e) {
      setRows([]);
      setError(e?.response?.data?.message || e?.message || 'Tải dữ liệu thất bại');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadActiveContracts();
  }, []);

  function setRow(contractId, patch) {
    setRows((prev) =>
      prev.map((r) =>
        String(r.contractId) === String(contractId)
          ? {
              ...r,
              ...patch,
              dirty: patch.hasOwnProperty('dirty') ? patch.dirty : r.dirty,
              rowError: patch.hasOwnProperty('rowError') ? patch.rowError : r.rowError,
            }
          : r
      )
    );
  }

  function setRowDirty(contractId, patch) {
    setRow(contractId, { ...patch, dirty: true });
  }

  async function saveOneRow(contractId) {
    const row = rows.find((r) => String(r.contractId) === String(contractId));
    if (!row) return;

    setRow(contractId, { saving: true, rowError: '' });
    try {
      if (row.electricityServiceId) {
        const payload = buildPayload(row.electricityPrev, row.electricityCurr);
        if (Object.keys(payload).length > 0) {
          await contractServicesApi.updateMeterReading(row.contractId, row.electricityServiceId, payload);
        }
      }

      if (row.waterServiceId) {
        const payload = buildPayload(row.waterPrev, row.waterCurr);
        if (Object.keys(payload).length > 0) {
          await contractServicesApi.updateMeterReading(row.contractId, row.waterServiceId, payload);
        }
      }

      setRow(contractId, { dirty: false, lastSavedAt: new Date().toISOString() });
    } catch (e) {
      setRow(contractId, {
        rowError: e?.response?.data?.message || e?.message || 'Lưu thất bại',
      });
    } finally {
      setRow(contractId, { saving: false });
    }
  }

  async function saveAll() {
    const targets = rows.filter((r) => r.dirty && !r.saving);
    if (targets.length === 0) return;

    setSavingAll(true);
    setSaveAllProgress({ done: 0, total: targets.length });
    setError('');

    try {
      for (let i = 0; i < targets.length; i += 1) {
        await saveOneRow(targets[i].contractId);
        setSaveAllProgress({ done: i + 1, total: targets.length });
      }
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Lưu hàng loạt thất bại');
    } finally {
      setSavingAll(false);
    }
  }

  // Helper: Tính số tiêu thụ để hiển thị gợi ý
  const calculateUsage = (curr, prev) => {
    if (curr === null || prev === null || curr === '' || prev === '') return null;
    const usage = Number(curr) - Number(prev);
    return usage;
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-24 font-sans text-gray-800">
      
      {/* --- Sticky Header --- */}
      <div className="bg-white border-b sticky top-0 z-30 shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <h1 className="text-xl font-bold text-gray-900">Ghi Chỉ Số Điện Nước</h1>
              <p className="text-sm text-gray-500 mt-1">
                Danh sách hợp đồng đang thuê (Active)
              </p>
            </div>
            
            <div className="flex items-center gap-2 w-full md:w-auto">
              {/* Input tìm kiếm */}
              <input 
                type="text"
                placeholder="Tìm phòng hoặc tên khách..."
                className="border border-gray-300 rounded px-3 py-2 text-sm w-full md:w-64 focus:outline-none focus:border-blue-500 shadow-sm"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
              <button
                className="border border-gray-300 bg-gray-50 text-gray-700 font-medium rounded px-4 py-2 text-sm hover:bg-gray-100 whitespace-nowrap shadow-sm"
                onClick={loadActiveContracts}
                disabled={loading || savingAll}
              >
                {loading ? 'Đang tải...' : 'Tải lại'}
              </button>
            </div>
          </div>
          
          {error && (
            <div className="mt-3 bg-red-50 text-red-700 px-4 py-2 rounded text-sm border border-red-200 font-medium">
              Hệ thống: {error}
            </div>
          )}
        </div>
      </div>

      {/* --- Main Table Content --- */}
      <div className="container mx-auto px-4 py-6">
        <div className="bg-white rounded-lg border shadow-sm overflow-hidden">
          <div className="overflow-x-auto max-h-[75vh]">
            <table className="w-full text-sm text-left border-collapse">
              <thead className="text-xs text-gray-700 uppercase bg-gray-100 sticky top-0 z-20 shadow-sm">
                <tr>
                  <th className="px-4 py-3 font-bold border-b w-12 text-center bg-gray-100">STT</th>
                  <th className="px-4 py-3 font-bold border-b w-48 bg-gray-100">Hợp đồng</th>
                  
                  {/* Nhóm cột Điện (Màu xanh dương nhạt) */}
                  <th className="px-2 py-3 font-bold border-b border-l border-r text-center bg-blue-50 text-blue-900 w-[320px]" colSpan={2}>
                    ĐIỆN (Kwh)
                  </th>
                  
                  {/* Nhóm cột Nước (Màu xanh ngọc nhạt) */}
                  <th className="px-2 py-3 font-bold border-b border-r text-center bg-cyan-50 text-cyan-900 w-[320px]" colSpan={2}>
                    NƯỚC (Khối)
                  </th>
                  
                  <th className="px-4 py-3 font-bold border-b text-center bg-gray-100 w-32">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {loading ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-gray-500">
                      Đang tải dữ liệu...
                    </td>
                  </tr>
                ) : filteredRows.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="px-6 py-12 text-center text-gray-500">
                      {searchTerm ? 'Không tìm thấy kết quả phù hợp.' : 'Không có hợp đồng nào.'}
                    </td>
                  </tr>
                ) : (
                  filteredRows.map((row, idx) => {
                    const elecUsage = calculateUsage(row.electricityCurr, row.electricityPrev);
                    const waterUsage = calculateUsage(row.waterCurr, row.waterPrev);

                    return (
                      <tr 
                        key={row.contractId} 
                        className={`hover:bg-gray-50 transition-colors ${row.dirty ? 'bg-yellow-50' : ''}`}
                      >
                        <td className="px-4 py-3 text-center text-gray-500 font-medium align-top pt-4">
                           {idx + 1}
                        </td>
                        <td className="px-4 py-3 align-top">
                          <div className="font-bold text-gray-900 text-base">{row.roomNumber}</div>
                          <div className="text-gray-600 truncate max-w-[180px]" title={row.tenantName}>
                            {row.tenantName}
                          </div>
                          <div className="text-xs text-gray-400 mt-1">
                            CN: {row.branchCode} <br/> HĐ: #{row.contractId}
                          </div>
                        </td>

                        {/* --- Cột Điện --- */}
                        <td className="px-2 py-3 border-l bg-blue-50/30 align-top">
                           <div className="text-xs text-gray-500 mb-1">Số cũ</div>
                           <input
                            type="number"
                            className="w-full border border-gray-300 rounded px-2 py-1.5 focus:ring-1 focus:ring-blue-500 focus:border-blue-500 outline-none bg-white text-gray-700"
                            placeholder="Cũ"
                            value={row.electricityPrev ?? ''}
                            onChange={(e) => setRowDirty(row.contractId, { electricityPrev: e.target.value === '' ? null : Number(e.target.value) })}
                            disabled={!row.electricityServiceId || row.saving || savingAll}
                          />
                        </td>
                        <td className="px-2 py-3 border-r bg-blue-50/30 align-top">
                           <div className="flex justify-between items-center mb-1">
                              <span className="text-xs text-gray-500">Số mới</span>
                              {elecUsage !== null && (
                                <span className={`text-xs font-bold ${elecUsage < 0 ? 'text-red-600' : 'text-blue-700'}`}>
                                  SD: {elecUsage}
                                </span>
                              )}
                           </div>
                           <input
                            type="number"
                            className={`w-full border rounded px-2 py-1.5 outline-none font-bold ${
                              row.electricityCurr 
                                ? 'border-blue-400 bg-blue-50 text-blue-900' 
                                : 'border-gray-300'
                            }`}
                            placeholder="Mới..."
                            value={row.electricityCurr ?? ''}
                            onChange={(e) => setRowDirty(row.contractId, { electricityCurr: e.target.value === '' ? null : Number(e.target.value) })}
                            disabled={!row.electricityServiceId || row.saving || savingAll}
                          />
                           {!row.electricityServiceId && <div className="text-xs text-red-400 mt-1 text-center italic">Chưa đăng ký</div>}
                        </td>

                        {/* --- Cột Nước --- */}
                        <td className="px-2 py-3 bg-cyan-50/30 align-top">
                           <div className="text-xs text-gray-500 mb-1">Số cũ</div>
                           <input
                            type="number"
                            className="w-full border border-gray-300 rounded px-2 py-1.5 focus:ring-1 focus:ring-cyan-500 focus:border-cyan-500 outline-none bg-white text-gray-700"
                            placeholder="Cũ"
                            value={row.waterPrev ?? ''}
                            onChange={(e) => setRowDirty(row.contractId, { waterPrev: e.target.value === '' ? null : Number(e.target.value) })}
                            disabled={!row.waterServiceId || row.saving || savingAll}
                          />
                        </td>
                        <td className="px-2 py-3 border-r bg-cyan-50/30 align-top">
                            <div className="flex justify-between items-center mb-1">
                              <span className="text-xs text-gray-500">Số mới</span>
                              {waterUsage !== null && (
                                <span className={`text-xs font-bold ${waterUsage < 0 ? 'text-red-600' : 'text-cyan-700'}`}>
                                  SD: {waterUsage}
                                </span>
                              )}
                           </div>
                           <input
                            type="number"
                            className={`w-full border rounded px-2 py-1.5 outline-none font-bold ${
                              row.waterCurr 
                                ? 'border-cyan-400 bg-cyan-50 text-cyan-900' 
                                : 'border-gray-300'
                            }`}
                            placeholder="Mới..."
                            value={row.waterCurr ?? ''}
                            onChange={(e) => setRowDirty(row.contractId, { waterCurr: e.target.value === '' ? null : Number(e.target.value) })}
                            disabled={!row.waterServiceId || row.saving || savingAll}
                          />
                           {!row.waterServiceId && <div className="text-xs text-red-400 mt-1 text-center italic">Chưa đăng ký</div>}
                        </td>

                        {/* --- Cột Thao tác --- */}
                        <td className="px-4 py-3 align-middle text-center">
                          {row.saving ? (
                            <span className="inline-block px-3 py-1 text-xs font-bold text-blue-600 bg-blue-100 rounded-full">
                              Đang lưu...
                            </span>
                          ) : row.rowError ? (
                            <div className="flex flex-col items-center">
                              <span className="inline-block px-3 py-1 text-xs font-bold text-red-600 bg-red-100 rounded-full mb-1">
                                Lỗi
                              </span>
                              <button 
                                onClick={() => saveOneRow(row.contractId)}
                                className="text-xs underline text-blue-600 hover:text-blue-800 font-medium"
                              >
                                Thử lại
                              </button>
                            </div>
                          ) : row.dirty ? (
                             <button
                                onClick={() => saveOneRow(row.contractId)}
                                className="inline-block w-full px-3 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded shadow-sm transition-all active:scale-95"
                              >
                                LƯU
                              </button>
                          ) : row.lastSavedAt ? (
                            <span className="inline-block px-3 py-1 text-xs font-bold text-green-700 bg-green-100 rounded-full border border-green-200">
                              Đã lưu
                            </span>
                          ) : (
                            <span className="text-gray-400 text-xs">-</span>
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* --- Sticky Footer Action Bar --- */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-[0_-4px_10px_-1px_rgba(0,0,0,0.1)] py-4 px-6 z-40">
        <div className="container mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
            <div className="text-sm flex items-center gap-4">
                <div>
                  <span className="text-gray-500 mr-2">Tổng hiển thị:</span>
                  <span className="font-bold text-gray-900">{filteredRows.length}</span>
                </div>
                
                {dirtyCount > 0 && (
                   <span className="text-yellow-800 bg-yellow-100 px-3 py-1 rounded-full text-xs font-bold border border-yellow-200">
                     Có {dirtyCount} thay đổi chưa lưu
                   </span>
                )}
            </div>

            <div className="w-full md:w-auto">
                <button 
                  className={`w-full md:w-auto px-8 py-3 rounded-lg font-bold text-white shadow-md transition-all uppercase tracking-wide text-sm ${
                     dirtyCount === 0 || loading || savingAll
                     ? 'bg-gray-300 cursor-not-allowed text-gray-500'
                     : 'bg-blue-600 hover:bg-blue-700 hover:shadow-lg active:transform active:scale-95'
                  }`}
                  onClick={saveAll}
                  disabled={dirtyCount === 0 || loading || savingAll}
                >
                  {savingAll 
                    ? `Đang thực hiện (${saveAllProgress.done}/${saveAllProgress.total})...` 
                    : `LƯU TẤT CẢ (${dirtyCount})`
                  }
                </button>
            </div>
        </div>
      </div>
    </div>
  );
}
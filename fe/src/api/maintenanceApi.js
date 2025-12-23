import axiosClient from './axiosClient';

const maintenanceApi = {
  createRequest: (formData) => {
    return axiosClient.post('/api/maintenance', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  // SỬA LẠI: Dùng API tự lấy thông tin từ Token
  getMyRequests: () => {
    return axiosClient.get('/api/maintenance/my-requests');
  },
  // Giữ lại hàm cũ để Admin dùng nếu cần
  getRequestsByTenant: (tenantId) => {
    return axiosClient.get(`/api/maintenance/tenant/${tenantId}`);
  },

  // Maintenance board (Staff/Maintenance)
  listAllForBoard: () => {
    return axiosClient.get('/api/maintenance/board');
  },
  updateStatus: (id, status) => {
    return axiosClient.patch(`/api/maintenance/${id}/status`, { status });
  },

  createTenantFaultInvoice: (id, payload) => {
    return axiosClient.post(`/api/maintenance/${id}/invoice`, payload);
  },
};

export default maintenanceApi;
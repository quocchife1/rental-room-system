import axiosClient from './axiosClient';

const checkoutRequestsApi = {
  listMyBranch: (params) => axiosClient.get('/api/checkout-requests/my-branch', { params }),
  approve: (id) => axiosClient.put(`/api/checkout-requests/${id}/approve`),

  getOrCreateReport: (requestId) => axiosClient.get(`/api/checkout-requests/${requestId}/inspection-report`),
  saveReport: (requestId, payload) =>
    axiosClient.put(`/api/checkout-requests/${requestId}/inspection-report`, payload),

  uploadItemImages: (requestId, itemKey, files) => {
    const form = new FormData();
    (files || []).forEach((f) => form.append('images', f));
    return axiosClient.post(
      `/api/checkout-requests/${requestId}/inspection-report/items/${encodeURIComponent(itemKey)}/images`,
      form,
      { headers: { 'Content-Type': 'multipart/form-data' } }
    );
  },

  createInvoice: (requestId, dueDate) =>
    axiosClient.post(`/api/checkout-requests/${requestId}/inspection-report/create-invoice`, null, {
      params: dueDate ? { dueDate } : {},
    }),
};

export default checkoutRequestsApi;

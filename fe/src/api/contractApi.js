import axiosClient from './axiosClient';

const contractApi = {
  createContract: (payload) => {
    // FE simplified payload; backend expects ContractCreateRequest
    return axiosClient.post('/api/contracts', payload);
  },
  getMyContracts: () => {
    return axiosClient.get('/api/contracts/my-contracts');
  },
  getMyBranchContracts: (params) => {
    return axiosClient.get('/api/contracts/my-branch', { params });
  },
  getById: (id) => {
    return axiosClient.get(`/api/contracts/${id}`);
  },
  update: (id, payload) => {
    return axiosClient.put(`/api/contracts/${id}`, payload);
  },
  delete: (id) => {
    return axiosClient.delete(`/api/contracts/${id}`);
  },
  downloadContract: (id) => {
    return axiosClient.get(`/api/contracts/${id}/download`, {
      responseType: 'blob',
    });
  },
  uploadSigned: (id, file) => {
    const form = new FormData();
    form.append('file', file);
    return axiosClient.post(`/api/contracts/${id}/upload-signed`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  confirmDepositPayment: (id, payload) => {
    return axiosClient.post(`/api/contracts/${id}/deposit/confirm`, payload);
  },
  initiateDepositMomo: (id, payload) => {
    return axiosClient.post(`/api/contracts/${id}/deposit/momo/initiate`, payload || {});
  },
  requestCheckout: (id, data) => {
    // API Checkout Request: POST /api/contracts/{id}/checkout-request
    // Payload: { requestDate, reason }
    return axiosClient.post(`/api/contracts/${id}/checkout-request`, data);
  }
};

export default contractApi;
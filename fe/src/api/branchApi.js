import axiosClient from './axiosClient';

const branchApi = {
  getAll: () => axiosClient.get('/api/branches'),
  getPublicAll: () => axiosClient.get('/api/public/branches'),
  getById: (id) => axiosClient.get(`/api/branches/${id}`),
  create: (payload) => axiosClient.post('/api/branches', payload),
  update: (id, payload) => axiosClient.put(`/api/branches/${id}`, payload),
  remove: (id) => axiosClient.delete(`/api/branches/${id}`),
};

export default branchApi;

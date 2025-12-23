import axiosClient from './axiosClient';

const staffApi = {
  // --- Moderation: Partner Posts ---
  getManagementPosts: ({ status, q = '', page = 0, size = 12 } = {}) => {
    const params = { q, page, size };
    if (status) params.status = status;
    return axiosClient.get('/api/management/partner-posts', { params });
  },
  getModerationStats: () => {
    return axiosClient.get('/api/management/partner-posts/stats');
  },
  getPendingPosts: ({ page = 0, size = 12 } = {}) => {
    return axiosClient.get('/api/management/partner-posts/pending', { params: { page, size } });
  },
  approvePost: (id) => {
    return axiosClient.post(`/api/management/partner-posts/${id}/approve`);
  },
  rejectPost: (id, reason) => {
    return axiosClient.post(`/api/management/partner-posts/${id}/reject`, null, { params: { reason } });
  },
  approvePostsBatch: (ids = []) => {
    return axiosClient.post('/api/management/partner-posts/approve-batch', { ids });
  },
  rejectPostsBatch: (ids = [], reason = '') => {
    return axiosClient.post('/api/management/partner-posts/reject-batch', { ids, reason });
  },
  getManagementPostById: (id) => {
    return axiosClient.get(`/api/management/partner-posts/${id}`);
  }
};

export default staffApi;

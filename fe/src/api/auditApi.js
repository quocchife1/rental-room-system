import axiosClient from './axiosClient';

const auditApi = {
  // New unified endpoint on BE
  search: (params) => axiosClient.get('/api/audit-logs/search', { params }),
};

export default auditApi;

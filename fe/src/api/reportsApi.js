import axiosClient from './axiosClient';

const reportsApi = {
  summary: (params) => axiosClient.get('/api/reports/summary', { params }),
};

export default reportsApi;

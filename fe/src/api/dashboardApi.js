import axiosClient from './axiosClient';

const dashboardApi = {
  getDirectorDashboard: () => axiosClient.get('/api/dashboard/director'),
};

export default dashboardApi;

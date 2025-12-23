import axiosClient from './axiosClient';

const publicPartnerApi = {
  list: ({ page = 0, size = 8, sort = 'createdAt,desc' } = {}) => {
    return axiosClient.get('/api/public/partner-posts', { params: { page, size, sort } });
  },
  getById: (id) => {
    return axiosClient.get(`/api/public/partner-posts/${id}`);
  }
};

export default publicPartnerApi;

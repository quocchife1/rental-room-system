import axiosClient from './axiosClient';

const systemConfigApi = {
  get: () => axiosClient.get('/api/system-config'),
  upsert: (body) => axiosClient.put('/api/system-config', body),
};

export default systemConfigApi;

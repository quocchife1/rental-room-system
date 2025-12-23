import axiosClient from './axiosClient';

const reservationApi = {
  createReservation: (data) => {
    return axiosClient.post('/api/reservations', data);
  },
  getReservationsByStatus: (status, params) => {
    return axiosClient.get(`/api/reservations/status/${status}`, { params });
  },
  getMyBranchReservations: (params) => {
    return axiosClient.get('/api/reservations/my-branch', { params });
  },
  searchReservations: (q, params) => {
    return axiosClient.get('/api/reservations/search', { params: { ...params, q } });
  },
  getMyReservations: (params) => {
    return axiosClient.get('/api/reservations/my-reservations', { params });
  },
  cancelReservation: (id) => {
    return axiosClient.delete(`/api/reservations/${id}`);
  },
  confirmReservation: (id) => {
    return axiosClient.put(`/api/reservations/${id}/confirm`);
  },
  markCompleted: (id) => {
    return axiosClient.put(`/api/reservations/${id}/mark-completed`);
  },
  markNoShow: (id) => {
    return axiosClient.put(`/api/reservations/${id}/mark-no-show`);
  },
  getContractPrefill: (id) => {
    return axiosClient.get(`/api/reservations/${id}/contract-prefill`);
  },
  markContracted: (id) => {
    return axiosClient.put(`/api/reservations/${id}/mark-contracted`);
  },
  convertToContract: (id) => {
    return axiosClient.post(`/api/reservations/${id}/convert-to-contract`);
  }
};

export default reservationApi;
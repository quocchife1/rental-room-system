import axiosClient from './axiosClient';

const serviceBookingApi = {
  listByContract: (contractId) => axiosClient.get(`/api/contracts/${contractId}/bookings`),
  bookCleaning: (contractId, payload) => axiosClient.post(`/api/contracts/${contractId}/bookings/cleaning`, payload || {}),
  markComplete: (bookingId) => axiosClient.post(`/api/bookings/${bookingId}/complete`),
};

export default serviceBookingApi;

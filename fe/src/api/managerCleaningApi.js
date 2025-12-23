import axiosClient from './axiosClient';

const managerCleaningApi = {
  getMyBranchCleaningBookings() {
    return axiosClient.get('/api/bookings/cleaning/my-branch');
  },

  cancelBooking(bookingId, reason) {
    return axiosClient.post(`/api/bookings/${bookingId}/cancel`, { reason });
  },
};

export default managerCleaningApi;

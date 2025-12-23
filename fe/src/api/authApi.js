import axiosClient from './axiosClient';

const authApi = {
  login: (data) => {
    return axiosClient.post('/api/auth/login', data);
  },

  logout: () => {
    return axiosClient.post('/api/auth/logout');
  },
  
  // Dùng cho tab "Sinh viên" (Guest)
  registerGuest: (data) => {
    return axiosClient.post('/api/auth/register/guest', data);
  },

  // Dùng cho tab "Chủ trọ" (Partner)
  registerPartner: (data) => {
    return axiosClient.post('/api/auth/register/partner', data);
  },

  // Dùng cho Admin tạo nhân viên (nếu cần sau này)
  registerEmployee: (data) => {
    return axiosClient.post('/api/auth/register/employee', data);
  }
};

export default authApi;
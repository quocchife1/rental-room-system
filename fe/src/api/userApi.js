import axiosClient from './axiosClient';

const userApi = {
  // --- Tenant ---
  getTenantProfile: (id) => {
    return axiosClient.get(`/api/management/tenants/${id}`);
  },
  updateTenantProfile: (id, data) => {
    return axiosClient.patch(`/api/management/tenants/${id}`, data);
  },
  
  // --- Partner ---
  getPartnerProfile: (id) => {
    return axiosClient.get(`/api/management/partners/${id}`);
  },
  updatePartnerProfile: (id, data) => {
    return axiosClient.patch(`/api/management/partners/${id}`, data);
  },

  // --- Employee / Admin ---
  getEmployeeProfile: (id) => {
    return axiosClient.get(`/api/management/employees/${id}`);
  },
  updateEmployeeStatus: (id, status) => {
    return axiosClient.patch(`/api/management/employees/${id}/status`, null, { params: { status } });
  }
};

export default userApi;
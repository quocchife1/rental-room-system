import axiosClient from './axiosClient';

const adminUsersApi = {
  listEmployees: () => axiosClient.get('/api/management/employees'),
  listTenants: () => axiosClient.get('/api/management/tenants'),
  listPartners: () => axiosClient.get('/api/management/partners'),

  // Tenant enable/disable (BE: status=ACTIVE/BANNED)
  setTenantStatus: (id, status) =>
    axiosClient.patch(`/api/management/tenants/${id}/status`, null, { params: { status } }),
};

export default adminUsersApi;

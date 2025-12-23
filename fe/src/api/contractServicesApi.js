import axiosClient from './axiosClient';

const contractServicesApi = {
  list: (contractId) => axiosClient.get(`/api/contracts/${contractId}/services`),
  add: (contractId, payload) => axiosClient.post(`/api/contracts/${contractId}/services`, payload),
  cancel: (contractId, contractServiceId) =>
    axiosClient.post(`/api/contracts/${contractId}/services/${contractServiceId}/cancel`),
  updateMeterReading: (contractId, contractServiceId, payload) =>
    axiosClient.put(`/api/contracts/${contractId}/services/${contractServiceId}/meter-reading`, payload),
};

export default contractServicesApi;

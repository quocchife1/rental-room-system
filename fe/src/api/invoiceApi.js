import axiosClient from './axiosClient';

const invoiceApi = {
  getById: (id) => {
    return axiosClient.get(`/api/invoices/${id}`);
  },
  getMyInvoices: () => {
    return axiosClient.get('/api/invoices/my-invoices');
  },
  payInvoice: (id, isDirect = false) => {
    return axiosClient.post(`/api/invoices/${id}/pay`, null, {
      params: { direct: isDirect }
    });
  },

  // Staff/Finance
  listPaged: (params) => {
    return axiosClient.get('/api/invoices/paged', { params });
  },
  monthlyPreviews: (year, month) => {
    return axiosClient.get('/api/invoices/monthly-previews', { params: { year, month } });
  },
  payInvoiceAsStaff: (id, isDirect = true) => {
    return axiosClient.post(`/api/invoices/${id}/pay`, null, {
      params: { direct: isDirect }
    });
  },

  generateMonthlyForContract: (contractId, payload) => {
    return axiosClient.post(`/api/invoices/generate-monthly/contracts/${contractId}`, payload);
  },

  generateMonthly: (payload) => {
    return axiosClient.post('/api/invoices/generate-monthly', payload);
  },
};

export default invoiceApi;
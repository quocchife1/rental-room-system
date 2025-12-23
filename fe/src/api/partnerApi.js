import axiosClient from './axiosClient';

const partnerApi = {
  // --- Tin đăng (Partner Posts) ---
  getMyPosts: () => {
    return axiosClient.get('/api/partner-posts/my-posts');
  },

  getMyPostsPaged: ({ page = 0, size = 10, sort = 'createdAt,desc' } = {}) => {
    return axiosClient.get('/api/partner-posts/my-posts/paged', {
      params: { page, size, sort }
    });
  },

  createPost: (data, images) => {
    // data: { title, description, price, area, address, postType }
    // images: FileList or array of File objects (max 5)
    const formData = new FormData();
    formData.append('data', JSON.stringify(data));

    if (images && images.length > 0) {
      for (let i = 0; i < Math.min(images.length, 5); i++) {
        formData.append('images', images[i]);
      }
    }

    return axiosClient.post('/api/partner-posts', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  updatePost: (id, data, images) => {
    const formData = new FormData();
    formData.append('data', JSON.stringify(data));

    if (images && images.length > 0) {
      for (let i = 0; i < Math.min(images.length, 5); i++) {
        formData.append('images', images[i]);
      }
    }

    return axiosClient.put(`/api/partner-posts/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  deletePost: (id) => {
    return axiosClient.delete(`/api/partner-posts/${id}`);
  },

  getMonthlyViews: () => {
    return axiosClient.get('/api/partner-posts/my-posts/stats/monthly-views');
  },

  getPostById: (id) => {
    return axiosClient.get(`/api/partner-posts/${id}`);
  },

  // --- Thanh toán (Partner Payments) ---
  createPayment: (data) => {
    // data: { postId, amount, method }
    return axiosClient.post('/api/partner-payment', data);
  },

  getMyPayments: () => {
    return axiosClient.get('/api/partner-payment/my-payments');
  },

  // --- Dashboard Stats ---
  getDashboardStats: () => {
    // Call getMyPosts and calculate stats on FE
    return axiosClient.get('/api/partner-posts/my-posts');
  },

  // --- Service Packages (Future expansion) ---
  getServicePackages: () => {
    return axiosClient.get('/api/service-packages');
  },

  // --- Simulate purchase for testing ---
  simulatePurchase: (postId, packageId) => {
    return axiosClient.post('/api/partner-payment/simulate-purchase', null, {
      params: { postId, packageId }
    });
  }

};

export default partnerApi;
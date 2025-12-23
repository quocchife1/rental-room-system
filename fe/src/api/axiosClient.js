import axios from 'axios';

const axiosClient = axios.create({
  baseURL: '/', // Vite proxy sẽ chuyển tiếp sang Backend
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach Authorization header from localStorage (if token exists)
axiosClient.interceptors.request.use(
  (config) => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers = config.headers || {};
        config.headers['Authorization'] = `Bearer ${token}`;
      }
    } catch (e) {
      // ignore
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Xử lý Response
axiosClient.interceptors.response.use(
  (response) => {
    // If response is a binary/blob (download), return full response so caller can access response.data (blob)
    try {
      const respType = response?.config?.responseType;
      if (respType === 'blob' || respType === 'arraybuffer') {
        return response; // caller will handle response.data
      }
    } catch (e) {
      // ignore
    }

    // Backend trả về: { statusCode: 200, message: "...", data: [...] }
    if (response && response.data) {
      // If backend wraps payload in ApiResponseDto, return the inner data
      if (response.data && Object.prototype.hasOwnProperty.call(response.data, 'data')) {
        return response.data.data;
      }
      return response.data;
    }
    return response;
  },
  (error) => {
    // Normalize axios error to include backend message when available
    console.error("API Error:", error);
    if (error.response && error.response.data) {
      // Backend sometimes returns ApiResponseDto with message
      const data = error.response.data;

      // Prefer detailed error when message is too generic
      const msg = data.message || null;
      const detail = data.error || null;
      const nested = (data.data && data.data.message) || null;
      const normalizedMessage = (detail && (!msg || msg === 'Lỗi hệ thống'))
        ? detail
        : (msg || detail || nested);

      if (normalizedMessage) {
        error.message = normalizedMessage;
        error.response.data = { ...data, message: normalizedMessage };
      }
    }
    return Promise.reject(error);
  }
);

export default axiosClient;

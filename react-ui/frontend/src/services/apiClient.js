import axios from 'axios';

/**
 * Axios client instance with JWT token interceptor
 */
const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor to add JWT token to all requests
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('adminToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor to handle authentication errors
 */
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Token expired or invalid - redirect to login
      localStorage.removeItem('adminToken');
      localStorage.removeItem('adminUser');
      if (window.location.pathname !== '/admin/login') {
        window.location.href = '/admin/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;

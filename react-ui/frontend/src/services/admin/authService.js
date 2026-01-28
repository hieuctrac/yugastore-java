import apiClient from '../apiClient';

/**
 * Authentication service for admin portal
 */
const authService = {
  /**
   * Login with username and password
   */
  login: async (username, password) => {
    const response = await apiClient.post('/admin/auth/login', {
      username,
      password,
    });

    const { token, user } = response.data;

    // Store token and user info
    localStorage.setItem('adminToken', token);
    localStorage.setItem('adminUser', JSON.stringify(user));

    // Set up session timeout check
    authService.setupSessionTimeout();

    return response.data;
  },

  /**
   * Logout current user
   */
  logout: async () => {
    try {
      await apiClient.post('/admin/auth/logout');
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear local storage
      localStorage.removeItem('adminToken');
      localStorage.removeItem('adminUser');
      localStorage.removeItem('tokenExpiry');

      // Clear session timeout
      if (authService.timeoutId) {
        clearInterval(authService.timeoutId);
      }
    }
  },

  /**
   * Get current authenticated user
   */
  getCurrentUser: () => {
    const userStr = localStorage.getItem('adminUser');
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: () => {
    const token = localStorage.getItem('adminToken');
    const user = authService.getCurrentUser();
    return !!(token && user);
  },

  /**
   * Get JWT token
   */
  getToken: () => {
    return localStorage.getItem('adminToken');
  },

  /**
   * Get current user from API
   */
  getMe: async () => {
    try {
      const response = await apiClient.get('/admin/auth/me');
      const user = response.data;
      localStorage.setItem('adminUser', JSON.stringify(user));
      return user;
    } catch (error) {
      console.error('Get current user error:', error);
      throw error;
    }
  },

  /**
   * Request password reset
   */
  requestPasswordReset: async (email) => {
    const response = await apiClient.post('/admin/auth/password-reset', {
      email,
    });
    return response.data;
  },

  /**
   * Setup session timeout detection
   * Checks token expiration every minute
   */
  setupSessionTimeout: () => {
    // Clear any existing timeout
    if (authService.timeoutId) {
      clearInterval(authService.timeoutId);
    }

    // Store token expiry time (30 minutes from now)
    const expiryTime = Date.now() + (30 * 60 * 1000);
    localStorage.setItem('tokenExpiry', expiryTime.toString());

    // Check expiration every minute
    authService.timeoutId = setInterval(() => {
      const expiry = localStorage.getItem('tokenExpiry');
      if (expiry && Date.now() >= parseInt(expiry, 10)) {
        alert('Your session has expired. Please log in again.');
        authService.logout();
        window.location.href = '/admin/login';
      }
    }, 60000); // Check every minute
  },
};

// Setup session timeout on page load if user is already logged in
if (authService.isAuthenticated()) {
  authService.setupSessionTimeout();
}

export default authService;

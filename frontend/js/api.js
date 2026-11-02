/**
 * Hostel Management System (HMS) - Centralized REST API Client
 */

const API_BASE_URL = "http://localhost:8080/api";

class ApiClient {
  constructor() {
    this.token = localStorage.getItem('hms_jwt_token') || null;
  }

  setToken(token) {
    this.token = token;
    if (token) {
      localStorage.setItem('hms_jwt_token', token);
    } else {
      localStorage.removeItem('hms_jwt_token');
    }
  }

  getToken() {
    return this.token || localStorage.getItem('hms_jwt_token');
  }

  getHeaders(customHeaders = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...customHeaders
    };
    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async request(endpoint, options = {}) {
    const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
    
    const config = {
      ...options,
      headers: this.getHeaders(options.headers || {})
    };

    try {
      const response = await fetch(url, config);

      if (response.status === 204) {
        return { success: true, status: 204, data: null };
      }

      let data;
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        if (response.status === 401 && !url.includes('/auth/login')) {
          this.setToken(null);
          localStorage.removeItem('hms_current_user');
          localStorage.setItem('hms_active_role', 'public');
          window.dispatchEvent(new CustomEvent('hms:unauthorized'));
        }
        const errorMsg = (data && data.message) ? data.message : (typeof data === 'string' ? data : `HTTP ${response.status} Error`);
        return {
          success: false,
          status: response.status,
          message: errorMsg,
          data
        };
      }

      return {
        success: true,
        status: response.status,
        data
      };
    } catch (err) {
      console.warn(`API network call paused on ${url}:`, err.message);
      return {
        success: false,
        status: 0,
        message: err.message || 'Network connection failed. Ensure backend Spring Boot server is running on http://localhost:8080.'
      };
    }
  }

  async get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }

  async post(endpoint, body) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(body)
    });
  }

  async put(endpoint, body) {
    return this.request(endpoint, {
      method: 'PUT',
      body: body ? JSON.stringify(body) : undefined
    });
  }

  async delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }

  // Gate & QR API Methods
  async getCommonQRCode() {
    return this.get('/hostel/qr');
  }

  async scanGateQR(qrToken) {
    return this.post('/gate/scan', { qrToken });
  }

  async getMyGateLogs() {
    return this.get('/gate/my-logs');
  }

  async getWardenGateLogs(student, room, date, type) {
    const params = new URLSearchParams();
    if (student) params.append('student', student);
    if (room) params.append('room', room);
    if (date) params.append('date', date);
    if (type && type !== 'all') params.append('type', type);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return this.get(`/gate/warden/logs${queryString}`);
  }

  async getAdminGateLogs(student, room, date, type) {
    const params = new URLSearchParams();
    if (student) params.append('student', student);
    if (room) params.append('room', room);
    if (date) params.append('date', date);
    if (type && type !== 'all') params.append('type', type);
    const queryString = params.toString() ? `?${params.toString()}` : '';
    return this.get(`/gate/admin/logs${queryString}`);
  }
}

const api = new ApiClient();

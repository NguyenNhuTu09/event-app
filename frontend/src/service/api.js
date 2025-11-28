// API configuration
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://ems-backend-jkjx.onrender.com/api';

// Helper function to make API calls
export const apiCall = async (endpoint, options = {}) => {
    const url = `${API_BASE_URL}${endpoint}`;

    // Build config object
    const config = {
        method: options.method || 'GET',
        mode: 'cors', // Enable CORS
        credentials: 'omit', // Don't send cookies
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers,
        },
    };

    // Add body if present
    if (options.body) {
        config.body = options.body;
    }

    // Add token if available (check both token and adminToken)
    const token = localStorage.getItem('token') || localStorage.getItem('adminToken');
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, config);

        // Check if response is ok before parsing
        const contentType = response.headers.get('content-type') || '';
        let data;

        // Try to parse JSON, but handle non-JSON responses
        if (contentType.includes('application/json')) {
            try {
                data = await response.json();
            } catch (e) {
                const text = await response.text();
                data = text ? { message: text } : { message: 'Unknown error' };
            }
        } else {
            // Non-JSON response (text/plain, etc.)
            const text = await response.text();
            data = { message: text || 'Unknown error' };
        }

        if (!response.ok) {
            const errorMessage = data.message ||
                (typeof data === 'string' ? data : JSON.stringify(data)) ||
                response.statusText ||
                'Something went wrong';
            throw new Error(errorMessage);
        }

        return data;
    } catch (error) {
        console.error('API Error:', error);

        // Handle network errors
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error('Không thể kết nối đến server. Vui lòng kiểm tra kết nối internet hoặc thử lại sau.');
        }

        // If it's already an Error object, throw it as is
        if (error instanceof Error) {
            throw error;
        }

        // Otherwise, wrap it in an Error
        throw new Error(error.message || 'Network error occurred');
    }
};

// Auth API
export const authAPI = {
    // Đăng ký
    register: async (username, email, password, confirmPassword) => {
        return apiCall('/auth/signup', {
            method: 'POST',
            body: JSON.stringify({ username, email, password, confirmPassword }),
        });
    },

    // Đăng nhập
    login: async (email, password) => {
        const response = await apiCall('/auth/signin', {
            method: 'POST',
            body: JSON.stringify({ email, password }),
        });
        return response;
    },

    // Lấy thông tin user từ token
    getCurrentUser: async () => {
        return apiCall('/auth/me', {
            method: 'GET',
        });
    },

    // Xác thực token
    verifyToken: async () => {
        return apiCall('/auth/verify', {
            method: 'GET',
        });
    },

    // Đăng xuất
    logout: async () => {
        return apiCall('/auth/logout', {
            method: 'POST',
        });
    },

    // Đổi oneTimeCode lấy JWT token (cho Google OAuth2)
    exchangeToken: async (oneTimeCode) => {
        return apiCall('/auth/token/exchange', {
            method: 'POST',
            body: JSON.stringify({ refreshToken: oneTimeCode }),
        });
    },
};

// ===== EVENTS API =====
export const eventsAPI = {
    // Client APIs (Public)
    getPublicEvents: async () => {
        return apiCall('/events/public', { method: 'GET' });
    },

    getEventById: async (eventId) => {
        return apiCall(`/events/${eventId}`, { method: 'GET' });
    },

    // Partner/Organizer APIs (Requires ORGANIZER role)
    createEvent: async (eventData) => {
        return apiCall('/events', {
            method: 'POST',
            body: JSON.stringify(eventData),
        });
    },

    getMyEvents: async () => {
        return apiCall('/events/my-events', { method: 'GET' });
    },

    updateEvent: async (eventId, eventData) => {
        return apiCall(`/events/${eventId}`, {
            method: 'PUT',
            body: JSON.stringify(eventData),
        });
    },

    deleteEvent: async (eventId) => {
        return apiCall(`/events/${eventId}`, { method: 'DELETE' });
    },

    // Super Admin APIs (Requires SADMIN role)
    getAllEvents: async () => {
        return apiCall('/events/all', { method: 'GET' });
    },

    approveEvent: async (eventId) => {
        return apiCall(`/events/${eventId}/approve`, { method: 'PUT' });
    },

    rejectEvent: async (eventId, reason = '') => {
        const url = reason ? `/events/${eventId}/reject?reason=${encodeURIComponent(reason)}` : `/events/${eventId}/reject`;
        return apiCall(url, { method: 'PUT' });
    },
};


export default authAPI;


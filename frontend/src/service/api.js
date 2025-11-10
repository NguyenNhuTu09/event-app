// API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000/api';

// Helper function to make API calls
const apiCall = async (endpoint, options = {}) => {
    const url = `${API_BASE_URL}${endpoint}`;
    const config = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers,
        },
        ...options,
    };

    // Add token if available
    const token = localStorage.getItem('token');
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }

    try {
        const response = await fetch(url, config);

        // Try to parse JSON, but handle non-JSON responses
        let data;
        const contentType = response.headers.get('content-type');
        const text = await response.text();

        if (contentType && contentType.includes('application/json')) {
            try {
                data = text ? JSON.parse(text) : {};
            } catch (e) {
                // If JSON parsing fails, use text as message
                data = { message: text || 'Unknown error' };
            }
        } else {
            // Non-JSON response, use text as message
            data = text ? { message: text } : {};
        }

        if (!response.ok) {
            const errorMessage = data.message || (typeof data === 'string' ? data : JSON.stringify(data)) || response.statusText || 'Something went wrong';
            throw new Error(errorMessage);
        }

        return data;
    } catch (error) {
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
    register: async (username, email, password) => {
        return apiCall('/auth/signup', {
            method: 'POST',
            body: JSON.stringify({ username, email, password }),
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
};

export default authAPI;


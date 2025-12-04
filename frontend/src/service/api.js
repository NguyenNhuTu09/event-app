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
        redirect: 'follow', // Follow redirects
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers,
        },
    };

    // Remove Content-Type for GET requests (browser will set it automatically)
    if ((options.method || 'GET') === 'GET' && !options.body) {
        delete config.headers['Content-Type'];
    }

    // Add body if present
    if (options.body) {
        config.body = options.body;
    }

    // Add token if available (check both token and adminToken)
    const token = localStorage.getItem('token') || localStorage.getItem('adminToken');

    // Check if token is expired before making request
    if (token) {
        try {
            // Decode JWT token to check expiration (without verification)
            const payload = JSON.parse(atob(token.split('.')[1]));
            const exp = payload.exp * 1000; // Convert to milliseconds
            const now = Date.now();

            if (exp < now) {
                // Token is expired
                console.warn('Token is expired. Clearing tokens and redirecting to login.');
                localStorage.removeItem('adminToken');
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('adminUser');
                localStorage.removeItem('user');

                const currentPath = window.location.pathname;
                if (currentPath.includes('/admin') || currentPath.includes('/super-admin') || currentPath.includes('/partner')) {
                    window.location.href = '/admin/login';
                }

                throw new Error('Token đã hết hạn. Vui lòng đăng nhập lại.');
            }
        } catch (decodeError) {
            // If token decode fails, it might be invalid format
            if (decodeError.message.includes('Token đã hết hạn')) {
                throw decodeError;
            }
            // Otherwise, continue with request (token might be in different format)
            console.warn('Could not decode token, proceeding with request:', decodeError);
        }

        config.headers['Authorization'] = `Bearer ${token}`;
    } else {
        console.warn('No token found in localStorage for API call to:', url);
    }

    try {
        // Log request details for debugging (only in development)
        if (import.meta.env.DEV) {
            console.log('API Request:', {
                url,
                method: config.method,
                hasToken: !!token,
                tokenPreview: token ? `${token.substring(0, 20)}...` : 'None'
            });
        }

        // Add timeout for fetch (60 seconds for Render.com free tier)
        const controller = new AbortController();
        const timeoutId = setTimeout(() => {
            controller.abort();
        }, 60000); // Increased to 60 seconds for Render.com wake up time

        let response;
        try {
            response = await fetch(url, {
                ...config,
                signal: controller.signal,
                // Add cache control
                cache: 'no-cache',
            });
        } catch (fetchError) {
            // Clear timeout on error
            clearTimeout(timeoutId);

            // If it's an abort error, throw it
            if (fetchError.name === 'AbortError') {
                throw fetchError;
            }

            // For other fetch errors, log more details
            console.error('Fetch error details:', {
                name: fetchError.name,
                message: fetchError.message,
                stack: fetchError.stack,
                url: url
            });

            throw fetchError;
        } finally {
            // Always clear timeout to prevent memory leaks
            clearTimeout(timeoutId);
        }

        // Check response status before parsing
        // Handle redirects (302, 301, 307, 308) - usually means token expired or unauthorized
        if (response.status === 302 || response.status === 301 || response.status === 307 || response.status === 308) {
            const redirectUrl = response.headers.get('Location') || response.url || '';
            console.error('Redirect detected (token likely expired):', {
                status: response.status,
                redirectUrl,
                originalUrl: url
            });

            // Clear tokens if redirected
            localStorage.removeItem('adminToken');
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('adminUser');
            localStorage.removeItem('user');

            // Redirect to login
            const currentPath = window.location.pathname;
            if (currentPath.includes('/admin') || currentPath.includes('/super-admin') || currentPath.includes('/partner')) {
                setTimeout(() => {
                    window.location.href = '/admin/login';
                }, 500);
            }

            throw new Error('Phiên đăng nhập đã hết hạn. Đang chuyển đến trang đăng nhập...');
        }

        // Check if response was redirected (after following redirects)
        if (response.redirected) {
            console.warn('API request was redirected:', {
                originalUrl: url,
                redirectedTo: response.url,
                status: response.status,
                statusText: response.statusText
            });
            // If redirected to login/auth page, token is invalid
            if (response.url.includes('/login') || response.url.includes('/auth') || response.url.includes('/oauth2')) {
                // Clear tokens if redirected to login
                localStorage.removeItem('adminToken');
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('adminUser');
                localStorage.removeItem('user');

                const currentPath = window.location.pathname;
                if (currentPath.includes('/admin') || currentPath.includes('/super-admin') || currentPath.includes('/partner')) {
                    setTimeout(() => {
                        window.location.href = '/admin/login';
                    }, 500);
                }

                throw new Error('Phiên đăng nhập đã hết hạn hoặc token không hợp lệ. Vui lòng đăng nhập lại.');
            }
        }

        // Đọc response body một lần duy nhất dưới dạng text
        const responseText = await response.text();
        let data;

        // Parse JSON từ text (nếu có thể)
        if (responseText) {
            try {
                data = JSON.parse(responseText);
            } catch (parseError) {
                // Nếu không parse được JSON, dùng text như message
                data = { message: responseText };
            }
        } else {
            data = { message: 'Empty response' };
        }

        if (!response.ok) {
            // Handle 401 Unauthorized (Token expired or invalid)
            if (response.status === 401) {
                // Clear invalid tokens
                localStorage.removeItem('adminToken');
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('adminUser');
                localStorage.removeItem('user');

                // Check if we're in admin panel and redirect to admin login
                const currentPath = window.location.pathname;
                if (currentPath.includes('/admin') || currentPath.includes('/super-admin') || currentPath.includes('/partner')) {
                    // Redirect to admin login after a short delay
                    setTimeout(() => {
                        window.location.href = '/admin/login';
                    }, 1000);
                }

                throw new Error('Phiên đăng nhập đã hết hạn. Đang chuyển đến trang đăng nhập...');
            }

            // Handle 403 Forbidden
            if (response.status === 403) {
                throw new Error('Bạn không có quyền truy cập tài nguyên này.');
            }

            const errorMessage = data.message ||
                (typeof data === 'string' ? data : JSON.stringify(data)) ||
                response.statusText ||
                `HTTP ${response.status}: ${response.statusText}`;
            throw new Error(errorMessage);
        }

        return data;
    } catch (error) {
        console.error('API Error:', {
            url,
            method: config.method,
            error: error.message,
            errorName: error.name
        });

        // Handle abort (timeout)
        if (error.name === 'AbortError') {
            throw new Error('Yêu cầu quá thời gian chờ (timeout). Backend server có thể đang sleep (Render.com free tier). Vui lòng thử lại sau vài giây.');
        }

        // Handle network errors (Failed to fetch, CORS, etc.)
        if (error.name === 'TypeError' && (
            error.message.includes('fetch') ||
            error.message.includes('Failed to fetch') ||
            error.message.includes('NetworkError') ||
            error.message.includes('Network request failed')
        )) {
            // Check if it's a CORS error
            if (error.message.includes('CORS') || error.message.includes('cors')) {
                throw new Error('Lỗi CORS: Backend server chưa cấu hình CORS cho domain này.');
            }

            // Check if backend might be sleeping (Render.com free tier)
            const errorMsg = `Không thể kết nối đến server (${url}). ` +
                `Có thể backend server đang sleep (Render.com free tier cần vài giây để wake up). ` +
                `Vui lòng: ` +
                `1) Đợi 10-15 giây và thử lại, ` +
                `2) Kiểm tra backend server đang chạy, ` +
                `3) Kiểm tra kết nối internet.`;
            throw new Error(errorMsg);
        }

        // Handle CORS errors
        if (error.message.includes('CORS') || error.message.includes('cors')) {
            throw new Error('Lỗi CORS: Backend server chưa cấu hình CORS cho domain này.');
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


// Test function to check backend connectivity (for debugging)
export const testBackendConnection = async () => {
    const testUrl = `${API_BASE_URL.replace('/api', '')}/actuator/health`;
    try {
        const response = await fetch(testUrl, {
            method: 'GET',
            mode: 'cors',
            cache: 'no-cache'
        });
        console.log('Backend health check:', {
            status: response.status,
            ok: response.ok,
            url: testUrl
        });
        return { success: response.ok, status: response.status };
    } catch (error) {
        console.error('Backend health check failed:', error);
        return { success: false, error: error.message };
    }
};

export default authAPI;


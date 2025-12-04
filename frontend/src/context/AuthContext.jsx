import React, { createContext, useState, useContext, useEffect } from 'react';
import { authAPI } from '../service/api';

const AuthContext = createContext({
    user: null,
    token: null,
    isAuthenticated: false,
    login: async () => ({ success: false }),
    register: async () => ({ success: false }),
    loginWithGoogleToken: async (token) => ({ success: false }),
    logout: async () => {},
    loading: false,
});

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [loading, setLoading] = useState(true);

    // Load user from localStorage on mount
    useEffect(() => {
        const storedToken = localStorage.getItem('token');
        const storedUser = localStorage.getItem('user');

        if (storedToken && storedUser) {
            try {
                setToken(storedToken);
                setUser(JSON.parse(storedUser));
            } catch (error) {
                console.error('Error parsing stored user:', error);
                localStorage.removeItem('token');
                localStorage.removeItem('user');
            }
        }
        setLoading(false);
    }, []);

    const login = async (email, password) => {
        try {
            const response = await authAPI.login(email, password);
            
            // Backend trả về: { accessToken, refreshToken, user: { id, username, email, ... } }
            const accessToken = response.accessToken || response.token;
            const userInfo = response.user || response;
            
            // Save token and user info
            const userData = {
                id: userInfo.id,
                username: userInfo.username,
                email: userInfo.email,
                role: userInfo.role,
                address: userInfo.address,
                gender: userInfo.gender,
                dateOfBirth: userInfo.dateOfBirth,
                phoneNumber: userInfo.phoneNumber,
                avatarUrl: userInfo.avatarUrl,
            };

            // Lưu accessToken và refreshToken
            localStorage.setItem('token', accessToken);
            if (response.refreshToken) {
                localStorage.setItem('refreshToken', response.refreshToken);
            }
            localStorage.setItem('user', JSON.stringify(userData));

            setToken(accessToken);
            setUser(userData);

            return { success: true };
        } catch (error) {
            console.error('Login error:', error);
            return { 
                success: false, 
                message: error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.' 
            };
        }
    };

    const register = async (username, email, password, confirmPassword) => {
        try {
            await authAPI.register(username, email, password, confirmPassword);
            return { success: true };
        } catch (error) {
            console.error('Register error:', error);
            return { 
                success: false, 
                message: error.message || 'Đăng ký thất bại. Vui lòng thử lại.' 
            };
        }
    };

    const loginWithGoogleToken = async (jwtToken) => {
        try {
            // Lưu token vào localStorage
            localStorage.setItem('token', jwtToken);
            setToken(jwtToken);

            // Lấy thông tin user từ backend (nếu có endpoint)
            try {
                const userData = await authAPI.getCurrentUser();
                
                // Lưu thông tin user đầy đủ
                const userInfo = {
                    id: userData.id,
                    username: userData.username,
                    email: userData.email,
                    avatarUrl: userData.avatarUrl,
                    role: userData.role,
                };

                localStorage.setItem('user', JSON.stringify(userInfo));
                setUser(userInfo);
            } catch (error) {
                // Nếu không lấy được user info, vẫn giữ token
                // User info có thể đã được lưu từ OAuth2RedirectPage
                const storedUser = localStorage.getItem('user');
                if (storedUser) {
                    try {
                        setUser(JSON.parse(storedUser));
                    } catch (e) {
                        console.warn('Could not parse stored user:', e);
                    }
                }
            }

            return { success: true };
        } catch (error) {
            // Xóa token nếu lỗi
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setToken(null);
            setUser(null);
            
            return { 
                success: false, 
                message: error.message || 'Không thể xử lý đăng nhập Google' 
            };
        }
    };

    const logout = async () => {
        try {
            // Gọi API logout (nếu có token)
            const token = localStorage.getItem('token');
            if (token) {
                try {
                    await authAPI.logout();
                } catch (error) {
                    // Nếu API logout thất bại, vẫn tiếp tục xóa token ở client
                    console.warn('Logout API call failed:', error);
                }
            }
        } catch (error) {
            console.error('Error during logout:', error);
        } finally {
            // Luôn xóa token và user ở client
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setToken(null);
            setUser(null);
        }
    };

    const value = {
        user,
        token,
        isAuthenticated: !!token,
        login,
        register,
        loginWithGoogleToken,
        logout,
        loading,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};

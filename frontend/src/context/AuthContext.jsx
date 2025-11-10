import React, { createContext, useState, useContext, useEffect } from 'react';
import authAPI from '../service/api';

const AuthContext = createContext({
    user: null,
    token: null,
    isAuthenticated: false,
    login: async () => {},
    register: async () => {},
    logout: () => {},
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
            
            // Save token and user info
            const userData = {
                id: response.id,
                username: response.username,
                email: response.email,
            };

            localStorage.setItem('token', response.token);
            localStorage.setItem('user', JSON.stringify(userData));

            setToken(response.token);
            setUser(userData);

            return { success: true };
        } catch (error) {
            return { 
                success: false, 
                message: error.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu.' 
            };
        }
    };

    const register = async (username, email, password) => {
        try {
            await authAPI.register(username, email, password);
            return { success: true };
        } catch (error) {
            return { 
                success: false, 
                message: error.message || 'Đăng ký thất bại. Vui lòng thử lại.' 
            };
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    };

    const value = {
        user,
        token,
        isAuthenticated: !!token,
        login,
        register,
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






import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { path } from '../../../utils/constant';
import './AdminLogin.css';

const AdminLogin = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        username: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    // Hardcode credentials
    const ADMIN_CREDENTIALS = {
        username: 'admin',
        password: 'admin123',
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        // Simulate API call delay
        setTimeout(() => {
            if (
                formData.username === ADMIN_CREDENTIALS.username &&
                formData.password === ADMIN_CREDENTIALS.password
            ) {
                // Store admin session (hardcode)
                localStorage.setItem('adminToken', 'admin_token_hardcoded');
                localStorage.setItem('adminUser', JSON.stringify({
                    username: formData.username,
                    role: 'admin',
                }));

                // Redirect to dashboard
                navigate(path.ADMIN_DASHBOARD);
            } else {
                setError('Tên đăng nhập hoặc mật khẩu không chính xác!');
            }
            setLoading(false);
        }, 500);
    };

    return (
        <div className="admin-login-container">
            <div className="admin-login-box">
                <div className="login-header">
                    <div className="login-logo">
                        <i className="bi bi-fire"></i>
                    </div>
                    <h1>EMS Admin Panel</h1>
                    <p>Đăng nhập để quản lý hệ thống</p>
                </div>

                <form onSubmit={handleSubmit} className="login-form">
                    {error && (
                        <div className="error-message">
                            <i className="bi bi-exclamation-circle"></i>
                            {error}
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="username">
                            <i className="bi bi-person"></i>
                            Tên đăng nhập
                        </label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleInputChange}
                            placeholder="Nhập tên đăng nhập"
                            required
                            disabled={loading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">
                            <i className="bi bi-lock"></i>
                            Mật khẩu
                        </label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            placeholder="Nhập mật khẩu"
                            required
                            disabled={loading}
                        />
                    </div>

                    <button
                        type="submit"
                        className="login-button"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <span className="spinner"></span>
                                Đang đăng nhập...
                            </>
                        ) : (
                            <>
                                <i className="bi bi-box-arrow-in-right"></i>
                                Đăng nhập
                            </>
                        )}
                    </button>

                    <div className="login-info">
                        <p><strong>Thông tin đăng nhập (Hardcode):</strong></p>
                        <p>Username: <code>admin</code></p>
                        <p>Password: <code>admin123</code></p>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AdminLogin;


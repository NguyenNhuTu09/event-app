import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { path } from '../../../utils/constant';
import { authAPI } from '../../../service/api';
import './AdminLogin.css';
import logoImageAdmin from '../../../assets/images/LOGO WEBIE ENENT-01.png';
const AdminLogin = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

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

        try {
            // Call API to login
            const response = await authAPI.login(formData.email, formData.password);

            if (!response || !response.user) {
                setError('Đăng nhập thất bại. Vui lòng thử lại.');
                return;
            }

            const userRole = response.user.role;

            // Check if user has super-admin role (SADMIN)
            if (userRole === 'SADMIN') {
                // Store token and user info for super-admin
                localStorage.setItem('adminToken', response.accessToken);
                localStorage.setItem('adminUser', JSON.stringify({
                    username: response.user.username,
                    email: response.user.email,
                    role: 'super-admin', // Map SADMIN to super-admin for frontend
                    userId: response.user.id,
                }));

                // Redirect to super-admin dashboard
                navigate(path.SUPER_ADMIN_DASHBOARD);
            }
            // Check if user is an organizer (ORGANIZER role)
            // Note: If user has ORGANIZER role, it means they have been approved
            // (backend sets role to ORGANIZER when organizer is approved)
            else if (userRole === 'ORGANIZER') {
                // Organizer is approved - redirect to partner dashboard
                localStorage.setItem('adminToken', response.accessToken);
                localStorage.setItem('adminUser', JSON.stringify({
                    username: response.user.username,
                    email: response.user.email,
                    role: 'partner',
                    userId: response.user.id,
                }));
                navigate(path.PARTNER_DASHBOARD);
            }
            else {
                setError('Tài khoản này không có quyền truy cập trang quản trị. Vui lòng đăng nhập với tài khoản Super Admin hoặc đối tác đã được duyệt.');
            }
        } catch (err) {
            console.error('Login error:', err);
            setError(err.message || 'Tên đăng nhập hoặc mật khẩu không chính xác!');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="admin-login-container">
            <div className="admin-login-box">
                <div className="login-header">
                    <div className="login-logo">
                        <img src={logoImageAdmin} alt="Webie Event" className="logo-image" />
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
                        <label htmlFor="email">
                            <i className="bi bi-envelope"></i>
                            Email
                        </label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            placeholder="Nhập email đăng nhập"
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
                        <p><strong>Đăng nhập Super Admin</strong></p>
                        <p><em>Vui lòng sử dụng tài khoản có quyền SADMIN để đăng nhập</em></p>
                        <p><em>Lưu ý: Partner đăng nhập tại <a href={path.PARTNER_LOGIN} style={{ color: '#3b82f6' }}>/partner/login</a></em></p>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AdminLogin;






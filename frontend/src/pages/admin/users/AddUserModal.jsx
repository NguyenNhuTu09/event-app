import React, { useState } from 'react';
import './AddUserModal.css';

const AddUserModal = ({
    isOpen,
    onClose,
    onSubmit,
    existingEmails = [],
}) => {
    const [loading, setLoading] = useState(false);
    const [errors, setErrors] = useState({});

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        role: 'User',
        password: '',
        confirmPassword: '',
    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value,
        }));
        // Clear error when user starts typing
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: '',
            }));
        }
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = 'Tên không được để trống';
        }

        if (!formData.email.trim()) {
            newErrors.email = 'Email không được để trống';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Email không hợp lệ';
        }

        if (!formData.password) {
            newErrors.password = 'Mật khẩu không được để trống';
        } else if (formData.password.length < 6) {
            newErrors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
        }

        if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Mật khẩu xác nhận không khớp';
        }

        // Check if email already exists
        if (existingEmails.includes(formData.email)) {
            newErrors.email = 'Email đã tồn tại trong hệ thống';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!validateForm()) {
            return;
        }

        setLoading(true);

        // Simulate API call
        setTimeout(() => {
            onSubmit({
                name: formData.name,
                email: formData.email,
                role: formData.role,
                password: formData.password,
            });

            // Reset form
            setFormData({
                name: '',
                email: '',
                role: 'User',
                password: '',
                confirmPassword: '',
            });
            setErrors({});
            setLoading(false);
            onClose();
        }, 500);
    };

    const handleClose = () => {
        setFormData({
            name: '',
            email: '',
            role: 'User',
            password: '',
            confirmPassword: '',
        });
        setErrors({});
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h3>Thêm User Mới</h3>
                    <button className="modal-close" onClick={handleClose} type="button">
                        <i className="bi bi-x-lg"></i>
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="modal-form">
                    <div className="form-group">
                        <label htmlFor="name">
                            <i className="bi bi-person"></i>
                            Tên <span className="required">*</span>
                        </label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            placeholder="Nhập tên người dùng"
                            className={errors.name ? 'error' : ''}
                        />
                        {errors.name && <span className="error-message">{errors.name}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">
                            <i className="bi bi-envelope"></i>
                            Email <span className="required">*</span>
                        </label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            placeholder="Nhập email"
                            className={errors.email ? 'error' : ''}
                        />
                        {errors.email && <span className="error-message">{errors.email}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="role">
                            <i className="bi bi-shield-check"></i>
                            Vai trò <span className="required">*</span>
                        </label>
                        <select
                            id="role"
                            name="role"
                            value={formData.role}
                            onChange={handleInputChange}
                        >
                            <option value="User">User</option>
                            <option value="Admin">Admin</option>
                        </select>
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">
                            <i className="bi bi-lock"></i>
                            Mật khẩu <span className="required">*</span>
                        </label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            placeholder="Nhập mật khẩu (tối thiểu 6 ký tự)"
                            className={errors.password ? 'error' : ''}
                        />
                        {errors.password && <span className="error-message">{errors.password}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">
                            <i className="bi bi-lock-fill"></i>
                            Xác nhận mật khẩu <span className="required">*</span>
                        </label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleInputChange}
                            placeholder="Nhập lại mật khẩu"
                            className={errors.confirmPassword ? 'error' : ''}
                        />
                        {errors.confirmPassword && <span className="error-message">{errors.confirmPassword}</span>}
                    </div>

                    <div className="modal-actions">
                        <button
                            type="button"
                            className="btn-cancel"
                            onClick={handleClose}
                            disabled={loading}
                        >
                            Hủy
                        </button>
                        <button
                            type="submit"
                            className="btn-submit"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <span className="spinner"></span>
                                    Đang thêm...
                                </>
                            ) : (
                                <>
                                    <i className="bi bi-check-circle"></i>
                                    Thêm User
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default AddUserModal;




















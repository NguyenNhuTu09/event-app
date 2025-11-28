import React, { useState } from 'react';
import './PartnerSupport.css';

const PartnerSupport = () => {
    const [formData, setFormData] = useState({
        title: '',
        category: '',
        priority: 'Trung bình',
        description: '',
    });
    const [sending, setSending] = useState(false);
    const [sent, setSent] = useState(false);
    const [error, setError] = useState('');

    // Fake data - Lịch sử yêu cầu đã gửi
    const [requestHistory] = useState([
        {
            id: 1,
            title: 'Yêu cầu hỗ trợ kỹ thuật',
            category: 'Kỹ thuật',
            priority: 'Cao',
            status: 'Chờ xử lý',
            date: '2024-01-15',
            description: 'Hệ thống quản lý event không hoạt động đúng. Không thể tạo event mới...',
        },
        {
            id: 2,
            title: 'Yêu cầu cập nhật thông tin event',
            category: 'Cập nhật',
            priority: 'Trung bình',
            status: 'Đang xử lý',
            date: '2024-01-10',
            description: 'Cần thay đổi địa điểm và thời gian tổ chức event...',
        },
        {
            id: 3,
            title: 'Báo cáo lỗi hiển thị',
            category: 'Báo lỗi',
            priority: 'Thấp',
            status: 'Đã giải quyết',
            date: '2024-01-05',
            description: 'Hình ảnh event không hiển thị đúng trên trang chi tiết...',
        },
    ]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        
        if (!formData.title || !formData.category || !formData.description) {
            setError('Vui lòng điền đầy đủ thông tin bắt buộc!');
            return;
        }

        setSending(true);

        // Simulate API call
        setTimeout(() => {
            console.log('Support request submitted:', formData);
            setSending(false);
            setSent(true);
            
            // Reset form
            setFormData({
                title: '',
                category: '',
                priority: 'Trung bình',
                description: '',
            });

            // Hide success message after 5 seconds
            setTimeout(() => {
                setSent(false);
            }, 5000);
        }, 1000);
    };

    const getStatusColor = (status) => {
        switch (status) {
            case 'Chờ xử lý':
                return '#f59e0b';
            case 'Đang xử lý':
                return '#3b82f6';
            case 'Đã giải quyết':
                return '#10b981';
            default:
                return '#64748b';
        }
    };

    const getPriorityColor = (priority) => {
        switch (priority) {
            case 'Cao':
            case 'Khẩn cấp':
                return '#ef4444';
            case 'Trung bình':
                return '#f59e0b';
            case 'Thấp':
                return '#10b981';
            default:
                return '#64748b';
        }
    };

    return (
        <div className="partner-support-page">
            <div className="support-header">
                <h2>Hỗ trợ</h2>
                <p>Gửi yêu cầu hỗ trợ cho admin hệ thống</p>
            </div>

            <div className="support-content-grid">
                {/* Left Column - Form và Lịch sử */}
                <div className="support-main-content">
                    {/* Gửi yêu cầu Form */}
                    <div className="support-card">
                        <div className="card-header">
                            <h3>
                                <i className="bi bi-send"></i>
                                Gửi yêu cầu hỗ trợ
                            </h3>
                        </div>
                        <div className="card-body">
                            {sent && (
                                <div className="success-message">
                                    <i className="bi bi-check-circle"></i>
                                    <p>Yêu cầu của bạn đã được gửi thành công! Admin sẽ phản hồi sớm nhất có thể.</p>
                                </div>
                            )}

                            {error && (
                                <div className="error-message">
                                    <i className="bi bi-exclamation-circle"></i>
                                    <p>{error}</p>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} className="support-form">
                                <div className="form-group">
                                    <label htmlFor="title">
                                        <i className="bi bi-tag"></i>
                                        Tiêu đề yêu cầu <span className="required">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        id="title"
                                        name="title"
                                        value={formData.title}
                                        onChange={handleInputChange}
                                        placeholder="Nhập tiêu đề yêu cầu hỗ trợ"
                                        required
                                    />
                                </div>

                                <div className="form-row">
                                    <div className="form-group">
                                        <label htmlFor="category">
                                            <i className="bi bi-folder"></i>
                                            Danh mục <span className="required">*</span>
                                        </label>
                                        <select
                                            id="category"
                                            name="category"
                                            value={formData.category}
                                            onChange={handleInputChange}
                                            required
                                        >
                                            <option value="">Chọn danh mục</option>
                                            <option value="Kỹ thuật">Kỹ thuật</option>
                                            <option value="Thanh toán">Thanh toán</option>
                                            <option value="Cập nhật">Cập nhật thông tin</option>
                                            <option value="Báo lỗi">Báo lỗi</option>
                                            <option value="Khác">Khác</option>
                                        </select>
                                    </div>

                                    <div className="form-group">
                                        <label htmlFor="priority">
                                            <i className="bi bi-flag"></i>
                                            Mức độ ưu tiên
                                        </label>
                                        <select
                                            id="priority"
                                            name="priority"
                                            value={formData.priority}
                                            onChange={handleInputChange}
                                        >
                                            <option value="Thấp">Thấp</option>
                                            <option value="Trung bình">Trung bình</option>
                                            <option value="Cao">Cao</option>
                                            <option value="Khẩn cấp">Khẩn cấp</option>
                                        </select>
                                    </div>
                                </div>

                                <div className="form-group">
                                    <label htmlFor="description">
                                        <i className="bi bi-chat-left-text"></i>
                                        Mô tả chi tiết <span className="required">*</span>
                                    </label>
                                    <textarea
                                        id="description"
                                        name="description"
                                        value={formData.description}
                                        onChange={handleInputChange}
                                        placeholder="Mô tả chi tiết vấn đề hoặc yêu cầu của bạn..."
                                        rows={6}
                                        required
                                    ></textarea>
                                </div>

                                <button
                                    type="submit"
                                    className="submit-btn"
                                    disabled={sending}
                                >
                                    {sending ? (
                                        <>
                                            <span className="spinner"></span>
                                            Đang gửi...
                                        </>
                                    ) : (
                                        <>
                                            <i className="bi bi-send"></i>
                                            Gửi yêu cầu
                                        </>
                                    )}
                                </button>
                            </form>
                        </div>
                    </div>

                    {/* Lịch sử yêu cầu */}
                    <div className="support-card">
                        <div className="card-header">
                            <h3>
                                <i className="bi bi-clock-history"></i>
                                Lịch sử yêu cầu
                            </h3>
                        </div>
                        <div className="card-body">
                            <div className="request-history">
                                {requestHistory.map((request) => (
                                    <div key={request.id} className="history-item">
                                        <div className="history-header">
                                            <div className="history-title-section">
                                                <h4>{request.title}</h4>
                                                <div className="history-meta">
                                                    <span className="history-date">
                                                        <i className="bi bi-calendar"></i>
                                                        {request.date}
                                                    </span>
                                                    <span 
                                                        className="history-category"
                                                        style={{ 
                                                            backgroundColor: `${getPriorityColor(request.priority)}20`,
                                                            color: getPriorityColor(request.priority)
                                                        }}
                                                    >
                                                        {request.category}
                                                    </span>
                                                    <span 
                                                        className="history-priority"
                                                        style={{ 
                                                            backgroundColor: `${getPriorityColor(request.priority)}20`,
                                                            color: getPriorityColor(request.priority)
                                                        }}
                                                    >
                                                        {request.priority}
                                                    </span>
                                                </div>
                                            </div>
                                            <span 
                                                className="history-status"
                                                style={{ 
                                                    backgroundColor: `${getStatusColor(request.status)}20`,
                                                    color: getStatusColor(request.status),
                                                    borderColor: getStatusColor(request.status)
                                                }}
                                            >
                                                {request.status}
                                            </span>
                                        </div>
                                        <div className="history-description">
                                            <p>{request.description}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Column - Hướng dẫn sử dụng */}
                <div className="support-sidebar">
                    <div className="guide-card">
                        <div className="guide-header">
                            <h3>
                                <i className="bi bi-book"></i>
                                Hướng dẫn sử dụng
                            </h3>
                        </div>
                        <div className="guide-content">
                            <div className="guide-item">
                                <div className="guide-icon">
                                    <i className="bi bi-1-circle-fill"></i>
                                </div>
                                <div className="guide-text">
                                    <h4>Tạo Event mới</h4>
                                    <p>Vào trang "Quản lý Event" → Nhấn "Thêm Event" → Điền đầy đủ thông tin event → Lưu lại</p>
                                </div>
                            </div>

                            <div className="guide-item">
                                <div className="guide-icon">
                                    <i className="bi bi-2-circle-fill"></i>
                                </div>
                                <div className="guide-text">
                                    <h4>Chỉnh sửa Event</h4>
                                    <p>Tìm event cần chỉnh sửa → Nhấn icon "Sửa" → Cập nhật thông tin → Lưu thay đổi</p>
                                </div>
                            </div>

                            <div className="guide-item">
                                <div className="guide-icon">
                                    <i className="bi bi-3-circle-fill"></i>
                                </div>
                                <div className="guide-text">
                                    <h4>Xem Dashboard</h4>
                                    <p>Trang Dashboard hiển thị tổng quan về events, số lượng người tham gia và thống kê của bạn</p>
                                </div>
                            </div>

                            <div className="guide-item">
                                <div className="guide-icon">
                                    <i className="bi bi-4-circle-fill"></i>
                                </div>
                                <div className="guide-text">
                                    <h4>Gửi yêu cầu hỗ trợ</h4>
                                    <p>Điền đầy đủ thông tin vào form bên trái → Chọn danh mục và mức độ ưu tiên → Gửi yêu cầu</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="contact-card">
                        <div className="contact-header">
                            <h3>
                                <i className="bi bi-headset"></i>
                                Liên hệ nhanh
                            </h3>
                        </div>
                        <div className="contact-content">
                            <div className="contact-item">
                                <i className="bi bi-envelope"></i>
                                <div>
                                    <span className="contact-label">Email</span>
                                    <span className="contact-value">support@emsevent.com</span>
                                </div>
                            </div>
                            <div className="contact-item">
                                <i className="bi bi-telephone"></i>
                                <div>
                                    <span className="contact-label">Hotline</span>
                                    <span className="contact-value">1900-1234</span>
                                </div>
                            </div>
                            <div className="contact-item">
                                <i className="bi bi-clock"></i>
                                <div>
                                    <span className="contact-label">Thời gian hỗ trợ</span>
                                    <span className="contact-value">8:00 - 18:00 (T2-T6)</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PartnerSupport;














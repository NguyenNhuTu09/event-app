import React, { useState } from 'react';
import './AdminSupport.css';

const AdminSupport = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('all');
    const [typeFilter, setTypeFilter] = useState('all');

    // Fake data - EMS Event
    const [supportRequests] = useState([
        {
            id: 1,
            partnerName: 'partner_vt_6',
            email: 'partner_vt_6@gmail.com',
            phone: '0123456789',
            eventName: 'Hội thảo Công nghệ 2024',
            request: 'Yêu cầu hỗ trợ kỹ thuật: Hệ thống quản lý event không hoạt động đúng. Không thể tạo event mới hoặc chỉnh sửa thông tin event...',
            tags: ['Kỹ thuật', 'Cao'],
            status: 'Chờ xử lý',
        },
        {
            id: 2,
            partnerName: 'partner_pt_5',
            email: 'partner_pt_5@gmail.com',
            phone: '0987654321',
            eventName: 'Workshop Marketing Digital',
            request: 'Gửi yêu cầu hỗ trợ đăng tải event mới: Cần hỗ trợ tạo event mới với thông tin đặc biệt',
            tags: ['Vi phạm', 'Khẩn cấp'],
            status: 'Đang xử lý',
        },
        {
            id: 3,
            partnerName: 'Lê Văn C',
            email: 'admin3@event.com',
            phone: '0912345678',
            eventName: 'Seminar Khởi nghiệp',
            request: 'Yêu cầu cập nhật thông tin: Cần thay đổi địa điểm và thời gian tổ chức event...',
            tags: ['Cập nhật', 'Trung bình'],
            status: 'Đã giải quyết',
        },
        {
            id: 4,
            partnerName: 'partner_hn_7',
            email: 'partner_hn_7@gmail.com',
            phone: '0923456789',
            eventName: 'Conference AI & Machine Learning',
            request: 'Yêu cầu hỗ trợ thanh toán: Giao dịch thanh toán phí event bị lỗi, không nhận được xác nhận từ hệ thống...',
            tags: ['Thanh toán', 'Cao'],
            status: 'Chờ xử lý',
        },
        {
            id: 5,
            partnerName: 'partner_hn_1',
            email: 'partner_hn_1@gmail.com',
            phone: '0934567890',
            eventName: 'Tech Summit 2024',
            request: 'Báo cáo lỗi: Hình ảnh event không hiển thị đúng trên trang chi tiết event. Cần kiểm tra và sửa lỗi.',
            tags: ['Báo lỗi', 'Trung bình'],
            status: 'Chờ xử lý',
        },
    ]);

    const filteredRequests = supportRequests.filter(request => {
        const matchesSearch =
            request.partnerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            request.eventName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            request.phone.includes(searchTerm) ||
            request.request.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesStatus = statusFilter === 'all' || request.status === statusFilter;
        const matchesType = typeFilter === 'all' || request.tags[0] === typeFilter;

        return matchesSearch && matchesStatus && matchesType;
    });

    const stats = {
        total: supportRequests.length,
        pending: supportRequests.filter(r => r.status === 'Chờ xử lý').length,
        inProgress: supportRequests.filter(r => r.status === 'Đang xử lý').length,
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

    const getTagColor = (tag) => {
        switch (tag) {
            case 'Kỹ thuật':
                return '#3b82f6';
            case 'Thanh toán':
                return '#8b5cf6';
            case 'Vi phạm':
                return '#ef4444';
            case 'Cập nhật':
                return '#10b981';
            case 'Báo lỗi':
                return '#f59e0b';
            case 'Cao':
            case 'Khẩn cấp':
                return '#ef4444';
            case 'Trung bình':
                return '#f59e0b';
            default:
                return '#64748b';
        }
    };

    return (
        <div className="admin-support-page">
            {/* Sample Data Button */}
            <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'flex-end' }}>
                <button className="sample-data-btn">
                    Dữ liệu mẫu
                </button>
            </div>

            {/* Search and Filter Section */}
            <div className="search-filter-section">
                <div className="search-box">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Tìm theo tên partner, event, SĐT, yêu cầu..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filters">
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">Lọc theo trạng thái: Tất cả</option>
                        <option value="Chờ xử lý">Chờ xử lý</option>
                        <option value="Đang xử lý">Đang xử lý</option>
                        <option value="Đã giải quyết">Đã giải quyết</option>
                    </select>
                    <select
                        value={typeFilter}
                        onChange={(e) => setTypeFilter(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">Lọc theo: Tất cả</option>
                        <option value="Kỹ thuật">Kỹ thuật</option>
                        <option value="Thanh toán">Thanh toán</option>
                        <option value="Vi phạm">Vi phạm</option>
                        <option value="Cập nhật">Cập nhật</option>
                        <option value="Báo lỗi">Báo lỗi</option>
                    </select>
                    <button
                        className="clear-filter-btn"
                        onClick={() => {
                            setSearchTerm('');
                            setStatusFilter('all');
                            setTypeFilter('all');
                        }}
                    >
                        Xóa bộ lọc
                    </button>
                </div>
            </div>

            {/* Summary Cards */}
            <div className="summary-cards">
                <div className="summary-card outlined">
                    <div className="card-label">Tổng yêu cầu</div>
                    <div className="card-value">{stats.total}</div>
                </div>
                <div className="summary-card outlined orange">
                    <div className="card-label">Chờ xử lý</div>
                    <div className="card-value">{stats.pending}</div>
                </div>
                <div className="summary-card outlined blue">
                    <div className="card-label">Đang xử lý</div>
                    <div className="card-value">{stats.inProgress}</div>
                </div>
            </div>

            {/* Data Table */}
            <div className="support-table-container">
                <table className="support-table">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>Tên Partner</th>
                            <th>Số điện thoại</th>
                            <th>Event</th>
                            <th>Yêu cầu</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredRequests.length === 0 ? (
                            <tr>
                                <td colSpan={7} className="no-data">
                                    Không tìm thấy yêu cầu nào
                                </td>
                            </tr>
                        ) : (
                            filteredRequests.map((request, index) => (
                                <tr key={request.id}>
                                    <td>{index + 1}</td>
                                    <td>
                                        <div className="owner-info">
                                            <div className="owner-name">{request.partnerName}</div>
                                            <div className="owner-email">{request.email}</div>
                                        </div>
                                    </td>
                                    <td>{request.phone}</td>
                                    <td>{request.eventName}</td>
                                    <td>
                                        <div className="request-info">
                                            <div className="request-text">{request.request}</div>
                                            <div className="request-tags">
                                                {request.tags.map((tag, idx) => (
                                                    <span
                                                        key={idx}
                                                        className="request-tag"
                                                        style={{
                                                            backgroundColor: `${getTagColor(tag)}20`,
                                                            color: getTagColor(tag),
                                                            borderColor: getTagColor(tag)
                                                        }}
                                                    >
                                                        {tag}
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <select
                                            className="status-select"
                                            style={{
                                                color: getStatusColor(request.status),
                                                borderColor: getStatusColor(request.status)
                                            }}
                                            defaultValue={request.status}
                                        >
                                            <option value="Chờ xử lý">Chờ xử lý</option>
                                            <option value="Đang xử lý">Đang xử lý</option>
                                            <option value="Đã giải quyết">Đã giải quyết</option>
                                        </select>
                                    </td>
                                    <td>
                                        <button className="action-btn" title="Xem chi tiết">
                                            <i className="bi bi-eye"></i>
                                        </button>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default AdminSupport;

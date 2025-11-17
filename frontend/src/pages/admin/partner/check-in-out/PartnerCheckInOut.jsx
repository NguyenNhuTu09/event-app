import React, { useState } from 'react';
import './PartnerCheckInOut.css';

const PartnerCheckInOut = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [eventFilter, setEventFilter] = useState('all');
    const [statusFilter, setStatusFilter] = useState('all');

    // Stats về check-in/check-out
    const stats = [
        {
            title: 'Tổng số Check-in',
            value: '1,245',
            icon: 'bi-box-arrow-in-right',
            color: '#10b981',
        },
        {
            title: 'Tổng số Check-out',
            value: '1,180',
            icon: 'bi-box-arrow-right',
            color: '#3b82f6',
        },
        {
            title: 'Chưa Check-in',
            value: '65',
            icon: 'bi-clock-history',
            color: '#f59e0b',
        },
        {
            title: 'Đang tham gia',
            value: '1,180',
            icon: 'bi-people-fill',
            color: '#8b5cf6',
        },
    ];

    // Fake data - Lịch sử check-in/check-out
    const [checkInOutHistory] = useState([
        {
            id: 1,
            userName: 'Nguyễn Văn A',
            email: 'nguyenvana@example.com',
            phone: '0123456789',
            eventName: 'Hội thảo Công nghệ 2024',
            eventDate: '2024-01-15',
            checkInTime: '2024-01-15 08:30:00',
            checkOutTime: '2024-01-15 17:45:00',
            status: 'Đã check-out',
        },
        {
            id: 2,
            userName: 'Trần Thị B',
            email: 'tranthib@example.com',
            phone: '0987654321',
            eventName: 'Hội thảo Công nghệ 2024',
            eventDate: '2024-01-15',
            checkInTime: '2024-01-15 09:15:00',
            checkOutTime: null,
            status: 'Đang tham gia',
        },
        {
            id: 3,
            userName: 'Lê Văn C',
            email: 'levanc@example.com',
            phone: '0912345678',
            eventName: 'Workshop Marketing Digital',
            eventDate: '2024-01-20',
            checkInTime: '2024-01-20 13:20:00',
            checkOutTime: '2024-01-20 16:30:00',
            status: 'Đã check-out',
        },
        {
            id: 4,
            userName: 'Phạm Thị D',
            email: 'phamthid@example.com',
            phone: '0923456789',
            eventName: 'Workshop Marketing Digital',
            eventDate: '2024-01-20',
            checkInTime: null,
            checkOutTime: null,
            status: 'Chưa check-in',
        },
        {
            id: 5,
            userName: 'Hoàng Văn E',
            email: 'hoangvane@example.com',
            phone: '0934567890',
            eventName: 'Seminar Khởi nghiệp',
            eventDate: '2024-01-25',
            checkInTime: '2024-01-25 09:00:00',
            checkOutTime: null,
            status: 'Đang tham gia',
        },
        {
            id: 6,
            userName: 'Võ Thị F',
            email: 'vothif@example.com',
            phone: '0945678901',
            eventName: 'Seminar Khởi nghiệp',
            eventDate: '2024-01-25',
            checkInTime: '2024-01-25 10:30:00',
            checkOutTime: '2024-01-25 15:20:00',
            status: 'Đã check-out',
        },
    ]);

    // Get unique events for filter
    const uniqueEvents = [...new Set(checkInOutHistory.map(item => item.eventName))];

    const filteredHistory = checkInOutHistory.filter(item => {
        const matchesSearch =
            item.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
            item.phone.includes(searchTerm) ||
            item.eventName.toLowerCase().includes(searchTerm.toLowerCase());

        const matchesEvent = eventFilter === 'all' || item.eventName === eventFilter;
        const matchesStatus = statusFilter === 'all' || item.status === statusFilter;

        return matchesSearch && matchesEvent && matchesStatus;
    });

    const getStatusColor = (status) => {
        switch (status) {
            case 'Đã check-out':
                return '#10b981';
            case 'Đang tham gia':
                return '#3b82f6';
            case 'Chưa check-in':
                return '#f59e0b';
            default:
                return '#64748b';
        }
    };

    const formatDateTime = (dateTime) => {
        if (!dateTime) return '-';
        const date = new Date(dateTime);
        return date.toLocaleString('vi-VN', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    };

    return (
        <div className="partner-checkinout-page">
            <div className="page-header">
                <h2>Check-in/Check-out</h2>
                <p>Thống kê lịch sử điểm danh của user đến event</p>
            </div>

            {/* Stats Cards */}
            <div className="stats-grid">
                {stats.map((stat, index) => (
                    <div key={index} className="stat-card">
                        <div className="stat-icon" style={{ background: `${stat.color}20`, color: stat.color }}>
                            <i className={stat.icon}></i>
                        </div>
                        <div className="stat-content">
                            <h3>{stat.value}</h3>
                            <p>{stat.title}</p>
                        </div>
                    </div>
                ))}
            </div>

            {/* Search and Filter */}
            <div className="search-filter-section">
                <div className="search-box">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Tìm theo tên, email, SĐT, event..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filters">
                    <select
                        value={eventFilter}
                        onChange={(e) => setEventFilter(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">Tất cả Event</option>
                        {uniqueEvents.map((event, idx) => (
                            <option key={idx} value={event}>{event}</option>
                        ))}
                    </select>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="filter-select"
                    >
                        <option value="all">Tất cả trạng thái</option>
                        <option value="Đã check-out">Đã check-out</option>
                        <option value="Đang tham gia">Đang tham gia</option>
                        <option value="Chưa check-in">Chưa check-in</option>
                    </select>
                    <button
                        className="clear-filter-btn"
                        onClick={() => {
                            setSearchTerm('');
                            setEventFilter('all');
                            setStatusFilter('all');
                        }}
                    >
                        Xóa bộ lọc
                    </button>
                </div>
            </div>

            {/* Data Table */}
            <div className="table-card">
                <div className="card-header">
                    <h3>Lịch sử Check-in/Check-out</h3>
                    <span className="total-count">Tổng: {filteredHistory.length} bản ghi</span>
                </div>
                <div className="table-container">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>STT</th>
                                <th>User</th>
                                <th>Event</th>
                                <th>Ngày Event</th>
                                <th>Thời gian Check-in</th>
                                <th>Thời gian Check-out</th>
                                <th>Trạng thái</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredHistory.length === 0 ? (
                                <tr>
                                    <td colSpan={8} className="no-data">
                                        Không tìm thấy bản ghi nào
                                    </td>
                                </tr>
                            ) : (
                                filteredHistory.map((item, index) => (
                                    <tr key={item.id}>
                                        <td>{index + 1}</td>
                                        <td>
                                            <div className="user-info">
                                                <div className="user-name">{item.userName}</div>
                                                <div className="user-details">
                                                    <span className="user-email">{item.email}</span>
                                                    <span className="user-phone">{item.phone}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <div className="event-info">
                                                <div className="event-name">{item.eventName}</div>
                                            </div>
                                        </td>
                                        <td>{item.eventDate}</td>
                                        <td>
                                            <div className="time-info">
                                                {item.checkInTime ? (
                                                    <>
                                                        <i className="bi bi-box-arrow-in-right" style={{ color: '#10b981' }}></i>
                                                        <span>{formatDateTime(item.checkInTime)}</span>
                                                    </>
                                                ) : (
                                                    <span className="no-time">-</span>
                                                )}
                                            </div>
                                        </td>
                                        <td>
                                            <div className="time-info">
                                                {item.checkOutTime ? (
                                                    <>
                                                        <i className="bi bi-box-arrow-right" style={{ color: '#3b82f6' }}></i>
                                                        <span>{formatDateTime(item.checkOutTime)}</span>
                                                    </>
                                                ) : (
                                                    <span className="no-time">-</span>
                                                )}
                                            </div>
                                        </td>
                                        <td>
                                            <span
                                                className="status-badge"
                                                style={{
                                                    backgroundColor: `${getStatusColor(item.status)}20`,
                                                    color: getStatusColor(item.status),
                                                    borderColor: getStatusColor(item.status)
                                                }}
                                            >
                                                {item.status}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="action-buttons">
                                                <button className="btn-icon" title="Xem chi tiết">
                                                    <i className="bi bi-eye"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default PartnerCheckInOut;

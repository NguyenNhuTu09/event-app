import React from 'react';
import './SuperAdminDashboard.css';

const SuperAdminDashboard = () => {
    // Hardcode data for super admin dashboard
    const stats = [
        {
            title: 'Tổng số Partner',
            value: '12',
            icon: 'bi-people',
            color: '#dc2626',
            change: '+2',
        },
        {
            title: 'Tổng số Admin',
            value: '45',
            icon: 'bi-person-badge',
            color: '#3b82f6',
            change: '+5',
        },
        {
            title: 'Tổng số Event',
            value: '1,234',
            icon: 'bi-calendar-event',
            color: '#10b981',
            change: '+156',
        },
        {
            title: 'Tổng số User',
            value: '15,678',
            icon: 'bi-person-circle',
            color: '#8b5cf6',
            change: '+1,234',
        },
    ];

    const recentPartners = [
        { id: 1, name: 'Partner A', email: 'partnera@example.com', status: 'Hoạt động', events: 45, users: 1200, created: '2024-01-10' },
        { id: 2, name: 'Partner B', email: 'partnerb@example.com', status: 'Hoạt động', events: 32, users: 890, created: '2024-01-15' },
        { id: 3, name: 'Partner C', email: 'partnerc@example.com', status: 'Tạm dừng', events: 18, users: 450, created: '2024-01-20' },
        { id: 4, name: 'Partner D', email: 'partnerd@example.com', status: 'Hoạt động', events: 67, users: 2100, created: '2024-01-25' },
    ];

    return (
        <div className="super-admin-dashboard">
            <div className="dashboard-header">
                <h2>Super Admin Dashboard</h2>
                <p>Quản lý toàn bộ hệ thống và các Partner</p>
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
                            <span className="stat-change" style={{ color: stat.color }}>
                                <i className="bi bi-arrow-up"></i>
                                {stat.change}
                            </span>
                        </div>
                    </div>
                ))}
            </div>

            <div className="dashboard-content">
                {/* Recent Partners */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h3>Danh sách Partner</h3>
                        <a href="#" className="view-all-link">Xem tất cả</a>
                    </div>
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Tên Partner</th>
                                    <th>Email</th>
                                    <th>Trạng thái</th>
                                    <th>Số Event</th>
                                    <th>Số User</th>
                                    <th>Ngày tạo</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentPartners.map((partner) => (
                                    <tr key={partner.id}>
                                        <td>{partner.name}</td>
                                        <td>{partner.email}</td>
                                        <td>
                                            <span className={`status-badge status-${partner.status.toLowerCase().replace(/\s/g, '-')}`}>
                                                {partner.status}
                                            </span>
                                        </td>
                                        <td>{partner.events}</td>
                                        <td>{partner.users}</td>
                                        <td>{partner.created}</td>
                                        <td>
                                            <div className="action-buttons">
                                                <button className="btn-icon" title="Xem">
                                                    <i className="bi bi-eye"></i>
                                                </button>
                                                <button className="btn-icon" title="Sửa">
                                                    <i className="bi bi-pencil"></i>
                                                </button>
                                                <button className="btn-icon btn-danger" title="Xóa">
                                                    <i className="bi bi-trash"></i>
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SuperAdminDashboard;


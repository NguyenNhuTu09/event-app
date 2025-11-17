import React from 'react';
import './PartnerDashboard.css';

const PartnerDashboard = () => {
    // Hardcode data for partner dashboard
    const stats = [
        {
            title: 'Tổng số Event',
            value: '45',
            icon: 'bi-calendar-event',
            color: '#10b981',
            change: '+5',
        },
        {
            title: 'Event đang diễn ra',
            value: '12',
            icon: 'bi-play-circle',
            color: '#f59e0b',
            change: '+2',
        },
        {
            title: 'Tổng số User',
            value: '1,200',
            icon: 'bi-people',
            color: '#3b82f6',
            change: '+120',
        },
        {
            title: 'Doanh thu tháng',
            value: '₫25M',
            icon: 'bi-cash-coin',
            color: '#8b5cf6',
            change: '+15%',
        },
    ];

    const recentEvents = [
        { id: 1, name: 'Hội thảo Công nghệ 2024', date: '2024-01-15', status: 'Đang diễn ra', participants: 250 },
        { id: 2, name: 'Workshop Marketing Digital', date: '2024-01-20', status: 'Sắp diễn ra', participants: 180 },
        { id: 3, name: 'Seminar Khởi nghiệp', date: '2024-01-25', status: 'Sắp diễn ra', participants: 320 },
    ];

    return (
        <div className="partner-dashboard">
            <div className="dashboard-header">
                <h2>Dashboard</h2>
                <p>Chào mừng trở lại, Partner!</p>
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
                {/* Recent Events */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h3>Event gần đây</h3>
                        <a href="#" className="view-all-link">Xem tất cả</a>
                    </div>
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Tên Event</th>
                                    <th>Ngày</th>
                                    <th>Trạng thái</th>
                                    <th>Người tham gia</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentEvents.map((event) => (
                                    <tr key={event.id}>
                                        <td>{event.name}</td>
                                        <td>{event.date}</td>
                                        <td>
                                            <span className={`status-badge status-${event.status.toLowerCase().replace(/\s/g, '-')}`}>
                                                {event.status}
                                            </span>
                                        </td>
                                        <td>{event.participants}</td>
                                        <td>
                                            <div className="action-buttons">
                                                <button className="btn-icon" title="Xem">
                                                    <i className="bi bi-eye"></i>
                                                </button>
                                                <button className="btn-icon" title="Sửa">
                                                    <i className="bi bi-pencil"></i>
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

export default PartnerDashboard;




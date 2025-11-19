import React from 'react';
import './AdminDashboard.css';

const AdminDashboard = () => {
    // Hardcode data for dashboard
    const stats = [
        {
            title: 'Tổng số User',
            value: '1,234',
            icon: 'bi-people',
            color: '#3b82f6',
            change: '+12%',
        },
        {
            title: 'Tổng số Event',
            value: '456',
            icon: 'bi-calendar-event',
            color: '#10b981',
            change: '+8%',
        },
        {
            title: 'Event đang diễn ra',
            value: '89',
            icon: 'bi-play-circle',
            color: '#f59e0b',
            change: '+5%',
        },
        {
            title: 'Doanh thu tháng',
            value: '₫125M',
            icon: 'bi-cash-coin',
            color: '#8b5cf6',
            change: '+23%',
        },
    ];

    const recentEvents = [
        { id: 1, name: 'Hội thảo Công nghệ 2024', date: '2024-01-15', status: 'Đang diễn ra', participants: 250 },
        { id: 2, name: 'Workshop Marketing Digital', date: '2024-01-20', status: 'Sắp diễn ra', participants: 180 },
        { id: 3, name: 'Seminar Khởi nghiệp', date: '2024-01-25', status: 'Sắp diễn ra', participants: 320 },
        { id: 4, name: 'Conference AI & Machine Learning', date: '2024-02-01', status: 'Đã lên lịch', participants: 500 },
    ];

    const recentUsers = [
        { id: 1, name: 'Nguyễn Văn A', email: 'nguyenvana@example.com', role: 'User', joined: '2024-01-10' },
        { id: 2, name: 'Trần Thị B', email: 'tranthib@example.com', role: 'User', joined: '2024-01-12' },
        { id: 3, name: 'Lê Văn C', email: 'levanc@example.com', role: 'User', joined: '2024-01-14' },
    ];

    return (
        <div className="admin-dashboard">
            <div className="dashboard-header">
                <h2>Dashboard</h2>
                <p>Chào mừng trở lại, Admin!</p>
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

                {/* Recent Users */}
                <div className="dashboard-card">
                    <div className="card-header">
                        <h3>User mới đăng ký</h3>
                        <a href="#" className="view-all-link">Xem tất cả</a>
                    </div>
                    <div className="table-container">
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Tên</th>
                                    <th>Email</th>
                                    <th>Vai trò</th>
                                    <th>Ngày tham gia</th>
                                    <th>Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentUsers.map((user) => (
                                    <tr key={user.id}>
                                        <td>{user.name}</td>
                                        <td>{user.email}</td>
                                        <td>
                                            <span className="role-badge">{user.role}</span>
                                        </td>
                                        <td>{user.joined}</td>
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

export default AdminDashboard;










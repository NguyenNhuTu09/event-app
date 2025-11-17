import React, { useState } from 'react';
import './ManageEvents.css';

const ManageEvents = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');

    // Hardcode events data
    const [events] = useState([
        {
            id: 1,
            name: 'Hội thảo Công nghệ 2024',
            category: 'Công nghệ',
            date: '2024-01-15',
            time: '09:00',
            location: 'Hà Nội',
            status: 'Đang diễn ra',
            participants: 250,
            capacity: 300,
        },
        {
            id: 2,
            name: 'Workshop Marketing Digital',
            category: 'Marketing',
            date: '2024-01-20',
            time: '14:00',
            location: 'TP.HCM',
            status: 'Sắp diễn ra',
            participants: 180,
            capacity: 200,
        },
        {
            id: 3,
            name: 'Seminar Khởi nghiệp',
            category: 'Khởi nghiệp',
            date: '2024-01-25',
            time: '10:00',
            location: 'Đà Nẵng',
            status: 'Sắp diễn ra',
            participants: 320,
            capacity: 350,
        },
        {
            id: 4,
            name: 'Conference AI & Machine Learning',
            category: 'Công nghệ',
            date: '2024-02-01',
            time: '08:00',
            location: 'Hà Nội',
            status: 'Đã lên lịch',
            participants: 500,
            capacity: 600,
        },
        {
            id: 5,
            name: 'Event đã kết thúc',
            category: 'Giáo dục',
            date: '2024-01-05',
            time: '15:00',
            location: 'TP.HCM',
            status: 'Đã kết thúc',
            participants: 150,
            capacity: 200,
        },
    ]);

    const filteredEvents = events.filter(event => {
        const matchesSearch = event.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            event.category.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = filterStatus === 'all' || 
            event.status.toLowerCase().replace(/\s/g, '-') === filterStatus;
        return matchesSearch && matchesStatus;
    });

    const getStatusClass = (status) => {
        return status.toLowerCase().replace(/\s/g, '-');
    };

    return (
        <div className="manage-events">
            <div className="page-header">
                <div>
                    <h2>Quản lý Event</h2>
                    <p>Quản lý tất cả sự kiện trong hệ thống</p>
                </div>
                <button className="btn-primary">
                    <i className="bi bi-plus-circle"></i>
                    Tạo Event mới
                </button>
            </div>

            <div className="filters-bar">
                <div className="search-box">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Tìm kiếm theo tên hoặc danh mục..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filter-group">
                    <label>Lọc theo trạng thái:</label>
                    <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                        <option value="all">Tất cả</option>
                        <option value="đang-diễn-ra">Đang diễn ra</option>
                        <option value="sắp-diễn-ra">Sắp diễn ra</option>
                        <option value="đã-lên-lịch">Đã lên lịch</option>
                        <option value="đã-kết-thúc">Đã kết thúc</option>
                    </select>
                </div>
            </div>

            <div className="stats-summary">
                <div className="stat-item">
                    <span className="stat-label">Tổng số Event</span>
                    <span className="stat-value">{events.length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Đang diễn ra</span>
                    <span className="stat-value">{events.filter(e => e.status === 'Đang diễn ra').length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Sắp diễn ra</span>
                    <span className="stat-value">{events.filter(e => e.status === 'Sắp diễn ra').length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Tổng người tham gia</span>
                    <span className="stat-value">{events.reduce((sum, e) => sum + e.participants, 0)}</span>
                </div>
            </div>

            <div className="table-card">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên Event</th>
                            <th>Danh mục</th>
                            <th>Ngày & Giờ</th>
                            <th>Địa điểm</th>
                            <th>Trạng thái</th>
                            <th>Người tham gia</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredEvents.length === 0 ? (
                            <tr>
                                <td colSpan={8} className="no-data">
                                    Không tìm thấy event nào
                                </td>
                            </tr>
                        ) : (
                            filteredEvents.map((event) => (
                                <tr key={event.id}>
                                    <td>#{event.id}</td>
                                    <td>
                                        <div className="event-name">
                                            <strong>{event.name}</strong>
                                        </div>
                                    </td>
                                    <td>
                                        <span className="category-badge">{event.category}</span>
                                    </td>
                                    <td>
                                        <div className="date-time">
                                            <div>{event.date}</div>
                                            <div className="time">{event.time}</div>
                                        </div>
                                    </td>
                                    <td>{event.location}</td>
                                    <td>
                                        <span className={`status-badge status-${getStatusClass(event.status)}`}>
                                            {event.status}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="participants-info">
                                            <span>{event.participants}</span>
                                            <span className="capacity">/ {event.capacity}</span>
                                            <div className="progress-bar">
                                                <div
                                                    className="progress-fill"
                                                    style={{ width: `${(event.participants / event.capacity) * 100}%` }}
                                                ></div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="action-buttons">
                                            <button className="btn-icon" title="Xem chi tiết">
                                                <i className="bi bi-eye"></i>
                                            </button>
                                            <button className="btn-icon" title="Chỉnh sửa">
                                                <i className="bi bi-pencil"></i>
                                            </button>
                                            <button className="btn-icon" title="Sao chép">
                                                <i className="bi bi-files"></i>
                                            </button>
                                            <button className="btn-icon btn-danger" title="Xóa">
                                                <i className="bi bi-trash"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            <div className="pagination">
                <button className="page-btn" disabled>
                    <i className="bi bi-chevron-left"></i>
                </button>
                <span className="page-info">Trang 1 / 1</span>
                <button className="page-btn" disabled>
                    <i className="bi bi-chevron-right"></i>
                </button>
            </div>
        </div>
    );
};

export default ManageEvents;








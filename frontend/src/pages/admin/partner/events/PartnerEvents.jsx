import React, { useState } from 'react';
import './PartnerEvents.css';

const PartnerEvents = () => {
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
    ]);

    const filteredEvents = events.filter(event => {
        const matchesSearch = event.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            event.location.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = filterStatus === 'all' || event.status === filterStatus;
        return matchesSearch && matchesStatus;
    });

    return (
        <div className="partner-events">
            <div className="page-header">
                <div>
                    <h2>Quản lý Event</h2>
                    <p>Quản lý các sự kiện của bạn</p>
                </div>
                <button className="btn-primary">
                    <i className="bi bi-plus-circle"></i>
                    Thêm Event
                </button>
            </div>

            <div className="filters-bar">
                <div className="search-box">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Tìm kiếm event..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filter-group">
                    <label>Lọc theo trạng thái:</label>
                    <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                        <option value="all">Tất cả</option>
                        <option value="Đang diễn ra">Đang diễn ra</option>
                        <option value="Sắp diễn ra">Sắp diễn ra</option>
                        <option value="Đã lên lịch">Đã lên lịch</option>
                    </select>
                </div>
            </div>

            <div className="stats-summary">
                <div className="stat-item">
                    <span className="stat-label">Tổng số Event</span>
                    <span className="stat-value">{events.length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Event đang diễn ra</span>
                    <span className="stat-value">{events.filter(e => e.status === 'Đang diễn ra').length}</span>
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
                            <th>Tên Event</th>
                            <th>Danh mục</th>
                            <th>Ngày</th>
                            <th>Giờ</th>
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
                                    <td>{event.name}</td>
                                    <td>{event.category}</td>
                                    <td>{event.date}</td>
                                    <td>{event.time}</td>
                                    <td>{event.location}</td>
                                    <td>
                                        <span className={`status-badge status-${event.status.toLowerCase().replace(/\s/g, '-')}`}>
                                            {event.status}
                                        </span>
                                    </td>
                                    <td>{event.participants} / {event.capacity}</td>
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
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default PartnerEvents;









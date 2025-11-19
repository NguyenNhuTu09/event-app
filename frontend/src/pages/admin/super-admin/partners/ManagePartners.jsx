import React, { useState } from 'react';
import './ManagePartners.css';

const ManagePartners = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Hardcode partners data
    const [partners, setPartners] = useState([
        { id: 1, name: 'Partner A', email: 'partnera@example.com', status: 'Hoạt động', events: 45, users: 1200, created: '2024-01-10' },
        { id: 2, name: 'Partner B', email: 'partnerb@example.com', status: 'Hoạt động', events: 32, users: 890, created: '2024-01-15' },
        { id: 3, name: 'Partner C', email: 'partnerc@example.com', status: 'Tạm dừng', events: 18, users: 450, created: '2024-01-20' },
        { id: 4, name: 'Partner D', email: 'partnerd@example.com', status: 'Hoạt động', events: 67, users: 2100, created: '2024-01-25' },
        { id: 5, name: 'Partner E', email: 'partnere@example.com', status: 'Hoạt động', events: 89, users: 3200, created: '2024-02-01' },
    ]);

    const filteredPartners = partners.filter(partner => {
        const matchesSearch = partner.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            partner.email.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = filterStatus === 'all' || partner.status === filterStatus;
        return matchesSearch && matchesStatus;
    });

    const handleAddPartner = (partnerData) => {
        const newPartner = {
            id: partners.length + 1,
            name: partnerData.name,
            email: partnerData.email,
            status: 'Hoạt động',
            events: 0,
            users: 0,
            created: new Date().toISOString().split('T')[0],
        };

        setPartners(prev => [...prev, newPartner]);
    };

    const handleToggleStatus = (partnerId) => {
        setPartners(prev => prev.map(partner => 
            partner.id === partnerId 
                ? { ...partner, status: partner.status === 'Hoạt động' ? 'Tạm dừng' : 'Hoạt động' }
                : partner
        ));
    };

    return (
        <div className="manage-partners">
            <div className="page-header">
                <div>
                    <h2>Quản lý Partner</h2>
                    <p>Quản lý tất cả các partner (admin) trong hệ thống</p>
                </div>
                <button className="btn-primary" onClick={() => setIsModalOpen(true)}>
                    <i className="bi bi-plus-circle"></i>
                    Thêm Partner
                </button>
            </div>

            <div className="filters-bar">
                <div className="search-box">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Tìm kiếm theo tên hoặc email..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filter-group">
                    <label>Lọc theo trạng thái:</label>
                    <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                        <option value="all">Tất cả</option>
                        <option value="Hoạt động">Hoạt động</option>
                        <option value="Tạm dừng">Tạm dừng</option>
                    </select>
                </div>
            </div>

            <div className="stats-summary">
                <div className="stat-item">
                    <span className="stat-label">Tổng số Partner</span>
                    <span className="stat-value">{partners.length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Partner Hoạt động</span>
                    <span className="stat-value">{partners.filter(p => p.status === 'Hoạt động').length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Tổng số Event</span>
                    <span className="stat-value">{partners.reduce((sum, p) => sum + p.events, 0)}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">Tổng số User</span>
                    <span className="stat-value">{partners.reduce((sum, p) => sum + p.users, 0)}</span>
                </div>
            </div>

            <div className="table-card">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
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
                        {filteredPartners.length === 0 ? (
                            <tr>
                                <td colSpan={8} className="no-data">
                                    Không tìm thấy partner nào
                                </td>
                            </tr>
                        ) : (
                            filteredPartners.map((partner) => (
                                <tr key={partner.id}>
                                    <td>#{partner.id}</td>
                                    <td>
                                        <div className="partner-info">
                                            <div className="partner-avatar">
                                                {partner.name.charAt(0)}
                                            </div>
                                            <span>{partner.name}</span>
                                        </div>
                                    </td>
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
                                            <button className="btn-icon" title="Xem chi tiết">
                                                <i className="bi bi-eye"></i>
                                            </button>
                                            <button className="btn-icon" title="Chỉnh sửa">
                                                <i className="bi bi-pencil"></i>
                                            </button>
                                            <button 
                                                className="btn-icon" 
                                                title={partner.status === 'Hoạt động' ? 'Tạm dừng' : 'Kích hoạt'}
                                                onClick={() => handleToggleStatus(partner.id)}
                                            >
                                                <i className={`bi ${partner.status === 'Hoạt động' ? 'bi-pause-circle' : 'bi-play-circle'}`}></i>
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

            {/* Add Partner Modal - Simple version */}
            {isModalOpen && (
                <div className="modal-overlay" onClick={() => setIsModalOpen(false)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h3>Thêm Partner mới</h3>
                            <button className="modal-close" onClick={() => setIsModalOpen(false)}>
                                <i className="bi bi-x"></i>
                            </button>
                        </div>
                        <form onSubmit={(e) => {
                            e.preventDefault();
                            const formData = new FormData(e.target);
                            handleAddPartner({
                                name: formData.get('name'),
                                email: formData.get('email'),
                            });
                            setIsModalOpen(false);
                        }}>
                            <div className="form-group">
                                <label>Tên Partner</label>
                                <input type="text" name="name" required />
                            </div>
                            <div className="form-group">
                                <label>Email</label>
                                <input type="email" name="email" required />
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn-secondary" onClick={() => setIsModalOpen(false)}>
                                    Hủy
                                </button>
                                <button type="submit" className="btn-primary">
                                    Thêm Partner
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ManagePartners;






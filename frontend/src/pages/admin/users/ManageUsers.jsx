import React, { useState } from 'react';
import './ManageUsers.css';
import AddUserModal from './AddUserModal';

const ManageUsers = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [filterRole, setFilterRole] = useState('all');
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Hardcode users data
    const [users, setUsers] = useState([
        { id: 1, name: 'Nguyễn Văn A', email: 'nguyenvana@example.com', role: 'User', status: 'Active', joined: '2024-01-10', events: 5 },
        { id: 2, name: 'Trần Thị B', email: 'tranthib@example.com', role: 'User', status: 'Active', joined: '2024-01-12', events: 3 },
        { id: 3, name: 'Lê Văn C', email: 'levanc@example.com', role: 'User', status: 'Inactive', joined: '2024-01-14', events: 0 },
        { id: 4, name: 'Phạm Thị D', email: 'phamthid@example.com', role: 'User', status: 'Active', joined: '2024-01-15', events: 8 },
        { id: 5, name: 'Hoàng Văn E', email: 'hoangvane@example.com', role: 'Admin', status: 'Active', joined: '2024-01-16', events: 12 },
    ]);

    const filteredUsers = users.filter(user => {
        const matchesSearch = user.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            user.email.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesRole = filterRole === 'all' || user.role.toLowerCase() === filterRole.toLowerCase();
        return matchesSearch && matchesRole;
    });

    const handleAddUser = (userData) => {
        const newUser = {
            id: users.length + 1,
            name: userData.name,
            email: userData.email,
            role: userData.role,
            status: 'Active',
            joined: new Date().toISOString().split('T')[0],
            events: 0,
        };

        setUsers(prev => [...prev, newUser]);
    };

    const existingEmails = users.map(user => user.email);

    return (
        <div className="manage-users">
            <div className="page-header">
                <div>
                    <h2>Quản lý User</h2>
                    <p>Quản lý tất cả người dùng trong hệ thống</p>
                </div>
                <button className="btn-primary" onClick={() => setIsModalOpen(true)}>
                    <i className="bi bi-plus-circle"></i>
                    Thêm User
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
                    <label>Lọc theo vai trò:</label>
                    <select value={filterRole} onChange={(e) => setFilterRole(e.target.value)}>
                        <option value="all">Tất cả</option>
                        <option value="user">User</option>
                        <option value="admin">Admin</option>
                    </select>
                </div>
            </div>

            <div className="stats-summary">
                <div className="stat-item">
                    <span className="stat-label">Tổng số User</span>
                    <span className="stat-value">{users.length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">User Active</span>
                    <span className="stat-value">{users.filter(u => u.status === 'Active').length}</span>
                </div>
                <div className="stat-item">
                    <span className="stat-label">User Inactive</span>
                    <span className="stat-value">{users.filter(u => u.status === 'Inactive').length}</span>
                </div>
            </div>

            <div className="table-card">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên</th>
                            <th>Email</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Ngày tham gia</th>
                            <th>Số Event</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredUsers.length === 0 ? (
                            <tr>
                                <td colSpan={8} className="no-data">
                                    Không tìm thấy user nào
                                </td>
                            </tr>
                        ) : (
                            filteredUsers.map((user) => (
                                <tr key={user.id}>
                                    <td>#{user.id}</td>
                                    <td>
                                        <div className="user-info">
                                            <div className="user-avatar">
                                                {user.name.charAt(0)}
                                            </div>
                                            <span>{user.name}</span>
                                        </div>
                                    </td>
                                    <td>{user.email}</td>
                                    <td>
                                        <span className={`role-badge role-${user.role.toLowerCase()}`}>
                                            {user.role}
                                        </span>
                                    </td>
                                    <td>
                                        <span className={`status-badge status-${user.status.toLowerCase()}`}>
                                            {user.status}
                                        </span>
                                    </td>
                                    <td>{user.joined}</td>
                                    <td>{user.events}</td>
                                    <td>
                                        <div className="action-buttons">
                                            <button className="btn-icon" title="Xem chi tiết">
                                                <i className="bi bi-eye"></i>
                                            </button>
                                            <button className="btn-icon" title="Chỉnh sửa">
                                                <i className="bi bi-pencil"></i>
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

            {/* Add User Modal */}
            <AddUserModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onSubmit={handleAddUser}
                existingEmails={existingEmails}
            />
        </div>
    );
};

export default ManageUsers;


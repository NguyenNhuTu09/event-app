import React, { useState, useEffect } from 'react';
import { organizersAPI } from '../../../../service/organizers';
import './ManagePartners.css';

const ManagePartners = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [filterStatus, setFilterStatus] = useState('all');
    const [partners, setPartners] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [processingId, setProcessingId] = useState(null);

    // Fetch organizers from API
    useEffect(() => {
        fetchOrganizers();
    }, []);

    const fetchOrganizers = async () => {
        try {
            setLoading(true);
            setError('');
            const response = await organizersAPI.getAllOrganizers();
            
            // Normalize the response data to handle both isApproved and approved fields
            // Also handle both boolean and number (1/0) formats from database
            const normalizedPartners = (response || []).map(partner => {
                // Check isApproved first (from backend DTO), then approved (fallback)
                let approvedValue = false;
                if (partner.isApproved !== undefined && partner.isApproved !== null) {
                    // Handle boolean true/false or number 1/0
                    approvedValue = partner.isApproved === true || partner.isApproved === 1 || partner.isApproved === '1';
                } else if (partner.approved !== undefined && partner.approved !== null) {
                    // Fallback to approved field
                    approvedValue = partner.approved === true || partner.approved === 1 || partner.approved === '1';
                }
                
                return {
                    ...partner,
                    approved: approvedValue,
                    isApproved: approvedValue // Ensure both fields are set
                };
            });
            
            setPartners(normalizedPartners);
        } catch (err) {
            console.error('Error fetching organizers:', err);
            setError(err.message || 'Không thể tải danh sách đối tác. Vui lòng thử lại.')
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (organizerId) => {
        if (!window.confirm('Bạn có chắc chắn muốn duyệt đăng ký này? Email thông báo sẽ được gửi tự động đến người đăng ký.')) {
            return;
        }

        try {
            setProcessingId(organizerId);
            setError('');
            setSuccessMessage('');

            // Call API to approve organizer
            const response = await organizersAPI.approveOrganizer(organizerId);

            // Normalize the approved value from response
            const approvedValue = response.isApproved === true || response.isApproved === 1 ||
                response.approved === true || response.approved === 1;

            // Update local state immediately using response data
            setPartners(prevPartners =>
                prevPartners.map(partner => {
                    if (partner.organizerId === organizerId) {
                        return {
                            ...partner,
                            ...response, // Spread all response data
                            approved: approvedValue,
                            isApproved: approvedValue
                        };
                    }
                    return partner;
                })
            );

            setSuccessMessage('Đã duyệt đăng ký thành công! Email thông báo đã được gửi đến người đăng ký.');

            // Refresh list to get latest data from server (with a small delay to ensure DB is updated)
            setTimeout(async () => {
                await fetchOrganizers();
            }, 500);

            // Clear success message after 5 seconds
            setTimeout(() => setSuccessMessage(''), 5000);
        } catch (err) {
            console.error('Error approving organizer:', err);
            const errorMsg = err.message || 'Không thể duyệt đăng ký. Vui lòng thử lại.';
            setError(errorMsg);

            // If error, refresh list to ensure UI is in sync with server
            await fetchOrganizers();
        } finally {
            setProcessingId(null);
        }
    };

    const handleReject = async (organizerId) => {
        if (!window.confirm('Bạn có chắc chắn muốn từ chối đăng ký này? Hành động này không thể hoàn tác.')) {
            return;
        }

        try {
            setProcessingId(organizerId);
            setError('');
            setSuccessMessage('');

            // Call API to delete/reject organizer
            await organizersAPI.deleteOrganizer(organizerId);

            // Remove from local state immediately for better UX
            setPartners(prevPartners =>
                prevPartners.filter(partner => partner.organizerId !== organizerId)
            );

            setSuccessMessage('Đã từ chối đăng ký thành công!');

            // Refresh list to get latest data from server
            await fetchOrganizers();

            // Clear success message after 5 seconds
            setTimeout(() => setSuccessMessage(''), 5000);
        } catch (err) {
            console.error('Error rejecting organizer:', err);
            const errorMsg = err.message || 'Không thể từ chối đăng ký. Vui lòng thử lại.';
            setError(errorMsg);

            // If error, refresh list to ensure UI is in sync with server
            await fetchOrganizers();
        } finally {
            setProcessingId(null);
        }
    };

    const getStatusLabel = (approved) => {
        return approved ? 'Đã duyệt' : 'Chờ duyệt';
    };

    const getStatusClass = (approved) => {
        return approved ? 'status-approved' : 'status-pending';
    };

    const filteredPartners = partners.filter(partner => {
        const matchesSearch =
            (partner.name && partner.name.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (partner.contactEmail && partner.contactEmail.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (partner.username && partner.username.toLowerCase().includes(searchTerm.toLowerCase()));

        const matchesStatus =
            filterStatus === 'all' ||
            (filterStatus === 'approved' && partner.approved) ||
            (filterStatus === 'pending' && !partner.approved);

        return matchesSearch && matchesStatus;
    });

    const pendingCount = partners.filter(p => !p.approved).length;
    const approvedCount = partners.filter(p => p.approved).length;

    if (loading) {
        return (
            <div className="manage-partners">
                <div className="loading-container">
                    <div className="spinner"></div>
                    <p>Đang tải danh sách đối tác...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="manage-partners">
            <div className="page-header">
                <div>
                    <h2>Quản lý Đối tác (Organizers)</h2>
                    <p>Duyệt hoặc từ chối đăng ký làm nhà tổ chức sự kiện</p>
                </div>
            </div>

            {error && (
                <div className="alert alert-error">
                    <i className="bi bi-exclamation-circle"></i>
                    {error}
                </div>
            )}

            {successMessage && (
                <div className="alert alert-success">
                    <i className="bi bi-check-circle"></i>
                    {successMessage}
                </div>
            )}

            <div className="filters-bar">
                <div className="search-box">
                    <i className="bi bi-search"></i>
                    <input
                        type="text"
                        placeholder="Tìm kiếm theo tên, email hoặc username..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
                <div className="filter-group">
                    <label>Lọc theo trạng thái:</label>
                    <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
                        <option value="all">Tất cả</option>
                        <option value="pending">Chờ duyệt</option>
                        <option value="approved">Đã duyệt</option>
                    </select>
                </div>
            </div>

            <div className="stats-summary">
                <div className="stat-item">
                    <span className="stat-label">Tổng số đăng ký</span>
                    <span className="stat-value">{partners.length}</span>
                </div>
                <div className="stat-item stat-item-warning">
                    <span className="stat-label">Chờ duyệt</span>
                    <span className="stat-value">{pendingCount}</span>
                </div>
                <div className="stat-item stat-item-success">
                    <span className="stat-label">Đã duyệt</span>
                    <span className="stat-value">{approvedCount}</span>
                </div>
            </div>

            <div className="table-card">
                <table className="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên đối tác</th>
                            <th>Email liên hệ</th>
                            <th>Username</th>
                            <th>Số điện thoại</th>
                            <th>Trạng thái</th>
                            <th>Ngày đăng ký</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        {filteredPartners.length === 0 ? (
                            <tr>
                                <td colSpan={8} className="no-data">
                                    {partners.length === 0
                                        ? 'Chưa có đăng ký nào'
                                        : 'Không tìm thấy đối tác nào phù hợp'}
                                </td>
                            </tr>
                        ) : (
                            filteredPartners.map((partner) => (
                                <tr key={partner.organizerId}>
                                    <td>#{partner.organizerId}</td>
                                    <td>
                                        <div className="partner-info">
                                            <div className="partner-avatar">
                                                {partner.name ? partner.name.charAt(0).toUpperCase() : '?'}
                                            </div>
                                            <span>{partner.name || 'N/A'}</span>
                                        </div>
                                    </td>
                                    <td>{partner.contactEmail || 'N/A'}</td>
                                    <td>{partner.username || 'N/A'}</td>
                                    <td>{partner.contactPhoneNumber || 'N/A'}</td>
                                    <td>
                                        <span className={`status-badge ${getStatusClass(partner.approved)}`}>
                                            {getStatusLabel(partner.approved)}
                                        </span>
                                    </td>
                                    <td>
                                        {partner.userId ? `User ID: ${partner.userId}` : 'N/A'}
                                    </td>
                                    <td>
                                        <div className="action-buttons">
                                            {!partner.approved ? (
                                                <>
                                                    <button
                                                        className="btn-icon btn-success"
                                                        title="Duyệt đăng ký"
                                                        onClick={() => handleApprove(partner.organizerId)}
                                                        disabled={processingId === partner.organizerId}
                                                    >
                                                        {processingId === partner.organizerId ? (
                                                            <span className="spinner-small"></span>
                                                        ) : (
                                                            <i className="bi bi-check-circle"></i>
                                                        )}
                                                    </button>
                                                    <button
                                                        className="btn-icon btn-danger"
                                                        title="Từ chối đăng ký"
                                                        onClick={() => handleReject(partner.organizerId)}
                                                        disabled={processingId === partner.organizerId}
                                                    >
                                                        {processingId === partner.organizerId ? (
                                                            <span className="spinner-small"></span>
                                                        ) : (
                                                            <i className="bi bi-x-circle"></i>
                                                        )}
                                                    </button>
                                                </>
                                            ) : (
                                                <span className="text-muted">Đã duyệt</span>
                                            )}
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

export default ManagePartners;

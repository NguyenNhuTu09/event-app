import React, { useState, useEffect, useRef } from 'react';
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom';
import { path } from '../utils/constant';
import './AdminLayout.css';

const AdminLayout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [showNotifications, setShowNotifications] = useState(false);
    const notificationRef = useRef(null);
    const [notifications] = useState([
        {
            id: 1,
            partner: 'partner_vt_6',
            message: 'Yêu cầu hỗ trợ kỹ thuật: Hệ thống quản lý event không hoạt động đúng. Không thể tạo event mới...',
            time: '2 giờ trước',
        },
        {
            id: 2,
            partner: 'partner_hn_7',
            message: 'Yêu cầu hỗ trợ thanh toán: Giao dịch thanh toán phí event bị lỗi, không nhận được xác nhận...',
            time: '5 giờ trước',
        },
        {
            id: 3,
            partner: 'partner_hn_1',
            message: 'Báo cáo lỗi: Hình ảnh event không hiển thị đúng trên trang chi tiết. Cần kiểm tra và sửa lỗi.',
            time: '1 ngày trước',
        },
    ]);

    // Get user role from localStorage
    const getCurrentRole = () => {
        try {
            const adminUser = localStorage.getItem('adminUser');
            if (adminUser) {
                const user = JSON.parse(adminUser);
                return user.role || 'super-admin';
            }
        } catch (error) {
            console.error('Error parsing adminUser:', error);
        }
        return 'super-admin';
    };

    const currentRole = getCurrentRole();

    // All menu items
    const allMenuItems = [
        {
            icon: 'bi-speedometer2',
            label: 'Dashboard',
            path: currentRole === 'partner' ? path.PARTNER_DASHBOARD : path.SUPER_ADMIN_DASHBOARD,
            roles: ['super-admin', 'partner'],
        },
        {
            icon: 'bi-people',
            label: 'Quản lý User',
            path: path.SUPER_ADMIN_USERS,
            roles: ['super-admin'],
        },
        {
            icon: 'bi-building',
            label: 'Quản lý Partner',
            path: path.SUPER_ADMIN_PARTNERS,
            roles: ['super-admin'],
        },
        {
            icon: 'bi-calendar-event',
            label: 'Quản lý Event',
            path: currentRole === 'partner' ? path.PARTNER_EVENTS : path.SUPER_ADMIN_EVENTS,
            roles: ['super-admin', 'partner'],
        },
        {
            icon: 'bi-clipboard-check',
            label: 'Check-in/Check-out',
            path: path.PARTNER_CHECK_IN_OUT,
            roles: ['partner'],
        },
        {
            icon: 'bi-tags',
            label: 'Danh mục',
            path: path.SUPER_ADMIN_CATEGORIES,
            roles: ['super-admin'],
        },
        {
            icon: 'bi-graph-up',
            label: 'Báo cáo',
            path: path.SUPER_ADMIN_REPORTS,
            roles: ['super-admin'],
        },
        {
            icon: 'bi-gear',
            label: 'Cài đặt',
            path: path.SUPER_ADMIN_SETTINGS,
            roles: ['super-admin'],
        },
        {
            icon: 'bi-headset',
            label: 'Hỗ trợ',
            path: currentRole === 'partner' ? path.PARTNER_SUPPORT : path.SUPER_ADMIN_SUPPORT,
            roles: ['super-admin', 'partner'],
        },
    ];

    // Filter menu items based on role
    const menuItems = allMenuItems.filter(item => item.roles.includes(currentRole));

    const handleLogout = () => {
        // Xóa admin session (hardcode)
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminUser');
        if (currentRole === 'partner') {
            navigate(path.PARTNER_LOGIN);
        } else {
            navigate(path.SUPER_ADMIN_LOGIN);
        }
    };

    const isActive = (menuPath) => {
        return location.pathname === menuPath;
    };

    // Close notification dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (notificationRef.current && !notificationRef.current.contains(event.target)) {
                setShowNotifications(false);
            }
        };

        if (showNotifications) {
            document.addEventListener('mousedown', handleClickOutside);
        }

        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, [showNotifications]);

    return (
        <div className="admin-layout">
            {/* Sidebar */}
            <aside className={`admin-sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
                <div className="sidebar-header">
                    <h2 className="sidebar-logo">
                        <i className="bi bi-fire"></i>
                        {sidebarOpen && <span>EMS Admin</span>}
                    </h2>
                    <button
                        className="sidebar-toggle"
                        onClick={() => setSidebarOpen(!sidebarOpen)}
                    >
                        <i className={`bi ${sidebarOpen ? 'bi-chevron-left' : 'bi-chevron-right'}`}></i>
                    </button>
                </div>

                <nav className="sidebar-nav">
                    {menuItems.map((item, index) => (
                        <Link
                            key={index}
                            to={item.path}
                            className={`nav-item ${isActive(item.path) ? 'active' : ''}`}
                        >
                            <i className={item.icon}></i>
                            {sidebarOpen && <span>{item.label}</span>}
                        </Link>
                    ))}
                </nav>

                <div className="sidebar-footer">
                    <button className="logout-btn" onClick={handleLogout}>
                        <i className="bi bi-box-arrow-right"></i>
                        {sidebarOpen && <span>Đăng xuất</span>}
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="admin-main">
                {/* Top Bar */}
                <header className="admin-header">
                    <div className="header-left">
                        <button
                            className="mobile-menu-toggle"
                            onClick={() => setSidebarOpen(!sidebarOpen)}
                        >
                            <i className="bi bi-list"></i>
                        </button>
                        <h1 className="page-title">
                            {(() => {
                                const activeItem = menuItems.find(item => isActive(item.path));
                                if (activeItem?.label === 'Hỗ trợ') {
                                    return 'Hỗ trợ / Báo cáo vi phạm';
                                }
                                return activeItem?.label || 'Dashboard';
                            })()}
                        </h1>
                    </div>
                    <div className="header-right">
                        <div className="notification-wrapper" ref={notificationRef}>
                            <button
                                className="notification-btn"
                                onClick={() => setShowNotifications(!showNotifications)}
                            >
                                <i className="bi bi-bell"></i>
                                {notifications.length > 0 && (
                                    <span className="notification-badge">{notifications.length}</span>
                                )}
                            </button>
                            {showNotifications && (
                                <div className="notification-dropdown">
                                    <div className="notification-header">
                                        <h3>Thông báo mới</h3>
                                        <div className="notification-actions">
                                            <button onClick={() => setShowNotifications(false)}>Đánh dấu đã đọc</button>
                                            <button>Xem tất cả</button>
                                        </div>
                                    </div>
                                    <div className="notification-list">
                                        {notifications.map((notif) => (
                                            <div key={notif.id} className="notification-item">
                                                <div className="notification-icon">
                                                    <span>P</span>
                                                </div>
                                                <div className="notification-content">
                                                    <div className="notification-partner">{notif.partner}</div>
                                                    <div className="notification-message">{notif.message}</div>
                                                    <div className="notification-time">{notif.time}</div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                        <div className="admin-user-info">
                            <i className="bi bi-person-circle"></i>
                            <span>
                                {(() => {
                                    try {
                                        const adminUser = localStorage.getItem('adminUser');
                                        if (adminUser) {
                                            const user = JSON.parse(adminUser);
                                            return user.username || 'User';
                                        }
                                    } catch (error) {
                                        console.error('Error parsing adminUser:', error);
                                    }
                                    return 'User';
                                })()}
                            </span>
                            <span className="role-badge" style={{
                                marginLeft: '10px',
                                padding: '4px 8px',
                                background: currentRole === 'super-admin' ? '#dc2626' : currentRole === 'partner' ? '#10b981' : '#3b82f6',
                                borderRadius: '4px',
                                fontSize: '12px',
                                textTransform: 'uppercase'
                            }}>
                                {currentRole === 'super-admin' ? 'Super Admin' : 'Partner'}
                            </span>
                        </div>
                        <span className="greeting-text">Chào, admin!</span>
                    </div>
                </header>

                {/* Content Area */}
                <div className="admin-content">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};

export default AdminLayout;






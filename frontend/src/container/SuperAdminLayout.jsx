import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom';
import { path } from '../utils/constant';
import './AdminLayout.css';

const SuperAdminLayout = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [sidebarOpen, setSidebarOpen] = useState(true);

    const menuItems = [
        {
            icon: 'bi-speedometer2',
            label: 'Dashboard',
            path: path.SUPER_ADMIN_DASHBOARD,
        },
        {
            icon: 'bi-people',
            label: 'Quản lý Partner',
            path: path.SUPER_ADMIN_PARTNERS,
        },
        {
            icon: 'bi-headset',
            label: 'Hỗ trợ',
            path: path.SUPER_ADMIN_SUPPORT,
        },
    ];

    const handleLogout = () => {
        // Xóa admin session (hardcode)
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminUser');
        navigate(path.SUPER_ADMIN_LOGIN);
    };

    const isActive = (menuPath) => {
        return location.pathname === menuPath;
    };

    return (
        <div className="admin-layout">
            {/* Sidebar */}
            <aside className={`admin-sidebar ${sidebarOpen ? 'open' : 'closed'}`}>
                <div className="sidebar-header">
                    <h2 className="sidebar-logo">
                        <i className="bi bi-fire"></i>
                        {sidebarOpen && <span>EMS Super Admin</span>}
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
                            {menuItems.find(item => isActive(item.path))?.label || 'Dashboard'}
                        </h1>
                    </div>
                    <div className="header-right">
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
                                background: '#dc2626',
                                borderRadius: '4px',
                                fontSize: '12px',
                                textTransform: 'uppercase'
                            }}>
                                Super Admin
                            </span>
                        </div>
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

export default SuperAdminLayout;


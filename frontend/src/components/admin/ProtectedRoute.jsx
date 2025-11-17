import React from 'react';
import { Navigate } from 'react-router-dom';
import { path } from '../../utils/constant';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
    // Check if admin is logged in (hardcode)
    const adminToken = localStorage.getItem('adminToken');
    const adminUser = localStorage.getItem('adminUser');

    if (!adminToken || !adminUser) {
        // Redirect to appropriate login based on allowed roles
        if (allowedRoles.length > 0 && allowedRoles.includes('partner')) {
            return <Navigate to={path.PARTNER_LOGIN} replace />;
        }
        return <Navigate to={path.SUPER_ADMIN_LOGIN} replace />;
    }

    // Check role if allowedRoles is specified
    if (allowedRoles.length > 0) {
        try {
            const user = JSON.parse(adminUser);
            if (!allowedRoles.includes(user.role)) {
                // Redirect to appropriate dashboard based on role
                if (user.role === 'super-admin') {
                    return <Navigate to={path.SUPER_ADMIN_DASHBOARD} replace />;
                } else if (user.role === 'partner') {
                    return <Navigate to={path.PARTNER_DASHBOARD} replace />;
                }
            }
        } catch (error) {
            console.error('Error parsing adminUser:', error);
            return <Navigate to={path.SUPER_ADMIN_LOGIN} replace />;
        }
    }

    return <>{children}</>;
};

export default ProtectedRoute;






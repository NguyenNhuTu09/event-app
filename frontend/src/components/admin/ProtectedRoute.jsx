import React from 'react';
import { Navigate } from 'react-router-dom';
import { path } from '../../utils/constant';

const ProtectedRoute = ({ children }) => {
    // Check if admin is logged in (hardcode)
    const adminToken = localStorage.getItem('adminToken');
    const adminUser = localStorage.getItem('adminUser');

    if (!adminToken || !adminUser) {
        // Redirect to login if not authenticated
        return <Navigate to={path.ADMIN_LOGIN} replace />;
    }

    return <>{children}</>;
};

export default ProtectedRoute;


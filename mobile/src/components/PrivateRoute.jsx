// src/components/PrivateRoute.jsx
import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';

const PrivateRoute = () => {
    // Kiểm tra xem có token trong localStorage không
    const token = localStorage.getItem('token');

    // Nếu có token -> Cho phép vào (render Outlet)
    // Nếu không -> Đá về trang Login
    return token ? <Outlet /> : <Navigate to="/login" />;
};

export default PrivateRoute;
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { path } from './utils/constant';

import ClientLayout from './container/ClientLayout';
// import AdminLayout from './container/AdminLayout'; // Sẽ tạo sau

import HomePage from './pages/HomePage';
const SystemLoginPage = () => <div><h1>Trang Đăng Nhập Hệ Thống</h1></div>;
const AdminDashboard = () => <div><h1>Trang Quản Trị - Dashboard</h1></div>;

import 'bootstrap-icons/font/bootstrap-icons.css';

function App() {
  return (
    <Router>
      <Routes>

        <Route path={path.HOME} element={<ClientLayout />}>
          <Route index element={<HomePage />} />


        </Route>


        <Route path={path.LOGIN} element={<SystemLoginPage />} />

        <Route path={path.SYSTEM} /* element={<AdminLayout/>} */>
          <Route path={path.DASHBOARD} element={<AdminDashboard />} />
          {/* <Route path={path.MANAGE_EVENTS} element={<ManageEventsPage />} /> */}
        </Route>

        <Route path="*" element={<div><h1>404 - Page Not Found</h1></div>} />
      </Routes>
    </Router>
  );
}

export default App;
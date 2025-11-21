import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { path } from './utils/constant';

import ClientLayout from './container/ClientLayout';
import AdminLayout from './container/AdminLayout';
import ProtectedRoute from './components/admin/ProtectedRoute';

import HomePage from './pages/HomePage';
import SolutionsPage from './pages/SolutionsPage';
import ResourcesPage from './pages/ResourcesPage';
import SupportPage from './pages/SupportPage';
import CompanyPage from './pages/CompanyPage';
import ContactPage from './pages/ContactPage';
import EventsPage from './pages/EventsPage';
import OAuth2RedirectPage from './pages/OAuth2RedirectPage';

import AdminLogin from './pages/admin/login';
import AdminDashboard from './pages/admin/dashboard';
import ManageUsers from './pages/admin/users';
import ManageEvents from './pages/admin/events';
import ManageCategories from './pages/admin/categories';
import AdminReports from './pages/admin/reports';
import AdminSettings from './pages/admin/settings';
import AdminSupport from './pages/admin/support';

// Super Admin
import ManagePartners from './pages/admin/super-admin/partners';

// Partner
import PartnerLogin from './pages/admin/partner/login';
import PartnerDashboard from './pages/admin/partner/dashboard';
import PartnerEvents from './pages/admin/partner/events';
import PartnerSupport from './pages/admin/partner/support';
import PartnerCheckInOut from './pages/admin/partner/check-in-out/PartnerCheckInOut';
import 'bootstrap-icons/font/bootstrap-icons.css';

function App() {
  return (
    <Router>
      <Routes>

        <Route path={path.HOME} element={<ClientLayout />}>
          <Route index element={<HomePage />} />
          <Route path={path.SOLUTIONS} element={<SolutionsPage />} />
          <Route path={path.RESOURCES} element={<ResourcesPage />} />
          <Route path={path.SUPPORT} element={<SupportPage />} />
          <Route path={path.COMPANY} element={<CompanyPage />} />
          <Route path={path.CONTACT} element={<ContactPage />} />
          <Route path={path.EVENTS} element={<EventsPage />} />
        </Route>


        <Route path={path.OAUTH2_REDIRECT} element={<OAuth2RedirectPage />} />

        {/* Super Admin Login (Admin hệ thống) */}
        <Route path={path.SUPER_ADMIN_LOGIN} element={<AdminLogin />} />

        {/* Partner Login */}
        <Route path={path.PARTNER_LOGIN} element={<PartnerLogin />} />

        {/* Super Admin Routes (Admin hệ thống - quản lý tất cả) */}
        <Route
          path={path.SUPER_ADMIN_SYSTEM}
          element={
            <ProtectedRoute allowedRoles={['super-admin']}>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="users" element={<ManageUsers />} />
          <Route path="partners" element={<ManagePartners />} />
          <Route path="events" element={<ManageEvents />} />
          <Route path="categories" element={<ManageCategories />} />
          <Route path="reports" element={<AdminReports />} />
          <Route path="settings" element={<AdminSettings />} />
          <Route path="support" element={<AdminSupport />} />
          <Route index element={<Navigate to={path.SUPER_ADMIN_DASHBOARD} replace />} />
        </Route>

        {/* Partner Routes (Đối tác - admin của đối tác) */}
        <Route
          path="/partner"
          element={
            <ProtectedRoute allowedRoles={['partner']}>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<PartnerDashboard />} />
          <Route path="events" element={<PartnerEvents />} />
          <Route path="check-in-out" element={<PartnerCheckInOut />} />
          <Route path="support" element={<PartnerSupport />} />
          <Route index element={<Navigate to={path.PARTNER_DASHBOARD} replace />} />
        </Route>

        <Route path="*" element={<div><h1>404 - Page Not Found</h1></div>} />
      </Routes>
    </Router>
  );
}

export default App;
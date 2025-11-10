import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { path } from './utils/constant';

import ClientLayout from './container/ClientLayout';
import AdminLayout from './container/AdminLayout';
import ProtectedRoute from './components/admin/ProtectedRoute';

import HomePage from './pages/HomePage';
import IndustriesPage from './pages/IndustriesPage';
import SolutionsPage from './pages/SolutionsPage';
import ResourcesPage from './pages/ResourcesPage';
import SupportPage from './pages/SupportPage';
import CompanyPage from './pages/CompanyPage';
import ContactPage from './pages/ContactPage';
import OAuth2RedirectPage from './pages/OAuth2RedirectPage';

import AdminLogin from './pages/admin/login';
import AdminDashboard from './pages/admin/dashboard';
import ManageUsers from './pages/admin/users';
import ManageEvents from './pages/admin/events';
import ManageCategories from './pages/admin/categories';
import AdminReports from './pages/admin/reports';
import AdminSettings from './pages/admin/settings';

import 'bootstrap-icons/font/bootstrap-icons.css';

function App() {
  return (
    <Router>
      <Routes>

        <Route path={path.HOME} element={<ClientLayout />}>
          <Route index element={<HomePage />} />
          <Route path={path.INDUSTRIES} element={<IndustriesPage />} />
          <Route path={path.SOLUTIONS} element={<SolutionsPage />} />
          <Route path={path.RESOURCES} element={<ResourcesPage />} />
          <Route path={path.SUPPORT} element={<SupportPage />} />
          <Route path={path.COMPANY} element={<CompanyPage />} />
          <Route path={path.CONTACT} element={<ContactPage />} />
        </Route>


        <Route path={path.OAUTH2_REDIRECT} element={<OAuth2RedirectPage />} />

        {/* Admin Routes */}
        <Route path={path.ADMIN_LOGIN} element={<AdminLogin />} />

        <Route
          path={path.SYSTEM}
          element={
            <ProtectedRoute>
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="users" element={<ManageUsers />} />
          <Route path="events" element={<ManageEvents />} />
          <Route path="categories" element={<ManageCategories />} />
          <Route path="reports" element={<AdminReports />} />
          <Route path="settings" element={<AdminSettings />} />
          <Route index element={<Navigate to={path.ADMIN_DASHBOARD} replace />} />
        </Route>

        <Route path="*" element={<div><h1>404 - Page Not Found</h1></div>} />
      </Routes>
    </Router>
  );
}

export default App;
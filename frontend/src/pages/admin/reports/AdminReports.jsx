import React, { useState } from 'react';
import { Chart as ChartJS, registerables } from "chart.js";
import { Bar, Line, Pie, Doughnut } from 'react-chartjs-2';
import './AdminReports.css';

// Đăng ký tất cả các components của Chart.js
ChartJS.register(...registerables);

const AdminReports = () => {
    const [selectedPeriod, setSelectedPeriod] = useState('month');

    // Dữ liệu mẫu - có thể thay thế bằng API call
    const userStats = {
        total: 1250,
        active: 980,
        inactive: 270,
        newThisMonth: 45,
    };

    const eventStats = {
        total: 342,
        upcoming: 28,
        completed: 298,
        cancelled: 16,
    };

    // Dữ liệu cho biểu đồ cột - Users theo tháng
    const usersByMonthData = {
        labels: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'],
        datasets: [
            {
                label: 'Người dùng mới',
                data: [120, 190, 300, 250, 280, 320, 250, 280, 320, 250, 280, 320],
                backgroundColor: 'rgba(59, 130, 246, 0.8)',
                borderColor: 'rgba(59, 130, 246, 1)',
                borderWidth: 1,
            },
        ],
    };

    // Dữ liệu cho biểu đồ đường - Events theo tháng
    const eventsByMonthData = {
        labels: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6'],
        datasets: [
            {
                label: 'Sự kiện',
                data: [45, 52, 48, 61, 55, 58],
                borderColor: 'rgba(16, 185, 129, 1)',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                tension: 0.4,
                fill: true,
            },
        ],
    };

    // Dữ liệu cho biểu đồ tròn - Phân bổ vai trò
    const roleDistributionData = {
        labels: ['User', 'Admin'],
        datasets: [
            {
                data: [userStats.total - 15, 15], // Giả sử có 15 admin
                backgroundColor: [
                    'rgba(59, 130, 246, 0.8)',
                    'rgba(245, 158, 11, 0.8)',
                ],
                borderColor: [
                    'rgba(59, 130, 246, 1)',
                    'rgba(245, 158, 11, 1)',
                ],
                borderWidth: 2,
            },
        ],
    };

    // Dữ liệu cho biểu đồ Doughnut - Trạng thái Events
    const eventStatusData = {
        labels: ['Hoàn thành', 'Sắp tới', 'Đã hủy'],
        datasets: [
            {
                data: [eventStats.completed, eventStats.upcoming, eventStats.cancelled],
                backgroundColor: [
                    'rgba(16, 185, 129, 0.8)',
                    'rgba(59, 130, 246, 0.8)',
                    'rgba(239, 68, 68, 0.8)',
                ],
                borderColor: [
                    'rgba(16, 185, 129, 1)',
                    'rgba(59, 130, 246, 1)',
                    'rgba(239, 68, 68, 1)',
                ],
                borderWidth: 2,
            },
        ],
    };

    // Options cho các biểu đồ
    const barOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true,
                position: 'top',
            },
            title: {
                display: true,
                text: 'Người dùng mới theo tháng',
                font: {
                    size: 16,
                    weight: 'bold',
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
            },
        },
    };

    const lineOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true,
                position: 'top',
            },
            title: {
                display: true,
                text: 'Sự kiện theo tháng',
                font: {
                    size: 16,
                    weight: 'bold',
                },
            },
        },
        scales: {
            y: {
                beginAtZero: true,
            },
        },
    };

    const pieOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true,
                position: 'right',
            },
            title: {
                display: true,
                text: 'Phân bổ vai trò người dùng',
                font: {
                    size: 16,
                    weight: 'bold',
                },
            },
        },
    };

    const doughnutOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: true,
                position: 'right',
            },
            title: {
                display: true,
                text: 'Trạng thái sự kiện',
                font: {
                    size: 16,
                    weight: 'bold',
                },
            },
        },
    };

    return (
        <div className="admin-reports">
            <div className="page-header">
                <div>
                    <h2>Báo cáo</h2>
                    <p>Xem các báo cáo và thống kê hệ thống</p>
                </div>
                <div className="period-selector">
                    <select
                        value={selectedPeriod}
                        onChange={(e) => setSelectedPeriod(e.target.value)}
                        className="period-select"
                    >
                        <option value="week">Tuần này</option>
                        <option value="month">Tháng này</option>
                        <option value="quarter">Quý này</option>
                        <option value="year">Năm nay</option>
                    </select>
                </div>
            </div>

            {/* Stats Cards */}
            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon user-icon">
                        <i className="bi bi-people"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Tổng người dùng</h3>
                        <p className="stat-value">{userStats.total.toLocaleString()}</p>
                        <p className="stat-change positive">
                            <i className="bi bi-arrow-up"></i>
                            +{userStats.newThisMonth} tháng này
                        </p>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="stat-icon active-icon">
                        <i className="bi bi-person-check"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Người dùng hoạt động</h3>
                        <p className="stat-value">{userStats.active.toLocaleString()}</p>
                        <p className="stat-change positive">
                            {((userStats.active / userStats.total) * 100).toFixed(1)}% tổng số
                        </p>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="stat-icon event-icon">
                        <i className="bi bi-calendar-event"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Tổng sự kiện</h3>
                        <p className="stat-value">{eventStats.total.toLocaleString()}</p>
                        <p className="stat-change">
                            {eventStats.upcoming} sắp tới
                        </p>
                    </div>
                </div>

                <div className="stat-card">
                    <div className="stat-icon completed-icon">
                        <i className="bi bi-check-circle"></i>
                    </div>
                    <div className="stat-content">
                        <h3>Sự kiện hoàn thành</h3>
                        <p className="stat-value">{eventStats.completed.toLocaleString()}</p>
                        <p className="stat-change positive">
                            {((eventStats.completed / eventStats.total) * 100).toFixed(1)}% tổng số
                        </p>
                    </div>
                </div>
            </div>

            {/* Charts Grid */}
            <div className="charts-grid">
                <div className="chart-card">
                    <div className="chart-container">
                        <Bar data={usersByMonthData} options={barOptions} />
                    </div>
                </div>

                <div className="chart-card">
                    <div className="chart-container">
                        <Line data={eventsByMonthData} options={lineOptions} />
                    </div>
                </div>

                <div className="chart-card">
                    <div className="chart-container">
                        <Pie data={roleDistributionData} options={pieOptions} />
                    </div>
                </div>

                <div className="chart-card">
                    <div className="chart-container">
                        <Doughnut data={eventStatusData} options={doughnutOptions} />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminReports;








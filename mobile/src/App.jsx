import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import OrganizerCheckIn from './pages/OrganizerCheckIn';
import ActivityQRGenerator from './pages/ActivityQRGenerator';
import UserActivityCheckIn from './pages/UserActivityCheckIn';
import LoginPage from './pages/LoginPage';
import PrivateRoute from './components/PrivateRoute';

// Component Menu điều hướng
const Navigation = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role'); // Giả sử lưu 'ORGANIZER' hoặc 'USER'

    const handleLogout = () => {
        localStorage.clear();
        navigate('/login');
    };

    if (!token) return null;

    return (
        <nav className="navbar">
            <div style={{ display: 'flex', alignItems: 'center' }}>
                <Link to="/" className="nav-brand">Webie</Link>
                
                <div className="nav-links">
                    {/* MENU CHO BAN TỔ CHỨC */}
                    {role === 'ORGANIZER' && (
                        <>
                             <Link className="nav-item" to="/organizer/checkin">Check-in Sự kiện</Link>
                             <Link className="nav-item" to="/organizer/activity-qr/1">Tạo QR Hoạt động</Link>
                        </>
                    )}
                    
                    {/* MENU CHO NGƯỜI DÙNG THƯỜNG (Ẩn nếu là Organizer) */}
                    {role === 'USER' && (
                        <Link className="nav-item" to="/user/checkin">Quét Hoạt động</Link>
                    )}
                </div>
            </div>

            <div style={{display: 'flex', alignItems: 'center', gap: '15px'}}>
                <span style={{color: '#888', fontSize: '0.9rem'}}>Vai trò: {role}</span>
                <button onClick={handleLogout} className="btn-logout">
                    Đăng xuất
                </button>
            </div>
        </nav>
    );
};

// ... Phần Routes và App giữ nguyên ...
// Nhớ export default App;
function App() {
  return (
    <Router>
      <div className="App">
        <Navigation />
        <div className="content-container"> 
            <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<PrivateRoute />}>
                <Route path="/organizer/checkin" element={<OrganizerCheckIn />} />
                <Route path="/organizer/activity-qr/:activityId" element={<ActivityQRGenerator />} />
                <Route path="/user/checkin" element={<UserActivityCheckIn />} />
            </Route>
            <Route path="*" element={<LoginPage />} />
            </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
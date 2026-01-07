import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import OrganizerCheckIn from './pages/OrganizerCheckIn';
import ActivityQRGenerator from './pages/ActivityQRGenerator';
import UserActivityCheckIn from './pages/UserActivityCheckIn';
import LoginPage from './pages/LoginPage'; // Giả sử bạn đã có trang Login
import PrivateRoute from './components/PrivateRoute';

// Component Menu điều hướng (Đã được Style lại)
const Navigation = () => {
    const navigate = useNavigate();
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
        navigate('/login');
    };

    if (!token) return null; // Chưa login thì không hiện menu

    return (
        <nav className="navbar">
            <div style={{ display: 'flex', alignItems: 'center' }}>
                {/* Logo giả lập */}
                <Link to="/" className="nav-brand">Webie</Link>
                
                <div className="nav-links">
                    {/* Menu cho Organizer */}
                    {role === 'ORGANIZER' && (
                        <>
                             <Link className="nav-item" to="/organizer/checkin">Check-in Sự kiện</Link>
                             <Link className="nav-item" to="/organizer/activity-qr/1">Tạo QR Hoạt động</Link>
                        </>
                    )}
                    
                    {/* Menu cho User */}
                    <Link className="nav-item" to="/user/checkin">Quét Hoạt động</Link>
                </div>
            </div>

            <button onClick={handleLogout} className="btn-logout">
                Đăng xuất
            </button>
        </nav>
    );
};

function App() {
  return (
    <Router>
      <div className="App">
        <Navigation />

        <div className="content-container"> 
        {/* Container này dùng để đảm bảo nội dung không bị dính sát lề nếu cần, 
            nhưng với CSS body margin:0 ở bước trước thì không bắt buộc */}
            
            <Routes>
            {/* Route công khai */}
            <Route path="/login" element={<LoginPage />} />

            {/* Route cần đăng nhập mới vào được */}
            <Route element={<PrivateRoute />}>
                <Route path="/organizer/checkin" element={<OrganizerCheckIn />} />
                <Route path="/organizer/activity-qr/:activityId" element={<ActivityQRGenerator />} />
                <Route path="/user/checkin" element={<UserActivityCheckIn />} />
            </Route>

            {/* Mặc định vào login nếu sai đường dẫn */}
            <Route path="*" element={<LoginPage />} />
            </Routes>
        </div>
      </div>
    </Router>
  );
}

export default App;
// src/App.jsx
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import OrganizerCheckIn from './pages/OrganizerCheckIn';
import ActivityQRGenerator from './pages/ActivityQRGenerator';
import UserActivityCheckIn from './pages/UserActivityCheckIn';
import LoginPage from './pages/LoginPage';
import PrivateRoute from './components/PrivateRoute';

// Component Menu điều hướng đơn giản
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
        <nav style={{ padding: 10, background: '#333', color: 'white', display: 'flex', justifyContent: 'space-between' }}>
            <div>
                {/* Chỉ hiện link dựa trên Role (Logic đơn giản) */}
                {role === 'ORGANIZER' && (
                    <>
                         <Link style={linkStyle} to="/organizer/checkin">Org: Quét Vé</Link>
                         <Link style={linkStyle} to="/organizer/activity-qr/1">Org: Mã QR HĐ</Link>
                    </>
                )}
                <Link style={linkStyle} to="/user/checkin">User: Quét HĐ</Link>
            </div>
            <button onClick={handleLogout} style={{background: 'red', color: 'white', border: 'none', cursor: 'pointer'}}>
                Đăng xuất
            </button>
        </nav>
    );
};

const linkStyle = { color: 'white', marginRight: 15, textDecoration: 'none' };

function App() {
  return (
    <Router>
      <div className="App">
        <Navigation />

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
    </Router>
  );
}

export default App;
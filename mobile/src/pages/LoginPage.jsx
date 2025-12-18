// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosClient from '../api/axiosClient';

const LoginPage = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await axiosClient.post('/auth/signin', { 
                email: email, 
                password: password 
            });

            const token = response.data.accessToken || response.data.token; 
            const role = response.data.role || (response.data.roles ? response.data.roles[0] : '');

            if (token) {
                localStorage.setItem('token', token);
                localStorage.setItem('role', role); // Lưu role để điều hướng giao diện

                if (role === 'ORGANIZER' || role === 'SADMIN') {
                    navigate('/organizer/checkin');
                } else {
                    navigate('/user/checkin');
                }
            } else {
                setError('Phản hồi từ server không chứa Token.');
            }

        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Đăng nhập thất bại. Kiểm tra lại email/pass.');
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h2>Đăng nhập Hệ thống</h2>
                <form onSubmit={handleLogin} style={styles.form}>
                    <div style={styles.inputGroup}>
                        <label>Email:</label>
                        <input 
                            type="email" 
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required 
                            style={styles.input}
                        />
                    </div>
                    <div style={styles.inputGroup}>
                        <label>Mật khẩu:</label>
                        <input 
                            type="password" 
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required 
                            style={styles.input}
                        />
                    </div>
                    {error && <p style={{color: 'red'}}>{error}</p>}
                    <button type="submit" style={styles.button}>Đăng nhập</button>
                </form>
            </div>
        </div>
    );
};

// CSS viết nhanh trong JS
const styles = {
    container: { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#f0f2f5' },
    card: { background: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', width: '300px' },
    form: { display: 'flex', flexDirection: 'column' },
    inputGroup: { marginBottom: '15px' },
    input: { width: '100%', padding: '8px', marginTop: '5px', boxSizing: 'border-box' },
    button: { padding: '10px', background: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' }
};

export default LoginPage;
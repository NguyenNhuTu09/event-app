import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import authAPI from '../service/api';

const OAuth2RedirectPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const { loginWithGoogleToken } = useAuth();

    useEffect(() => {
        const processOAuth2 = async () => {
            const oneTimeCode = searchParams.get('token');
            const error = searchParams.get('error');

            if (error) {
                const errorMessage = searchParams.get('message') || 'Đăng nhập Google thất bại';
                console.error('OAuth2 error:', errorMessage);
                // Redirect về trang chủ với thông báo lỗi
                navigate('/', {
                    state: {
                        error: errorMessage
                    }
                });
                return;
            }

            if (!oneTimeCode) {
                // Không có token
                navigate('/', {
                    state: {
                        error: 'Thiếu thông tin đăng nhập'
                    }
                });
                return;
            }

            try {
                // Bước 1: Đổi oneTimeCode lấy JWT token
                const jwtResponse = await authAPI.exchangeToken(oneTimeCode);
                
                if (!jwtResponse || !jwtResponse.token) {
                    throw new Error('Không nhận được JWT token từ server');
                }

                // Bước 2: Lưu token và user info từ response
                const userData = {
                    id: jwtResponse.id,
                    username: jwtResponse.username,
                    email: jwtResponse.email,
                };

                localStorage.setItem('token', jwtResponse.token);
                localStorage.setItem('user', JSON.stringify(userData));

                // Bước 3: Cập nhật AuthContext (không cần gọi API getCurrentUser nữa vì đã có user info)
                const result = await loginWithGoogleToken(jwtResponse.token);
                
                if (result && result.success) {
                    // Redirect về trang chủ sau khi đăng nhập thành công
                    navigate('/', {
                        replace: true,
                        state: {
                            message: 'Đăng nhập Google thành công!'
                        }
                    });
                } else {
                    navigate('/', {
                        state: {
                            error: (result && result.message) || 'Không thể xử lý đăng nhập'
                        }
                    });
                }
            } catch (err) {
                console.error('Error processing OAuth2:', err);
                // Xóa token nếu có lỗi
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                navigate('/', {
                    state: {
                        error: err.message || 'Lỗi xử lý đăng nhập Google'
                    }
                });
            }
        };

        processOAuth2();
    }, [searchParams, navigate, loginWithGoogleToken]);

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
            flexDirection: 'column',
            gap: '20px',
            fontFamily: 'Lato, sans-serif'
        }}>
            <div style={{
                width: '40px',
                height: '40px',
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #007bff',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite'
            }}></div>
            <style>{`
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
            `}</style>
            <p style={{ fontSize: '16px', color: '#333' }}>Đang xử lý đăng nhập Google...</p>
        </div>
    );
};

export default OAuth2RedirectPage;


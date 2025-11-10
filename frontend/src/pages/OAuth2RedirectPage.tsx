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
            const token = searchParams.get('token');
            const error = searchParams.get('error');
            const success = searchParams.get('success');

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

            if (token && success === 'true') {
                try {
                    // Xử lý token từ Google OAuth2
                    // @ts-ignore - Type definition issue
                    const result = await loginWithGoogleToken(token);
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
                                error: (result && result.message) || 'Không thể xử lý token đăng nhập'
                            }
                        });
                    }
                } catch (err) {
                    console.error('Error processing OAuth2 token:', err);
                    navigate('/', {
                        state: {
                            error: 'Lỗi xử lý đăng nhập Google'
                        }
                    });
                }
            } else {
                // Không có token hoặc success flag
                navigate('/', {
                    state: {
                        error: 'Thiếu thông tin đăng nhập'
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


// pages/UserActivityCheckIn.js
import React, { useState, useCallback } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';

const UserActivityCheckIn = () => {
    const [status, setStatus] = useState('idle'); // idle, success, error
    const [message, setMessage] = useState('');

    const handleScan = useCallback((decodedText) => {
        if (status === 'success') return; // Nếu đã thành công thì dừng quét cho đến khi user bấm nút

        axiosClient.post('/checkin/activity', { activityQrCode: decodedText })
            .then(response => {
                setStatus('success');
                setMessage(response.data || "Điểm danh hoạt động thành công!");
            })
            .catch(error => {
                // Với user, nếu quét sai mã (ví dụ quét nhầm mã wifi), ta chỉ hiện toast nhỏ hoặc log, 
                // không nhất thiết phải chặn màn hình như Organizer
                setStatus('error');
                setMessage(error.response?.data?.message || "Mã QR không hợp lệ");
                setTimeout(() => {
                    setStatus('idle'); // Tự reset lỗi sau 2s để quét lại
                    setMessage('');
                }, 2000);
            });
    }, [status]);

    return (
        <div style={{ 
            minHeight: '100vh', 
            background: 'var(--bg-color)', 
            padding: '20px',
            display: 'flex', flexDirection: 'column', alignItems: 'center' 
        }}>
            <h2 style={{ marginBottom: '10px' }}>ĐIỂM DANH HOẠT ĐỘNG</h2>
            <p style={{ color: 'var(--text-secondary)', marginBottom: '30px', textAlign: 'center' }}>
                Quét mã QR tại khu vực hoạt động để ghi nhận tham gia
            </p>

            {status !== 'success' ? (
                <>
                    <QRScanner onScanSuccess={handleScan} />
                    {status === 'error' && (
                        <div style={{ 
                            marginTop: '20px', color: '#ff4d4f', background: 'rgba(255, 77, 79, 0.1)', 
                            padding: '10px 20px', borderRadius: '8px' 
                        }}>
                            ⚠️ {message}
                        </div>
                    )}
                </>
            ) : (
                // Màn hình thành công
                <div style={{ 
                    textAlign: 'center', marginTop: '50px', 
                    background: 'var(--glass-bg)', padding: '40px', 
                    borderRadius: '20px', border: '1px solid var(--gold-primary)',
                    maxWidth: '400px'
                }}>
                    <div style={{ fontSize: '80px', marginBottom: '20px' }}>🎉</div>
                    <h3 style={{ color: 'var(--gold-primary)', marginBottom: '15px' }}>CHECK-IN THÀNH CÔNG</h3>
                    <p style={{ color: 'white', marginBottom: '30px' }}>{message}</p>
                    <button 
                        onClick={() => { setStatus('idle'); setMessage(''); }} 
                        className="btn-gold"
                    >
                        Quét hoạt động khác
                    </button>
                </div>
            )}
        </div>
    );
};

export default UserActivityCheckIn;
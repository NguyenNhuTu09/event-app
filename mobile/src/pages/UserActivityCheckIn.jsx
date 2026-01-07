import React, { useState, useCallback } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';

const UserActivityCheckIn = () => {
    const [status, setStatus] = useState('idle'); 
    const [message, setMessage] = useState('');
    
    // State điều khiển camera
    const [isScanning, setIsScanning] = useState(true);

    const handleScan = useCallback((decodedText) => {
        // Camera đã tự dừng, giờ gọi API
        setIsScanning(false);
        setStatus('processing');

        axiosClient.post('/checkin/activity', { activityQrCode: decodedText })
            .then(response => {
                setStatus('success');
                setMessage(response.data || "Điểm danh thành công!");
            })
            .catch(error => {
                setStatus('error');
                setMessage(error.response?.data?.message || "Mã QR không hợp lệ");
            });
    }, []);

    const handleRetry = () => {
        setStatus('idle');
        setMessage('');
        setIsScanning(true); // Resume camera
    };

    return (
        <div style={{ minHeight: '100vh', background: 'var(--bg-color)', padding: '20px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h2 style={{ marginBottom: '10px' }}>ĐIỂM DANH HOẠT ĐỘNG</h2>

            {/* Chỉ ẩn camera khi thành công để hiển thị thông báo to đẹp, 
                còn khi lỗi thì camera vẫn hiện nhưng ở trạng thái Pause (đông cứng) */}
            {status !== 'success' ? (
                <>
                    <QRScanner onScanSuccess={handleScan} isScanning={isScanning} />
                    
                    {status === 'error' && (
                        <div style={{ marginTop: '20px', textAlign: 'center' }}>
                            <p style={{ color: '#ff4d4f', fontWeight: 'bold' }}>⚠️ {message}</p>
                            <button onClick={handleRetry} className="btn-gold" style={{marginTop: 10}}>
                                Quét lại
                            </button>
                        </div>
                    )}
                     {status === 'processing' && (
                        <p style={{color: 'var(--gold-primary)', marginTop: 20}}>Đang kiểm tra...</p>
                    )}
                </>
            ) : (
                <div style={{ textAlign: 'center', marginTop: '50px', background: 'var(--glass-bg)', padding: '40px', borderRadius: '20px', border: '1px solid var(--gold-primary)' }}>
                    <div style={{ fontSize: '80px', marginBottom: '20px' }}>🎉</div>
                    <h3 style={{ color: 'var(--gold-primary)' }}>CHECK-IN THÀNH CÔNG</h3>
                    <p style={{ color: 'white', marginBottom: '30px' }}>{message}</p>
                    <button onClick={handleRetry} className="btn-gold">
                        Quét hoạt động khác
                    </button>
                </div>
            )}
        </div>
    );
};

export default UserActivityCheckIn;
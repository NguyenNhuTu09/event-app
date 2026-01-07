import React, { useState, useCallback } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';

const OrganizerCheckIn = () => {
    const [checkInData, setCheckInData] = useState(null);
    const [message, setMessage] = useState('');
    const [status, setStatus] = useState('idle'); // idle, processing, success, error
    
    // State mới để điều khiển camera
    const [isScanning, setIsScanning] = useState(true);

    const handleScan = useCallback((decodedText) => {
        // Lúc này camera đã tự Pause ở bên trong QRScanner rồi
        // Ta chỉ cần cập nhật state UI
        setIsScanning(false); // Báo hiệu UI đang bận, dừng quét
        setStatus('processing');
        
        axiosClient.post('/checkin/event', { ticketCode: decodedText })
            .then(response => {
                setCheckInData(response.data);
                setStatus('success');
                setMessage('CHECK-IN THÀNH CÔNG');
            })
            .catch(error => {
                setStatus('error');
                setMessage(error.response?.data?.message || "Vé không hợp lệ");
            });
    }, []);

    const resetScan = () => {
        setCheckInData(null);
        setStatus('idle');
        setMessage('');
        setIsScanning(true); // <--- QUAN TRỌNG: Bấm nút này sẽ Resume camera
    };

    return (
        <div style={{ minHeight: '100vh', padding: '20px', background: 'var(--bg-color)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                <h2 style={{color: 'var(--gold-primary)'}}>CỔNG CHECK-IN</h2>
            </div>

            {/* Truyền prop isScanning xuống */}
            <QRScanner 
                onScanSuccess={handleScan} 
                isScanning={isScanning} 
            />

            {/* Modal Kết quả */}
            {status !== 'idle' && (
                <div style={styles.resultOverlay}>
                    <div style={styles.resultCard}>
                        <div style={{ textAlign: 'center', marginBottom: 15 }}>
                            {status === 'processing' && <span style={{fontSize: 40}}>⏳</span>}
                            {status === 'success' && <span style={{fontSize: 40}}>✅</span>}
                            {status === 'error' && <span style={{fontSize: 40}}>❌</span>}
                        </div>

                        <h3 style={{ textAlign: 'center', color: status === 'error' ? '#ff4d4f' : 'var(--gold-primary)' }}>
                            {status === 'processing' ? 'Đang xử lý...' : message}
                        </h3>

                        {/* ... Phần hiển thị thông tin vé giữ nguyên ... */}
                        {checkInData && (
                            <div style={{ marginTop: 20 }}>
                                <p style={{color: 'white'}}>Khách: <strong>{checkInData.attendee.username}</strong></p>
                                <p style={{color: '#aaa'}}>Vé: {checkInData.attendee.ticketCode}</p>
                            </div>
                        )}

                        <div style={{ marginTop: 25, textAlign: 'center' }}>
                            {/* Nút này sẽ kích hoạt lại Camera */}
                            {(status === 'success' || status === 'error') && (
                                <button onClick={resetScan} className="btn-gold">
                                    {status === 'success' ? 'Quét khách tiếp theo' : 'Thử lại'}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};
// ... styles giữ nguyên
export default OrganizerCheckIn;
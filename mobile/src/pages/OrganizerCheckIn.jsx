// pages/OrganizerCheckIn.js
import React, { useState, useCallback } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';
// Nhớ import file CSS nếu chưa import global

const OrganizerCheckIn = () => {
    const [checkInData, setCheckInData] = useState(null);
    const [message, setMessage] = useState('');
    const [status, setStatus] = useState('idle'); // idle, processing, success, error

    // Hàm gọi API
    const handleScan = useCallback((decodedText) => {
        if (status === 'processing' || status === 'success') return; // Chặn quét liên tục khi đang xử lý

        setStatus('processing');
        
        axiosClient.post('/checkin/event', { ticketCode: decodedText })
            .then(response => {
                setCheckInData(response.data);
                setStatus('success');
                setMessage('CHECK-IN THÀNH CÔNG');
                // Tự động reset sau 5 giây nếu muốn, hoặc để user bấm nút
            })
            .catch(error => {
                setStatus('error');
                setMessage(error.response?.data?.message || "Vé không hợp lệ");
                setTimeout(() => setStatus('idle'), 3000); // Tự động cho phép quét lại sau 3s nếu lỗi
            });
    }, [status]);

    const resetScan = () => {
        setCheckInData(null);
        setStatus('idle');
        setMessage('');
    };

    return (
        <div style={{ minHeight: '100vh', padding: '20px', background: 'var(--bg-color)', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                <img src="/logo-webie.png" alt="Webie" style={{ height: '40px', marginBottom: '10px' }} /> {/* Thay logo của bạn */}
                <h2>CỔNG CHECK-IN SỰ KIỆN</h2>
                <p style={{ color: 'var(--text-secondary)' }}>Quét vé tham dự của khách mời</p>
            </div>

            {/* Khu vực Camera */}
            <QRScanner onScanSuccess={handleScan} />

            {/* Kết quả Check-in (Hiện lên như Modal/Card) */}
            {status !== 'idle' && (
                <div style={styles.resultOverlay}>
                    <div style={styles.resultCard}>
                        <div style={{ textAlign: 'center', marginBottom: 15 }}>
                            {status === 'processing' && <span style={{fontSize: 40}}>⏳</span>}
                            {status === 'success' && <span style={{fontSize: 40}}>✅</span>}
                            {status === 'error' && <span style={{fontSize: 40}}>❌</span>}
                        </div>

                        <h3 style={{ textAlign: 'center', color: status === 'error' ? '#ff4d4f' : 'var(--gold-primary)' }}>
                            {status === 'processing' ? 'Đang kiểm tra vé...' : message}
                        </h3>

                        {checkInData && (
                            <div style={{ marginTop: 20 }}>
                                <div style={styles.infoRow}>
                                    <span style={styles.label}>Họ tên:</span>
                                    <span style={styles.value}>{checkInData.attendee.username}</span>
                                </div>
                                <div style={styles.infoRow}>
                                    <span style={styles.label}>Email:</span>
                                    <span style={styles.value}>{checkInData.attendee.email}</span>
                                </div>
                                <div style={styles.infoRow}>
                                    <span style={styles.label}>Loại vé:</span>
                                    <span style={styles.valueTag}>{checkInData.attendee.ticketCode}</span>
                                </div>
                                <hr style={{ borderColor: 'rgba(255,255,255,0.1)', margin: '15px 0' }} />
                                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                                    Sự kiện: {checkInData.event.eventName}
                                </p>
                            </div>
                        )}

                        <div style={{ marginTop: 25, textAlign: 'center' }}>
                            {(status === 'success' || status === 'error') && (
                                <button onClick={resetScan} className="btn-gold">
                                    Tiếp tục quét vé khác
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

const styles = {
    resultOverlay: {
        position: 'fixed', bottom: 0, left: 0, right: 0,
        background: 'rgba(0,0,0,0.85)',
        padding: '20px',
        borderTopLeftRadius: '20px',
        borderTopRightRadius: '20px',
        zIndex: 100,
        animation: 'slideUp 0.3s ease-out',
        display: 'flex', justifyContent: 'center'
    },
    resultCard: {
        width: '100%', maxWidth: '500px',
        background: '#1a1a1a',
        borderRadius: '15px',
        padding: '25px',
        border: '1px solid var(--gold-primary)',
        boxShadow: '0 -5px 20px rgba(212, 175, 55, 0.2)'
    },
    infoRow: {
        display: 'flex', justifyContent: 'space-between', marginBottom: '10px', alignItems: 'center'
    },
    label: {
        color: 'var(--text-secondary)', fontSize: '0.9rem'
    },
    value: {
        color: 'white', fontWeight: '600', fontSize: '1.1rem'
    },
    valueTag: {
        background: 'rgba(212, 175, 55, 0.2)',
        color: 'var(--gold-primary)',
        padding: '4px 8px', borderRadius: '4px',
        fontFamily: 'monospace'
    }
};

export default OrganizerCheckIn;
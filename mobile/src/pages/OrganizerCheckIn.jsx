import React, { useState, useCallback } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';

const OrganizerCheckIn = () => {
    const [checkInData, setCheckInData] = useState(null);
    const [message, setMessage] = useState('');
    const [status, setStatus] = useState('idle'); 
    
    // State điều khiển: true = Hiện camera, false = Hiện nút "Bắt đầu"
    const [isCameraActive, setIsCameraActive] = useState(false);
    // State điều khiển quét liên tục
    const [isScanning, setIsScanning] = useState(true);

    // Xử lý khi quét được mã
    const handleScan = useCallback((decodedText) => {
        if (!decodedText) return;

        setIsScanning(false); // Khóa quét
        setStatus('processing');
        
        axiosClient.post('/checkin/event', { ticketCode: decodedText })
            .then(response => {
                setCheckInData(response.data);
                setStatus('success');
                setMessage('CHECK-IN THÀNH CÔNG');
            })
            .catch(error => {
                setStatus('error');
                setMessage(error.response?.data?.message || "Vé không hợp lệ/Lỗi hệ thống");
                setCheckInData(null);
            });
    }, []);

    // Nút "Quét tiếp" trong Modal
    const handleNextScan = () => {
        setCheckInData(null);
        setStatus('idle');
        setMessage('');
        setIsScanning(true); // Mở khóa để quét tiếp
    };

    // Nút "Dừng quét" để quay về màn hình chính
    const handleStopScanning = () => {
        setIsCameraActive(false);
        setCheckInData(null);
        setStatus('idle');
    };

    // --- Render phần thông tin Lịch trình (Agenda) ---
    const renderAgenda = () => {
        if (!checkInData?.agenda || checkInData.agenda.length === 0) {
            return <p style={{color: '#999', fontStyle: 'italic'}}>Chưa có hoạt động nào.</p>;
        }
        return (
            <div style={{ marginTop: 15, textAlign: 'left', maxHeight: '200px', overflowY: 'auto' }}>
                <h4 style={{ color: '#d4af37', borderBottom: '1px solid #444', paddingBottom: 5, marginBottom: 10 }}>
                    📅 Lịch trình dành cho khách
                </h4>
                {checkInData.agenda.map((act, index) => (
                    <div key={index} style={{ 
                        background: '#333', padding: '10px', marginBottom: '8px', borderRadius: '6px', borderLeft: '3px solid #d4af37' 
                    }}>
                        <div style={{ color: '#fff', fontWeight: 'bold' }}>{act.activityName}</div>
                        <div style={{ fontSize: '0.85rem', color: '#aaa' }}>
                            ⏰ {new Date(act.startTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - 
                            📍 {act.roomOrVenue || "Chưa cập nhật"}
                        </div>
                    </div>
                ))}
            </div>
        );
    };

    return (
        <div style={{ minHeight: '100vh', padding: '20px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            
            {/* 1. MÀN HÌNH CHỜ: Chỉ hiện khi Camera chưa bật */}
            {!isCameraActive && (
                <div style={{ textAlign: 'center', marginTop: '50px' }}>
                    <h1 style={{ color: '#d4af37', fontSize: '2.5rem', marginBottom: '10px' }}>CỔNG CHECK-IN</h1>
                    <p style={{ color: '#aaa', marginBottom: '40px' }}>Hệ thống quản lý sự kiện Webie</p>
                    
                    <button 
                        className="btn-gold" 
                        style={{ padding: '15px 40px', fontSize: '1.2rem' }}
                        onClick={() => {
                            setIsCameraActive(true);
                            setIsScanning(true);
                        }}
                    >
                        📸 Bắt đầu Quét vé
                    </button>
                </div>
            )}

            {/* 2. MÀN HÌNH CAMERA: Hiện khi bấm nút Bắt đầu */}
            {isCameraActive && (
                <>
                    <div style={{ width: '100%', maxWidth: '450px', marginBottom: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h3 style={{color: '#fff', margin: 0}}>Đang quét...</h3>
                        <button onClick={handleStopScanning} style={{background: 'none', border: '1px solid #666', color: '#aaa', borderRadius: '4px', padding: '5px 10px', cursor: 'pointer'}}>
                            Thoát
                        </button>
                    </div>

                    <QRScanner onScanSuccess={handleScan} isScanning={isScanning} />
                </>
            )}

            {/* 3. MODAL KẾT QUẢ: Hiện đè lên Camera khi có kết quả */}
            {isCameraActive && status !== 'idle' && (
                <div style={styles.resultOverlay}>
                    <div style={styles.resultCard}>
                        {/* Header Modal */}
                        <div style={{ textAlign: 'center', marginBottom: 15 }}>
                            {status === 'processing' && <span style={{fontSize: 40}}>⏳</span>}
                            {status === 'success' && <span style={{fontSize: 40}}>✅</span>}
                            {status === 'error' && <span style={{fontSize: 40}}>❌</span>}
                            
                            <h2 style={{ 
                                margin: '10px 0', 
                                color: status === 'error' ? '#ff4d4f' : '#d4af37' 
                            }}>
                                {status === 'processing' ? 'Đang xử lý...' : message}
                            </h2>
                        </div>

                        {/* Nội dung chi tiết (Chỉ hiện khi thành công và có data) */}
                        {checkInData && (
                            <div style={{color: 'white'}}>
                                {/* Thông tin khách */}
                                <div style={{background: 'rgba(212, 175, 55, 0.15)', padding: '15px', borderRadius: '8px', marginBottom: '15px'}}>
                                    <div style={{fontSize: '0.9rem', color: '#aaa'}}>Khách tham dự:</div>
                                    <div style={{fontSize: '1.4rem', fontWeight: 'bold', color: '#fff'}}>{checkInData.attendee.username}</div>
                                    <div style={{fontSize: '0.9rem', color: '#d4af37'}}>{checkInData.attendee.email}</div>
                                    <div style={{marginTop: 5, fontSize: '0.85rem', color: '#ccc'}}>Vé: <span style={{fontFamily: 'monospace', background: '#333', padding: '2px 5px'}}>{checkInData.attendee.ticketCode}</span></div>
                                </div>

                                {/* Thông tin sự kiện */}
                                <div style={{marginBottom: '15px'}}>
                                    <div style={{fontWeight: 'bold', color: '#d4af37'}}>🎉 {checkInData.event.eventName}</div>
                                    <div style={{fontSize: '0.9rem', color: '#aaa'}}>📍 {checkInData.event.location}</div>
                                </div>

                                {/* Danh sách Activity */}
                                {renderAgenda()}
                            </div>
                        )}

                        {/* Footer Modal (Nút bấm) */}
                        <div style={{ marginTop: 25, textAlign: 'center', display: 'flex', gap: '10px', justifyContent: 'center' }}>
                            {(status === 'success' || status === 'error') && (
                                <>
                                    <button onClick={handleStopScanning} style={{...styles.btnOutline, flex: 1}}>
                                        Dừng
                                    </button>
                                    <button onClick={handleNextScan} className="btn-gold" style={{flex: 2}}>
                                        {status === 'success' ? 'Quét vé tiếp' : 'Thử lại'}
                                    </button>
                                </>
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
        position: 'fixed', bottom: 0, left: 0, right: 0, top: 0,
        zIndex: 100, 
        background: 'rgba(0,0,0,0.9)', // Nền đen đậm hơn chút để dễ đọc chữ
        display: 'flex', justifyContent: 'center', alignItems: 'center',
        padding: 20
    },
    resultCard: {
        width: '100%', maxWidth: '450px', maxHeight: '90vh', overflowY: 'auto', // Cho phép cuộn nếu danh sách dài
        background: '#1a1a1a', borderRadius: '15px', padding: '25px',
        border: '1px solid #d4af37', boxShadow: '0 0 30px rgba(212, 175, 55, 0.2)'
    },
    btnOutline: {
        background: 'transparent', border: '1px solid #666', color: '#fff',
        padding: '12px', borderRadius: '30px', cursor: 'pointer', fontWeight: 'bold'
    }
};

export default OrganizerCheckIn;
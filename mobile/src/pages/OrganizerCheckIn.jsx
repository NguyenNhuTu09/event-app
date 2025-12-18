import React, { useState } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';

const OrganizerCheckIn = () => {
    // State lưu toàn bộ cục data backend trả về
    const [checkInData, setCheckInData] = useState(null); 
    const [message, setMessage] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);

    const onNewScanResult = (decodedText) => {
        if (isProcessing) return;
        setIsProcessing(true);

        axiosClient.post('/checkin/event', { ticketCode: decodedText })
            .then(response => {
                // response.data bây giờ là EventCheckInResultDTO
                setCheckInData(response.data); 
                setMessage(`✅ CHECK-IN THÀNH CÔNG`);
                // Play sound beep...
            })
            .catch(error => {
                setCheckInData(null); // Reset nếu lỗi
                setMessage(`❌ LỖI: ${error.response?.data?.message || "Vé không hợp lệ"}`);
            })
            .finally(() => {
                setTimeout(() => setIsProcessing(false), 2000);
            });
    };

    // Hàm render danh sách hoạt động
    const renderAgenda = () => {
        if (!checkInData?.agenda || checkInData.agenda.length === 0) {
            return <p>Sự kiện này chưa có hoạt động nào.</p>;
        }

        return (
            <div style={{ marginTop: 15, textAlign: 'left' }}>
                <h4 style={{ borderBottom: '2px solid #ddd', paddingBottom: 5 }}>📅 Lịch trình sự kiện</h4>
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                    {checkInData.agenda.map((act) => (
                        <div key={act.activityId} style={styles.activityCard}>
                            <div style={{ fontWeight: 'bold', color: '#007bff' }}>
                                {formatTime(act.startTime)} - {formatTime(act.endTime)}
                            </div>
                            <div style={{ fontWeight: 'bold', fontSize: '1.1rem' }}>
                                {act.activityName}
                            </div>
                            <div style={{ fontSize: '0.9rem', color: '#666' }}>
                                📍 {act.roomOrVenue || "Chưa cập nhật phòng"} | 🎤 {act.presenter?.fullName || "Không có diễn giả"}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        );
    };

    const formatTime = (timeString) => {
        if(!timeString) return "";
        return new Date(timeString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }

    return (
        <div style={{ padding: 20, maxWidth: 600, margin: '0 auto' }}>
            <h2 style={{ textAlign: 'center' }}>Cổng Check-in</h2>
            
            <div style={{ marginBottom: 20 }}>
                <QRScanner fps={10} qrbox={250} qrCodeSuccessCallback={onNewScanResult} />
            </div>

            {/* Khu vực hiển thị thông báo trạng thái */}
            <div style={{ textAlign: 'center', marginBottom: 20 }}>
                <h3 style={{ color: message.includes('LỖI') ? 'red' : 'green' }}>{message}</h3>
            </div>

            {/* Khu vực hiển thị thông tin chi tiết sau khi quét thành công */}
            {checkInData && (
                <div style={styles.resultContainer}>
                    {/* 1. Thông tin Khách */}
                    <div style={styles.attendeeBox}>
                        <h3>👤 {checkInData.attendee.username}</h3>
                        <p>Email: {checkInData.attendee.email}</p>
                        <p>Mã vé: <span style={{ fontFamily: 'monospace', background: '#eee', padding: '2px 5px' }}>
                            {checkInData.attendee.ticketCode}
                        </span></p>
                    </div>

                    {/* 2. Thông tin Sự kiện */}
                    <div style={{ marginTop: 15 }}>
                        <h4 style={{ margin: '10px 0 5px 0', color: '#555' }}>Đang tham gia:</h4>
                        <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: '#333' }}>
                            {checkInData.event.eventName}
                        </div>
                        <p style={{ fontSize: '0.9rem', color: '#777' }}>📍 {checkInData.event.location}</p>
                    </div>

                    {/* 3. Danh sách Hoạt động (Agenda) */}
                    {renderAgenda()}
                </div>
            )}
        </div>
    );
};

// CSS styles đơn giản
const styles = {
    resultContainer: {
        background: 'white',
        padding: 20,
        borderRadius: 10,
        boxShadow: '0 4px 15px rgba(0,0,0,0.1)',
        border: '1px solid #eee'
    },
    attendeeBox: {
        background: '#e3f2fd', // Màu xanh nhạt
        padding: 15,
        borderRadius: 8,
        borderLeft: '5px solid #2196f3'
    },
    activityCard: {
        background: '#f9f9f9',
        padding: 10,
        marginBottom: 10,
        borderRadius: 6,
        border: '1px solid #eee'
    }
};

export default OrganizerCheckIn;
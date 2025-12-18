import React, { useState } from 'react';
import QRScanner from '../components/QRScanner';
import axiosClient from '../api/axiosClient';

const UserActivityCheckIn = () => {
    const [message, setMessage] = useState('');
    const [isSuccess, setIsSuccess] = useState(false);

    const handleScan = (decodedText) => {
        axiosClient.post('/checkin/activity', { activityQrCode: decodedText })
            .then(response => {
                setMessage(response.data); // "Điểm danh thành công..."
                setIsSuccess(true);
            })
            .catch(error => {
                setMessage(error.response?.data?.message || "Lỗi điểm danh");
                setIsSuccess(false);
            });
    };

    return (
        <div style={{ padding: 20 }}>
            <h2>Điểm danh hoạt động</h2>
            {!isSuccess ? (
                <>
                    <p>Hãy hướng camera về phía mã QR của hoạt động</p>
                    <QRScanner qrCodeSuccessCallback={handleScan} />
                </>
            ) : (
                <div style={{ textAlign: 'center', color: 'green', marginTop: 50 }}>
                    <h1>✅</h1>
                    <h3>{message}</h3>
                    <button onClick={() => { setIsSuccess(false); setMessage(''); }}>
                        Quét hoạt động khác
                    </button>
                </div>
            )}
            
            {message && !isSuccess && (
                <p style={{ color: 'red', textAlign: 'center', fontWeight: 'bold' }}>
                    {message}
                </p>
            )}
        </div>
    );
};

export default UserActivityCheckIn;
import React, { useState } from 'react';
import QRScanner from '../components/QRScanner'; 
import axiosClient from '../api/axiosClient';

const OrganizerCheckIn = () => {
    const [scanResult, setScanResult] = useState(null);
    const [message, setMessage] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);

    const onNewScanResult = (decodedText, decodedResult) => {
        if (isProcessing) return;

        setIsProcessing(true);
        setScanResult(decodedText);
        
        axiosClient.post('/checkin/event', { ticketCode: decodedText })
            .then(response => {
                setMessage(`✅ THÀNH CÔNG: ${response.data.username}`);
            })
            .catch(error => {
                console.error(error);
                setMessage(`❌ LỖI: ${error.response?.data?.message || "Vé không hợp lệ"}`);
            })
            .finally(() => {
                setTimeout(() => setIsProcessing(false), 2000);
            });
    };

    return (
        <div style={{ textAlign: 'center', padding: 20 }}>
            <h2>Organizer: Check-in Cổng</h2>
            <QRScanner 
                fps={10} 
                qrbox={250} 
                disableFlip={false} 
                qrCodeSuccessCallback={onNewScanResult} 
            />
            <div style={{ marginTop: 20 }}>
                <p>Kết quả: {scanResult}</p>
                <h3>{message}</h3>
            </div>
        </div>
    );
};

export default OrganizerCheckIn;
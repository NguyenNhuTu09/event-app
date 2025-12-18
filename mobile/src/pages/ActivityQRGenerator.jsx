import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import axiosClient from '../api/axiosClient';

const ActivityQRGenerator = () => {
    const { activityId } = useParams(); 
    const [qrString, setQrString] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        axiosClient.get(`/activities/${activityId}/qr-code`)
            .then(response => {
                setQrString(response.data); 
                setLoading(false);
            })
            .catch(err => {
                setError(err.response?.data?.message || "Không thể tải mã QR");
                setLoading(false);
            });
    }, [activityId]);

    return (
        <div style={{ 
            display: 'flex', 
            flexDirection: 'column', 
            alignItems: 'center', 
            justifyContent: 'center', 
            height: '100vh',
            background: '#f0f2f5' 
        }}>
            <h1>Check-in Hoạt Động</h1>
            {loading && <p>Đang tải mã...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            
            {qrString && (
                <div style={{ background: 'white', padding: '30px', borderRadius: '10px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
                    <QRCodeSVG 
                        value={qrString} 
                        size={300} 
                        level={"H"} 
                        includeMargin={true}
                    />
                    <p style={{ marginTop: 15, fontSize: '1.2rem', fontWeight: 'bold' }}>
                        Quét mã này để điểm danh
                    </p>
                </div>
            )}
        </div>
    );
};

export default ActivityQRGenerator;
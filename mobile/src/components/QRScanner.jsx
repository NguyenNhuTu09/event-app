import React, { useEffect, useRef } from 'react';
import { Html5Qrcode } from 'html5-qrcode';

const QRScanner = ({ onScanSuccess, onScanFailure }) => {
    const html5QrCodeRef = useRef(null);

    useEffect(() => {
        const elementId = "reader-element";
        
        if (!html5QrCodeRef.current) {
            html5QrCodeRef.current = new Html5Qrcode(elementId);
        }
        
        const html5QrCode = html5QrCodeRef.current;

        const config = { 
            fps: 10, 
            qrbox: { width: 250, height: 250 },
            aspectRatio: 1.0,
            disableFlip: false 
        };

        const startScanning = async () => {
            try {
                if (html5QrCode.isScanning) return;
                
                await html5QrCode.start(
                    { facingMode: "environment" }, 
                    config,
                    (decodedText) => {
                        if(onScanSuccess) onScanSuccess(decodedText);
                    },
                    (errorMessage) => {
                        if(onScanFailure) onScanFailure(errorMessage);
                    }
                );
            } catch (err) {
                console.error("Lỗi khởi động camera:", err);
            }
        };

        startScanning();

        return () => {
            if (html5QrCode.isScanning) {
                html5QrCode.stop().then(() => {
                    html5QrCode.clear();
                }).catch(err => {
                    console.error("Lỗi khi tắt camera:", err);
                });
            }
        };
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    return (
        <div className="scanner-container">
            {/* Element chứa video camera */}
            <div id="reader-element"></div>
            
            {/* Lớp phủ giao diện (Khung ngắm) */}
            <div className="scan-overlay">
                <div className="scan-box">
                    <div className="scan-corner-bl"></div>
                    <div className="scan-corner-br"></div>
                    <div className="scan-line"></div>
                </div>
            </div>
            
            <p style={{
                position: 'absolute', bottom: '20px', width: '100%', textAlign: 'center',
                color: 'white', zIndex: 20, fontSize: '0.9rem', textShadow: '0 2px 4px rgba(0,0,0,0.8)',
                pointerEvents: 'none'
            }}>
                Di chuyển camera đến mã QR để quét
            </p>
        </div>
    );
};

export default QRScanner;
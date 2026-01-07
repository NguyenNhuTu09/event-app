import React, { useEffect, useRef } from 'react';
import { Html5Qrcode, Html5QrcodeScannerState } from 'html5-qrcode';

const QRScanner = ({ onScanSuccess, isScanning }) => {
    // isScanning = true: Cho phép nhận mã
    // isScanning = false: Đang xử lý, bỏ qua mọi mã quét được
    
    const html5QrCodeRef = useRef(null);
    const isLockedRef = useRef(false);

    // Đồng bộ state isScanning từ cha xuống ref
    useEffect(() => {
        if (isScanning) {
            isLockedRef.current = false; // Mở khóa
        } else {
            isLockedRef.current = true; // Khóa lại
        }
    }, [isScanning]);

    useEffect(() => {
        const elementId = "reader-element";
        
        // Tạo instance một lần duy nhất
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
            // Kiểm tra trạng thái camera trước khi start để tránh lỗi "transition"
            const state = html5QrCode.getState();
            if (state === Html5QrcodeScannerState.SCANNING || 
                state === Html5QrcodeScannerState.PAUSED) {
                // Nếu đang chạy rồi thì thôi, không start lại
                return;
            }

            try {
                await html5QrCode.start(
                    { facingMode: "environment" }, 
                    config,
                    (decodedText) => {
                        // 1. Nếu đang bị khóa (isScanning = false) thì lờ đi
                        if (isLockedRef.current) return;

                        // 2. Khóa ngay lập tức để chặn mã tiếp theo
                        isLockedRef.current = true;

                        // 3. Gửi kết quả lên cha
                        if (onScanSuccess) onScanSuccess(decodedText);
                    },
                    (errorMessage) => { /* Bỏ qua lỗi quét frame */ }
                );
            } catch (err) {
                // Nếu lỗi là do "already under transition" thì bỏ qua, không sao cả
                console.log("Scanner start warning:", err);
            }
        };

        // Chạy hàm start
        startScanning();

        // Cleanup function
        return () => {
            // Quan trọng: Chỉ stop khi component thực sự bị hủy (Unmount)
            if (html5QrCode.isScanning) {
                html5QrCode.stop().then(() => {
                    html5QrCode.clear();
                }).catch(err => console.log("Stop failed", err));
            }
        };
    }, []); 

    return (
        // Thêm class 'blurred' để làm mờ khi isScanning = false
        <div className={`scanner-container ${!isScanning ? 'blurred' : ''}`}>
            <div id="reader-element"></div>
            
            <div className="scan-overlay">
                <div className="scan-box">
                    <div className="scan-corner-bl"></div>
                    <div className="scan-corner-br"></div>
                    <div className="scan-line"></div>
                </div>
            </div>
            
            {isScanning && (
                <p style={{
                    position: 'absolute', bottom: '20px', width: '100%', textAlign: 'center',
                    color: 'white', zIndex: 20, fontSize: '0.9rem', pointerEvents: 'none',
                    textShadow: '0 2px 4px rgba(0,0,0,0.8)'
                }}>
                    Di chuyển camera đến mã QR
                </p>
            )}
        </div>
    );
};

export default QRScanner;
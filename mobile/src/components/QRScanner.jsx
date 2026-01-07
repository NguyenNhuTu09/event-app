// components/QRScanner.js
import React, { useEffect, useRef } from 'react';
import { Html5Qrcode, Html5QrcodeScannerState } from 'html5-qrcode';

const QRScanner = ({ onScanSuccess, onScanFailure, isScanning }) => {
    const html5QrCodeRef = useRef(null);
    const isHandlingRef = useRef(false); // Biến lock để chặn gọi 2 lần liên tiếp

    useEffect(() => {
        const elementId = "reader-element";
        
        // 1. Khởi tạo Scanner nếu chưa có
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

        // 2. Hàm start quét
        const startScanning = async () => {
            try {
                // Chỉ start nếu đang ở trạng thái NOT_STARTED hoặc UNKNOWN
                if (html5QrCode.getState() === Html5QrcodeScannerState.NOT_STARTED || 
                    html5QrCode.getState() === Html5QrcodeScannerState.UNKNOWN) {
                    
                    await html5QrCode.start(
                        { facingMode: "environment" }, 
                        config,
                        (decodedText) => {
                            // --- LOGIC QUAN TRỌNG Ở ĐÂY ---
                            
                            // Nếu đang xử lý rồi thì bỏ qua ngay
                            if (isHandlingRef.current) return;
                            
                            // Khóa lại ngay lập tức
                            isHandlingRef.current = true;

                            // PAUSE CAMERA (Làm đông cứng hình ảnh)
                            // true: freezeImage (giữ lại hình ảnh hiện tại)
                            html5QrCode.pause(true); 

                            // Gửi dữ liệu lên component cha
                            if (onScanSuccess) onScanSuccess(decodedText);
                        },
                        (errorMessage) => {
                            // Không làm gì khi lỗi quét frame
                        }
                    );
                }
            } catch (err) {
                console.error("Lỗi start camera:", err);
            }
        };

        // Khởi động lần đầu
        startScanning();

        return () => {
            // Cleanup khi unmount
            if (html5QrCode.isScanning) {
                html5QrCode.stop().catch(err => console.error(err));
            }
        };
    }, []); 

    // 3. Effect để lắng nghe props isScanning từ cha để Resume lại
    useEffect(() => {
        const html5QrCode = html5QrCodeRef.current;
        
        if (html5QrCode && html5QrCode.getState() === Html5QrcodeScannerState.PAUSED) {
            if (isScanning) {
                // Nếu cha bảo quét tiếp -> Resume
                html5QrCode.resume();
                isHandlingRef.current = false; // Mở khóa để nhận mã mới
            }
        }
    }, [isScanning]);

    return (
        <div className="scanner-container">
            <div id="reader-element"></div>
            <div className="scan-overlay">
                <div className="scan-box">
                    <div className="scan-corner-bl"></div>
                    <div className="scan-corner-br"></div>
                    <div className="scan-line"></div>
                </div>
            </div>
            <p style={{
                position: 'absolute', bottom: '20px', width: '100%', textAlign: 'center',
                color: 'white', zIndex: 20, fontSize: '0.9rem', pointerEvents: 'none'
            }}>
                Di chuyển camera đến mã QR
            </p>
        </div>
    );
};

export default QRScanner;
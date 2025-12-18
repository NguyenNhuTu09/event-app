import React, { useEffect } from 'react';
import { Html5QrcodeScanner } from 'html5-qrcode';

const QRScanner = ({ fps, qrbox, disableFlip, verbose, qrCodeSuccessCallback, qrCodeErrorCallback }) => {
    
    useEffect(() => {
        const config = {
            fps: fps ? fps : 10,
            qrbox: qrbox ? qrbox : { width: 250, height: 250 },
            disableFlip: disableFlip === undefined ? false : disableFlip,
        };

        const html5QrcodeScanner = new Html5QrcodeScanner(
            "reader", config, verbose === undefined ? false : verbose
        );
        
        html5QrcodeScanner.render(qrCodeSuccessCallback, qrCodeErrorCallback);

        return () => {
            html5QrcodeScanner.clear().catch(error => {
                console.error("Failed to clear html5QrcodeScanner. ", error);
            });
        };
    }, []);

    return (
        <div id="reader" style={{ width: '100%', maxWidth: '500px', margin: '0 auto' }}></div>
    );
};

export default QRScanner;
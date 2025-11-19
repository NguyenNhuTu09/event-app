import React from "react";
import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';
import './Customer.css';

// Import các logo công ty đối tác
import glLogo from '../../assets/images/gl-logo.webp';
import mgvLogo from '../../assets/images/MGV-logo-white.webp';
import logicrossLogo from '../../assets/images/Logicross-Logo.pdf-1-1.png.webp';
import ctbcnLogo from '../../assets/images/CTBCN-1-1.webp';
import ipcLogo from '../../assets/images/IPC-Logo-trang-ko-nen-e1740325775423.webp';
import mmaLogo from '../../assets/images/MMA_Logo-White_edited.webp';
import ogawaLogo from '../../assets/images/Logo-Ogawa.webp';
import logoWhite from '../../assets/images/logo-white-e1738741553712-300x101-1.webp';

const Customer = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    // Danh sách các logo công ty đối tác
    const partnerLogos = [
        {
            name: 'Savills',
            logo: logoWhite,
        },
        {
            name: 'GAMUDA LAND',
            logo: glLogo,
        },
        {
            name: 'MGV',
            logo: mgvLogo,
        },
        {
            name: 'mde pub consultants',
            logo: logoWhite,
        },
        {
            name: 'Logicross',
            logo: logicrossLogo,
        },
        {
            name: 'MINH NGUYENDESIGN',
            logo: logoWhite,
        },
        {
            name: 'CTBCN ENGINEERING',
            logo: ctbcnLogo,
        },
        {
            name: 'INTERCONTINENTAL (IPC) PROPERTY',
            logo: ipcLogo,
        },
        {
            name: 'MARKET MINDS ASIA',
            logo: mmaLogo,
        },
        {
            name: 'OGAWA',
            logo: ogawaLogo,
        },
    ];

    return (
        <div className="container-customer">
            <div className="container-title">
                <h3>{t.customerTitle}</h3>
                <p>{t.customerDesc}</p>
            </div>
            <div className="container-content">
                <div className="container-logos">
                    {partnerLogos.map((partner, index) => (
                        <div key={index} className="customer-item">
                            <img src={partner.logo} alt={partner.name} />
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

export default Customer;

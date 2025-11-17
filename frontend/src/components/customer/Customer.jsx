import React from "react";

import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';
import './Customer.css';
import D8C97B6 from '../../assets/images/D8C97B-6.jpg';
import D8C97B9 from '../../assets/images/D8C97B-9.jpg';
import D8C97B10 from '../../assets/images/D8C97B-10.jpg';
import D8C97B12 from '../../assets/images/D8C97B-12.jpg';
import CTBCN from '../../assets/images/CTBCN-1-1.jpg';
import Logicross from '../../assets/images/Logicross-Logo.pdf-1-1.png.webp';
import Ogawa from '../../assets/images/Logo-Ogawa.webp';
import GLLogo from '../../assets/images/gl-logo.webp';

const Customer = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    const customerData = [
        {
            image: D8C97B6,
        },
        {
            image: D8C97B9,
        },
        {
            image: D8C97B10,
        },
        {
            image: D8C97B12,
        },
        {
            image: CTBCN,
        },
        {
            image: Logicross,
        },
        {
            image: Ogawa,
        },
        {
            image: GLLogo,
        }
    ];

    return (
        <div className="container-customer">
            <div className="container-title">
                <h3>{t.customerTitle}</h3>
                <p>{t.customerDesc}</p>
            </div>
            <div className="container-content">
                <div className="container-logos">
                    {customerData.map((customer, index) => (
                        <div key={index} className="customer-item">
                            <img src={customer.image} alt={`customer-${index}`} />
                        </div>
                    ))}
                </div>
            </div>
            <button className="customer-demo">Demo</button>
        </div>
    )
}

export default Customer;


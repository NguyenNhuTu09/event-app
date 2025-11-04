import React from "react";

import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';
import './Customer.css';
import D8C97B6 from '../../assets/images/D8C97B-6.webp';
import D8C97B8 from '../../assets/images/D8C97B-8.webp';

import D8C97B9 from '../../assets/images/D8C97B-9.webp';

import D8C97B10 from '../../assets/images/D8C97B-10.webp';
import D8C97B12 from '../../assets/images/D8C97B-12.webp';

const Customer = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;
    const customerData = [
        {
            image: D8C97B6,
        },
        {
            image: D8C97B8,
        },
        {
            image: D8C97B9,
        },
        {
            image: D8C97B10,
        },
        {
            image: D8C97B12,
        }
    ]
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


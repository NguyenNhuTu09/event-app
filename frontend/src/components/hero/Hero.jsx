import React from 'react';
import './Hero.css';

// Import hook và file dịch
import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';

const Hero = () => {
    // Lấy ngôn ngữ từ context
    const { language } = useLanguage();
    const t = translations[language];

    return (
        <section className="hero">
            <div className="hero-content">
                <h1>{t.heroTitle}</h1>
                <p>{t.heroSubtitle}</p>
                <div className="hero-cta">
                    <a href="#" className="btn btn-dark">{t.requestDemo} <i className="fas fa-arrow-right"></i></a>
                    <a href="#" className="link-arrow">{t.watchDemo} <i className="fas fa-arrow-right"></i></a>
                </div>
            </div>

        </section>
    );
};

export default Hero;
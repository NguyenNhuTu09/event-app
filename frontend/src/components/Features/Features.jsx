import React from 'react';
import './Features.css';
import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';

import videoConfImage from '../../assets/images/EMS Classroom Scheduling Software.webp';
import classroomImage from '../../assets/images/Videoconferencing Herowebp.webp';
import eventImage from '../../assets/images/Ems Conference.jpg';

const Features = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    const featuresData = [
        {
            image: videoConfImage,
            title: t.feature1Title,
            description: t.feature1Desc,
        },
        {
            image: classroomImage,
            title: t.feature2Title,
            description: t.feature2Desc,
        },
        {
            image: eventImage,
            title: t.feature3Title,
            description: t.feature3Desc,
        },
        {
            image: eventImage,
            title: t.feature3Title,
            description: t.feature3Desc,
        },
        {
            image: eventImage,
            title: t.feature3Title,
            description: t.feature3Desc,
        },
        {
            image: eventImage,
            title: t.feature3Title,
            description: t.feature3Desc,
        },
    ];

    return (
        <section className="features-section">
            <div className="container">
                <div className="section-header">
                    <h2>{t.featuresTitle}</h2>
                    <p>{t.featuresSubtitle}</p>
                </div>
                <div className="features-grid">
                    {featuresData.map((feature, index) => (
                        <div className="feature-card" key={index}>
                            <div className="feature-image-container">
                                <img src={feature.image} alt={feature.title} />
                            </div>
                            <h3>{feature.title}</h3>
                            <p>{feature.description}</p>
                            <a href="#" className="learn-more-link">{t.learnMore}</a>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default Features;
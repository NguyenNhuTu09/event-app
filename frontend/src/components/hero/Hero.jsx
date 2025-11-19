import React, { useEffect, useRef } from 'react';
import './Hero.css';
import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import LightBeamEffect from './LightBeamEffect';
import heroBanner from '../../assets/images/pexels-mat-brown-150387-1395967 (1).jpg';

gsap.registerPlugin(ScrollTrigger);

const Hero = () => {
    const { language } = useLanguage();
    const t = translations[language];
    const heroRef = useRef(null);

    useEffect(() => {
        const hero = heroRef.current;
        if (!hero) return;

        // Animation khởi đầu (banner fade-in khi load)
        gsap.fromTo(
            hero.querySelector('.hero-background-image'),
            { opacity: 0, scale: 1.2 },
            { opacity: 1, scale: 1, duration: 1.5, ease: 'power2.out' }
        );

        // Fade out hero content khi scroll (không kéo hero section lên để tránh khoảng trống)
        const heroContent = hero.querySelector('.hero-content');
        if (heroContent) {
            gsap.to(heroContent, {
                scrollTrigger: {
                    trigger: hero,
                    start: 'top top',
                    end: 'center top',
                    scrub: true,
                },
                opacity: 0,
                y: -20,
                ease: 'power1.out',
            });
        }

        // Background fade out nhẹ khi scroll
        const heroOverlay = hero.querySelector('.hero-background-overlay');
        if (heroOverlay) {
            gsap.to(heroOverlay, {
                scrollTrigger: {
                    trigger: hero,
                    start: 'top top',
                    end: 'bottom top',
                    scrub: true,
                },
                opacity: 0.3,
                ease: 'power1.inOut',
            });
        }

    }, []);

    return (
        <section className="hero" ref={heroRef}>
            <div className="hero-background">
                <img
                    src={heroBanner}
                    alt="Hero Banner"
                    className="hero-background-image"
                />
                <div className="hero-background-overlay"></div>
                {/* <LightBeamEffect /> */}
            </div>
            <div className="hero-content">
                <h1>{t.heroTitle}</h1>
                <p>{t.heroSubtitle}</p>
                <div className="hero-cta">
                    <a href="#" className="btn btn-dark">
                        {t.requestDemo} <i className="fas fa-arrow-right"></i>
                    </a>
                    <a href="#" className="link-arrow">
                        {t.watchDemo} <i className="fas fa-arrow-right"></i>
                    </a>
                </div>
            </div>
        </section>
    );
};

export default Hero;

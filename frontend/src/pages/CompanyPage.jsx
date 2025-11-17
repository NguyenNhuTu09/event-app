import React, { useEffect, useRef } from 'react';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import './CompanyPage.css';

gsap.registerPlugin(ScrollTrigger);

const CompanyPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;
    
    // Refs cho GSAP animations
    const heroRef = useRef(null);
    const servicesRef = useRef(null);
    const whyRef = useRef(null);
    const aboutRef = useRef(null);
    const contactRef = useRef(null);

    // GSAP Animations
    useEffect(() => {
        // Animate hero section
        const hero = heroRef.current;
        if (hero) {
            const title = hero.querySelector('.hero-title');
            const description = hero.querySelector('.hero-description');
            const tagline = hero.querySelector('.hero-tagline');
            const button = hero.querySelector('.btn-primary');
            
            const tl = gsap.timeline();
            
            if (tagline) {
                tl.fromTo(
                    tagline,
                    { opacity: 0, y: 30 },
                    { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out' }
                );
            }
            
            if (title) {
                tl.fromTo(
                    title,
                    { opacity: 0, y: 50, scale: 0.9 },
                    { opacity: 1, y: 0, scale: 1, duration: 1, ease: 'power3.out' },
                    '-=0.3'
                );
            }
            
            if (description) {
                tl.fromTo(
                    description,
                    { opacity: 0, y: 30 },
                    { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out' },
                    '-=0.5'
                );
            }
            
            if (button) {
                tl.fromTo(
                    button,
                    { opacity: 0, y: 20 },
                    { opacity: 1, y: 0, duration: 0.6, ease: 'power2.out' },
                    '-=0.3'
                );
            }
        }

        // Animate sections khi scroll
        const sections = [servicesRef, whyRef, aboutRef, contactRef];
        sections.forEach((section) => {
            if (section.current) {
                gsap.fromTo(
                    section.current,
                    { opacity: 0, y: 60 },
                    {
                        opacity: 1,
                        y: 0,
                        duration: 1,
                        ease: 'power2.out',
                        scrollTrigger: {
                            trigger: section.current,
                            start: 'top 80%',
                            toggleActions: 'play none none reverse',
                        },
                    }
                );
            }
        });

        // Animate cards trong services và why sections
        const serviceCards = servicesRef.current?.querySelectorAll('.service-card');
        const whyCards = whyRef.current?.querySelectorAll('.why-card');
        
        if (serviceCards && serviceCards.length > 0) {
            serviceCards.forEach((card, index) => {
                gsap.fromTo(
                    card,
                    { opacity: 0, y: 40, scale: 0.9 },
                    {
                        opacity: 1,
                        y: 0,
                        scale: 1,
                        duration: 0.8,
                        ease: 'power2.out',
                        scrollTrigger: {
                            trigger: card,
                            start: 'top 85%',
                            toggleActions: 'play none none reverse',
                        },
                        delay: index * 0.1,
                    }
                );
            });
        }
        
        if (whyCards && whyCards.length > 0) {
            whyCards.forEach((card, index) => {
                gsap.fromTo(
                    card,
                    { opacity: 0, y: 40, scale: 0.9 },
                    {
                        opacity: 1,
                        y: 0,
                        scale: 1,
                        duration: 0.8,
                        ease: 'power2.out',
                        scrollTrigger: {
                            trigger: card,
                            start: 'top 85%',
                            toggleActions: 'play none none reverse',
                        },
                        delay: index * 0.1,
                    }
                );
            });
        }

        return () => {
            ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
        };
    }, []);

    return (
        <div className="company-page">
            {/* Hero Section */}
            <section className="company-hero" ref={heroRef}>
                <div className="container">
                    <div className="hero-content">
                        <p className="hero-tagline">{t.companyTagline}</p>
                        <h1 className="hero-title">{t.companyName}</h1>
                        <p className="hero-description">{t.companyDescription}</p>
                        <a href="#contact" className="btn-primary cta-button-contact">{t.contactUs}</a>
                    </div>
                </div>
            </section>

            {/* Services Section */}
            <section className="company-services" ref={servicesRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.companyServicesTitle}</h2>
                        <p>{t.companyServicesSubtitle}</p>
                    </div>
                    <div className="services-grid">
                        <div className="service-card">
                            <div className="service-icon">
                                <i className="bi bi-laptop"></i>
                            </div>
                            <h3>{t.serviceWebsiteTitle}</h3>
                            <p>{t.serviceWebsiteDesc}</p>
                        </div>
                        <div className="service-card">
                            <div className="service-icon">
                                <i className="bi bi-graph-up-arrow"></i>
                            </div>
                            <h3>{t.serviceMarketingTitle}</h3>
                            <p>{t.serviceMarketingDesc}</p>
                        </div>
                        <div className="service-card">
                            <div className="service-icon">
                                <i className="bi bi-gear"></i>
                            </div>
                            <h3>{t.serviceITTitle}</h3>
                            <p>{t.serviceITDesc}</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Why Choose Us Section */}
            <section className="company-why" ref={whyRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.companyWhyTitle}</h2>
                    </div>
                    <div className="why-grid">
                        <div className="why-card">
                            <div className="why-icon">
                                <i className="bi bi-award"></i>
                            </div>
                            <h3>{t.whyExpertiseTitle}</h3>
                            <p>{t.whyExpertiseDesc}</p>
                        </div>
                        <div className="why-card">
                            <div className="why-icon">
                                <i className="bi bi-people"></i>
                            </div>
                            <h3>{t.whyCustomerTitle}</h3>
                            <p>{t.whyCustomerDesc}</p>
                        </div>
                        <div className="why-card">
                            <div className="why-icon">
                                <i className="bi bi-lightbulb"></i>
                            </div>
                            <h3>{t.whyInnovationTitle}</h3>
                            <p>{t.whyInnovationDesc}</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* About Section */}
            <section className="company-about" ref={aboutRef}>
                <div className="container">
                    <div className="about-content">
                        <div className="about-text">
                            <h2>{t.companyAboutTitle}</h2>
                            <p>{t.companyAboutDesc1}</p>
                            <p>{t.companyAboutDesc2}</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Contact Section */}
            <section id="contact" className="company-contact" ref={contactRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.companyContactTitle}</h2>
                        <p>{t.companyContactSubtitle}</p>
                    </div>
                    <div className="contact-grid">
                        <div className="contact-card">
                            <div className="contact-icon">
                                <i className="bi bi-telephone"></i>
                            </div>
                            <h3>{t.contactCall}</h3>
                            <p>+84 969 838 467</p>
                        </div>
                        <div className="contact-card">
                            <div className="contact-icon">
                                <i className="bi bi-envelope"></i>
                            </div>
                            <h3>{t.contactEmail}</h3>
                            <p>Huyen.dang@webie.com.vn</p>
                        </div>
                        <div className="contact-card">
                            <div className="contact-icon">
                                <i className="bi bi-geo-alt"></i>
                            </div>
                            <h3>{t.contactAddress}</h3>
                            <p>53th street 57, An Phu Ward,<br />Thu Duc City, HCMC</p>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default CompanyPage;








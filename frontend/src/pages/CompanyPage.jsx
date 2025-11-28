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
    const websiteServicesRef = useRef(null);
    const marketingServicesRef = useRef(null);
    const itServicesRef = useRef(null);
    const whyRef = useRef(null);
    const contactRef = useRef(null);

    // GSAP Animations
    useEffect(() => {
        // Animate hero section với hiệu ứng mạnh hơn
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
                    { opacity: 0, y: 30, scale: 0.95 },
                    { opacity: 1, y: 0, scale: 1, duration: 0.8, ease: 'power2.out' }
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
                    { opacity: 0, y: 20, scale: 0.95 },
                    { opacity: 1, y: 0, scale: 1, duration: 0.6, ease: 'power2.out' },
                    '-=0.3'
                );
            }
        }

        // Animate sections khi scroll với hiệu ứng mượt hơn
        const sections = [websiteServicesRef, contactRef];
        sections.forEach((section) => {
            if (section.current) {
                gsap.fromTo(
                    section.current,
                    { opacity: 0, y: 80 },
                    {
                        opacity: 1,
                        y: 0,
                        duration: 1.2,
                        ease: 'power3.out',
                        scrollTrigger: {
                            trigger: section.current,
                            start: 'top 85%',
                            toggleActions: 'play none none reverse',
                        },
                    }
                );
            }
        });

        // Hiệu ứng đổ vào đặc biệt cho Digital Marketing Service - từ phải sang trái
        if (marketingServicesRef.current) {
            const marketingSection = marketingServicesRef.current;
            const header = marketingSection.querySelector('.service-section-header');
            const grid = marketingSection.querySelector('.marketing-grid');
            
            // Background reveal effect
            ScrollTrigger.create({
                trigger: marketingSection,
                start: 'top 80%',
                onEnter: () => marketingSection.classList.add('animate'),
            });

            // Tạo timeline cho section này
            const marketingTL = gsap.timeline({
                scrollTrigger: {
                    trigger: marketingSection,
                    start: 'top 80%',
                    toggleActions: 'play none none reverse',
                }
            });

            // Header slide từ phải
            if (header) {
                marketingTL.fromTo(
                    header,
                    { opacity: 0, x: 100, scale: 0.9 },
                    { opacity: 1, x: 0, scale: 1, duration: 1, ease: 'power3.out' }
                );
            }

            // Cards đổ vào từ phải với stagger
            if (grid) {
                const cards = grid.querySelectorAll('.sub-service-card');
                marketingTL.fromTo(
                    cards,
                    { 
                        opacity: 0, 
                        x: 150, 
                        scale: 0.8,
                        rotation: 5
                    },
                    { 
                        opacity: 1, 
                        x: 0, 
                        scale: 1,
                        rotation: 0,
                        duration: 0.8,
                        ease: 'back.out(1.2)',
                        stagger: {
                            amount: 0.6,
                            from: 'start'
                        }
                    },
                    '-=0.5'
                );
            }
        }

        // Hiệu ứng đổ vào đặc biệt cho IT Solutions - từ trái sang phải
        if (itServicesRef.current) {
            const itSection = itServicesRef.current;
            const header = itSection.querySelector('.service-section-header');
            const grid = itSection.querySelector('.it-grid');
            
            // Thêm class animate cho background reveal
            ScrollTrigger.create({
                trigger: itSection,
                start: 'top 80%',
                onEnter: () => itSection.classList.add('animate'),
            });

            // Tạo timeline cho section này
            const itTL = gsap.timeline({
                scrollTrigger: {
                    trigger: itSection,
                    start: 'top 80%',
                    toggleActions: 'play none none reverse',
                }
            });

            // Header slide từ trái
            if (header) {
                itTL.fromTo(
                    header,
                    { opacity: 0, x: -100, scale: 0.9 },
                    { opacity: 1, x: 0, scale: 1, duration: 1, ease: 'power3.out' }
                );
            }

            // Cards đổ vào từ trái với stagger
            if (grid) {
                const cards = grid.querySelectorAll('.sub-service-card');
                itTL.fromTo(
                    cards,
                    { 
                        opacity: 0, 
                        x: -150, 
                        scale: 0.8,
                        rotation: -5
                    },
                    { 
                        opacity: 1, 
                        x: 0, 
                        scale: 1,
                        rotation: 0,
                        duration: 0.8,
                        ease: 'back.out(1.2)',
                        stagger: {
                            amount: 0.6,
                            from: 'end'
                        }
                    },
                    '-=0.5'
                );
            }
        }

        // Hiệu ứng đổ vào đặc biệt cho Why Choose Us - từ dưới lên với scale
        if (whyRef.current) {
            const whySection = whyRef.current;
            const header = whySection.querySelector('.section-header');
            const grid = whySection.querySelector('.why-grid');
            
            // Thêm class animate cho background reveal
            ScrollTrigger.create({
                trigger: whySection,
                start: 'top 80%',
                onEnter: () => whySection.classList.add('animate'),
            });

            // Tạo timeline cho section này
            const whyTL = gsap.timeline({
                scrollTrigger: {
                    trigger: whySection,
                    start: 'top 80%',
                    toggleActions: 'play none none reverse',
                }
            });

            // Header từ dưới lên với scale
            if (header) {
                whyTL.fromTo(
                    header,
                    { opacity: 0, y: 80, scale: 0.8 },
                    { 
                        opacity: 1, 
                        y: 0, 
                        scale: 1, 
                        duration: 1.2, 
                        ease: 'elastic.out(1, 0.5)' 
                    }
                );
            }

            // Cards đổ vào từ dưới với scale và rotation
            if (grid) {
                const cards = grid.querySelectorAll('.why-card');
                whyTL.fromTo(
                    cards,
                    { 
                        opacity: 0, 
                        y: 120, 
                        scale: 0.6,
                        rotation: 10
                    },
                    { 
                        opacity: 1, 
                        y: 0, 
                        scale: 1,
                        rotation: 0,
                        duration: 1,
                        ease: 'back.out(1.4)',
                        stagger: {
                            amount: 0.8,
                            from: 'center'
                        }
                    },
                    '-=0.7'
                );
            }
        }

        // Animate service cards với stagger effect
        const serviceCards = document.querySelectorAll('.service-card, .sub-service-card');
        if (serviceCards && serviceCards.length > 0) {
            serviceCards.forEach((card, index) => {
                gsap.fromTo(
                    card,
                    { opacity: 0, y: 50, scale: 0.95 },
                    {
                        opacity: 1,
                        y: 0,
                        scale: 1,
                        duration: 0.8,
                        ease: 'power2.out',
                        scrollTrigger: {
                            trigger: card,
                            start: 'top 90%',
                            toggleActions: 'play none none reverse',
                        },
                        delay: index * 0.1,
                    }
                );
            });
        }

        // Animate section headers
        const sectionHeaders = document.querySelectorAll('.service-section-header');
        if (sectionHeaders && sectionHeaders.length > 0) {
            sectionHeaders.forEach((header) => {
                gsap.fromTo(
                    header,
                    { opacity: 0, y: 40 },
                    {
                        opacity: 1,
                        y: 0,
                        duration: 1,
                        ease: 'power2.out',
                        scrollTrigger: {
                            trigger: header,
                            start: 'top 90%',
                            toggleActions: 'play none none reverse',
                        },
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
                <div className="hero-background-overlay"></div>
                <div className="container">
                    <div className="hero-content">
                        <p className="hero-tagline">{t.companyTagline}</p>
                        <h1 className="hero-title">{t.companyName}</h1>
                        <p className="hero-description">{t.companyDescription}</p>
                        <a href="#contact" className="btn-primary cta-button-contact">{t.contactUs}</a>
                    </div>
                </div>
            </section>

            {/* Website Design Developer Section */}
            <section className="company-services website-services" ref={websiteServicesRef}>
                <div className="container">
                    <div className="service-section-header">
                        <h2>{t.serviceWebsiteTitle}</h2>
                        <p>{t.serviceWebsiteDesc}</p>
                    </div>
                    <div className="services-grid">
                        <div className="service-card">
                            <div className="service-icon">
                                <i className="bi bi-graph-up-arrow"></i>
                            </div>
                            <h3>{t.serviceWebsiteMarketingTitle}</h3>
                            <p>{t.serviceWebsiteMarketingDesc}</p>
                        </div>
                        <div className="service-card">
                            <div className="service-icon">
                                <i className="bi bi-palette"></i>
                            </div>
                            <h3>{t.serviceUXUITitle}</h3>
                            <p>{t.serviceUXUIDesc}</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Digital Marketing Service Section */}
            <section className="company-services marketing-services" ref={marketingServicesRef}>
                <div className="container">
                    <div className="service-section-header">
                        <h2>{t.serviceMarketingTitle}</h2>
                        <p>{t.serviceMarketingDesc}</p>
                    </div>
                    <div className="services-grid marketing-grid">
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-search"></i>
                            </div>
                            <h3>{t.serviceSEOTitle}</h3>
                            <p>{t.serviceSEODesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-megaphone"></i>
                            </div>
                            <h3>{t.servicePaidAdsTitle}</h3>
                            <p>{t.servicePaidAdsDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-facebook"></i>
                            </div>
                            <h3>{t.serviceSocialMediaTitle}</h3>
                            <p>{t.serviceSocialMediaDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-envelope"></i>
                            </div>
                            <h3>{t.serviceEmailMarketingTitle}</h3>
                            <p>{t.serviceEmailMarketingDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-star"></i>
                            </div>
                            <h3>{t.serviceBrandStrategyTitle}</h3>
                            <p>{t.serviceBrandStrategyDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-diagram-3"></i>
                            </div>
                            <h3>{t.serviceMarketingStrategiesTitle}</h3>
                            <p>{t.serviceMarketingStrategiesDesc}</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* IT Solutions Section */}
            <section className="company-services it-services" ref={itServicesRef}>
                <div className="container">
                    <div className="service-section-header">
                        <h2>{t.serviceITTitle}</h2>
                        <p>{t.serviceITDesc}</p>
                    </div>
                    <div className="services-grid it-grid">
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-card-text"></i>
                            </div>
                            <h3>{t.serviceVcardTitle}</h3>
                            <p>{t.serviceVcardDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-gear"></i>
                            </div>
                            <h3>{t.serviceManagementSystemTitle}</h3>
                            <p>{t.serviceManagementSystemDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-qr-code-scan"></i>
                            </div>
                            <h3>{t.serviceCheckinSystemTitle}</h3>
                            <p>{t.serviceCheckinSystemDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-people"></i>
                            </div>
                            <h3>{t.serviceCRMTitle}</h3>
                            <p>{t.serviceCRMDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-database"></i>
                            </div>
                            <h3>{t.serviceDatabaseTitle}</h3>
                            <p>{t.serviceDatabaseDesc}</p>
                        </div>
                        <div className="sub-service-card">
                            <div className="service-icon">
                                <i className="bi bi-calendar-event"></i>
                            </div>
                            <h3>{t.serviceEventSolutionsTitle}</h3>
                            <p>{t.serviceEventSolutionsDesc}</p>
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

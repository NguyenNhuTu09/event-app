import React, { useEffect, useRef } from 'react';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import './SolutionsPage.css';

gsap.registerPlugin(ScrollTrigger);

const SolutionsPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;
    const bannerRef = useRef(null);
    const introRef = useRef(null);
    const solutionsRef = useRef(null);
    const flowRef = useRef(null);
    const ctaRef = useRef(null);

    useEffect(() => {
        const banner = bannerRef.current;
        if (!banner) return;

        // Banner animations với GSAP
        const title = banner.querySelector('.banner-title');
        const description = banner.querySelector('.banner-description');
        const particles = banner.querySelectorAll('.banner-particle');

        // Timeline cho banner
        const tl = gsap.timeline();

        // Animate title
        if (title) {
            tl.fromTo(
                title,
                { opacity: 0, y: 50, scale: 0.9 },
                { opacity: 1, y: 0, scale: 1, duration: 1, ease: 'power3.out' }
            );
        }

        // Animate description
        if (description) {
            tl.fromTo(
                description,
                { opacity: 0, y: 30 },
                { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out' },
                '-=0.5'
            );
        }

        // Animate particles
        particles.forEach((particle, index) => {
            tl.fromTo(
                particle,
                { opacity: 0, scale: 0, rotation: 0 },
                {
                    opacity: 1,
                    scale: 1,
                    rotation: 360,
                    duration: 0.6,
                    ease: 'back.out(1.7)',
                },
                '-=0.3'
            );
        });

        // Parallax effect khi scroll
        gsap.to(banner, {
            scrollTrigger: {
                trigger: banner,
                start: 'top top',
                end: 'bottom top',
                scrub: true,
            },
            y: 100,
            opacity: 0.8,
        });

        // Fade in sections khi scroll
        const sections = [introRef, solutionsRef, flowRef, ctaRef];
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

        // Animate solution cards
        const solutionsSection = solutionsRef.current;
        const solutionCards = solutionsSection?.querySelectorAll('.solution-card');
        if (solutionCards) {
            solutionCards.forEach((card, index) => {
                gsap.fromTo(
                    card,
                    { opacity: 0, x: -50, rotationY: -15 },
                    {
                        opacity: 1,
                        x: 0,
                        rotationY: 0,
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

        // Animate flow steps
        const flowSection = flowRef.current;
        const flowSteps = flowSection?.querySelectorAll('.flow-step');
        if (flowSteps) {
            flowSteps.forEach((step, index) => {
                gsap.fromTo(
                    step,
                    { opacity: 0, scale: 0.8, y: 30 },
                    {
                        opacity: 1,
                        scale: 1,
                        y: 0,
                        duration: 0.6,
                        ease: 'back.out(1.7)',
                        scrollTrigger: {
                            trigger: step,
                            start: 'top 90%',
                            toggleActions: 'play none none reverse',
                        },
                        delay: index * 0.15,
                    }
                );
            });
        }

        return () => {
            ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
        };
    }, []);

    const solutions = [
        {
            name: t.solutionQRName,
            description: t.solutionQRDescription,
            features: t.solutionQRFeatures,
            icon: 'bi-qr-code-scan',
        },
        {
            name: t.solutionBookingName,
            description: t.solutionBookingDescription,
            features: t.solutionBookingFeatures,
            icon: 'bi-calendar-check',
        },
        {
            name: t.solutionAIName,
            description: t.solutionAIDescription,
            features: t.solutionAIFeatures,
            icon: 'bi-cpu',
        },
        {
            name: t.solutionAnalyticsName,
            description: t.solutionAnalyticsDescription,
            features: t.solutionAnalyticsFeatures,
            icon: 'bi-graph-up-arrow',
        },
    ];

    const flowSteps = [
        { step: '1', title: t.flowStep1Title, icon: 'bi-person-plus', description: t.flowStep1Desc },
        { step: '2', title: t.flowStep2Title, icon: 'bi-qr-code', description: t.flowStep2Desc },
        { step: '3', title: t.flowStep3Title, icon: 'bi-check-circle', description: t.flowStep3Desc },
        { step: '4', title: t.flowStep4Title, icon: 'bi-calendar-event', description: t.flowStep4Desc },
        { step: '5', title: t.flowStep5Title, icon: 'bi-people', description: t.flowStep5Desc },
        { step: '6', title: t.flowStep6Title, icon: 'bi-bar-chart', description: t.flowStep6Desc },
    ];

    return (
        <div className="solutions-page">
            {/* Banner Section với GSAP Animation */}
            <section className="solutions-banner" ref={bannerRef}>
                <div className="banner-background">
                    <div className="banner-gradient"></div>
                    <div className="banner-particles">
                        <div className="banner-particle particle-1"></div>
                        <div className="banner-particle particle-2"></div>
                        <div className="banner-particle particle-3"></div>
                        <div className="banner-particle particle-4"></div>
                        <div className="banner-particle particle-5"></div>
                    </div>
                </div>
                <div className="container">
                    <div className="banner-content">
                        <h1 className="banner-title">{t.solutionsBannerTitle}</h1>
                        <p className="banner-description">{t.solutionsBannerDescription}</p>
                    </div>
                </div>
            </section>

            {/* Section 1 - Giới thiệu tổng quan */}
            <section className="solutions-intro" ref={introRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.solutionsIntroTitle}</h2>
                        <p>{t.solutionsIntroDescription}</p>
                    </div>
                    <div className="intro-features">
                        <div className="intro-feature">
                            <div className="feature-icon">
                                <i className="bi bi-lightning-charge"></i>
                            </div>
                            <h3>{t.solutionsIntroFeature1Title}</h3>
                            <p>{t.solutionsIntroFeature1Desc}</p>
                        </div>
                        <div className="intro-feature">
                            <div className="feature-icon">
                                <i className="bi bi-shield-check"></i>
                            </div>
                            <h3>{t.solutionsIntroFeature2Title}</h3>
                            <p>{t.solutionsIntroFeature2Desc}</p>
                        </div>
                        <div className="intro-feature">
                            <div className="feature-icon">
                                <i className="bi bi-speedometer2"></i>
                            </div>
                            <h3>{t.solutionsIntroFeature3Title}</h3>
                            <p>{t.solutionsIntroFeature3Desc}</p>
                        </div>
                        <div className="intro-feature">
                            <div className="feature-icon">
                                <i className="bi bi-graph-up"></i>
                            </div>
                            <h3>{t.solutionsIntroFeature4Title}</h3>
                            <p>{t.solutionsIntroFeature4Desc}</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* Section 2 - Các nhóm giải pháp */}
            <section className="solutions-list" ref={solutionsRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.solutionsListTitle}</h2>
                        <p>{t.solutionsListSubtitle}</p>
                    </div>
                    <div className="solutions-grid">
                        {solutions.map((solution, index) => (
                            <div key={index} className="solution-card">
                                <div className="solution-icon">
                                    <i className={`bi ${solution.icon}`}></i>
                                </div>
                                <h3>{solution.name}</h3>
                                <p className="solution-description">{solution.description}</p>
                                <div className="solution-features">
                                    <h4>{t.solutionsKeyFeatures}</h4>
                                    <p>{solution.features}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Section 3 - Mô phỏng giao diện (Flow Chart) */}
            <section className="solutions-flow" ref={flowRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.solutionsFlowTitle}</h2>
                        <p>{t.solutionsFlowSubtitle}</p>
                    </div>
                    <div className="flow-container">
                        {flowSteps.map((step, index) => (
                            <React.Fragment key={index}>
                                <div className="flow-step">
                                    <div className="flow-step-number">{step.step}</div>
                                    <div className="flow-step-icon">
                                        <i className={`bi ${step.icon}`}></i>
                                    </div>
                                    <h3>{step.title}</h3>
                                    <p>{step.description}</p>
                                </div>
                                {index < flowSteps.length - 1 && (
                                    <div className="flow-arrow">
                                        <i className="bi bi-arrow-right"></i>
                                    </div>
                                )}
                            </React.Fragment>
                        ))}
                    </div>
                </div>
            </section>

            {/* Section 4 - CTA */}
            <section className="solutions-cta" ref={ctaRef}>
                <div className="container">
                    <div className="cta-content">
                        <h2>{t.solutionsCTATitle}</h2>
                        <p>{t.solutionsCTADescription}</p>
                        <div className="cta-buttons">
                            <a href="/contact" className="btn-cta btn-cta-primary">
                                {t.solutionsCTAButton1}
                                <i className="bi bi-arrow-right"></i>
                            </a>
                            <a href="/company" className="btn-cta btn-cta-secondary">
                                {t.solutionsCTAButton2}
                                <i className="bi bi-info-circle"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default SolutionsPage;










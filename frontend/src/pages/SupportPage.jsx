import React, { useState, useEffect, useRef } from 'react';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import './SupportPage.css';

gsap.registerPlugin(ScrollTrigger);

const SupportPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    // FAQ state
    const [openIndex, setOpenIndex] = useState(null);
    const toggle = (i) => setOpenIndex(openIndex === i ? null : i);

    // Contact form state
    const [form, setForm] = useState({
        name: '',
        email: '',
        phone: '',
        topic: t.supportFormTopicDefault,
        message: '',
    });
    const [sending, setSending] = useState(false);
    const [error, setError] = useState('');
    const [sent, setSent] = useState(false);

    // Refs for animations
    const heroRef = useRef(null);
    const faqRef = useRef(null);
    const formRef = useRef(null);
    const contactRef = useRef(null);

    // FAQ data
    const faqs = [
        {
            question: t.supportFAQ1Question,
            answer: t.supportFAQ1Answer,
        },
        {
            question: t.supportFAQ2Question,
            answer: t.supportFAQ2Answer,
        },
        {
            question: t.supportFAQ3Question,
            answer: t.supportFAQ3Answer,
        },
        {
            question: t.supportFAQ4Question,
            answer: t.supportFAQ4Answer,
        },
        {
            question: t.supportFAQ5Question,
            answer: t.supportFAQ5Answer,
        },
    ];

    // GSAP Animations
    useEffect(() => {
        // Hero animation với Timeline
        const hero = heroRef.current;
        if (!hero) return;

        const title = hero.querySelector('.support-hero-title');
        const description = hero.querySelector('.support-hero-description');
        const particles = hero.querySelectorAll('.support-particle');

        // Timeline cho banner
        const tl = gsap.timeline();

        // Animate title với scale và rotation
        if (title) {
            tl.fromTo(
                title,
                { opacity: 0, y: 50, scale: 0.9, rotationX: -15 },
                {
                    opacity: 1,
                    y: 0,
                    scale: 1,
                    rotationX: 0,
                    duration: 1.2,
                    ease: 'power3.out'
                }
            );
        }

        // Animate description với stagger
        if (description) {
            tl.fromTo(
                description,
                { opacity: 0, y: 30, scale: 0.95 },
                {
                    opacity: 1,
                    y: 0,
                    scale: 1,
                    duration: 0.9,
                    ease: 'power2.out'
                },
                '-=0.6'
            );
        }

        // Animate particles nếu có
        if (particles && particles.length > 0) {
            particles.forEach((particle, index) => {
                tl.fromTo(
                    particle,
                    { opacity: 0, scale: 0, rotation: 0, y: 20 },
                    {
                        opacity: 1,
                        scale: 1,
                        rotation: 360,
                        y: 0,
                        duration: 0.7,
                        ease: 'back.out(1.7)',
                    },
                    '-=0.4'
                );
            });
        }

        // Parallax effect khi scroll
        gsap.to(hero, {
            scrollTrigger: {
                trigger: hero,
                start: 'top top',
                end: 'bottom top',
                scrub: true,
            },
            y: 80,
            opacity: 0.85,
            ease: 'power1.inOut',
        });

        // Fade out hero content khi scroll
        const heroContent = hero.querySelector('.support-hero-content');
        if (heroContent) {
            gsap.to(heroContent, {
                scrollTrigger: {
                    trigger: hero,
                    start: 'top top',
                    end: 'center top',
                    scrub: true,
                },
                opacity: 0,
                y: -30,
                ease: 'power1.out',
            });
        }

        // Section animations
        const sections = [faqRef, formRef, contactRef];
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

        return () => {
            ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
        };
    }, []);

    // Handle form input change
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({
            ...prev,
            [name]: value
        }));
        setError('');
    };

    // Handle form submit
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSending(true);

        // Validate form
        if (!form.name || !form.email || !form.message) {
            setError(t.supportFormErrorRequired);
            setSending(false);
            return;
        }

        // Simulate API call
        setTimeout(() => {
            console.log('Support form submitted:', form);
            setSending(false);
            setSent(true);

            // Reset form
            setForm({
                name: '',
                email: '',
                phone: '',
                topic: t.supportFormTopicDefault,
                message: '',
            });

            // Hide success message after 5 seconds
            setTimeout(() => {
                setSent(false);
            }, 5000);
        }, 1000);
    };

    return (
        <div className="support-page">
            {/* Hero Section */}
            <section className="support-hero" ref={heroRef}>
                <div className="support-hero-background">
                    <div className="support-hero-gradient"></div>
                    <div className="support-hero-particles">
                        <div className="support-particle particle-1"></div>
                        <div className="support-particle particle-2"></div>
                        <div className="support-particle particle-3"></div>
                        <div className="support-particle particle-4"></div>
                        <div className="support-particle particle-5"></div>
                    </div>
                </div>
                <div className="container">
                    <div className="support-hero-content">
                        <h1 className="support-hero-title">{t.supportHeroTitle}</h1>
                        <p className="support-hero-description">{t.supportHeroDescription}</p>
                    </div>
                </div>
            </section>

            {/* FAQ Section */}
            <section className="support-faq" ref={faqRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.supportFAQTitle}</h2>
                        <p>{t.supportFAQSubtitle}</p>
                    </div>
                    <div className="faq-list">
                        {faqs.map((faq, index) => (
                            <div
                                key={index}
                                className={`faq-item ${openIndex === index ? 'active' : ''}`}
                            >
                                <button
                                    className="faq-question"
                                    onClick={() => toggle(index)}
                                >
                                    <span>{faq.question}</span>
                                    <i className={`bi ${openIndex === index ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
                                </button>
                                <div className="faq-answer">
                                    <p>{faq.answer}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Contact Form Section */}
            <section className="support-form-section" ref={formRef}>
                <div className="container">
                    <div className="support-form-container">
                        <div className="form-header">
                            <h2>{t.supportFormTitle}</h2>
                            <p>{t.supportFormSubtitle}</p>
                        </div>

                        {sent && (
                            <div className="success-message">
                                <i className="bi bi-check-circle"></i>
                                <p>{t.supportFormSuccess}</p>
                            </div>
                        )}

                        {error && (
                            <div className="error-message">
                                <i className="bi bi-exclamation-circle"></i>
                                <p>{error}</p>
                            </div>
                        )}

                        <form className="support-form" onSubmit={handleSubmit}>
                            <div className="form-row">
                                <div className="form-group">
                                    <label htmlFor="name">
                                        <i className="bi bi-person"></i>
                                        {t.supportFormName}
                                    </label>
                                    <input
                                        type="text"
                                        id="name"
                                        name="name"
                                        value={form.name}
                                        onChange={handleInputChange}
                                        placeholder={t.supportFormNamePlaceholder}
                                        required
                                    />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="email">
                                        <i className="bi bi-envelope"></i>
                                        {t.supportFormEmail}
                                    </label>
                                    <input
                                        type="email"
                                        id="email"
                                        name="email"
                                        value={form.email}
                                        onChange={handleInputChange}
                                        placeholder={t.supportFormEmailPlaceholder}
                                        required
                                    />
                                </div>
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label htmlFor="phone">
                                        <i className="bi bi-telephone"></i>
                                        {t.supportFormPhone}
                                    </label>
                                    <input
                                        type="tel"
                                        id="phone"
                                        name="phone"
                                        value={form.phone}
                                        onChange={handleInputChange}
                                        placeholder={t.supportFormPhonePlaceholder}
                                    />
                                </div>

                                <div className="form-group">
                                    <label htmlFor="topic">
                                        <i className="bi bi-tag"></i>
                                        {t.supportFormTopic}
                                    </label>
                                    <select
                                        id="topic"
                                        name="topic"
                                        value={form.topic}
                                        onChange={handleInputChange}
                                    >
                                        <option value={t.supportFormTopicDefault}>{t.supportFormTopicDefault}</option>
                                        <option value={t.supportFormTopic1}>{t.supportFormTopic1}</option>
                                        <option value={t.supportFormTopic2}>{t.supportFormTopic2}</option>
                                        <option value={t.supportFormTopic3}>{t.supportFormTopic3}</option>
                                        <option value={t.supportFormTopic4}>{t.supportFormTopic4}</option>
                                    </select>
                                </div>
                            </div>

                            <div className="form-group">
                                <label htmlFor="message">
                                    <i className="bi bi-chat-left-text"></i>
                                    {t.supportFormMessage}
                                </label>
                                <textarea
                                    id="message"
                                    name="message"
                                    value={form.message}
                                    onChange={handleInputChange}
                                    placeholder={t.supportFormMessagePlaceholder}
                                    rows={6}
                                    required
                                ></textarea>
                            </div>

                            <button
                                type="submit"
                                className="btn-submit"
                                disabled={sending}
                            >
                                {sending ? (
                                    <>
                                        <span className="spinner"></span>
                                        {t.supportFormSending}
                                    </>
                                ) : (
                                    <>
                                        <i className="bi bi-send"></i>
                                        {t.supportFormSubmit}
                                    </>
                                )}
                            </button>
                        </form>
                    </div>
                </div>
            </section>

            {/* Contact Info Section */}
            <section className="support-contact" ref={contactRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.supportContactTitle}</h2>
                        <p>{t.supportContactSubtitle}</p>
                    </div>
                    <div className="contact-info-grid">
                        <div className="contact-info-card">
                            <div className="contact-icon">
                                <i className="bi bi-telephone-fill"></i>
                            </div>
                            <h3>{t.supportContactPhone}</h3>
                            <p>+84 969 838 467</p>
                            <a href="tel:+84969838467" className="contact-link">
                                {t.supportContactCallNow}
                                <i className="bi bi-arrow-right"></i>
                            </a>
                        </div>

                        <div className="contact-info-card">
                            <div className="contact-icon">
                                <i className="bi bi-envelope-fill"></i>
                            </div>
                            <h3>{t.supportContactEmail}</h3>
                            <p>Huyen.dang@webie.com.vn</p>
                            <a href="mailto:Huyen.dang@webie.com.vn" className="contact-link">
                                {t.supportContactSendEmail}
                                <i className="bi bi-arrow-right"></i>
                            </a>
                        </div>

                        <div className="contact-info-card">
                            <div className="contact-icon">
                                <i className="bi bi-geo-alt-fill"></i>
                            </div>
                            <h3>{t.supportContactAddress}</h3>
                            <p>53th street 57, An Phu Ward,<br />Thu Duc City, HCMC</p>
                            <a href="https://maps.google.com" target="_blank" rel="noopener noreferrer" className="contact-link">
                                {t.supportContactViewMap}
                                <i className="bi bi-arrow-right"></i>
                            </a>
                        </div>
                    </div>

                    {/* Chat Support (Optional) */}
                    <div className="chat-support">
                        <div className="chat-support-content">
                            <i className="bi bi-chat-dots"></i>
                            <div>
                                <h3>{t.supportChatTitle}</h3>
                                <p>{t.supportChatDescription}</p>
                            </div>
                            <button className="btn-chat">
                                {t.supportChatButton}
                                <i className="bi bi-arrow-right"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default SupportPage;

import React from 'react';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import './CompanyPage.css';

const CompanyPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    return (
        <div className="company-page">
            {/* Hero Section */}
            <section className="company-hero">
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
            <section className="company-services">
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
            <section className="company-why">
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
            <section className="company-about">
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
            <section id="contact" className="company-contact">
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

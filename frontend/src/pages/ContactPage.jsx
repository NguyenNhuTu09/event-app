import React, { useState, useEffect, useRef } from 'react';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import './ContactPage.css';

const ContactPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;
    const contactRef = useRef(null);
    const formRef = useRef(null);

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        helpType: '',
        project: '',
        acceptTerms: false,
    });

    // Smooth scroll effect khi vào trang - scroll từ header xuống form
    useEffect(() => {
        // Scroll mượt từ đầu trang xuống form với offset cho header
        const timer = setTimeout(() => {
            if (formRef.current) {
                const headerHeight = 80; // Chiều cao header
                const elementPosition = formRef.current.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerHeight;

                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            }
        }, 300); // Delay một chút để đảm bảo DOM đã render

        return () => clearTimeout(timer);
    }, []);

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        // Xử lý submit form ở đây
        console.log('Form submitted:', formData);
        // Reset form
        setFormData({
            name: '',
            email: '',
            helpType: '',
            project: '',
            acceptTerms: false,
        });
    };

    return (
        <div className="contact-page" ref={contactRef}>
            <div className="contact-container">
                {/* Form Section - Left */}
                <div className="contact-form-section" ref={formRef}>
                    <form className="contact-form" onSubmit={handleSubmit}>
                        <div className="form-group">
                            <input
                                type="text"
                                name="name"
                                id="name"
                                placeholder={t.contactFormName}
                                value={formData.name}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <input
                                type="email"
                                name="email"
                                id="email"
                                placeholder={t.contactFormEmail}
                                value={formData.email}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <div className="select-wrapper">
                                <select
                                    name="helpType"
                                    id="helpType"
                                    value={formData.helpType}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="" disabled>{t.contactFormHelp}</option>
                                    <option value="website">{t.contactHelpWebsite}</option>
                                    <option value="marketing">{t.contactHelpMarketing}</option>
                                    <option value="it">{t.contactHelpIT}</option>
                                    <option value="other">{t.contactHelpOther}</option>
                                </select>
                                <i className="bi bi-chevron-down"></i>
                            </div>
                        </div>

                        <div className="form-group">
                            <textarea
                                name="project"
                                id="project"
                                placeholder={t.contactFormProject}
                                value={formData.project}
                                onChange={handleInputChange}
                                rows={6}
                                required
                            ></textarea>
                        </div>

                        <div className="form-group checkbox-group">
                            <input
                                type="checkbox"
                                name="acceptTerms"
                                id="acceptTerms"
                                checked={formData.acceptTerms}
                                onChange={handleInputChange}
                                required
                            />
                            <label htmlFor="acceptTerms">
                                {t.contactFormTerms}
                            </label>
                        </div>

                        <button type="submit" className="submit-btn">
                            {t.contactFormSend}
                        </button>
                    </form>
                </div>

                {/* Contact Info Section - Right */}
                <div className="contact-info-section">
                    <div className="contact-info-item">
                        <h2 className="contact-info-title">{t.contactCall}</h2>
                        <p className="contact-info-text">+84 969 838 467</p>
                    </div>

                    <div className="contact-info-item">
                        <h2 className="contact-info-title">{t.contactEmail}</h2>
                        <p className="contact-info-text">
                            {t.contactEmailDesc} <br />
                            <a href="mailto:Huyen.dang@webie.com.vn">Huyen.dang@webie.com.vn</a>
                        </p>
                    </div>

                    <div className="contact-info-item">
                        <h2 className="contact-info-title">{t.contactAddress}</h2>
                        <p className="contact-info-text">
                            53th street 57, An Phu Ward,<br />
                            Thu Duc City, HCMC
                        </p>
                    </div>

                    <div className="contact-social">
                        <a href="#" className="social-icon" aria-label="Facebook">
                            <i className="bi bi-facebook"></i>
                        </a>
                        <a href="#" className="social-icon" aria-label="LinkedIn">
                            <i className="bi bi-linkedin"></i>
                        </a>
                        <a href="mailto:Huyen.dang@webie.com.vn" className="social-icon" aria-label="Email">
                            <i className="bi bi-envelope"></i>
                        </a>
                    </div>

                    <div className="contact-copyright">
                        <p>All content © copyright 2024 Webie Vietnam. All rights reserved.</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ContactPage;













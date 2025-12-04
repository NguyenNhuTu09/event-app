import React, { useState, useEffect, useRef } from 'react';
import { gsap } from 'gsap';
import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';
import './EventRegistrationModal.css';

const EventRegistrationModal = ({ isOpen, event, onClose, onSubmit }) => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    const modalRef = useRef(null);
    const modalContentRef = useRef(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitSuccess, setSubmitSuccess] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
    });

    // Reset form when modal opens/closes
    useEffect(() => {
        if (isOpen) {
            setFormData({ name: '', email: '', phone: '' });
            setSubmitSuccess(false);
            setIsSubmitting(false);

            // GSAP animation for modal
            setTimeout(() => {
                if (modalRef.current && modalContentRef.current) {
                    gsap.fromTo(modalRef.current,
                        { opacity: 0 },
                        { opacity: 1, duration: 0.3 }
                    );
                    gsap.fromTo(modalContentRef.current,
                        { opacity: 0, y: 50, scale: 0.9 },
                        { opacity: 1, y: 0, scale: 1, duration: 0.5, ease: 'back.out(1.2)' }
                    );
                }
            }, 10);
        }
    }, [isOpen]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);

        // Simulate API call
        setTimeout(() => {
            console.log('Registration submitted:', {
                event: event,
                ...formData
            });

            // Simulate email notification
            console.log(`Email sent to ${formData.email}:`, {
                subject: `Xác nhận đăng ký tham gia ${event.title}`,
                body: `Xin chào ${formData.name},\n\nCảm ơn bạn đã đăng ký tham gia sự kiện "${event.title}".\n\nThông tin sự kiện:\n- Ngày: ${event.date}\n- Giờ: ${event.time}\n- Địa điểm: ${event.location}\n\nChúng tôi sẽ gửi thêm thông tin chi tiết qua email trước ngày sự kiện.\n\nTrân trọng,\nWebie Event Team`
            });

            // Call parent onSubmit if provided
            if (onSubmit) {
                onSubmit({ event, ...formData });
            }

            setIsSubmitting(false);
            setSubmitSuccess(true);

            // Auto close modal after 3 seconds
            setTimeout(() => {
                handleClose();
            }, 3000);
        }, 1500);
    };

    const handleClose = () => {
        if (modalRef.current && modalContentRef.current) {
            gsap.to(modalContentRef.current, {
                opacity: 0,
                y: 30,
                scale: 0.95,
                duration: 0.3,
                ease: 'power2.in',
                onComplete: () => {
                    gsap.to(modalRef.current, {
                        opacity: 0,
                        duration: 0.2,
                        onComplete: () => {
                            onClose();
                        }
                    });
                }
            });
        } else {
            onClose();
        }
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString(language === 'vi' ? 'vi-VN' : 'en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    if (!isOpen || !event) return null;

    return (
        <div
            className="event-modal-overlay"
            onClick={handleClose}
            ref={modalRef}
        >
            <div
                className="event-modal-content"
                onClick={(e) => e.stopPropagation()}
                ref={modalContentRef}
            >
                <button className="event-modal-close" onClick={handleClose}>
                    <i className="bi bi-x-lg"></i>
                </button>

                {!submitSuccess ? (
                    <>
                        <div className="event-modal-header">
                            <div className="event-modal-icon-wrapper">
                                <i className="bi bi-calendar-check"></i>
                            </div>
                            <div className="event-modal-header-content">
                                <h2>{t.eventRegistrationTitle}</h2>
                                <p className="event-modal-event-name">{event.title}</p>
                            </div>
                        </div>

                        <div className="event-modal-info">
                            <div className="event-info-item">
                                <div className="event-info-icon">
                                    <i className="bi bi-calendar-event"></i>
                                </div>
                                <div className="event-info-content">
                                    <span className="event-info-label">{language === 'vi' ? 'Ngày' : 'Date'}</span>
                                    <span className="event-info-value">{formatDate(event.date)}</span>
                                </div>
                            </div>
                            <div className="event-info-item">
                                <div className="event-info-icon">
                                    <i className="bi bi-clock"></i>
                                </div>
                                <div className="event-info-content">
                                    <span className="event-info-label">{language === 'vi' ? 'Giờ' : 'Time'}</span>
                                    <span className="event-info-value">{event.time}</span>
                                </div>
                            </div>
                            <div className="event-info-item">
                                <div className="event-info-icon">
                                    <i className="bi bi-geo-alt"></i>
                                </div>
                                <div className="event-info-content">
                                    <span className="event-info-label">{language === 'vi' ? 'Địa điểm' : 'Location'}</span>
                                    <span className="event-info-value">{event.location}</span>
                                </div>
                            </div>
                        </div>

                        <form className="event-registration-form" onSubmit={handleSubmit}>
                            <div className="event-form-group">
                                <label htmlFor="name">
                                    <i className="bi bi-person"></i>
                                    {t.eventFormName}
                                </label>
                                <input
                                    type="text"
                                    id="name"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleInputChange}
                                    placeholder={t.eventFormNamePlaceholder}
                                    required
                                />
                            </div>

                            <div className="event-form-group">
                                <label htmlFor="email">
                                    <i className="bi bi-envelope"></i>
                                    {t.eventFormEmail}
                                </label>
                                <input
                                    type="email"
                                    id="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleInputChange}
                                    placeholder={t.eventFormEmailPlaceholder}
                                    required
                                />
                            </div>

                            <div className="event-form-group">
                                <label htmlFor="phone">
                                    <i className="bi bi-telephone"></i>
                                    {t.eventFormPhone}
                                </label>
                                <input
                                    type="tel"
                                    id="phone"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleInputChange}
                                    placeholder={t.eventFormPhonePlaceholder}
                                    required
                                />
                            </div>

                            <button
                                type="submit"
                                className="event-submit-btn"
                                disabled={isSubmitting}
                            >
                                {isSubmitting ? (
                                    <>
                                        <span className="event-spinner"></span>
                                        {t.eventFormSubmitting}
                                    </>
                                ) : (
                                    <>
                                        {t.eventFormSubmit}
                                        <i className="bi bi-arrow-right"></i>
                                    </>
                                )}
                            </button>
                        </form>
                    </>
                ) : (
                    <div className="event-success-message">
                        <div className="event-success-icon">
                            <i className="bi bi-check-circle-fill"></i>
                        </div>
                        <h3>{t.eventRegistrationSuccess}</h3>
                        <p>{t.eventRegistrationSuccessDesc}</p>
                        <div className="event-success-email">
                            <i className="bi bi-envelope-check"></i>
                            <span>{t.eventRegistrationEmailSent} <strong>{formData.email}</strong></span>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default EventRegistrationModal;


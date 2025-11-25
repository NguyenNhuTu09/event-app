import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { gsap } from 'gsap';
import { useLanguage } from "../context/LanguageContext";
import translations from "../translate/translations";
import { path } from "../utils/constant";
import './PartnerRegistrationPage.css';

// fun simulator creation random password
const generateRandomPassword = (length = 12) => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*';
    let password = '';
    for (let i = 0; i < length; i++) {
        password += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return password;
};

const PartnerRegistrationPage = () => {
    const navigate = useNavigate();
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;
    const pageRef = useRef(null);
    const formRef = useRef(null);

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitSuccess, setSubmitSuccess] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState({
        companyName: '',
        contactName: '',
        email: '',
        phone: '',
        industry: '',
        experience: '',
        agreement: false,
    });

    useEffect(() => {
        // Banner animation
        if (pageRef.current) {
            const container = pageRef.current.querySelector('.partner-registration-container');
            if (container) {
                const title = container.querySelector('h1');
                const subtitle = container.querySelector('p');

                const tl = gsap.timeline();
                if (title) {
                    tl.fromTo(
                        title,
                        { opacity: 0, y: 50, scale: 0.9 },
                        { opacity: 0.9, y: 0, scale: 1, duration: 1, ease: 'power3.out' }
                    );
                }
                if (subtitle) {
                    tl.fromTo(
                        subtitle,
                        { opacity: 0, y: 30 },
                        { opacity: 0.8, y: 0, duration: 0.8, ease: 'power2.out' },
                        '-=0.5'
                    );
                }
            }
        }

        // Form animation
        if (formRef.current) {
            gsap.fromTo(
                formRef.current,
                { opacity: 0, y: 50 },
                { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out', delay: 0.4 }
            );
        }
    }, []);

    const handleInputChange = (e) => {
        const { name, type, value, checked } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        // Validate (ví dụ đơn giản)
        if (!formData.companyName || !formData.contactName || !formData.email || !formData.phone) {
            setError(t.partnerFormValidationError || 'Vui lòng điền đầy đủ các trường bắt buộc.');
            return;
        }
        setIsSubmitting(true);

        try {
            // === Thay đoạn này bằng gọi API thật của bạn ===
            // Mô phỏng delay & tạo tài khoản với mật khẩu tạm thời
            const tempPassword = generateRandomPassword();
            await new Promise(res => setTimeout(res, 1000)); // mô phỏng network

            // Nếu dùng API, gửi formData + tempPassword và chờ kết quả
            // const result = await api.post('/partner/register', { ...formData, tempPassword });

            // Giả sử thành công:
            setSubmitSuccess(true);
            setIsSubmitting(false);

            // (Bạn có thể lưu tempPassword vào state để hiển thị hoặc không)
        } catch (err) {
            console.error(err);
            setError(t.partnerFormSubmitError || 'Có lỗi xảy ra khi gửi. Vui lòng thử lại.');
            setIsSubmitting(false);
        }
    };

    return (
        <div className="partner-registration-wrapper">
            {/* Banner Section */}
            <section className='partner-registration-banner' ref={pageRef}>
                <div className='banner-background-overlay'></div>
                <div className='container'>
                    <div className='partner-registration-container'>
                        <h1>{t.partnerRegistrationTitle || 'Đăng ký trở thành nhà tổ chức sự kiện'}</h1>
                        <p>{t.partnerRegistrationSubtitle || 'Hãy cùng chúng tôi lan tỏa các sự kiện chất lượng này '}</p>
                    </div>
                </div>
            </section>

            {/* Form Section */}
            <div className='partner-form-section'>
                <div className='partner-form-container container'>
                    {!submitSuccess ? (
                        <form className='partner-registration-form' onSubmit={handleSubmit} ref={formRef}>
                            <h2>{t.partnerFormHeading || 'Thông tin đăng ký'}</h2>

                            <div className="form-group">
                                <label htmlFor="companyName">{t.partnerFormCompanyName}<span className='required'>*</span></label>
                                <input
                                    type='text'
                                    id='companyName'
                                    name='companyName'
                                    value={formData.companyName}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="industry">{t.partnerFormIndustry}<span className='required'>*</span></label>
                                <select id='industry' name='industry' value={formData.industry} onChange={handleInputChange} required>
                                    <option value="Technology">{t.industryTech || 'Công nghệ'}</option>
                                    <option value="Marketing">{t.industryMarketing || 'Marketing'}</option>
                                    <option value="Education">{t.industryEducation || 'Giáo dục'}</option>
                                    <option value="Business">{t.industryBusiness || 'Kinh doanh'}</option>
                                    <option value="Other">{t.industryOther || 'Khác'}</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="contactName">{t.partnerFormContactName}<span className='required'>*</span></label>
                                <input
                                    type="text"
                                    id="contactName"
                                    name="contactName"
                                    value={formData.contactName}
                                    onChange={handleInputChange}
                                    placeholder={t.partnerFormContactNamePlaceholder || "Tên người đại diện"}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="email">{t.partnerFormEmail} <span className="required">*</span></label>
                                <input
                                    type="email"
                                    id="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleInputChange}
                                    placeholder={t.partnerFormEmailPlaceholder || "Email (Sẽ dùng làm tài khoản)"}
                                    required
                                />
                            </div>

                            {/* Phone */}
                            <div className="form-group">
                                <label htmlFor="phone">{t.partnerFormPhone} <span className="required">*</span></label>
                                <input
                                    type="tel"
                                    id="phone"
                                    name="phone"
                                    value={formData.phone}
                                    onChange={handleInputChange}
                                    placeholder={t.partnerFormPhonePlaceholder || "Số điện thoại liên hệ"}
                                    required
                                />
                            </div>

                            {/* Experience */}
                            <div className="form-group">
                                <label htmlFor="experience">{t.partnerFormExperience} <span className="required">*</span></label>
                                <textarea
                                    id="experience"
                                    name="experience"
                                    rows="3"
                                    value={formData.experience}
                                    onChange={handleInputChange}
                                    placeholder={t.partnerFormExperiencePlaceholder || "Mô tả ngắn gọn về kinh nghiệm tổ chức sự kiện của bạn."}
                                    required
                                />
                            </div>

                            {/* Agreement Checkbox */}
                            <div className="form-group checkbox-group">
                                <input
                                    type="checkbox"
                                    id="agreement"
                                    name="agreement"
                                    checked={formData.agreement}
                                    onChange={handleInputChange}
                                    required
                                />
                                <label htmlFor="agreement">
                                    {t.partnerFormAgreementText || 'Tôi đồng ý với các Điều khoản & Chính sách Đối Tác.'} <span className="required">*</span>
                                </label>
                            </div>

                            {error && <div className="form-error">{error}</div>}

                            <button
                                type="submit"
                                className="submit-btn-partner"
                                disabled={isSubmitting || !formData.agreement}
                            >
                                {isSubmitting ? (
                                    <>
                                        <span className="spinner" /> {t.partnerFormSubmitting || 'Đang gửi...'}
                                    </>
                                ) : (
                                    t.partnerFormSubmit || 'Đăng Ký Ngay'
                                )}
                            </button>
                        </form>
                    ) : (
                        <div className="partner-success-message">
                            <i className="bi bi-send-check-fill success-icon"></i>
                            <h3>{t.partnerSuccessTitle || 'Đăng Ký Thành Công!'}</h3>
                            <p>{t.partnerSuccessDesc1 || 'Cảm ơn bạn đã đăng ký. Hệ thống đã tạo tài khoản và gửi thông tin đăng nhập tới'}</p>
                            <strong className="success-email-display">{formData.email}</strong>
                            <p>{t.partnerSuccessDesc2 || 'Vui lòng kiểm tra email (bao gồm cả thư mục Spam) để nhận Tên đăng nhập và Mật khẩu tạm thời.'}</p>
                            <button
                                onClick={() => navigate(path.PARTNER_LOGIN)}
                                className="go-to-login-btn"
                            >
                                {t.partnerSuccessLoginBtn || 'Đi đến Trang Đăng Nhập Đối Tác'}
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PartnerRegistrationPage;

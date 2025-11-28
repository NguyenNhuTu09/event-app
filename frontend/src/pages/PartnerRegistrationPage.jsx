import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import { gsap } from 'gsap';
import { useLanguage } from "../context/LanguageContext";
import translations from "../translate/translations";
import { path } from "../utils/constant";
import { authAPI } from "../service/api";
import { organizersAPI } from "../service/organizers";
import './PartnerRegistrationPage.css';

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
        // Clear error when user types
        if (error) {
            setError('');
        }
    };



    const validateForm = () => {
        // Validate required fields
        if (!formData.companyName.trim()) {
            return 'Vui lòng nhập tên công ty.';
        }
        if (!formData.contactName.trim()) {
            return 'Vui lòng nhập tên người đại diện.';
        }
        if (!formData.email.trim()) {
            return 'Vui lòng nhập email.';
        }
        // Email format validation
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData.email)) {
            return 'Email không hợp lệ.';
        }
        if (!formData.phone.trim()) {
            return 'Vui lòng nhập số điện thoại.';
        }
        // Phone format validation (Vietnamese phone numbers)
        const phoneRegex = /^(0|\+84)[1-9][0-9]{8,9}$/;
        if (!phoneRegex.test(formData.phone.replace(/\s/g, ''))) {
            return 'Số điện thoại không hợp lệ. Vui lòng nhập số điện thoại Việt Nam (10-11 chữ số).';
        }
        if (!formData.industry) {
            return 'Vui lòng chọn lĩnh vực hoạt động.';
        }
        if (!formData.experience.trim()) {
            return 'Vui lòng nhập mô tả kinh nghiệm.';
        }
        if (formData.experience.trim().length < 20) {
            return 'Mô tả kinh nghiệm phải có ít nhất 20 ký tự.';
        }

        if (!formData.agreement) {
            return 'Vui lòng đồng ý với các điều khoản và chính sách.';
        }
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // Validate form
        const validationError = validateForm();
        if (validationError) {
            setError(validationError);
            return;
        }

        setIsSubmitting(true);
        setError('');

        try {
            // Step 1: Generate a temporary password
            const tempPassword = Math.random().toString(36).slice(-12) +
                Math.random().toString(36).slice(-12).toUpperCase() +
                '!@#123';

            // Step 2: Register user account (or handle existing user)
            let userRegistered = false;
            let loginResponse = null;

            try {
                // Generate username from contact name
                const username = formData.contactName
                    .toLowerCase()
                    .normalize('NFD')
                    .replace(/[\u0300-\u036f]/g, '') // Remove accents
                    .replace(/[^a-z0-9]/g, '') // Remove special chars
                    .substring(0, 20) || 'user' + Date.now();

                await authAPI.register(
                    username,
                    formData.email,
                    tempPassword,
                    tempPassword
                );
                userRegistered = true;
            } catch (registerError) {
                // Check if user already exists
                const errorMsg = registerError.message?.toLowerCase() || '';
                if (errorMsg.includes('đã được sử dụng') ||
                    errorMsg.includes('already exists') ||
                    errorMsg.includes('email') && errorMsg.includes('exist')) {
                    // User exists - we need to inform them to login instead
                    throw new Error('Email này đã được sử dụng. Vui lòng đăng nhập hoặc sử dụng email khác.');
                } else {
                    throw registerError;
                }
            }

            // Step 3: Login to get token
            if (userRegistered) {
                try {
                    loginResponse = await authAPI.login(formData.email, tempPassword);
                } catch (loginError) {
                    throw new Error('Đăng nhập thất bại sau khi đăng ký. Vui lòng thử lại.');
                }
            } else {
                throw new Error('Không thể tạo tài khoản. Vui lòng thử lại.');
            }

            // Step 4: Save token
            const accessToken = loginResponse?.accessToken || loginResponse?.token;
            if (!accessToken) {
                throw new Error('Không nhận được token từ server. Vui lòng thử lại.');
            }

            localStorage.setItem('token', accessToken);
            if (loginResponse?.refreshToken) {
                localStorage.setItem('refreshToken', loginResponse.refreshToken);
            }

            // Step 5: Register as organizer
            const organizerData = {
                name: formData.contactName.trim(), // Tên người đại diện
                description: formData.experience.trim(),
                contactPhoneNumber: formData.phone.trim(),
                contactEmail: formData.email.trim(),
                logoUrl: 'null', // API expects string "null" or null
            };

            await organizersAPI.registerOrganizer(organizerData);

            // Success
            setSubmitSuccess(true);
            setIsSubmitting(false);
        } catch (err) {
            console.error('Registration error:', err);
            setError(err.message || t.partnerFormSubmitError || 'Có lỗi xảy ra khi gửi. Vui lòng thử lại.');
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
                                <label htmlFor="companyName">{t.partnerFormCompanyName || 'Tên công ty'}<span className='required'>*</span></label>
                                <input
                                    type='text'
                                    id='companyName'
                                    name='companyName'
                                    value={formData.companyName}
                                    onChange={handleInputChange}
                                    placeholder="Nhập tên công ty"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label htmlFor="industry">{t.partnerFormIndustry || 'Lĩnh vực hoạt động'}<span className='required'>*</span></label>
                                <select id='industry' name='industry' value={formData.industry} onChange={handleInputChange} required>
                                    <option value="">-- Chọn lĩnh vực --</option>
                                    <option value="Technology">{t.industryTech || 'Công nghệ'}</option>
                                    <option value="Marketing">{t.industryMarketing || 'Marketing'}</option>
                                    <option value="Education">{t.industryEducation || 'Giáo dục'}</option>
                                    <option value="Business">{t.industryBusiness || 'Kinh doanh'}</option>
                                    <option value="Other">{t.industryOther || 'Khác'}</option>
                                </select>
                            </div>

                            <div className="form-group">
                                <label htmlFor="contactName">{t.partnerFormContactName || 'Tên người đại diện'}<span className='required'>*</span></label>
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
                                <label htmlFor="email">{t.partnerFormEmail || 'Email Công việc'} <span className="required">*</span></label>
                                <input
                                    type="email"
                                    id="email"
                                    name="email"
                                    value={formData.email}
                                    onChange={handleInputChange}
                                    placeholder={t.partnerFormEmailPlaceholder || "Ví dụ: contact@webie.com"}
                                    required
                                />
                            </div>

                            {/* Phone */}
                            <div className="form-group">
                                <label htmlFor="phone">{t.partnerFormPhone || 'Số Điện thoại'} <span className="required">*</span></label>
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
                                <label htmlFor="experience">{t.partnerFormExperience || 'Mô tả kinh nghiệm'} <span className="required">*</span></label>
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

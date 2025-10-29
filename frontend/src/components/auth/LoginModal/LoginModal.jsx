import React, { useState, useCallback, useEffect } from "react";
import './LoginModal.css';
import { useLanguage } from "../../../context/LanguageContext";
import translations from "../../../translate/translations";

//  Màn hình chọn phương thức đăng nhập
const OptionsView = ({ t, onEmailClick }) => (
    <>
        <div className="modal-header-polished">
            <div className="header-text"><h2>{t.loginOfferTitle}</h2></div>
        </div>
        <div className="modal-body-polished">
            <div className="login-providers">
                <button className="provider-btn google">
                    <i className="bi bi-google"></i>
                    <span className="provider-text">{t.loginWithGoogle}</span>
                    <span className="recent-badge">{t.recentlyUsed}</span>
                </button>
                <button className="provider-btn email" onClick={onEmailClick}>
                    <i className="bi bi-envelope"></i>
                    <span className="provider-text">{t.loginWithEmail}</span>
                </button>
            </div>
            <a href="#" className="other-options-link">{t.otherOptions}</a>
            <p className="promo-text">{t.promoText}</p>
            <p className="legal-text">
                {t.legalText} <a href="#">{t.termsAndConditions}</a> {t.andHaveBeenNotified} <a href="#">{t.privacyPolicy}</a> của chúng tôi.
            </p>
        </div>
        <div className="modal-footer-polished">
            <a href="#" className="guest-link">{t.continueAsGuest}</a>
        </div>
    </>
);

//  Màn hình form email/password
const EmailFormView = ({ t, formData, showPassword, onInputChange, onShowPasswordToggle, onBackClick, onForgotPasswordClick }) => (
    <>
        <div className="modal-header-form">
            <button className="back-button" onClick={onBackClick}>
                <i className="bi bi-arrow-left"></i> {t.backButton}
            </button>
            <h2 className="title-login-email">{t.emailFormTitle}</h2>
        </div>
        <div className="modal-body-polished">
            <form className="email-login-form" onSubmit={(e) => e.preventDefault()}>
                <div className="form-group">
                    <label htmlFor="email">{t.emailLabel}</label>
                    <input type="email" id="email" placeholder={t.emailPlaceholder} required value={formData.email} onChange={onInputChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="password">{t.passwordLabel}</label>
                    <div className="password-wrapper">
                        <input type={showPassword ? "text" : "password"} id="password" placeholder={t.passwordPlaceholder} required value={formData.password} onChange={onInputChange} />
                        <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`} onClick={onShowPasswordToggle}></i>
                    </div>
                    <a href="#" className="forgot-password-link" onClick={onForgotPasswordClick}>{t.forgotPassword}</a>
                </div>
                <button type="submit" className="btn-login-submit">{t.loginButton}</button>
            </form>
        </div>
    </>
);

// Màn hình quên mật khẩu
const ForgotPasswordView = ({ t, onBackToLoginClick }) => {
    // State  cho form 
    const [forgotData, setForgotData] = useState({ email: '', newPassword: '', confirmPassword: '', otp: '' });
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [timer, setTimer] = useState(0);

    //   logic đếm ngược
    useEffect(() => {
        if (timer <= 0) return;
        const intervalId = setInterval(() => {
            setTimer(prevTimer => prevTimer - 1);
        }, 1000);
        return () => clearInterval(intervalId);
    }, [timer]);

    const handleInputChange = (e) => {
        const { id, value } = e.target;
        setForgotData(prev => ({ ...prev, [id]: value }));
    };

    const handleGetOtp = () => {
        console.log("Getting OTP for email:", forgotData.email);
        setTimer(60);
    };

    return (
        <>
            <div className="modal-header-form">
                <button className="back-button" onClick={onBackToLoginClick}>
                    <i className="bi bi-arrow-left"></i> {t.backToLogin}
                </button>
                <h2 className="title-login-email">{t.forgotPasswordTitle}</h2>
            </div>
            <div className="modal-body-polished">
                <form className="email-login-form" onSubmit={(e) => e.preventDefault()}>
                    <div className="form-group">
                        <label htmlFor="email_forgot">{t.emailLabel}</label>
                        <input type="email" id="email_forgot" placeholder={t.emailPlaceholder} value={forgotData.email} onChange={handleInputChange} required />
                    </div>
                    <div className="form-group">
                        <label htmlFor="newPassword">{t.newPasswordLabel}</label>
                        <div className="password-wrapper">
                            <input type={showNewPassword ? "text" : "password"} id="newPassword" placeholder={t.newPasswordPlaceholder} value={forgotData.newPassword} onChange={handleInputChange} required />
                            <i className={`bi ${showNewPassword ? 'bi-eye-slash' : 'bi-eye'}`} onClick={() => setShowNewPassword(!showNewPassword)}></i>
                        </div>
                    </div>
                    <div className="form-group">
                        <label htmlFor="confirmPassword">{t.confirmPasswordLabel}</label>
                        <div className="password-wrapper">
                            <input type={showConfirmPassword ? "text" : "password"} id="confirmPassword" placeholder={t.confirmPasswordPlaceholder} value={forgotData.confirmPassword} onChange={handleInputChange} required />
                            <i className={`bi ${showConfirmPassword ? 'bi-eye-slash' : 'bi-eye'}`} onClick={() => setShowConfirmPassword(!showConfirmPassword)}></i>
                        </div>
                    </div>
                    <div className="form-group otp-group">
                        <label htmlFor="otp">{t.otpLabel}</label>
                        <div className="otp-wrapper">
                            <input type="text" id="otp" placeholder={t.otpPlaceholder} value={forgotData.otp} onChange={handleInputChange} required />
                            <button type="button" className="btn-otp" onClick={handleGetOtp} disabled={timer > 0}>
                                {timer > 0 ? `${t.resendOtpButton} ${timer}s` : t.getOtpButton}
                            </button>
                        </div>
                    </div>
                    <button type="submit" className="btn-login-submit">{t.updatePasswordButton}</button>
                </form>
            </div>
        </>
    );
};

// --- COMPONENT  ---
const LoginModal = ({ isOpen, onClose }) => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    const [modalView, setModalView] = useState('options');
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({ email: '', password: '' });

    const handleInputChange = useCallback((e) => {
        const { id, value } = e.target;
        setFormData(prevData => ({ ...prevData, [id]: value }));
    }, []);

    const handleShowPasswordToggle = useCallback(() => {
        setShowPassword(prev => !prev);
    }, []);

    if (!isOpen) return null;

    const handleModalContentClick = (event) => {
        event.stopPropagation();
    };

    const handleClose = () => {
        onClose();
        setTimeout(() => {
            setModalView('options');
            setFormData({ email: '', password: '' });
            setShowPassword(false);
        }, 300);
    };

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content-polished" onClick={handleModalContentClick}>
                <button className="close-button" onClick={handleClose}>&times;</button>

                {modalView === 'options' && (
                    <OptionsView
                        t={t}
                        onEmailClick={() => setModalView('emailForm')}
                    />
                )}

                {modalView === 'emailForm' && (
                    <EmailFormView
                        t={t}
                        formData={formData}
                        showPassword={showPassword}
                        onInputChange={handleInputChange}
                        onShowPasswordToggle={handleShowPasswordToggle}
                        onBackClick={() => setModalView('options')}
                        onForgotPasswordClick={() => setModalView('forgotPassword')}
                    />
                )}

                {modalView === 'forgotPassword' && (
                    <ForgotPasswordView
                        t={t}
                        onBackToLoginClick={() => setModalView('emailForm')}
                    />
                )}
            </div>
        </div>
    );
}

export default LoginModal;
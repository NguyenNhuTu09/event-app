import React, { useState, useCallback, useEffect } from "react";
import './LoginModal.css';
import { useLanguage } from "../../../context/LanguageContext";
import { useAuth } from "../../../context/AuthContext";
import translations from "../../../translate/translations";
import { API_BASE_URL } from "../../../service/api";

//  Màn hình chọn phương thức đăng nhập
const OptionsView = ({ t, onEmailClick, onRegisterClick, onGoogleClick }) => (
    <>
        <div className="modal-header-polished">
            <div className="header-text"><h2>{t.loginOfferTitle}</h2></div>
        </div>
        <div className="modal-body-polished">
            <div className="login-providers">
                <button className="provider-btn google" onClick={onGoogleClick}>
                    <i className="bi bi-google"></i>
                    <span className="provider-text">{t.loginWithGoogle}</span>
                    <span className="recent-badge">{t.recentlyUsed}</span>
                </button>
                <button className="provider-btn email" onClick={onEmailClick}>
                    <i className="bi bi-envelope"></i>
                    <span className="provider-text">{t.loginWithEmail}</span>
                </button>
            </div>
            <div style={{ textAlign: 'center', marginTop: '15px' }}>
                <a href="#" className="other-options-link" onClick={(e) => { e.preventDefault(); onRegisterClick(); }}>
                    {t.registerWithEmail}
                </a>
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
const EmailFormView = ({ t, formData, showPassword, onInputChange, onShowPasswordToggle, onBackClick, onForgotPasswordClick, onSubmit, error, loading }) => (
    <>
        <div className="modal-header-form">
            <button className="back-button" onClick={onBackClick}>
                <i className="bi bi-arrow-left"></i> {t.backButton}
            </button>
            <h2 className="title-login-email">{t.emailFormTitle}</h2>
        </div>
        <div className="modal-body-polished">
            {error && (
                <div style={{
                    padding: '10px',
                    marginBottom: '15px',
                    backgroundColor: '#fee',
                    color: '#c33',
                    borderRadius: '5px',
                    fontSize: '14px'
                }}>
                    {error}
                </div>
            )}
            <form className="email-login-form" onSubmit={onSubmit}>
                <div className="form-group">
                    <label htmlFor="email">{t.emailLabel}</label>
                    <input type="email" id="email" placeholder={t.emailPlaceholder} required value={formData.email} onChange={onInputChange} disabled={loading} />
                </div>
                <div className="form-group">
                    <label htmlFor="password">{t.passwordLabel}</label>
                    <div className="password-wrapper">
                        <input type={showPassword ? "text" : "password"} id="password" placeholder={t.passwordPlaceholder} required value={formData.password} onChange={onInputChange} disabled={loading} />
                        <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`} onClick={onShowPasswordToggle}></i>
                    </div>
                    <a href="#" className="forgot-password-link" onClick={onForgotPasswordClick}>{t.forgotPassword}</a>
                </div>
                <button type="submit" className="btn-login-submit" disabled={loading}>
                    {loading ? '...' : t.loginButton}
                </button>
                <div style={{ textAlign: 'center', marginTop: '15px', fontSize: '14px' }}>
                    <span>{t.alreadyHaveAccount} </span>
                    <a href="#" onClick={(e) => { e.preventDefault(); onBackClick(); }} style={{ color: '#007bff', textDecoration: 'none' }}>
                        {t.signInLink}
                    </a>
                </div>
            </form>
        </div>
    </>
);

// Màn hình form đăng ký
const RegisterFormView = ({ t, formData, showPassword, showConfirmPassword, onInputChange, onShowPasswordToggle, onShowConfirmPasswordToggle, onBackClick, onSubmit, error, loading }) => (
    <>
        <div className="modal-header-form">
            <button className="back-button" onClick={onBackClick}>
                <i className="bi bi-arrow-left"></i> {t.backButton}
            </button>
            <h2 className="title-login-email">{t.registerTitle}</h2>
        </div>
        <div className="modal-body-polished">
            {error && (
                <div style={{
                    padding: '8px 12px',
                    marginBottom: '10px',
                    backgroundColor: '#fee',
                    color: '#c33',
                    borderRadius: '5px',
                    fontSize: '13px',
                    lineHeight: '1.4'
                }}>
                    {error}
                </div>
            )}
            <form className="email-login-form" onSubmit={onSubmit}>
                <div className="form-group">
                    <label htmlFor="username">{t.usernameLabel}</label>
                    <input type="text" id="username" placeholder={t.usernamePlaceholder} required value={formData.username} onChange={onInputChange} disabled={loading} />
                </div>
                <div className="form-group">
                    <label htmlFor="email">{t.emailLabel}</label>
                    <input type="email" id="email" placeholder={t.emailPlaceholder} required value={formData.email} onChange={onInputChange} disabled={loading} />
                </div>
                <div className="form-group">
                    <label htmlFor="password">{t.passwordLabel}</label>
                    <div className="password-wrapper">
                        <input type={showPassword ? "text" : "password"} id="password" placeholder={t.passwordPlaceholder} required value={formData.password} onChange={onInputChange} disabled={loading} />
                        <i className={`bi ${showPassword ? 'bi-eye-slash' : 'bi-eye'}`} onClick={onShowPasswordToggle}></i>
                    </div>
                </div>
                <div className="form-group">
                    <label htmlFor="confirmPassword">{t.confirmPasswordLabelRegister}</label>
                    <div className="password-wrapper">
                        <input type={showConfirmPassword ? "text" : "password"} id="confirmPassword" placeholder={t.confirmPasswordPlaceholderRegister} required value={formData.confirmPassword} onChange={onInputChange} disabled={loading} />
                        <i className={`bi ${showConfirmPassword ? 'bi-eye-slash' : 'bi-eye'}`} onClick={onShowConfirmPasswordToggle}></i>
                    </div>
                </div>
                <button type="submit" className="btn-login-submit" disabled={loading}>
                    {loading ? (
                        <span style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                            <span style={{
                                width: '16px',
                                height: '16px',
                                border: '2px solid #ffffff',
                                borderTop: '2px solid transparent',
                                borderRadius: '50%',
                                animation: 'spin 0.8s linear infinite',
                                display: 'inline-block'
                            }}></span>
                            {t.registerButton || 'Đang đăng ký...'}
                        </span>
                    ) : (
                        t.registerButton || 'Đăng ký'
                    )}
                </button>
                <div style={{ textAlign: 'center', marginTop: '10px', fontSize: '13px' }}>
                    <span>{t.alreadyHaveAccount} </span>
                    <a href="#" onClick={(e) => { e.preventDefault(); onBackClick(); }} style={{ color: '#007bff', textDecoration: 'none' }}>
                        {t.signInLink}
                    </a>
                </div>
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
    const { login, register } = useAuth();

    const [modalView, setModalView] = useState('options');
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [formData, setFormData] = useState({ email: '', password: '', username: '', confirmPassword: '' });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState('');

    const handleInputChange = useCallback((e) => {
        const { id, value } = e.target;
        setFormData(prevData => ({ ...prevData, [id]: value }));
        setError(''); // Clear error when user types
    }, []);

    const handleShowPasswordToggle = useCallback(() => {
        setShowPassword(prev => !prev);
    }, []);

    const handleShowConfirmPasswordToggle = useCallback(() => {
        setShowConfirmPassword(prev => !prev);
    }, []);

    const handleGoogleLogin = () => {
        // Redirect đến backend OAuth2 endpoint
        // OAuth2 endpoint không nằm trong /api, nên cần lấy base URL không có /api
        const backendUrl = API_BASE_URL.replace('/api', '');
        window.location.href = `${backendUrl}/oauth2/authorization/google`;
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const result = await login(formData.email, formData.password);
            if (result.success) {
                setSuccess(t.loginSuccess);
                setTimeout(() => {
                    handleClose();
                }, 1000);
            } else {
                setError(result.message);
            }
        } catch (err) {
            setError(err.message || 'Đăng nhập thất bại');
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');

        // Validate required fields
        if (!formData.username || !formData.username.trim()) {
            setError('Vui lòng nhập tên đăng nhập');
            return;
        }

        if (!formData.email || !formData.email.trim()) {
            setError('Vui lòng nhập email');
            return;
        }

        if (!formData.password || !formData.password.trim()) {
            setError('Vui lòng nhập mật khẩu');
            return;
        }

        if (!formData.confirmPassword || !formData.confirmPassword.trim()) {
            setError('Vui lòng nhập lại mật khẩu');
            return;
        }

        // Validate password match
        if (formData.password !== formData.confirmPassword) {
            setError('Mật khẩu xác nhận không khớp');
            return;
        }

        // Validate password length
        if (formData.password.length < 6) {
            setError('Mật khẩu phải có ít nhất 6 ký tự');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const result = await register(formData.username, formData.email, formData.password, formData.confirmPassword);
            if (result.success) {
                setSuccess(t.registerSuccess || 'Đăng ký thành công!');
                setTimeout(() => {
                    setModalView('emailForm');
                    setFormData(prev => ({ email: prev.email, password: '', username: '', confirmPassword: '' }));
                    setError('');
                }, 2000);
            } else {
                setError(result.message || 'Đăng ký thất bại. Vui lòng thử lại.');
            }
        } catch (err) {
            console.error('Registration error:', err);
            setError(err.message || 'Đăng ký thất bại. Vui lòng thử lại.');
        } finally {
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    const handleModalContentClick = (event) => {
        event.stopPropagation();
    };

    const handleClose = () => {
        onClose();
        setTimeout(() => {
            setModalView('options');
            setFormData({ email: '', password: '', username: '', confirmPassword: '' });
            setShowPassword(false);
            setShowConfirmPassword(false);
            setError('');
            setSuccess('');
        }, 300);
    };

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content-polished" onClick={handleModalContentClick}>
                <button className="close-button" onClick={handleClose}>&times;</button>

                {success && (
                    <div style={{
                        padding: '10px',
                        marginBottom: '15px',
                        backgroundColor: '#dfd',
                        color: '#3a3',
                        borderRadius: '5px',
                        fontSize: '14px',
                        textAlign: 'center'
                    }}>
                        {success}
                    </div>
                )}

                {modalView === 'options' && (
                    <OptionsView
                        t={t}
                        onEmailClick={() => setModalView('emailForm')}
                        onRegisterClick={() => setModalView('registerForm')}
                        onGoogleClick={handleGoogleLogin}
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
                        onSubmit={handleLogin}
                        error={error}
                        loading={loading}
                    />
                )}

                {modalView === 'registerForm' && (
                    <RegisterFormView
                        t={t}
                        formData={formData}
                        showPassword={showPassword}
                        showConfirmPassword={showConfirmPassword}
                        onInputChange={handleInputChange}
                        onShowPasswordToggle={handleShowPasswordToggle}
                        onShowConfirmPasswordToggle={handleShowConfirmPasswordToggle}
                        onBackClick={() => setModalView('options')}
                        onSubmit={handleRegister}
                        error={error}
                        loading={loading}
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
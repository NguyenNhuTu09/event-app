import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import translations from '../../translate/translations';
import { path } from '../../utils/constant';
import './Footer.css';
import logoImage from '../../assets/images/LOGO WEBIE ENENT-01.png';

const Footer = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    const currentYear = new Date().getFullYear();

    // State để quản lý việc mở/đóng các section trên mobile
    const [openSections, setOpenSections] = useState({
        quickLinks: false,
        company: false,
        support: false,
    });

    const toggleSection = (section) => {
        setOpenSections(prev => ({
            ...prev,
            [section]: !prev[section]
        }));
    };

    return (
        <footer className="footer">
            <div className="footer-container">
                <div className="footer-content">
                    {/* Company Info */}
                    <div className="footer-section">
                        <div className="footer-logo">
                            <Link to={path.HOME}>
                                <img src={logoImage} alt="Webie Event" className="footer-logo-image" />
                            </Link>
                        </div>
                        <p className="footer-description">
                            {t.footerDescription || 'Nền tảng quản lý sự kiện chuyên nghiệp, giúp bạn tổ chức và quản lý các sự kiện một cách hiệu quả.'}
                        </p>
                        <div className="social-links">
                            <a href="#" aria-label="Facebook" className="social-link">
                                <i className="bi bi-facebook"></i>
                            </a>
                            <a href="#" aria-label="Twitter" className="social-link">
                                <i className="bi bi-twitter"></i>
                            </a>
                            <a href="#" aria-label="LinkedIn" className="social-link">
                                <i className="bi bi-linkedin"></i>
                            </a>
                            <a href="#" aria-label="Instagram" className="social-link">
                                <i className="bi bi-instagram"></i>
                            </a>
                            <a href="#" aria-label="YouTube" className="social-link">
                                <i className="bi bi-youtube"></i>
                            </a>
                        </div>
                    </div>

                    {/* Quick Links */}
                    <div className={`footer-section ${openSections.quickLinks ? 'open' : ''}`}>
                        <button
                            className="footer-title-button"
                            onClick={() => toggleSection('quickLinks')}
                            aria-expanded={openSections.quickLinks}
                        >
                            <h3 className="footer-title">{t.footerQuickLinks || 'Liên Kết Nhanh'}</h3>
                            <i className={`bi ${openSections.quickLinks ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
                        </button>
                        <ul className="footer-links">
                            <li className='footer-link-item'>
                                <Link to={path.HOME}>{t.navHome || 'Trang Chủ'}</Link>
                            </li>
                            <li>
                                <Link to={path.INDUSTRIES}>{t.navIndustries}</Link>
                            </li>
                            <li>
                                <Link to={path.SOLUTIONS}>{t.navSolutions}</Link>
                            </li>
                            <li>
                                <Link to={path.RESOURCES}>{t.navResources}</Link>
                            </li>
                            <li>
                                <Link to={path.SUPPORT}>{t.navSupport}</Link>
                            </li>
                        </ul>
                    </div>

                    {/* Company */}
                    <div className={`footer-section ${openSections.company ? 'open' : ''}`}>
                        <button
                            className="footer-title-button"
                            onClick={() => toggleSection('company')}
                            aria-expanded={openSections.company}
                        >
                            <h3 className="footer-title">{t.footerCompany || 'Công Ty'}</h3>
                            <i className={`bi ${openSections.company ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
                        </button>
                        <ul className="footer-links">
                            <li>
                                <Link to={path.COMPANY}>{t.navCompany}</Link>
                            </li>
                            <li>
                                <Link to={path.CONTACT}>{t.contactUs}</Link>
                            </li>
                            <li>
                                <a href="#">{t.footerAboutUs || 'Về Chúng Tôi'}</a>
                            </li>
                            <li>
                                <a href="#">{t.footerCareers || 'Tuyển Dụng'}</a>
                            </li>
                            <li>
                                <a href="#">{t.footerBlog || 'Blog'}</a>
                            </li>
                        </ul>
                    </div>

                    {/* Support */}
                    <div className={`footer-section ${openSections.support ? 'open' : ''}`}>
                        <button
                            className="footer-title-button"
                            onClick={() => toggleSection('support')}
                            aria-expanded={openSections.support}
                        >
                            <h3 className="footer-title">{t.footerSupport || 'Hỗ Trợ'}</h3>
                            <i className={`bi ${openSections.support ? 'bi-chevron-up' : 'bi-chevron-down'}`}></i>
                        </button>
                        <ul className="footer-links">
                            <li>
                                <Link to={path.SUPPORT}>{t.navSupport}</Link>
                            </li>
                            <li>
                                <a href="#">{t.footerHelpCenter || 'Trung Tâm Trợ Giúp'}</a>
                            </li>
                            <li>
                                <a href="#">{t.footerDocumentation || 'Tài Liệu'}</a>
                            </li>
                            <li>
                                <a href="#">{t.footerPrivacy || 'Chính Sách Bảo Mật'}</a>
                            </li>
                            <li>
                                <a href="#">{t.footerTerms || 'Điều Khoản Sử Dụng'}</a>
                            </li>
                        </ul>
                    </div>
                </div>

                {/* Footer Bottom */}
                <div className="footer-bottom">
                    <div className="footer-bottom-content">
                        <p className="copyright">
                            &copy; {currentYear} Webie Event. {t.footerAllRightsReserved || 'Tất cả quyền được bảo lưu.'}
                        </p>
                        <div className="footer-bottom-links">
                            <a href="#">{t.footerPrivacy || 'Bảo Mật'}</a>
                            <span className="separator">|</span>
                            <a href="#">{t.footerTerms || 'Điều Khoản'}</a>
                            <span className="separator">|</span>
                            <a href="#">{t.footerCookies || 'Cookies'}</a>
                        </div>
                    </div>
                </div>
            </div>
        </footer>
    );
};

export default Footer;


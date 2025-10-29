import React, { useState } from 'react';
import './Header.css';
import { useLanguage } from '../../context/LanguageContext'; // Import hook
import translations from '../../translate/translations'; // Import file dịch
import LoginModal from '../auth/LoginModal/LoginModal';
const Header = ({ onLoginClick }) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const { language, setLanguage } = useLanguage(); // Lấy ngôn ngữ và hàm set từ context
    const t = translations[language]; // Lấy object dịch tương ứng

    const toggleMenu = () => {
        setIsMenuOpen(!isMenuOpen);
    };

    const handleLanguageChange = (lang) => {
        setLanguage(lang);
    };
    const handleLoginClick = (event) => {
        event.preventDefault();
        onLoginClick();
        if (isMenuOpen) {
            setIsMenuOpen(false);
        }
    }
    return (
        <header>
            <div className="container">
                <nav>
                    <div className="logo">
                        <a href="/">
                            <span className="logo-text">Event Website</span>
                            <i className="bi bi-fire"></i>
                        </a>
                    </div>

                    <ul className="nav-links-desktop">
                        <li><a href="#">{t.navIndustries}</a></li>
                        <li><a href="#">{t.navSolutions}</a></li>
                        <li><a href="#">{t.navResources}</a></li>
                        <li><a href="#">{t.navSupport}</a></li>
                        <li><a href="#">{t.navCompany}</a></li>
                    </ul>

                    <div className="nav-actions">

                        <a href="#">{t.contactUs}</a>

                        <a href="#" onClick={handleLoginClick}>{t.emsLogin}</a>
                        <div className="language-switcher">
                            <button
                                onClick={() => handleLanguageChange('vi')}
                                className={language === 'vi' ? 'active' : ''}
                            >
                                VI
                            </button>
                            <span className="lang-separator">|</span>
                            <button
                                onClick={() => handleLanguageChange('en')}
                                className={language === 'en' ? 'active' : ''}
                            >
                                EN
                            </button>
                        </div>
                    </div>

                    <div className={`mobile-menu ${isMenuOpen ? 'active' : ''}`}>
                        <a href="#">{t.navIndustries}</a>
                        <a href="#">{t.navSolutions}</a>
                        <a href="#">{t.navResources}</a>
                        <a href="#">{t.navSupport}</a>
                        <a href="#">{t.navCompany}</a>
                        <hr />
                        <a href="#">{t.contactUs}</a>

                        <a href="#">{t.emsLogin}</a>




                        <div className="language-switcher mobile">
                            <button
                                onClick={() => handleLanguageChange('vi')}
                                className={language === 'vi' ? 'active' : ''}
                            >
                                Tiếng Việt
                            </button>
                            <button
                                onClick={() => handleLanguageChange('en')}
                                className={language === 'en' ? 'active' : ''}
                            >
                                English
                            </button>
                        </div>
                    </div>

                    <div className="mobile-menu-icon" onClick={toggleMenu}>
                        <div className={`bar ${isMenuOpen ? 'open' : ''}`}></div>
                        <div className={`bar ${isMenuOpen ? 'open' : ''}`}></div>
                        <div className={`bar ${isMenuOpen ? 'open' : ''}`}></div>
                    </div>
                </nav>
            </div>
        </header>
    );
};

export default Header;
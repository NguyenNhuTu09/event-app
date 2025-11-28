import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import './Header.css';
import { useLanguage } from '../../context/LanguageContext'; // Import hook
import { useAuth } from '../../context/AuthContext'; // Import auth hook
import translations from '../../translate/translations'; // Import file dịch
import { path } from '../../utils/constant';
import LoginModal from '../auth/LoginModal/LoginModal';
import logoImage from '../../assets/images/LOGO WEBIE ENENT-01.png';

// Đăng ký ScrollTrigger plugin
gsap.registerPlugin(ScrollTrigger);

const Header = ({ onLoginClick }) => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const { language, setLanguage } = useLanguage(); // Lấy ngôn ngữ và hàm set từ context
    const { user, isAuthenticated, logout } = useAuth(); // Lấy user và logout từ auth context
    const t = translations[language]; // Lấy object dịch tương ứng

    const headerRef = useRef(null);
    const logoRef = useRef(null);
    const navLinksRef = useRef(null);
    const navActionsRef = useRef(null);
    const [scrolledState, setScrolledState] = useState(false);

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

    const handleLogout = (event) => {
        event.preventDefault();
        logout();
        if (isMenuOpen) {
            setIsMenuOpen(false);
        }
    }

    // GSAP ScrollTrigger cho Sticky Shrink Header và Dynamic Navbar Resize
    useEffect(() => {
        const header = headerRef.current;
        const logo = logoRef.current;
        const navLinks = navLinksRef.current;
        const navActions = navActionsRef.current;

        if (!header || !logo || !navLinks || !navActions) return;

        // ScrollTrigger để shrink header khi scroll
        ScrollTrigger.create({
            trigger: 'body',
            start: 'top -100',
            end: 'bottom top',
            onEnter: () => {
                setScrolledState(true);
                // Shrink header
                gsap.to(header, {
                    height: 60,
                    padding: '0',
                    duration: 0.3,
                    ease: 'power2.out'
                });
                // Shrink logo
                const logoImg = logo.querySelector('.logo-image');
                if (logoImg) {
                    gsap.to(logoImg, {
                        height: '50px',
                        scale: 0.9,
                        duration: 0.3,
                        ease: 'power2.out'
                    });
                }
                // Shrink nav links
                gsap.to(navLinks.querySelectorAll('a'), {
                    fontSize: '14px',
                    padding: '8px 12px',
                    duration: 0.3,
                    ease: 'power2.out'
                });
                // Shrink nav actions
                gsap.to(navActions, {
                    gap: '15px',
                    fontSize: '14px',
                    duration: 0.3,
                    ease: 'power2.out'
                });
            },
            onLeaveBack: () => {
                setScrolledState(false);
                // Expand header
                gsap.to(header, {
                    height: 80,
                    padding: '0',
                    duration: 0.3,
                    ease: 'power2.out'
                });
                // Expand logo
                const logoImg = logo.querySelector('.logo-image');
                if (logoImg) {
                    gsap.to(logoImg, {
                        height: '60px',
                        scale: 1,
                        duration: 0.3,
                        ease: 'power2.out'
                    });
                }
                // Expand nav links
                gsap.to(navLinks.querySelectorAll('a'), {
                    fontSize: '16px',
                    padding: '10px 15px',
                    duration: 0.3,
                    ease: 'power2.out'
                });
                // Expand nav actions
                gsap.to(navActions, {
                    gap: '25px',
                    fontSize: '16px',
                    duration: 0.3,
                    ease: 'power2.out'
                });
            }
        });

        return () => {
            ScrollTrigger.getAll().forEach(trigger => {
                if (trigger.vars.trigger === 'body') {
                    trigger.kill();
                }
            });
        };
    }, []);

    return (
        <header
            ref={headerRef}
            className={scrolledState ? 'scrolled shrink' : ''}
        >
            <div className="container">
                <nav>
                    <div className="logo">
                        <Link to={path.HOME} ref={logoRef}>
                            <img src={logoImage} alt="Webie Event" className="logo-image" />
                        </Link>
                    </div>

                    <ul
                        ref={navLinksRef}
                        className="nav-links-desktop"
                    >
                        <li><Link to={path.SOLUTIONS}>{t.navSolutions}</Link></li>
                        <li><Link to={path.RESOURCES}>{t.navResources}</Link></li>
                        <li><Link to={path.SUPPORT}>{t.navSupport}</Link></li>
                        <li><Link to={path.COMPANY}>{t.navCompany}</Link></li>
                        <li><Link to={path.EVENTS}>{t.navEvent}</Link></li>
                        <li><Link to={path.PARTNER_REGISTER}>{t.navPartnerRegister}</Link></li>
                    </ul>

                    <div
                        ref={navActionsRef}
                        className="nav-actions"
                    >
                        <Link to={path.CONTACT}>{t.contactUs}</Link>

                        {isAuthenticated ? (
                            <>
                                <span style={{ marginRight: '10px', color: '#ffffff', textShadow: '0 1px 2px rgba(0, 0, 0, 0.3)' }}>
                                    <strong>{user?.username}</strong>
                                </span>
                                <a href="#" onClick={handleLogout}>{t.logout}</a>
                            </>
                        ) : (
                            <a href="#" onClick={handleLoginClick}>{t.emsLogin}</a>
                        )}
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

                    <div
                        className={`mobile-menu ${isMenuOpen ? 'active' : ''}`}
                        style={{
                            top: scrolledState ? '60px' : '80px',
                            height: scrolledState ? 'calc(100vh - 60px)' : 'calc(100vh - 80px)'
                        }}
                    >
                        <Link to={path.SOLUTIONS} onClick={() => setIsMenuOpen(false)}>{t.navSolutions}</Link>
                        <Link to={path.RESOURCES} onClick={() => setIsMenuOpen(false)}>{t.navResources}</Link>
                        <Link to={path.SUPPORT} onClick={() => setIsMenuOpen(false)}>{t.navSupport}</Link>
                        <Link to={path.COMPANY} onClick={() => setIsMenuOpen(false)}>{t.navCompany}</Link>
                        <Link to={path.EVENTS} onClick={() => setIsMenuOpen(false)}>{t.navEvent}</Link>
                        <Link to={path.PARTNER_REGISTER} onClick={() => setIsMenuOpen(false)}>{t.navPartnerRegister}</Link>


                        <hr />
                        <Link to={path.CONTACT} onClick={() => setIsMenuOpen(false)}>{t.contactUs}</Link>

                        {isAuthenticated ? (
                            <>
                                <div style={{ padding: '10px', marginBottom: '10px', borderBottom: '1px solid #eee' }}>
                                    <span style={{ color: '#333' }}>
                                        <strong>{user?.username}</strong>
                                    </span>
                                </div>
                                <a href="#" onClick={handleLogout}>{t.logout}</a>
                            </>
                        ) : (
                            <a href="#" onClick={handleLoginClick}>{t.emsLogin}</a>
                        )}

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

                    <div
                        className="mobile-menu-icon"
                        onClick={toggleMenu}
                    >
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
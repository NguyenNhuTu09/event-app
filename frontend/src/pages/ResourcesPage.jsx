import React, { useState, useEffect, useRef } from 'react';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import './ResourcesPage.css';

gsap.registerPlugin(ScrollTrigger);

const ResourcesPage = () => {
    // Lấy ngôn ngữ hiện tại từ context
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    // State để quản lý filter của media gallery
    const [selectedCategory, setSelectedCategory] = useState('all');

    // Refs cho GSAP animations
    const heroRef = useRef(null);
    const categoriesRef = useRef(null);
    const documentsRef = useRef(null);
    const mediaRef = useRef(null);

    // Animation khi component mount
    useEffect(() => {
        // Animate hero section
        const hero = heroRef.current;
        if (hero) {
            const title = hero.querySelector('.hero-title');
            const description = hero.querySelector('.hero-description');
            
            if (title) {
                gsap.fromTo(
                    title,
                    { opacity: 0, y: 50 },
                    { opacity: 1, y: 0, duration: 1, ease: 'power3.out' }
                );
            }
            
            if (description) {
                gsap.fromTo(
                    description,
                    { opacity: 0, y: 30 },
                    { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out', delay: 0.3 }
                );
            }
        }

        // Animate sections khi scroll
        const sections = [categoriesRef, documentsRef, mediaRef];
        sections.forEach((section) => {
            if (section.current) {
                gsap.fromTo(
                    section.current,
                    { opacity: 0, y: 60 },
                    {
                        opacity: 1,
                        y: 0,
                        duration: 1,
                        ease: 'power2.out',
                        scrollTrigger: {
                            trigger: section.current,
                            start: 'top 80%',
                            toggleActions: 'play none none reverse',
                        },
                    }
                );
            }
        });

        return () => {
            ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
        };
    }, []);

    // Dữ liệu các danh mục tài nguyên
    const resourceCategories = [
        {
            id: 'guide',
            icon: 'bi-file-text',
            title: t.resourceCategoryGuide,
            description: t.resourceCategoryGuideDesc,
        },
        {
            id: 'checkin',
            icon: 'bi-qr-code-scan',
            title: t.resourceCategoryCheckin,
            description: t.resourceCategoryCheckinDesc,
        },
        {
            id: 'rules',
            icon: 'bi-shield-check',
            title: t.resourceCategoryRules,
            description: t.resourceCategoryRulesDesc,
        },
        {
            id: 'schedule',
            icon: 'bi-calendar-event',
            title: t.resourceCategorySchedule,
            description: t.resourceCategoryScheduleDesc,
        },
    ];

    // Dữ liệu tài liệu tải xuống
    const documents = [
        {
            name: t.documentVolunteerName,
            description: t.documentVolunteerDesc,
            type: 'PDF',
            downloadLink: '#',
            icon: 'bi-file-earmark-pdf',
        },
        {
            name: t.documentMapName,
            description: t.documentMapDesc,
            type: 'PDF',
            downloadLink: '#',
            icon: 'bi-file-earmark-pdf',
        },
        {
            name: t.documentBookingName,
            description: t.documentBookingDesc,
            type: 'Online',
            viewLink: '#',
            icon: 'bi-link-45deg',
        },
    ];

    // Dữ liệu media gallery (hình ảnh/video)
    // Trong thực tế, bạn sẽ lấy từ API hoặc import từ assets
    const mediaItems = [
        { id: 1, type: 'image', category: 'event', url: 'https://via.placeholder.com/400x300?text=Event+2023', title: 'Sự kiện 2023' },
        { id: 2, type: 'image', category: 'event', url: 'https://via.placeholder.com/400x500?text=Workshop', title: 'Workshop' },
        { id: 3, type: 'image', category: 'event', url: 'https://via.placeholder.com/400x400?text=Networking', title: 'Networking' },
        { id: 4, type: 'image', category: 'venue', url: 'https://via.placeholder.com/400x300?text=Venue+2023', title: 'Địa điểm 2023' },
        { id: 5, type: 'image', category: 'venue', url: 'https://via.placeholder.com/400x600?text=Stage', title: 'Sân khấu' },
        { id: 6, type: 'video', category: 'event', url: 'https://via.placeholder.com/400x300?text=Video+2022', title: 'Video 2022' },
        { id: 7, type: 'image', category: 'event', url: 'https://via.placeholder.com/400x400?text=Team', title: 'Đội ngũ' },
        { id: 8, type: 'image', category: 'venue', url: 'https://via.placeholder.com/400x500?text=Exhibition', title: 'Triển lãm' },
    ];

    // Filter media theo category
    const filteredMedia = selectedCategory === 'all' 
        ? mediaItems 
        : mediaItems.filter(item => item.category === selectedCategory);

    return (
        <div className="resources-page">
            {/* Hero Section */}
            <section className="resources-hero" ref={heroRef}>
                <div className="container">
                    <div className="hero-content">
                        <h1 className="hero-title">{t.resourcesHeroTitle}</h1>
                        <p className="hero-description">{t.resourcesHeroDescription}</p>
                    </div>
                </div>
            </section>

            {/* Section 1 - Trung tâm tài nguyên (Categories) */}
            <section className="resources-categories" ref={categoriesRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.resourcesCategoriesTitle}</h2>
                        <p>{t.resourcesCategoriesSubtitle}</p>
                    </div>
                    <div className="categories-grid">
                        {resourceCategories.map((category) => (
                            <div key={category.id} className="category-card">
                                <div className="category-icon">
                                    <i className={`bi ${category.icon}`}></i>
                                </div>
                                <h3>{category.title}</h3>
                                <p>{category.description}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Section 2 - Tài liệu tải xuống */}
            <section className="resources-documents" ref={documentsRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.resourcesDocumentsTitle}</h2>
                        <p>{t.resourcesDocumentsSubtitle}</p>
                    </div>
                    <div className="documents-table-container">
                        <table className="documents-table">
                            <thead>
                                <tr>
                                    <th>{t.documentTableName}</th>
                                    <th>{t.documentTableDescription}</th>
                                    <th>{t.documentTableAction}</th>
                                </tr>
                            </thead>
                            <tbody>
                                {documents.map((doc, index) => (
                                    <tr key={index}>
                                        <td>
                                            <div className="document-name">
                                                <i className={`bi ${doc.icon}`}></i>
                                                <span>{doc.name}</span>
                                            </div>
                                        </td>
                                        <td>{doc.description}</td>
                                        <td>
                                            {doc.downloadLink ? (
                                                <a 
                                                    href={doc.downloadLink} 
                                                    className="btn-download"
                                                    download
                                                >
                                                    <i className="bi bi-download"></i>
                                                    {t.documentDownloadPDF}
                                                </a>
                                            ) : (
                                                <a 
                                                    href={doc.viewLink} 
                                                    className="btn-view"
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                >
                                                    <i className="bi bi-eye"></i>
                                                    {t.documentViewOnline}
                                                </a>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>

            {/* Section 3 - Thư viện media */}
            <section className="resources-media" ref={mediaRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.resourcesMediaTitle}</h2>
                        <p>{t.resourcesMediaSubtitle}</p>
                    </div>
                    
                    {/* Filter buttons */}
                    <div className="media-filters">
                        <button
                            className={`filter-btn ${selectedCategory === 'all' ? 'active' : ''}`}
                            onClick={() => setSelectedCategory('all')}
                        >
                            {t.mediaFilterAll}
                        </button>
                        <button
                            className={`filter-btn ${selectedCategory === 'event' ? 'active' : ''}`}
                            onClick={() => setSelectedCategory('event')}
                        >
                            {t.mediaFilterEvent}
                        </button>
                        <button
                            className={`filter-btn ${selectedCategory === 'venue' ? 'active' : ''}`}
                            onClick={() => setSelectedCategory('venue')}
                        >
                            {t.mediaFilterVenue}
                        </button>
                    </div>

                    {/* Media Gallery - Masonry Grid */}
                    <div className="media-gallery">
                        {filteredMedia.map((item) => (
                            <div key={item.id} className="media-item">
                                {item.type === 'image' ? (
                                    <img src={item.url} alt={item.title} />
                                ) : (
                                    <div className="video-placeholder">
                                        <i className="bi bi-play-circle"></i>
                                        <p>{item.title}</p>
                                    </div>
                                )}
                                <div className="media-overlay">
                                    <h4>{item.title}</h4>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>
        </div>
    );
};

export default ResourcesPage;


















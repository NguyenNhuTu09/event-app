import React, { useState, useEffect, useRef } from 'react';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import EventRegistrationModal from '../components/eventsModal/EventRegistrationModal';
import './EventsPage.css';

gsap.registerPlugin(ScrollTrigger);

const EventsPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;

    const bannerRef = useRef(null);
    const eventsRef = useRef(null);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);

    // Hardcode danh sách sự kiện
    const events = [
        {
            id: 1,
            title: 'Tech Innovation Summit 2024',
            description: 'Hội nghị công nghệ hàng đầu với các diễn giả quốc tế, workshop thực hành và networking session.',
            date: '2024-03-15',
            time: '09:00 - 17:00',
            location: 'Ho Chi Minh City Convention Center',
            image: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800',
            category: 'Technology',
            capacity: 500,
            registered: 320,
        },
        {
            id: 2,
            title: 'Digital Marketing Workshop',
            description: 'Workshop thực hành về Digital Marketing, SEO, Social Media Marketing và Content Strategy.',
            date: '2024-03-20',
            time: '14:00 - 18:00',
            location: 'Webie Office, Thu Duc City',
            image: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800',
            category: 'Marketing',
            capacity: 100,
            registered: 75,
        },
        {
            id: 3,
            title: 'Startup Pitch Day',
            description: 'Ngày hội khởi nghiệp với các startup trình bày ý tưởng trước các nhà đầu tư và chuyên gia.',
            date: '2024-03-25',
            time: '10:00 - 16:00',
            location: 'Innovation Hub, District 1',
            image: 'https://images.unsplash.com/photo-1552664730-d307ca884978?w=800',
            category: 'Business',
            capacity: 200,
            registered: 150,
        },
        {
            id: 4,
            title: 'Web Development Bootcamp',
            description: 'Khóa học cấp tốc về Web Development, từ HTML/CSS đến React và Node.js với các dự án thực tế.',
            date: '2024-04-01',
            time: '09:00 - 17:00',
            location: 'Online & Webie Office',
            image: 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800',
            category: 'Education',
            capacity: 50,
            registered: 35,
        },
        {
            id: 5,
            title: 'AI & Machine Learning Conference',
            description: 'Hội thảo về AI và Machine Learning với các case study thực tế và demo công nghệ mới nhất.',
            date: '2024-04-10',
            time: '08:30 - 17:30',
            location: 'University of Technology, HCMC',
            image: 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800',
            category: 'Technology',
            capacity: 300,
            registered: 280,
        },
        {
            id: 6,
            title: 'Networking Night - Tech Professionals',
            description: 'Buổi gặp gỡ và networking dành cho các chuyên gia công nghệ, startup founders và investors.',
            date: '2024-04-15',
            time: '18:00 - 21:00',
            location: 'Rooftop Bar, District 2',
            image: 'https://images.unsplash.com/photo-1511578314322-379afb476865?w=800',
            category: 'Networking',
            capacity: 150,
            registered: 120,
        },
    ];

    // GSAP Animations
    useEffect(() => {
        // Banner animation
        const banner = bannerRef.current;
        if (banner) {
            const title = banner.querySelector('.banner-title');
            const description = banner.querySelector('.banner-description');

            const tl = gsap.timeline();
            if (title) {
                tl.fromTo(
                    title,
                    { opacity: 0, y: 50, scale: 0.9 },
                    { opacity: 1, y: 0, scale: 1, duration: 1, ease: 'power3.out' }
                );
            }
            if (description) {
                tl.fromTo(
                    description,
                    { opacity: 0, y: 30 },
                    { opacity: 1, y: 0, duration: 0.8, ease: 'power2.out' },
                    '-=0.5'
                );
            }
        }

        // Events grid animation
        const eventsSection = eventsRef.current;
        if (eventsSection) {
            const cards = eventsSection.querySelectorAll('.event-card');
            cards.forEach((card, index) => {
                gsap.fromTo(
                    card,
                    { opacity: 0, y: 60, scale: 0.9 },
                    {
                        opacity: 1,
                        y: 0,
                        scale: 1,
                        duration: 0.8,
                        ease: 'back.out(1.2)',
                        scrollTrigger: {
                            trigger: card,
                            start: 'top 85%',
                            toggleActions: 'play none none reverse',
                        },
                        delay: index * 0.1,
                    }
                );
            });
        }

        return () => {
            ScrollTrigger.getAll().forEach((trigger) => trigger.kill());
        };
    }, []);

    const handleEventClick = (event) => {
        setSelectedEvent(event);
        setIsModalOpen(true);
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setSelectedEvent(null);
    };

    const handleRegistrationSubmit = (data) => {
        console.log('Registration submitted:', data);
        // Có thể thêm logic xử lý ở đây nếu cần
    };

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString(language === 'vi' ? 'vi-VN' : 'en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <div className="events-page">
            {/* Banner Section */}
            <section className="events-banner" ref={bannerRef}>
                <div className="banner-background">
                    <div className="banner-gradient"></div>
                </div>
                <div className="container">
                    <div className="banner-content">
                        <h1 className="banner-title">{t.eventsBannerTitle}</h1>
                        <p className="banner-description">{t.eventsBannerDescription}</p>
                    </div>
                </div>
            </section>

            {/* Events List Section */}
            <section className="events-list" ref={eventsRef}>
                <div className="container">
                    <div className="section-header">
                        <h2>{t.eventsListTitle}</h2>
                        <p>{t.eventsListSubtitle}</p>
                    </div>
                    <div className="events-grid">
                        {events.map((event) => (
                            <div key={event.id} className="event-card" onClick={() => handleEventClick(event)}>
                                <div className="event-image">
                                    <img src={event.image} alt={event.title} />
                                    <div className="event-category">{event.category}</div>
                                    <div className="event-overlay">
                                        <button className="event-register-btn">{t.eventRegisterNow}</button>
                                    </div>
                                </div>
                                <div className="event-content">
                                    <h3 className="event-title">{event.title}</h3>
                                    <p className="event-description">{event.description}</p>
                                    <div className="event-details">
                                        <div className="event-detail-item">
                                            <i className="bi bi-calendar-event"></i>
                                            <span>{formatDate(event.date)}</span>
                                        </div>
                                        <div className="event-detail-item">
                                            <i className="bi bi-clock"></i>
                                            <span>{event.time}</span>
                                        </div>
                                        <div className="event-detail-item">
                                            <i className="bi bi-geo-alt"></i>
                                            <span>{event.location}</span>
                                        </div>
                                    </div>
                                    <div className="event-capacity">
                                        <div className="capacity-bar">
                                            <div
                                                className="capacity-fill"
                                                style={{ width: `${(event.registered / event.capacity) * 100}%` }}
                                            ></div>
                                        </div>
                                        <span className="capacity-text">
                                            {event.registered} / {event.capacity} {t.eventRegistered}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Registration Modal */}
            <EventRegistrationModal
                isOpen={isModalOpen}
                event={selectedEvent}
                onClose={handleCloseModal}
                onSubmit={handleRegistrationSubmit}
            />
        </div>
    );
};

export default EventsPage;



import React, { useEffect, useRef } from 'react';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { useLanguage } from '../context/LanguageContext';
import translations from '../translate/translations';
import industriesImage from '../assets/images/pexels-nietjuhart-796602.jpg';
import './IndustriesPage.css';

gsap.registerPlugin(ScrollTrigger);

const IndustriesPage = () => {
    const { language } = useLanguage();
    const t = translations[language] || translations.vi;
    const heroRef = useRef(null);
    const contentRef = useRef(null);
    const titleRef = useRef(null);
    const subtitleRef = useRef(null);

    useEffect(() => {
        const hero = heroRef.current;
        const content = contentRef.current;
        const title = titleRef.current;
        const subtitle = subtitleRef.current;
        const bgImage = hero?.querySelector('.industries-background-image');

        if (!hero || !content || !title || !subtitle || !bgImage) return;

        // Animation timeline cho entrance
        const tl = gsap.timeline();

        // Background image fade in và scale
        tl.fromTo(
            bgImage,
            { 
                opacity: 0, 
                scale: 1.3,
                filter: 'brightness(0.4) blur(5px)'
            },
            { 
                opacity: 1, 
                scale: 1,
                filter: 'brightness(0.6) blur(0px)',
                duration: 2,
                ease: 'power2.out'
            }
        );

        // Title fade in từ dưới lên
        tl.fromTo(
            title,
            {
                opacity: 0,
                y: 50,
                scale: 0.9
            },
            {
                opacity: 1,
                y: 0,
                scale: 1,
                duration: 1,
                ease: 'power3.out'
            },
            '-=1'
        );

        // Subtitle fade in
        tl.fromTo(
            subtitle,
            {
                opacity: 0,
                y: 30
            },
            {
                opacity: 1,
                y: 0,
                duration: 0.8,
                ease: 'power2.out'
            },
            '-=0.5'
        );

        // Parallax effect cho background image khi scroll
        gsap.to(bgImage, {
            scrollTrigger: {
                trigger: hero,
                start: 'top top',
                end: 'bottom top',
                scrub: true,
            },
            y: '30%',
            scale: 1.1,
            ease: 'power1.inOut',
        });

        // Fade out content khi scroll
        gsap.to(content, {
            scrollTrigger: {
                trigger: hero,
                start: 'top top',
                end: 'center top',
                scrub: true,
            },
            opacity: 0,
            y: -50,
            ease: 'power1.out',
        });

        // Overlay fade khi scroll
        const overlay = hero?.querySelector('.industries-background-overlay');
        if (overlay) {
            gsap.to(overlay, {
                scrollTrigger: {
                    trigger: hero,
                    start: 'top top',
                    end: 'bottom top',
                    scrub: true,
                },
                opacity: 0.8,
                ease: 'power1.inOut',
            });
        }

        // Animation cho main content section (fade in khi scroll vào view)
        const mainContent = document.querySelector('.industries-main-content');
        if (mainContent) {
            gsap.fromTo(
                mainContent.querySelectorAll('.fade-in-up'),
                {
                    opacity: 0,
                    y: 50
                },
                {
                    opacity: 1,
                    y: 0,
                    duration: 1,
                    ease: 'power2.out',
                    scrollTrigger: {
                        trigger: mainContent,
                        start: 'top 80%',
                        end: 'top 50%',
                        toggleActions: 'play none none reverse'
                    },
                    stagger: 0.2
                }
            );
        }

        // Cleanup
        return () => {
            ScrollTrigger.getAll().forEach(trigger => {
                if (trigger.trigger === hero || trigger.vars?.trigger === hero) {
                    trigger.kill();
                }
            });
        };
    }, []);

    return (
        <div className="industries-page">
            <section className="industries-hero" ref={heroRef}>
                <div className="industries-background">
                    <img
                        src={industriesImage}
                        alt="Industries Background"
                        className="industries-background-image"
                    />
                    <div className="industries-background-overlay"></div>
                </div>
                <div className="industries-content" ref={contentRef}>
                    <h1 ref={titleRef} className="industries-title">
                        {t.navIndustries || 'Ngành Nghề'}
                    </h1>
                    <p ref={subtitleRef} className="industries-subtitle">
                        Khám phá các giải pháp cho từng ngành nghề
                    </p>
                </div>
            </section>
            
            <section className="industries-main-content">
                <div className="container">
                    <h2 className="fade-in-up">Nội dung ngành nghề</h2>
                    <p className="fade-in-up">Thông tin chi tiết về các ngành nghề sẽ được cập nhật tại đây.</p>
                </div>
            </section>
        </div>
    );
};

export default IndustriesPage;




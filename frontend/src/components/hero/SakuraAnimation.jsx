import React, { useEffect, useState } from 'react';
import './SakuraAnimation.css';

const SakuraAnimation = () => {
    const [petals, setPetals] = useState([]);

    useEffect(() => {
        // Tạo các cánh hoa với vị trí và thời gian ngẫu nhiên - Tăng số lượng lên 80 cánh hoa
        const createPetals = () => {
            const newPetals = Array.from({ length: 80 }).map((_, index) => ({
                id: index,
                left: Math.random() * 100, // Vị trí ngang ngẫu nhiên
                delay: Math.random() * 8, // Độ trễ ngẫu nhiên (tăng lên 8s)
                duration: Math.random() * 8 + 8, // Thời gian rơi 8-16s (nhanh hơn)
                size: Math.random() * 12 + 8, // Kích thước 8-20px
                rotation: Math.random() * 360, // Góc xoay ngẫu nhiên
            }));
            setPetals(newPetals);
        };

        createPetals();
    }, []);

    return (
        <div className="sakura-container">
            {petals.map((petal) => (
                <div
                    key={petal.id}
                    className="sakura-petal"
                    style={{
                        left: `${petal.left}%`,
                        animationDelay: `${petal.delay}s`,
                        animationDuration: `${petal.duration}s`,
                        width: `${petal.size}px`,
                        height: `${petal.size}px`,
                        transform: `rotate(${petal.rotation}deg)`,
                    }}
                >
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path
                            d="M12 2C12 2 8 6 8 10C8 12 10 14 12 14C14 14 16 12 16 10C16 6 12 2 12 2Z"
                            fill="#FFB6C1"
                            opacity="0.8"
                        />
                        <path
                            d="M12 2C12 2 16 6 16 10C16 12 14 14 12 14C10 14 8 12 8 10C8 6 12 2 12 2Z"
                            fill="#FFC0CB"
                            opacity="0.6"
                        />
                    </svg>
                </div>
            ))}
        </div>
    );
};

export default SakuraAnimation;


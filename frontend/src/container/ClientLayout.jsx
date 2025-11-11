import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Header from '../components/header/Header';
import LoginModal from '../components/auth/LoginModal/LoginModal';
import Footer from '../components/footer/Footer';

const ClientLayout = () => {
    const [isModalOpen, setIsModalOpen] = useState(false);

    const handleOpenLoginModal = () => setIsModalOpen(true);
    const handleCloseLoginModal = () => setIsModalOpen(false);

    return (
        <>
            <LoginModal isOpen={isModalOpen} onClose={handleCloseLoginModal} />

            <Header onLoginClick={handleOpenLoginModal} />

            <main>
                <Outlet />
            </main>

            <Footer />
        </>
    );
};

export default ClientLayout;


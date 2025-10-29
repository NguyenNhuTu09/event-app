import React from "react";
import './Header.css';

const Header = () => {
    return (
        <header >
            <div className="container">
                <nav>
                    <div className="logo">Event Website</div>
                    <ul className="nav-links">
                        <li><a href="#">Industries <i className="fas fa-chevron-down"></i></a></li>
                        <li><a href="#">Solutions <i className="fas fa-chevron-down"></i></a></li>
                        <li><a href="#">Resources <i className="fas fa-chevron-down"></i></a></li>
                        <li><a href="#">Support <i className="fas fa-chevron-down"></i></a></li>
                        <li><a href="#">Company <i className="fas fa-chevron-down"></i></a></li>
                    </ul>
                    <div className="nav-actions">
                        <a href="#" className="btn btn-outline">Contact Us</a>
                        <a href="#">Pricing</a>
                        <a href="#"><i className="fas fa-user"></i> EMS Login</a>
                        <a href="#" className="btn btn-primary"><i className="fas fa-calendar-alt"></i> Book a Demo</a>
                        <a href="#"><i className="fas fa-search"></i></a>
                    </div>
                </nav>
            </div>
        </header>
    );
}

export default Header;
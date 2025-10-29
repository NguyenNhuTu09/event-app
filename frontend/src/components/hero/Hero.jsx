import React from "react";
import './Hero.css';

const Hero = () => {
    return (
        <section className="hero">
            <div className="hero-content">
                <h1>EMS Software:Desk, Room and Event Scheduling Made Easy</h1>
                <p>Streamline your event management process with our intuitive software.</p>

                <div className="hero-cta">
                    <a href="#" className="btn btn-dark">Request a Demo<i className="fa fa-arrow-right"></i></a>
                    <a href="#" className="link-arrow">Watch Demo Instantly<i className="fas fa-arrow-right"></i></a>
                </div>
            </div>
            <div className="hero-image">

            </div>
        </section>
    )
}
export default Hero;
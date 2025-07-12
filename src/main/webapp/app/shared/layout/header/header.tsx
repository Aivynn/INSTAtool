import React, { useState } from 'react';
import './header.scss';
import { NavLink } from 'reactstrap';
import { Link, useNavigate } from 'react-router-dom';

const Header = () => {
  const [menuOpen, setMenuOpen] = useState(false);
  const toggleMenu = () => setMenuOpen(!menuOpen);
  const navigate = useNavigate();

  const handleFaqClick = (e: React.MouseEvent) => {
    e.preventDefault();
    navigate('/faq');
  };
  return (
    <header className="header-gradient">
      <div className="header-inner">
        <Link to="/" className="logo-text">
          INSTA<span>tool</span>
        </Link>
        <nav className="nav-links">
          <a href="#features-section" className="nav-link">
            Features
          </a>
          <NavLink href="/faq" onClick={handleFaqClick} className="nav-link">
            FAQ
          </NavLink>
        </nav>
      </div>
    </header>
  );
};

export default Header;

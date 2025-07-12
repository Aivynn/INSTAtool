import './footer.scss';

import React from 'react';
const Footer = () => (
  <footer className="footer bg-light text-dark py-4 mt-5 border-top">
    <div id="features-section" className="footer-disclaimer-container">
      <h4 className="footer-disclaimer-title">Usage Policy & Disclaimer</h4>
      <p className="footer-disclaimer-text">
        INSTAtool is designed to help users download their own publicly available Instagram media. Misuse — such as downloading private
        content without permission or violating intellectual property — is strictly forbidden.
        <br />
        <br />
        Please review our Terms of Service before using this tool. By continuing to use the platform, you agree to our terms, policies, and
        any future updates.
        <br />
        <br />
        <strong>Key points:</strong>
        <br />• INSTAtool is independently developed and <strong>not affiliated with Instagram or Meta</strong>.<br />
        • The INSTAtool™ branding is created solely for this service.
        <br />• We display minimal ads to maintain operations. Our partners may use cookies — you may opt out by disabling cookies or
        discontinuing use.
      </p>
    </div>
    <div className="footer-section">
      <div className="footer-card">
        <div className="footer-card-icon">🛡️</div>
        <div className="footer-card-title">Secure & Private</div>
        <div className="footer-card-text">We prioritize your privacy with end-to-end encryption and no data logging.</div>
      </div>

      <div className="footer-card">
        <div className="footer-card-icon">⚡</div>
        <div className="footer-card-title">Fast Downloads</div>
        <div className="footer-card-text">Get your media instantly with high-speed delivery.</div>
      </div>
    </div>

    <div className="footer-section">
      <div className="footer-card">
        <div className="footer-card-icon">💸</div>
        <div className="footer-card-title">Completely free</div>
        <div className="footer-card-text">Completely free — only light ads to support the service.</div>
      </div>

      <div className="footer-card">
        <div className="footer-card-icon">📷</div>
        <div className="footer-card-title">Download in high resolutions</div>
        <div className="footer-card-text">Download Photos and Videos from Instagram in Full HD, 2K, or 4K.</div>
      </div>
    </div>
  </footer>
);

export default Footer;

import React from 'react';
import './faq.scss';

const Faq = () => {
  return (
    <div className="faq-container">
      <h2>Frequently Asked Questions</h2>
      <div className="faq-item">
        <h5>🔒 Is InstaTool secure?</h5>
        <p>Yes! We do not store your data. All downloads are private and local to your device.</p>
      </div>
      <div className="faq-item">
        <h5>📷 What kind of media can I download?</h5>
        <p>You can download images, videos, and stories from public Instagram accounts.</p>
      </div>
      <div className="faq-item">
        <h5>💸 Is this tool free?</h5>
        <p>Absolutely. The tool is completely free, supported by minimal ads to help with server costs.</p>
      </div>
      <div className="faq-item">
        <h5>📜 Is it legal to download content from Instagram?</h5>
        <p>You may only download media you own or have permission for. We do not support violating Instagram’s terms.</p>
      </div>
    </div>
  );
};

export default Faq;

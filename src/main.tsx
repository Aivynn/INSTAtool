import React, { useState } from 'react';
import './app.scss';

const Main = () => {
  const [inputValue, setInputValue] = useState('');

  const handleDownload = () => {
    console.log('Download from URL:', inputValue);
    // Trigger download action here
  };

  return (
    <div className="app-container">
      <div className="download-box">
        <h1>Download Videos Effortlessly</h1>
        <p>Paste the video link and get a direct download link in seconds. No watermarks, no ads, just pure video content.</p>
        <div className="download-input">
          <input type="text" placeholder="Paste video link here" value={inputValue} onChange={e => setInputValue(e.target.value)} />
          <button onClick={handleDownload}>Download</button>
        </div>
      </div>

      <div className="features">
        <div className="feature-card">
          <h3>Fast Downloads</h3>
          <p>Get your videos downloaded in seconds with our high-speed servers.</p>
        </div>
        <div className="feature-card">
          <h3>Secure & Private</h3>
          <p>End-to-end encryption and no data logging for your safety.</p>
        </div>
        <div className="feature-card">
          <h3>Always Available</h3>
          <p>Access downloads anytime – 24/7 availability.</p>
        </div>
      </div>
    </div>
  );
};

export default Main;

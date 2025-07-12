import React, { useState } from 'react';
import './app.scss';

const Main = () => {
  const [url, setUrl] = useState('');
  const [mediaItems, setMediaItems] = useState([]);

  const handleSubmit = async () => {
    try {
      const res = await fetch(`/api/submit?url=${encodeURIComponent(url)}`);
      const { size } = await res.json();
      const items = [];

      for (let i = 0; i < size; i++) {
        const blob = await fetch(`/api/media/${i}`).then(r => r.blob());
        const mediaType = blob.type.includes('video') ? 'video' : 'image';
        items.push({ blobUrl: URL.createObjectURL(blob), mediaType });
      }

      setMediaItems(items);
    } catch (error) {
      console.error('Failed to load media:', error);
    }
  };

  return (
    <div className="main-wrapper">
      <div className="container">
        <h1 className="title">Instagram Media Downloader</h1>
        <input className="input-url" value={url} onChange={e => setUrl(e.target.value)} placeholder="Paste Instagram URL here" />
        <button className="download-button" onClick={handleSubmit}>
          Submit
        </button>

        <div className="media-grid">
          {mediaItems.map((item, index) => (
            <div key={index} className="media-card">
              {item.mediaType === 'video' ? <video src={item.blobUrl} controls /> : <img src={item.blobUrl} alt={`media-${index}`} />}
              <a className="download-button" href={item.blobUrl} download>
                Download
              </a>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Main;

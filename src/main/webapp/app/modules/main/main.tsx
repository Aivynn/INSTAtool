import React, { useState } from 'react';
import './main.scss';
import { random } from 'lodash';

type MediaItem = {
  url: string;
  type: string;
  filename: string;
};

const Main: React.FC = () => {
  const [input, setInput] = useState('');
  const [mediaItems, setMediaItems] = useState<MediaItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    setMediaItems([]);

    try {
      const res = await fetch('/api/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: input }),
      });

      if (!res.ok) {
        throw new Error(`Server responded with status ${res.status}`);
      }

      const text = await res.text();
      const count = parseInt(text, 10);

      if (isNaN(count) || count < 1) {
        throw new Error('Invalid media count received from server.');
      }

      const items: MediaItem[] = [];

      for (let i = 0; i < count; i++) {
        const mediaRes = await fetch(`/api/media/${i}`);

        if (!mediaRes.ok) {
          console.error(`Failed to load media ${i}`);
          continue;
        }

        const blob = await mediaRes.blob();
        const contentType = mediaRes.headers.get('Content-Type') || blob.type || 'application/octet-stream';
        const extension = getExtensionFromMime(contentType);
        const filename = `INSTAtool_${crypto.randomUUID()}${extension}`;
        const blobWithType = blob.slice(0, blob.size, contentType);
        const url = URL.createObjectURL(blobWithType);

        items.push({ url, type: contentType, filename });
      }

      setMediaItems(items);
    } catch (err: any) {
      console.error(err);
      setError(err.message || 'An error occurred while fetching media.');
    } finally {
      setLoading(false);
    }
  };

  const getExtensionFromMime = (mime: string): string => {
    const map: { [key: string]: string } = {
      'image/jpeg': '.jpg',
      'image/png': '.png',
      'image/gif': '.gif',
      'video/mp4': '.mp4',
      'video/webm': '.webm',
    };
    return map[mime] || '';
  };

  return (
    <div className="main-wrapper">
      {/* Search Box Section */}
      <div className="search-block">
        <h2>Download your images or videos</h2>
        <input
          type="text"
          value={input}
          onChange={e => setInput(e.target.value)}
          placeholder="Paste Instagram Post URL"
          style={{ width: '400px', padding: '8px', marginRight: '10px' }}
        />
        <button className="download-button" onClick={handleSubmit} disabled={loading}>
          {loading ? 'Loading...' : 'Load media'}
        </button>
        {loading && <div className="loading-bar"></div>}
        {error && <p style={{ color: 'red' }}>{error}</p>}
      </div>

      {/* Media Results Section */}
      <div className="media-block">
        <div className="media-grid">
          {mediaItems.map((item, idx) => (
            <div className="media-card" key={idx}>
              {item.type.startsWith('video') ? (
                <video className="media-element" controls width="320" src={item.url} />
              ) : (
                <img className="media-element" width="320" src={item.url} alt={`media-${idx}`} />
              )}
              <a href={item.url} download={item.filename}>
                <button className="download-button">Download</button>
              </a>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Main;

import React, { useState } from 'react';
import axios from 'axios';

const Home = () => {
  const [url, setUrl] = useState('');
  const [media, setMedia] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    try {
      const countRes = await axios.post('/api/submit', null, {
        params: { url },
      });
      const mediaCount = parseInt(countRes.data, 10);
      const mediaItems = [];

      for (let i = 0; i < mediaCount; i++) {
        const response = await axios.get(`/api/media/${i}`, {
          responseType: 'blob',
        });
        const mediaUrl = URL.createObjectURL(response.data);
        const type = response.headers['content-type'];
        mediaItems.push({ url: mediaUrl, type });
      }

      setMedia(mediaItems);
    } catch (err) {
      setError('Failed to fetch media');
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 text-center p-10">
      <h1 className="text-2xl font-bold mb-5">Instagram Media Downloader</h1>
      <div className="mb-5">
        <input
          type="text"
          value={url}
          onChange={e => setUrl(e.target.value)}
          placeholder="Enter Instagram URL"
          className="border p-2 w-1/2 rounded"
        />
        <button onClick={handleSubmit} className="ml-2 p-2 bg-blue-500 text-white rounded">
          Download
        </button>
      </div>
      {error && <p className="text-red-500">{error}</p>}
      <div className="flex flex-wrap gap-4 justify-center mt-5">
        {media.map((m, idx) => (
          <div key={idx} className="border rounded p-2 bg-white shadow">
            {m.type.startsWith('image') ? (
              <img src={m.url} alt={`media-${idx}`} className="max-w-xs rounded" />
            ) : (
              <video src={m.url} controls className="max-w-xs rounded" />
            )}
            <a href={m.url} download={`media-${idx}`} className="block mt-2 text-blue-600 underline">
              Download
            </a>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Home;

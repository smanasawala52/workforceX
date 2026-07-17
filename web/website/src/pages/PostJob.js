import React, { useState } from 'react';
import API_URL from '../config';

function PostJob() {
  const [title, setTitle] = useState('');
  const [company, setCompany] = useState('');
  const [description, setDescription] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    const job = { title, company, description };

    fetch(`${API_URL}/jobs`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(job)
    })
    .then(response => response.json())
    .then(data => {
      console.log('Job posted:', data);
      // Clear form or redirect
    })
    .catch(error => console.error('Error posting job:', error));
  };

  return (
    <div style={{ padding: '20px', maxWidth: '600px', margin: 'auto' }}>
      <h2>Post a New Job</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        <input type="text" placeholder="Job Title" value={title} onChange={e => setTitle(e.target.value)} required />
        <input type="text" placeholder="Company Name" value={company} onChange={e => setCompany(e.target.value)} required />
        <textarea placeholder="Job Description" value={description} onChange={e => setDescription(e.target.value)} required />
        <button type="submit">Post Job</button>
      </form>
    </div>
  );
}

export default PostJob;
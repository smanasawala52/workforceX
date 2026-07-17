import React, { useState, useEffect } from 'react';
import API_URL from '../config';

function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`${API_URL}/jobs`)
      .then(response => response.json())
      .then(data => {
        setJobs(data);
        setLoading(false);
      })
      .catch(error => {
        console.error('Error fetching jobs:', error);
        setLoading(false);
      });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h2>Job Listings</h2>
      {loading ? (
        <p>Loading jobs...</p>
      ) : (
        <ul>
          {jobs.map(job => (
            <li key={job.id}>{job.title} at {job.company}</li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default Jobs;
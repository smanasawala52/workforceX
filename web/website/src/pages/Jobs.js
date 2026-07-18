import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getJobs().then(data => {
      setJobs(data);
      setLoading(false);
    }).catch(error => {
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
            <li key={job.id}>
              <Link to={`/job/${job.id}`}>{job.title} at {job.companyName}</Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default Jobs;
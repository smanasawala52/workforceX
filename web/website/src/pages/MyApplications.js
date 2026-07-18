import React, { useState, useEffect } from 'react';
import { api } from '../api';

function MyApplications() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getMyApplications().then(data => {
      setApplications(data);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching applications:', error);
      setLoading(false);
    });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h2>My Applications</h2>
      {loading ? (
        <p>Loading applications...</p>
      ) : (
        <ul>
          {applications.map(app => (
            <li key={app.applicationId}>
              {app.jobTitle} at {app.companyName} - {app.status}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default MyApplications;
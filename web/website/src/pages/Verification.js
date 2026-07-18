import React, { useState, useEffect } from 'react';
import { api } from '../api';

function Verification() {
  const [verifications, setVerifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getVerificationStatus().then(data => {
      setVerifications(data);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching verification status:', error);
      setLoading(false);
    });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h2>Verification Status</h2>
      {loading ? (
        <p>Loading verification status...</p>
      ) : (
        <ul>
          {verifications.map(v => (
            <li key={v.id}>
              {v.verificationType}: {v.status}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default Verification;
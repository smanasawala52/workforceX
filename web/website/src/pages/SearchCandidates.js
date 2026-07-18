import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

function SearchCandidates() {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Assuming an API endpoint to get all workers
    api.getWorkers().then(data => {
      setWorkers(data);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching workers:', error);
      setLoading(false);
    });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h2>Search Candidates</h2>
      {loading ? (
        <p>Loading candidates...</p>
      ) : (
        <ul>
          {workers.map(worker => (
            <li key={worker.workerId}>
              <Link to={`/worker/${worker.workerId}`}>{worker.name} - {worker.skills}</Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default SearchCandidates;
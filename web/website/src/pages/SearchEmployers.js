import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

function SearchEmployers() {
  const [employers, setEmployers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Assuming an API endpoint to get all employers
    api.getEmployers().then(data => {
      const employerUsers = data.filter(user => user.role === 'EMPLOYER');
      setEmployers(employerUsers);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching employers:', error);
      setLoading(false);
    });
  }, []);

  return (
    <div style={{ padding: '20px' }}>
      <h2>Search Employers</h2>
      {loading ? (
        <p>Loading employers...</p>
      ) : (
        <ul>
          {employers.map(employer => (
            <li key={employer.id}>
              <Link to={`/employer/${employer.id}`}>{employer.mobileNumber}</Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default SearchEmployers;
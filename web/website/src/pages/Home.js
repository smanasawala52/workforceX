import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import '../App.css';

function Home() {
  const [workers, setWorkers] = useState([]);
  const [employers, setEmployers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.getWorkers(),
      api.getEmployers()
    ]).then(([workerData, employerData]) => {
      setWorkers(workerData);
      const employerUsers = employerData.filter(user => user.role === 'EMPLOYER');
      setEmployers(employerUsers);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching data:', error);
      setLoading(false);
    });
  }, []);

  return (
    <div className="main-content">
      <p className="tagline">Your one-stop solution for jobs and hiring.</p>
      <div className="card-container">
        <div className="card">
          <h2>Workers</h2>
          {loading ? (
            <p>Loading workers...</p>
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
        <div className="card">
          <h2>Employers</h2>
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
      </div>
    </div>
  );
}

export default Home;
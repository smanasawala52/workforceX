import React from 'react';
import { Link } from 'react-router-dom';
import './Card.css';

function Worker({ worker }) {
  return (
    <div className="card">
      <h2>{worker.name}</h2>
      <p>{worker.profession}</p>
      <Link to={`/worker/${worker.id}`} className="card-button">View Profile</Link>
    </div>
  );
}

export default Worker;
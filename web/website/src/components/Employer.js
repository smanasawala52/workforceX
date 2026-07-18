import React from 'react';
import { Link } from 'react-router-dom';
import './Card.css';

function Employer({ employer }) {
  return (
    <div className="card">
      <h2>{employer.name}</h2>
      <p>{employer.industry}</p>
      <Link to={`/employer/${employer.id}`} className="card-button">View Profile</Link>
    </div>
  );
}

export default Employer;
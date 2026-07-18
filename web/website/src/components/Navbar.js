import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">WorkforceX</Link>
      </div>
      <ul className="navbar-links">
        <li><Link to="/jobs">My Jobs</Link></li>
        <li><Link to="/my-applications">My Applications</Link></li>
        <li><Link to="/profile">Worker Profile</Link></li>
        <li><Link to="/employer-profile">Employer Profile</Link></li>
        <li><Link to="/verification">Verification</Link></li>
        <li><Link to="/search-candidates">Search Candidates</Link></li>
        <li><Link to="/search-employers">Search Employers</Link></li>
        <li><Link to="/post-job">Post a Job</Link></li>
        <li><Link to="/notifications">Notifications</Link></li>
        <li><Link to="/login">Login</Link></li>
      </ul>
    </nav>
  );
}

export default Navbar;
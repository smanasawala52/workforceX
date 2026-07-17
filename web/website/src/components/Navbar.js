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
        <li><Link to="/jobs">Find a Job</Link></li>
        <li><Link to="/post-job">Post a Job</Link></li>
        <li><Link to="/login">Login</Link></li>
      </ul>
    </nav>
  );
}

export default Navbar;
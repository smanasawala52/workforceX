import React from 'react';
import '../App.css';
import Worker from '../components/Worker';
import Employer from '../components/Employer';

function Home() {
  return (
    <div className="main-content">
      <p className="tagline">Your one-stop solution for jobs and hiring.</p>
      <div className="card-container">
        <Worker />
        <Employer />
      </div>
    </div>
  );
}

export default Home;
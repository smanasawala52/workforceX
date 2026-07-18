import React, { useState, useEffect } from 'react';
import { api } from '../api';

function EmployerProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getEmployerProfile().then(data => {
      setProfile(data);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching profile:', error);
      setLoading(false);
    });
  }, []);

  const handleSave = (e) => {
    e.preventDefault();
    api.saveEmployerProfile(profile).then(() => {
      alert('Profile saved!');
    }).catch(error => {
      console.error('Error saving profile:', error);
      alert('Error saving profile.');
    });
  };

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  if (loading) {
    return <p>Loading profile...</p>;
  }

  return (
    <div style={{ padding: '20px' }}>
      <h2>Edit Employer Profile</h2>
      <form onSubmit={handleSave}>
        <label>Company Name: <input type="text" name="companyName" value={profile.companyName || ''} onChange={handleChange} /></label><br/>
        <label>Contact Person: <input type="text" name="contactPerson" value={profile.contactPerson || ''} onChange={handleChange} /></label><br/>
        <label>Email: <input type="email" name="email" value={profile.email || ''} onChange={handleChange} /></label><br/>
        <label>Address: <input type="text" name="address" value={profile.address || ''} onChange={handleChange} /></label><br/>
        <button type="submit">Save</button>
      </form>
    </div>
  );
}

export default EmployerProfile;
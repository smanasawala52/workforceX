import React, { useState, useEffect } from 'react';
import { api } from '../api';

function WorkerProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getWorkerProfile().then(data => {
      setProfile(data);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching profile:', error);
      setLoading(false);
    });
  }, []);

  const handleSave = (e) => {
    e.preventDefault();
    api.saveWorkerProfile(profile).then(() => {
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
      <h2>Edit Profile</h2>
      <form onSubmit={handleSave}>
        <label>Name: <input type="text" name="name" value={profile.name || ''} onChange={handleChange} /></label><br/>
        <label>Skills: <input type="text" name="skills" value={profile.skills || ''} onChange={handleChange} /></label><br/>
        <label>Experience: <input type="number" name="experience" value={profile.experience || ''} onChange={handleChange} /></label><br/>
        <label>City: <input type="text" name="city" value={profile.city || ''} onChange={handleChange} /></label><br/>
        <label>Preferred Salary: <input type="number" name="preferredSalary" value={profile.preferredSalary || ''} onChange={handleChange} /></label><br/>
        <label>Description: <textarea name="description" value={profile.description || ''} onChange={handleChange} /></label><br/>
        <button type="submit">Save</button>
      </form>
    </div>
  );
}

export default WorkerProfile;
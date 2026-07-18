import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';

function JobDetailPage() {
  const { id } = useParams();
  const [job, setJob] = useState(null);

  useEffect(() => {
    // Replace with your actual API call
    api.getJob(id).then(setJob);
  }, [id]);

  if (!job) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1>{job.title}</h1>
      <h2>{job.companyName}</h2>
      <p>{job.description}</p>
      <p>Location: {job.location}</p>
      <p>Salary: {job.salaryMin} - {job.salaryMax}</p>
    </div>
  );
}

export default JobDetailPage;
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';

function WorkerDetailPage() {
  const { id } = useParams();
  const [worker, setWorker] = useState(null);

  useEffect(() => {
    // Replace with your actual API call
    api.getWorker(id).then(setWorker);
  }, [id]);

  if (!worker) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1>{worker.name}</h1>
      <p>Profession: {worker.skills}</p>
      <p>Experience: {worker.experience}</p>
      <p>City: {worker.city}</p>
      <p>Preferred Salary: {worker.preferredSalary}</p>
      <p>Description: {worker.description}</p>
    </div>
  );
}

export default WorkerDetailPage;
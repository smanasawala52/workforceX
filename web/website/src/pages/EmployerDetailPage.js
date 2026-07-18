import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { api } from '../api';

function EmployerDetailPage() {
  const { id } = useParams();
  const [employer, setEmployer] = useState(null);

  useEffect(() => {
    // Replace with your actual API call
    api.getEmployer(id).then(setEmployer);
  }, [id]);

  if (!employer) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1>{employer.companyName}</h1>
      <p>Contact Person: {employer.contactPerson}</p>
      <p>Email: {employer.email}</p>
      <p>Address: {employer.address}</p>
    </div>
  );
}

export default EmployerDetailPage;
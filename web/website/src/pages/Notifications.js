import React, { useState, useEffect } from 'react';
import { api } from '../api';

function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getNotifications().then(data => {
      setNotifications(data);
      setLoading(false);
    }).catch(error => {
      console.error('Error fetching notifications:', error);
      setLoading(false);
    });
  }, []);

  return (
    <div>
      <h1>Notifications</h1>
      {loading ? (
        <p>Loading notifications...</p>
      ) : (
        <ul>
          {notifications.map(notification => (
            <li key={notification.id}>{notification.message}</li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default Notifications;
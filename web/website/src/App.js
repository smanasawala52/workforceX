import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './App.css';
import Navbar from './components/Navbar';
import Home from './pages/Home';
import Jobs from './pages/Jobs';
import PostJob from './pages/PostJob';
import Login from './pages/Login';
import Notifications from './pages/Notifications';
import WorkerDetailPage from './pages/WorkerDetailPage';
import EmployerDetailPage from './pages/EmployerDetailPage';
import JobDetailPage from './pages/JobDetailPage';
import SearchCandidates from './pages/SearchCandidates';
import SearchEmployers from './pages/SearchEmployers';
import MyApplications from './pages/MyApplications';
import WorkerProfile from './pages/WorkerProfile';
import Verification from './pages/Verification';
import EmployerProfile from './pages/EmployerProfile';

function App() {
  return (
    <div className="App">
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/jobs" element={<Jobs />} />
          <Route path="/post-job" element={<PostJob />} />
          <Route path="/login" element={<Login />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/worker/:id" element={<WorkerDetailPage />} />
          <Route path="/employer/:id" element={<EmployerDetailPage />} />
          <Route path="/job/:id" element={<JobDetailPage />} />
          <Route path="/search-candidates" element={<SearchCandidates />} />
          <Route path="/search-employers" element={<SearchEmployers />} />
          <Route path="/my-applications" element={<MyApplications />} />
          <Route path="/profile" element={<WorkerProfile />} />
          <Route path="/employer-profile" element={<EmployerProfile />} />
          <Route path="/verification" element={<Verification />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
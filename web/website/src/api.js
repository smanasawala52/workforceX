import API_URL from './config';

export async function api(path, options = {}, token) {
  const headers = { ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }), ...options.headers };
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${API_URL}${path}`, { ...options, headers });
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || data?.error || `Request failed (${response.status})`);
  return data;
}
export const getApiUrl = () => API_URL;

export const getWorkers = (token) => api('/api/matching/search', { method: 'POST', body: JSON.stringify({}) }, token);
export const getWorker = (workerId, token) => api(`/api/worker/profile/${workerId}`, {}, token);
export const getWorkerProfile = (token) => api('/api/worker/profile', {}, token);
export const saveWorkerProfile = (profile, token) => api('/api/worker/profile', { method: 'PUT', body: JSON.stringify(profile) }, token);
export const getEmployers = (token) => api('/api/csr/users', {}, token); // This is not ideal, but there is no endpoint to get all employers
export const getEmployer = (id, token) => api(`/api/employer/profile/${id}`, {}, token);
export const getEmployerProfile = (token) => api('/api/employer/profile', {}, token);
export const saveEmployerProfile = (profile, token) => api('/api/employer/profile', { method: 'PUT', body: JSON.stringify(profile) }, token);
export const getJobs = (token) => api('/api/jobs', {}, token);
export const getJob = (id, token) => api(`/api/jobs/${id}`, {}, token);
export const getNotifications = (token) => api('/api/notifications', {}, token);
export const getMyApplications = (token) => api('/api/applications/my', {}, token);
export const getVerificationStatus = (token) => api('/api/verification/status', {}, token);

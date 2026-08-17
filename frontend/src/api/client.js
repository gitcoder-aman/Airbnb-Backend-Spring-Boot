import axios from 'axios';

const client = axios.create({
  baseURL: '/api',
  timeout: 60000,
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const isAuthUrl = (url) =>
  url.includes('/auth/login') || url.includes('/auth/signup') || url.includes('/auth/refresh');

export async function refreshAccessToken() {
  const res = await axios.post('/api/v1/auth/refresh', null);
  const accessToken = res?.data?.data?.accessToken;
  if (!accessToken) throw new Error('Refresh failed');
  localStorage.setItem('accessToken', accessToken);
  return accessToken;
}

let refreshing = null;

client.interceptors.response.use(
  (response) => {
    const body = response.data;
    if (body === null || body === undefined || body === '' || body === 'OK') return body;
    if (typeof body === 'object' && body !== null && 'data' in body) {
      return body.data;
    }
    return body;
  },
  async (error) => {
    const original = error.config;
    const status = error.response?.status;
    const isRetryable401 =
      status === 401 &&
      original &&
      !original._retried &&
      !isAuthUrl(original.url);

    if (isRetryable401) {
      original._retried = true;
      try {
        if (!refreshing) {
          refreshing = refreshAccessToken().finally(() => {
            refreshing = null;
          });
        }
        const token = await refreshing;
        original.headers = original.headers || {};
        original.headers.Authorization = `Bearer ${token}`;
        return client(original);
      } catch (refreshErr) {
        localStorage.removeItem('accessToken');
        return Promise.reject(buildError(refreshErr, status));
      }
    }

    return Promise.reject(buildError(error, status));
  }
);

function buildError(error, status) {
  const apiError = error?.response?.data?.error;
  if (apiError) {
    return { ...apiError, status: apiError.httpStatus || status };
  }
  return { message: error?.message || 'Something went wrong', status: status || 0 };
}

export default client;
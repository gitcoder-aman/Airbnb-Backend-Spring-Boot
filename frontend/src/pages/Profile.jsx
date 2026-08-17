import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { userApi, authApi } from '../api/api';
import { apiErrorText } from '../utils';
import { usePersistentState, pageKeys } from '../hooks/usePersistentState';

export default function Profile() {
  const { user, refreshUser } = useAuth();
  const [form, setForm] = usePersistentState(pageKeys.profile.form, {
    userId: user?.id || null,
    name: user?.name || '',
    dateOfBirth: user?.dateOfBirth ? user.dateOfBirth.slice(0, 10) : '',
    gender: user?.gender || 'MALE',
  });

  // Seed / reseed the (per-user) draft whenever the logged-in user changes.
  useEffect(() => {
    const userId = user?.id;
    if (!userId) return;
    let draft = null;
    try {
      draft = JSON.parse(localStorage.getItem(pageKeys.profile.form));
    } catch {
      // ignore corrupt draft
    }
    if (!draft || draft.userId !== userId) {
      setForm({
        userId,
        name: user?.name || '',
        dateOfBirth: user?.dateOfBirth ? user.dateOfBirth.slice(0, 10) : '',
        gender: user?.gender || 'MALE',
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);
  const [refreshMsg, setRefreshMsg] = useState('');

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');
    try {
      await userApi.updateProfile({
        name: form.name,
        dateOfBirth: form.dateOfBirth || null,
        gender: form.gender,
      });
      setMessage('Profile updated successfully');
      await refreshUser();
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setSaving(false);
    }
  };

  const handleRefresh = async () => {
    setRefreshMsg('');
    try {
      await authApi.refresh();
      setRefreshMsg('Access token refreshed successfully');
    } catch (err) {
      setRefreshMsg('Refresh failed: ' + apiErrorText(err));
    }
  };

  return (
    <div className="page">
      <div className="form-card" style={{ margin: '0 0 20px', maxWidth: 'none' }}>
        <h2>Profile</h2>
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-label">Email</div>
            <div className="stat-value" style={{ fontSize: '1.1rem' }}>
              {user?.email}
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-label">Member since profile</div>
            <div className="stat-value" style={{ fontSize: '1.1rem' }}>
              {user?.id ? `User #${user.id}` : '—'}
            </div>
          </div>
        </div>
      </div>

      <form className="form-card" onSubmit={save}>
        <h2>Edit profile</h2>
        {error && <div className="form-error">{error}</div>}
        {message && <div className="form-success">{message}</div>}
        <div className="form-group">
          <label>Name</label>
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required minLength={3} />
        </div>
        <div className="form-group">
          <label>Date of birth</label>
          <input type="date" value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} />
        </div>
        <div className="form-group">
          <label>Gender</label>
          <select value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
          </select>
        </div>
        <button className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : 'Save changes'}
        </button>
      </form>

      <div className="form-card" style={{ marginTop: 20 }}>
        <h2>Session</h2>
        <button className="btn btn-outline" onClick={handleRefresh}>
          Refresh access token
        </button>
        {refreshMsg && <p className="muted" style={{ marginTop: 10 }}>{refreshMsg}</p>}
      </div>
    </div>
  );
}
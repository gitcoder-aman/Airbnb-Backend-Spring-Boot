import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useOutletContext } from 'react-router-dom';
import { adminApi } from '../../api/api';
import { apiErrorText, formatCurrency } from '../../utils';

export default function Dashboard() {
  const { hotels, setHotels } = useOutletContext();
  const [reports, setReports] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    adminApi
      .getMyHotels()
      .then((data) => {
        setHotels(data || []);
        return data || [];
      })
      .then(async (list) => {
        const entries = {};
        for (const h of list) {
          try {
            entries[h.id] = await adminApi.getHotelReport(h.id);
          } catch {
            entries[h.id] = null;
          }
        }
        setReports(entries);
      })
      .catch((e) => setError(apiErrorText(e)))
      .finally(() => setLoading(false));
  }, [setHotels]);

  const removeHotel = async (h) => {
    if (!window.confirm(`Delete hotel "${h.name}"? This cannot be undone.`)) return;
    try {
      await adminApi.deleteHotel(h.id);
      setHotels(hotels.filter((x) => x.id !== h.id));
    } catch (e) {
      setError(apiErrorText(e));
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner" />
      </div>
    );
  }

  if (hotels.length === 0) {
    return (
      <div className="empty">
        <h3>You have no hotels yet</h3>
        <p>Create your first hotel listing to start managing rooms and inventory.</p>
        <Link to="/admin/hotels/new" className="btn btn-primary">+ New hotel</Link>
      </div>
    );
  }

  return (
    <>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))' }}>
        {hotels.map((h) => {
          const r = reports[h.id];
          return (
            <div className="hotel-card" key={h.id}>
              <div className="hotel-card-img">
                <img src={h.photos?.[0] || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60'} alt={h.name} />
              </div>
              <div className="hotel-card-body">
                <div className="hotel-card-top">
                  <h3>{h.name}</h3>
                  <span className={`badge ${h.active ? 'badge-green' : 'badge-gray'}`}>
                    {h.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
                <p className="muted">📍 {h.city}</p>
                <div className="stats-grid" style={{ gap: 10, marginTop: 10 }}>
                  <div className="stat-card" style={{ padding: 12 }}>
                    <div className="stat-label">Bookings (30d)</div>
                    <div className="stat-value" style={{ fontSize: '1.2rem' }}>
                      {r ? r.bookingCount : '—'}
                    </div>
                  </div>
                  <div className="stat-card" style={{ padding: 12 }}>
                    <div className="stat-label">Revenue (30d)</div>
                    <div className="stat-value" style={{ fontSize: '1.2rem' }}>
                      {r ? formatCurrency(r.totalRevenue) : '—'}
                    </div>
                  </div>
                  <div className="stat-card" style={{ padding: 12 }}>
                    <div className="stat-label">Avg / booking</div>
                    <div className="stat-value" style={{ fontSize: '1.2rem' }}>
                      {r ? formatCurrency(r.avgRevenue) : '—'}
                    </div>
                  </div>
                </div>
                <div className="row-actions">
                  <Link className="btn btn-sm btn-primary" to={`/admin/hotels/${h.id}`}>
                    Manage
                  </Link>
                  <Link className="btn btn-sm btn-outline" to={`/admin/hotels/${h.id}/rooms`}>
                    Rooms
                  </Link>
                  <button className="btn btn-sm btn-danger" onClick={() => removeHotel(h)}>
                    Delete
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
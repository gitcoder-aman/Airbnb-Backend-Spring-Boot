import { useEffect, useState } from 'react';
import { NavLink, Link, Outlet } from 'react-router-dom';
import { adminApi } from '../../api/api';
import { apiErrorText } from '../../utils';

export default function AdminLayout() {
  const [hotels, setHotels] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    adminApi
      .getMyHotels()
      .then(setHotels)
      .catch((e) => setError(apiErrorText(e)));
  }, []);

  return (
    <div className="page page-lg">
      <h1 className="section-title">Hotel Manager Dashboard</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="admin-layout">
        <aside className="admin-sidenav">
          <NavLink to="/admin" end>
            🏨 My hotels
          </NavLink>
          <NavLink to="/admin/bookings">📅 All bookings</NavLink>
          <Link to="/admin/hotels/new" className="btn btn-primary btn-sm" style={{ marginTop: 6 }}>
            + New hotel
          </Link>
          {hotels.map((h) => (
            <NavLink key={h.id} to={`/admin/hotels/${h.id}`}>
              {h.name}
            </NavLink>
          ))}
        </aside>
        <div className="admin-content">
          <Outlet context={{ hotels, setHotels }} />
        </div>
      </div>
    </div>
  );
}
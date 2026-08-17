import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminApi, bookingApi } from '../../api/api';
import { apiErrorText, formatCurrency, formatDate } from '../../utils';

const statusBadge = (status) => {
  const cls =
    {
      CONFIRMED: 'badge-green',
      RESERVED: 'badge-blue',
      CHECKED_IN: 'badge-amber',
      COMPLETED: 'badge-blue',
      PAYMENT_PENDING: 'badge-amber',
      GUEST_ADDED: 'badge-amber',
      CANCELLED: 'badge-red',
      EXPIRED: 'badge-gray',
    }[status] || 'badge-gray';
  return <span className={`badge ${cls}`}>{status}</span>;
};

const NEW_WINDOW_MS = 10 * 60 * 1000;

const isNew = (createdAt) =>
  !!createdAt && Date.now() - new Date(createdAt).getTime() < NEW_WINDOW_MS;

export default function AllBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');

  const fetchAll = useCallback(async () => {
    setError('');
    try {
      const res = await bookingApi.getAllOwnerBookings();
      setBookings(res || []);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  useEffect(() => {
    const t = setInterval(() => fetchAll(), 20000);
    return () => clearInterval(t);
  }, [fetchAll]);

  const doCheckIn = async (bookingId) => {
    if (!window.confirm('Mark this guest as checked in?')) return;
    try {
      await bookingApi.checkIn(bookingId);
      fetchAll();
    } catch (e) {
      setError(apiErrorText(e));
    }
  };

  const visible =
    filter === 'ALL' ? bookings : bookings.filter((b) => b.bookingStatus === filter);

  return (
    <>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
        <h2 className="section-title" style={{ margin: 0 }}>📅 All bookings</h2>
        <button className="btn btn-sm" onClick={fetchAll} disabled={loading}>
          {loading ? 'Refreshing…' : '↻ Refresh'}
        </button>
        <select style={{ width: 'auto' }} value={filter} onChange={(e) => setFilter(e.target.value)}>
          <option value="ALL">All statuses</option>
          <option>CONFIRMED</option>
          <option>RESERVED</option>
          <option>CHECKED_IN</option>
          <option>COMPLETED</option>
          <option>PAYMENT_PENDING</option>
          <option>GUEST_ADDED</option>
          <option>CANCELLED</option>
          <option>EXPIRED</option>
        </select>
      </div>
      <p className="muted">Newest bookings first, across all your hotels. Auto-refreshes every 20s.</p>
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-screen"><div className="spinner" /></div>
      ) : bookings.length === 0 ? (
        <div className="empty"><h3>No bookings yet</h3><p>Bookings from any of your hotels will appear here.</p></div>
      ) : visible.length === 0 ? (
        <div className="empty"><h3>No bookings with status “{filter}”</h3></div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Hotel</th>
                <th>ID</th>
                <th>Customer</th>
                <th>Room</th>
                <th>Dates</th>
                <th>Total</th>
                <th>Status</th>
                <th>Created</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {visible.map((b) => (
                <tr key={b.id} className={isNew(b.createdAt) ? 'row-new' : ''}>
                  <td>
                    <Link to={`/admin/hotels/${b.hotelId}/bookings`}>{b.hotelName}</Link>
                    {isNew(b.createdAt) && <span className="badge badge-amber" style={{ marginLeft: 6 }}>New</span>}
                  </td>
                  <td>#{b.id}</td>
                  <td>{b.customerName || '—'}</td>
                  <td>{b.roomType || '—'}</td>
                  <td>{formatDate(b.checkInDate)} → {formatDate(b.checkOutDate)}</td>
                  <td>{formatCurrency(b.totalAmount)}</td>
                  <td>{statusBadge(b.bookingStatus)}</td>
                  <td className="muted">{new Date(b.createdAt).toLocaleString('en-IN', { day: 'numeric', month: 'short', hour: 'numeric', minute: '2-digit' })}</td>
                  <td>
                    {b.bookingStatus === 'CONFIRMED' && (
                      <button className="btn btn-sm btn-primary" onClick={() => doCheckIn(b.id)}>
                        Check-in
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}
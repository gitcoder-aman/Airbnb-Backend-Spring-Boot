import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingApi } from '../api/api';
import { apiErrorText, formatDate, formatCurrency } from '../utils';
import {
  useCountdown,
  formatCountdown,
  expiresAtFromBooking,
  usePolling,
} from '../hooks/useCountdown';

const ACTIVE_STATUSES = ['RESERVED', 'GUEST_ADDED', 'PAYMENT_PENDING'];

const statusBadge = (status) => {
  const cls =
    {
      CONFIRMED: 'badge-green',
      RESERVED: 'badge-blue',
      CHECKED_IN: 'badge-amber',
      COMPLETED: 'badge-blue',
      PENDING: 'badge-amber',
      PAYMENT_PENDING: 'badge-amber',
      GUEST_ADDED: 'badge-amber',
      CANCELLED: 'badge-red',
      EXPIRED: 'badge-red',
    }[status] || 'badge-gray';
  return <span className={`badge ${cls}`}>{status}</span>;
};

function BookingRow({ booking, onRefresh }) {
  const deadline = expiresAtFromBooking(booking);
  const remaining = useCountdown(deadline);
  const expired = deadline ? remaining <= 0 : false;

  const poll = usePolling(
    useCallback(async () => {
      if (!ACTIVE_STATUSES.includes(booking.bookingStatus)) return null;
      const res = await bookingApi.getBookingStatus(booking.id);
      return res.status;
    }, [booking.id, booking.bookingStatus]),
    8000,
    ACTIVE_STATUSES.includes(booking.bookingStatus)
  );

  const status = poll || booking.bookingStatus;

  useEffect(() => {
    if (poll && poll !== booking.bookingStatus) onRefresh();
  }, [poll, booking.bookingStatus, onRefresh]);

  const cancel = async () => {
    if (!window.confirm('Cancel this confirmed booking? A refund will be issued.')) return;
    await bookingApi.cancelBooking(booking.id);
    onRefresh();
  };

  const checkStatus = async () => {
    try {
      const res = await bookingApi.getBookingStatus(booking.id);
      alert('Booking status: ' + res.status);
    } catch (e) {
      alert('Error: ' + apiErrorText(e));
    }
  };

  return (
    <tr>
      <td>#{booking.id}</td>
      <td>
        {formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)}
        <br />
        <span className="muted" style={{ fontSize: '0.8rem' }}>
          booked {booking.createdAt ? new Date(booking.createdAt).toLocaleString() : ''}
        </span>
      </td>
      <td>{booking.roomCount}</td>
      <td>{formatCurrency(booking.totalAmount)}</td>
      <td>{statusBadge(status)}</td>
      <td style={{ minWidth: 170 }}>
        {ACTIVE_STATUSES.includes(status) && (
          <span
            className={`badge ${expired ? 'badge-red' : 'badge-amber'}`}
            style={{ fontSize: '0.95rem', fontVariantNumeric: 'tabular-nums' }}
          >
            {expired ? 'Expired' : `⏳ ${formatCountdown(remaining)} left`}
          </span>
        )}
      </td>
      <td>
        <div className="row-actions" style={{ marginTop: 0 }}>
          <button className="btn btn-sm btn-outline" onClick={checkStatus}>
            Check status
          </button>
          {status === 'CONFIRMED' && (
            <button className="btn btn-sm btn-danger" onClick={cancel}>
              Cancel
            </button>
          )}
        </div>
      </td>
    </tr>
  );
}

export default function MyBookings() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchBookings = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await bookingApi.getMyBookings();
      setBookings(res || []);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchBookings();
  }, [fetchBookings]);

  return (
    <div className="page">
      <h1 className="section-title">My bookings</h1>
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-screen">
          <div className="spinner" />
        </div>
      ) : bookings.length === 0 ? (
        <div className="empty">
          <h3>No bookings yet</h3>
          <p>Find a place to stay to see your bookings here.</p>
          <Link to="/" className="btn btn-outline">Browse stays</Link>
        </div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Dates</th>
                <th>Rooms</th>
                <th>Total</th>
                <th>Status</th>
                <th>Reservation window</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <BookingRow key={b.id} booking={b} onRefresh={fetchBookings} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
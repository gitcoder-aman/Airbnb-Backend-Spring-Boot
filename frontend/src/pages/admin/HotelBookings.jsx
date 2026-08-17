import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi, bookingApi } from '../../api/api';
import { apiErrorText, formatDate, formatCurrency } from '../../utils';

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

export default function HotelBookings() {
  const { hotelId } = useParams();
  const [hotel, setHotel] = useState(null);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [h, b] = await Promise.all([
          adminApi.getHotelById(hotelId),
          adminApi.getHotelBookings(hotelId),
        ]);
        setHotel(h);
        setBookings(b || []);
      } catch (e) {
        setError(apiErrorText(e));
      } finally {
        setLoading(false);
      }
    })();
  }, [hotelId]);

  const doCheckIn = async (bookingId) => {
    if (!window.confirm('Mark this guest as checked in?')) return;
    try {
      await bookingApi.checkIn(bookingId);
      const b = await adminApi.getHotelBookings(hotelId);
      setBookings(b || []);
    } catch (e) {
      setError(apiErrorText(e));
    }
  };

  return (
    <>
      <Link to={`/admin/hotels/${hotelId}`} className="btn btn-sm btn-ghost">← {hotel?.name || 'Hotel'}</Link>
      <h2 className="section-title">Bookings — {hotel?.name}</h2>
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-screen"><div className="spinner" /></div>
      ) : bookings.length === 0 ? (
        <div className="empty"><h3>No bookings for this hotel yet</h3></div>
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
                <th>Guests</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.id}>
                  <td>#{b.id}</td>
                  <td>{formatDate(b.checkInDate)} → {formatDate(b.checkOutDate)}</td>
                  <td>{b.roomCount}</td>
                  <td>{formatCurrency(b.totalAmount)}</td>
                  <td>{statusBadge(b.bookingStatus)}</td>
                  <td>{b.guests?.length} guest(s)</td>
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
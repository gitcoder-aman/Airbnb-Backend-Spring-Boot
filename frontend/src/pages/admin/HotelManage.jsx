import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi } from '../../api/api';
import { apiErrorText, formatCurrency } from '../../utils';

export default function HotelManage() {
  const { hotelId } = useParams();
  const [hotel, setHotel] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const fetchHotel = async () => {
    setLoading(true);
    setError('');
    try {
      const h = await adminApi.getHotelById(hotelId);
      setHotel(h);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHotel();
  }, [hotelId]);

  const activate = async () => {
    setBusy(true);
    setError('');
    try {
      const h = await adminApi.activateHotel(hotelId);
      setHotel(h);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setBusy(false);
    }
  };

  const toggleActive = async () => {
    setBusy(true);
    setError('');
    try {
      const h = await adminApi.updatePartialHotel(hotelId, { active: !hotel.active });
      setHotel(h);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setBusy(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner" />
      </div>
    );
  }

  if (!hotel) {
    return (
      <div className="empty">
        <h3>{error || 'Hotel not found'}</h3>
        <Link to="/admin" className="btn btn-outline">Back</Link>
      </div>
    );
  }

  return (
    <>
      <Link to="/admin" className="btn btn-sm btn-ghost">← Dashboard</Link>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="hotel-hero" style={{ marginTop: 12 }}>
        <div className="hotel-hero-img" style={{ height: 240 }}>
          <img src={hotel.photos?.[0]} alt={hotel.name} />
        </div>
        <div className="hotel-hero-body">
          <div className="hotel-card-top">
            <h1>{hotel.name}</h1>
            <span className={`badge ${hotel.active ? 'badge-green' : 'badge-gray'}`}>
              {hotel.active ? 'Active' : 'Inactive'}
            </span>
          </div>
          <p className="muted">
            {hotel.city} · {hotel.startingPrice ? `from ${formatCurrency(hotel.startingPrice)}/night` : 'no price set'}
          </p>
          <p className="room-meta">
            📍 {hotel.contactInfo?.address}, {hotel.contactInfo?.location}
            <br />📞 {hotel.contactInfo?.phoneNumber} · ✉️ {hotel.contactInfo?.email}
            <br />
          </p>
          <div className="row-actions">
            <Link className="btn btn-sm btn-primary" to={`/admin/hotels/${hotel.id}/rooms`}>
              Manage rooms
            </Link>
            <Link className="btn btn-sm btn-outline" to={`/admin/hotels/${hotel.id}/inventory`}>
              Inventory & pricing
            </Link>
            <Link className="btn btn-sm btn-outline" to={`/admin/hotels/${hotel.id}/bookings`}>
              Bookings & check-in
            </Link>
            <Link className="btn btn-sm btn-outline" to={`/admin/hotels/${hotel.id}/edit`}>
              Edit hotel
            </Link>
            {!hotel.active && (
              <button className="btn btn-sm btn-primary" onClick={activate} disabled={busy}>
                {busy ? 'Activating…' : 'Activate & generate inventory'}
              </button>
            )}
            <button className="btn btn-sm btn-outline" onClick={toggleActive} disabled={busy}>
              {hotel.active ? 'Deactivate' : 'Set active'}
            </button>
          </div>
        </div>
      </div>

      <h2 className="section-title">Amenities</h2>
      <p>{hotel.amenities?.map((a) => `✓ ${a}`).join('  ·  ')}</p>

      <h2 className="section-title">Photos</h2>
      <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))' }}>
        {hotel.photos?.map((p, i) => (
          <img key={i} src={p} alt={`${hotel.name} ${i + 1}`} style={{ width: '100%', borderRadius: 10, objectFit: 'cover', height: 160 }} />
        ))}
      </div>
    </>
  );
}
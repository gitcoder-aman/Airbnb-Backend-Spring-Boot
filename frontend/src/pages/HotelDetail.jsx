import { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { hotelApi, reviewApi } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { formatCurrency, formatDate, apiErrorText } from '../utils';
import { usePersistentState, pageKeys } from '../hooks/usePersistentState';

const today = () => new Date().toISOString().split('T')[0];

export default function HotelDetail() {
  const { hotelId } = useParams();
  const navigate = useNavigate();
  const { token, isGuest } = useAuth();

  const [info, setInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [dates, setDates] = useState({
    checkInDate: today(),
    checkOutDate: '',
    numberOfRooms: 1,
  });
  const [rooms, setRooms] = useState([]);
  const [roomsLoading, setRoomsLoading] = useState(false);

  const [reviews, setReviews] = useState(null);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [reviewPage, setReviewPage] = useState(0);
  const [hasPhotos, setHasPhotos] = usePersistentState(pageKeys.hotelDetail.photos(hotelId), '');
  const [selectedRoomId, setSelectedRoomId] = usePersistentState(pageKeys.hotelDetail.roomId(hotelId), '');
  const [canReview, setCanReview] = useState(false);

  const hotel = info?.hotel;

  useEffect(() => {
    (async () => {
      setLoading(true);
      setError('');
      try {
        const res = await hotelApi.getHotelInfo(hotelId);
        setInfo(res);
        if (res.rooms?.length) {
          // Keep the previously selected room if it still exists, else fall back to the first.
          setSelectedRoomId((prev) =>
            prev && res.rooms.some((r) => String(r.id) === String(prev))
              ? prev
              : String(res.rooms[0].id)
          );
        }
      } catch (e) {
        setError(apiErrorText(e));
      } finally {
        setLoading(false);
      }
    })();
  }, [hotelId]);

  useEffect(() => {
    if (!selectedRoomId) return;
    fetchReviews(0);
    if (token && isGuest) {
      reviewApi.hasCompletedBookingForReview(selectedRoomId).then(setCanReview).catch(() => setCanReview(false));
    }
  }, [selectedRoomId, token, isGuest]);

  const fetchReviews = async (pageNum, photosFilter = hasPhotos) => {
    setReviewsLoading(true);
    try {
      const params = { roomId: selectedRoomId, page: pageNum, size: 5, sortBy: 'createdAt', direction: 'desc' };
      if (photosFilter) params.hasPhotos = true;
      const res = await reviewApi.getReviews(params);
      setReviews(res);
      setReviewPage(pageNum);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setReviewsLoading(false);
    }
  };

  const togglePhotos = () => {
    const next = hasPhotos ? '' : 'yes';
    setHasPhotos(next);
    fetchReviews(0, next);
  };

  const fetchRooms = async (e) => {
    e.preventDefault();
    setRoomsLoading(true);
    setRooms([]);
    setError('');
    try {
      const res = await hotelApi.getRoomsByHotel(hotelId, dates.checkInDate, dates.checkOutDate);
      setRooms(res);
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setRoomsLoading(false);
    }
  };

  const startBooking = (room) => {
    navigate('/checkout', {
      state: { hotel, room, ...dates },
    });
  };

  const gallery = useMemo(
    () => (hotel?.photos?.length ? hotel.photos : ['https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200&q=60']),
    [hotel]
  );

  if (loading) {
    return (
      <div className="page">
        <div className="loading-screen">
          <div className="spinner" />
        </div>
      </div>
    );
  }

  if (error && !hotel) {
    return (
      <div className="page">
        <div className="empty">
          <h3>Unable to load hotel</h3>
          <p>{error}</p>
          <Link to="/" className="btn btn-outline">
            Back to home
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page page-lg">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="hotel-hero">
        <div className="hotel-hero-img">
          <img src={gallery[0]} alt={hotel.name} />
        </div>
        <div className="hotel-hero-body">
          <h1>{hotel.name}</h1>
          <p className="muted">
            {hotel.city} · {hotel.startingPrice != null ? `from ${formatCurrency(hotel.startingPrice)}/night` : ''}
          </p>
          <p>
            {hotel.amenities?.map((a) => `✓ ${a}`).join('  ·  ')}
          </p>
          <p className="muted" style={{ marginTop: 10, fontSize: '0.9rem' }}>
            📍 {hotel.contactInfo?.address} · {hotel.contactInfo?.location}
            <br />
            📞 {hotel.contactInfo?.phoneNumber} · ✉️ {hotel.contactInfo?.email}
          </p>
        </div>
      </div>

      <h2 className="section-title">Book a room at {hotel.name}</h2>
      <div className="form-card" style={{ maxWidth: 'none', margin: '0 0 24px' }}>
        <form className="search-row" onSubmit={fetchRooms}>
          <div className="field">
            <label>Check-in</label>
            <input
              type="date"
              required
              value={dates.checkInDate}
              onChange={(e) => setDates({ ...dates, checkInDate: e.target.value })}
            />
          </div>
          <div className="field">
            <label>Check-out</label>
            <input
              type="date"
              required
              value={dates.checkOutDate}
              onChange={(e) => setDates({ ...dates, checkOutDate: e.target.value })}
            />
          </div>
          <div className="field" style={{ maxWidth: 120 }}>
            <label>Rooms</label>
            <input
              type="number"
              min="1"
              value={dates.numberOfRooms}
              onChange={(e) => setDates({ ...dates, numberOfRooms: e.target.value })}
            />
          </div>
          <button className="btn btn-primary" disabled={roomsLoading}>
            {roomsLoading ? 'Checking…' : 'Show rooms & prices'}
          </button>
        </form>

        {(roomsLoading || rooms.length > 0) && (
          <div className={roomsLoading ? 'loading-inline' : ''} style={{ marginTop: 16 }}>
            {roomsLoading ? (
              <div className="spinner" />
            ) : (
              rooms.map((room) => (
                <div className="room-card" key={room.id}>
                  <img
                    className="room-card-img"
                    src={room.photos?.[0] || 'https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=400&q=60'}
                    alt={room.type}
                  />
                  <div className="room-info">
                    <h4>{room.type}</h4>
                    <p className="room-meta">
                      Sleeps {room.capacity} · {room.totalCount} available of this type
                      <br />
                      {room.amenities?.map((a) => `• ${a}`).join(' ')}
                      <br />
                      {room.description}
                    </p>
                  </div>
                  <div className="room-price">
                    {formatCurrency(room.totalPrice)}
                    <small className="muted" style={{ display: 'block', fontWeight: 400 }}>
                      for your stay
                    </small>
                    <button className="btn btn-primary btn-sm" onClick={() => startBooking(room)}>
                      Reserve
                    </button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}
      </div>

      <h2 className="section-title">Guest reviews</h2>
      <div className="segment" style={{ marginTop: 0 }}>
        <select
          value={selectedRoomId}
          onChange={(e) => setSelectedRoomId(e.target.value)}
          style={{ padding: '9px 14px', borderRadius: 999, border: '1px solid var(--border)' }}
        >
          {(info.rooms || []).map((r) => (
            <option key={r.id} value={r.id}>
              {r.type}
            </option>
          ))}
        </select>
        <button
          className={hasPhotos ? 'active' : ''}
          onClick={togglePhotos}
        >
          With photos
        </button>
      </div>

      {token && isGuest && canReview && selectedRoomId && (
        <ReviewForm roomId={selectedRoomId} onCreated={() => fetchReviews(0)} />
      )}

      {reviewsLoading ? (
        <div className="loading-inline">
          <div className="spinner" />
        </div>
      ) : reviews?.content?.length === 0 ? (
        <div className="empty">
          <h3>No reviews yet</h3>
          <p>Be the first to review this room.</p>
        </div>
      ) : (
        <>
          {reviews?.content?.map((r) => (
            <div className="review-card" key={r.id}>
              <div className="review-top">
                <span className="avatar">{r.userName?.[0] || 'U'}</span>
                <div>
                  <strong>{r.userName}</strong>
                  {r.verified && <span className="verified"> ✓ Verified stay</span>}
                  <div className="stars">{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</div>
                  <span className="muted" style={{ fontSize: '0.8rem' }}>
                    {r.createdAt ? formatDate(r.createdAt) : ''}
                  </span>
                </div>
              </div>
              <p>{r.comment}</p>
              {r.photos?.length > 0 && (
                <div className="review-photos">
                  {r.photos.map((p, i) => (
                    <img key={i} src={p} alt="review" />
                  ))}
                </div>
              )}
            </div>
          ))}
          {reviews && reviews.totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-sm btn-outline"
                disabled={reviews.first}
                onClick={() => fetchReviews(reviewPage - 1)}
              >
                Prev
              </button>
              <span>
                Page {reviewPage + 1} of {reviews.totalPages}
              </span>
              <button
                className="btn btn-sm btn-outline"
                disabled={reviews.last}
                onClick={() => fetchReviews(reviewPage + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

function ReviewForm({ roomId, onCreated }) {
  const [form, setForm] = useState({ rating: 5, comment: '' });
  const [photoInput, setPhotoInput] = useState('');
  const [photos, setPhotos] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await reviewApi.createReview(roomId, { ...form, photos });
      setSuccess('Review submitted. Thank you!');
      setForm({ rating: 5, comment: '' });
      setPhotos([]);
      setPhotoInput('');
      onCreated();
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="form-card" style={{ maxWidth: 'none', margin: '0 0 20px' }} onSubmit={submit}>
      <h3>Write a review</h3>
      {error && <div className="form-error">{error}</div>}
      {success && <div className="form-success">{success}</div>}
      <div className="form-group">
        <label>Rating</label>
        <select value={form.rating} onChange={(e) => setForm({ ...form, rating: Number(e.target.value) })}>
          {[1, 2, 3, 4, 5].map((n) => (
            <option key={n} value={n}>
              {n} star{n > 1 ? 's' : ''}
            </option>
          ))}
        </select>
      </div>
      <div className="form-group">
        <label>Comment</label>
        <textarea
          required
          value={form.comment}
          onChange={(e) => setForm({ ...form, comment: e.target.value })}
          placeholder="Share your experience…"
        />
      </div>
      <div className="form-group">
        <label>Add photo URL (optional, max 5)</label>
        <div style={{ display: 'flex', gap: 8 }}>
          <input
            value={photoInput}
            onChange={(e) => setPhotoInput(e.target.value)}
            placeholder="https://…"
          />
          <button
            type="button"
            className="btn btn-outline btn-sm"
            onClick={() => {
              if (photoInput && photos.length < 5) {
                setPhotos([...photos, photoInput]);
                setPhotoInput('');
              }
            }}
          >
            Add
          </button>
        </div>
        {photos.length > 0 && (
          <div className="review-photos">
            {photos.map((p, i) => (
              <img key={i} src={p} alt="preview" />
            ))}
          </div>
        )}
      </div>
      <button className="btn btn-primary" disabled={submitting}>
        {submitting ? 'Submitting…' : 'Submit review'}
      </button>
    </form>
  );
}
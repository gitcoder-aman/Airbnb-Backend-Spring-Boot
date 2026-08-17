import { useCallback, useEffect, useState } from 'react';
import { useLocation, Link, useNavigate } from 'react-router-dom';
import { bookingApi } from '../api/api';
import { useAuth } from '../context/AuthContext';
import { apiErrorText, formatCurrency, formatDate } from '../utils';
import {
  useCountdown,
  formatCountdown,
  expiresAtFromBooking,
  usePolling,
} from '../hooks/useCountdown';
import { loadDraft, saveDraft, clearDraft } from '../hooks/useDraft';

const ACTIVE_STATUSES = ['RESERVED', 'GUEST_ADDED', 'PAYMENT_PENDING'];
const TERMINAL_STATUSES = ['CONFIRMED', 'CANCELLED', 'EXPIRED'];

function ageFromDateOfBirth(dob) {
  if (!dob) return '';
  const birth = new Date(dob);
  const now = new Date();
  let age = now.getFullYear() - birth.getFullYear();
  const m = now.getMonth() - birth.getMonth();
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--;
  return age > 0 && age < 120 ? age : '';
}

export default function Checkout() {
  const location = useLocation();
  const initial = location.state;
  const draft = loadDraft();

  // Came here with a fresh room selection from a hotel page
  if (initial?.hotel && initial?.room) {
    return <BookingWizard key={`fresh-${initial.room.id}`} initial={initial} />;
  }

  // Came back to /checkout with an unfinished booking → resume where they left off
  if (draft && draft.booking) {
    return <BookingWizard key={`resume-${draft.booking.id}`} resumeData={draft} />;
  }

  return (
    <div className="page">
      <div className="empty">
        <h3>Nothing to book</h3>
        <p>Select a room from a hotel to start a booking.</p>
        <Link to="/" className="btn btn-outline">Browse stays</Link>
      </div>
    </div>
  );
}

function BookingWizard({ initial, resumeData }) {
  const { user: currentUser } = useAuth();
  const navigate = useNavigate();
  const resumed = Boolean(resumeData);

  const bookingData = initial || resumeData?.initial;
  const { hotel, room, checkInDate, checkOutDate, numberOfRooms } = bookingData || {};

  const [step, setStep] = useState(resumeData?.step || 1);
  const [booking, setBooking] = useState(resumeData?.booking || null);
  const [guests, setGuests] = useState(resumeData?.guests || []);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [sessionUrl, setSessionUrl] = useState(resumeData?.sessionUrl || '');
  const [paying, setPaying] = useState(false);

  const deadline = booking ? expiresAtFromBooking(booking) : null;
  const remaining = useCountdown(deadline);
  const expired = deadline ? remaining <= 0 : false;

  const primaryGuest = {
    name: currentUser?.name || '',
    gender: currentUser?.gender || 'MALE',
    age: ageFromDateOfBirth(currentUser?.dateOfBirth),
  };

  // Starting a brand new reservation replaces any leftover draft
  useEffect(() => {
    if (!resumed && initial) clearDraft();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Persist the in-progress booking so it can be resumed later
  useEffect(() => {
    if (!booking || !hotel || !room) return;
    saveDraft({
      initial: { hotel, room, checkInDate, checkOutDate, numberOfRooms },
      booking,
      step,
      guests,
      sessionUrl,
    });
  }, [booking, step, guests, sessionUrl, hotel, room, checkInDate, checkOutDate, numberOfRooms]);

  const initBooking = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await bookingApi.initBooking({
        hotelId: Number(hotel.id),
        roomId: Number(room.id),
        checkInDate,
        checkOutDate,
        numberOfRooms: Number(numberOfRooms),
      });
      setBooking(res);
      setGuests([primaryGuest]);
      setStep(2);
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setLoading(false);
    }
  };

  const addGuests = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const payload = guests.map((g) => ({
        user: { id: 0 },
        name: g.name,
        gender: g.gender,
        age: Number(g.age),
      }));
      const updated = await bookingApi.addGuests(booking.id, payload);
      setBooking(updated);
      setStep(3);
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setLoading(false);
    }
  };

  const initiatePayment = async () => {
    setPaying(true);
    setError('');
    try {
      const res = await bookingApi.initiatePayment(booking.id);
      setSessionUrl(res.sessionUrl);
      setBooking((b) => ({ ...b, bookingStatus: 'PAYMENT_PENDING' }));
      window.open(res.sessionUrl, '_blank');
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setPaying(false);
    }
  };

  const refreshBooking = useCallback(async () => {
    const res = await bookingApi.getBookingStatus(booking?.id);
    return res.status;
  }, [booking?.id]);

  const pollStatus = usePolling(
    refreshBooking,
    5000,
    Boolean(booking) &&
      ACTIVE_STATUSES.includes(booking?.bookingStatus) &&
      step === 3
  );

  const status = pollStatus || booking?.bookingStatus;

  // Clear the draft once the booking finishes
  useEffect(() => {
    if (booking && TERMINAL_STATUSES.includes(status)) clearDraft();
  }, [booking, status]);

  const updateGuest = (i, key, value) => {
    const next = [...guests];
    next[i] = { ...next[i], [key]: value };
    setGuests(next);
  };

  const addGuest = () => {
    if (guests.length >= 10) return;
    setGuests([...guests, { name: '', gender: 'MALE', age: '' }]);
  };

  const removeGuest = (i) => {
    if (guests.length <= 1) return;
    setGuests(guests.filter((_, idx) => idx !== i));
  };

  const discardAndReset = async () => {
    const bookingId = booking?.id;
    if (bookingId) {
      try {
        await bookingApi.expireBooking(bookingId);
      } catch {
        // booking already gone/expired — still clear locally
      }
    }
    clearDraft();
    navigate('/');
  };

  if (!hotel || !room) {
    return (
      <div className="page">
        <div className="empty">
          <h3>Unable to resume booking</h3>
          <p>Some booking details are missing.</p>
          <Link to="/" className="btn btn-outline">Browse stays</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      {resumed && booking && (
        <div className="alert alert-success">
          ↩️ Resumed your booking <strong>#{booking.id}</strong> from where you left off (step {step} of 3).
          The reservation hold is still active — continue below.
        </div>
      )}

      <div className="segment">
        {['Details', 'Guests', 'Payment'].map((label, i) => (
          <button key={label} className={step === i + 1 ? 'active' : ''}>
            {i + 1}. {label}
          </button>
        ))}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {booking && deadline && (
        <div className={`alert ${expired ? 'alert-error' : ''}`} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
          <span>
            {expired ? '⛔ This reservation has expired.' : '⏳ Reservation is held for you. Complete checkout before it expires.'}
          </span>
          {!expired && (
            <strong style={{ fontVariantNumeric: 'tabular-nums', fontSize: '1.1rem' }}>
              {formatCountdown(remaining)}
            </strong>
          )}
        </div>
      )}

      <div className="admin-layout">
        <div className="form-card" style={{ margin: 0, maxWidth: 'none', flex: 1 }}>
          {step === 1 && (
            <>
              <h2>Review your stay</h2>
              <div className="room-card" style={{ alignItems: 'flex-start' }}>
                <div className="room-info">
                  <h4>{hotel.name}</h4>
                  <p className="room-meta">
                    {room.type} · Sleeps {room.capacity}
                    <br />
                    📅 {formatDate(checkInDate)} → {formatDate(checkOutDate)}
                    <br />
                    Rooms: {numberOfRooms}
                    <br />
                    {room.amenities?.map((a) => `• ${a}`).join(' ')}
                  </p>
                </div>
                <div className="room-price">
                  {formatCurrency(room.totalPrice)}
                  <small className="muted" style={{ display: 'block', fontWeight: 400 }}>
                    estimated total
                  </small>
                </div>
              </div>
              <button className="btn btn-primary btn-block" onClick={initBooking} disabled={loading}>
                {loading ? 'Reserving…' : 'Confirm & reserve'}
              </button>
            </>
          )}

          {step === 2 && booking && (
            <>
              <h2>Guest details</h2>
              <p className="muted" style={{ marginBottom: 16 }}>
                Booking #{booking.id} · only add people who will actually stay. The first guest is you.
              </p>
              {guests.map((g, i) => (
                <div key={i} className="form-card" style={{ boxShadow: 'none', border: '1px solid var(--border)', padding: 16, margin: '0 0 12px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <h4 style={{ margin: 0 }}>{i === 0 ? 'Guest 1 (you)' : `Guest ${i + 1}`}</h4>
                    {i > 0 && (
                      <button type="button" className="btn btn-sm btn-danger" onClick={() => removeGuest(i)}>
                        Remove guest
                      </button>
                    )}
                  </div>
                  <div className="form-grid-2">
                    <div className="form-group">
                      <label>Name</label>
                      <input required value={g.name} onChange={(e) => updateGuest(i, 'name', e.target.value)} />
                    </div>
                    <div className="form-group">
                      <label>Age</label>
                      <input required type="number" min="1" max="120" value={g.age} onChange={(e) => updateGuest(i, 'age', e.target.value)} />
                    </div>
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label>Gender</label>
                    <select value={g.gender} onChange={(e) => updateGuest(i, 'gender', e.target.value)}>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                </div>
              ))}
              <div className="row-actions" style={{ justifyContent: 'space-between' }}>
                <button type="button" className="btn btn-outline" onClick={addGuest} disabled={guests.length >= 10}>
                  + Add guest
                </button>
                <button className="btn btn-primary" onClick={addGuests} disabled={loading || expired}>
                  {loading ? 'Saving…' : 'Save guest details'}
                </button>
              </div>
            </>
          )}

          {step === 3 && booking && (
            <>
              <h2>Payment</h2>
              <p className="muted" style={{ marginBottom: 16 }}>
                Booking #{booking.id} · total {formatCurrency(booking.totalAmount)} (incl. tax {formatCurrency(booking.taxAmount)})
              </p>

              {status === 'CONFIRMED' && (
                <div className="alert alert-success">
                  ✅ Payment confirmed! Your booking is confirmed.
                </div>
              )}
              {status === 'EXPIRED' && (
                <div className="alert alert-error">
                  ⛔ This booking expired and the rooms were released. Please start a new booking.
                </div>
              )}

              {!sessionUrl ? (
                <button
                  className="btn btn-primary btn-block"
                  onClick={initiatePayment}
                  disabled={paying || expired || status === 'CONFIRMED' || status === 'EXPIRED'}
                >
                  {paying ? 'Opening Stripe…' : 'Pay with Stripe'}
                </button>
              ) : (
                <>
                  <div className="alert alert-success">
                    Stripe Checkout is open in a new tab. Once you complete the payment there, this page updates automatically.
                  </div>
                  <div className="row-actions">
                    <a className="btn btn-outline" href={sessionUrl} target="_blank" rel="noreferrer">
                      Reopen payment
                    </a>
                    <Link className="btn btn-primary" to="/my-bookings">
                      My bookings
                    </Link>
                  </div>
                </>
              )}
              {status === 'EXPIRED' && (
                <Link className="btn btn-outline btn-block" to="/" style={{ marginTop: 12 }}>
                  Browse other stays
                </Link>
              )}
            </>
          )}

          <div className="row-actions" style={{ marginTop: 20 }}>
            <button className="btn btn-ghost" onClick={discardAndReset}>
              ✕ Discard booking & go home
            </button>
          </div>
        </div>

        {booking && (
          <div className="form-card" style={{ margin: 0, maxWidth: 260 }}>
            <h3>Booking #{booking.id}</h3>
            <p className="muted">
              {formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)}
              <br />
              {booking.roomCount} room(s)
            </p>
            <hr style={{ margin: '12px 0', border: 'none', borderTop: '1px solid var(--border)' }} />
            <p>
              Subtotal: {formatCurrency(booking.subTotalAmount)}
              <br />
              Tax: {formatCurrency(booking.taxAmount)}
              <br />
              <strong>Total: {formatCurrency(booking.totalAmount)}</strong>
            </p>
            <hr style={{ margin: '12px 0', border: 'none', borderTop: '1px solid var(--border)' }} />
            <p>
              Status: <strong>{status}</strong>
            </p>
            {ACTIVE_STATUSES.includes(status) && (
              <p className={`${expired ? 'alert-error' : ''}`} style={{ fontSize: '0.85rem' }}>
                {expired ? 'Expired — rooms released' : `Auto-expires in ${formatCountdown(remaining)}`}
              </p>
            )}
            {status === 'CONFIRMED' && (
              <button className="btn btn-outline btn-sm btn-block" onClick={() => navigate('/my-bookings')}>
                Go to bookings
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
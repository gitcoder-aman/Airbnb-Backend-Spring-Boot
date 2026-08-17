import { Link } from 'react-router-dom';

export default function PaymentResult({ ok }) {
  return (
    <div className="page">
      <div
        className="form-card"
        style={{ textAlign: 'center', maxWidth: 480, paddingTop: 40, paddingBottom: 40 }}
      >
        <div
          style={{
            width: 88,
            height: 88,
            borderRadius: '50%',
            margin: '0 auto 20px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 40,
            color: ok ? '#1a7f37' : '#c0392b',
            background: ok ? '#e7f6e7' : '#ffe9e9',
          }}
        >
          {ok ? '✓' : '✕'}
        </div>
        <h2>{ok ? 'Payment successful!' : 'Payment not completed'}</h2>
        <p className="muted">
          {ok
            ? 'Your stay is confirmed. You can view the details and any future bookings in My Bookings.'
            : 'No payment was charged. Your reservation is still held — you can try again or browse other stays.'}
        </p>
        <div className="row-actions" style={{ justifyContent: 'center' }}>
          <Link to="/my-bookings" className="btn btn-primary">
            My bookings
          </Link>
          <Link to="/" className="btn btn-outline">
            Browse stays
          </Link>
        </div>
      </div>
    </div>
  );
}
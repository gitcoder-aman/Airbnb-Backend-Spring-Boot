import { Link } from 'react-router-dom';
import { formatCurrency } from '../utils';

export default function HotelCard({ hotel, price }) {
  const photo = hotel.photos?.[0] || 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=60';
  return (
    <Link to={`/hotels/${hotel.id}`} className="hotel-card">
      <div className="hotel-card-img">
        <img src={photo} alt={hotel.name} loading="lazy" />
      </div>
      <div className="hotel-card-body">
        <div className="hotel-card-top">
          <h3>{hotel.name}</h3>
          {hotel.active !== undefined && (
            <span className={`badge ${hotel.active ? 'badge-green' : 'badge-gray'}`}>
              {hotel.active ? 'Active' : 'Inactive'}
            </span>
          )}
        </div>
        <p className="muted">📍 {hotel.city}</p>
        <p className="amenities">
          {hotel.amenities?.slice(0, 4).map((a) => `• ${a}`).join(' ')}
        </p>
        <div className="price-row">
          {price != null ? (
            <span className="price">{formatCurrency(price)} <small>/night</small></span>
          ) : hotel.startingPrice != null ? (
            <span className="price">{formatCurrency(hotel.startingPrice)} <small>starting</small></span>
          ) : null}
        </div>
      </div>
    </Link>
  );
}
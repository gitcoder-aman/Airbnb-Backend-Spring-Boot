import { useEffect, useState } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { adminApi } from '../../api/api';
import { apiErrorText } from '../../utils';
import {
  usePersistentState,
  clearPersistentState,
  hasPersistedState,
  pageKeys,
} from '../../hooks/usePersistentState';

export default function HotelForm() {
  const { hotelId } = useParams();
  const navigate = useNavigate();
  const isEdit = Boolean(hotelId);
  const storageKey = isEdit ? pageKeys.admin.hotelFormEdit(hotelId) : pageKeys.admin.hotelFormNew;

  const [form, setForm] = usePersistentState(storageKey, {
    name: '',
    city: '',
    startingPrice: '',
    active: true,
    photos: [''],
    amenities: [''],
    contact: { address: '', phoneNumber: '', email: '', location: '' },
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(isEdit);

  useEffect(() => {
    if (!isEdit) return;
    (async () => {
      try {
        const h = await adminApi.getHotelById(hotelId);
        // If the user has an unsaved draft for this hotel, keep it — only seed
        // the form from the server when there is nothing in progress.
        if (hasPersistedState(storageKey)) return;
        setForm({
          name: h.name,
          city: h.city,
          startingPrice: h.startingPrice ?? '',
          active: h.active ?? true,
          photos: h.photos?.length ? [...h.photos] : [''],
          amenities: h.amenities?.length ? [...h.amenities] : [''],
          contact: {
            address: h.contactInfo?.address || '',
            phoneNumber: h.contactInfo?.phoneNumber || '',
            email: h.contactInfo?.email || '',
            location: h.contactInfo?.location || '',
          },
        });
      } catch (e) {
        setError(apiErrorText(e));
      } finally {
        setLoading(false);
      }
    })();
  }, [hotelId, isEdit, storageKey]);

  const setList = (key, i, value) => {
    const list = [...form[key]];
    list[i] = value;
    setForm({ ...form, [key]: list });
  };
  const addList = (key) => setForm({ ...form, [key]: [...form[key], ''] });
  const removeList = (key, i) => setForm({ ...form, [key]: form[key].filter((_, idx) => idx !== i) });

  const submit = async (e) => {
    e.preventDefault();
    const payload = {
      name: form.name,
      city: form.city,
      photos: form.photos.filter((p) => p.trim()),
      amenities: form.amenities.filter((a) => a.trim()),
      startingPrice: form.startingPrice ? Number(form.startingPrice) : null,
      active: form.active,
      contactInfo: { ...form.contact },
    };
    setError('');
    setLoading(true);
    try {
      if (isEdit) {
        await adminApi.updateHotel(hotelId, payload);
      } else {
        const created = await adminApi.createHotel(payload);
        clearPersistentState(storageKey);
        navigate(`/admin/hotels/${created.id}`);
        return;
      }
      clearPersistentState(storageKey);
      navigate(`/admin/hotels/${hotelId}`);
    } catch (err) {
      setError(apiErrorText(err));
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <Link to="/admin" className="btn btn-sm btn-ghost">← Back to dashboard</Link>
      <h2 className="section-title">{isEdit ? 'Edit hotel' : 'Create new hotel'}</h2>
      {error && <div className="alert alert-error">{error}</div>}

      <form className="form-card" style={{ maxWidth: 'none' }} onSubmit={submit}>
        <div className="form-grid-2">
          <div className="form-group">
            <label>Hotel name</label>
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </div>
          <div className="form-group">
            <label>City</label>
            <input required value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} />
          </div>
        </div>
        <div className="form-grid-2">
          <div className="form-group">
            <label>Starting price (₹)</label>
            <input type="number" min="0" value={form.startingPrice} onChange={(e) => setForm({ ...form, startingPrice: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Active</label>
            <select value={form.active} onChange={(e) => setForm({ ...form, active: e.target.value === 'true' })}>
              <option value="true">Yes — listed publicly</option>
              <option value="false">No — draft/inactive</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label>Photos URLs</label>
          <div className="photos-list">
            {form.photos.map((p, i) => (
              <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                <input value={p} onChange={(e) => setList('photos', i, e.target.value)} placeholder="https://…" />
                <button type="button" className="btn btn-sm btn-danger" onClick={() => removeList('photos', i)}>✕</button>
              </div>
            ))}
          </div>
          <button type="button" className="btn btn-sm btn-outline" onClick={() => addList('photos')}>+ Add photo</button>
        </div>

        <div className="form-group">
          <label>Amenities</label>
          <div className="photos-list">
            {form.amenities.map((a, i) => (
              <div key={i} style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                <input value={a} onChange={(e) => setList('amenities', i, e.target.value)} placeholder="e.g. Free WiFi" />
                <button type="button" className="btn btn-sm btn-danger" onClick={() => removeList('amenities', i)}>✕</button>
              </div>
            ))}
          </div>
          <button type="button" className="btn btn-sm btn-outline" onClick={() => addList('amenities')}>+ Add amenity</button>
        </div>

        <h3 style={{ margin: '18px 0 12px' }}>Contact information</h3>
        <div className="form-grid-2">
          <div className="form-group">
            <label>Address</label>
            <input required value={form.contact.address} onChange={(e) => setForm({ ...form, contact: { ...form.contact, address: e.target.value } })} />
          </div>
          <div className="form-group">
            <label>Location / area</label>
            <input required value={form.contact.location} onChange={(e) => setForm({ ...form, contact: { ...form.contact, location: e.target.value } })} />
          </div>
          <div className="form-group">
            <label>Phone number</label>
            <input required value={form.contact.phoneNumber} onChange={(e) => setForm({ ...form, contact: { ...form.contact, phoneNumber: e.target.value } })} placeholder="10-12 digits" />
          </div>
          <div className="form-group">
            <label>Email</label>
            <input required type="email" value={form.contact.email} onChange={(e) => setForm({ ...form, contact: { ...form.contact, email: e.target.value } })} />
          </div>
        </div>

        <button className="btn btn-primary" disabled={loading} style={{ marginTop: 8 }}>
          {isEdit ? 'Save changes' : 'Create hotel'}
        </button>
      </form>
    </div>
  );
}
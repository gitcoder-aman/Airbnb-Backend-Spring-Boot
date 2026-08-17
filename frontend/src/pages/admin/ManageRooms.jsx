import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi, adminRoomApi } from '../../api/api';
import { apiErrorText, formatCurrency } from '../../utils';
import { usePersistentState, pageKeys } from '../../hooks/usePersistentState';

const ROOM_TYPES = ['STANDARD', 'DELUXE', 'SUPER_DELUXE', 'SUITE', 'FAMILY', 'SINGLE'];

const emptyRoom = {
  type: 'DELUXE',
  basePrice: '',
  photos: [''],
  amenities: [''],
  totalCount: 1,
  capacity: 1,
  description: '',
};

export default function ManageRooms() {
  const { hotelId } = useParams();
  const [hotel, setHotel] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [draft, setDraft] = usePersistentState(pageKeys.admin.rooms(hotelId), {
    form: emptyRoom,
    editingId: null,
  });
  const form = draft?.form || emptyRoom;
  const editingId = draft?.editingId || null;
  const setForm = (updater) =>
    setDraft((prev) => ({
      form: typeof updater === 'function' ? updater(prev?.form || emptyRoom) : updater,
      editingId: prev?.editingId || null,
    }));
  const setEditingId = (id) => setDraft((prev) => ({ editingId: id, form: prev?.form || emptyRoom }));
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const fetchAll = async () => {
    setLoading(true);
    setError('');
    try {
      const [h, r] = await Promise.all([
        adminApi.getHotelById(hotelId),
        adminRoomApi.getRoomsInHotel(hotelId),
      ]);
      setHotel(h);
      setRooms(r || []);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAll();
  }, [hotelId]);

  const setList = (key, i, value) => {
    const list = [...form[key]];
    list[i] = value;
    setForm({ ...form, [key]: list });
  };
  const addList = (key) => setForm({ ...form, [key]: [...form[key], ''] });
  const removeList = (key, i) => setForm({ ...form, [key]: form[key].filter((_, idx) => idx !== i) });

  const startEdit = (room) => {
    setEditingId(room.id);
    setForm({
      type: room.type,
      basePrice: room.basePrice ?? '',
      photos: room.photos?.length ? [...room.photos] : [''],
      amenities: room.amenities?.length ? [...room.amenities] : [''],
      totalCount: room.totalCount,
      capacity: room.capacity,
      description: room.description,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const resetForm = () => {
    setEditingId(null);
    setForm(emptyRoom);
  };

  const submit = async (e) => {
    e.preventDefault();
    const payload = {
      type: form.type,
      basePrice: Number(form.basePrice),
      photos: form.photos.filter((p) => p.trim()),
      amenities: form.amenities.filter((a) => a.trim()),
      totalCount: Number(form.totalCount),
      capacity: Number(form.capacity),
      description: form.description,
    };
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await adminRoomApi.updateRoom(hotelId, editingId, payload);
      } else {
        await adminRoomApi.createRoom(hotelId, payload);
      }
      resetForm();
      await Promise.all([fetchAll()]);
    } catch (err) {
      setError(apiErrorText(err));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (room) => {
    if (!window.confirm(`Delete room "${room.type}"?`)) return;
    try {
      await adminRoomApi.deleteRoom(hotelId, room.id);
      await fetchAll();
    } catch (e) {
      setError(apiErrorText(e));
    }
  };

  return (
    <>
      <Link to={`/admin/hotels/${hotelId}`} className="btn btn-sm btn-ghost">← {hotel?.name || 'Hotel'}</Link>
      <h2 className="section-title">Rooms — {hotel?.name}</h2>
      {error && <div className="alert alert-error">{error}</div>}

      <form className="form-card" style={{ maxWidth: 'none', marginBottom: 24 }} onSubmit={submit}>
        <h3>{editingId ? 'Edit room' : 'Add a new room'}</h3>
        <div className="form-grid-2">
          <div className="form-group">
            <label>Room type</label>
            <select required value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              {ROOM_TYPES.map((t) => (
                <option key={t} value={t}>
                  {t.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Base price (₹/night)</label>
            <input required type="number" min="1" value={form.basePrice} onChange={(e) => setForm({ ...form, basePrice: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Total rooms of this type</label>
            <input required type="number" min="1" value={form.totalCount} onChange={(e) => setForm({ ...form, totalCount: e.target.value })} />
          </div>
          <div className="form-group">
            <label>Capacity (guests)</label>
            <input required type="number" min="1" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} />
          </div>
        </div>
        <div className="form-group">
          <label>Description</label>
          <textarea required value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
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
                <input value={a} onChange={(e) => setList('amenities', i, e.target.value)} placeholder="e.g. AC" />
                <button type="button" className="btn btn-sm btn-danger" onClick={() => removeList('amenities', i)}>✕</button>
              </div>
            ))}
          </div>
          <button type="button" className="btn btn-sm btn-outline" onClick={() => addList('amenities')}>+ Add amenity</button>
        </div>
        <div className="row-actions">
          <button className="btn btn-primary" disabled={saving}>
            {saving ? 'Saving…' : editingId ? 'Update room' : 'Add room'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-outline" onClick={resetForm}>Cancel edit</button>
          )}
        </div>
      </form>

      {loading ? (
        <div className="loading-screen"><div className="spinner" /></div>
      ) : rooms.length === 0 ? (
        <div className="empty"><h3>No rooms yet</h3><p>Add your first room above.</p></div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Base price</th>
                <th>Rooms</th>
                <th>Capacity</th>
                <th>Amenities</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rooms.map((r) => (
                <tr key={r.id}>
                  <td><strong>{r.type}</strong><br /><span className="muted" style={{ fontSize: '0.8rem' }}>{r.description}</span></td>
                  <td>{formatCurrency(r.basePrice)}</td>
                  <td>{r.totalCount}</td>
                  <td>{r.capacity}</td>
                  <td>{r.amenities?.join(', ')}</td>
                  <td>
                    <div className="row-actions" style={{ marginTop: 0 }}>
                      <button className="btn btn-sm btn-outline" onClick={() => startEdit(r)}>Edit</button>
                      <button className="btn btn-sm btn-danger" onClick={() => remove(r)}>Delete</button>
                    </div>
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
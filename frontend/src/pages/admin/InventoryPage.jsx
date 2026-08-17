import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi, adminRoomApi, adminInventoryApi } from '../../api/api';
import { apiErrorText, formatCurrency } from '../../utils';
import { usePersistentState, pageKeys } from '../../hooks/usePersistentState';

export default function InventoryPage() {
  const { hotelId } = useParams();
  const [hotel, setHotel] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [roomId, setRoomId] = usePersistentState(pageKeys.admin.inventoryRoom(hotelId), '');
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [invLoading, setInvLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = usePersistentState(pageKeys.admin.inventoryForm(hotelId), {
    checkInDate: '',
    checkOutDate: '',
    surgeFactor: '1.0',
    closed: 'false',
  });

  useEffect(() => {
    (async () => {
      try {
        const [h, r] = await Promise.all([
          adminApi.getHotelById(hotelId),
          adminRoomApi.getRoomsInHotel(hotelId),
        ]);
        setHotel(h);
        setRooms(r || []);
        if (r?.length) {
          // Keep the previously selected room if it still exists, else fall back to the first.
          setRoomId((prev) =>
            prev && r.some((x) => String(x.id) === String(prev))
              ? prev
              : String(r[0].id)
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
    if (!roomId) return;
    (async () => {
      setInvLoading(true);
      setError('');
      try {
        const list = await adminInventoryApi.getInventoryByRoom(roomId);
        setInventory(list || []);
      } catch (e) {
        setError(apiErrorText(e));
      } finally {
        setInvLoading(false);
      }
    })();
  }, [roomId]);

  const updateInventory = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await adminInventoryApi.updateInventory(roomId, {
        checkInDate: form.checkInDate,
        checkOutDate: form.checkOutDate,
        surgeFactor: form.surgeFactor ? Number(form.surgeFactor) : null,
        closed: form.closed === 'true',
      });
      const list = await adminInventoryApi.getInventoryByRoom(roomId);
      setInventory(list || []);
    } catch (err) {
      setError(apiErrorText(err));
    }
  };

  const today = new Date().toISOString().split('T')[0];

  if (loading) {
    return (
      <div className="loading-screen"><div className="spinner" /></div>
    );
  }

  return (
    <>
      <Link to={`/admin/hotels/${hotelId}`} className="btn btn-sm btn-ghost">← {hotel?.name || 'Hotel'}</Link>
      <h2 className="section-title">Inventory & pricing — {hotel?.name}</h2>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-card" style={{ maxWidth: 'none', marginBottom: 20 }}>
        <div className="search-row">
          <div className="field" style={{ flex: 2 }}>
            <label>Room</label>
            <select value={roomId} onChange={(e) => setRoomId(e.target.value)}>
              {rooms.map((r) => (
                <option key={r.id} value={r.id}>{r.type}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      <form className="form-card" style={{ maxWidth: 'none', marginBottom: 20 }} onSubmit={updateInventory}>
        <h3>Update inventory for a date range</h3>
        <div className="search-row">
          <div className="field">
            <label>From</label>
            <input type="date" required min={today} value={form.checkInDate} onChange={(e) => setForm({ ...form, checkInDate: e.target.value })} />
          </div>
          <div className="field">
            <label>To</label>
            <input type="date" required min={form.checkInDate || today} value={form.checkOutDate} onChange={(e) => setForm({ ...form, checkOutDate: e.target.value })} />
          </div>
          <div className="field" style={{ maxWidth: 130 }}>
            <label>Surge factor</label>
            <input type="number" step="0.1" value={form.surgeFactor} onChange={(e) => setForm({ ...form, surgeFactor: e.target.value })} />
          </div>
          <div className="field" style={{ maxWidth: 140 }}>
            <label>Close dates</label>
            <select value={form.closed} onChange={(e) => setForm({ ...form, closed: e.target.value })}>
              <option value="false">Open</option>
              <option value="true">Closed</option>
            </select>
          </div>
          <button className="btn btn-primary">Apply</button>
        </div>
      </form>

      {invLoading ? (
        <div className="loading-screen"><div className="spinner" /></div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Price</th>
                <th>Total</th>
                <th>Booked</th>
                <th>Reserved</th>
                <th>Surge</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {inventory.map((inv) => (
                <tr key={inv.id}>
                  <td>{inv.date}</td>
                  <td>{formatCurrency(inv.price)}</td>
                  <td>{inv.totalCount}</td>
                  <td>{inv.bookedCount}</td>
                  <td>{inv.reservedCount}</td>
                  <td>{inv.surgeFactor ?? 1}</td>
                  <td>
                    {inv.closed ? <span className="badge badge-red">Closed</span> : <span className="badge badge-green">Open</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!invLoading && inventory.length === 0 && (
            <p className="empty">No inventory. Activate the hotel to auto-generate inventory for a year.</p>
          )}
        </div>
      )}
    </>
  );
}
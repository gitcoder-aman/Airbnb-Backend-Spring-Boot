import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingApi, hotelApi } from '../api/api';
import HotelCard from '../components/HotelCard';
import { apiErrorText } from '../utils';
import { useDraft, clearDraft } from '../hooks/useDraft';
import { usePersistentState, pageKeys } from '../hooks/usePersistentState';

const today = () => new Date().toISOString().split('T')[0];

export default function Home() {
  const draft = useDraft();
  const [allData, setAllData] = useState(null);
  const [searchData, setSearchData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = usePersistentState(pageKeys.home.page, 0);
  const [searched, setSearched] = usePersistentState(pageKeys.home.searched, false);

  const [criteria, setCriteria] = usePersistentState(pageKeys.home.criteria, {
    city: '',
    checkInDate: today(),
    checkOutDate: '',
    numberOfRooms: 1,
    maxPrice: '',
  });

  useEffect(() => {
    // Restore the last search/showing-all view after a refresh or navigation.
    if (searched) {
      handleSearch(null, page);
    } else {
      fetchAll(page);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchAll = async (p) => {
    setLoading(true);
    setError('');
    try {
      const res = await hotelApi.getAllHotels(p, 10);
      setAllData(res);
      setPage(p);
      setSearched(false);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e, pageNum = 0) => {
    if (e?.preventDefault) e.preventDefault();
    setSearching(true);
    setError('');
    try {
      const payload = {
        city: criteria.city,
        checkInDate: criteria.checkInDate,
        checkOutDate: criteria.checkOutDate,
        numberOfRooms: Number(criteria.numberOfRooms),
        page: pageNum,
        size: 10,
      };
      if (criteria.maxPrice) payload.maxPrice = Number(criteria.maxPrice);
      const res = await hotelApi.searchHotels(payload);
      setSearchData(res);
      setSearched(true);
      setPage(pageNum);
    } catch (e) {
      setError(apiErrorText(e));
    } finally {
      setSearching(false);
    }
  };

  const clearSearch = () => {
    setSearchData(null);
    setSearched(false);
    fetchAll(0);
  };

  const hotels = searchData?.content || allData?.content || [];
  const listSource = searchData ? searchData : allData;

  const discardDraftBooking = async () => {
    const bookingId = draft?.booking?.id;
    if (bookingId) {
      try {
        await bookingApi.expireBooking(bookingId);
      } catch {
        // booking already gone/expired — still clear locally
      }
    }
    clearDraft();
  };

  return (
    <>
      <section className="hero">
        <h1>Find your home away from home</h1>
        <p>Search thousands of stays across India</p>

        <form className="search-card" onSubmit={handleSearch}>
          <div className="search-row">
            <div className="field">
              <label>Where</label>
              <input
                required
                placeholder="City"
                value={criteria.city}
                onChange={(e) => setCriteria({ ...criteria, city: e.target.value })}
              />
            </div>
            <div className="field">
              <label>Check-in</label>
              <input
                type="date"
                required
                value={criteria.checkInDate}
                onChange={(e) => setCriteria({ ...criteria, checkInDate: e.target.value })}
              />
            </div>
            <div className="field">
              <label>Check-out</label>
              <input
                type="date"
                required
                value={criteria.checkOutDate}
                onChange={(e) => setCriteria({ ...criteria, checkOutDate: e.target.value })}
              />
            </div>
            <div className="field" style={{ maxWidth: 120 }}>
              <label>Rooms</label>
              <input
                type="number"
                min="1"
                value={criteria.numberOfRooms}
                onChange={(e) => setCriteria({ ...criteria, numberOfRooms: e.target.value })}
              />
            </div>
            <div className="field" style={{ maxWidth: 140 }}>
              <label>Max price</label>
              <input
                type="number"
                min="0"
                placeholder="₹ Any"
                value={criteria.maxPrice}
                onChange={(e) => setCriteria({ ...criteria, maxPrice: e.target.value })}
              />
            </div>
            <button className="btn btn-primary" type="submit" disabled={searching}>
              {searching ? 'Searching…' : 'Search'}
            </button>
          </div>
        </form>
      </section>

      <div className="page page-lg">
        {draft && (
          <div className="alert alert-success continue-banner">
            <span>
              ⏳ You have an unfinished booking #{draft.booking?.id} — you were at step {draft.step} of 3 (details → guests → payment).
            </span>
            <span className="row-actions" style={{ marginTop: 0 }}>
              <Link to="/checkout" className="btn btn-primary btn-sm">
                Continue booking
              </Link>
              <button className="btn btn-ghost btn-sm" onClick={discardDraftBooking}>
                Discard booking
              </button>
            </span>
          </div>
        )}

        {error && <div className="alert alert-error">{error}</div>}

        {searchData && (
          <div className="row-actions">
            <span className="muted">
              {searchData.totalElements} result(s) for “{criteria.city}”
            </span>
            <button className="btn btn-sm btn-outline" onClick={clearSearch}>
              Clear search
            </button>
          </div>
        )}

        <h2 className="section-title">
          {searchData ? 'Search results' : 'Available stays'}
        </h2>

        {loading || searching ? (
          <div className="loading-inline">
            <div className="spinner" />
          </div>
        ) : hotels.length === 0 ? (
          <div className="empty">
            <h3>No stays found</h3>
            <p>Try a different city or date range.</p>
          </div>
        ) : (
          <>
            <div className="grid">
              {hotels.map((item) => (
                <HotelCard
                  key={item.hotel?.id ?? item.id}
                  hotel={searchData ? item.hotel : item}
                  price={searchData ? item.price : null}
                />
              ))}
            </div>

            {listSource && listSource.totalPages > 1 && (
              <div className="pagination">
                <button
                  className="btn btn-sm btn-outline"
                  disabled={listSource.first}
                  onClick={() => (searchData ? handleSearch(null, page - 1) : fetchAll(page - 1))}
                >
                  Prev
                </button>
                <span>
                  Page {page + 1} of {listSource.totalPages}
                </span>
                <button
                  className="btn btn-sm btn-outline"
                  disabled={listSource.last}
                  onClick={() => (searchData ? handleSearch(null, page + 1) : fetchAll(page + 1))}
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </>
  );
}
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useDraft } from '../hooks/useDraft';

export default function Navbar() {
  const { token, user, isManager, logout } = useAuth();
  const draft = useDraft();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/" className="brand">
          <span className="brand-logo">🏨</span> AtithiStay
        </Link>

        <nav className="nav-links">
          <NavLink to="/" end>
            Stays
          </NavLink>
          <NavLink to="/ai-assistant">AI Assistant</NavLink>
          {isManager && <NavLink to="/admin">Dashboard</NavLink>}
          {token && <NavLink to="/my-bookings">My Bookings</NavLink>}
          {token && draft && (
            <NavLink to="/checkout" className="continue-link">
              ⏳ Continue booking #{draft.booking?.id}
            </NavLink>
          )}
        </nav>

        <div className="nav-actions">
          {token ? (
            <>
              <Link to="/profile" className="user-chip">
                <span className="avatar">{(user?.name || 'U')[0].toUpperCase()}</span>
                <span className="user-name">{user?.name || 'Profile'}</span>
              </Link>
              <button className="btn btn-ghost" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-ghost">
                Log in
              </Link>
              <Link to="/signup" className="btn btn-primary btn-sm">
                Sign up
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
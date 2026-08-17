import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import LoadingScreen from './LoadingScreen';

export default function ProtectedRoute({ children, requireManager = false }) {
  const { token, loading, isManager } = useAuth();

  if (loading) return <LoadingScreen />;
  if (!token) return <Navigate to="/login" replace />;
  if (requireManager && !isManager) return <Navigate to="/" replace />;
  return children;
}
import { createContext, useContext, useEffect, useState, useCallback } from 'react';
import { authApi, userApi } from '../api/api';
import { rolesFromToken } from '../utils';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('accessToken') || null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const roles = rolesFromToken(token);

  const loadProfile = useCallback(async () => {
    try {
      const profile = await userApi.getProfile();
      setUser(profile);
    } catch {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    async function init() {
      if (!token) {
        setLoading(false);
        return;
      }
      try {
        await loadProfile();
      } finally {
        setLoading(false);
      }
    }
    init();
  }, [token, loadProfile]);

  const signup = async (payload) => {
    await authApi.signup(payload);
  };

  const login = async (email, password) => {
    const res = await authApi.login({ email, password });
    localStorage.setItem('accessToken', res.accessToken);
    setToken(res.accessToken);
    await loadProfile();
    return res;
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    setToken(null);
    setUser(null);
  };

  const isManager = roles.includes('HOTEL_MANAGER');
  const isGuest = roles.includes('GUEST');

  const value = {
    token,
    user,
    loading,
    roles,
    isManager,
    isGuest,
    login,
    signup,
    logout,
    refreshUser: loadProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
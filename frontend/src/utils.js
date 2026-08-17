export function decodeJwt(token) {
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export function rolesFromToken(token) {
  const payload = decodeJwt(token);
  if (!payload?.roles) return [];
  const roles = payload.roles
    .replace(/[\[\]']/g, '')
    .split(',')
    .map((r) => r.trim())
    .filter(Boolean);
  return roles;
}

export function formatCurrency(amount) {
  const n = Number(amount);
  if (Number.isNaN(n)) return '';
  return '₹' + n.toLocaleString('en-IN', { maximumFractionDigits: 0 });
}

export function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

export function apiErrorText(err) {
  if (!err) return 'Something went wrong';
  if (err.subErrors && Object.keys(err.subErrors).length) {
    return Object.values(err.subErrors).join(', ');
  }
  return err.message || 'Something went wrong';
}
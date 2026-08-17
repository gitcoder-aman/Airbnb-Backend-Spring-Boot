import { useEffect, useState } from 'react';

const KEY = 'atithistay.booking.draft';

export function loadDraft() {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveDraft(data) {
  localStorage.setItem(KEY, JSON.stringify(data));
  window.dispatchEvent(new Event('atithistay:draft'));
}

export function clearDraft() {
  localStorage.removeItem(KEY);
  window.dispatchEvent(new Event('atithistay:draft'));
}

export function useDraft() {
  const [draft, setDraft] = useState(loadDraft);

  useEffect(() => {
    const handler = () => setDraft(loadDraft());
    window.addEventListener('atithistay:draft', handler);
    return () => window.removeEventListener('atithistay:draft', handler);
  }, []);

  return draft;
}
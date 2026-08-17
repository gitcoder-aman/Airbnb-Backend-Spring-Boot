import { useEffect, useRef, useState } from 'react';

/**
 * useState that mirrors its value to localStorage.
 * - Restores the persisted value on first render (key present in storage).
 * - Writes back only after the value actually changes (so merely mounting
 *   a page never creates a stale draft).
 */
export function usePersistentState(key, defaultValue) {
  const [value, setValue] = useState(() => {
    try {
      const raw = localStorage.getItem(key);
      if (raw !== null) return JSON.parse(raw);
    } catch {
      // ignore corrupt/unavailable storage
    }
    return defaultValue;
  });

  const skipWrite = useRef(true);

  useEffect(() => {
    if (skipWrite.current) {
      skipWrite.current = false;
      return;
    }
    try {
      if (value === undefined || value === null) {
        localStorage.removeItem(key);
      } else {
        localStorage.setItem(key, JSON.stringify(value));
      }
    } catch {
      // storage unavailability (private mode / quota) should not crash the app
    }
  }, [key, value]);

  return [value, setValue];
}

export function hasPersistedState(key) {
  try {
    return localStorage.getItem(key) !== null;
  } catch {
    return false;
  }
}

export function clearPersistentState(key) {
  try {
    localStorage.removeItem(key);
  } catch {
    // ignore
  }
}

/* All localStorage keys used by the app, grouped by page. */
export const pageKeys = {
  home: {
    criteria: 'atithistay.page.home.criteria',
    page: 'atithistay.page.home.page',
    searched: 'atithistay.page.home.searched',
  },
  hotelDetail: {
    roomId: (hotelId) => `atithistay.page.hotel.${hotelId}.room`,
    photos: (hotelId) => `atithistay.page.hotel.${hotelId}.photos`,
  },
  aiAssistant: {
    messages: 'atithistay.page.ai.messages',
  },
  profile: {
    form: 'atithistay.page.profile.form',
  },
  admin: {
    hotelFormNew: 'atithistay.page.admin.hotelForm.new',
    hotelFormEdit: (hotelId) => `atithistay.page.admin.hotelForm.${hotelId}`,
    rooms: (hotelId) => `atithistay.page.admin.rooms.${hotelId}`,
    inventoryRoom: (hotelId) => `atithistay.page.admin.inventory.room.${hotelId}`,
    inventoryForm: (hotelId) => `atithistay.page.admin.inventory.form.${hotelId}`,
  },
};
import { useEffect, useRef, useState } from 'react';

export function useCountdown(deadlineMs) {
  const [remaining, setRemaining] = useState(() =>
    deadlineMs ? Math.max(0, deadlineMs - Date.now()) : 0
  );

  useEffect(() => {
    if (!deadlineMs) return undefined;
    const tick = () => setRemaining(Math.max(0, deadlineMs - Date.now()));
    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [deadlineMs]);

  return remaining;
}

export function formatCountdown(ms) {
  if (ms <= 0) return '00:00';
  const totalSec = Math.floor(ms / 1000);
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export function expiresAtFromBooking(booking) {
  if (!booking?.createdAt) return null;
  return new Date(booking.createdAt).getTime() + 10 * 60 * 1000;
}

export function usePolling(fn, intervalMs, active = true) {
  const fnRef = useRef(fn);
  fnRef.current = fn;
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (!active) return undefined;
    let alive = true;
    const run = async () => {
      try {
        const r = await fnRef.current();
        if (alive) setResult(r);
      } catch {
        // polling errors are non-fatal
      }
    };
    run();
    const id = setInterval(run, intervalMs);
    return () => {
      alive = false;
      clearInterval(id);
    };
  }, [intervalMs, active]);

  return result;
}
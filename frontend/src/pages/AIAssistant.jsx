import { useState } from 'react';
import { aiApi } from '../api/api';
import { apiErrorText } from '../utils';
import { usePersistentState, pageKeys } from '../hooks/usePersistentState';

const SUGGESTIONS = [
  { label: 'Search hotels', text: 'Hotels in Bangalore under 5000' },
  { label: 'Hotel details', text: 'Tell me about Taj Bangalore' },
];

const DEFAULT_MESSAGES = [
  {
    role: 'bot',
    text: 'Hi! I can search hotels, tell you about a hotel, or just chat. Try one of the suggestions below.',
  },
];

export default function AIAssistant() {
  const [messages, setMessages] = usePersistentState(pageKeys.aiAssistant.messages, DEFAULT_MESSAGES);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);

  const send = async (text, mode = 'chat') => {
    if (!text.trim() || busy) return;
    setMessages((m) => [...m, { role: 'user', text }]);
    setInput('');
    setBusy(true);
    try {
      let res;
      if (mode === 'search') res = await aiApi.search(text);
      else if (mode === 'detail') res = await aiApi.hotelDetail(text);
      else res = await aiApi.chat(text);
      setMessages((m) => [...m, { role: 'bot', text: typeof res === 'string' ? res : JSON.stringify(res) }]);
    } catch (e) {
      setMessages((m) => [...m, { role: 'bot', text: '⚠️ ' + apiErrorText(e) }]);
    } finally {
      setBusy(false);
    }
  };

  const smartSend = (text) => {
    const lower = text.toLowerCase();
    if (lower.startsWith('search') || /\bhotels?\b.*(in|under|between)/.test(lower)) {
      return send(text, 'search');
    }
    if (lower.startsWith('tell me about') || lower.includes('about')) {
      return send(text, 'detail');
    }
    return send(text, 'chat');
  };

  return (
    <div className="page page-lg">
      <h1 className="section-title">AI Assistant</h1>
      <p className="muted" style={{ marginBottom: 16 }}>
        Powered by Google Gemini. Ask things like{' '}
        <em>“Search hotels in Goa under 3000”</em> or <em>“Tell me about The Taj Mahal Palace”</em>.
      </p>

      <div className="segment">
        {SUGGESTIONS.map((s) => (
          <button key={s.label} onClick={() => smartSend(s.text)} disabled={busy}>
            {s.label}
          </button>
        ))}
      </div>

      <div className="chat-box">
        <div className="chat-messages">
          {messages.map((m, i) => (
            <div key={i} className={`chat-msg ${m.role}`}>
              {m.text}
            </div>
          ))}
          {busy && <div className="chat-msg bot">Thinking…</div>}
        </div>
        <form
          className="chat-input"
          onSubmit={(e) => {
            e.preventDefault();
            smartSend(input);
          }}
        >
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask anything about stays…"
          />
          <button className="btn btn-primary" disabled={busy || !input.trim()}>
            Send
          </button>
        </form>
      </div>
    </div>
  );
}
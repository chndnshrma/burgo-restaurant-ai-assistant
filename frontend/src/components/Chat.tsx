"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { AnimatePresence, motion } from "motion/react";
import { useCart } from "@/lib/cart";
import { sendChat } from "@/lib/api";
import type { ChatResponse } from "@/lib/types";
import { ChatIcon, CloseIcon, SendIcon } from "./icons";

interface Msg {
  id: number;
  from: "bot" | "me";
  text: string;
  action?: ChatResponse["action"];
}

const STARTERS = ["Show bestsellers", "Veg options", "Book a table", "Opening hours"];

export function Chat() {
  const { restaurant } = useCart();
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState("");
  const [busy, setBusy] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>(STARTERS);
  const [msgs, setMsgs] = useState<Msg[]>([
    {
      id: 0,
      from: "bot",
      text: `Hi! I'm the ${restaurant.name} assistant. Ask me about the menu, delivery, opening hours, or booking a table.`,
    },
  ]);
  const nextId = useRef(1);
  const log = useRef<HTMLDivElement>(null);
  const field = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const el = log.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [msgs, busy, open]);

  useEffect(() => {
    if (open) field.current?.focus();
  }, [open]);

  function push(m: Omit<Msg, "id">) {
    setMsgs((all) => [...all, { ...m, id: nextId.current++ }]);
  }

  async function ask(raw: string) {
    const text = raw.trim();
    if (!text || busy) return;
    push({ from: "me", text });
    setInput("");
    setBusy(true);
    try {
      const res = await sendChat(text);
      push({ from: "bot", text: res.reply, action: res.action });
      setSuggestions(res.suggestions.length ? res.suggestions : STARTERS);
      if (res.action === "OPEN_RESERVATION") {
        setOpen(false);
        document.getElementById("reserve")?.scrollIntoView();
      }
    } catch (e) {
      push({ from: "bot", text: e instanceof Error ? e.message : "Something went wrong. Please try again." });
    } finally {
      setBusy(false);
    }
  }

  function submit(e: FormEvent) {
    e.preventDefault();
    void ask(input);
  }

  return (
    <>
      {!open && (
        <button type="button" className="chat-fab" onClick={() => setOpen(true)} aria-label="Ask us a question">
          <ChatIcon width={26} height={26} />
        </button>
      )}
      <AnimatePresence>
        {open && (
          <motion.section
            className="chat"
            role="dialog"
            aria-label={`Chat with ${restaurant.name}`}
            initial={{ opacity: 0, y: 24, scale: 0.96 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 24, scale: 0.96 }}
            transition={{ type: "spring", stiffness: 420, damping: 34 }}
            onKeyDown={(e) => e.key === "Escape" && setOpen(false)}
          >
            <header className="chat-head">
              <div>
                <strong>Ask {restaurant.name.split(" ")[0]}</strong>
                <span>Answers come from our menu and details</span>
              </div>
              <button type="button" className="icon-btn icon-btn-dark" onClick={() => setOpen(false)} aria-label="Close chat">
                <CloseIcon />
              </button>
            </header>

            <div className="chat-log" ref={log} role="log" aria-live="polite">
              {msgs.map((m) => (
                <div key={m.id} className={m.from === "me" ? "msg msg-me" : "msg msg-bot"}>
                  <p>{m.text}</p>
                  {m.from === "bot" && m.action === "SCROLL_MENU" && (
                    <a className="chip chip-sm" href="#menu" onClick={() => setOpen(false)}>
                      See the menu
                    </a>
                  )}
                  {m.from === "bot" && m.action === "OPEN_RESERVATION" && (
                    <a className="chip chip-sm" href="#reserve" onClick={() => setOpen(false)}>
                      Go to booking form
                    </a>
                  )}
                </div>
              ))}
              {busy && (
                <div className="msg msg-bot typing" aria-label="Typing">
                  <span />
                  <span />
                  <span />
                </div>
              )}
            </div>

            <div className="chat-suggest">
              {suggestions.map((s) => (
                <button key={s} type="button" className="chip chip-sm" onClick={() => void ask(s)} disabled={busy}>
                  {s}
                </button>
              ))}
            </div>

            <form className="chat-form" onSubmit={submit}>
              <label htmlFor="chat-input" className="sr-only">
                Your question
              </label>
              <input
                id="chat-input"
                ref={field}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="Ask about the menu, hours, delivery…"
                maxLength={500}
                autoComplete="off"
              />
              <button type="submit" className="btn btn-red btn-sm send" disabled={busy || !input.trim()} aria-label="Send">
                <SendIcon />
              </button>
            </form>
          </motion.section>
        )}
      </AnimatePresence>
    </>
  );
}

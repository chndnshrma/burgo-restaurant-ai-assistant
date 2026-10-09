"use client";

import { useEffect, useState, type FormEvent } from "react";
import { useCart } from "@/lib/cart";
import { ApiError, getSlots, reserve } from "@/lib/api";
import { addDaysISO, lastSeating, prettyDate, to12h, todayISO } from "@/lib/format";
import type { ReservationResponse, Slot } from "@/lib/types";
import { CheckIcon, MinusIcon, PlusIcon } from "./icons";

const MAX_PARTY = 12;
const MAX_DAYS = 30;

export function Reserve() {
  const { restaurant } = useCart();

  // The date is set after hydration so the server and browser can't disagree about "today".
  const [today, setToday] = useState("");
  const [date, setDate] = useState("");
  const [party, setParty] = useState(2);
  const [time, setTime] = useState("");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [notes, setNotes] = useState("");

  const [slots, setSlots] = useState<Slot[] | null>(null);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [slotsError, setSlotsError] = useState("");

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [done, setDone] = useState<ReservationResponse | null>(null);

  useEffect(() => {
    const t = todayISO();
    setToday(t);
    setDate(t);
  }, []);

  useEffect(() => {
    if (!date) return;
    const ac = new AbortController();
    setLoadingSlots(true);
    setSlotsError("");
    getSlots(date, ac.signal)
      .then((r) => {
        setSlots(r.slots);
        setLoadingSlots(false);
      })
      .catch((e: unknown) => {
        if (e instanceof DOMException && e.name === "AbortError") return;
        setSlots(null);
        setSlotsError(e instanceof Error ? e.message : "We couldn't load the times. Please try again.");
        setLoadingSlots(false);
      });
    return () => ac.abort();
  }, [date]);

  const chosen = slots?.find((s) => s.time === time);
  const fits = !!chosen && chosen.seatsLeft >= party;

  function pickDate(value: string) {
    setDate(value);
    setTime("");
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (busy) return;
    setBusy(true);
    setError("");
    setFields({});
    try {
      const res = await reserve({
        name,
        phone,
        date,
        time,
        partySize: party,
        notes: notes.trim() || undefined,
      });
      setDone(res);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        setFields(err.fieldErrors);
        if (err.status === 409) {
          // Someone else took the seats: reload the times so the grid is honest again.
          setTime("");
          getSlots(date).then((r) => setSlots(r.slots)).catch(() => undefined);
        }
      } else {
        setError("Something went wrong. Please try again.");
      }
    } finally {
      setBusy(false);
    }
  }

  function bookAnother() {
    setDone(null);
    setTime("");
    setNotes("");
    getSlots(date).then((r) => setSlots(r.slots)).catch(() => undefined);
  }

  return (
    <section id="reserve" className="reserve" aria-labelledby="reserve-title">
      <div className="wrap reserve-grid">
        <div className="reserve-copy">
          <h2 id="reserve-title" className="display h2">
            Book a table
          </h2>
          <p className="lede">
            Tables for 1 to {MAX_PARTY} guests, every 30 minutes from {to12h(restaurant.opens)} to{" "}
            {to12h(lastSeating(restaurant.closes))}. You can book up to {MAX_DAYS} days ahead, and we need at least 30
            minutes&apos; notice.
          </p>
          <p className="lede">
            Coming with more than {MAX_PARTY}? Call us on{" "}
            <a href={`tel:${restaurant.phone.replace(/\s/g, "")}`}>{restaurant.phone}</a> and we&apos;ll push some
            tables together.
          </p>
        </div>

        <div className="panel">
          {done ? (
            <div className="done" role="status">
              <span className="done-icon">
                <CheckIcon width={30} height={30} />
              </span>
              <h3 className="display">You&apos;re booked, {done.name.split(" ")[0]}.</h3>
              <p>
                {prettyDate(done.date)} at {to12h(done.time)} for {done.partySize}{" "}
                {done.partySize === 1 ? "guest" : "guests"}.
              </p>
              <p className="ref">{done.reference}</p>
              <p className="muted">
                Quote this reference if you need to change your booking. Call{" "}
                <a href={`tel:${restaurant.phone.replace(/\s/g, "")}`}>{restaurant.phone}</a>.
              </p>
              <button type="button" className="btn btn-yellow btn-sm" onClick={bookAnother}>
                Book another table
              </button>
            </div>
          ) : (
            <form onSubmit={submit} noValidate>
              <div className="row-2">
                <div className="field">
                  <label htmlFor="res-date">Date</label>
                  <input
                    id="res-date"
                    type="date"
                    value={date}
                    min={today || undefined}
                    max={today ? addDaysISO(today, MAX_DAYS) : undefined}
                    onChange={(e) => e.target.value && pickDate(e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <span className="label" id="res-party-label">
                    Guests
                  </span>
                  <div className="stepper stepper-lg" role="group" aria-labelledby="res-party-label">
                    <button type="button" onClick={() => setParty((p) => Math.max(1, p - 1))} disabled={party <= 1} aria-label="One fewer guest">
                      <MinusIcon width={18} height={18} />
                    </button>
                    <output aria-live="polite">{party}</output>
                    <button type="button" onClick={() => setParty((p) => Math.min(MAX_PARTY, p + 1))} disabled={party >= MAX_PARTY} aria-label="One more guest">
                      <PlusIcon width={18} height={18} />
                    </button>
                  </div>
                </div>
              </div>

              <div className="field">
                <span className="label" id="res-time-label">
                  Time
                </span>
                {loadingSlots && <p className="muted">Checking tables…</p>}
                {slotsError && (
                  <p className="alert" role="alert">
                    {slotsError}
                  </p>
                )}
                {slots && !loadingSlots && (
                  <div className="slots" role="group" aria-labelledby="res-time-label">
                    {slots.map((s) => {
                      const full = s.seatsLeft < party;
                      return (
                        <button
                          key={s.time}
                          type="button"
                          className="slot"
                          aria-pressed={time === s.time}
                          disabled={full}
                          onClick={() => setTime(s.time)}
                          aria-label={`${to12h(s.time)}, ${full ? "not available for your group" : `${s.seatsLeft} seats left`}`}
                        >
                          {to12h(s.time)}
                          {full ? <small>Unavailable</small> : s.seatsLeft <= 8 ? <small>{s.seatsLeft} left</small> : null}
                        </button>
                      );
                    })}
                  </div>
                )}
                {fields.time && <p className="field-error">{fields.time}</p>}
              </div>

              <div className="row-2">
                <div className="field">
                  <label htmlFor="res-name">Name</label>
                  <input id="res-name" value={name} onChange={(e) => setName(e.target.value)} autoComplete="name" maxLength={80} aria-invalid={!!fields.name} aria-describedby={fields.name ? "res-name-err" : undefined} />
                  {fields.name && (
                    <p className="field-error" id="res-name-err">
                      {fields.name}
                    </p>
                  )}
                </div>
                <div className="field">
                  <label htmlFor="res-phone">Phone</label>
                  <input id="res-phone" type="tel" inputMode="tel" value={phone} onChange={(e) => setPhone(e.target.value)} autoComplete="tel" aria-invalid={!!fields.phone} aria-describedby={fields.phone ? "res-phone-err" : undefined} />
                  {fields.phone && (
                    <p className="field-error" id="res-phone-err">
                      {fields.phone}
                    </p>
                  )}
                </div>
              </div>

              <div className="field">
                <label htmlFor="res-notes">Anything we should know? (optional)</label>
                <textarea id="res-notes" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={300} />
              </div>

              {time && !fits && <p className="alert">There isn&apos;t room for {party} guests at {to12h(time)}. Pick another time.</p>}
              {error && (
                <p className="alert" role="alert">
                  {error}
                </p>
              )}

              <button type="submit" className="btn btn-red btn-block" disabled={busy || !time || !fits}>
                {busy ? "Booking…" : time ? `Book ${to12h(time)} for ${party}` : "Pick a time to book"}
              </button>
            </form>
          )}
        </div>
      </div>
    </section>
  );
}

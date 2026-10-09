"use client";

import { useEffect, useRef, useState, type FormEvent, type KeyboardEvent } from "react";
import { AnimatePresence, motion } from "motion/react";
import { useCart } from "@/lib/cart";
import { ApiError, placeOrder } from "@/lib/api";
import { estimate, inr } from "@/lib/format";
import type { OrderResponse, OrderType } from "@/lib/types";
import { CheckIcon, CloseIcon, MinusIcon, PlusIcon } from "./icons";

export function CartDrawer() {
  const { restaurant, lines, subtotal, count, add, dec, clear, open, setOpen } = useCart();
  const [type, setType] = useState<OrderType>("PICKUP");
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [notes, setNotes] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [done, setDone] = useState<OrderResponse | null>(null);

  const panel = useRef<HTMLElement>(null);
  const opener = useRef<Element | null>(null);

  const { tax, delivery, total } = estimate(subtotal, type, restaurant.deliveryFeeInr, restaurant.freeDeliveryAboveInr);
  const shortBy = type === "DELIVERY" ? restaurant.minDeliveryOrderInr - subtotal : 0;

  function close() {
    setOpen(false);
  }

  // Lock page scroll while open, and hand focus back to whatever opened the drawer.
  useEffect(() => {
    if (!open) return;
    opener.current = document.activeElement;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    panel.current?.focus();
    return () => {
      document.body.style.overflow = previous;
      if (opener.current instanceof HTMLElement) opener.current.focus();
      setDone(null);
      setError("");
      setFields({});
    };
  }, [open]);

  function onKeyDown(e: KeyboardEvent) {
    if (e.key === "Escape") {
      close();
      return;
    }
    if (e.key !== "Tab" || !panel.current) return;
    const focusable = panel.current.querySelectorAll<HTMLElement>(
      'a[href], button:not([disabled]), input:not([disabled]), textarea:not([disabled])',
    );
    if (focusable.length === 0) return;
    const first = focusable[0];
    const last = focusable[focusable.length - 1];
    if (e.shiftKey && (document.activeElement === first || document.activeElement === panel.current)) {
      e.preventDefault();
      last.focus();
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault();
      first.focus();
    }
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (busy || lines.length === 0) return;
    setBusy(true);
    setError("");
    setFields({});
    try {
      const res = await placeOrder({
        type,
        name,
        phone,
        address: type === "DELIVERY" ? address : undefined,
        notes: notes.trim() || undefined,
        items: lines.map((l) => ({ menuItemId: l.item.id, quantity: l.qty })),
      });
      setDone(res);
      clear();
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        setFields(err.fieldErrors);
      } else {
        setError("Something went wrong. Please try again.");
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            key="scrim"
            className="scrim"
            onClick={close}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.18 }}
          />
          <motion.aside
            key="drawer"
            ref={panel}
            className="drawer"
            role="dialog"
            aria-modal="true"
            aria-labelledby="cart-title"
            tabIndex={-1}
            onKeyDown={onKeyDown}
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", stiffness: 420, damping: 40 }}
          >
            <header className="drawer-head">
              <h2 id="cart-title" className="display">
                {done ? "Order placed" : "Your order"}
              </h2>
              <button type="button" className="icon-btn" onClick={close} aria-label="Close cart">
                <CloseIcon />
              </button>
            </header>

            {done ? (
              <div className="drawer-body done" role="status">
                <span className="done-icon">
                  <CheckIcon width={30} height={30} />
                </span>
                <h3 className="display">We&apos;ve got it, {done.items.length > 0 ? "thanks" : ""}.</h3>
                <p className="ref">{done.reference}</p>
                <p>
                  {done.type === "DELIVERY" ? "Arriving" : "Ready for pickup"} in about {done.etaMinutes} minutes. Pay{" "}
                  {inr(done.totalInr)} {done.type === "DELIVERY" ? "when it arrives" : "when you pick up"}.
                </p>
                <ul className="receipt">
                  {done.items.map((i) => (
                    <li key={i.name}>
                      <span>
                        {i.quantity} × {i.name}
                      </span>
                      <span>{inr(i.lineTotalInr)}</span>
                    </li>
                  ))}
                </ul>
                <p className="muted">
                  Quote the reference if you call us on{" "}
                  <a href={`tel:${restaurant.phone.replace(/\s/g, "")}`}>{restaurant.phone}</a>.
                </p>
                <button type="button" className="btn btn-yellow btn-sm" onClick={close}>
                  Back to the menu
                </button>
              </div>
            ) : lines.length === 0 ? (
              <div className="drawer-body empty-cart">
                <p>Your cart is empty. Add something from the menu.</p>
                <a className="btn btn-yellow btn-sm" href="#menu" onClick={close}>
                  See the menu
                </a>
              </div>
            ) : (
              <form className="drawer-form" onSubmit={submit} noValidate>
                <div className="drawer-body">
                  <ul className="lines">
                    {lines.map(({ item, qty }) => (
                      <li key={item.id} className="line">
                        <div className="line-main">
                          <strong>{item.name}</strong>
                          <span className="muted">{inr(item.priceInr)} each</span>
                        </div>
                        <div className="stepper" role="group" aria-label={`${item.name} quantity`}>
                          <button type="button" onClick={() => dec(item.id)} aria-label={`Remove one ${item.name}`}>
                            <MinusIcon width={16} height={16} />
                          </button>
                          <output aria-live="polite">{qty}</output>
                          <button type="button" onClick={() => add(item.id)} disabled={qty >= 20} aria-label={`Add one more ${item.name}`}>
                            <PlusIcon width={16} height={16} />
                          </button>
                        </div>
                        <span className="line-total">{inr(item.priceInr * qty)}</span>
                      </li>
                    ))}
                  </ul>

                  <fieldset className="seg">
                    <legend className="label">How do you want it?</legend>
                    <label className={type === "PICKUP" ? "seg-opt is-on" : "seg-opt"}>
                      <input type="radio" name="type" checked={type === "PICKUP"} onChange={() => setType("PICKUP")} />
                      Pickup
                    </label>
                    <label className={type === "DELIVERY" ? "seg-opt is-on" : "seg-opt"}>
                      <input type="radio" name="type" checked={type === "DELIVERY"} onChange={() => setType("DELIVERY")} />
                      Delivery
                    </label>
                  </fieldset>

                  <div className="field">
                    <label htmlFor="ord-name">Name</label>
                    <input id="ord-name" value={name} onChange={(e) => setName(e.target.value)} autoComplete="name" maxLength={80} aria-invalid={!!fields.name} />
                    {fields.name && <p className="field-error">{fields.name}</p>}
                  </div>
                  <div className="field">
                    <label htmlFor="ord-phone">Phone</label>
                    <input id="ord-phone" type="tel" inputMode="tel" value={phone} onChange={(e) => setPhone(e.target.value)} autoComplete="tel" aria-invalid={!!fields.phone} />
                    {fields.phone && <p className="field-error">{fields.phone}</p>}
                  </div>
                  {type === "DELIVERY" && (
                    <div className="field">
                      <label htmlFor="ord-address">Delivery address</label>
                      <textarea id="ord-address" rows={2} value={address} onChange={(e) => setAddress(e.target.value)} maxLength={300} autoComplete="street-address" aria-invalid={!!fields.address} />
                      {fields.address && <p className="field-error">{fields.address}</p>}
                    </div>
                  )}
                  <div className="field">
                    <label htmlFor="ord-notes">Notes for the kitchen (optional)</label>
                    <textarea id="ord-notes" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} maxLength={300} />
                  </div>
                </div>

                <footer className="drawer-foot">
                  <dl className="totals">
                    <div>
                      <dt>Subtotal</dt>
                      <dd>{inr(subtotal)}</dd>
                    </div>
                    <div>
                      <dt>GST (5%)</dt>
                      <dd>{inr(tax)}</dd>
                    </div>
                    {type === "DELIVERY" && (
                      <div>
                        <dt>Delivery</dt>
                        <dd>{delivery === 0 ? "Free" : inr(delivery)}</dd>
                      </div>
                    )}
                    <div className="grand">
                      <dt>Total</dt>
                      <dd>{inr(total)}</dd>
                    </div>
                  </dl>
                  {shortBy > 0 && (
                    <p className="alert">
                      Delivery orders start at {inr(restaurant.minDeliveryOrderInr)}. Add {inr(shortBy)} more, or choose
                      pickup.
                    </p>
                  )}
                  {error && (
                    <p className="alert" role="alert">
                      {error}
                    </p>
                  )}
                  <button type="submit" className="btn btn-red btn-block" disabled={busy || shortBy > 0}>
                    {busy ? "Placing order…" : `Place order, ${inr(total)}`}
                  </button>
                  <p className="muted small">
                    {count} {count === 1 ? "item" : "items"}. You pay {type === "DELIVERY" ? "on delivery" : "at pickup"}.
                    Final prices are confirmed by the restaurant.
                  </p>
                </footer>
              </form>
            )}
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  );
}

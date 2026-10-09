"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import type { MenuItem, RestaurantInfo } from "./types";

type Lines = Record<number, number>;

interface CartContextValue {
  restaurant: RestaurantInfo;
  items: MenuItem[];
  lines: { item: MenuItem; qty: number }[];
  count: number;
  subtotal: number;
  qty: (id: number) => number;
  add: (id: number) => void;
  dec: (id: number) => void;
  clear: () => void;
  open: boolean;
  setOpen: (open: boolean) => void;
}

const CartContext = createContext<CartContextValue | null>(null);
const STORAGE_KEY = "burgo-cart-v1";
const MAX_QTY = 20;

export function CartProvider({
  restaurant,
  items,
  children,
}: {
  restaurant: RestaurantInfo;
  items: MenuItem[];
  children: ReactNode;
}) {
  const [stored, setStored] = useState<Lines>({});
  const [loaded, setLoaded] = useState(false);
  const [open, setOpen] = useState(false);

  // Restore the cart after hydration, keeping only items that are still on the menu.
  useEffect(() => {
    try {
      const raw = window.localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw) as Lines;
        const valid: Lines = {};
        for (const item of items) {
          const q = Number(parsed[item.id]);
          if (q > 0) valid[item.id] = Math.min(q, MAX_QTY);
        }
        setStored(valid);
      }
    } catch {
      // storage unavailable or corrupt: start with an empty cart
    }
    setLoaded(true);
  }, [items]);

  useEffect(() => {
    if (!loaded) return;
    try {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
    } catch {
      // ignore: the cart still works for this visit
    }
  }, [stored, loaded]);

  const add = useCallback((id: number) => {
    setStored((s) => ({ ...s, [id]: Math.min(MAX_QTY, (s[id] ?? 0) + 1) }));
  }, []);

  const dec = useCallback((id: number) => {
    setStored((s) => {
      const next = { ...s };
      if ((next[id] ?? 0) <= 1) delete next[id];
      else next[id] -= 1;
      return next;
    });
  }, []);

  const clear = useCallback(() => setStored({}), []);

  const value = useMemo<CartContextValue>(() => {
    const lines = items.filter((i) => stored[i.id] > 0).map((item) => ({ item, qty: stored[item.id] }));
    return {
      restaurant,
      items,
      lines,
      count: lines.reduce((n, l) => n + l.qty, 0),
      subtotal: lines.reduce((n, l) => n + l.qty * l.item.priceInr, 0),
      qty: (id) => stored[id] ?? 0,
      add,
      dec,
      clear,
      open,
      setOpen,
    };
  }, [items, restaurant, stored, add, dec, clear, open]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used inside <CartProvider>");
  return ctx;
}

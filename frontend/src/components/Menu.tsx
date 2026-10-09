"use client";

import { useMemo, useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import { useCart } from "@/lib/cart";
import { inr } from "@/lib/format";
import type { Category, MenuItem } from "@/lib/types";
import { FoodArt, Spice, VegMark } from "./art/FoodArt";
import { MinusIcon, PlusIcon } from "./icons";

const CATEGORY_LABEL: Record<Category, string> = {
  BURGERS: "Burgers",
  WRAPS: "Wraps",
  SIDES: "Sides",
  DRINKS: "Drinks",
  COMBOS: "Combos",
};

function Card({ item }: { item: MenuItem }) {
  const { qty, add, dec } = useCart();
  const q = qty(item.id);

  return (
    <motion.article
      layout
      className="card"
      initial={{ opacity: 0, scale: 0.94 }}
      whileInView={{ opacity: 1, scale: 1 }}
      exit={{ opacity: 0, scale: 0.94 }}
      viewport={{ once: true, margin: "0px 0px -40px 0px" }}
      transition={{ type: "spring", stiffness: 380, damping: 30 }}
    >
      <div className="card-art">
        {item.bestSeller && <span className="sticker">Best seller</span>}
        <FoodArt item={item} className="card-art-img" />
      </div>
      <div className="card-body">
        <div className="card-title">
          <VegMark veg={item.veg} />
          <h3>{item.name}</h3>
        </div>
        <p className="card-desc">{item.description}</p>
        <p className="card-meta">
          <Spice level={item.spiceLevel} />
          <span>{item.calories} kcal</span>
        </p>
        <div className="card-foot">
          <span className="price">{inr(item.priceInr)}</span>
          {q === 0 ? (
            <button type="button" className="btn btn-red btn-sm" onClick={() => add(item.id)} aria-label={`Add ${item.name} to cart`}>
              Add
            </button>
          ) : (
            <div className="stepper" role="group" aria-label={`${item.name} quantity`}>
              <button type="button" onClick={() => dec(item.id)} aria-label={`Remove one ${item.name}`}>
                <MinusIcon width={16} height={16} />
              </button>
              <output aria-live="polite">{q}</output>
              <button type="button" onClick={() => add(item.id)} disabled={q >= 20} aria-label={`Add one more ${item.name}`}>
                <PlusIcon width={16} height={16} />
              </button>
            </div>
          )}
        </div>
      </div>
    </motion.article>
  );
}

export function Menu() {
  const { items, restaurant } = useCart();
  const [category, setCategory] = useState<Category | "ALL">("ALL");
  const [vegOnly, setVegOnly] = useState(false);

  const categories = useMemo(() => Array.from(new Set(items.map((i) => i.category))), [items]);
  const shown = useMemo(
    () => items.filter((i) => (category === "ALL" || i.category === category) && (!vegOnly || i.veg)),
    [items, category, vegOnly],
  );

  return (
    <section id="menu" className="menu" aria-labelledby="menu-title">
      <div className="wrap menu-head">
        <h2 id="menu-title" className="display h2">
          Pick your stack
        </h2>
        <p className="lede">
          Every burger is smashed to order. Prices are before GST, which is 5% and added at checkout. Delivery is{" "}
          {inr(restaurant.deliveryFeeInr)}, or free over {inr(restaurant.freeDeliveryAboveInr)}.
        </p>
      </div>

      <div className="menu-bar">
        <div className="wrap menu-bar-inner">
          <div className="chips" role="group" aria-label="Menu category">
            <button type="button" className="chip" aria-pressed={category === "ALL"} onClick={() => setCategory("ALL")}>
              All
            </button>
            {categories.map((c) => (
              <button key={c} type="button" className="chip" aria-pressed={category === c} onClick={() => setCategory(c)}>
                {CATEGORY_LABEL[c]}
              </button>
            ))}
          </div>
          <label className="toggle">
            <input type="checkbox" checked={vegOnly} onChange={(e) => setVegOnly(e.target.checked)} />
            <span className="toggle-track" aria-hidden="true" />
            Veg only
          </label>
        </div>
      </div>

      <div className="wrap">
        <p className="sr-only" role="status">
          Showing {shown.length} {shown.length === 1 ? "item" : "items"}
        </p>
        <div className="grid">
          <AnimatePresence mode="popLayout">
            {shown.map((item) => (
              <Card key={item.id} item={item} />
            ))}
          </AnimatePresence>
        </div>
        {shown.length === 0 && (
          <p className="empty">
            Nothing here
            {vegOnly && category !== "ALL" ? ` in ${CATEGORY_LABEL[category].toLowerCase()} for veg` : ""}. Try another
            category{vegOnly ? " or turn off Veg only" : ""}.
          </p>
        )}
      </div>
    </section>
  );
}

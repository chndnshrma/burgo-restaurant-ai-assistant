"use client";

import { useEffect, useRef, useState } from "react";
import {
  cubicBezier,
  motion,
  useMotionValue,
  useReducedMotion,
  useScroll,
  useTransform,
  type MotionValue,
} from "motion/react";
import { useCart } from "@/lib/cart";
import { inr, to12h } from "@/lib/format";
import { BurgerSvg, CLASSIC, LAYER_ORDER, LAYERS, type LayerId } from "./art/BurgerArt";
import { ChevronDown } from "./icons";

/*
 * The hero is one tall section with a pinned stage. Scrolling through it pulls the burger apart:
 * every layer slides up by its own distance (the top bun the furthest), then the ingredient labels appear.
 * All of it is driven by one scroll progress value, so nothing re-renders while you scroll.
 */

const VB_TOP = -212;
const VB_H = 652;
const VIEWBOX = `0 ${VB_TOP} 400 ${VB_H}`;

/** How far each layer travels (in SVG units) from the stacked burger to the pulled-apart one. */
const EXPLODE: Record<LayerId, number> = {
  bottomBun: 0,
  patty1: -28,
  cheese1: -91,
  patty2: -120,
  cheese2: -184,
  pickles: -219,
  sauce: -277,
  topBun: -295,
};

/** Where each layer's middle sits in the stacked burger. */
const CENTER: Record<LayerId, number> = {
  bottomBun: 404,
  patty1: 362,
  cheese1: 355,
  patty2: 314,
  cheese2: 308,
  pickles: 273,
  sauce: 261,
  topBun: 171,
};

/** These describe the Classic Smash, the burger drawn here. */
const LABELS: Record<LayerId, string> = {
  bottomBun: "Toasted bun",
  patty1: "Smashed chicken patty",
  cheese1: "Melted cheddar",
  patty2: "Smashed chicken patty",
  cheese2: "Melted cheddar",
  pickles: "Pickles",
  sauce: "Burger sauce",
  topBun: "Sesame bun",
};

const SPREAD_START = 0.12;
const SPREAD_END = 0.62;
const easeOut = cubicBezier(0.22, 1, 0.36, 1);

function Layer({ id, progress }: { id: LayerId; progress: MotionValue<number> }) {
  const y = useTransform(progress, [SPREAD_START, SPREAD_END], [0, EXPLODE[id]], { ease: easeOut });
  return <motion.g style={{ y }}>{LAYERS[id](CLASSIC)}</motion.g>;
}

function Label({ id, index, progress }: { id: LayerId; index: number; progress: MotionValue<number> }) {
  const start = 0.58 + index * 0.03;
  const opacity = useTransform(progress, [start, start + 0.07], [0, 1]);
  const x = useTransform(progress, [start, start + 0.07], [-10, 0]);
  const top = ((CENTER[id] + EXPLODE[id] - VB_TOP) / VB_H) * 100;
  return (
    <motion.div className="callout" style={{ top: `${top}%`, opacity, x }}>
      {LABELS[id]}
    </motion.div>
  );
}

/** True on phone-sized screens, where the burger gets less room and so a gentler zoom. */
function useNarrow() {
  const [narrow, setNarrow] = useState(false);
  useEffect(() => {
    const mq = window.matchMedia("(max-width: 860px)");
    const update = () => setNarrow(mq.matches);
    update();
    mq.addEventListener("change", update);
    return () => mq.removeEventListener("change", update);
  }, []);
  return narrow;
}

export function Hero() {
  const { restaurant } = useCart();
  const reduce = useReducedMotion();
  const ref = useRef<HTMLElement>(null);
  const { scrollYProgress } = useScroll({ target: ref, offset: ["start start", "end end"] });
  const still = useMotionValue(0);
  const progress = reduce ? still : scrollYProgress;

  const narrow = useNarrow();
  const zoomWide = useTransform(progress, [SPREAD_START - 0.02, SPREAD_END], [1.3, 1], { ease: easeOut });
  const zoomNarrow = useTransform(progress, [SPREAD_START - 0.02, SPREAD_END], [1.06, 1], { ease: easeOut });
  const scale = narrow ? zoomNarrow : zoomWide;
  // The stacked burger sits in the lower part of the drawing; lift it so it starts centred in the stage.
  const lift = useTransform(progress, [SPREAD_START - 0.02, SPREAD_END], ["-22.7%", "0%"], { ease: easeOut });
  const hintOpacity = useTransform(progress, [0, 0.06], [1, 0]);
  const lines = restaurant.tagline.split(/(?<=\.)\s+/);

  return (
    <section id="top" ref={ref} className={reduce ? "hero hero-static" : "hero"} aria-labelledby="hero-title">
      <div className="hero-pin">
        <div className="hero-copy">
          <h1 id="hero-title" className="display hero-title">
            {lines.map((line) => (
              <span key={line}>{line}</span>
            ))}
          </h1>
          <p className="hero-sub">
            Two patties pressed flat on a hot grill and stacked to order. Get it for pickup or delivery, or book a
            table for tonight.
          </p>
          <div className="hero-actions">
            <a className="btn btn-yellow" href="#menu">
              Order now
            </a>
            <a className="btn btn-light" href="#reserve">
              Book a table
            </a>
          </div>
          <p className="hero-facts">
            Open daily, {to12h(restaurant.opens)} to {to12h(restaurant.closes)}. Free delivery over{" "}
            {inr(restaurant.freeDeliveryAboveInr)} within {restaurant.deliveryRadiusKm} km.
          </p>
        </div>

        <div className="hero-stage">
          <motion.div className="burger-wrap" style={{ scale, y: lift, transformOrigin: "50% 73%" }}>
            <BurgerSvg viewBox={VIEWBOX} label="A smash burger pulling apart to show its layers">
              {LAYER_ORDER.map((id) => (
                <Layer key={id} id={id} progress={progress} />
              ))}
            </BurgerSvg>
          </motion.div>
          <div className="callouts" aria-hidden="true">
            {LAYER_ORDER.map((id, i) => (
              <Label key={id} id={id} index={i} progress={progress} />
            ))}
          </div>
        </div>

        {!reduce && (
          <motion.p className="scroll-hint" style={{ opacity: hintOpacity }} aria-hidden="true">
            Scroll to take it apart
            <ChevronDown />
          </motion.p>
        )}
      </div>
    </section>
  );
}

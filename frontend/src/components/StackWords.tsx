"use client";

import { useRef } from "react";
import { motion, useReducedMotion, useScroll, useTransform } from "motion/react";

/** Three giant words that slide sideways as you scroll past them, each row the opposite way to the one above. */
export function StackWords() {
  const ref = useRef<HTMLElement>(null);
  const reduce = useReducedMotion();
  const { scrollYProgress } = useScroll({ target: ref, offset: ["start end", "end start"] });
  const x1 = useTransform(scrollYProgress, [0, 1], ["6%", "-34%"]);
  const x2 = useTransform(scrollYProgress, [0, 1], ["-40%", "2%"]);
  const x3 = useTransform(scrollYProgress, [0, 1], ["4%", "-36%"]);
  const rep = (word: string) => Array.from({ length: 4 }, () => word).join(" ");

  return (
    <section ref={ref} className="words" aria-labelledby="words-title">
      <h2 id="words-title" className="sr-only">
        How we make a burger
      </h2>
      <div aria-hidden="true">
        <motion.p className="word-row" style={reduce ? undefined : { x: x1 }}>
          {rep("Smash.")}
        </motion.p>
        <motion.p className="word-row word-fill" style={reduce ? undefined : { x: x2 }}>
          {rep("Stack.")}
        </motion.p>
        <motion.p className="word-row" style={reduce ? undefined : { x: x3 }}>
          {rep("Serve.")}
        </motion.p>
      </div>
      <p className="words-copy">
        We press each patty flat on a very hot grill for a crisp, lacy edge, melt the cheese on top, and stack it the
        moment it comes off. Nothing sits under a lamp.
      </p>
    </section>
  );
}

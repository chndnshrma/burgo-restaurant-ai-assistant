import type { SVGProps } from "react";

const base = {
  width: 20,
  height: 20,
  viewBox: "0 0 24 24",
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2.5,
  strokeLinecap: "round",
  strokeLinejoin: "round",
  "aria-hidden": true,
} as const;

type P = SVGProps<SVGSVGElement>;

export const CartIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M3 4h2.5l2.2 10.2a1 1 0 0 0 1 .8h8.6a1 1 0 0 0 1-.8L20 8H6.2" />
    <circle cx="9.5" cy="19.5" r="1.2" fill="currentColor" />
    <circle cx="17" cy="19.5" r="1.2" fill="currentColor" />
  </svg>
);

export const ChatIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M4 5h16v11H9.5L5 20v-4H4z" />
  </svg>
);

export const CloseIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M6 6l12 12M18 6L6 18" />
  </svg>
);

export const PlusIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M12 5v14M5 12h14" />
  </svg>
);

export const MinusIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M5 12h14" />
  </svg>
);

export const SendIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M4 12l16-8-6 16-2.5-6.5z" />
  </svg>
);

export const ChevronDown = (p: P) => (
  <svg {...base} {...p}>
    <path d="M6 9l6 6 6-6" />
  </svg>
);

export const CheckIcon = (p: P) => (
  <svg {...base} {...p}>
    <path d="M5 12.5l4.5 4.5L19 7.5" />
  </svg>
);

/** A sesame seed, used as the separator in the ticker strip. */
export const Seed = (p: P) => (
  <svg width={18} height={12} viewBox="0 0 18 12" fill="currentColor" aria-hidden="true" {...p}>
    <ellipse cx="9" cy="6" rx="8" ry="4.5" transform="rotate(-18 9 6)" />
  </svg>
);

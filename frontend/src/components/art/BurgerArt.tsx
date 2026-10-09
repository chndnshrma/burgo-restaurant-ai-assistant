import type { ReactElement, ReactNode } from "react";

export const INK = "#2A140B";

export type LayerId = "bottomBun" | "patty1" | "cheese1" | "patty2" | "cheese2" | "pickles" | "sauce" | "topBun";

/** Bottom to top. This is also the order the layers are painted in. */
export const LAYER_ORDER: LayerId[] = [
  "bottomBun",
  "patty1",
  "cheese1",
  "patty2",
  "cheese2",
  "pickles",
  "sauce",
  "topBun",
];

export interface BurgerTheme {
  patty: string;
  pattyMark: string;
  sauce: string;
}

export const CLASSIC: BurgerTheme = { patty: "#6B3A1E", pattyMark: "#4A2512", sauce: "#E5341D" };
export const VEG_GOLD: BurgerTheme = { patty: "#C98A3B", pattyMark: "#A56A22", sauce: "#7BB83D" };
export const HOT: BurgerTheme = { patty: "#5A2A14", pattyMark: "#3A1A0C", sauce: "#FF6A1A" };
export const MAYO: BurgerTheme = { patty: "#6B3A1E", pattyMark: "#4A2512", sauce: "#FFF1C9" };

const BUN = "#F2A33A";
const CHEESE = "#FFC629";

function pattyLayer(y: number, theme: BurgerTheme, marks: string): ReactElement {
  return (
    <g>
      <rect x={44} y={y} width={312} height={44} rx={20} fill={theme.patty} />
      <path d={marks} fill="none" stroke={theme.pattyMark} strokeWidth={5} />
    </g>
  );
}

/** Each layer of the burger, drawn at the spot where it rests in the finished stack (viewBox 400 x 440). */
export const LAYERS: Record<LayerId, (t: BurgerTheme) => ReactElement> = {
  bottomBun: () => <path d="M52 382 H348 V398 Q348 426 318 426 H82 Q52 426 52 398 Z" fill={BUN} />,
  patty1: (t) => pattyLayer(340, t, "M80 362 l10 6 M150 356 l12 5 M232 364 l10 5 M296 356 l10 6"),
  cheese1: () => (
    <path
      d="M38 334 H362 V350 H338 Q330 350 330 358 V374 Q330 384 321 384 Q312 384 312 374 V360 Q312 350 304 350 H112 Q104 350 104 358 V366 Q104 376 95 376 Q86 376 86 366 V356 Q86 350 78 350 H38 Z"
      fill={CHEESE}
    />
  ),
  patty2: (t) => pattyLayer(292, t, "M70 314 l12 6 M142 308 l10 5 M220 316 l12 5 M300 308 l10 6"),
  cheese2: () => (
    <path
      d="M38 286 H362 V302 H344 Q336 302 336 310 V326 Q336 336 327 336 Q318 336 318 326 V312 Q318 302 310 302 H130 Q122 302 122 310 V318 Q122 328 113 328 Q104 328 104 318 V308 Q104 302 96 302 H38 Z"
      fill={CHEESE}
    />
  ),
  pickles: () => (
    <g fill="#7BB83D">
      <ellipse cx={96} cy={276} rx={44} ry={13} />
      <ellipse cx={204} cy={270} rx={44} ry={13} />
      <ellipse cx={308} cy={276} rx={44} ry={13} />
    </g>
  ),
  sauce: (t) => (
    <path
      d="M58 254 Q88 238 118 254 T178 254 T238 254 T298 254 T342 250 V266 Q318 278 298 266 T238 266 T178 266 T118 266 T58 268 Z"
      fill={t.sauce}
    />
  ),
  topBun: () => (
    <g transform="translate(0 -36)">
      <path d="M48 262 C48 190 108 134 200 134 C292 134 352 190 352 262 Q352 280 334 280 H66 Q48 280 48 262 Z" fill={BUN} />
      <path d="M84 190 Q120 152 176 148" fill="none" stroke="#FFD08A" strokeWidth={8} />
      <g fill="#FFF1C9" strokeWidth={3}>
        <ellipse cx={140} cy={176} rx={9} ry={5} transform="rotate(-20 140 176)" />
        <ellipse cx={200} cy={162} rx={9} ry={5} />
        <ellipse cx={258} cy={178} rx={9} ry={5} transform="rotate(25 258 178)" />
        <ellipse cx={170} cy={214} rx={9} ry={5} transform="rotate(12 170 214)" />
        <ellipse cx={236} cy={216} rx={9} ry={5} transform="rotate(-14 236 216)" />
        <ellipse cx={108} cy={228} rx={9} ry={5} transform="rotate(-30 108 228)" />
        <ellipse cx={294} cy={230} rx={9} ry={5} transform="rotate(30 294 230)" />
      </g>
    </g>
  ),
};

/** The shared outline style and viewBox for burger drawings, so every layer lines up. */
export function BurgerSvg({
  children,
  className,
  label = "A double smash burger",
  viewBox = "0 0 400 440",
}: {
  children: ReactNode;
  className?: string;
  /** Pass an empty string for a purely decorative burger. */
  label?: string;
  viewBox?: string;
}) {
  return (
    <svg
      viewBox={viewBox}
      className={className}
      role={label ? "img" : undefined}
      aria-label={label || undefined}
      aria-hidden={label ? undefined : true}
    >
      <g stroke={INK} strokeWidth={6} strokeLinejoin="round" strokeLinecap="round">
        {children}
      </g>
    </svg>
  );
}

/** The finished burger, all layers in place. */
export function BurgerArt({
  theme = CLASSIC,
  className,
  label,
}: {
  theme?: BurgerTheme;
  className?: string;
  label?: string;
}) {
  return (
    <BurgerSvg className={className} label={label}>
      {LAYER_ORDER.map((id) => (
        <g key={id}>{LAYERS[id](theme)}</g>
      ))}
    </BurgerSvg>
  );
}

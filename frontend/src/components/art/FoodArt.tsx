import type { Category, MenuItem } from "@/lib/types";
import { BurgerArt, CLASSIC, HOT, INK, MAYO, VEG_GOLD } from "./BurgerArt";

const stroke = { stroke: INK, strokeWidth: 5, strokeLinejoin: "round", strokeLinecap: "round" } as const;

function Wrap({ veg }: { veg: boolean }) {
  return (
    <svg viewBox="0 0 200 150" role="img" aria-label="A wrap">
      <g {...stroke} transform="rotate(-16 100 78)">
        <rect x={26} y={52} width={148} height={52} rx={26} fill="#F7D9A0" />
        <ellipse cx={38} cy={78} rx={13} ry={25} fill="#FFF1C9" />
        <g fill={veg ? "#7BB83D" : "#E5341D"} strokeWidth={3}>
          <circle cx={35} cy={68} r={6} />
          <circle cx={40} cy={82} r={6} />
        </g>
        <rect x={112} y={46} width={70} height={64} rx={14} fill="#E6E6E6" />
        <path d="M126 56 V100 M140 56 V100 M154 56 V100 M168 56 V100" fill="none" strokeWidth={3} />
      </g>
    </svg>
  );
}

function Fries({ spicy }: { spicy: boolean }) {
  const sticks = [
    [58, 22, 14, 70],
    [74, 12, 14, 80],
    [90, 24, 14, 68],
    [106, 8, 14, 84],
    [122, 20, 14, 72],
  ];
  return (
    <svg viewBox="0 0 200 150" role="img" aria-label="Fries">
      <g {...stroke}>
        {sticks.map(([x, y, w, h]) => (
          <rect key={x} x={x} y={y} width={w} height={h} rx={5} fill="#FFC629" />
        ))}
        {spicy && (
          <g fill="#E5341D" strokeWidth={0}>
            <circle cx={82} cy={44} r={3} />
            <circle cx={112} cy={36} r={3} />
            <circle cx={96} cy={60} r={3} />
            <circle cx={128} cy={54} r={3} />
          </g>
        )}
        <path d="M50 70 H146 L136 138 H60 Z" fill="#E5341D" />
        <path d="M64 88 H132" fill="none" stroke="#FFF" strokeWidth={6} />
      </g>
    </svg>
  );
}

function Drink({ shake }: { shake: boolean }) {
  return (
    <svg viewBox="0 0 200 150" role="img" aria-label="A drink">
      <g {...stroke}>
        <path d="M112 8 L124 52" fill="none" strokeWidth={8} />
        {shake && <path d="M66 52 Q66 22 100 22 Q134 22 134 52 Z" fill="#FFF1C9" />}
        <rect x={60} y={50} width={80} height={14} rx={7} fill="#FFFFFF" />
        <path d="M66 64 H134 L124 140 H76 Z" fill={shake ? "#FFB8C6" : "#FFE27A"} />
        <path d="M72 92 H128" fill="none" stroke="#FFFFFF" strokeWidth={8} />
      </g>
    </svg>
  );
}

function Bag() {
  return (
    <svg viewBox="0 0 200 150" role="img" aria-label="A paper bag of food">
      <g {...stroke}>
        <rect x={64} y={14} width={12} height={52} rx={5} fill="#FFC629" transform="rotate(-8 70 40)" />
        <rect x={86} y={8} width={12} height={58} rx={5} fill="#FFC629" />
        <path d="M118 6 L128 60" fill="none" strokeWidth={8} />
        <path d="M44 56 H156 L166 140 H34 Z" fill="#D9A066" />
        <path d="M44 56 H156 L152 74 H48 Z" fill="#C48848" />
        <circle cx={100} cy={106} r={20} fill="#E5341D" />
        <path d="M90 106 H110 M100 96 V116" fill="none" stroke="#FFF" strokeWidth={5} />
      </g>
    </svg>
  );
}

/** A small illustration for a menu item, picked from its category and shaped by what's in it. */
export function FoodArt({ item, className }: { item: Pick<MenuItem, "category" | "veg" | "spiceLevel" | "name">; className?: string }) {
  const cat: Category = item.category;
  let art;
  if (cat === "BURGERS") {
    const theme = item.veg ? VEG_GOLD : item.spiceLevel >= 3 ? HOT : item.spiceLevel === 0 ? MAYO : CLASSIC;
    art = <BurgerArt theme={theme} label={item.name} />;
  } else if (cat === "WRAPS") {
    art = <Wrap veg={item.veg} />;
  } else if (cat === "SIDES") {
    art = <Fries spicy={item.spiceLevel >= 2} />;
  } else if (cat === "DRINKS") {
    art = <Drink shake={/shake|coffee/i.test(item.name)} />;
  } else {
    art = <Bag />;
  }
  return <div className={className}>{art}</div>;
}

export function VegMark({ veg }: { veg: boolean }) {
  const c = veg ? "#1E8E3E" : "#B3261E";
  return (
    <svg width={18} height={18} viewBox="0 0 18 18" role="img" aria-label={veg ? "Vegetarian" : "Non-vegetarian"}>
      <title>{veg ? "Vegetarian" : "Non-vegetarian"}</title>
      <rect x={1.5} y={1.5} width={15} height={15} rx={3} fill="#fff" stroke={c} strokeWidth={2} />
      <circle cx={9} cy={9} r={4} fill={c} />
    </svg>
  );
}

const SPICE_WORDS = ["Not spicy", "Mild", "Spicy", "Very spicy"];

export function Spice({ level }: { level: number }) {
  if (level <= 0) return null;
  return (
    <span className="spice" role="img" aria-label={SPICE_WORDS[Math.min(level, 3)]} title={SPICE_WORDS[Math.min(level, 3)]}>
      {[1, 2, 3].map((n) => (
        <svg key={n} width={14} height={14} viewBox="0 0 16 16" aria-hidden="true">
          <path
            d="M4 3 C9 1 14 5 14 12 C10 12 4 10 4 3 Z"
            fill={n <= level ? "#E5341D" : "none"}
            stroke={n <= level ? "#2A140B" : "#B9A89C"}
            strokeWidth={1.6}
            strokeLinejoin="round"
          />
        </svg>
      ))}
    </span>
  );
}

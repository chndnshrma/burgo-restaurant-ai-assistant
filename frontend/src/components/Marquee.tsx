import { inr, to12h } from "@/lib/format";
import type { RestaurantInfo } from "@/lib/types";
import { Seed } from "./icons";

/** A ticker strip of the practical facts. Pauses on hover and stops entirely for reduced motion. */
export function Marquee({ restaurant: r }: { restaurant: RestaurantInfo }) {
  const items = [
    `Open daily, ${to12h(r.opens)} to ${to12h(r.closes)}`,
    `Free delivery over ${inr(r.freeDeliveryAboveInr)}`,
    "Smashed to order",
    `${r.seatCapacity} seats, book ahead`,
    "Veg and non-veg",
    `Delivering within ${r.deliveryRadiusKm} km`,
  ];

  const group = (hidden: boolean) => (
    <div className="marquee-group" aria-hidden={hidden || undefined}>
      {items.map((text) => (
        <span key={text} className="marquee-item">
          {text}
          <Seed />
        </span>
      ))}
    </div>
  );

  return (
    <div className="marquee" role="region" aria-label="Quick facts">
      <div className="marquee-track">
        {group(false)}
        {group(true)}
      </div>
    </div>
  );
}

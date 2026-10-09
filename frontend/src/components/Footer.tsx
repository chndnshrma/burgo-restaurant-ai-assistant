import { inr, lastSeating, to12h } from "@/lib/format";
import type { RestaurantInfo } from "@/lib/types";

export function Footer({ restaurant: r }: { restaurant: RestaurantInfo }) {
  const tel = `tel:${r.phone.replace(/\s/g, "")}`;
  return (
    <footer id="visit" className="footer">
      <div className="wrap footer-grid">
        <div>
          <p className="display footer-brand">{r.name}</p>
          <p>{r.tagline}</p>
        </div>
        <div>
          <h2 className="footer-h">Find us</h2>
          <address>{r.address}</address>
          <p>{r.parkingNote}</p>
        </div>
        <div>
          <h2 className="footer-h">Hours</h2>
          <p>
            Every day, {to12h(r.opens)} to {to12h(r.closes)}.
          </p>
          <p>Last table at {to12h(lastSeating(r.closes))}.</p>
        </div>
        <div>
          <h2 className="footer-h">Delivery and calls</h2>
          <p>
            Within {r.deliveryRadiusKm} km. Orders from {inr(r.minDeliveryOrderInr)}, free over{" "}
            {inr(r.freeDeliveryAboveInr)}.
          </p>
          <p>
            <a href={tel}>{r.phone}</a>
          </p>
        </div>
      </div>
      <p className="wrap footer-note">
        Sample restaurant for a demo project. The menu, prices and address are made up.
      </p>
    </footer>
  );
}

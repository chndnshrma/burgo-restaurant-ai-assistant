"use client";

import { useCart } from "@/lib/cart";
import { BurgerArt } from "./art/BurgerArt";
import { CartIcon } from "./icons";

export function Nav() {
  const { restaurant, count, setOpen } = useCart();
  const short = restaurant.name.split(" ")[0];

  return (
    <header className="nav">
      <div className="nav-inner">
        <a href="#top" className="brand" aria-label={`${restaurant.name}, back to top`}>
          <BurgerArt className="brand-mark" label="" />
          <span>{short}</span>
        </a>
        <nav className="nav-links" aria-label="Main">
          <a href="#menu">Menu</a>
          <a href="#reserve">Book a table</a>
          <a href="#visit">Find us</a>
        </nav>
        <button
          type="button"
          className="btn btn-ink btn-sm cart-btn"
          onClick={() => setOpen(true)}
          aria-label={count > 0 ? `Open cart, ${count} ${count === 1 ? "item" : "items"}` : "Open cart"}
        >
          <CartIcon />
          <span>Cart</span>
          {count > 0 && (
            <span className="badge" key={count}>
              {count}
            </span>
          )}
        </button>
      </div>
    </header>
  );
}

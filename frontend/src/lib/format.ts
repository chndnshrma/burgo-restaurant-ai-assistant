export const inr = (n: number) => `₹${n.toLocaleString("en-IN")}`;

/** "19:30" or "19:30:00" -> "7:30 pm" */
export function to12h(time: string): string {
  const [h, m] = time.split(":").map(Number);
  const suffix = h >= 12 ? "pm" : "am";
  const hour = h % 12 === 0 ? 12 : h % 12;
  return `${hour}:${String(m).padStart(2, "0")} ${suffix}`;
}

/** The last table is seated an hour before closing. */
export function lastSeating(closes: string): string {
  const [h, m] = closes.split(":").map(Number);
  const total = h * 60 + m - 60;
  return `${String(Math.floor(total / 60)).padStart(2, "0")}:${String(total % 60).padStart(2, "0")}`;
}

export function todayISO(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

export function addDaysISO(iso: string, days: number): string {
  const [y, m, d] = iso.split("-").map(Number);
  const date = new Date(y, m - 1, d + days);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

export function prettyDate(iso: string): string {
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d).toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short" });
}

/** Mirrors the backend's pricing for the cart preview. The server's numbers are the ones that count. */
export function estimate(subtotal: number, type: "PICKUP" | "DELIVERY", fee: number, freeAbove: number) {
  const tax = Math.floor((subtotal * 5 + 50) / 100);
  const delivery = type === "DELIVERY" && subtotal < freeAbove ? fee : 0;
  return { tax, delivery, total: subtotal + tax + delivery };
}

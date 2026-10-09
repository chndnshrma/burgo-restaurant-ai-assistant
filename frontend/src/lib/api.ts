import type {
  ChatResponse,
  OrderRequest,
  OrderResponse,
  ReservationRequest,
  ReservationResponse,
  Slot,
} from "./types";

export const SLUG = process.env.NEXT_PUBLIC_RESTAURANT_SLUG ?? "burgo";
const BASE = `/api/r/${SLUG}`;

/** Every error from the backend has a customer-ready message, and field errors when a form is invalid. */
export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public fieldErrors: Record<string, string> = {},
  ) {
    super(message);
  }
}

async function call<T>(path: string, init?: RequestInit, signal?: AbortSignal): Promise<T> {
  let res: Response;
  try {
    res = await fetch(BASE + path, {
      ...init,
      signal,
      headers: { "Content-Type": "application/json", ...init?.headers },
    });
  } catch (e) {
    if (e instanceof DOMException && e.name === "AbortError") throw e;
    throw new ApiError(0, "We can't reach the restaurant right now. Check your connection and try again.");
  }
  let body: unknown = null;
  try {
    const text = await res.text();
    body = text ? JSON.parse(text) : null;
  } catch {
    // not JSON (for example a proxy error page): fall through to the generic message
  }
  if (!res.ok) {
    const err = (body ?? {}) as { message?: string; fieldErrors?: Record<string, string> };
    throw new ApiError(res.status, err.message ?? "Something went wrong. Please try again.", err.fieldErrors ?? {});
  }
  return body as T;
}

export const getSlots = (date: string, signal?: AbortSignal) =>
  call<{ date: string; slots: Slot[] }>(`/reservations/slots?date=${encodeURIComponent(date)}`, undefined, signal);

export const reserve = (req: ReservationRequest) =>
  call<ReservationResponse>("/reservations", { method: "POST", body: JSON.stringify(req) });

export const placeOrder = (req: OrderRequest) =>
  call<OrderResponse>("/orders", { method: "POST", body: JSON.stringify(req) });

export const sendChat = (message: string) =>
  call<ChatResponse>("/chat", { method: "POST", body: JSON.stringify({ message }) });

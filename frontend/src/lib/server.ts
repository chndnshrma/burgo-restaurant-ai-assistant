import { SLUG } from "./api";
import type { MenuResponse } from "./types";

const API_URL = process.env.API_URL ?? "http://localhost:8080";

export { API_URL };

/** Loads the menu on the server. Returns null if the backend isn't reachable, so the page can say so. */
export async function getMenu(): Promise<MenuResponse | null> {
  try {
    const res = await fetch(`${API_URL}/api/r/${SLUG}/menu`, { cache: "no-store" });
    if (!res.ok) return null;
    return (await res.json()) as MenuResponse;
  } catch {
    return null;
  }
}

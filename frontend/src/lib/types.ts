export type Category = "BURGERS" | "WRAPS" | "SIDES" | "DRINKS" | "COMBOS";
export type OrderType = "PICKUP" | "DELIVERY";

export interface RestaurantInfo {
  slug: string;
  name: string;
  tagline: string;
  address: string;
  phone: string;
  opens: string; // "11:00"
  closes: string; // "23:00"
  seatCapacity: number;
  deliveryRadiusKm: number;
  deliveryFeeInr: number;
  freeDeliveryAboveInr: number;
  minDeliveryOrderInr: number;
  parkingNote: string;
}

export interface MenuItem {
  id: number;
  name: string;
  description: string;
  category: Category;
  priceInr: number;
  veg: boolean;
  spiceLevel: number; // 0 to 3
  bestSeller: boolean;
  calories: number;
}

export interface MenuResponse {
  restaurant: RestaurantInfo;
  items: MenuItem[];
}

export interface Slot {
  time: string; // "19:30"
  seatsLeft: number;
}

export interface ReservationRequest {
  name: string;
  phone: string;
  date: string; // yyyy-mm-dd
  time: string; // HH:mm
  partySize: number;
  notes?: string;
}

export interface ReservationResponse {
  reference: string;
  name: string;
  date: string;
  time: string; // "19:30:00"
  partySize: number;
  status: string;
}

export interface OrderRequest {
  type: OrderType;
  name: string;
  phone: string;
  address?: string;
  notes?: string;
  items: { menuItemId: number; quantity: number }[];
}

export interface OrderResponse {
  reference: string;
  status: string;
  type: OrderType;
  items: { name: string; quantity: number; unitPriceInr: number; lineTotalInr: number }[];
  subtotalInr: number;
  taxInr: number;
  deliveryFeeInr: number;
  totalInr: number;
  etaMinutes: number;
  createdAt: string;
}

export interface ChatResponse {
  reply: string;
  action: "OPEN_RESERVATION" | "SCROLL_MENU" | null;
  suggestions: string[];
}

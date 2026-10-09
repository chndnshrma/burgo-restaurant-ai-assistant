import type { Metadata, Viewport } from "next";
import "@fontsource/bowlby-one";
import "@fontsource-variable/figtree";
import "./globals.css";

export const metadata: Metadata = {
  title: "Burgo Smash Kitchen: smash burgers, wraps and shakes",
  description:
    "Order smash burgers for pickup or delivery, or book a table. Ask our assistant about the menu, hours and delivery.",
};

export const viewport: Viewport = {
  themeColor: "#D92D16",
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}

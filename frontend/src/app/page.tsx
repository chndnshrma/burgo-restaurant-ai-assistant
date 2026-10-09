import { CartProvider } from "@/lib/cart";
import { getMenu } from "@/lib/server";
import { CartDrawer } from "@/components/CartDrawer";
import { Chat } from "@/components/Chat";
import { Footer } from "@/components/Footer";
import { Hero } from "@/components/Hero";
import { Marquee } from "@/components/Marquee";
import { Menu } from "@/components/Menu";
import { Nav } from "@/components/Nav";
import { Offline } from "@/components/Offline";
import { Reserve } from "@/components/Reserve";
import { StackWords } from "@/components/StackWords";

// The menu comes from the backend on every request, so a price change shows up straight away.
export const dynamic = "force-dynamic";

export default async function Page() {
  const data = await getMenu();
  if (!data) return <Offline />;

  return (
    <CartProvider restaurant={data.restaurant} items={data.items}>
      <a className="skip" href="#menu">
        Skip to the menu
      </a>
      <Nav />
      <main>
        <Hero />
        <Marquee restaurant={data.restaurant} />
        <Menu />
        <StackWords />
        <Reserve />
      </main>
      <Footer restaurant={data.restaurant} />
      <CartDrawer />
      <Chat />
    </CartProvider>
  );
}

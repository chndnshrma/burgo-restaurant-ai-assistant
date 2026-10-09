# Burgo: restaurant website, ordering and chat assistant

A sample fast-food restaurant ("Burgo Smash Kitchen") with a Java backend for the business rules and a Next.js
frontend with scroll animations. All the data (menu, prices, address) is made up and loads automatically.

- **Backend:** Spring Boot 3.5, Java 21, JPA, in-memory H2 by default, PostgreSQL optional.
- **Frontend:** Next.js 16, React 19, TypeScript, Motion for animation.
- **What it does:** menu with filters, cart and ordering (pickup or delivery), table reservations with live
  seat counts, and a chat assistant that answers only from the restaurant's own data.

## Run it

You need Java 21, Maven, and Node 20 or newer. Use two terminals.

```bash
# Terminal 1: backend on http://localhost:8080
cd backend
mvn spring-boot:run

# Terminal 2: frontend on http://localhost:3000
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Open http://localhost:3000. If the backend isn't running, the site shows a "kitchen is closed" page with the
command to start it.

Run the backend tests with `cd backend && mvn test`.

## How it fits together

```
browser ──► Next.js (:3000) ──/api/* rewrite──► Spring Boot (:8080) ──► H2 / PostgreSQL
```

The browser only talks to the Next.js site. `next.config.ts` forwards `/api/*` to the backend, so there is no
CORS setup and the backend address stays on the server.

```
backend/src/main/java/com/burgo/api/
  core/      Business rules in plain Java, no framework: ReservationRules, OrderPricing, ChatEngine
  domain/    JPA entities: Restaurant, MenuItem, Reservation, CustomerOrder, OrderLine
  repo/      Spring Data repositories
  service/   Menu, reservation, order and chat services, plus the optional Gemini client
  web/       REST controller, request and response shapes (Dto), error handling
  config/    CORS, the clock, and the sample-data seeder

frontend/src/
  app/         layout, page, global styles
  components/  Hero (scroll animation), Menu, Reserve, CartDrawer, Chat, and the SVG food art
  lib/         API client, cart state, formatting helpers
```

## API

Every route is under `/api/r/{slug}`, and the sample restaurant's slug is `burgo`.

| Method | Path | What it does |
| --- | --- | --- |
| GET | `/menu` | Restaurant details and available menu items |
| GET | `/reservations/slots?date=YYYY-MM-DD` | Every time slot of the day with seats left |
| POST | `/reservations` | Book a table (1 to 12 guests, 30 minutes' notice, up to 30 days ahead) |
| POST | `/orders` | Place a pickup or delivery order |
| GET | `/orders/{reference}` | Look an order up by its reference, like `ORD-7K3M9Q` |
| POST | `/chat` | Ask the assistant a question |

Errors always look like `{ "message": "...", "fieldErrors": { "name": "..." } }`, and the message is written for
the customer.

## Decisions worth knowing

- **Prices are never trusted from the browser.** An order carries item ids and quantities only. The server reads
  prices from the database and works out GST (5%), delivery fee and total in whole rupees.
- **Bookings can't be oversold.** Creating a reservation takes a row lock on the restaurant, so two people booking
  the last seats at the same moment are handled one after the other.
- **The assistant never guesses.** It answers from the menu and FAQ. For allergy, vegan, jain or halal questions
  it says it can't confirm and points to the staff. For anything it can't match, it gives the restaurant's phone
  number.
- **Optional LLM.** Set `GEMINI_API_KEY` and unanswered questions are sent to Gemini, together with the best
  matching menu and FAQ facts. Without a key, everything still works.
- **Multi-tenant ready.** Every menu item, booking and order belongs to a `restaurant_id`, and routes take the
  restaurant slug. A second restaurant is another row and another `NEXT_PUBLIC_RESTAURANT_SLUG`.
- **Motion respects the user.** With "reduce motion" turned on, the hero does not pin or animate, the ticker
  stops, and the word band stays still.

## PostgreSQL

```bash
createdb burgo
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
# override with DB_URL, DB_USER, DB_PASSWORD if needed
```

## What was checked, and what wasn't

Checked in the build environment:

- The business rules and chat assistant: 19 unit tests, all passing. They were run against a small stand-in for
  the JUnit API, because Maven Central wasn't reachable there.
- The frontend: type-check and production build pass. A browser run against a stand-in API (which used the real
  Java chat engine) covered filters, cart, ordering, booking, chat, keyboard handling, reduced motion, and phone
  and desktop layouts.

**Not checked:** the Spring Boot layer itself (entities, queries, controllers, `BurgoApiTests`) has never been
compiled or started, because dependencies couldn't be downloaded there. Expect to run `mvn spring-boot:run` once
and fix anything small it reports. If the build complains about the Spring Boot version, change it in
`backend/pom.xml` to the latest 3.5.x.

## Where to take it next

1. An owner login and dashboard to edit the menu and see orders and bookings.
2. Upload a menu PDF and answer from it with pgvector retrieval, the same approach as your resume project.
3. A WhatsApp channel for the same assistant and booking flow.
4. Online payments, then email or WhatsApp confirmations.

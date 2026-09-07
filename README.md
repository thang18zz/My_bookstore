# Book Store System — Full-stack reference implementation

Stack: **Vue 3 (Vite + Pinia) + Java 17 / Spring Boot 3 + MySQL + Groq API (chatbot)**.

This project is a working skeleton that implements the full Theme → Epic → Use Case breakdown
(14 Theme / 23 Epic / ~91 Use Case) from the two source documents. Every controller/service method
carries a `// UCx.y.z` comment so you can trace any use case straight to its code.

## How the modules are wired together (this is the part the brief asked for)

```
auth (User) ─┬─> wishlist (User + Product)
             ├─> cart (User) ──> order (checkout reads Cart + Address, writes Order, updates Product.stock)
             ├─> address (User) ─┘
             ├─> review (User + Product)
             ├─> chatbot (ChatSession.user optional) ──reads──> product, order  (via DataSimplifier)
             └─> customer admin (same User entity, Role.CUSTOMER only)

product (Product, Category) ─> wishlist / cart / order-item / review / inventory / stats / chatbot
order (Order, OrderItem)    ─> stats (revenue, best-sellers) / chatbot (order lookup)
```

Nothing is duplicated: e.g. `AdminProductController` calls the *same* `ProductService.getDetail()`
customers use; `StatsService` reads `OrderRepository`/`ProductRepository` directly instead of a
separate reporting table, so admin dashboards can never drift from the live data.

## Backend (`backend/`)

Maven/Spring Boot project, one package per module under `com.bookstore`:

| Package | Theme(s) | Use cases |
|---|---|---|
| `common` | — | JWT auth, ApiResponse envelope, global exception handling |
| `auth` | 1 | UC1.1.1-3, UC1.2.1-2 |
| `product` | 2, 8, 9 | UC2.1.x, UC2.2.x, UC8.1.x, UC9.1.x |
| `wishlist` | 2.3 | UC2.3.1-3 |
| `cart` | 3 | UC3.1.x, UC3.2.x |
| `order` | 4, 5 | UC4.1.x, UC4.2.x, UC5.1.x, UC5.2.x |
| `address` | 6 | UC6.1.1-5 |
| `review` | 7 | UC7.1.x, UC7.2.x |
| `customer` | 10 | UC10.1.1-6 |
| `inventory` | 11 | UC11.1.1-5 |
| `stats` | 12 | UC12.1.x, UC12.2.x |
| `contact` | 13 | UC13.1.1-4 |
| `chatbot` | 14 | UC14.1.1-6, UC14.2.1-5 |

Run it:
```bash
cd backend
export GROQ_API_KEY=gsk_your_real_key      # never commit this
mvn spring-boot:run
```
Create the `bookstore` MySQL schema first (or point `application.yml` at H2 for a quick local run).

### The chatbot pipeline (Epic 14.1/14.2), the part explicitly requested

```
ChatbotController → ChatbotService
                       ├─ IntentDetector.detect(message)        // keyword classifier, no external call
                       ├─ fetch relevant entities (Product / Order / FaqEntry) from their own repos
                       ├─ DataSimplifier.simplify*(...)          // <-- "hàm viết sẵn để đơn giản hóa
                       │                                              dữ liệu đầu vào" the brief asked for
                       ├─ GroqPromptBuilder.build(context, history)
                       └─ GroqClient.chat(messages) → api.groq.com/openai/v1/chat/completions
```

`DataSimplifier` is the single choke point all DB data passes through before it can reach Groq:
it strips PII (passwords, emails, phone numbers, other users' data), flattens JPA entity graphs
into short plain-text lines, and truncates everything to keep prompts small and stable. Every new
data source you want to expose to the chatbot should get its own `simplifyXxx()` method there,
never send an entity to `GroqClient` directly.

`IntentDetector` decides *which* data to fetch (product info vs order lookup vs recommendation vs
handoff) — it is plain keyword matching by design, so it runs before any network call. Swap it for
a real classifier later without touching `ChatbotService`.

## Frontend (`frontend/`)

Vue 3 + Vite + Pinia + vue-router + axios.

- `src/api/*Api.js` — one thin module per backend controller (naming matches 1:1).
- `src/stores/*.js` — Pinia stores for state that must survive navigation: `auth`, `cart`,
  `wishlist`, `chatbot`. Everything else (orders, addresses, admin CRUD) is fetched directly in the
  view component since it's not shared across pages.
- `src/router/index.js` — one route per view, with `meta.auth` / `meta.admin` guards read from
  `stores/auth.js` (the same auth store `http.js` uses to attach the JWT to every request).
- `src/components/ChatbotWidget.vue` — floating widget mounted once in `App.vue`, visible on every
  page; talks to the backend chatbot module described above.

Run it:
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173, proxies /api to the backend on :8080
```

## What's intentionally simplified

- Payment gateway integration is out of scope; `checkout()` marks the order `CONFIRMED` directly.
- Image upload isn't wired up; `imageUrl` is a plain string field (paste a URL).
- `IntentDetector` is keyword-based, not ML-based — swap the implementation, keep the interface.
- Soft delete (an `active`/`isActive` flag) is used for Product and User instead of hard delete, so
  historical orders and reviews never end up pointing at a row that no longer exists.

Theme 14 (Chatbot) itself is a proposed extension per the source documents, not part of the
original reference repository being modeled — this codebase treats it as a first-class module
alongside the other 13 Themes.

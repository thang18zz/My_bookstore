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

## 🚀 Hướng Dẫn Chạy Thủ Công Website (Từ A Đến Z)

### 1. Yêu cầu môi trường (Prerequisites)
- **Java**: JDK 17 trở lên (`java -version`)
- **Maven**: 3.8 trở lên (`mvn -version` hoặc dùng script `mvnw.cmd` có sẵn)
- **Node.js**: Phiên bản 18+ hoặc mới nhất (`node -v` và `npm -v`)
- **MySQL Server**: Phiên bản 8.0 (`mysql --version`)

---

### 2. Bước 1: Khởi động MySQL Server
Đảm bảo dịch vụ MySQL đang chạy trên máy của bạn (mặc định cổng `3306`):
- **Trên Windows (PowerShell quyền Administrator)**:
  ```powershell
  Start-Service MySQL80
  ```
  *(Hoặc mở Services (`services.msc`) -> tìm `MySQL80` -> click Start).*

---

### 3. Bước 2: Cấu hình mật khẩu & API Key cục bộ (Local)
Dự án sử dụng cơ chế bảo mật tách biệt, thông tin nhạy cảm được đặt trong file `application-local.yml` và được chặn bởi `.gitignore` (không sợ lộ khi đẩy lên GitHub).

1. Vào thư mục `backend/src/main/resources/`
2. Tạo file `application-local.yml` (hoặc sao chép từ file mẫu `application-local.yml.example`):
   ```yaml
   spring:
     datasource:
       password: "MatKhauMySQLCuaBan"   # Ví dụ: Example123.

   groq:
     api-key: "gsk_your_groq_api_key"     # API key từ console.groq.com
   ```

---

### 4. Bước 3: Khởi chạy Backend (Spring Boot)
Mở một cửa sổ Terminal tại thư mục gốc dự án:
```powershell
# Di chuyển vào thư mục backend
cd backend

# Chạy ứng dụng bằng Maven
mvn spring-boot:run
```
*(Nếu máy chưa cài sẵn biến môi trường Maven, bạn có thể chạy `.\mvnw.cmd spring-boot:run`)*.

- Khi chạy thành công, Backend sẽ lắng nghe tại: **`http://localhost:8080`**.
- **Cơ chế tự động tạo DB:** Spring Boot sẽ tự động tạo cơ sở dữ liệu `bookstore` và toàn bộ 15 bảng JPA tương ứng mà bạn không cần phải tạo thủ công.

---

### 5. Bước 4 (Tùy chọn): Nạp dữ liệu sản phẩm & danh mục mẫu
Nếu database của bạn đang trống và muốn nạp sẵn danh mục sách cùng các sản phẩm mẫu:

> [!WARNING]
> **Không dùng PowerShell pipe (`|`) để import file SQL có tiếng Việt.** PowerShell không truyền UTF-8 đúng cách sang tiến trình ngoài, khiến toàn bộ ký tự tiếng Việt bị lưu thành dấu hỏi `???` trong database.

```powershell
cmd /c "chcp 65001 >nul & `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`" --default-character-set=utf8mb4 -u root -p bookstore < backend\src\main\resources\seed.sql"
```

*(Thay `MATKHAU_CUA_BAN` bằng mật khẩu MySQL thực tế của bạn, ví dụ: `Example123.`).*

Sau khi import, kiểm tra lại bằng lệnh:
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -pMATKHAU_CUA_BAN -e "SELECT name FROM bookstore.products LIMIT 3;"
```
Kết quả đúng phải hiển thị tên tiếng Việt rõ ràng, không có ký tự `?`.

---

### 6. Bước 5: Khởi chạy Frontend (Vue 3 + Vite)
Mở một cửa sổ Terminal thứ hai tại thư mục gốc dự án:
```powershell
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt thư viện dependencies (chỉ cần chạy lần đầu)
npm install

# Khởi chạy server phát triển
npm run dev
```

- Mở trình duyệt và truy cập: **`http://localhost:5173`**.
- Mọi yêu cầu `/api` từ giao diện sẽ được Vite tự động chuyển tiếp (proxy) sang Backend tại `http://localhost:8080`.

---

## Backend Architecture (`backend/`)


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

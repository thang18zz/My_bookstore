# Book Store System — Full-stack reference implementation

Stack: **Vue 3 (Vite + Pinia) + Java 17 / Spring Boot 3 + MySQL + Mona Pay Sandbox (Online Payment) + Spring Mail (Gmail E-Invoice) + Groq API (Chatbot AI)**.

Dự án là một hệ thống thương mại điện tử nhà sách trực tuyến hoàn chỉnh từ Frontend tới Backend, bao gồm quản lý danh mục, sách, giỏ hàng, sổ địa chỉ nhận hàng, đặt hàng, thanh toán trực tuyến (Mona Pay sandbox) kết hợp gửi email hóa đơn tự động (Gmail SMTP), tích hợp trợ lý ảo AI tư vấn sách (Groq AI Chatbot), và trang quản trị Admin toàn diện.

---

## 🏗 Kiến trúc kết nối các module (Architecture & Data Flow)

```
auth (User) ────────┬─> wishlist (User + Product)
                    ├─> cart (User) ──> order (checkout reads Cart + Address, writes Order, updates Product.stock)
                    ├─> address (User, bắt buộc có Email nhận hóa đơn) ─┘
                    ├─> review (User + Product)
                    ├─> chatbot (ChatSession.user optional) ──reads──> product, order (via DataSimplifier)
                    └─> customer admin (same User entity, Role.CUSTOMER only)

product (Product, Category) ─> wishlist / cart / order-item / review / inventory / stats / chatbot
order (Order, OrderItem)    ─> stats (revenue, best-sellers) / chatbot (order lookup)
payment (Mona Pay Sandbox)  ─> webhook (HMAC-SHA256) / auto-pay QR / 1-click pay ──> order (PAID, CONFIRMED)
                            └─> emailService ──> gửi hóa đơn điện tử chi tiết qua Gmail
```

- **Tính nhất quán dữ liệu:** `AdminProductController` gọi chung `ProductService.getDetail()` với phía khách hàng; `StatsService` tính toán trực tiếp từ `OrderRepository`/`ProductRepository` thay vì bảng tổng hợp riêng, đảm bảo số liệu dashboard luôn đồng bộ với dữ liệu thời gian thực.
- **Hóa đơn & Thanh toán:** Mỗi địa chỉ nhận hàng (`Address`) bắt buộc có email người nhận. Khi đơn hàng được thanh toán thành công (qua Webhook Mona Pay, quét mã QR camera, hoặc nút thanh toán nhanh), hệ thống sẽ gửi ngay email hóa đơn HTML chi tiết tới Gmail của khách hàng.

---

## 🚀 Hướng Dẫn Chạy Thủ Công Website (Từ A Đến Z)

### 1. Yêu cầu môi trường (Prerequisites)
- **Java**: JDK 17 trở lên (`java -version`)
- **Maven**: 3.8 trở lên (`mvn -version` hoặc script `mvnw.cmd` có sẵn trong thư mục `backend`)
- **Node.js**: Phiên bản 18+ (`node -v` và `npm -v`)
- **MySQL Server**: Phiên bản 8.0 (`mysql --version`)
- **Ngrok** *(tùy chọn, cần khi nhận Webhook Mona Pay và quét QR trên điện thoại)*: (`ngrok version`)

---

### 2. Bước 1: Khởi động MySQL Server
Đảm bảo dịch vụ MySQL đang chạy trên máy của bạn (mặc định cổng `3306`):
- **Trên Windows (PowerShell quyền Administrator)**:
  ```powershell
  Start-Service MySQL80
  ```
  *(Hoặc mở Services (`services.msc`) -> tìm `MySQL80` -> click Start).*

---

### 3. Bước 2: Cấu hình bảo mật cục bộ (`application-local.yml`)
Dự án sử dụng cơ chế bảo mật tách biệt, thông tin nhạy cảm (mật khẩu database, token thanh toán, mail, API key) được lưu trong file `application-local.yml` và được chặn bởi `.gitignore` (không bị lộ khi đẩy lên GitHub).

1. Vào thư mục `backend/src/main/resources/`
2. Tạo file `application-local.yml` (sao chép từ file mẫu `application-local.yml.example`):
   ```yaml
   spring:
     datasource:
       password: "MatKhauMySQLCuaBan"   # Mật khẩu tài khoản root MySQL của bạn

     mail:
       username: "your-email@gmail.com"        # Địa chỉ Gmail gửi hóa đơn
       password: "xxxx xxxx xxxx xxxx"        # Mật khẩu ứng dụng Gmail (App Password 16 ký tự)

   groq:
     api-key: "gsk_your_groq_api_key_here"    # API key chatbot từ console.groq.com

   monapay:
     base-url: https://api.monapay.vn
     client-id: "YOUR_CLIENT_ID"              # Client ID từ sandbox Mona Pay
     client-secret: "YOUR_CLIENT_SECRET"      # Client Secret từ sandbox Mona Pay
     webhook-secret: "YOUR_WEBHOOK_SECRET"    # Webhook Secret để xác thực chữ ký HMAC
     sandbox: true
     expires-in: 900
     return-url: https://deafeningly-conjugated-marisha.ngrok-free.dev/orders
     cancel-url: https://deafeningly-conjugated-marisha.ngrok-free.dev/cart
   ```

> [!TIP]
> **Cách lấy Mật khẩu ứng dụng Gmail (16 ký tự):** Vào tài khoản Google -> *Bảo mật* -> *Xác minh 2 bước* -> kéo xuống dưới chọn *Mật khẩu ứng dụng (App Passwords)* -> Tạo mật khẩu mới và dán chuỗi 16 chữ cái vào cấu hình trên.

---

### 4. Bước 3: Khởi chạy Backend (Spring Boot)
Mở một cửa sổ Terminal tại thư mục gốc dự án:
```powershell
# Di chuyển vào thư mục backend
cd backend

# Chạy ứng dụng bằng Maven
mvn spring-boot:run
```
*(Nếu máy chưa cài sẵn Maven toàn cục, bạn có thể chạy `.\mvnw.cmd spring-boot:run`)*.

- Khi chạy thành công, Backend sẽ lắng nghe tại: **`http://localhost:8080`**.
- **Tự động khởi tạo DB:** Spring Boot tự động tạo cơ sở dữ liệu `bookstore` và toàn bộ các bảng JPA liên kết mà bạn không cần phải tạo thủ công.

---

### 5. Bước 4 (Tùy chọn): Nạp dữ liệu sản phẩm & danh mục mẫu
Nếu database của bạn đang trống và muốn nạp sẵn danh mục sách cùng các sản phẩm mẫu:

> [!WARNING]
> **Không dùng PowerShell pipe (`|`) để import file SQL có tiếng Việt.** PowerShell không truyền UTF-8 đúng cách sang tiến trình ngoài, khiến toàn bộ ký tự tiếng Việt bị lưu thành dấu hỏi `???` trong database. Hãy dùng lệnh `cmd /c` với `chcp 65001`:

```powershell
cmd /c "chcp 65001 >nul & `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`" --default-character-set=utf8mb4 -u root -p bookstore < backend\src\main\resources\seed.sql"
```
*(Nhập mật khẩu MySQL khi được nhắc)*.

Kiểm tra lại dữ liệu tiếng Việt:
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p -e "SELECT name FROM bookstore.products LIMIT 3;"
```

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

### 7. Bước 6 (Tùy chọn): Mở đường hầm Ngrok để quét QR & nhận Webhook
Để điện thoại có thể quét mã QR thanh toán hoặc nhận Webhook từ máy chủ Mona Pay về máy local:
```powershell
ngrok http 8080 --domain deafeningly-conjugated-marisha.ngrok-free.dev
```

> [!NOTE]
> Nếu ngrok báo lỗi `ERR_NGROK_334` (domain đã online do có tiến trình cũ đang chạy ngầm), hãy tắt tiến trình cũ bằng lệnh PowerShell:
> ```powershell
> Stop-Process -Name ngrok -Force
> ```
> rồi khởi chạy lại.

- **URL cấu hình Webhook trên trang quản trị Mona Pay:**
  `https://deafeningly-conjugated-marisha.ngrok-free.dev/api/payment/monapay/webhook`

---

## 💳 Trải Nghiệm Luồng Thanh Toán Online & Gửi Gmail Tự Động

1. **Thêm địa chỉ giao hàng:**
   - Vào mục **Sổ địa chỉ** (`/addresses`) hoặc trong màn hình **Thanh toán** (`/checkout`).
   - Nhập thông tin người nhận, số điện thoại, địa chỉ và **bắt buộc nhập Email người nhận** để hệ thống gửi hóa đơn điện tử.
2. **Đặt hàng:**
   - Trong trang Checkout, chọn hình thức: **Thanh toán online (Mona Pay)**.
   - Bấm **"Tiến hành thanh toán"** $\rightarrow$ Hệ thống tự động chuyển đến trang chi tiết hóa đơn `OrderCreated.vue`.
3. **Thực hiện thanh toán:**
   - Bấm nút **"Thanh toán ngay"** trên màn hình để mở hộp thoại mã QR.
   - Bạn có thể thực hiện thanh toán theo một trong các cách sau:
     - 📱 **Quét mã QR bằng Camera điện thoại:** Hướng camera điện thoại vào mã QR $\rightarrow$ Camera sẽ hiện liên kết web thanh toán tự động $\rightarrow$ Bấm mở liên kết $\rightarrow$ Màn hình điện thoại hiện thông báo xác nhận thành công, màn hình máy tính tự động cập nhật và Gmail của người nhận nhận ngay hóa đơn chi tiết.
     - ⚡ **Bấm nút 1-Click trên Web:** Nhấn nút **"⚡ Bấm vào đây để tự động thanh toán & Gửi Gmail luôn"** ngay bên dưới mã QR để hoàn tất thanh toán và kích hoạt gửi email ngay lập tức.
     - 🔗 **Gửi Webhook từ Mona Pay:** Thử nghiệm bắn webhook từ trang quản trị Mona Pay về endpoint `/api/payment/monapay/webhook`.

---

## 👥 Tài khoản đăng nhập hệ thống

| Loại tài khoản | Email | Mật khẩu | Ghi chú / Quyền hạn |
| :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin@bookstore.vn` | `admin123` | Toàn quyền quản trị tại `/admin` (Sản phẩm, Đơn hàng, Tồn kho, Danh mục, Khách hàng, Đánh giá, Chatbot) |
| **Khách hàng (Customer)** | Đăng ký tại `/register` | Tự đặt khi đăng ký | Mua sách, giỏ hàng, đặt hàng, sổ địa chỉ, yêu thích, đánh giá sách |

---

## 🧩 Backend Architecture (`backend/`)

### 1. The chatbot pipeline (Epic 14.1/14.2)
```
ChatbotController → ChatbotService
                       ├─ IntentDetector.detect(message)        // Phân loại ý định người dùng (keyword classifier)
                       ├─ fetch relevant entities (Product / Order / FaqEntry)
                       ├─ DataSimplifier.simplify*(...)          // Rút gọn, làm sạch PII trước khi gửi cho AI
                       ├─ GroqPromptBuilder.build(context, history)
                       └─ GroqClient.chat(messages) → api.groq.com/openai/v1/chat/completions
```

`DataSimplifier` đóng vai trò là chốt chặn an toàn: loại bỏ toàn bộ dữ liệu nhạy cảm (mật khẩu, email, số điện thoại người khác), chuẩn hóa thông tin thực thể JPA thành văn bản ngắn gọn, đảm bảo prompt gửi tới Groq luôn bảo mật và chính xác.

### 2. Payment & Email Notification Architecture
```
PaymentController
   ├─ POST /api/payments/{orderId}/create   --> Gọi MonaPayClient tạo giao dịch & lấy link thanh toán
   ├─ GET  /api/payments/{orderId}/status   --> Kiểm tra trạng thái thanh toán đơn hàng (polling)
   ├─ GET  /api/payments/{orderId}/pay-now  --> Xử lý quét QR bằng camera điện thoại, hiển thị HTML xác nhận
   ├─ POST /api/payments/{orderId}/pay-now  --> Nút 1-Click thanh toán trực tiếp từ giao diện web
   └─ POST /api/payment/monapay/webhook     --> Nhận webhook từ Mona Pay, xác thực chữ ký HMAC-SHA256
                                                 │
                                                 ▼
                                        PaymentService.processAutoPay()
                                                 │
                                                 ├─ Cập nhật Order: PAID & CONFIRMED
                                                 └─ EmailService.sendOrderInvoice()
                                                       └─ Gửi email HTML hóa đơn chi tiết qua Gmail SMTP
```

---

## 💻 Frontend Architecture (`frontend/`)

Xây dựng trên nền tảng **Vue 3 (Composition API) + Vite + Pinia + Vue Router + Axios**.

- `src/api/*Api.js`: Các module kết nối API backend tương ứng (`paymentApi.js`, `orderApi.js`, `addressApi.js`, `productApi.js`, v.v.).
- `src/stores/*.js`: Quản lý state toàn cục với Pinia: `auth`, `cart`, `wishlist`, `chatbot`.
- `src/views/Checkout.vue`: Trang thanh toán cho phép chọn COD hoặc Mona Pay online, quản lý sổ địa chỉ với email người nhận bắt buộc.
- `src/views/OrderCreated.vue`: Trang xác nhận đơn hàng sau khi tạo, hỗ trợ hiển thị mã QR thanh toán động, polling trạng thái theo thời gian thực, nút 1-click thanh toán và thông báo chúc mừng.
- `src/views/OrderDetail.vue`: Xem chi tiết toàn diện thông tin đơn hàng, danh sách sản phẩm, địa chỉ người nhận (kèm email), và trạng thái thanh toán.
- `src/components/ChatbotWidget.vue`: Trợ lý AI tư vấn sách nổi ở góc phải màn hình, hỗ trợ tìm sách, kiểm tra đơn hàng và giải đáp thắc mắc.

---

## 📌 Các tính năng mở rộng đã hoàn thiện

- ✅ **Cổng thanh toán trực tuyến Mona Pay Sandbox:** Tích hợp quy trình tạo thanh toán, mã QR thanh toán động, tự động xác nhận đơn hàng khi khách thanh toán.
- ✅ **Gửi hóa đơn điện tử tự động qua Gmail:** Sử dụng Spring Boot Starter Mail gửi email HTML thiết kế chuyên nghiệp, đầy đủ chi tiết sản phẩm, giá tiền và thông tin giao nhận ngay khi thanh toán thành công.
- ✅ **Bắt buộc Email cho sổ địa chỉ:** Bổ sung trường `email` vào thực thể `Address`, xác thực `@Email` và `@NotBlank`, đảm bảo mọi đơn hàng đều có hòm thư nhận hóa đơn.
- ✅ **Quét mã QR bằng Camera điện thoại:** Mã QR mã hóa trực tiếp đường dẫn thanh toán Web, cho phép dùng camera mặc định của điện thoại thông minh để quét và thanh toán tức thì.
- ✅ **Nút 1-Click thanh toán giả lập:** Giúp kiểm thử nhanh chóng ngay trên giao diện web mà không cần thao tác chuyển khoản thủ công.
- ✅ **Chatbot AI tư vấn sách (Groq AI):** Trợ lý hỗ trợ tìm kiếm sách, gợi ý theo sở thích, tra cứu tình trạng đơn hàng bằng ngôn ngữ tự nhiên.

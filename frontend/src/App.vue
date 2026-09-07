<template>
  <div id="layout">
    <header class="topbar">
      <div class="topbar-inner">
        <router-link to="/" class="brand">
          <span class="brand-icon">📚</span>
          <span class="brand-text">Book<span class="highlight">Store</span></span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/products" class="nav-item">Sản phẩm</router-link>
          <router-link to="/cart" class="nav-item">
            Giỏ hàng
            <span v-if="cartCount > 0" class="badge">{{ cartCount }}</span>
          </router-link>
          <router-link to="/wishlist" class="nav-item">Yêu thích</router-link>
          <router-link to="/orders" class="nav-item">Đơn hàng</router-link>
          <router-link to="/contact" class="nav-item">Liên hệ</router-link>
          <router-link v-if="auth.isAdmin" to="/admin" class="nav-item admin-badge">
            ⚙️ Quản trị
          </router-link>
        </nav>

        <div class="auth-actions">
          <template v-if="!auth.isLoggedIn">
            <router-link to="/login" class="btn btn-outline">Đăng nhập</router-link>
            <router-link to="/register" class="btn btn-primary">Đăng ký</router-link>
          </template>
          <template v-else>
            <div class="user-info">
              <span class="user-avatar">👤</span>
              <span class="user-name">{{ auth.fullName || auth.email }}</span>
              <span v-if="auth.isAdmin" class="role-pill">Admin</span>
            </div>
            <button @click="logout" class="btn btn-logout">Đăng xuất</button>
          </template>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="footer">
      <div class="footer-inner">
        <div>
          <h4>📚 BookStore Việt Nam</h4>
          <p>Hiệu sách trực tuyến cung cấp tri thức chất lượng cao cùng Trợ lý ảo AI thông minh tích hợp Groq.</p>
        </div>
        <div>
          <h4>Hỗ trợ khách hàng</h4>
          <p>Hotline: <strong>1900 6868</strong> | Email: <strong>support@bookstore.com</strong></p>
          <p>Giờ làm việc: 8:00 - 21:30 (Thứ 2 - Chủ Nhật)</p>
        </div>
        <div>
          <h4>Tài khoản mẫu</h4>
          <p>Admin: <code>admin@bookstore.com</code> / <code>admin123</code></p>
          <p>Customer: <code>customer@bookstore.com</code> / <code>customer123</code></p>
        </div>
      </div>
    </footer>

    <!-- Chatbot widget is global: Theme 14 is available from every page -->
    <ChatbotWidget />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import ChatbotWidget from './components/ChatbotWidget.vue'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const cart = useCartStore()
const router = useRouter()

const cartCount = computed(() => {
  return cart.items ? cart.items.reduce((sum, i) => sum + i.quantity, 0) : 0
})

onMounted(async () => {
  if (auth.isLoggedIn) {
    try {
      await cart.fetch()
    } catch (e) {
      // session might be expired
    }
  }
})

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style>
:root {
  --primary: #4f46e5;
  --primary-hover: #4338ca;
  --secondary: #0ea5e9;
  --bg-main: #f8fafc;
  --surface: #ffffff;
  --text-main: #0f172a;
  --text-muted: #64748b;
  --border: #e2e8f0;
  --radius: 12px;
  --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
}

* { box-sizing: border-box; }

body {
  font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
  margin: 0;
  background-color: var(--bg-main);
  color: var(--text-main);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

#layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.topbar {
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(12px);
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: white;
  font-size: 1.35rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.brand .highlight {
  color: #38bdf8;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  position: relative;
  color: #cbd5e1;
  text-decoration: none;
  font-size: 0.95rem;
  font-weight: 500;
  padding: 8px 14px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.nav-item:hover, .nav-item.router-link-active {
  color: #ffffff;
  background: rgba(255, 255, 255, 0.1);
}

.admin-badge {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24 !important;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.badge {
  display: inline-block;
  background: #ef4444;
  color: white;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 9999px;
  margin-left: 4px;
}

.auth-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: 600;
  border-radius: 8px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #4f46e5, #3b82f6);
  color: white;
}

.btn-primary:hover {
  opacity: 0.92;
  transform: translateY(-1px);
}

.btn-outline {
  background: transparent;
  color: #f1f5f9;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.1);
}

.btn-logout {
  background: rgba(239, 68, 68, 0.15);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.btn-logout:hover {
  background: rgba(239, 68, 68, 0.25);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e2e8f0;
  font-size: 0.9rem;
}

.user-name {
  font-weight: 600;
}

.role-pill {
  background: #f59e0b;
  color: #78350f;
  font-size: 0.7rem;
  font-weight: 800;
  padding: 2px 6px;
  border-radius: 4px;
  text-transform: uppercase;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 32px 24px;
}

.footer {
  background: #0f172a;
  color: #94a3b8;
  border-top: 1px solid #1e293b;
  padding: 36px 24px 24px;
  margin-top: auto;
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 28px;
  font-size: 0.9rem;
}

.footer h4 {
  color: #f8fafc;
  margin: 0 0 12px;
  font-size: 1rem;
}

.footer p {
  margin: 6px 0;
  line-height: 1.6;
}

.footer code {
  background: #1e293b;
  color: #38bdf8;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
}

@media (max-width: 768px) {
  .topbar-inner {
    flex-direction: column;
    align-items: flex-start;
  }
  .nav-links {
    flex-wrap: wrap;
  }
}
</style>

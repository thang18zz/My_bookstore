<template>
  <div id="layout" class="app-wrapper">
    <!-- Top Announcement Bar -->
    <div class="top-announcement">
      <div class="container announcement-content">
        <div class="announcement-left">
          <span class="announcement-badge">Thông Báo</span>
          <span class="announcement-text">
            🚚 <strong>Miễn phí vận chuyển</strong> toàn quốc cho mọi đơn hàng sách!
          </span>
        </div>
        <div class="announcement-right">
          <router-link to="/contact">📞 Hỗ Trợ Khách Hàng</router-link>
          <span class="divider">|</span>
          <router-link to="/products">📚 Khám Phá Kho Sách</router-link>
          <span class="divider">|</span>
          <span>🇻🇳 Tiếng Việt</span>
        </div>
      </div>
    </div>

    <!-- Main Navigation Header -->
    <header class="main-header">
      <div class="container header-container">
        <!-- Logo -->
        <div class="brand-wrapper">
          <router-link to="/products" class="brand-logo">
            <span class="logo-icon">
              <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"></path>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"></path>
                <path d="M6 6h10"></path>
                <path d="M6 10h10"></path>
              </svg>
            </span>
            <div class="logo-text">
              <span class="brand-name font-serif">BookStore</span>
              <span class="brand-slogan">Hiệu Sách Trực Tuyến</span>
            </div>
          </router-link>
          <span v-if="auth.isAdmin" class="admin-pill">QUẢN TRỊ VIÊN</span>
        </div>

        <!-- Central Search Bar -->
        <form class="search-form" @submit.prevent="handleHeaderSearch">
          <div class="search-input-wrapper">
            <input 
              v-model="headerSearchQuery"
              type="text" 
              placeholder="Tìm kiếm sách theo tên tác phẩm..." 
              class="search-input"
            />
            <button type="submit" class="search-btn" title="Tìm kiếm">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              <span>Tìm sách</span>
            </button>
          </div>
        </form>

        <!-- Header Actions: Wishlist, Cart, Account -->
        <div class="header-actions">
          <!-- Wishlist -->
          <router-link to="/wishlist" class="action-item" title="Sách yêu thích">
            <div class="icon-wrap">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              <span v-if="wishlist.items.length > 0" class="counter-badge">{{ wishlist.items.length }}</span>
            </div>
            <span class="action-label">Yêu thích</span>
          </router-link>

          <!-- Cart -->
          <router-link to="/cart" class="action-item" title="Giỏ hàng">
            <div class="icon-wrap">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="9" cy="21" r="1"></circle>
                <circle cx="20" cy="21" r="1"></circle>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
              </svg>
              <span v-if="cart.itemCount > 0" class="counter-badge">{{ cart.itemCount }}</span>
            </div>
            <div class="cart-label-wrap">
              <span class="action-label">Giỏ hàng</span>
              <span v-if="cart.total > 0" class="cart-total-mini">{{ cart.total.toLocaleString() }}đ</span>
            </div>
          </router-link>

          <!-- User Account Menu -->
          <div class="user-menu-wrapper" @mouseenter="menuOpen = true" @mouseleave="menuOpen = false">
            <template v-if="auth.isLoggedIn">
              <div class="user-trigger">
                <div class="user-avatar">
                  {{ (auth.fullName || 'U').charAt(0).toUpperCase() }}
                </div>
                <div class="user-meta">
                  <span class="greeting">Xin chào,</span>
                  <span class="user-name">{{ auth.fullName || 'Khách hàng' }}</span>
                </div>
              </div>

              <!-- Dropdown -->
              <div v-show="menuOpen" class="user-dropdown card-surface">
                <div class="dropdown-header">
                  <strong>{{ auth.fullName }}</strong>
                  <small>{{ auth.email }}</small>
                </div>
                <router-link to="/profile" class="dropdown-link" @click="menuOpen = false">
                  👤 Thông tin cá nhân
                </router-link>
                <router-link to="/orders" class="dropdown-link" @click="menuOpen = false">
                  📦 Đơn hàng của tôi
                </router-link>
                <router-link to="/addresses" class="dropdown-link" @click="menuOpen = false">
                  📍 Sổ địa chỉ nhận hàng
                </router-link>
                <router-link v-if="auth.isAdmin" to="/admin" class="dropdown-link admin-link" @click="menuOpen = false">
                  📊 Bảng điều khiển Quản trị
                </router-link>
                <div class="dropdown-divider"></div>
                <a href="#" class="dropdown-link logout-link" @click.prevent="logout">
                  🚪 Đăng xuất
                </a>
              </div>
            </template>

            <template v-else>
              <router-link to="/login" class="btn btn-sm btn-primary login-btn">
                <span>Đăng nhập</span>
              </router-link>
            </template>
          </div>
        </div>
      </div>
    </header>

    <!-- Sub-Navbar (Categories Navigation) -->
    <nav class="sub-navbar">
      <div class="container subnav-container">
        <ul class="category-links">
          <li><router-link to="/products">📚 Tất cả sách</router-link></li>
          <li><router-link to="/products?category=1">📖 Văn học</router-link></li>
          <li><router-link to="/products?category=2">💡 Kinh tế</router-link></li>
          <li><router-link to="/products?category=3">🧠 Tâm lý học</router-link></li>
          <li><router-link to="/products?category=4">🚀 Khoa học</router-link></li>
        </ul>
      </div>
    </nav>

    <!-- Admin Sub-Navigation Menu (when logged in as ADMIN) -->
    <nav v-if="auth.isAdmin" class="admin-nav">
      <div class="container admin-nav-container">
        <span class="admin-label">⚡ QUẢN TRỊ:</span>
        <router-link to="/admin">📊 Dashboard</router-link>
        <router-link to="/admin/products">📦 Sản phẩm</router-link>
        <router-link to="/admin/categories">📂 Danh mục</router-link>
        <router-link to="/admin/orders">📑 Đơn hàng</router-link>
        <router-link to="/admin/inventory">🏬 Kho</router-link>
        <router-link to="/admin/customers">👥 Khách hàng</router-link>
        <router-link to="/admin/reviews">⭐ Đánh giá</router-link>
        <router-link to="/admin/chatbot">🤖 Chatbot AI</router-link>
      </div>
    </nav>

    <!-- Main View Content -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- BookStore Footer -->
    <footer class="main-footer">
      <!-- Newsletter Subscription Banner -->
      <div class="newsletter-bar">
        <div class="container newsletter-content">
          <div class="newsletter-info">
            <h3 class="font-serif">Đăng ký nhận bản tin sách mới</h3>
            <p>Cập nhật những cuốn sách chọn lọc mới nhất và thông tin hữu ích từ BookStore.</p>
          </div>
          <form class="newsletter-form" @submit.prevent="subscribeNewsletter">
            <input v-model="newsletterEmail" type="email" placeholder="Nhập địa chỉ email của bạn..." required />
            <button type="submit" class="btn btn-accent">Đăng ký</button>
          </form>
        </div>
      </div>

      <!-- Footer Columns -->
      <div class="footer-links-wrapper">
        <div class="container footer-grid">
          <!-- Col 1: About BookStore -->
          <div class="footer-col">
            <div class="footer-brand font-serif">BookStore</div>
            <p class="footer-desc">
              Hệ thống hiệu sách trực tuyến cung cấp các tựa sách chọn lọc chất lượng cao và giao hàng nhanh chóng trên toàn quốc.
            </p>
            <div class="brand-commitments">
              <div class="commit-item">✓ 100% Sách mới chính hãng</div>
              <div class="commit-item">✓ Đóng gói cẩn thận</div>
              <div class="commit-item">✓ Giao hàng toàn quốc</div>
            </div>
          </div>

          <!-- Col 2: Categories -->
          <div class="footer-col">
            <h4>Khám phá sách</h4>
            <ul>
              <li><router-link to="/products?category=1">Văn học</router-link></li>
              <li><router-link to="/products?category=2">Kinh tế</router-link></li>
              <li><router-link to="/products?category=3">Tâm lý học</router-link></li>
              <li><router-link to="/products?category=4">Khoa học</router-link></li>
            </ul>
          </div>

          <!-- Col 3: Customer Service -->
          <div class="footer-col">
            <h4>Hỗ trợ khách hàng</h4>
            <ul>
              <li><router-link to="/orders">Tra cứu đơn hàng</router-link></li>
              <li><router-link to="/addresses">Sổ địa chỉ nhận hàng</router-link></li>
              <li><router-link to="/contact">Thông tin liên hệ</router-link></li>
            </ul>
          </div>

          <!-- Col 4: Contact Info -->
          <div class="footer-col">
            <h4>Liên hệ</h4>
            <div class="contact-info">
              <p>📍 123 Book Street, Hanoi</p>
              <p>📞 Hotline: 1900-1234</p>
              <p>✉️ Email: support@bookstore.vn</p>
            </div>
            <div class="payment-badges">
              <span class="pay-chip">Thanh toán khi nhận hàng (COD)</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Copyright & Bottom Bar -->
      <div class="footer-bottom">
        <div class="container footer-bottom-inner">
          <p>© 2026 BookStore. Tất cả các quyền được bảo lưu.</p>
          <div class="bottom-links">
            <router-link to="/contact">Liên hệ</router-link>
            <router-link to="/products">Cửa hàng</router-link>
          </div>
        </div>
      </div>
    </footer>

    <!-- Chatbot widget (only when logged in) -->
    <ChatbotWidget v-if="auth.isLoggedIn" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import { useWishlistStore } from './stores/wishlist'
import ChatbotWidget from './components/ChatbotWidget.vue'

const auth = useAuthStore()
const cart = useCartStore()
const wishlist = useWishlistStore()
const router = useRouter()

const headerSearchQuery = ref('')
const menuOpen = ref(false)
const newsletterEmail = ref('')

onMounted(async () => {
  if (auth.isLoggedIn) {
    try {
      await cart.fetch()
      await wishlist.fetch()
    } catch (e) {
      console.warn('Could not prefetch cart/wishlist:', e)
    }
  }
})

function handleHeaderSearch() {
  if (headerSearchQuery.value.trim()) {
    router.push({ path: '/products', query: { q: headerSearchQuery.value.trim() } })
  } else {
    router.push('/products')
  }
}

function logout() {
  auth.logout()
  menuOpen.value = false
  router.push('/login')
}

function subscribeNewsletter() {
  alert(`Cảm ơn bạn đã đăng ký nhận bản tin với email: ${newsletterEmail.value}`)
  newsletterEmail.value = ''
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* --- Top Announcement Bar --- */
.top-announcement {
  background-color: var(--tb-teal-800);
  color: #ffffff;
  font-size: 0.82rem;
  padding: 6px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.announcement-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.announcement-badge {
  background-color: var(--tb-teal-500);
  color: #ffffff;
  font-weight: 700;
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: var(--tb-radius-sm);
  text-transform: uppercase;
}

.announcement-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.announcement-right a {
  color: #e2e8f0;
  font-size: 0.8rem;
}

.announcement-right a:hover {
  color: #ffffff;
  text-decoration: underline;
}

.divider {
  color: rgba(255, 255, 255, 0.3);
}

/* --- Main Navigation Header --- */
.main-header {
  background-color: #ffffff;
  border-bottom: 1px solid var(--tb-border);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding-top: 14px;
  padding-bottom: 14px;
}

/* Brand Logo */
.brand-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.logo-icon {
  width: 42px;
  height: 42px;
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-600);
  border-radius: var(--tb-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--tb-teal-200);
  transition: transform var(--tb-transition-fast);
}

.brand-logo:hover .logo-icon {
  transform: rotate(-4deg) scale(1.05);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.brand-name {
  font-size: 1.55rem;
  font-weight: 900;
  color: var(--tb-teal-700);
  letter-spacing: -0.5px;
  line-height: 1.1;
}

.brand-slogan {
  font-size: 0.68rem;
  color: var(--tb-slate-500);
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.admin-pill {
  background-color: #e11d48;
  color: white;
  font-size: 0.65rem;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: var(--tb-radius-full);
  margin-left: 4px;
}

/* Central Search Bar */
.search-form {
  flex: 1;
  max-width: 580px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  border: 2px solid var(--tb-teal-600);
  border-radius: var(--tb-radius-full);
  overflow: hidden;
  background-color: #ffffff;
  transition: box-shadow var(--tb-transition-fast);
}

.search-input-wrapper:focus-within {
  box-shadow: 0 0 0 4px rgba(0, 133, 117, 0.15);
}

.search-input {
  flex: 1;
  border: none !important;
  box-shadow: none !important;
  padding: 10px 18px !important;
  font-size: 0.92rem;
  outline: none;
  background: transparent;
}

.search-btn {
  background-color: var(--tb-teal-600);
  color: #ffffff;
  border: none;
  padding: 10px 20px;
  font-weight: 600;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: background-color var(--tb-transition-fast);
}

.search-btn:hover {
  background-color: var(--tb-teal-700);
}

/* Header Action Items */
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--tb-slate-700);
  text-decoration: none;
  padding: 6px 10px;
  border-radius: var(--tb-radius-md);
  transition: background-color var(--tb-transition-fast), color var(--tb-transition-fast);
}

.action-item:hover {
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-700);
}

.icon-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.counter-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background-color: var(--tb-amber-500);
  color: #ffffff;
  font-size: 0.7rem;
  font-weight: 700;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.15);
}

.action-label {
  font-size: 0.88rem;
  font-weight: 600;
}

.cart-label-wrap {
  display: flex;
  flex-direction: column;
}

.cart-total-mini {
  font-size: 0.76rem;
  font-weight: 700;
  color: var(--tb-teal-700);
}

/* User Account Menu */
.user-menu-wrapper {
  position: relative;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--tb-radius-md);
  transition: background-color var(--tb-transition-fast);
}

.user-trigger:hover {
  background-color: var(--tb-slate-100);
}

.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background-color: var(--tb-teal-600);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.greeting {
  font-size: 0.72rem;
  color: var(--tb-slate-400);
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--tb-slate-800);
  max-width: 110px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.login-btn {
  border-radius: var(--tb-radius-full);
}

/* Dropdown */
.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  width: 220px;
  padding: 8px 0;
  margin-top: 6px;
  z-index: 1000;
}

.dropdown-header {
  padding: 10px 16px;
  border-bottom: 1px solid var(--tb-border);
  display: flex;
  flex-direction: column;
}

.dropdown-header strong {
  font-size: 0.9rem;
  color: var(--tb-slate-900);
}

.dropdown-header small {
  color: var(--tb-slate-500);
  font-size: 0.78rem;
  word-break: break-all;
}

.dropdown-link {
  display: block;
  padding: 8px 16px;
  color: var(--tb-slate-700);
  font-size: 0.88rem;
  font-weight: 500;
  text-decoration: none;
  transition: background-color var(--tb-transition-fast);
}

.dropdown-link:hover {
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-700);
}

.dropdown-divider {
  height: 1px;
  background-color: var(--tb-border);
  margin: 6px 0;
}

.logout-link {
  color: var(--tb-red-600);
}

.logout-link:hover {
  background-color: var(--tb-red-50);
  color: var(--tb-red-600);
}

.admin-link {
  color: #2563eb;
  font-weight: 600;
}

/* --- Sub-Navbar (Categories) --- */
.sub-navbar {
  background-color: #ffffff;
  border-bottom: 1px solid var(--tb-border);
}

.subnav-container {
  overflow-x: auto;
}

.category-links {
  list-style: none;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 10px 0;
  white-space: nowrap;
}

.category-links a {
  color: var(--tb-slate-700);
  font-size: 0.9rem;
  font-weight: 600;
  text-decoration: none;
  transition: color var(--tb-transition-fast);
  padding: 4px 0;
}

.category-links a:hover,
.category-links a.router-link-active {
  color: var(--tb-teal-600);
  border-bottom: 2px solid var(--tb-teal-600);
}

/* --- Admin Navigation --- */
.admin-nav {
  background-color: var(--tb-slate-900);
  border-top: 1px solid #374151;
  padding: 8px 0;
}

.admin-nav-container {
  display: flex;
  align-items: center;
  gap: 10px;
  overflow-x: auto;
}

.admin-label {
  color: #9ca3af;
  font-size: 0.75rem;
  font-weight: 800;
  margin-right: 6px;
  white-space: nowrap;
}

.admin-nav a {
  color: #cbd5e1;
  font-size: 0.82rem;
  padding: 5px 12px;
  border-radius: var(--tb-radius-sm);
  background-color: var(--tb-slate-800);
  text-decoration: none;
  white-space: nowrap;
  transition: all var(--tb-transition-fast);
}

.admin-nav a:hover,
.admin-nav a.router-link-active {
  background-color: var(--tb-teal-600);
  color: #ffffff;
}

/* --- Main Content --- */
.main-content {
  flex: 1;
  padding: 28px 0 60px 0;
}

/* --- Footer --- */
.main-footer {
  background-color: var(--tb-slate-900);
  color: #cbd5e1;
  margin-top: auto;
}

/* Newsletter bar */
.newsletter-bar {
  background-color: var(--tb-teal-800);
  color: #ffffff;
  padding: 32px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.newsletter-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  flex-wrap: wrap;
}

.newsletter-info h3 {
  color: #ffffff;
  font-size: 1.4rem;
  margin-bottom: 6px;
}

.newsletter-info p {
  color: var(--tb-teal-100);
  font-size: 0.92rem;
}

.newsletter-form {
  display: flex;
  gap: 10px;
  min-width: 380px;
}

.newsletter-form input {
  background-color: #ffffff;
  border: none;
  border-radius: var(--tb-radius-md);
  padding: 12px 16px;
}

/* Footer Grid */
.footer-links-wrapper {
  padding: 50px 0 40px 0;
}

.footer-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1.3fr;
  gap: 36px;
}

.footer-col h4 {
  color: #ffffff;
  font-size: 1.05rem;
  margin-bottom: 18px;
  font-weight: 700;
  position: relative;
  padding-bottom: 8px;
}

.footer-col h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 32px;
  height: 2px;
  background-color: var(--tb-teal-500);
}

.footer-brand {
  font-size: 1.6rem;
  color: #ffffff;
  margin-bottom: 12px;
  font-weight: 700;
}

.footer-desc {
  font-size: 0.88rem;
  line-height: 1.6;
  color: #94a3b8;
  margin-bottom: 16px;
}

.brand-commitments {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.82rem;
  color: var(--tb-teal-200);
}

.footer-col ul {
  list-style: none;
}

.footer-col ul li {
  margin-bottom: 10px;
}

.footer-col ul li a {
  color: #94a3b8;
  font-size: 0.88rem;
  transition: color var(--tb-transition-fast);
}

.footer-col ul li a:hover {
  color: #ffffff;
  padding-left: 4px;
}

.contact-info p {
  font-size: 0.88rem;
  margin-bottom: 8px;
  color: #94a3b8;
}

.payment-badges {
  display: flex;
  gap: 8px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.pay-chip {
  background-color: #1e293b;
  border: 1px solid #334155;
  color: #e2e8f0;
  font-size: 0.75rem;
  padding: 4px 10px;
  border-radius: var(--tb-radius-sm);
}

/* Footer Bottom */
.footer-bottom {
  border-top: 1px solid #1e293b;
  padding: 20px 0;
  font-size: 0.82rem;
  color: #64748b;
}

.footer-bottom-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.bottom-links {
  display: flex;
  gap: 16px;
}

.bottom-links a {
  color: #64748b;
  font-size: 0.82rem;
}

.bottom-links a:hover {
  color: #cbd5e1;
}

/* Responsive adjustments */
@media (max-width: 960px) {
  .header-container {
    flex-wrap: wrap;
  }
  .search-form {
    order: 3;
    max-width: 100%;
    width: 100%;
  }
  .footer-grid {
    grid-template-columns: 1fr 1fr;
  }
  .newsletter-form {
    min-width: 100%;
  }
}

@media (max-width: 640px) {
  .announcement-right {
    display: none;
  }
  .footer-grid {
    grid-template-columns: 1fr;
  }
  .action-label, .cart-label-wrap {
    display: none;
  }
}
</style>

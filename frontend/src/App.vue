<template>
  <div id="layout">
    <header class="topbar">
      <div class="brand-group">
        <router-link to="/" class="brand">📚 Book Store</router-link>
        <span v-if="auth.isAdmin" class="admin-badge">QUẢN TRỊ VIÊN</span>
      </div>
      <nav>
        <router-link to="/products">Cửa hàng</router-link>
        <router-link to="/cart">Giỏ hàng</router-link>
        <router-link to="/wishlist">Yêu thích</router-link>
        <router-link to="/orders">Đơn hàng</router-link>
        <router-link v-if="auth.isLoggedIn" to="/addresses">Sổ địa chỉ</router-link>
        <router-link v-if="!auth.isLoggedIn" to="/login">Đăng nhập</router-link>
        <a v-else href="#" @click.prevent="logout">Đăng xuất ({{ auth.fullName }})</a>
      </nav>
    </header>

    <!-- Admin sub-navigation menu when logged in as ADMIN -->
    <nav v-if="auth.isAdmin" class="admin-nav">
      <router-link to="/admin">📊 Dashboard</router-link>
      <router-link to="/admin/products">📦 Quản lý Sản phẩm</router-link>
      <router-link to="/admin/categories">📂 Quản lý Danh mục</router-link>
      <router-link to="/admin/orders">📑 Quản lý Đơn hàng</router-link>
      <router-link to="/admin/inventory">🏬 Quản lý Kho</router-link>
      <router-link to="/admin/customers">👥 Khách hàng</router-link>
      <router-link to="/admin/reviews">⭐ Đánh giá</router-link>
      <router-link to="/admin/chatbot">🤖 Chatbot AI</router-link>
    </nav>

    <main>
      <router-view />
    </main>

    <!-- Chatbot widget requires login (Epic 14.1 is customer-only, see SecurityConfig) -->
    <ChatbotWidget v-if="auth.isLoggedIn" />
  </div>
</template>

<script setup>
import { useAuthStore } from './stores/auth'
import ChatbotWidget from './components/ChatbotWidget.vue'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style>
body { font-family: system-ui, -apple-system, sans-serif; margin: 0; background: #f9fafb; color: #1f2937; }
.topbar { display: flex; justify-content: space-between; align-items: center; padding: 12px 24px; background: #1f2937; color: white; }
.brand-group { display: flex; align-items: center; gap: 12px; }
.brand { font-weight: bold; font-size: 1.2rem; color: white; text-decoration: none; }
.admin-badge { background: #e11d48; color: white; font-size: 0.7rem; font-weight: bold; padding: 2px 8px; border-radius: 9999px; letter-spacing: 0.5px; }
.topbar a { color: white; margin-left: 16px; text-decoration: none; font-size: 0.95rem; }
.topbar a:hover { color: #60a5fa; }
.admin-nav { display: flex; gap: 8px; overflow-x: auto; padding: 10px 24px; background: #111827; border-top: 1px solid #374151; }
.admin-nav a { color: #d1d5db; text-decoration: none; font-size: 0.85rem; padding: 6px 12px; border-radius: 6px; background: #1f2937; transition: background 0.15s, color 0.15s; white-space: nowrap; }
.admin-nav a:hover, .admin-nav a.router-link-active { background: #2563eb; color: white; font-weight: 500; }
main { padding: 24px; max-width: 1150px; margin: 0 auto; }
</style>

<template>
  <div class="auth-card-wrapper">
    <div class="auth-card">
      <div class="auth-header">
        <span class="auth-icon">🔐</span>
        <h2>Đăng nhập tài khoản</h2>
        <p>Truy cập để quản lý đơn hàng, giỏ hàng và danh sách yêu thích</p>
      </div>

      <form @submit.prevent="submit" class="auth-form">
        <div class="form-group">
          <label>Email</label>
          <input v-model="email" type="email" placeholder="example@email.com" required />
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <input v-model="password" type="password" placeholder="••••••••" required />
        </div>

        <p v-if="error" class="error-msg">{{ error }}</p>

        <button type="submit" class="btn btn-submit" :disabled="loading">
          {{ loading ? 'Đang xử lý...' : 'Đăng nhập' }}
        </button>
      </form>

      <div class="demo-box">
        <div class="demo-title">⚡ Đăng nhập nhanh tài khoản mẫu:</div>
        <div class="demo-btns">
          <button type="button" class="btn-demo admin" @click="fillCredentials('admin@bookstore.com', 'admin123')">
            👨‍💼 Admin Demo
          </button>
          <button type="button" class="btn-demo customer" @click="fillCredentials('customer@bookstore.com', 'customer123')">
            👤 Khách hàng Demo
          </button>
        </div>
      </div>

      <div class="auth-footer">
        <span>Chưa có tài khoản?</span>
        <router-link to="/register">Đăng ký ngay</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()

function fillCredentials(e, p) {
  email.value = e
  password.value = p
  submit()
}

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.login({ email: email.value, password: password.value })
    router.push(auth.isAdmin ? '/admin' : '/products')
  } catch (e) {
    error.value = e.response?.data?.message || e.message || 'Đăng nhập thất bại'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-card-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 16px;
}

.auth-card {
  background: white;
  width: 100%;
  max-width: 440px;
  padding: 36px 32px;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border: 1px solid var(--border);
}

.auth-header {
  text-align: center;
  margin-bottom: 28px;
}

.auth-icon {
  font-size: 2.2rem;
  display: inline-block;
  margin-bottom: 8px;
}

.auth-header h2 {
  margin: 0 0 8px;
  font-size: 1.5rem;
  font-weight: 800;
  color: #0f172a;
}

.auth-header p {
  margin: 0;
  color: #64748b;
  font-size: 0.9rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.form-group input {
  padding: 12px 16px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-size: 0.95rem;
  outline: none;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.15);
}

.error-msg {
  color: #ef4444;
  background: #fef2f2;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.85rem;
  margin: 0;
  border: 1px solid #fecaca;
}

.btn-submit {
  background: linear-gradient(135deg, #4f46e5, #4338ca);
  color: white;
  padding: 12px;
  border-radius: 10px;
  font-weight: 700;
  font-size: 1rem;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
  margin-top: 4px;
}

.btn-submit:hover:not(:disabled) {
  opacity: 0.95;
  transform: translateY(-1px);
}

.demo-box {
  margin-top: 24px;
  padding: 16px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
}

.demo-title {
  font-size: 0.8rem;
  font-weight: 700;
  color: #475569;
  margin-bottom: 10px;
}

.demo-btns {
  display: flex;
  gap: 8px;
}

.btn-demo {
  flex: 1;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  border: 1px solid;
  transition: all 0.2s;
}

.btn-demo.admin {
  background: #fffbeb;
  color: #b45309;
  border-color: #fde68a;
}

.btn-demo.admin:hover {
  background: #fef3c7;
}

.btn-demo.customer {
  background: #eff6ff;
  color: #1d4ed8;
  border-color: #bfdbfe;
}

.btn-demo.customer:hover {
  background: #dbeafe;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 0.9rem;
  color: #64748b;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.auth-footer a {
  color: #4f46e5;
  font-weight: 600;
  text-decoration: none;
}

.auth-footer a:hover {
  text-decoration: underline;
}
</style>

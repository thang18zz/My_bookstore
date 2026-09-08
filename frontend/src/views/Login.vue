<template>
  <div class="auth-page container">
    <div class="auth-card card-surface">
      <!-- Left side: Form -->
      <div class="auth-form-side">
        <div class="auth-header">
          <h1 class="font-serif auth-title">Đăng Nhập</h1>
          <p class="auth-subtitle">Chào mừng bạn quay trở lại với BookStore</p>
        </div>

        <form class="auth-form" @submit.prevent="submit">
          <div class="form-group">
            <label for="login-email">Địa chỉ Email</label>
            <input 
              id="login-email"
              v-model="email" 
              type="email" 
              placeholder="tenban@example.com" 
              required 
            />
          </div>

          <div class="form-group">
            <label for="login-password">Mật khẩu</label>
            <input 
              id="login-password"
              v-model="password" 
              type="password" 
              placeholder="Nhập mật khẩu..." 
              required 
            />
          </div>

          <div v-if="error" class="error-alert">
            <span class="err-icon">⚠️</span>
            <span>{{ error }}</span>
          </div>

          <button type="submit" class="btn btn-primary btn-lg btn-auth-submit" :disabled="loading">
            {{ loading ? 'Đang xác thực...' : 'Đăng nhập vào tài khoản' }}
          </button>
        </form>

        <div class="auth-footer-prompt">
          <span>Chưa có tài khoản BookStore?</span>
          <router-link to="/register" class="register-link">Đăng ký ngay &rarr;</router-link>
        </div>
      </div>

      <!-- Right side: Features Showcase -->
      <div class="auth-perk-side">
        <div class="perk-side-content">
          <span class="perk-tag">Tài Khoản BookStore</span>
          <h2 class="font-serif perk-heading">Trải Nghiệm Mua Sắm Sách Thuận Tiện</h2>
          <p class="perk-desc">
            Đăng nhập tài khoản để quản lý đơn hàng và trải nghiệm các tính năng dành cho bạn:
          </p>

          <ul class="perk-bullet-list">
            <li>
              <span class="bullet-icon">📦</span>
              <div>
                <strong>Theo Dõi Đơn Hàng Dễ Dàng</strong>
                <p>Cập nhật trạng thái xử lý và vận chuyển đơn hàng trực quan</p>
              </div>
            </li>
            <li>
              <span class="bullet-icon">❤️</span>
              <div>
                <strong>Lưu Sách Yêu Thích</strong>
                <p>Tạo danh sách những tựa sách bạn muốn đọc và mua sắm sau</p>
              </div>
            </li>
            <li>
              <span class="bullet-icon">🤖</span>
              <div>
                <strong>Trợ Lý Ảo Chatbot AI</strong>
                <p>Tư vấn gợi ý sách và giải đáp câu hỏi về kho sách nhanh chóng</p>
              </div>
            </li>
          </ul>
        </div>
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

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.login({ email: email.value, password: password.value })
    router.push(auth.isAdmin ? '/admin' : '/products')
  } catch (e) {
    error.value = e.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại email hoặc mật khẩu.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 350px);
  padding: 20px 0;
}

.auth-card {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  max-width: 880px;
  width: 100%;
  background-color: #ffffff;
  overflow: hidden;
  border-radius: var(--tb-radius-xl);
  box-shadow: var(--tb-shadow-xl);
}

.auth-form-side {
  padding: 44px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-header {
  margin-bottom: 26px;
}

.auth-title {
  font-size: 1.8rem;
  color: var(--tb-slate-900);
  margin-bottom: 6px;
}

.auth-subtitle {
  color: var(--tb-slate-500);
  font-size: 0.9rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--tb-slate-700);
}

.error-alert {
  background-color: var(--tb-red-50);
  border: 1px solid var(--tb-red-100);
  color: var(--tb-red-600);
  padding: 10px 14px;
  border-radius: var(--tb-radius-md);
  font-size: 0.85rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-auth-submit {
  width: 100%;
  margin-top: 8px;
}

.auth-footer-prompt {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--tb-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.88rem;
  color: var(--tb-slate-600);
}

.register-link {
  font-weight: 700;
  color: var(--tb-teal-700);
}

/* Right Side: Perks */
.auth-perk-side {
  background: linear-gradient(145deg, #004d43 0%, #008575 100%);
  color: #ffffff;
  padding: 44px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.perk-tag {
  background-color: var(--tb-teal-500);
  color: #ffffff;
  font-size: 0.72rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 4px 10px;
  border-radius: var(--tb-radius-full);
  display: inline-block;
  margin-bottom: 12px;
}

.perk-heading {
  color: #ffffff;
  font-size: 1.55rem;
  margin-bottom: 12px;
  line-height: 1.3;
}

.perk-desc {
  color: var(--tb-teal-100);
  font-size: 0.92rem;
  line-height: 1.6;
  margin-bottom: 24px;
}

.perk-bullet-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.perk-bullet-list li {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.bullet-icon {
  font-size: 1.3rem;
  line-height: 1;
}

.perk-bullet-list strong {
  font-size: 0.92rem;
  display: block;
  color: #ffffff;
}

.perk-bullet-list p {
  font-size: 0.8rem;
  color: var(--tb-teal-200);
  margin: 2px 0 0 0;
}

@media (max-width: 768px) {
  .auth-card {
    grid-template-columns: 1fr;
  }
  .auth-perk-side {
    display: none;
  }
  .auth-form-side {
    padding: 30px 20px;
  }
}
</style>

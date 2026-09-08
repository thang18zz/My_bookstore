<template>
  <div class="auth-page container">
    <div class="auth-card card-surface">
      <!-- Left side: Form -->
      <div class="auth-form-side">
        <div class="auth-header">
          <h1 class="font-serif auth-title">Tạo Tài Khoản Mới</h1>
          <p class="auth-subtitle">Gia nhập BookStore để trải nghiệm mua sắm sách dễ dàng</p>
        </div>

        <form class="auth-form" @submit.prevent="submit">
          <div class="form-group">
            <label for="reg-name">Họ và tên của bạn</label>
            <input 
              id="reg-name"
              v-model="form.fullName" 
              type="text" 
              placeholder="Nguyễn Văn A" 
              required 
            />
          </div>

          <div class="form-group">
            <label for="reg-email">Địa chỉ Email</label>
            <input 
              id="reg-email"
              v-model="form.email" 
              type="email" 
              placeholder="email@example.com" 
              required 
            />
          </div>

          <div class="form-group">
            <label for="reg-password">Mật khẩu</label>
            <input 
              id="reg-password"
              v-model="form.password" 
              type="password" 
              placeholder="Tối thiểu 6 ký tự..." 
              required 
            />
          </div>

          <div class="form-group">
            <label for="reg-phone">Số điện thoại (tùy chọn)</label>
            <input 
              id="reg-phone"
              v-model="form.phone" 
              type="tel" 
              placeholder="0912 345 678" 
            />
          </div>

          <div v-if="error" class="error-alert">
            <span class="err-icon">⚠️</span>
            <span>{{ error }}</span>
          </div>

          <button type="submit" class="btn btn-primary btn-lg btn-auth-submit" :disabled="loading">
            {{ loading ? 'Đang tạo tài khoản...' : 'Hoàn tất đăng ký' }}
          </button>
        </form>

        <div class="auth-footer-prompt">
          <span>Bạn đã có tài khoản?</span>
          <router-link to="/login" class="register-link">Đăng nhập &rarr;</router-link>
        </div>
      </div>

      <!-- Right side: Features Showcase -->
      <div class="auth-perk-side">
        <div class="perk-side-content">
          <span class="perk-tag">Thành Viên BookStore</span>
          <h2 class="font-serif perk-heading">Mua Sắm Sách Thuận Tiện Tại BookStore</h2>
          <p class="perk-desc">
            Chỉ mất vài thao tác để tạo tài khoản và quản lý đơn hàng của bạn:
          </p>

          <ul class="perk-bullet-list">
            <li>
              <span class="bullet-icon">📦</span>
              <div>
                <strong>Quản Lý Đơn Hàng</strong>
                <p>Theo dõi lịch sử mua sách và trạng thái vận chuyển chi tiết</p>
              </div>
            </li>
            <li>
              <span class="bullet-icon">📍</span>
              <div>
                <strong>Sổ Địa Chỉ Giao Nhận</strong>
                <p>Lưu sẵn nhiều địa chỉ để thanh toán và nhận hàng nhanh chóng</p>
              </div>
            </li>
            <li>
              <span class="bullet-icon">🤖</span>
              <div>
                <strong>Trợ Lý Chatbot AI Riêng</strong>
                <p>Tư vấn gợi ý sách và giải đáp thông tin liên quan</p>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const form = reactive({ fullName: '', email: '', password: '', phone: '' })
const error = ref('')
const loading = ref(false)
const auth = useAuthStore()
const router = useRouter()

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await auth.register(form)
    router.push('/products')
  } catch (e) {
    error.value = e.message || 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.'
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
  margin-bottom: 22px;
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
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
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

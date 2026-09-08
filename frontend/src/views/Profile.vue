<template>
  <div class="profile-page container">
    <div class="page-title-row">
      <h1 class="font-serif page-title">Tài Khoản Của Bạn</h1>
      <p class="page-sub">Quản lý thông tin cá nhân và tài khoản BookStore</p>
    </div>

    <div class="profile-layout">
      <!-- Left Column: User Summary Card -->
      <div class="profile-sidebar">
        <div class="user-id-card card-surface">
          <div class="large-avatar">
            {{ (profile.fullName || 'U').charAt(0).toUpperCase() }}
          </div>
          <h3 class="user-display-name font-serif">{{ profile.fullName || 'Độc giả BookStore' }}</h3>
          <span class="user-email-tag">{{ profile.email }}</span>

          <div class="quick-nav-links">
            <router-link to="/orders" class="q-link">📦 Đơn hàng của tôi &rarr;</router-link>
            <router-link to="/addresses" class="q-link">📍 Sổ địa chỉ giao nhận &rarr;</router-link>
            <router-link to="/wishlist" class="q-link">❤️ Sách yêu thích &rarr;</router-link>
          </div>
        </div>
      </div>

      <!-- Right Column: Profile Edit Form -->
      <div class="profile-main">
        <div class="form-card card-surface">
          <h3 class="font-serif card-heading">Cập Nhật Thông Tin Cá Nhân</h3>

          <form class="profile-form" @submit.prevent="save">
            <div class="form-group">
              <label>Địa chỉ Email (Định danh tài khoản)</label>
              <div class="email-disabled-wrap">
                <input :value="profile.email" disabled class="disabled-input" />
                <span class="verified-check">✓ Đã kích hoạt</span>
              </div>
              <small class="field-hint">Email không thể thay đổi vì được liên kết với tài khoản của bạn.</small>
            </div>

            <div class="form-group">
              <label for="prof-name">Họ và tên</label>
              <input id="prof-name" v-model="profile.fullName" placeholder="Nhập họ và tên đầy đủ..." required />
            </div>

            <div class="form-group">
              <label for="prof-phone">Số điện thoại liên lạc</label>
              <input id="prof-phone" v-model="profile.phone" placeholder="0912 345 678" />
              <small class="field-hint">Dùng để shipper liên hệ khi giao sách đến địa chỉ của bạn.</small>
            </div>

            <div v-if="saved" class="save-success">
              <span>✓ Thông tin cá nhân đã được lưu thành công!</span>
            </div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary btn-lg" :disabled="saving">
                {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import authApi from '../api/authApi'

const profile = reactive({ email: '', fullName: '', phone: '' })
const saved = ref(false)
const saving = ref(false)

onMounted(async () => {
  try {
    const res = await authApi.me()
    Object.assign(profile, res.data)
  } catch (e) {
    console.error('Error fetching profile:', e)
  }
})

async function save() {
  saving.value = true
  saved.value = false
  try {
    const res = await authApi.updateMe({ fullName: profile.fullName, phone: profile.phone })
    Object.assign(profile, res.data)
    saved.value = true
    setTimeout(() => { saved.value = false }, 4000)
  } catch (e) {
    alert('Không thể lưu thông tin: ' + (e.response?.data?.message || e.message))
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-title {
  font-size: 1.8rem;
  color: var(--tb-slate-900);
  margin-bottom: 4px;
}

.page-sub {
  color: var(--tb-slate-500);
  font-size: 0.92rem;
}

.profile-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 28px;
  align-items: start;
}

.user-id-card {
  padding: 30px 24px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.large-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background-color: var(--tb-teal-700);
  color: #ffffff;
  font-size: 2rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  box-shadow: 0 4px 10px rgba(0, 133, 117, 0.25);
}

.user-display-name {
  font-size: 1.25rem;
  color: var(--tb-slate-900);
  margin-bottom: 4px;
}

.user-email-tag {
  font-size: 0.85rem;
  color: var(--tb-slate-500);
  margin-bottom: 24px;
}

.quick-nav-links {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-top: 1px solid var(--tb-border);
  padding-top: 18px;
  text-align: left;
}

.q-link {
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--tb-slate-700);
  padding: 6px 10px;
  border-radius: var(--tb-radius-sm);
  transition: background-color var(--tb-transition-fast);
}

.q-link:hover {
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-700);
}

/* Right Main Form */
.form-card {
  padding: 32px;
  background-color: #ffffff;
}

.card-heading {
  font-size: 1.3rem;
  color: var(--tb-slate-900);
  margin-bottom: 22px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--tb-teal-100);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 580px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.88rem;
  font-weight: 700;
  color: var(--tb-slate-700);
}

.email-disabled-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.disabled-input {
  background-color: var(--tb-slate-50) !important;
  color: var(--tb-slate-600) !important;
  cursor: not-allowed;
}

.verified-check {
  position: absolute;
  right: 14px;
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--tb-teal-700);
}

.field-hint {
  font-size: 0.78rem;
  color: var(--tb-slate-400);
}

.save-success {
  background-color: #dcfce7;
  color: #15803d;
  border: 1px solid #bbf7d0;
  padding: 10px 14px;
  border-radius: var(--tb-radius-md);
  font-size: 0.88rem;
  font-weight: 600;
}

.form-actions {
  margin-top: 6px;
}

@media (max-width: 860px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}
</style>

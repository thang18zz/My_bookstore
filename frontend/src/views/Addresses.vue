<template>
  <div class="addresses-page">
    <div class="page-header">
      <div>
        <h2>Sổ địa chỉ nhận hàng</h2>
        <p class="subtitle">Quản lý danh sách địa chỉ giao hàng để thanh toán nhanh chóng hơn</p>
      </div>
      <button class="btn btn-primary" @click="openNew">
        <span>+</span> Thêm địa chỉ mới
      </button>
    </div>

    <!-- Alert message -->
    <div v-if="notice" :class="['alert', notice.type]">
      {{ notice.message }}
    </div>

    <!-- Address Cards Grid -->
    <div class="address-grid">
      <div v-if="addresses.length === 0" class="empty-box">
        <p>Bạn chưa có địa chỉ giao hàng nào.</p>
        <button class="btn btn-primary btn-sm" @click="openNew">+ Thêm địa chỉ ngay</button>
      </div>

      <div 
        v-for="a in addresses" 
        :key="a.id" 
        :class="['address-card', { 'is-default': a.default }]"
      >
        <div class="card-top">
          <div class="recipient-info">
            <span class="recipient-name">{{ a.recipientName }}</span>
            <span class="phone-number">{{ a.phone }}</span>
            <span v-if="a.email" class="email-badge">✉️ {{ a.email }}</span>
          </div>
          <span v-if="a.default" class="badge-default">Địa chỉ mặc định</span>
        </div>

        <div class="card-address">
          <p class="address-line">{{ a.line1 }}</p>
          <p class="address-sub">{{ a.district }}, {{ a.city }}</p>
        </div>

        <div class="card-actions">
          <button 
            v-if="!a.default" 
            class="btn btn-sm btn-link" 
            @click="setDefault(a.id)"
          >
            ⭐ Đặt làm mặc định
          </button>
          <div class="edit-group">
            <button class="btn btn-sm btn-edit" @click="edit(a)">Sửa</button>
            <button class="btn btn-sm btn-delete" @click="remove(a)">Xóa</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Form: Add / Edit Address -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingId ? '✏️ Chỉnh sửa địa chỉ' : '📍 Thêm địa chỉ nhận hàng mới' }}</h3>
          <button class="close-btn" @click="showModal = false">&times;</button>
        </div>

        <form @submit.prevent="save">
          <div class="modal-body">
            <div class="form-row">
              <div class="form-group half">
                <label>Họ tên người nhận <span class="req">*</span></label>
                <input v-model="form.recipientName" placeholder="Ví dụ: Nguyễn Văn A" required />
              </div>

              <div class="form-group half">
                <label>Email người nhận <span class="req">*</span></label>
                <input type="email" v-model="form.email" placeholder="Ví dụ: nguyenvana@gmail.com" required />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label>Số điện thoại <span class="req">*</span></label>
                <input v-model="form.phone" placeholder="Ví dụ: 0912345678" required />
              </div>

              <div class="form-group half">
                <label>Tỉnh / Thành phố <span class="req">*</span></label>
                <input v-model="form.city" placeholder="Ví dụ: Hà Nội / TP. Hồ Chí Minh" required />
              </div>
            </div>

            <div class="form-group">
              <label>Quận / Huyện <span class="req">*</span></label>
              <input v-model="form.district" placeholder="Ví dụ: Cầu Giấy / Quận 1" required />
            </div>

            <div class="form-group">
              <label>Địa chỉ chi tiết (Số nhà, ngõ, tên đường) <span class="req">*</span></label>
              <input v-model="form.line1" placeholder="Ví dụ: Số 123 đường Cầu Giấy, Phường Dịch Vọng" required />
            </div>

            <div class="form-checkbox">
              <label>
                <input type="checkbox" v-model="form.isDefault" />
                <span>Đặt làm địa chỉ nhận hàng mặc định</span>
              </label>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showModal = false">Hủy bỏ</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật địa chỉ' : 'Lưu địa chỉ') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import addressApi from '../api/addressApi'

const addresses = ref([])
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)
const notice = ref(null)

const form = reactive({
  recipientName: '',
  email: '',
  phone: '',
  line1: '',
  district: '',
  city: '',
  isDefault: false
})

onMounted(load)

async function load() {
  try {
    const res = await addressApi.list()
    addresses.value = res.data || []
  } catch (err) {
    showNotice('Lỗi tải danh sách địa chỉ: ' + err.message, 'error')
  }
}

function openNew() {
  editingId.value = null
  Object.assign(form, {
    recipientName: '',
    email: '',
    phone: '',
    line1: '',
    district: '',
    city: '',
    isDefault: addresses.value.length === 0
  })
  showModal.value = true
}

function edit(a) {
  editingId.value = a.id
  Object.assign(form, {
    recipientName: a.recipientName,
    email: a.email || '',
    phone: a.phone,
    line1: a.line1,
    district: a.district,
    city: a.city,
    isDefault: a.default || false
  })
  showModal.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await addressApi.update(editingId.value, form)
      showNotice('Đã cập nhật địa chỉ thành công!', 'success')
    } else {
      await addressApi.add(form)
      showNotice('Đã thêm địa chỉ nhận hàng mới!', 'success')
    }
    showModal.value = false
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi lưu'
    showNotice(msg, 'error')
  } finally {
    saving.value = false
  }
}

async function remove(a) {
  if (!confirm(`Bạn có chắc muốn xóa địa chỉ của "${a.recipientName}" (${a.line1}, ${a.city})?`)) {
    return
  }
  try {
    await addressApi.remove(a.id)
    showNotice('Đã xóa địa chỉ!', 'success')
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi xóa'
    showNotice(msg, 'error')
  }
}

async function setDefault(id) {
  try {
    await addressApi.setDefault(id)
    showNotice('Đã thiết lập địa chỉ mặc định!', 'success')
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi đặt mặc định'
    showNotice(msg, 'error')
  }
}

function showNotice(message, type = 'success') {
  notice.value = { message, type }
  setTimeout(() => {
    if (notice.value && notice.value.message === message) notice.value = null
  }, 4000)
}
</script>

<style scoped>
.addresses-page {
  max-width: 860px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h2 { margin: 0 0 4px 0; color: #111827; }
.subtitle { margin: 0; color: #6b7280; font-size: 0.9rem; }

/* Alert */
.alert {
  padding: 10px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 0.9rem;
}
.alert.success { background: #def7ec; color: #03543f; border: 1px solid #bcf0da; }
.alert.error { background: #fde8e8; color: #9b1c1c; border: 1px solid #fbd5d5; }

/* Address Grid */
.address-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 16px;
}

.empty-box {
  grid-column: 1 / -1;
  text-align: center;
  background: white;
  padding: 40px;
  border-radius: 8px;
  border: 1px dashed #d1d5db;
  color: #6b7280;
}

.address-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: all 0.15s ease;
  position: relative;
}
.address-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.address-card.is-default {
  border-color: #3b82f6;
  background: #f8faff;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}
.recipient-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
.recipient-name {
  font-weight: 600;
  color: #111827;
  font-size: 1rem;
}
.phone-number {
  color: #6b7280;
  font-size: 0.9rem;
}
.email-badge {
  font-size: 0.82rem;
  color: #0f766e;
  background: #ccfbf1;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.badge-default {
  background: var(--tb-teal-600);
  color: white;
  font-size: 0.75rem;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
}

.card-address {
  margin-bottom: 16px;
}
.address-line {
  margin: 0 0 4px 0;
  color: #1f2937;
  font-size: 0.95rem;
}
.address-sub {
  margin: 0;
  color: #6b7280;
  font-size: 0.85rem;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
}

.edit-group {
  display: flex;
  gap: 6px;
  margin-left: auto;
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.15s;
}
.btn-primary { background: var(--tb-teal-600); color: white; }
.btn-primary:hover { background: var(--tb-teal-700); }
.btn-primary:disabled { background: #94a3b8; cursor: not-allowed; }

.btn-secondary { background: #f3f4f6; color: #374151; }
.btn-secondary:hover { background: #e5e7eb; }

.btn-sm { padding: 4px 10px; font-size: 0.8rem; }
.btn-link {
  background: transparent;
  color: var(--tb-teal-700);
  padding: 0;
  text-decoration: underline;
  font-weight: 600;
}
.btn-link:hover { color: var(--tb-teal-800); }

.btn-edit { background: #f3f4f6; color: #1f2937; border: 1px solid #d1d5db; }
.btn-edit:hover { background: #e5e7eb; }

.btn-delete { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.btn-delete:hover { background: #fca5a5; }

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  width: 90%;
  max-width: 540px;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}
.modal-header h3 { margin: 0; font-size: 1.05rem; }
.close-btn {
  background: transparent;
  border: none;
  font-size: 1.4rem;
  color: #6b7280;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.form-group { margin-bottom: 14px; }
.form-group label { display: block; font-size: 0.85rem; font-weight: 500; margin-bottom: 4px; color: #374151; }
.req { color: #dc2626; }

.form-group input {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}
.form-group input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37,99,235,0.15);
}

.form-row { display: flex; gap: 12px; }
.half { flex: 1; }

.form-checkbox {
  margin-top: 10px;
}
.form-checkbox label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #374151;
}
</style>

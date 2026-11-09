<template>
  <div class="checkout-page">
    <h2>Thanh toán & Đặt hàng</h2>

    <div class="checkout-layout">
      <!-- Left column: Address Selection -->
      <div class="checkout-main">
        <section class="checkout-section">
          <div class="section-header">
            <h3>1. Chọn địa chỉ giao hàng</h3>
            <button class="btn btn-sm btn-outline" @click="showAddModal = true">
              + Thêm địa chỉ mới
            </button>
          </div>

          <div v-if="addresses.length === 0" class="no-address-box">
            <p>Bạn chưa có địa chỉ nhận hàng nào.</p>
            <button class="btn btn-primary btn-sm" @click="showAddModal = true">
              + Thêm địa chỉ nhận hàng ngay
            </button>
          </div>

          <div v-else class="address-options">
            <label 
              v-for="a in addresses" 
              :key="a.id" 
              :class="['addr-card', { selected: addressId === a.id }]"
            >
              <input 
                type="radio" 
                v-model="addressId" 
                :value="a.id" 
                name="shippingAddress" 
                class="addr-radio"
              />
              <div class="addr-info">
                <div class="addr-top">
                  <span class="name">{{ a.recipientName }}</span>
                  <span class="phone">{{ a.phone }}</span>
                  <span v-if="a.email" class="email">✉️ {{ a.email }}</span>
                  <span v-if="a.default" class="badge-def">Mặc định</span>
                </div>
                <div class="addr-detail">
                  {{ a.line1 }}, {{ a.district }}, {{ a.city }}
                </div>
              </div>
            </label>
          </div>

          <div class="manage-link">
            <router-link to="/addresses">⚙️ Quản lý sổ địa chỉ (Sửa / Xóa)</router-link>
          </div>
        </section>

        <!-- Payment method info -->
        <section class="checkout-section">
          <h3>2. Phương thức thanh toán</h3>
          <div class="payment-options-list">
            <label :class="['payment-option-card', { selected: paymentMethod === 'OFFLINE' }]">
              <input type="radio" value="OFFLINE" v-model="paymentMethod" name="paymentMethod" />
              <div class="option-content">
                <div class="option-title">💵 Thanh toán khi nhận hàng (COD)</div>
                <div class="option-desc">Thanh toán bằng tiền mặt khi nhận hàng tại địa chỉ của bạn.</div>
              </div>
            </label>

            <label :class="['payment-option-card', { selected: paymentMethod === 'ONLINE' }]">
              <input type="radio" value="ONLINE" v-model="paymentMethod" name="paymentMethod" />
              <div class="option-content">
                <div class="option-title">💳 Thanh toán online (Mã QR Mona Pay)</div>
                <div class="option-desc">Thanh toán trực tuyến bảo mật qua cổng thanh toán Mona Pay (Quét mã QR).</div>
              </div>
            </label>
          </div>
        </section>
      </div>

      <!-- Right column: Order Preview & Confirm -->
      <div class="checkout-sidebar">
        <div class="order-summary-box">
          <h3>Đơn hàng của bạn</h3>

          <div v-if="cart.items.length === 0" class="empty-cart-msg">
            Giỏ hàng trống. <router-link to="/products">Mua sắm ngay</router-link>
          </div>

          <div v-else>
            <div class="summary-items">
              <div v-for="i in cart.items" :key="i.productId" class="summary-item">
                <div class="item-name">
                  <strong>{{ i.productName }}</strong>
                  <span class="item-qty">x {{ i.quantity }}</span>
                </div>
                <div class="item-price">
                  {{ (i.price * i.quantity).toLocaleString() }} đ
                </div>
              </div>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-row">
              <span>Tạm tính:</span>
              <span>{{ cart.total.toLocaleString() }} đ</span>
            </div>
            <div class="summary-row">
              <span>Phí vận chuyển:</span>
              <span class="free-ship">Miễn phí</span>
            </div>

            <div v-if="preview" class="shipping-to">
              <small>Giao tới: {{ preview.addressSummary }}</small>
            </div>

            <div class="summary-divider"></div>

            <div class="summary-total">
              <span>Tổng cộng:</span>
              <span class="total-price">{{ cart.total.toLocaleString() }} đ</span>
            </div>

            <div v-if="error" class="alert-error">
              {{ error }}
            </div>

            <button 
              class="btn btn-primary btn-checkout" 
              :disabled="!addressId || cart.items.length === 0 || placingOrder"
              @click="confirm"
            >
              {{ placingOrder ? 'Đang xử lý...' : 'Tiến hành thanh toán' }}
            </button>
            <p v-if="!addressId" class="hint-msg">Vui lòng chọn hoặc thêm địa chỉ nhận hàng để tiếp tục</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Add Address Quickly -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>📍 Thêm địa chỉ nhận hàng mới</h3>
          <button class="close-btn" @click="showAddModal = false">&times;</button>
        </div>

        <form @submit.prevent="saveNewAddress">
          <div class="modal-body">
            <div class="form-row">
              <div class="form-group half">
                <label>Họ tên người nhận <span class="req">*</span></label>
                <input v-model="newAddr.recipientName" placeholder="Nguyễn Văn A" required />
              </div>
              <div class="form-group half">
                <label>Email người nhận <span class="req">*</span></label>
                <input type="email" v-model="newAddr.email" placeholder="nguyenvana@gmail.com" required />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label>Số điện thoại <span class="req">*</span></label>
                <input v-model="newAddr.phone" placeholder="0912345678" required />
              </div>
              <div class="form-group half">
                <label>Tỉnh / Thành phố <span class="req">*</span></label>
                <input v-model="newAddr.city" placeholder="Hà Nội" required />
              </div>
            </div>

            <div class="form-group">
              <label>Quận / Huyện <span class="req">*</span></label>
              <input v-model="newAddr.district" placeholder="Cầu Giấy" required />
            </div>

            <div class="form-group">
              <label>Địa chỉ chi tiết (Số nhà, tên đường) <span class="req">*</span></label>
              <input v-model="newAddr.line1" placeholder="Số 123 đường Cầu Giấy" required />
            </div>

            <div class="form-checkbox">
              <label>
                <input type="checkbox" v-model="newAddr.isDefault" />
                <span>Đặt làm địa chỉ nhận hàng mặc định</span>
              </label>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showAddModal = false">Hủy</button>
            <button type="submit" class="btn btn-primary" :disabled="savingAddr">
              {{ savingAddr ? 'Đang lưu...' : 'Sử dụng địa chỉ này' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import addressApi from '../api/addressApi'
import orderApi from '../api/orderApi'
import { useCartStore } from '../stores/cart'

const addresses = ref([])
const addressId = ref(null)
const paymentMethod = ref('OFFLINE')
const preview = ref(null)
const error = ref('')
const placingOrder = ref(false)
const showAddModal = ref(false)
const savingAddr = ref(false)

const router = useRouter()
const cart = useCartStore()

const newAddr = reactive({
  recipientName: '',
  email: '',
  phone: '',
  line1: '',
  district: '',
  city: '',
  isDefault: true
})

onMounted(async () => {
  await cart.fetch()
  await loadAddresses()
})

async function loadAddresses() {
  try {
    const res = await addressApi.list()
    addresses.value = res.data || []
    if (!addressId.value) {
      const def = addresses.value.find((a) => a.default)
      if (def) addressId.value = def.id
      else if (addresses.value.length > 0) addressId.value = addresses.value[0].id
    }
  } catch (err) {
    console.error('Lỗi tải địa chỉ:', err)
  }
}

watch(addressId, async (id) => {
  if (!id) {
    preview.value = null
    return
  }
  try {
    preview.value = (await orderApi.preview(id)).data
  } catch (err) {
    console.error('Lỗi preview đơn hàng:', err)
  }
})

async function saveNewAddress() {
  savingAddr.value = true
  try {
    const res = await addressApi.add(newAddr)
    showAddModal.value = false
    await loadAddresses()
    if (res.data?.id) {
      addressId.value = res.data.id
    }
  } catch (err) {
    alert(err.response?.data?.message || err.message || 'Không thể lưu địa chỉ')
  } finally {
    savingAddr.value = false
  }
}

async function confirm() {
  if (!addressId.value) return
  placingOrder.value = true
  error.value = ''
  try {
    const res = await orderApi.checkout({
      addressId: addressId.value,
      paymentMethod: paymentMethod.value
    })
    await cart.fetch()
    router.push(`/order-created/${res.data.id}`)
  } catch (e) {
    error.value = e.response?.data?.message || e.message || 'Đặt hàng thất bại'
  } finally {
    placingOrder.value = false
  }
}
</script>

<style scoped>
.checkout-page {
  max-width: 1000px;
  margin: 0 auto;
}
.checkout-page h2 { margin-bottom: 24px; color: #111827; }

.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.checkout-section {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-header h3 { margin: 0; font-size: 1.1rem; color: #1f2937; }

.no-address-box {
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
  padding: 24px;
  text-align: center;
  color: #6b7280;
}

.address-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.addr-card {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
}
.addr-card:hover { border-color: var(--tb-teal-200); background: var(--tb-teal-50); }
.addr-card.selected { border-color: var(--tb-teal-600); background: var(--tb-teal-50); border-width: 2px; }

.addr-radio { margin-top: 4px; accent-color: var(--tb-teal-600); }
.addr-info { flex: 1; }
.addr-top { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin-bottom: 4px; }
.name { font-weight: 600; color: #111827; }
.phone { color: #6b7280; font-size: 0.9rem; }
.addr-top .email { color: var(--tb-teal-700); font-size: 0.85rem; font-weight: 500; }
.badge-def {
  background: var(--tb-teal-100);
  color: var(--tb-teal-800);
  font-size: 0.75rem;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
}
.addr-detail { font-size: 0.9rem; color: #374151; }

.manage-link {
  margin-top: 14px;
  font-size: 0.85rem;
}
.manage-link a { color: var(--tb-teal-700); text-decoration: none; font-weight: 600; }
.manage-link a:hover { text-decoration: underline; }

.payment-options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-option-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.payment-option-card:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.payment-option-card.selected {
  background: #f0fdfa;
  border-color: var(--tb-teal-600, #0d9488);
  box-shadow: 0 0 0 1px var(--tb-teal-600, #0d9488);
}

.payment-option-card input[type="radio"] {
  margin-top: 3px;
  accent-color: var(--tb-teal-600, #0d9488);
  cursor: pointer;
}

.option-content {
  flex: 1;
}

.option-title {
  font-weight: 600;
  font-size: 0.95rem;
  color: #1f2937;
  margin-bottom: 2px;
}

.option-desc {
  font-size: 0.84rem;
  color: #6b7280;
}

/* Sidebar Order Summary */
.order-summary-box {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}
.order-summary-box h3 { margin: 0 0 16px 0; font-size: 1.1rem; color: #1f2937; }

.summary-items {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 12px;
}
.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.9rem;
}
.item-name { flex: 1; padding-right: 8px; }
.item-qty { color: #6b7280; margin-left: 4px; }
.item-price { font-weight: 500; }

.summary-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 12px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 0.9rem;
  color: #4b5563;
}
.free-ship { color: #059669; font-weight: 500; }

.shipping-to {
  background: #f3f4f6;
  padding: 6px 10px;
  border-radius: 4px;
  color: #4b5563;
  margin: 8px 0;
  font-size: 0.8rem;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 1.1rem;
  font-weight: bold;
  color: #111827;
  margin: 16px 0;
}
.total-price { color: var(--tb-teal-700); font-size: 1.4rem; font-weight: 800; }

.btn-checkout {
  width: 100%;
  padding: 12px;
  font-size: 1rem;
  font-weight: 600;
}
.hint-msg {
  font-size: 0.8rem;
  color: #dc2626;
  text-align: center;
  margin-top: 8px;
}
.alert-error {
  background: #fde8e8;
  color: #9b1c1c;
  padding: 8px;
  border-radius: 4px;
  font-size: 0.85rem;
  margin-bottom: 12px;
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
.btn-outline { background: transparent; border: 1px solid var(--tb-teal-600); color: var(--tb-teal-700); }
.btn-outline:hover { background: var(--tb-teal-50); }

/* Modal styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  width: 90%;
  max-width: 500px;
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid #e5e7eb;
}
.modal-header h3 { margin: 0; font-size: 1.05rem; }
.close-btn { background: transparent; border: none; font-size: 1.4rem; cursor: pointer; }
.modal-body { padding: 18px; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 18px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 0.85rem; font-weight: 500; margin-bottom: 4px; color: #374151; }
.req { color: #dc2626; }
.form-group input {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}
.form-row { display: flex; gap: 10px; }
.half { flex: 1; }
.form-checkbox { margin-top: 8px; }
.form-checkbox label { display: flex; align-items: center; gap: 6px; font-size: 0.85rem; cursor: pointer; }
</style>

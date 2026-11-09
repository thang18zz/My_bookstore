<template>
  <div class="order-created-page container">
    <div v-if="loading" class="card-surface loading-box">
      <div class="spinner"></div>
      <p>Đang tải thông tin đơn hàng vừa tạo...</p>
    </div>

    <div v-else-if="error" class="card-surface error-box">
      <div class="error-icon">⚠️</div>
      <h2>Không thể tải thông tin đơn hàng</h2>
      <p>{{ error }}</p>
      <router-link to="/orders" class="btn btn-primary">Xem danh sách đơn hàng</router-link>
    </div>

    <div v-else-if="order" class="created-container">
      <!-- Success Header Banner -->
      <div class="card-surface success-header-card">
        <div class="success-icon-badge">✓</div>
        <h1 class="font-serif success-title">Tạo đơn hàng thành công</h1>
        <p class="success-subtitle">
          Cảm ơn bạn đã đặt hàng tại BookStore! Đơn hàng của bạn đã được ghi nhận vào hệ thống.
        </p>

        <!-- Top summary chips -->
        <div class="top-chips">
          <div class="chip">
            <span class="chip-label">Mã đơn hàng:</span>
            <strong class="chip-val">#{{ order.id }}</strong>
          </div>
          <div class="chip">
            <span class="chip-label">Thời gian tạo:</span>
            <span class="chip-val">{{ formatDate(order.createdAt) }}</span>
          </div>
          <div class="chip">
            <span class="chip-label">Trạng thái đơn hàng:</span>
            <span :class="['status-pill', getStatusClass(orderStatus)]">
              {{ getStatusText(orderStatus) }}
            </span>
          </div>
          <div class="chip">
            <span class="chip-label">Trạng thái thanh toán:</span>
            <span :class="['pay-pill', getPaymentStatusClass(order.paymentStatus)]">
              {{ getPaymentStatusText(order.paymentStatus) }}
            </span>
          </div>
        </div>

        <!-- Action Banner: Only show Pay button if ONLINE and not paid -->
        <div v-if="isOnlinePayment" class="online-payment-banner">
          <div v-if="order.paymentStatus !== 'PAID'" class="pay-cta-box">
            <div class="pay-cta-info">
              <h3>💳 Tiến hành thanh toán đơn hàng</h3>
              <p>Đơn hàng được chọn thanh toán trực tuyến qua cổng thanh toán <strong>Mona Pay</strong>. Vui lòng nhấn nút bên dưới để chuyển sang cổng thanh toán hoặc quét mã QR ngân hàng.</p>
            </div>
            <button 
              class="btn btn-primary btn-pay-now" 
              :disabled="paying"
              @click="handlePayNow"
            >
              {{ paying ? 'Đang chuyển hướng thanh toán...' : 'Thanh toán' }}
            </button>
          </div>
          <div v-else class="paid-alert">
            ✓ Đơn hàng này đã được thanh toán thành công qua Mona Pay!
          </div>
        </div>

        <!-- Offline/COD notice -->
        <div v-else class="cod-banner">
          <p>
            📦 <strong>Phương thức: Thanh toán khi nhận hàng (COD).</strong> 
            Đơn hàng của bạn sẽ được nhân viên đóng gói và chuyển phát đến địa chỉ nhận. Vui lòng thanh toán số tiền 
            <strong>{{ order.totalAmount.toLocaleString() }} đ</strong> khi nhận sách.
          </p>
        </div>
      </div>

      <!-- 2-Column Detailed Layout -->
      <div class="details-grid">
        <!-- Left Column: Items List -->
        <div class="card-surface items-card">
          <h3 class="font-serif section-heading">
            Kiện sách trong đơn ({{ order.items ? order.items.length : 0 }})
          </h3>

          <div class="items-list">
            <div v-for="i in order.items" :key="i.productId" class="item-row">
              <div class="item-main">
                <router-link :to="`/products/${i.productId}`" class="item-name font-serif">
                  {{ i.productName }}
                </router-link>
                <div class="item-meta">
                  <span>Mã sách: #{{ i.productId }}</span>
                  <span class="dot">•</span>
                  <span>Đơn giá: {{ i.priceAtPurchase.toLocaleString() }} đ</span>
                </div>
              </div>
              <div class="item-qty">
                x {{ i.quantity }}
              </div>
              <div class="item-subtotal">
                {{ (i.priceAtPurchase * i.quantity).toLocaleString() }} đ
              </div>
            </div>
          </div>

          <div class="items-summary-footer">
            <div class="footer-row">
              <span>Tạm tính sách:</span>
              <span>{{ order.totalAmount.toLocaleString() }} đ</span>
            </div>
            <div class="footer-row">
              <span>Phí giao hàng:</span>
              <span class="free-badge">Miễn phí</span>
            </div>
            <div class="divider"></div>
            <div class="footer-row total-row">
              <span>Tổng thanh toán:</span>
              <strong class="total-price">{{ order.totalAmount.toLocaleString() }} đ</strong>
            </div>
          </div>
        </div>

        <!-- Right Column: Shipping & Payment Information -->
        <div class="info-sidebar">
          <!-- Shipping info -->
          <div class="card-surface side-card">
            <h4 class="font-serif card-title">📍 Thông tin giao hàng</h4>
            <div class="info-group">
              <span class="label">Người nhận hàng:</span>
              <strong class="value">{{ order.recipientName || 'Chưa cập nhật' }}</strong>
            </div>
            <div class="info-group">
              <span class="label">Địa chỉ nhận hàng:</span>
              <p class="value address-val">{{ order.shippingAddress }}</p>
            </div>
            <div class="info-group">
              <span class="label">Hình thức vận chuyển:</span>
              <span class="value">Giao hàng tiêu chuẩn (Miễn phí)</span>
            </div>
          </div>

          <!-- Payment info details (OrderDto fields) -->
          <div class="card-surface side-card">
            <h4 class="font-serif card-title">💳 Chi tiết thanh toán</h4>

            <div class="info-group">
              <span class="label">Phương thức:</span>
              <span class="value font-semibold">
                {{ isOnlinePayment ? 'Thanh toán online (Mona Pay)' : 'Thanh toán khi nhận hàng (COD)' }}
              </span>
            </div>

            <div class="info-group">
              <span class="label">Trạng thái thanh toán:</span>
              <span :class="['pay-pill-sm', getPaymentStatusClass(order.paymentStatus)]">
                {{ getPaymentStatusText(order.paymentStatus) }}
              </span>
            </div>

            <div v-if="order.paymentReference" class="info-group">
              <span class="label">Mã tham chiếu checkout:</span>
              <code class="code-val">{{ order.paymentReference }}</code>
            </div>

            <div v-if="order.transactionId" class="info-group">
              <span class="label">Mã giao dịch:</span>
              <code class="code-val">{{ order.transactionId }}</code>
            </div>

            <div v-if="order.paidAt" class="info-group">
              <span class="label">Thời gian thanh toán:</span>
              <span class="value">{{ formatDate(order.paidAt) }}</span>
            </div>

            <div class="info-group">
              <span class="label">Tổng tiền:</span>
              <strong class="value total-val">{{ order.totalAmount.toLocaleString() }} đ</strong>
            </div>
          </div>

          <!-- Navigation actions -->
          <div class="nav-actions">
            <!-- If ONLINE and UNPAID, emphasize the Pay button here too -->
            <button 
              v-if="isOnlinePayment && order.paymentStatus !== 'PAID'" 
              class="btn btn-primary btn-block btn-pay-action"
              :disabled="paying"
              @click="handlePayNow"
            >
              {{ paying ? 'Đang chuyển hướng...' : '💳 Thanh toán ngay' }}
            </button>

            <router-link :to="`/orders/${order.id}`" class="btn btn-outline btn-block">
              📄 Xem chi tiết đơn hàng
            </router-link>

            <router-link to="/products" class="btn btn-ghost btn-block">
              🛍️ Tiếp tục mua sắm
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import orderApi from '../api/orderApi'
import paymentApi from '../api/paymentApi'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const error = ref('')
const paying = ref(false)

onMounted(loadOrder)

async function loadOrder() {
  loading.value = true
  error.value = ''
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data
  } catch (err) {
    error.value = err.response?.data?.message || err.message || 'Không thể tải chi tiết đơn hàng.'
  } finally {
    loading.value = false
  }
}

const orderStatus = computed(() => {
  if (!order.value) return 'PENDING'
  return order.value.orderStatus || order.value.oderStatus || order.value.status || 'PENDING'
})

const isOnlinePayment = computed(() => {
  if (!order.value) return false
  return (order.value.paymentMethod || '').toUpperCase() === 'ONLINE'
})

async function handlePayNow() {
  if (!order.value || paying.value) return
  paying.value = true
  try {
    const res = await paymentApi.create(order.value.id)
    const checkoutUrl = res.data?.checkoutUrl
    if (checkoutUrl) {
      window.location.href = checkoutUrl
    } else {
      alert('Không nhận được đường dẫn thanh toán từ cổng Mona Pay.')
    }
  } catch (err) {
    alert('Khởi tạo thanh toán thất bại: ' + (err.response?.data?.message || err.message))
  } finally {
    paying.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return isNaN(d.getTime()) ? dateStr : d.toLocaleDateString('vi-VN', {
    hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric'
  })
}

function getStatusClass(status) {
  switch (status) {
    case 'PENDING': return 'status-pending'
    case 'CONFIRMED': return 'status-confirmed'
    case 'SHIPPING': return 'status-shipping'
    case 'DELIVERED': return 'status-delivered'
    case 'CANCELLED': return 'status-cancelled'
    default: return 'status-default'
  }
}

function getStatusText(status) {
  switch (status) {
    case 'PENDING': return '⏳ Chờ xử lý'
    case 'CONFIRMED': return '✓ Đã xác nhận'
    case 'SHIPPING': return '🚚 Đang giao hàng'
    case 'DELIVERED': return '🎉 Đã giao'
    case 'CANCELLED': return '✕ Đã hủy'
    default: return status || 'Chờ xử lý'
  }
}

function getPaymentStatusClass(pStatus) {
  switch (pStatus) {
    case 'PAID': return 'pay-paid'
    case 'UNPAID': return 'pay-unpaid'
    case 'FAILED': return 'pay-failed'
    default: return 'pay-unpaid'
  }
}

function getPaymentStatusText(pStatus) {
  switch (pStatus) {
    case 'PAID': return '✓ Đã thanh toán'
    case 'UNPAID': return 'Chưa thanh toán'
    case 'FAILED': return 'Thanh toán thất bại'
    default: return pStatus || 'Chưa thanh toán'
  }
}
</script>

<style scoped>
.order-created-page {
  padding-top: 20px;
  padding-bottom: 60px;
}

.loading-box, .error-box {
  padding: 60px 20px;
  text-align: center;
  border-radius: 12px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e5e7eb;
  border-top-color: var(--tb-teal-600, #0d9488);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  font-size: 3rem;
  margin-bottom: 12px;
}

.created-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Success Header Card */
.success-header-card {
  padding: 32px 28px;
  text-align: center;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f0fdfa 100%);
  border: 1px solid #ccfbf1;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.success-icon-badge {
  width: 64px;
  height: 64px;
  background: #10b981;
  color: white;
  font-size: 2.2rem;
  font-weight: 700;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 16px -4px rgba(16, 185, 129, 0.35);
}

.success-title {
  font-size: 2rem;
  color: #0f172a;
  margin-bottom: 8px;
}

.success-subtitle {
  color: #475569;
  font-size: 1rem;
  max-width: 600px;
  margin: 0 auto 24px;
}

.top-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
}

.chip {
  background: white;
  padding: 8px 14px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.88rem;
}

.chip-label {
  color: #64748b;
}

.chip-val {
  color: #0f172a;
  font-weight: 600;
}

/* Status & Payment Pills */
.status-pill {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 12px;
}
.status-pending { background: #fef3c7; color: #b45309; }
.status-confirmed { background: #e0f2fe; color: #0369a1; }
.status-shipping { background: #f3e8ff; color: #7e22ce; }
.status-delivered { background: #dcfce7; color: #15803d; }
.status-cancelled { background: #fee2e2; color: #b91c1c; }

.pay-pill, .pay-pill-sm {
  font-size: 0.8rem;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 12px;
}
.pay-paid { background: #dcfce7; color: #166534; }
.pay-unpaid { background: #fef3c7; color: #b45309; }
.pay-failed { background: #fee2e2; color: #991b1b; }

/* Action Banners */
.online-payment-banner {
  max-width: 720px;
  margin: 0 auto;
}

.pay-cta-box {
  background: white;
  border: 2px solid var(--tb-teal-600, #0d9488);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  text-align: left;
  box-shadow: 0 10px 15px -3px rgba(13, 148, 136, 0.1);
}

.pay-cta-info h3 {
  font-size: 1.15rem;
  color: #0f172a;
  margin-bottom: 4px;
}

.pay-cta-info p {
  font-size: 0.9rem;
  color: #475569;
  margin: 0;
}

.btn-pay-now {
  padding: 12px 28px;
  font-size: 1.05rem;
  font-weight: 700;
  white-space: nowrap;
  background: var(--tb-teal-600, #0d9488);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(13, 148, 136, 0.3);
  transition: all 0.2s;
}

.btn-pay-now:hover:not(:disabled) {
  background: var(--tb-teal-700, #0f766e);
  transform: translateY(-1px);
}

.paid-alert {
  background: #ecfdf5;
  color: #065f46;
  border: 1px solid #a7f3d0;
  padding: 14px 20px;
  border-radius: 8px;
  font-weight: 600;
}

.cod-banner {
  max-width: 680px;
  margin: 0 auto;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 14px 20px;
  border-radius: 8px;
  font-size: 0.92rem;
  color: #334155;
}

/* 2-Column Details Grid */
.details-grid {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 860px) {
  .details-grid {
    grid-template-columns: 1fr;
  }
  .pay-cta-box {
    flex-direction: column;
    text-align: center;
  }
}

.items-card {
  padding: 24px;
  border-radius: 12px;
}

.section-heading {
  font-size: 1.25rem;
  color: #0f172a;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e2e8f0;
}

.item-main {
  flex: 1;
}

.item-name {
  font-size: 1.02rem;
  color: #0f172a;
  text-decoration: none;
  font-weight: 600;
  display: block;
  margin-bottom: 4px;
}

.item-name:hover {
  color: var(--tb-teal-600, #0d9488);
}

.item-meta {
  font-size: 0.85rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  color: #cbd5e1;
}

.item-qty {
  font-size: 0.95rem;
  color: #475569;
  font-weight: 500;
}

.item-subtotal {
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
}

.items-summary-footer {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 8px;
}

.footer-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.92rem;
  color: #475569;
}

.free-badge {
  color: #10b981;
  font-weight: 600;
}

.divider {
  height: 1px;
  background: #e2e8f0;
  margin: 4px 0;
}

.total-row {
  font-size: 1.05rem;
  color: #0f172a;
}

.total-price {
  font-size: 1.25rem;
  color: var(--tb-teal-700, #0f766e);
}

/* Sidebar Cards */
.info-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-card {
  padding: 20px;
  border-radius: 12px;
}

.card-title {
  font-size: 1.1rem;
  color: #0f172a;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f5f9;
}

.info-group {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.info-group:last-child {
  margin-bottom: 0;
}

.label {
  font-size: 0.82rem;
  color: #64748b;
  font-weight: 500;
}

.value {
  font-size: 0.92rem;
  color: #1e293b;
}

.address-val {
  margin: 0;
  line-height: 1.45;
}

.code-val {
  background: #f1f5f9;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.85rem;
  color: #0f172a;
  word-break: break-all;
  font-family: monospace;
}

.font-semibold {
  font-weight: 600;
}

.total-val {
  font-size: 1.15rem;
  color: var(--tb-teal-700, #0f766e);
}

.nav-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-block {
  width: 100%;
  text-align: center;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 600;
  text-decoration: none;
  display: block;
}

.btn-pay-action {
  background: var(--tb-teal-600, #0d9488);
  color: white;
  border: none;
  cursor: pointer;
  margin-bottom: 4px;
}

.btn-pay-action:hover:not(:disabled) {
  background: var(--tb-teal-700, #0f766e);
}

.btn-outline {
  background: white;
  border: 1px solid #cbd5e1;
  color: #334155;
}

.btn-outline:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}

.btn-ghost {
  background: transparent;
  color: #64748b;
}

.btn-ghost:hover {
  background: #f1f5f9;
  color: #1e293b;
}
</style>


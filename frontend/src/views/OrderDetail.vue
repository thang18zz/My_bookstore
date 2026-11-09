<template>
  <div class="order-detail-page container">
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
      <router-link to="/orders">← Quay lại danh sách đơn hàng</router-link>
    </nav>

    <div v-if="!order" class="loading-box card-surface">
      <p>Đang tải thông tin đơn hàng...</p>
    </div>

    <div v-else class="order-detail-layout">
      <!-- Order Header Card -->
      <div class="detail-header-card card-surface">
        <div class="header-top">
          <div>
            <h1 class="font-serif order-title">Chi Tiết Đơn Hàng #{{ order.id }}</h1>
            <span class="order-time">Thời gian đặt hàng: {{ formatDate(order.createdAt) }}</span>
          </div>
          <span :class="['status-badge', getStatusClass(orderStatus)]">
            {{ getStatusText(orderStatus) }}
          </span>
        </div>

        <!-- Tracking Timeline (matching OrderStatus enum: PENDING, CONFIRMED, SHIPPING, DELIVERED) -->
        <div class="tracking-timeline">
          <div :class="['timeline-step', { done: isStepDone(1) }]">
            <div class="step-dot">1</div>
            <span class="step-lbl">Đặt hàng</span>
          </div>
          <div :class="['timeline-line', { done: isStepDone(2) }]"></div>
          <div :class="['timeline-step', { done: isStepDone(2) }]">
            <div class="step-dot">2</div>
            <span class="step-lbl">Xác nhận</span>
          </div>
          <div :class="['timeline-line', { done: isStepDone(3) }]"></div>
          <div :class="['timeline-step', { done: isStepDone(3) }]">
            <div class="step-dot">3</div>
            <span class="step-lbl">Đang giao</span>
          </div>
          <div :class="['timeline-line', { done: isStepDone(4) }]"></div>
          <div :class="['timeline-step', { done: isStepDone(4) }]">
            <div class="step-dot">4</div>
            <span class="step-lbl">Hoàn tất</span>
          </div>
        </div>
      </div>

      <!-- 2-Column Details -->
      <div class="order-body-grid">
        <!-- Left Column: Items List -->
        <div class="items-card card-surface">
          <h3 class="font-serif card-heading">Kiện Sách Trong Đơn ({{ order.items ? order.items.length : 0 }})</h3>

          <div class="items-table">
            <div v-for="i in order.items" :key="i.productId" class="item-row">
              <div class="item-info">
                <router-link :to="`/products/${i.productId}`" class="item-name font-serif">
                  {{ i.productName }}
                </router-link>
                <span class="item-spec">Đơn giá: {{ i.priceAtPurchase.toLocaleString() }} đ</span>
              </div>
              <div class="item-qty">
                x {{ i.quantity }}
              </div>
              <div class="item-subtotal">
                <strong>{{ (i.priceAtPurchase * i.quantity).toLocaleString() }} đ</strong>
              </div>
            </div>
          </div>

          <!-- Cancel order button if pending/confirmed -->
          <div v-if="['PENDING', 'CONFIRMED'].includes(orderStatus)" class="cancel-row">
            <button class="btn btn-danger" @click="handleCancel">
              ✕ Hủy đơn hàng này
            </button>
            <small class="cancel-note">* Bạn có thể hủy đơn hàng khi trạng thái là Chờ xử lý hoặc Đã xác nhận.</small>
          </div>
        </div>

        <!-- Right Column: Shipping & Summary -->
        <div class="summary-col">
          <!-- Shipping Address -->
          <div class="info-card card-surface">
            <h4 class="font-serif">Địa Chỉ Nhận Hàng</h4>
            
            <div class="info-line">
              <span class="info-label">Người nhận:</span>
              <strong class="recipient-name">{{ order.recipientName || 'Chưa cập nhật' }}</strong>
            </div>

            <div v-if="order.recipientEmail" class="info-line">
              <span class="info-label">Email nhận:</span>
              <span class="info-val">{{ order.recipientEmail }}</span>
            </div>

            <div class="info-line">
              <span class="info-label">Địa chỉ:</span>
              <p class="address-text">{{ order.shippingAddress }}</p>
            </div>

            <span class="shipping-method">🚚 Giao hàng tiêu chuẩn (Miễn phí)</span>
          </div>

          <!-- Payment Info (Full OrderDto fields) -->
          <div class="info-card card-surface">
            <h4 class="font-serif">Thanh Toán & Hóa Đơn</h4>

            <div class="sum-row">
              <span>Hình thức thanh toán:</span>
              <strong>{{ isOnlinePayment ? 'Thanh toán online (Mona Pay)' : 'Thanh toán khi nhận hàng (COD)' }}</strong>
            </div>

            <div class="sum-row">
              <span>Trạng thái thanh toán:</span>
              <span :class="['pay-pill-sm', getPaymentStatusClass(order.paymentStatus)]">
                {{ getPaymentStatusText(order.paymentStatus) }}
              </span>
            </div>

            <div v-if="order.paymentReference" class="sum-row">
              <span>Mã tham chiếu checkout:</span>
              <code class="code-val">{{ order.paymentReference }}</code>
            </div>

            <div v-if="order.transactionId" class="sum-row">
              <span>Mã giao dịch:</span>
              <code class="code-val">{{ order.transactionId }}</code>
            </div>

            <div v-if="order.paidAt" class="sum-row">
              <span>Thời gian thanh toán:</span>
              <span>{{ formatDate(order.paidAt) }}</span>
            </div>

            <div class="sum-divider"></div>

            <div class="sum-row">
              <span>Tạm tính sách:</span>
              <span>{{ order.totalAmount.toLocaleString() }} đ</span>
            </div>
            <div class="sum-row">
              <span>Phí vận chuyển:</span>
              <span class="free-text">Miễn phí</span>
            </div>

            <div class="sum-divider"></div>

            <div class="sum-total-row">
              <span>Tổng thanh toán:</span>
              <strong class="total-highlight">{{ order.totalAmount.toLocaleString() }} đ</strong>
            </div>

            <div :class="['payment-method-chip', { online: isOnlinePayment }]">
              {{ isOnlinePayment ? '💳 Thanh toán online qua cổng Mona Pay' : '💵 Thanh toán khi nhận hàng (COD - Tiền mặt)' }}
            </div>

            <!-- Pay Now button if ONLINE and UNPAID and not CANCELLED -->
            <button 
              v-if="isOnlinePayment && order.paymentStatus !== 'PAID' && orderStatus !== 'CANCELLED'"
              class="btn btn-primary btn-pay-now-detail"
              :disabled="paying"
              @click="handlePayNow"
            >
              {{ paying ? 'Đang chuyển hướng...' : '💳 Thanh toán ngay' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import orderApi from '../api/orderApi'
import paymentApi from '../api/paymentApi'

const route = useRoute()
const order = ref(null)
const paying = ref(false)

onMounted(load)

async function load() {
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data
  } catch (e) {
    console.error('Failed to load order detail:', e)
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
      alert('Không nhận được đường dẫn thanh toán từ Mona Pay.')
    }
  } catch (err) {
    alert('Khởi tạo thanh toán thất bại: ' + (err.response?.data?.message || err.message))
  } finally {
    paying.value = false
  }
}

async function handleCancel() {
  if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) {
    try {
      const res = await orderApi.cancel(route.params.id)
      order.value = res.data
      alert('Đơn hàng đã được hủy thành công.')
    } catch (e) {
      alert('Không thể hủy đơn hàng: ' + (e.response?.data?.message || e.message))
    }
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
    case 'DELIVERED': return 'status-completed'
    case 'CANCELLED': return 'status-cancelled'
    default: return 'status-default'
  }
}

function getStatusText(status) {
  switch (status) {
    case 'PENDING': return '⏳ Chờ xử lý'
    case 'CONFIRMED': return '✓ Đã xác nhận'
    case 'SHIPPING': return '🚚 Đang giao hàng'
    case 'DELIVERED': return '🎉 Giao thành công'
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

function isStepDone(stepNum) {
  const s = orderStatus.value
  if (s === 'CANCELLED') return false
  if (stepNum === 1) return true
  if (stepNum === 2) return ['CONFIRMED', 'SHIPPING', 'DELIVERED'].includes(s)
  if (stepNum === 3) return ['SHIPPING', 'DELIVERED'].includes(s)
  if (stepNum === 4) return s === 'DELIVERED'
  return false
}
</script>

<style scoped>
.order-detail-page {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.breadcrumb a {
  font-size: 0.88rem;
  color: var(--tb-teal-700);
  font-weight: 600;
}

.loading-box {
  padding: 50px;
  text-align: center;
  background-color: #ffffff;
}

.order-detail-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Header Card */
.detail-header-card {
  padding: 24px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.order-title {
  font-size: 1.7rem;
  color: var(--tb-slate-900);
  margin-bottom: 4px;
}

.order-time {
  font-size: 0.85rem;
  color: var(--tb-slate-500);
}

.status-badge {
  font-size: 0.82rem;
  font-weight: 700;
  padding: 6px 14px;
  border-radius: var(--tb-radius-full);
}

.status-pending { background-color: var(--tb-amber-100); color: var(--tb-amber-600); }
.status-confirmed { background-color: #e0f2fe; color: #0284c7; }
.status-shipping { background-color: #f3e8ff; color: #7e22ce; }
.status-completed { background-color: #dcfce7; color: #15803d; }
.status-cancelled { background-color: var(--tb-red-100); color: var(--tb-red-600); }
.status-default { background-color: var(--tb-slate-100); color: var(--tb-slate-600); }

/* Payment Pills */
.pay-pill-sm {
  font-size: 0.78rem;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 10px;
}
.pay-paid { background: #dcfce7; color: #166534; }
.pay-unpaid { background: #fef3c7; color: #b45309; }
.pay-failed { background: #fee2e2; color: #991b1b; }

.code-val {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.82rem;
  font-family: monospace;
  word-break: break-all;
}

/* Tracking Timeline */
.tracking-timeline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  padding: 0 10px;
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 2;
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--tb-slate-100);
  border: 2px solid var(--tb-slate-300);
  color: var(--tb-slate-500);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
  transition: all 0.3s ease;
}

.timeline-step.done .step-dot {
  background-color: var(--tb-teal-600);
  border-color: var(--tb-teal-600);
  color: #ffffff;
}

.step-lbl {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--tb-slate-500);
}

.timeline-step.done .step-lbl {
  color: var(--tb-teal-800);
}

.timeline-line {
  flex-grow: 1;
  height: 3px;
  background-color: var(--tb-slate-200);
  margin: 0 8px;
  margin-bottom: 22px;
  z-index: 1;
}

.timeline-line.done {
  background-color: var(--tb-teal-500);
}

/* 2-Column Body */
.order-body-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}

/* Left Column: Items */
.items-card {
  padding: 24px;
  background-color: #ffffff;
}

.card-heading {
  font-size: 1.25rem;
  color: var(--tb-slate-900);
  margin-bottom: 18px;
}

.items-table {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--tb-slate-100);
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-grow: 1;
  padding-right: 16px;
}

.item-name {
  color: var(--tb-slate-900);
  text-decoration: none;
  font-size: 1.05rem;
}

.item-name:hover {
  color: var(--tb-teal-700);
}

.item-spec {
  font-size: 0.82rem;
  color: var(--tb-slate-500);
}

.item-qty {
  font-size: 0.9rem;
  color: var(--tb-slate-600);
  margin-right: 24px;
}

.item-subtotal {
  font-size: 0.95rem;
  color: var(--tb-slate-900);
}

.cancel-row {
  margin-top: 24px;
  padding-top: 18px;
  border-top: 1px solid var(--tb-slate-100);
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}

.cancel-note {
  font-size: 0.78rem;
  color: var(--tb-slate-500);
}

/* Right Column: Summary */
.summary-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card {
  padding: 20px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-card h4 {
  font-size: 1.1rem;
  color: var(--tb-slate-900);
  border-bottom: 1px solid var(--tb-slate-100);
  padding-bottom: 10px;
  margin-bottom: 4px;
}

.info-line {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.info-label {
  font-size: 0.8rem;
  color: var(--tb-slate-500);
}

.recipient-name {
  font-size: 0.95rem;
  color: var(--tb-slate-900);
}

.address-text {
  font-size: 0.9rem;
  color: var(--tb-slate-700);
  line-height: 1.45;
  margin: 0;
}

.shipping-method {
  font-size: 0.82rem;
  color: var(--tb-teal-700);
  font-weight: 600;
  margin-top: 4px;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  color: var(--tb-slate-600);
  gap: 8px;
}

.free-text {
  color: #10b981;
  font-weight: 600;
}

.sum-divider {
  height: 1px;
  background-color: var(--tb-slate-100);
  margin: 4px 0;
}

.sum-total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 1rem;
  color: var(--tb-slate-900);
}

.total-highlight {
  font-size: 1.4rem;
  color: var(--tb-teal-700);
}

.payment-method-chip {
  font-size: 0.82rem;
  color: var(--tb-slate-700);
  background-color: var(--tb-slate-100);
  padding: 8px 12px;
  border-radius: var(--tb-radius-sm);
  margin-top: 6px;
  font-weight: 500;
}

.payment-method-chip.online {
  background-color: #f0fdfa;
  color: #0f766e;
  border: 1px solid #ccfbf1;
}

.btn-pay-now-detail {
  margin-top: 8px;
  width: 100%;
  padding: 10px 16px;
  font-size: 0.95rem;
  font-weight: 600;
  background: var(--tb-teal-600, #0d9488);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  box-shadow: 0 2px 4px rgba(13, 148, 136, 0.2);
  transition: all 0.2s;
}

.btn-pay-now-detail:hover:not(:disabled) {
  background: var(--tb-teal-700, #0f766e);
}

@media (max-width: 860px) {
  .order-body-grid {
    grid-template-columns: 1fr;
  }
}
</style>

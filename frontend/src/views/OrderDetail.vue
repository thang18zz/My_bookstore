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
          <span :class="['status-badge', getStatusClass(order.status)]">
            {{ getStatusText(order.status) }}
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
          <h3 class="font-serif card-heading">Kiện Sách Trong Đơn ({{ order.items.length }})</h3>

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
          <div v-if="['PENDING', 'CONFIRMED'].includes(order.status)" class="cancel-row">
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
            <p class="address-text">{{ order.shippingAddress }}</p>
            <span class="shipping-method">🚚 Giao hàng tiêu chuẩn</span>
          </div>

          <!-- Payment Info -->
          <div class="info-card card-surface">
            <h4 class="font-serif">Thanh Toán & Hóa Đơn</h4>
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
            <div class="payment-method-chip">
              💵 Thanh toán khi nhận hàng (COD)
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import orderApi from '../api/orderApi'

const route = useRoute()
const order = ref(null)

onMounted(load)

async function load() {
  try {
    const res = await orderApi.detail(route.params.id)
    order.value = res.data
  } catch (e) {
    console.error('Failed to load order detail:', e)
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
  if (!dateStr) return 'Gần đây'
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
    default: return status
  }
}

function isStepDone(stepNum) {
  if (!order.value) return false
  const s = order.value.status
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
.status-shipping { background-color: var(--tb-teal-100); color: var(--tb-teal-700); }
.status-completed { background-color: #dcfce7; color: #16a34a; }
.status-cancelled { background-color: var(--tb-red-100); color: var(--tb-red-600); }

/* Timeline */
.tracking-timeline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background-color: var(--tb-slate-50);
  border-radius: var(--tb-radius-lg);
}

.timeline-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--tb-slate-300);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
}

.step-lbl {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--tb-slate-500);
}

.timeline-step.done .step-dot {
  background-color: var(--tb-teal-600);
}

.timeline-step.done .step-lbl {
  color: var(--tb-teal-800);
}

.timeline-line {
  flex: 1;
  height: 3px;
  background-color: var(--tb-slate-300);
  margin: 0 10px;
  position: relative;
  top: -12px;
}

.timeline-line.done {
  background-color: var(--tb-teal-600);
}

/* 2-Column Body */
.order-body-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
}

.items-card {
  padding: 24px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-heading {
  font-size: 1.25rem;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--tb-border);
}

.items-table {
  display: flex;
  flex-direction: column;
}

.item-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--tb-border);
  gap: 16px;
}

.item-row:last-child {
  border-bottom: none;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--tb-slate-900);
}

.item-name:hover {
  color: var(--tb-teal-700);
}

.item-spec {
  font-size: 0.82rem;
  color: var(--tb-slate-500);
}

.item-qty {
  font-size: 0.92rem;
  color: var(--tb-slate-600);
}

.item-subtotal {
  font-size: 1.05rem;
  color: var(--tb-teal-700);
}

.cancel-row {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed var(--tb-border);
  display: flex;
  align-items: center;
  gap: 12px;
}

.cancel-note {
  font-size: 0.78rem;
  color: var(--tb-slate-400);
}

/* Right Column Summary */
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
  padding-bottom: 6px;
  border-bottom: 1px solid var(--tb-border);
}

.address-text {
  font-size: 0.92rem;
  color: var(--tb-slate-700);
  line-height: 1.5;
}

.shipping-method {
  font-size: 0.82rem;
  color: var(--tb-teal-700);
  background-color: var(--tb-teal-50);
  padding: 6px 10px;
  border-radius: var(--tb-radius-sm);
  font-weight: 600;
}

.sum-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--tb-slate-600);
}

.free-text {
  color: var(--tb-teal-700);
  font-weight: 700;
}

.sum-divider {
  height: 1px;
  background-color: var(--tb-border);
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
}

@media (max-width: 860px) {
  .order-body-grid {
    grid-template-columns: 1fr;
  }
}
</style>

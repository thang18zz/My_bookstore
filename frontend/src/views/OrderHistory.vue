<template>
  <div class="orders-page container">
    <div class="page-title-row">
      <h1 class="font-serif page-title">Đơn Hàng Của Bạn</h1>
      <p class="page-sub">Theo dõi lịch sử mua sắm và tiến độ vận chuyển các đơn hàng của bạn</p>
    </div>

    <!-- Empty Orders -->
    <div v-if="!orders.length" class="empty-orders card-surface">
      <div class="empty-icon">📦</div>
      <h2 class="font-serif">Bạn chưa có đơn hàng nào</h2>
      <p>Các đơn hàng bạn đặt tại BookStore sẽ được lưu giữ và cập nhật hành trình tại đây.</p>
      <router-link to="/products" class="btn btn-primary btn-lg">
        Bắt đầu mua sắm ngay
      </router-link>
    </div>

    <!-- Orders List -->
    <div v-else class="orders-list">
      <div 
        v-for="o in orders" 
        :key="o.id" 
        class="order-card card-surface"
      >
        <div class="order-card-header">
          <div class="header-left">
            <span class="order-number font-serif">Mã đơn #{{ o.id }}</span>
            <span class="order-date">Thời gian đặt: {{ formatDate(o.createdAt) }}</span>
          </div>
          <span :class="['status-badge', getStatusClass(o.status)]">
            {{ getStatusText(o.status) }}
          </span>
        </div>

        <div class="order-card-body">
          <div class="order-info-item">
            <span class="info-lbl">Địa chỉ nhận hàng:</span>
            <span class="info-val">{{ o.shippingAddress || 'Địa chỉ mặc định' }}</span>
          </div>
          <div class="order-info-item">
            <span class="info-lbl">Tổng giá trị đơn:</span>
            <strong class="order-total">{{ o.totalAmount.toLocaleString() }} đ</strong>
          </div>
        </div>

        <div class="order-card-footer">
          <router-link :to="`/orders/${o.id}`" class="btn btn-secondary btn-sm">
            Xem chi tiết đơn hàng &rarr;
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import orderApi from '../api/orderApi'

const orders = ref([])

onMounted(async () => {
  try {
    const res = await orderApi.myOrders()
    orders.value = res.data
  } catch (e) {
    console.error('Error fetching orders:', e)
  }
})

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
</script>

<style scoped>
.orders-page {
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

/* Empty State */
.empty-orders {
  padding: 60px 20px;
  text-align: center;
  background-color: #ffffff;
  max-width: 580px;
  margin: 30px auto;
}

.empty-icon {
  font-size: 3.5rem;
  margin-bottom: 16px;
}

.empty-orders h2 {
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.empty-orders p {
  color: var(--tb-slate-500);
  margin-bottom: 24px;
}

/* Orders List */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background-color: #ffffff;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--tb-border);
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.order-number {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--tb-teal-800);
}

.order-date {
  font-size: 0.82rem;
  color: var(--tb-slate-500);
}

.status-badge {
  font-size: 0.78rem;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: var(--tb-radius-sm);
  text-transform: uppercase;
}

.status-pending {
  background-color: var(--tb-amber-100);
  color: var(--tb-amber-600);
}

.status-confirmed {
  background-color: #e0f2fe;
  color: #0284c7;
}

.status-shipping {
  background-color: var(--tb-teal-100);
  color: var(--tb-teal-700);
}

.status-completed {
  background-color: #dcfce7;
  color: #16a34a;
}

.status-cancelled {
  background-color: var(--tb-red-100);
  color: var(--tb-red-600);
}

.order-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.order-info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-lbl {
  font-size: 0.78rem;
  color: var(--tb-slate-500);
  text-transform: uppercase;
  font-weight: 700;
}

.info-val {
  font-size: 0.92rem;
  color: var(--tb-slate-800);
}

.order-total {
  font-size: 1.3rem;
  color: var(--tb-teal-700);
  font-weight: 800;
}

.order-card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  border-top: 1px dashed var(--tb-border);
}
</style>

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
              <p>Quét mã QR bằng ứng dụng ngân hàng hoặc camera trên điện thoại để hoàn tất thanh toán qua <strong>Mona Pay</strong>.</p>
            </div>
            <button 
              class="btn btn-primary btn-pay-now" 
              :disabled="paying"
              @click="handlePayNow"
            >
              {{ paying ? 'Đang tạo mã QR...' : 'Thanh toán' }}
            </button>
          </div>
          <div v-else class="paid-alert">
            <div class="paid-alert-title">✓ Đơn hàng đã được thanh toán thành công!</div>
            <div class="paid-alert-sub">Hóa đơn chi tiết đã được gửi về Gmail: <strong>{{ userEmail || 'của bạn' }}</strong></div>
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
              {{ paying ? 'Đang tạo mã QR...' : '💳 Quét mã QR thanh toán' }}
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

    <!-- QR Payment Modal Overlay -->
    <div v-if="showQrModal" class="modal-overlay" @click.self="closeQrModal">
      <div class="modal-card">
        <!-- Close button -->
        <button class="modal-close-btn" @click="closeQrModal">&times;</button>

        <!-- State 1: Awaiting Payment (Showing QR code) -->
        <div v-if="!paymentSuccess && qrData" class="qr-payment-content">
          <div class="modal-header-section">
            <div class="modal-tag">Mona Pay Sandbox</div>
            <h2 class="font-serif modal-title">Quét mã QR để thanh toán</h2>
            <p class="modal-subtitle">
              Sử dụng <strong>Camera điện thoại</strong> hoặc <strong>Ứng dụng Ngân hàng</strong> để quét mã QR bên dưới
            </p>
          </div>

          <!-- Mode selector tab: Camera phone vs Banking app -->
          <div class="qr-mode-tabs">
            <button 
              type="button" 
              :class="['qr-mode-btn', qrMode === 'camera' ? 'active' : '']"
              @click="qrMode = 'camera'"
            >
              📷 Camera điện thoại
            </button>
            <button 
              type="button" 
              :class="['qr-mode-btn', qrMode === 'bank' ? 'active' : '']"
              @click="qrMode = 'bank'"
            >
              🏦 App Ngân hàng (VietQR)
            </button>
          </div>

          <div class="qr-display-box">
            <div class="qr-image-wrapper">
              <img 
                :src="currentQrUrl" 
                alt="Mã QR thanh toán Mona Pay" 
                class="qr-code-img"
              />
            </div>

            <div class="qr-instruction">
              <span v-if="qrMode === 'camera'">
                📱 Mở <strong>Camera trên điện thoại</strong>, đưa ống kính về mã QR để mở trang thanh toán Mona Pay.
              </span>
              <span v-else>
                🏦 Mở <strong>Ứng dụng Ngân hàng</strong> bất kỳ (VCB, MB, Techcombank, ...) để quét chuyển khoản.
              </span>
            </div>

            <div class="amount-badge">
              <span class="amount-label">Số tiền cần thanh toán:</span>
              <span class="amount-val">{{ order.totalAmount.toLocaleString() }} đ</span>
            </div>
          </div>

          <!-- Live payment status radar -->
          <div class="live-status-bar">
            <span class="radar-dot"></span>
            <span>Đang chờ bạn quét mã và thanh toán... Hệ thống sẽ tự động xác nhận ngay lập tức!</span>
          </div>

          <!-- Bank Account Details with Copy Buttons -->
          <div class="bank-details-box">
            <div class="bank-row">
              <span class="b-lbl">Ngân hàng:</span>
              <strong class="b-val">{{ qrData.bankName || 'Mona Pay Sandbox' }}</strong>
            </div>

            <div v-if="qrData.bankAccount" class="bank-row">
              <span class="b-lbl">Số tài khoản:</span>
              <div class="b-val-group">
                <code class="b-code">{{ qrData.bankAccount }}</code>
                <button class="btn-copy" @click="copyText(qrData.bankAccount, 'Số tài khoản')">Sao chép</button>
              </div>
            </div>

            <div v-if="qrData.accountName" class="bank-row">
              <span class="b-lbl">Chủ tài khoản:</span>
              <strong class="b-val">{{ qrData.accountName }}</strong>
            </div>

            <div class="bank-row">
              <span class="b-lbl">Nội dung CK:</span>
              <div class="b-val-group">
                <code class="b-code font-bold">{{ qrData.transferContent || ('ORDER-' + order.id) }}</code>
                <button class="btn-copy" @click="copyText(qrData.transferContent || ('ORDER-' + order.id), 'Nội dung chuyển khoản')">Sao chép</button>
              </div>
            </div>
          </div>

          <!-- Sandbox Direct Checkout Link -->
          <div class="sandbox-action-row">
            <a 
              v-if="qrData.checkoutUrl" 
              :href="qrData.checkoutUrl" 
              target="_blank" 
              class="btn btn-outline btn-sm-link"
            >
              ↗ Mở trang thanh toán Mona Pay (Sandbox Web)
            </a>
          </div>
        </div>

        <!-- State 2: Payment Succeeded! 🎉 -->
        <div v-else-if="paymentSuccess" class="payment-success-content">
          <div class="success-anim-badge">🎉</div>
          <h2 class="font-serif success-heading">Thanh toán thành công!</h2>
          <p class="success-lead">
            Hệ thống Mona Pay đã xác nhận thanh toán cho đơn hàng <strong>#{{ order.id }}</strong>.
          </p>

          <div class="invoice-sent-card">
            <div class="mail-icon">📧</div>
            <div class="mail-text">
              <div class="mail-title">Hóa đơn chi tiết đã được gửi về Gmail</div>
              <div class="mail-target">{{ userEmail || 'Email đặt hàng của bạn' }}</div>
              <small class="mail-hint">Vui lòng kiểm tra hộp thư đến (Inbox) hoặc thư mục Quảng cáo/Spam.</small>
            </div>
          </div>

          <div class="success-order-summary">
            <div class="s-row">
              <span>Mã giao dịch:</span>
              <code>{{ order.transactionId || 'MONAPAY-SUCCESS' }}</code>
            </div>
            <div class="s-row">
              <span>Số tiền đã thanh toán:</span>
              <strong>{{ order.totalAmount.toLocaleString() }} đ</strong>
            </div>
            <div class="s-row">
              <span>Trạng thái đơn hàng:</span>
              <span class="status-pill status-confirmed">✓ Đã xác nhận</span>
            </div>
          </div>

          <div class="success-actions">
            <button class="btn btn-primary btn-block" @click="closeQrModal">
              Xem đơn hàng đã thanh toán
            </button>
            <router-link :to="`/orders/${order.id}`" class="btn btn-outline btn-block">
              Trang chi tiết đơn hàng
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Notification for Copy -->
    <div v-if="toastMessage" class="toast-popup">
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import orderApi from '../api/orderApi'
import paymentApi from '../api/paymentApi'

const route = useRoute()
const router = useRouter()

const order = ref(null)
const loading = ref(true)
const error = ref('')
const paying = ref(false)

// QR Payment Modal & Realtime Polling State
const showQrModal = ref(false)
const qrData = ref(null)
const qrMode = ref('camera')
const polling = ref(false)
const paymentSuccess = ref(false)
const toastMessage = ref('')
let pollTimer = null

const currentQrUrl = computed(() => {
  if (!qrData.value) return ''
  if (qrMode.value === 'camera' && qrData.value.checkoutUrl) {
    return 'https://api.qrserver.com/v1/create-qr-code/?size=320x320&data=' + encodeURIComponent(qrData.value.checkoutUrl)
  }
  return qrData.value.qrImageUrl || getVietQrUrl(qrData.value)
})

onMounted(loadOrder)

onUnmounted(() => {
  stopPolling()
})

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

const userEmail = computed(() => {
  try {
    const token = localStorage.getItem('token')
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || ''
    }
  } catch (e) {}
  return ''
})

async function handlePayNow() {
  if (!order.value || paying.value) return
  paying.value = true
  try {
    const res = await paymentApi.create(order.value.id)
    qrData.value = res.data
    paymentSuccess.value = (res.data?.paymentStatus === 'PAID')
    showQrModal.value = true

    if (!paymentSuccess.value) {
      startPollingPayment()
    }
  } catch (err) {
    alert('Khởi tạo thanh toán Mona Pay thất bại: ' + (err.response?.data?.message || err.message))
  } finally {
    paying.value = false
  }
}

function startPollingPayment() {
  stopPolling()
  polling.value = true
  pollTimer = setInterval(async () => {
    try {
      const res = await paymentApi.status(order.value.id)
      if (res.data?.paymentStatus === 'PAID') {
        stopPolling()
        paymentSuccess.value = true
        order.value.paymentStatus = 'PAID'
        if (res.data.transactionId) order.value.transactionId = res.data.transactionId
        if (res.data.paidAt) order.value.paidAt = res.data.paidAt
        order.value.orderStatus = res.data.orderStatus || 'CONFIRMED'
      }
    } catch (e) {
      console.warn('Lỗi kiểm tra trạng thái thanh toán:', e)
    }
  }, 2500)
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  polling.value = false
}

function closeQrModal() {
  showQrModal.value = false
  stopPolling()
}

function getVietQrUrl(data) {
  if (!data || !data.bankAccount) {
    return 'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=' + encodeURIComponent(data?.checkoutUrl || window.location.href)
  }
  return `https://img.vietqr.io/image/${data.bankCode || 'ACB'}-${data.bankAccount}-compact2.png?amount=${order.value.totalAmount}&addInfo=${data.transferContent || ('ORDER-' + order.value.id)}&accountName=${encodeURIComponent(data.accountName || 'BOOKSTORE')}`
}

function copyText(text, label) {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    toastMessage.value = `Đã sao chép ${label}!`
    setTimeout(() => {
      toastMessage.value = ''
    }, 2500)
  }).catch(() => {
    prompt('Sao chép thông tin:', text)
  })
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
  padding: 18px 24px;
  border-radius: 10px;
  text-align: center;
}

.paid-alert-title {
  font-size: 1.15rem;
  font-weight: 700;
  margin-bottom: 4px;
}

.paid-alert-sub {
  font-size: 0.95rem;
  color: #047857;
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

/* ========================================================
   QR PAYMENT MODAL OVERLAY & POPUP STYLES
   ======================================================== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 23, 42, 0.65);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal-card {
  background: #ffffff;
  border-radius: 16px;
  max-width: 520px;
  width: 100%;
  max-height: 92vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  padding: 28px;
  animation: scaleUp 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes scaleUp {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.modal-close-btn {
  position: absolute;
  top: 16px;
  right: 18px;
  background: #f1f5f9;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 1.4rem;
  line-height: 1;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
}

.modal-close-btn:hover {
  background: #e2e8f0;
  color: #0f172a;
}

.modal-header-section {
  text-align: center;
  margin-bottom: 20px;
}

.modal-tag {
  display: inline-block;
  background: #ccfbf1;
  color: #0f766e;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 20px;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.modal-title {
  font-size: 1.5rem;
  color: #0f172a;
  margin: 0 0 6px 0;
}

.modal-subtitle {
  font-size: 0.9rem;
  color: #475569;
  margin: 0;
  line-height: 1.45;
}

/* Mode selector tab */
.qr-mode-tabs {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 10px;
  margin-bottom: 16px;
  gap: 4px;
}

.qr-mode-btn {
  flex: 1;
  padding: 8px 12px;
  font-size: 0.85rem;
  font-weight: 600;
  border: none;
  background: transparent;
  color: #64748b;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.qr-mode-btn.active {
  background: #ffffff;
  color: var(--tb-teal-700, #0f766e);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.qr-instruction {
  font-size: 0.82rem;
  color: #475569;
  text-align: center;
  margin: 6px 0 12px 0;
  line-height: 1.4;
  padding: 0 10px;
}

.qr-display-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px;
  margin-bottom: 16px;
}

.qr-image-wrapper {
  background: #ffffff;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-code-img {
  width: 220px;
  height: 220px;
  object-fit: contain;
  display: block;
}

.amount-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.92rem;
}

.amount-label {
  color: #64748b;
}

.amount-val {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--tb-teal-700, #0f766e);
}

/* Radar live status indicator */
.live-status-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 0.84rem;
  color: #166534;
  margin-bottom: 16px;
  line-height: 1.4;
}

.radar-dot {
  width: 10px;
  height: 10px;
  background: #22c55e;
  border-radius: 50%;
  flex-shrink: 0;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7);
  animation: radarPulse 1.6s infinite;
}

@keyframes radarPulse {
  0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 8px rgba(34, 197, 94, 0); }
  100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
}

/* Bank Details Table */
.bank-details-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.bank-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.88rem;
  gap: 12px;
}

.b-lbl {
  color: #64748b;
  flex-shrink: 0;
}

.b-val {
  color: #0f172a;
  text-align: right;
}

.b-val-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.b-code {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.88rem;
  color: #0f172a;
}

.btn-copy {
  background: #e2e8f0;
  border: none;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #334155;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-copy:hover {
  background: #cbd5e1;
  color: #0f172a;
}

.sandbox-action-row {
  text-align: center;
}

.btn-sm-link {
  font-size: 0.85rem;
  padding: 8px 14px;
  display: inline-block;
  color: var(--tb-teal-700, #0f766e);
  border-color: #99f6e4;
  background: #f0fdfa;
}

.btn-sm-link:hover {
  background: #ccfbf1;
}

/* Payment Success View in Modal */
.payment-success-content {
  text-align: center;
  padding: 12px 0;
}

.success-anim-badge {
  font-size: 3.5rem;
  margin-bottom: 12px;
  animation: bounce 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes bounce {
  0% { transform: scale(0.3); opacity: 0; }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); opacity: 1; }
}

.success-heading {
  font-size: 1.75rem;
  color: #059669;
  margin: 0 0 8px 0;
}

.success-lead {
  font-size: 0.95rem;
  color: #475569;
  margin: 0 0 20px 0;
}

.invoice-sent-card {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  text-align: left;
  margin-bottom: 20px;
}

.mail-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.mail-title {
  font-weight: 700;
  font-size: 0.95rem;
  color: #166534;
  margin-bottom: 2px;
}

.mail-target {
  font-weight: 600;
  font-size: 0.92rem;
  color: #0f172a;
  word-break: break-all;
}

.mail-hint {
  font-size: 0.78rem;
  color: #64748b;
  display: block;
  margin-top: 4px;
}

.success-order-summary {
  background: #f8fafc;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 0.88rem;
}

.s-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #475569;
}

.s-row strong {
  color: #0f172a;
}

.success-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* Toast Popup */
.toast-popup {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.9);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.88rem;
  font-weight: 500;
  z-index: 10000;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
  animation: toastFade 0.2s ease-out;
}

@keyframes toastFade {
  from { opacity: 0; transform: translate(-50%, 10px); }
  to { opacity: 1; transform: translate(-50%, 0); }
}
</style>

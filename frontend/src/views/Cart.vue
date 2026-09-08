<template>
  <div class="cart-page container">
    <div class="page-title-row">
      <h1 class="font-serif page-title">Giỏ Hàng Của Bạn</h1>
      <span v-if="cart.items.length > 0" class="cart-count-badge">
        {{ cart.itemCount }} cuốn sách
      </span>
    </div>

    <!-- Empty State -->
    <div v-if="!cart.items.length" class="empty-cart card-surface">
      <div class="empty-icon">🛒</div>
      <h2 class="font-serif">Giỏ hàng của bạn đang trống</h2>
      <p>Chưa có cuốn sách nào được chọn. Hãy khám phá kho sách đa dạng tại BookStore!</p>
      <router-link to="/products" class="btn btn-primary btn-lg">
        Khám phá sách ngay
      </router-link>
    </div>

    <!-- Active Cart Layout -->
    <div v-else class="cart-layout">
      <!-- Left Column: Items List -->
      <div class="cart-items-section">
        <!-- Items Table / Cards -->
        <div class="cart-items-list card-surface">
          <div class="cart-header-row">
            <span class="col-product">Sản phẩm</span>
            <span class="col-price">Đơn giá</span>
            <span class="col-qty">Số lượng</span>
            <span class="col-subtotal">Thành tiền</span>
            <span class="col-action"></span>
          </div>

          <div v-for="item in cart.items" :key="item.productId" class="cart-item-row">
            <!-- Product Info -->
            <div class="item-product">
              <router-link :to="`/products/${item.productId}`" class="item-cover-wrap">
                <img :src="item.imageUrl" :alt="item.productName" class="item-cover" />
              </router-link>
              <div class="item-details">
                <router-link :to="`/products/${item.productId}`" class="item-name font-serif">
                  {{ item.productName }}
                </router-link>
                <span v-if="item.availableStock < 5" class="low-stock-alert">
                  Chỉ còn {{ item.availableStock }} cuốn trong kho!
                </span>
              </div>
            </div>

            <!-- Price -->
            <div class="item-price">
              <span>{{ item.price.toLocaleString() }} đ</span>
            </div>

            <!-- Qty Stepper -->
            <div class="item-qty">
              <div class="stepper">
                <button 
                  class="step-btn" 
                  :disabled="item.quantity <= 1" 
                  @click="updateQty(item, item.quantity - 1)"
                >-</button>
                <input 
                  type="number" 
                  min="1" 
                  :max="item.availableStock" 
                  v-model.number="item.quantity"
                  @change="cart.updateQuantity(item.productId, item.quantity)" 
                  class="step-input"
                />
                <button 
                  class="step-btn" 
                  :disabled="item.quantity >= item.availableStock" 
                  @click="updateQty(item, item.quantity + 1)"
                >+</button>
              </div>
            </div>

            <!-- Subtotal -->
            <div class="item-subtotal">
              <strong>{{ item.subtotal.toLocaleString() }} đ</strong>
            </div>

            <!-- Remove Button -->
            <div class="item-remove">
              <button 
                class="btn-trash" 
                title="Xóa cuốn sách này" 
                @click="cart.remove(item.productId)"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- Bottom Actions -->
        <div class="cart-bottom-links">
          <router-link to="/products" class="btn btn-outline">
            ← Tiếp tục chọn sách
          </router-link>
          <button class="btn btn-outline btn-clear-cart" @click="confirmClear">
            Xóa toàn bộ giỏ hàng
          </button>
        </div>
      </div>

      <!-- Right Column: Summary Box -->
      <div class="cart-summary-section">
        <div class="summary-card card-surface">
          <h3 class="font-serif summary-title">Tóm Tắt Đơn Hàng</h3>

          <div class="summary-line">
            <span>Tạm tính ({{ cart.itemCount }} cuốn):</span>
            <strong>{{ cart.total.toLocaleString() }} đ</strong>
          </div>

          <div class="summary-line">
            <span>Phí vận chuyển:</span>
            <span class="free-badge">Miễn phí</span>
          </div>

          <div class="summary-divider"></div>

          <div class="total-line">
            <span>Tổng thanh toán:</span>
            <div class="total-amount-wrap">
              <span class="total-num">{{ cart.total.toLocaleString() }} đ</span>
            </div>
          </div>

          <router-link to="/checkout" class="btn btn-primary btn-lg btn-checkout">
            <span>Tiến hành thanh toán</span>
            <span>&rarr;</span>
          </router-link>

          <div class="security-badges">
            <div class="sec-item">📦 Giao hàng toàn quốc</div>
            <div class="sec-item">💵 Thanh toán khi nhận hàng (COD)</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../stores/cart'

const cart = useCartStore()

onMounted(() => {
  cart.fetch()
})

function updateQty(item, newQty) {
  if (newQty < 1 || newQty > item.availableStock) return
  item.quantity = newQty
  cart.updateQuantity(item.productId, newQty)
}

function confirmClear() {
  if (confirm('Bạn có chắc chắn muốn xóa toàn bộ sách trong giỏ hàng?')) {
    cart.clear()
  }
}
</script>

<style scoped>
.cart-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 1.8rem;
  color: var(--tb-slate-900);
}

.cart-count-badge {
  color: var(--tb-slate-500);
  font-size: 0.95rem;
}

/* Empty State */
.empty-cart {
  padding: 60px 20px;
  text-align: center;
  background-color: #ffffff;
  max-width: 600px;
  margin: 20px auto;
}

.empty-icon {
  font-size: 3.5rem;
  margin-bottom: 16px;
}

.empty-cart h2 {
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.empty-cart p {
  color: var(--tb-slate-500);
  margin-bottom: 24px;
  line-height: 1.6;
}

/* 2-Column Cart Layout */
.cart-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 28px;
  align-items: start;
}

/* Left Column */
.cart-items-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* Cart Items Table */
.cart-items-list {
  background-color: #ffffff;
  padding: 0 20px;
}

.cart-header-row {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1.2fr 1fr 40px;
  padding: 14px 0;
  border-bottom: 1px solid var(--tb-border);
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--tb-slate-500);
  text-transform: uppercase;
}

.cart-item-row {
  display: grid;
  grid-template-columns: 2.5fr 1fr 1.2fr 1fr 40px;
  align-items: center;
  padding: 18px 0;
  border-bottom: 1px solid var(--tb-border);
}

.cart-item-row:last-child {
  border-bottom: none;
}

.item-product {
  display: flex;
  gap: 14px;
  align-items: center;
}

.item-cover-wrap {
  width: 60px;
  height: 80px;
  flex-shrink: 0;
  background-color: #f1f5f9;
  border-radius: var(--tb-radius-sm);
  overflow: hidden;
}

.item-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  box-shadow: var(--tb-shadow-sm);
}

.item-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--tb-slate-900);
  line-height: 1.3;
}

.item-name:hover {
  color: var(--tb-teal-600);
}

.low-stock-alert {
  font-size: 0.75rem;
  color: var(--tb-red-600);
  font-weight: 600;
}

.item-price {
  font-size: 0.92rem;
  color: var(--tb-slate-700);
}

.item-qty .stepper {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--tb-border);
  border-radius: var(--tb-radius-sm);
  background-color: #ffffff;
}

.step-btn {
  width: 28px;
  height: 32px;
  border: none;
  background-color: var(--tb-slate-100);
  cursor: pointer;
  font-weight: 700;
  color: var(--tb-slate-700);
}

.step-input {
  width: 36px !important;
  height: 32px;
  text-align: center;
  border: none !important;
  font-weight: 700;
  padding: 0 !important;
  box-shadow: none !important;
}

.item-subtotal {
  font-size: 1.05rem;
  color: var(--tb-teal-700);
}

.btn-trash {
  background: none;
  border: none;
  color: var(--tb-slate-400);
  cursor: pointer;
  padding: 4px;
  border-radius: var(--tb-radius-sm);
  transition: color var(--tb-transition-fast);
}

.btn-trash:hover {
  color: var(--tb-red-600);
}

.cart-bottom-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-clear-cart {
  color: var(--tb-red-600);
}

.btn-clear-cart:hover {
  background-color: var(--tb-red-50);
}

/* Right Column: Summary Card */
.summary-card {
  padding: 24px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border-top: 4px solid var(--tb-teal-600);
}

.summary-title {
  font-size: 1.3rem;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--tb-border);
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.92rem;
  color: var(--tb-slate-700);
}

.free-badge {
  color: var(--tb-teal-700);
  font-weight: 700;
  background-color: var(--tb-teal-50);
  padding: 2px 6px;
  border-radius: var(--tb-radius-sm);
}

.summary-divider {
  height: 1px;
  background-color: var(--tb-border);
}

.total-line {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.total-line span {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--tb-slate-900);
}

.total-amount-wrap {
  text-align: right;
}

.total-num {
  font-size: 1.6rem;
  font-weight: 900;
  color: var(--tb-teal-700);
  display: block;
}

.btn-checkout {
  width: 100%;
  padding: 14px;
  font-size: 1.05rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.security-badges {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid var(--tb-border);
  font-size: 0.8rem;
  color: var(--tb-slate-500);
}

/* Responsive */
@media (max-width: 900px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .cart-header-row {
    display: none;
  }
  .cart-item-row {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>

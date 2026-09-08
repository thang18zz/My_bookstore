<template>
  <div class="product-detail-page container">
    <!-- Breadcrumb -->
    <nav class="breadcrumb">
      <router-link to="/products">Trang chủ</router-link>
      <span class="bc-sep">/</span>
      <router-link to="/products">Cửa hàng</router-link>
      <span class="bc-sep">/</span>
      <span v-if="product" class="bc-current">{{ product.name }}</span>
    </nav>

    <!-- Loading skeleton -->
    <div v-if="!product" class="loading-state card-surface">
      <div class="spinner"></div>
      <p>Đang tải thông tin sách...</p>
    </div>

    <!-- Main Product Detail -->
    <div v-else class="detail-layout">
      <!-- Left Column: Book Cover & Guarantees -->
      <div class="detail-left">
        <div class="cover-card card-surface">
          <div class="cover-image-container">
            <img 
              :src="product.imageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'" 
              :alt="product.name" 
              class="book-hero-cover"
            />
          </div>
        </div>

        <!-- BookStore Guarantee Box -->
        <div class="guarantee-box card-surface">
          <div class="guarantee-item">
            <span class="g-icon">🛡️</span>
            <div>
              <strong>100% Sách Chính Hãng</strong>
              <p>Chất lượng in ấn chuẩn nhà xuất bản</p>
            </div>
          </div>
          <div class="guarantee-item">
            <span class="g-icon">📦</span>
            <div>
              <strong>Đóng Gói Cẩn Thận</strong>
              <p>Bảo vệ bìa và gáy sách khi vận chuyển</p>
            </div>
          </div>
          <div class="guarantee-item">
            <span class="g-icon">🚚</span>
            <div>
              <strong>Giao Hàng Toàn Quốc</strong>
              <p>Vận chuyển nhanh chóng đến tận nơi</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column: Meta, Purchase Box, Description -->
      <div class="detail-right">
        <div class="product-header">
          <span v-if="product.categoryName" class="category-badge">{{ product.categoryName }}</span>
          <h1 class="book-title font-serif">{{ product.name }}</h1>

          <!-- Real Rating & Reviews Bar -->
          <div class="rating-bar">
            <template v-if="reviews.length > 0">
              <span class="stars">{{ '★'.repeat(Math.round(avgRating || 5)) }}</span>
              <span class="score">{{ avgRating }} / 5.0</span>
              <span class="divider">|</span>
              <a href="#reviews-section" class="review-count">{{ reviews.length }} đánh giá</a>
            </template>
            <template v-else>
              <span class="no-rating-label">Chưa có đánh giá</span>
              <span class="divider">|</span>
              <a href="#reviews-section" class="review-count">Hãy là người đầu tiên đánh giá</a>
            </template>
          </div>
        </div>

        <!-- Price & Cart Action Card -->
        <div class="purchase-box card-surface">
          <div class="price-header">
            <span class="current-price">{{ product.price.toLocaleString() }} đ</span>
          </div>

          <!-- Stock availability -->
          <div class="stock-status">
            <span v-if="product.inStock" class="status-pill in-stock">
              ✓ Còn {{ product.stockQuantity }} cuốn trong kho
            </span>
            <span v-else class="status-pill out-of-stock">
              ✕ Tạm thời hết hàng
            </span>
          </div>

          <!-- Quantity and Actions -->
          <div class="actions-row">
            <div class="qty-stepper">
              <button 
                class="stepper-btn" 
                :disabled="qty <= 1 || !product.inStock" 
                @click="qty--"
              >-</button>
              <input 
                type="number" 
                v-model.number="qty" 
                min="1" 
                :max="product.stockQuantity || 1" 
                class="qty-input"
              />
              <button 
                class="stepper-btn" 
                :disabled="!product.inStock || qty >= product.stockQuantity" 
                @click="qty++"
              >+</button>
            </div>

            <button 
              :disabled="!product.inStock || isAdding" 
              class="btn btn-primary btn-lg btn-buy" 
              @click="addToCart"
            >
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="9" cy="21" r="1"></circle>
                <circle cx="20" cy="21" r="1"></circle>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
              </svg>
              <span>{{ isAdding ? 'Đang thêm...' : 'Thêm vào giỏ hàng' }}</span>
            </button>

            <button 
              class="btn btn-outline btn-lg btn-wishlist" 
              :title="wishlist.isWishlisted(product.id) ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'"
              @click="toggleWishlist"
            >
              <svg viewBox="0 0 24 24" width="20" height="20" :fill="wishlist.isWishlisted(product.id) ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
              <span>{{ wishlist.isWishlisted(product.id) ? 'Đã thích' : 'Yêu thích' }}</span>
            </button>
          </div>
        </div>

        <!-- Book Description Section -->
        <div class="description-card card-surface">
          <h3 class="font-serif desc-heading">Giới Thiệu Nội Dung</h3>
          <div class="desc-content">
            <p>{{ product.description || 'Chưa có mô tả chi tiết cho cuốn sách này.' }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Customer Reviews Section (UC7.1.1 / UC7.1.2) -->
    <section id="reviews-section" class="reviews-section card-surface">
      <div class="reviews-header">
        <div>
          <h2 class="font-serif">Đánh Giá Của Độc Giả</h2>
          <p class="subtitle">Nhận xét từ độc giả về cuốn sách này</p>
        </div>
      </div>

      <!-- Real Reviews Summary -->
      <div v-if="reviews.length > 0" class="reviews-summary-bar">
        <div class="big-rating">
          <span class="big-num">{{ avgRating }}</span>
          <div class="big-stars">{{ '★'.repeat(Math.round(avgRating)) }}</div>
          <span class="total-reviews">Dựa trên {{ reviews.length }} lượt đánh giá</span>
        </div>
      </div>

      <!-- Reviews List -->
      <div class="reviews-list">
        <div v-if="reviews.length === 0" class="no-reviews">
          <p>Chưa có đánh giá nào cho cuốn sách này. Hãy là người đầu tiên chia sẻ cảm nhận của bạn!</p>
        </div>

        <div v-for="r in reviews" :key="r.id" class="review-card">
          <div class="reviewer-avatar">
            {{ (r.userName || 'B').charAt(0).toUpperCase() }}
          </div>
          <div class="review-body">
            <div class="reviewer-top">
              <strong>{{ r.userName || 'Độc giả' }}</strong>
              <span class="review-date">{{ formatDate(r.createdAt) }}</span>
            </div>
            <div class="review-stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</div>
            <p class="review-text">{{ r.comment }}</p>
          </div>
        </div>
      </div>

      <!-- Submit Review Form -->
      <div class="review-form-wrap">
        <h3 class="font-serif">Viết Đánh Giá Của Bạn</h3>
        
        <form v-if="auth.isLoggedIn" class="review-form" @submit.prevent="submitReview">
          <div class="form-rating-row">
            <label>Đánh giá số sao:</label>
            <select v-model.number="newReview.rating" class="rating-select">
              <option :value="5">★★★★★ (5 sao - Rất hài lòng)</option>
              <option :value="4">★★★★☆ (4 sao - Hài lòng)</option>
              <option :value="3">★★★☆☆ (3 sao - Bình thường)</option>
              <option :value="2">★★☆☆☆ (2 sao - Tạm được)</option>
              <option :value="1">★☆☆☆☆ (1 sao - Chưa hài lòng)</option>
            </select>
          </div>

          <div class="form-comment-row">
            <textarea 
              v-model="newReview.comment" 
              placeholder="Chia sẻ cảm nhận của bạn về cuốn sách này..." 
              rows="4" 
              required
            ></textarea>
          </div>

          <button type="submit" class="btn btn-primary" :disabled="submittingReview">
            {{ submittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
          </button>
        </form>

        <div v-else class="login-prompt">
          <p>Bạn cần <router-link to="/login">đăng nhập</router-link> để gửi đánh giá cho cuốn sách này.</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import productApi from '../api/productApi'
import reviewApi from '../api/reviewApi'
import { useCartStore } from '../stores/cart'
import { useWishlistStore } from '../stores/wishlist'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const reviews = ref([])
const qty = ref(1)
const isAdding = ref(false)
const submittingReview = ref(false)
const newReview = reactive({ rating: 5, comment: '' })

const cart = useCartStore()
const wishlist = useWishlistStore()
const auth = useAuthStore()

const avgRating = computed(() => {
  if (!reviews.value.length) return null
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return (sum / reviews.value.length).toFixed(1)
})

onMounted(load)

async function load() {
  try {
    const pRes = await productApi.detail(route.params.id)
    product.value = pRes.data
    const rRes = await reviewApi.forProduct(route.params.id)
    reviews.value = rRes.data.content
    if (auth.isLoggedIn) {
      await wishlist.fetch()
    }
  } catch (e) {
    console.error('Error loading product details:', e)
  }
}

async function addToCart() {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  isAdding.value = true
  try {
    await cart.add(product.value.id, qty.value)
    alert(`Đã thêm ${qty.value} cuốn "${product.value.name}" vào giỏ hàng!`)
  } catch (e) {
    alert('Không thể thêm vào giỏ hàng: ' + (e.response?.data?.message || e.message))
  } finally {
    isAdding.value = false
  }
}

async function toggleWishlist() {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    if (wishlist.isWishlisted(product.value.id)) {
      await wishlist.remove(product.value.id)
    } else {
      await wishlist.add(product.value.id)
    }
  } catch (e) {
    console.error('Wishlist error:', e)
  }
}

async function submitReview() {
  submittingReview.value = true
  try {
    await reviewApi.add({ productId: product.value.id, ...newReview })
    newReview.comment = ''
    alert('Cảm ơn bạn đã gửi đánh giá!')
    await load()
  } catch (e) {
    alert('Lỗi gửi đánh giá: ' + (e.response?.data?.message || e.message))
  } finally {
    submittingReview.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return isNaN(d.getTime()) ? dateStr : d.toLocaleDateString('vi-VN')
}
</script>

<style scoped>
.product-detail-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Breadcrumbs */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.88rem;
  color: var(--tb-slate-500);
}

.bc-sep {
  color: var(--tb-slate-400);
}

.bc-current {
  color: var(--tb-slate-800);
  font-weight: 600;
  max-width: 400px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Loading */
.loading-state {
  padding: 60px;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--tb-border);
  border-top-color: var(--tb-teal-600);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 2-Column Detail Layout */
.detail-layout {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 36px;
  align-items: start;
}

/* Left Column */
.detail-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cover-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #ffffff;
}

.cover-image-container {
  width: 260px;
  height: 360px;
  background-color: #f8faf9;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--tb-radius-md);
}

.book-hero-cover {
  max-width: 90%;
  max-height: 94%;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: var(--tb-book-shadow-hover);
}

/* Guarantees Box */
.guarantee-box {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background-color: #ffffff;
}

.guarantee-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.g-icon {
  font-size: 1.4rem;
  line-height: 1;
}

.guarantee-item strong {
  font-size: 0.88rem;
  color: var(--tb-slate-900);
  display: block;
}

.guarantee-item p {
  font-size: 0.78rem;
  color: var(--tb-slate-500);
  margin: 0;
}

/* Right Column */
.detail-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-badge {
  font-size: 0.8rem;
  color: var(--tb-teal-700);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.book-title {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--tb-slate-900);
  line-height: 1.25;
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 0.9rem;
}

.stars {
  color: var(--tb-amber-500);
  letter-spacing: 1px;
}

.score {
  font-weight: 800;
  color: var(--tb-slate-800);
}

.no-rating-label {
  color: var(--tb-slate-500);
  font-style: italic;
}

.review-count {
  color: var(--tb-teal-700);
  text-decoration: underline;
  cursor: pointer;
}

/* Purchase Box */
.purchase-box {
  padding: 24px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 18px;
  border-top: 4px solid var(--tb-teal-600);
}

.current-price {
  font-size: 2.2rem;
  font-weight: 900;
  color: var(--tb-teal-700);
}

.status-pill {
  display: inline-block;
  font-size: 0.85rem;
  font-weight: 700;
  padding: 6px 12px;
  border-radius: var(--tb-radius-sm);
}

.status-pill.in-stock {
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-700);
  border: 1px solid var(--tb-teal-200);
}

.status-pill.out-of-stock {
  background-color: var(--tb-red-50);
  color: var(--tb-red-600);
  border: 1px solid var(--tb-red-100);
}

/* Actions Row */
.actions-row {
  display: flex;
  gap: 12px;
}

.qty-stepper {
  display: flex;
  align-items: center;
  border: 1px solid var(--tb-border);
  border-radius: var(--tb-radius-md);
  overflow: hidden;
  background-color: #ffffff;
}

.stepper-btn {
  width: 38px;
  height: 48px;
  border: none;
  background-color: var(--tb-slate-100);
  font-size: 1.2rem;
  font-weight: 700;
  cursor: pointer;
  color: var(--tb-slate-700);
  transition: background-color var(--tb-transition-fast);
}

.stepper-btn:hover:not(:disabled) {
  background-color: var(--tb-slate-200);
}

.qty-input {
  width: 50px !important;
  height: 48px;
  text-align: center;
  border: none !important;
  font-weight: 700;
  font-size: 1rem !important;
  padding: 0 !important;
  box-shadow: none !important;
}

.btn-buy {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-wishlist {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Description Card */
.description-card {
  padding: 24px;
  background-color: #ffffff;
}

.desc-heading {
  font-size: 1.3rem;
  color: var(--tb-slate-900);
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--tb-teal-100);
}

.desc-content p {
  line-height: 1.8;
  font-size: 0.98rem;
  color: var(--tb-slate-700);
  white-space: pre-line;
}

/* Reviews Section */
.reviews-section {
  padding: 36px;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.reviews-header h2 {
  font-size: 1.6rem;
  margin-bottom: 4px;
}

.subtitle {
  color: var(--tb-slate-500);
  font-size: 0.9rem;
}

.reviews-summary-bar {
  background-color: var(--tb-teal-50);
  border: 1px solid var(--tb-teal-200);
  border-radius: var(--tb-radius-lg);
  padding: 18px 24px;
  display: flex;
  align-items: center;
}

.big-rating {
  display: flex;
  align-items: center;
  gap: 14px;
}

.big-num {
  font-size: 2.5rem;
  font-weight: 900;
  color: var(--tb-teal-800);
  line-height: 1;
}

.big-stars {
  color: var(--tb-amber-500);
  font-size: 1.2rem;
  letter-spacing: 2px;
}

.total-reviews {
  font-size: 0.85rem;
  color: var(--tb-slate-500);
}

.no-reviews {
  padding: 24px;
  background-color: var(--tb-slate-50);
  border-radius: var(--tb-radius-md);
  color: var(--tb-slate-500);
  font-style: italic;
}

/* Review Cards */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-card {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--tb-border);
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--tb-teal-700);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  flex-shrink: 0;
}

.review-body {
  flex: 1;
}

.reviewer-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.review-date {
  font-size: 0.75rem;
  color: var(--tb-slate-400);
}

.review-stars {
  color: var(--tb-amber-500);
  font-size: 0.95rem;
  margin-bottom: 6px;
}

.review-text {
  font-size: 0.92rem;
  color: var(--tb-slate-700);
  line-height: 1.6;
}

/* Review Form */
.review-form-wrap {
  border-top: 1px solid var(--tb-border);
  padding-top: 24px;
}

.review-form-wrap h3 {
  font-size: 1.25rem;
  margin-bottom: 16px;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 600px;
}

.form-rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-select {
  max-width: 260px;
}

.login-prompt {
  background-color: var(--tb-slate-50);
  padding: 16px;
  border-radius: var(--tb-radius-md);
  font-size: 0.9rem;
  color: var(--tb-slate-600);
}

@media (max-width: 860px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .cover-image-container {
    width: 220px;
    height: 300px;
  }
  .actions-row {
    flex-wrap: wrap;
  }
}
</style>

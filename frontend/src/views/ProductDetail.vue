<template>
  <div v-if="product" class="detail-container">
    <div class="breadcrumb">
      <router-link to="/products">← Quay lại danh sách</router-link>
      <span>/</span>
      <span>{{ product.categoryName }}</span>
    </div>

    <div class="product-showcase">
      <div class="gallery">
        <img :src="product.imageUrl || fallbackImage" :alt="product.name" class="main-img" />
      </div>

      <div class="info">
        <span class="category-tag">{{ product.categoryName }}</span>
        <h1 class="title">{{ product.name }}</h1>

        <div class="status-row">
          <span :class="['stock-badge', product.inStock ? 'in' : 'out']">
            {{ product.inStock ? `Còn ${product.stockQuantity} cuốn trong kho` : 'Tạm hết hàng' }}
          </span>
          <span v-if="reviews.length" class="review-count">
            ⭐ {{ averageRating }} / 5 ({{ reviews.length }} đánh giá)
          </span>
        </div>

        <div class="price-box">
          <span class="price">{{ formatPrice(product.price) }}</span>
          <span class="tax-note">Đã bao gồm VAT</span>
        </div>

        <div class="desc-box">
          <h3>Mô tả cuốn sách</h3>
          <p>{{ product.description || 'Chưa có mô tả chi tiết cho sản phẩm này.' }}</p>
        </div>

        <div class="purchase-actions">
          <div class="qty-picker">
            <button @click="qty > 1 && qty--">-</button>
            <input type="number" v-model.number="qty" min="1" :max="product.stockQuantity" />
            <button @click="qty < product.stockQuantity && qty++">+</button>
          </div>

          <button
            :disabled="!product.inStock || addingToCart"
            @click="addToCart"
            class="btn btn-add-cart"
          >
            {{ addingToCart ? 'Đang thêm...' : '🛒 Thêm vào giỏ hàng' }}
          </button>

          <button @click="toggleWishlist" class="btn btn-wishlist" :class="{ wishlisted: wishlist.isWishlisted(product.id) }">
            {{ wishlist.isWishlisted(product.id) ? '💔 Đã yêu thích' : '❤️ Yêu thích' }}
          </button>
        </div>

        <div v-if="successMsg" class="success-alert">
          {{ successMsg }}
        </div>
      </div>
    </div>

    <!-- Reviews Section -->
    <section class="reviews-section">
      <div class="reviews-header">
        <h2>Đánh giá từ độc giả ({{ reviews.length }})</h2>
      </div>

      <!-- Add Review Form -->
      <div v-if="auth.isLoggedIn" class="review-form-card">
        <h4>Chia sẻ cảm nhận của bạn về cuốn sách này:</h4>
        <form @submit.prevent="submitReview" class="review-form">
          <div class="rating-select">
            <label>Đánh giá sao:</label>
            <div class="star-rating">
              <span
                v-for="n in 5"
                :key="n"
                class="star"
                :class="{ active: newReview.rating >= n }"
                @click="newReview.rating = n"
              >
                ★
              </span>
            </div>
          </div>
          <textarea
            v-model="newReview.comment"
            rows="3"
            placeholder="Viết nhận xét ngắn về nội dung, chất lượng in ấn hoặc ấn tượng của bạn..."
            required
          ></textarea>
          <button type="submit" class="btn btn-submit-review" :disabled="submittingReview">
            {{ submittingReview ? 'Đang gửi...' : 'Gửi đánh giá' }}
          </button>
        </form>
      </div>

      <div v-else class="login-prompt">
        <router-link to="/login">Đăng nhập</router-link> để viết đánh giá cho cuốn sách này.
      </div>

      <!-- Reviews List -->
      <div v-if="reviews.length === 0" class="no-reviews">
        Chưa có đánh giá nào. Hãy là người đầu tiên để lại cảm nhận!
      </div>
      <div v-else class="reviews-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-top">
            <span class="review-user">👤 {{ r.userFullName || 'Độc giả' }}</span>
            <span class="stars">{{ '★'.repeat(r.rating) }}{{ '☆'.repeat(5 - r.rating) }}</span>
          </div>
          <p class="review-content">{{ r.comment }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import productApi from '../api/productApi'
import reviewApi from '../api/reviewApi'
import { useCartStore } from '../stores/cart'
import { useWishlistStore } from '../stores/wishlist'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const product = ref(null)
const reviews = ref([])
const qty = ref(1)
const newReview = reactive({ rating: 5, comment: '' })
const addingToCart = ref(false)
const submittingReview = ref(false)
const successMsg = ref('')

const fallbackImage = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'

const cart = useCartStore()
const wishlist = useWishlistStore()
const auth = useAuthStore()

const averageRating = computed(() => {
  if (!reviews.value.length) return '0.0'
  const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
  return (sum / reviews.value.length).toFixed(1)
})

onMounted(load)

async function load() {
  try {
    const pRes = await productApi.detail(route.params.id)
    product.value = pRes.data
    const rRes = await reviewApi.forProduct(route.params.id)
    reviews.value = rRes.data.content || []
    if (auth.isLoggedIn) {
      await wishlist.fetch()
    }
  } catch (e) {
    console.error('Failed to load product detail', e)
  }
}

async function addToCart() {
  addingToCart.value = true
  try {
    await cart.add(product.value.id, qty.value)
    successMsg.value = `✓ Đã thêm ${qty.value} cuốn vào giỏ hàng thành công!`
    setTimeout(() => { successMsg.value = '' }, 3500)
  } catch (e) {
    alert(e.response?.data?.message || 'Không thể thêm vào giỏ hàng')
  } finally {
    addingToCart.value = false
  }
}

async function toggleWishlist() {
  if (!auth.isLoggedIn) {
    alert('Vui lòng đăng nhập để lưu sách yêu thích!')
    return
  }
  if (wishlist.isWishlisted(product.value.id)) {
    await wishlist.remove(product.value.id)
  } else {
    await wishlist.add(product.value.id)
  }
}

async function submitReview() {
  submittingReview.value = true
  try {
    await reviewApi.add({ productId: product.value.id, ...newReview })
    newReview.comment = ''
    await load()
  } catch (e) {
    alert(e.response?.data?.message || 'Không thể gửi đánh giá')
  } finally {
    submittingReview.value = false
  }
}

function formatPrice(val) {
  if (val == null) return '0 đ'
  return Number(val).toLocaleString('vi-VN') + ' đ'
}
</script>

<style scoped>
.detail-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.breadcrumb {
  display: flex;
  gap: 8px;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.breadcrumb a {
  color: var(--primary);
  text-decoration: none;
  font-weight: 600;
}

.product-showcase {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 40px;
  background: white;
  padding: 36px;
  border-radius: 20px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
}

.gallery .main-img {
  width: 100%;
  height: 480px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: var(--shadow-md);
}

.info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-tag {
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #6366f1;
  letter-spacing: 0.05em;
}

.title {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.3;
}

.status-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stock-badge {
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 0.85rem;
  font-weight: 700;
}

.stock-badge.in {
  background: #ecfdf5;
  color: #059669;
}

.stock-badge.out {
  background: #fef2f2;
  color: #dc2626;
}

.review-count {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.price-box {
  background: #f8fafc;
  padding: 16px 20px;
  border-radius: 12px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price {
  font-size: 2rem;
  font-weight: 800;
  color: #dc2626;
}

.tax-note {
  font-size: 0.85rem;
  color: #94a3b8;
}

.desc-box h3 {
  font-size: 1.1rem;
  margin: 0 0 8px;
}

.desc-box p {
  color: #475569;
  line-height: 1.7;
  margin: 0;
}

.purchase-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding-top: 12px;
}

.qty-picker {
  display: flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
  background: white;
}

.qty-picker button {
  background: #f8fafc;
  border: none;
  width: 38px;
  height: 42px;
  font-size: 1.1rem;
  cursor: pointer;
  font-weight: bold;
}

.qty-picker input {
  width: 50px;
  height: 42px;
  border: none;
  text-align: center;
  font-size: 1rem;
  font-weight: 700;
  outline: none;
}

.btn-add-cart {
  background: linear-gradient(135deg, #4f46e5, #3b82f6);
  color: white;
  padding: 12px 24px;
  font-size: 1rem;
  font-weight: 700;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.btn-wishlist {
  background: white;
  border: 1px solid var(--border);
  color: #334155;
  padding: 12px 20px;
  font-weight: 600;
  border-radius: 10px;
  cursor: pointer;
}

.btn-wishlist.wishlisted {
  background: #fff1f2;
  border-color: #fecdd3;
  color: #e11d48;
}

.success-alert {
  background: #ecfdf5;
  color: #065f46;
  padding: 12px 16px;
  border-radius: 10px;
  font-weight: 600;
  border: 1px solid #a7f3d0;
}

.reviews-section {
  background: white;
  padding: 32px;
  border-radius: 20px;
  border: 1px solid var(--border);
}

.reviews-header h2 {
  margin: 0 0 20px;
  font-size: 1.4rem;
}

.review-form-card {
  background: #f8fafc;
  padding: 20px;
  border-radius: 14px;
  margin-bottom: 24px;
  border: 1px solid var(--border);
}

.review-form-card h4 {
  margin: 0 0 12px;
}

.review-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rating-select {
  display: flex;
  align-items: center;
  gap: 12px;
}

.star-rating {
  display: flex;
  gap: 4px;
}

.star {
  font-size: 1.5rem;
  color: #cbd5e1;
  cursor: pointer;
}

.star.active {
  color: #f59e0b;
}

.review-form textarea {
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #cbd5e1;
  font-family: inherit;
  outline: none;
}

.btn-submit-review {
  align-self: flex-start;
  background: #0f172a;
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

.login-prompt {
  background: #f8fafc;
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 24px;
  color: #64748b;
}

.login-prompt a {
  color: var(--primary);
  font-weight: 600;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
}

.review-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.review-user {
  font-weight: 700;
  color: #1e293b;
}

.stars {
  color: #f59e0b;
}

.review-content {
  margin: 0;
  color: #475569;
}

@media (max-width: 860px) {
  .product-showcase {
    grid-template-columns: 1fr;
  }
  .gallery .main-img {
    height: 350px;
  }
}
</style>

<template>
  <div class="wishlist-page container">
    <div class="page-title-row">
      <div>
        <h1 class="font-serif page-title">Danh Sách Yêu Thích</h1>
        <p class="page-sub">Những tựa sách bạn đã lưu để đọc hoặc mua sau tại BookStore</p>
      </div>
      <span v-if="wishlist.items.length > 0" class="wishlist-counter">
        {{ wishlist.items.length }} cuốn sách đã lưu
      </span>
    </div>

    <!-- Empty State -->
    <div v-if="!wishlist.items.length" class="empty-wishlist card-surface">
      <div class="empty-icon">❤️</div>
      <h2 class="font-serif">Chưa có cuốn sách nào trong danh sách</h2>
      <p>Nhấp vào biểu tượng trái tim ở bất kỳ cuốn sách nào bạn quan tâm để lưu lại vào đây.</p>
      <router-link to="/products" class="btn btn-primary btn-lg">
        Khám phá kho sách ngay
      </router-link>
    </div>

    <!-- Wishlist Grid -->
    <div v-else class="wishlist-grid">
      <div 
        v-for="item in wishlist.items" 
        :key="item.id" 
        class="wishlist-card card-surface"
      >
        <div class="card-cover-wrap">
          <router-link :to="`/products/${item.product.id}`">
            <img 
              :src="item.product.imageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=400&q=80'" 
              :alt="item.product.name" 
              class="card-cover" 
            />
          </router-link>
          <button 
            class="btn-remove-wishlist" 
            title="Xóa khỏi yêu thích" 
            @click="removeItem(item.product.id)"
          >
            ✕
          </button>
        </div>

        <div class="card-info">
          <h4 class="card-title font-serif">
            <router-link :to="`/products/${item.product.id}`" :title="item.product.name">
              {{ item.product.name }}
            </router-link>
          </h4>

          <div class="card-price-row">
            <span class="card-price">{{ item.product.price.toLocaleString() }} đ</span>
            <span :class="['card-stock', item.product.inStock ? 'in' : 'out']">
              {{ item.product.inStock ? 'Còn hàng' : 'Hết hàng' }}
            </span>
          </div>

          <div class="card-btn-row">
            <button 
              :disabled="!item.product.inStock || addingId === item.product.id"
              class="btn btn-primary btn-sm btn-move-cart"
              @click="moveToCart(item.product)"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="9" cy="21" r="1"></circle>
                <circle cx="20" cy="21" r="1"></circle>
                <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
              </svg>
              <span>{{ addingId === item.product.id ? 'Đang thêm...' : 'Thêm vào giỏ' }}</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useWishlistStore } from '../stores/wishlist'
import { useCartStore } from '../stores/cart'

const wishlist = useWishlistStore()
const cart = useCartStore()
const addingId = ref(null)

onMounted(() => {
  wishlist.fetch()
})

async function removeItem(productId) {
  await wishlist.remove(productId)
}

async function moveToCart(product) {
  addingId.value = product.id
  try {
    await cart.add(product.id, 1)
    alert(`Đã thêm "${product.name}" vào giỏ hàng!`)
  } catch (e) {
    alert('Không thể thêm vào giỏ hàng: ' + (e.response?.data?.message || e.message))
  } finally {
    addingId.value = null
  }
}
</script>

<style scoped>
.wishlist-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 12px;
}

.page-title {
  font-size: 1.8rem;
  color: var(--tb-slate-900);
  margin-bottom: 4px;
}

.page-sub {
  color: var(--tb-slate-500);
  font-size: 0.9rem;
}

.wishlist-counter {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--tb-teal-700);
  background-color: var(--tb-teal-50);
  padding: 4px 12px;
  border-radius: var(--tb-radius-full);
}

/* Empty State */
.empty-wishlist {
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

.empty-wishlist h2 {
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.empty-wishlist p {
  color: var(--tb-slate-500);
  margin-bottom: 24px;
  line-height: 1.6;
}

/* Grid */
.wishlist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.wishlist-card {
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  padding: 16px;
  transition: all var(--tb-transition-normal);
}

.wishlist-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--tb-shadow-lg);
}

.card-cover-wrap {
  position: relative;
  width: 100%;
  height: 240px;
  background-color: #f1f5f9;
  border-radius: var(--tb-radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  overflow: hidden;
}

.card-cover {
  width: 80%;
  height: 90%;
  object-fit: cover;
  border-radius: 3px;
  box-shadow: var(--tb-book-shadow);
}

.btn-remove-wishlist {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--tb-border);
  color: var(--tb-slate-500);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  transition: all var(--tb-transition-fast);
}

.btn-remove-wishlist:hover {
  background-color: var(--tb-red-50);
  color: var(--tb-red-600);
  border-color: var(--tb-red-100);
}

.card-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 0.98rem;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 10px;
  min-height: 2.8em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-title a {
  color: var(--tb-slate-900);
}

.card-title a:hover {
  color: var(--tb-teal-700);
}

.card-price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 14px;
}

.card-price {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--tb-teal-700);
}

.card-stock {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
}

.card-stock.in {
  color: var(--tb-teal-600);
}

.card-stock.out {
  color: var(--tb-red-600);
}

.card-btn-row {
  margin-top: auto;
}

.btn-move-cart {
  width: 100%;
}
</style>

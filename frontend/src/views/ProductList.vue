<template>
  <div class="product-list-page container">
    <!-- BookStore Hero Section -->
    <section v-if="!keyword && !categoryId" class="hero-section">
      <div class="hero-split-grid">
        <!-- Main Left Banner -->
        <div class="hero-banner main-banner">
          <div class="banner-content">
            <span class="hero-tag">Chào Mừng Đến Với BookStore</span>
            <h1 class="hero-title font-serif">Khám Phá Thế Giới Tri Thức Qua Từng Trang Sách</h1>
            <p class="hero-sub">
              Hàng ngàn đầu sách mới chọn lọc chất lượng cao từ các nhà xuất bản uy tín, giao hàng nhanh chóng đến tận tay bạn.
            </p>
            <div class="hero-cta-group">
              <a href="#book-catalog" class="btn btn-primary btn-lg">Khám phá sách ngay</a>
              <span class="hero-guarantee">✓ Đóng gói cẩn thận</span>
            </div>
          </div>
        </div>

        <!-- Right Side Stack Banners -->
        <div class="hero-side-banners">
          <div class="hero-banner sub-banner deals-banner">
            <div class="banner-content">
              <span class="mini-tag">💡 Chuyên Đề</span>
              <h3 class="font-serif">Tủ Sách Kinh Tế & Khởi Nghiệp</h3>
              <p>Nâng cao tư duy kinh doanh và kỹ năng quản trị hiện đại.</p>
              <a href="#book-catalog" class="banner-link" @click="selectCategoryByName('Kinh tế')">Xem ngay &rarr;</a>
            </div>
          </div>

          <div class="hero-banner sub-banner bestsellers-banner">
            <div class="banner-content">
              <span class="mini-tag">📖 Tinh Tuyển</span>
              <h3 class="font-serif">Tủ Sách Văn Học & Nghệ Thuật</h3>
              <p>Thưởng thức những tác phẩm văn học kinh điển và đương đại.</p>
              <a href="#book-catalog" class="banner-link" @click="selectCategoryByName('Văn học')">Khám phá &rarr;</a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- BookStore 4-Pillars / Perks Bar -->
    <section class="perks-bar card-surface">
      <div class="perk-item">
        <div class="perk-icon">📚</div>
        <div class="perk-text">
          <strong>Sách Chọn Lọc</strong>
          <span>Đa dạng thể loại từ các nhà xuất bản uy tín</span>
        </div>
      </div>
      <div class="perk-divider"></div>
      <div class="perk-item">
        <div class="perk-icon">💰</div>
        <div class="perk-text">
          <strong>Giá Cả Minh Bạch</strong>
          <span>Mức giá niêm yết rõ ràng, chất lượng đảm bảo</span>
        </div>
      </div>
      <div class="perk-divider"></div>
      <div class="perk-item">
        <div class="perk-icon">🚚</div>
        <div class="perk-text">
          <strong>Giao Hàng Toàn Quốc</strong>
          <span>Vận chuyển nhanh chóng đến mọi miền đất nước</span>
        </div>
      </div>
      <div class="perk-divider"></div>
      <div class="perk-item">
        <div class="perk-icon">⭐</div>
        <div class="perk-text">
          <strong>Hỗ Trợ Tận Tâm</strong>
          <span>Đội ngũ chăm sóc và tư vấn sách nhiệt tình</span>
        </div>
      </div>
    </section>

    <!-- Shop by Category Section -->
    <section v-if="!keyword" class="categories-showcase">
      <div class="section-title-wrap">
        <div>
          <h2 class="section-title font-serif">Khám Phá Theo Danh Mục</h2>
          <p class="section-subtitle">Lựa chọn sách theo sở thích và lĩnh vực bạn quan tâm</p>
        </div>
      </div>

      <div class="category-cards-grid">
        <div 
          :class="['category-card', { active: categoryId === null }]"
          @click="selectCategory(null)"
        >
          <div class="cat-icon-bg">📚</div>
          <h4>Tất cả danh mục</h4>
          <span class="cat-count">Toàn bộ sách</span>
        </div>

        <div 
          v-for="c in categories" 
          :key="c.id" 
          :class="['category-card', { active: categoryId === c.id }]"
          @click="selectCategory(c.id)"
        >
          <div class="cat-icon-bg">{{ getCategoryEmoji(c.name) }}</div>
          <h4>{{ c.name }}</h4>
          <span class="cat-count">Khám phá</span>
        </div>
      </div>
    </section>

    <!-- Catalog Anchor & Filter Toolbar -->
    <section id="book-catalog" class="catalog-section">
      <div class="catalog-header">
        <div class="catalog-title-group">
          <h2 class="font-serif catalog-heading">
            <span v-if="keyword">Kết quả tìm kiếm: "{{ keyword }}"</span>
            <span v-else-if="currentCategoryName">{{ currentCategoryName }}</span>
            <span v-else>Toàn Bộ Sách Tại BookStore</span>
          </h2>
          <span class="products-count">{{ products.length }} cuốn sách</span>
        </div>

        <!-- Filter & Sort Toolbar -->
        <div class="toolbar card-surface">
          <div class="filter-group">
            <div class="search-box">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
              <input 
                v-model="keyword" 
                placeholder="Lọc sách theo tên..." 
                @keyup.enter="applySearch" 
              />
              <button v-if="keyword" class="clear-btn" @click="clearKeyword">✕</button>
            </div>

            <div class="select-box">
              <label>Danh mục:</label>
              <select v-model="categoryId" @change="applyCategory">
                <option :value="null">Tất cả danh mục</option>
                <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
            </div>
          </div>

          <div class="sort-group">
            <label>Sắp xếp:</label>
            <select v-model="sort" @change="load(false)">
              <option value="newest">Mới nhất</option>
              <option value="price_asc">Giá: Thấp đến cao</option>
              <option value="price_desc">Giá: Cao đến thấp</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="products.length === 0 && !loading" class="empty-state card-surface">
        <div class="empty-icon">📖</div>
        <h3 class="font-serif">Không tìm thấy sách nào</h3>
        <p>Vui lòng thử tìm kiếm bằng từ khóa khác hoặc chọn một danh mục khác.</p>
        <button class="btn btn-primary" @click="resetFilters">Xem tất cả sách</button>
      </div>

      <!-- Book Cards Grid -->
      <div v-else class="book-grid">
        <div 
          v-for="p in products" 
          :key="p.id" 
          class="book-card card-surface"
        >
          <!-- Book Cover with 3D Spine Shadow -->
          <div class="cover-wrapper">
            <router-link :to="`/products/${p.id}`">
              <img 
                :src="p.imageUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=400&q=80'" 
                :alt="p.name" 
                class="book-cover"
                loading="lazy"
              />
            </router-link>

            <!-- Stock Status Pill (Backend inStock) -->
            <div class="cover-badges">
              <span v-if="!p.inStock" class="stock-tag out">Hết hàng</span>
              <span v-else class="stock-tag in">Còn hàng</span>
            </div>

            <!-- Wishlist Button -->
            <button 
              v-if="auth.isLoggedIn"
              :class="['btn-wishlist-quick', { active: wishlist.isWishlisted(p.id) }]"
              :title="wishlist.isWishlisted(p.id) ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'"
              @click.stop="toggleWishlist(p.id)"
            >
              <svg viewBox="0 0 24 24" width="18" height="18" :fill="wishlist.isWishlisted(p.id) ? '#ef4444' : 'none'" stroke="currentColor" stroke-width="2">
                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
              </svg>
            </button>
          </div>

          <!-- Book Metadata -->
          <div class="book-info">
            <h4 class="book-title font-serif">
              <router-link :to="`/products/${p.id}`" :title="p.name">
                {{ p.name }}
              </router-link>
            </h4>

            <!-- Real Price from Backend -->
            <div class="price-block">
              <span class="price-current">{{ p.price.toLocaleString() }} đ</span>
            </div>

            <!-- Card Actions -->
            <div class="card-action-row">
              <button 
                :disabled="!p.inStock || addingId === p.id"
                class="btn btn-primary btn-add-cart"
                @click="addToCart(p)"
              >
                <span v-if="addingId === p.id">Đang thêm...</span>
                <span v-else>
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="9" cy="21" r="1"></circle>
                    <circle cx="20" cy="21" r="1"></circle>
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                  </svg>
                  Thêm vào giỏ
                </span>
              </button>
              <router-link :to="`/products/${p.id}`" class="btn btn-outline btn-detail-link" title="Xem chi tiết">
                Chi tiết
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Load More Button -->
      <div v-if="hasMore" class="load-more-wrap">
        <button class="btn btn-secondary btn-lg" :disabled="loading" @click="loadMore">
          <span v-if="loading">Đang tải thêm sách...</span>
          <span v-else>Xem thêm sách khác ▾</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import productApi from '../api/productApi'
import { useCartStore } from '../stores/cart'
import { useWishlistStore } from '../stores/wishlist'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const wishlist = useWishlistStore()
const auth = useAuthStore()

const products = ref([])
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const sort = ref('newest')
const page = ref(0)
const hasMore = ref(false)
const loading = ref(false)
const addingId = ref(null)

const currentCategoryName = computed(() => {
  if (!categoryId.value) return null
  const found = categories.value.find(c => c.id === categoryId.value)
  return found ? found.name : null
})

onMounted(async () => {
  try {
    const catRes = await productApi.categories()
    categories.value = catRes.data
  } catch (e) {
    console.error('Error fetching categories:', e)
  }

  syncWithRoute()
  load()
})

watch(() => route.query, () => {
  syncWithRoute()
  load(false)
})

function syncWithRoute() {
  if (route.query.q) {
    keyword.value = route.query.q
    categoryId.value = null
  } else if (route.query.category) {
    categoryId.value = parseInt(route.query.category)
    keyword.value = ''
  }
}

async function load(append = false) {
  loading.value = true
  const params = { page: page.value, size: 12, sort: sort.value }
  let res
  try {
    if (keyword.value) {
      res = await productApi.search({ keyword: keyword.value, ...params })
    } else if (categoryId.value) {
      res = await productApi.byCategory(categoryId.value, params)
    } else {
      res = await productApi.list(params)
    }
    products.value = append ? [...products.value, ...res.data.content] : res.data.content
    hasMore.value = !res.data.last
  } catch (e) {
    console.error('Failed to load products:', e)
  } finally {
    loading.value = false
  }
}

function applySearch() {
  page.value = 0
  categoryId.value = null
  load(false)
}

function clearKeyword() {
  keyword.value = ''
  page.value = 0
  router.push('/products')
  load(false)
}

function selectCategory(id) {
  categoryId.value = id
  keyword.value = ''
  page.value = 0
  load(false)
}

function selectCategoryByName(namePart) {
  const found = categories.value.find(c => c.name.toLowerCase().includes(namePart.toLowerCase()))
  if (found) {
    selectCategory(found.id)
  }
}

function applyCategory() {
  page.value = 0
  keyword.value = ''
  load(false)
}

function resetFilters() {
  keyword.value = ''
  categoryId.value = null
  page.value = 0
  router.push('/products')
  load(false)
}

function loadMore() {
  page.value++
  load(true)
}

async function addToCart(product) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  addingId.value = product.id
  try {
    await cart.add(product.id, 1)
  } catch (e) {
    alert('Không thể thêm vào giỏ hàng: ' + (e.response?.data?.message || e.message))
  } finally {
    addingId.value = null
  }
}

async function toggleWishlist(productId) {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    if (wishlist.isWishlisted(productId)) {
      await wishlist.remove(productId)
    } else {
      await wishlist.add(productId)
    }
  } catch (e) {
    console.error('Wishlist toggle error:', e)
  }
}

function getCategoryEmoji(name) {
  const n = name.toLowerCase()
  if (n.includes('văn học') || n.includes('tiểu thuyết')) return '📖'
  if (n.includes('kinh tế') || n.includes('kinh doanh')) return '💡'
  if (n.includes('tâm lý') || n.includes('kỹ năng')) return '🧠'
  if (n.includes('khoa học') || n.includes('công nghệ')) return '🚀'
  if (n.includes('thiếu nhi')) return '🧸'
  if (n.includes('lịch sử')) return '🏛️'
  return '📚'
}
</script>

<style scoped>
.product-list-page {
  display: flex;
  flex-direction: column;
  gap: 36px;
}

/* --- Hero Section --- */
.hero-section {
  margin-top: -6px;
}

.hero-split-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
}

.hero-banner {
  border-radius: var(--tb-radius-xl);
  overflow: hidden;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 36px 40px;
  box-shadow: var(--tb-shadow-md);
}

.main-banner {
  background: linear-gradient(135deg, #005a4e 0%, #008575 60%, #0ba08f 100%);
  color: #ffffff;
  min-height: 280px;
}

.hero-tag {
  display: inline-block;
  background-color: var(--tb-teal-500);
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 4px 10px;
  border-radius: var(--tb-radius-full);
  margin-bottom: 12px;
}

.hero-title {
  color: #ffffff;
  font-size: 2.1rem;
  font-weight: 800;
  line-height: 1.25;
  margin-bottom: 14px;
}

.hero-sub {
  color: var(--tb-teal-100);
  font-size: 1.02rem;
  line-height: 1.6;
  max-width: 480px;
  margin-bottom: 24px;
}

.hero-cta-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-cta-group .btn-primary {
  background-color: #ffffff;
  color: var(--tb-teal-800);
  border-color: #ffffff;
}

.hero-cta-group .btn-primary:hover {
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-900);
}

.hero-guarantee {
  font-size: 0.82rem;
  color: var(--tb-teal-200);
  font-weight: 600;
}

/* Right Stack Banners */
.hero-side-banners {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.sub-banner {
  flex: 1;
  padding: 24px 28px;
}

.deals-banner {
  background: linear-gradient(135deg, #fef3c7 0%, #fed7aa 100%);
  border: 1px solid #fde68a;
  color: #78350f;
}

.deals-banner h3 {
  color: #92400e;
  font-size: 1.3rem;
  margin-bottom: 6px;
}

.deals-banner p {
  color: #b45309;
  font-size: 0.88rem;
  margin-bottom: 12px;
}

.bestsellers-banner {
  background: linear-gradient(135deg, #e0f2fe 0%, #eef8f6 100%);
  border: 1px solid #bae6fd;
  color: #0369a1;
}

.bestsellers-banner h3 {
  color: #075985;
  font-size: 1.3rem;
  margin-bottom: 6px;
}

.bestsellers-banner p {
  color: #0284c7;
  font-size: 0.88rem;
  margin-bottom: 12px;
}

.mini-tag {
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 4px;
  display: inline-block;
}

.banner-link {
  font-weight: 700;
  font-size: 0.88rem;
  text-decoration: underline;
}

/* --- Perks Bar --- */
.perks-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20px 24px;
  background-color: #ffffff;
}

.perk-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.perk-icon {
  font-size: 2rem;
  line-height: 1;
}

.perk-text {
  display: flex;
  flex-direction: column;
}

.perk-text strong {
  font-size: 0.95rem;
  color: var(--tb-slate-900);
}

.perk-text span {
  font-size: 0.8rem;
  color: var(--tb-slate-500);
}

.perk-divider {
  width: 1px;
  height: 36px;
  background-color: var(--tb-border);
}

/* --- Categories Showcase --- */
.section-title-wrap {
  margin-bottom: 18px;
}

.section-title {
  font-size: 1.6rem;
  color: var(--tb-slate-900);
  margin-bottom: 4px;
}

.section-subtitle {
  color: var(--tb-slate-500);
  font-size: 0.92rem;
}

.category-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}

.category-card {
  background-color: #ffffff;
  border: 1px solid var(--tb-border);
  border-radius: var(--tb-radius-lg);
  padding: 18px 14px;
  text-align: center;
  cursor: pointer;
  transition: all var(--tb-transition-fast);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-card:hover {
  border-color: var(--tb-teal-600);
  transform: translateY(-3px);
  box-shadow: var(--tb-shadow-md);
}

.category-card.active {
  background-color: var(--tb-teal-50);
  border-color: var(--tb-teal-600);
  border-width: 2px;
}

.cat-icon-bg {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--tb-slate-100);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-bottom: 10px;
}

.category-card.active .cat-icon-bg {
  background-color: var(--tb-teal-100);
}

.category-card h4 {
  font-size: 0.9rem;
  color: var(--tb-slate-800);
  margin-bottom: 4px;
}

.cat-count {
  font-size: 0.75rem;
  color: var(--tb-slate-400);
}

/* --- Catalog Section --- */
.catalog-section {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.catalog-header {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.catalog-title-group {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 10px;
}

.catalog-heading {
  font-size: 1.7rem;
  color: var(--tb-slate-900);
}

.products-count {
  font-size: 0.88rem;
  color: var(--tb-slate-500);
  font-weight: 600;
}

/* Toolbar */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background-color: #ffffff;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  min-width: 260px;
}

.search-box svg {
  position: absolute;
  left: 12px;
  color: var(--tb-slate-400);
}

.search-box input {
  padding-left: 36px !important;
  padding-right: 32px !important;
  font-size: 0.88rem !important;
}

.clear-btn {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  color: var(--tb-slate-400);
  cursor: pointer;
  font-size: 0.9rem;
}

.select-box, .sort-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.select-box label, .sort-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--tb-slate-600);
  white-space: nowrap;
}

.select-box select, .sort-group select {
  font-size: 0.88rem;
  padding: 8px 12px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background-color: #ffffff;
}

.empty-icon {
  font-size: 3.5rem;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 1.4rem;
  color: var(--tb-slate-800);
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--tb-slate-500);
  font-size: 0.95rem;
  margin-bottom: 20px;
}

/* --- Book Cards Grid --- */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 22px;
}

.book-card {
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  padding: 16px;
  transition: all var(--tb-transition-normal);
  position: relative;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--tb-shadow-xl);
}

/* Book Cover with 3D Spine Shadow */
.cover-wrapper {
  position: relative;
  width: 100%;
  height: 250px;
  background-color: #f1f5f9;
  border-radius: var(--tb-radius-md);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}

.book-cover {
  width: 82%;
  height: 92%;
  object-fit: cover;
  border-radius: 3px;
  box-shadow: var(--tb-book-shadow);
  transition: transform var(--tb-transition-normal), box-shadow var(--tb-transition-normal);
}

.book-card:hover .book-cover {
  transform: scale(1.03) rotate(-1deg);
  box-shadow: var(--tb-book-shadow-hover);
}

/* Cover Badges */
.cover-badges {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stock-tag {
  font-size: 0.68rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: var(--tb-radius-sm);
  text-transform: uppercase;
}

.stock-tag.in {
  background-color: var(--tb-teal-600);
  color: #ffffff;
}

.stock-tag.out {
  background-color: var(--tb-red-600);
  color: #ffffff;
}

/* Wishlist Button */
.btn-wishlist-quick {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--tb-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--tb-slate-600);
  transition: all var(--tb-transition-fast);
  backdrop-filter: blur(4px);
}

.btn-wishlist-quick:hover {
  background-color: #ffffff;
  color: var(--tb-red-600);
  transform: scale(1.1);
}

.btn-wishlist-quick.active {
  color: var(--tb-red-600);
}

/* Book Metadata */
.book-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.book-title {
  font-size: 1rem;
  font-weight: 700;
  line-height: 1.4;
  margin-bottom: 12px;
  min-height: 2.8em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-title a {
  color: var(--tb-slate-900);
  text-decoration: none;
}

.book-title a:hover {
  color: var(--tb-teal-700);
}

/* Price Block */
.price-block {
  margin-top: auto;
  padding: 10px 0 14px 0;
  border-top: 1px solid var(--tb-border);
}

.price-current {
  font-size: 1.3rem;
  font-weight: 800;
  color: var(--tb-teal-700);
}

/* Action Buttons */
.card-action-row {
  display: flex;
  gap: 8px;
}

.btn-add-cart {
  flex: 1;
  padding: 8px 10px;
  font-size: 0.84rem;
}

.btn-detail-link {
  padding: 8px 12px;
  font-size: 0.84rem;
}

/* Load More */
.load-more-wrap {
  text-align: center;
  padding: 20px 0;
}

/* Responsive adjustments */
@media (max-width: 960px) {
  .hero-split-grid {
    grid-template-columns: 1fr;
  }
  .perks-bar {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }
  .perk-divider {
    display: none;
  }
}

@media (max-width: 640px) {
  .perks-bar {
    grid-template-columns: 1fr;
  }
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .cover-wrapper {
    height: 180px;
  }
  .card-action-row {
    flex-direction: column;
  }
}
</style>

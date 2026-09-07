<template>
  <div class="product-page">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1>Khám Phá Thế Giới Tri Thức</h1>
        <p>Hàng ngàn đầu sách chọn lọc từ các tác giả uy tín, đồng hành cùng Trợ lý ảo AI thông minh.</p>
        <div class="search-box">
          <input
            v-model="keyword"
            placeholder="Tìm kiếm sách theo tiêu đề, tác giả..."
            @keyup.enter="applySearch"
          />
          <button @click="applySearch" class="btn btn-search">🔍 Tìm kiếm</button>
        </div>
      </div>
    </div>

    <!-- Category Pills & Sorting Toolbar -->
    <div class="filter-bar">
      <div class="category-pills">
        <button
          :class="['pill', { active: categoryId === null }]"
          @click="selectCategory(null)"
        >
          Tất cả ({{ totalCount }})
        </button>
        <button
          v-for="c in categories"
          :key="c.id"
          :class="['pill', { active: categoryId === c.id }]"
          @click="selectCategory(c.id)"
        >
          {{ c.name }}
        </button>
      </div>

      <div class="sort-selector">
        <label>Sắp xếp:</label>
        <select v-model="sort" @change="load(false)">
          <option value="newest">Mới nhất</option>
          <option value="price_asc">Giá tăng dần</option>
          <option value="price_desc">Giá giảm dần</option>
        </select>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="products.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📖</div>
      <h3>Không tìm thấy cuốn sách nào</h3>
      <p>Thử tìm kiếm với từ khóa khác hoặc xóa bộ lọc danh mục.</p>
      <button @click="resetFilters" class="btn btn-primary">Xem tất cả sách</button>
    </div>

    <!-- Products Grid -->
    <div v-else class="grid">
      <router-link
        v-for="p in products"
        :key="p.id"
        :to="`/products/${p.id}`"
        class="card"
      >
        <div class="card-img-wrapper">
          <img :src="p.imageUrl || fallbackImage" :alt="p.name" loading="lazy" />
          <span v-if="!p.inStock" class="badge-stock out">Hết hàng</span>
          <span v-else class="badge-stock in">Còn hàng</span>
        </div>

        <div class="card-body">
          <span class="card-cat">{{ p.categoryName }}</span>
          <h4 class="card-title" :title="p.name">{{ p.name }}</h4>
          <div class="card-bottom">
            <span class="card-price">{{ formatPrice(p.price) }}</span>
            <span class="card-btn">Chi tiết →</span>
          </div>
        </div>
      </router-link>
    </div>

    <!-- Load More -->
    <div v-if="hasMore" class="load-more">
      <button @click="page++; load(true)" class="btn btn-outline-dark">
        {{ loading ? 'Đang tải...' : 'Xem thêm sách khác' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import productApi from '../api/productApi'

const products = ref([])
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const sort = ref('newest')
const page = ref(0)
const hasMore = ref(false)
const loading = ref(false)
const totalCount = ref(0)

const fallbackImage = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80'

onMounted(async () => {
  try {
    const catRes = await productApi.categories()
    categories.value = catRes.data || []
  } catch (e) {
    console.error('Failed to load categories', e)
  }
  load()
})

async function load(append = false) {
  loading.value = true
  try {
    const params = { page: page.value, size: 12, sort: sort.value }
    let res
    if (keyword.value) {
      res = await productApi.search({ keyword: keyword.value, ...params })
    } else if (categoryId.value) {
      res = await productApi.byCategory(categoryId.value, params)
    } else {
      res = await productApi.list(params)
    }
    const data = res.data
    products.value = append ? [...products.value, ...(data.content || [])] : (data.content || [])
    hasMore.value = !data.last
    if (!append) totalCount.value = data.totalElements || products.value.length
  } catch (e) {
    console.error('Failed to load products', e)
  } finally {
    loading.value = false
  }
}

function selectCategory(id) {
  categoryId.value = id
  keyword.value = ''
  page.value = 0
  load(false)
}

function applySearch() {
  page.value = 0
  categoryId.value = null
  load(false)
}

function resetFilters() {
  keyword.value = ''
  categoryId.value = null
  page.value = 0
  load(false)
}

function formatPrice(val) {
  if (val == null) return '0 đ'
  return Number(val).toLocaleString('vi-VN') + ' đ'
}
</script>

<style scoped>
.product-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.hero-banner {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #1e293b 100%);
  color: white;
  padding: 44px 32px;
  border-radius: 20px;
  box-shadow: 0 10px 25px -5px rgba(30, 27, 75, 0.4);
  position: relative;
  overflow: hidden;
}

.hero-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.25) 0%, rgba(0,0,0,0) 70%);
  border-radius: 50%;
}

.hero-content {
  position: relative;
  max-width: 700px;
}

.hero-banner h1 {
  font-size: 2.2rem;
  font-weight: 800;
  margin: 0 0 10px;
  letter-spacing: -0.02em;
  background: linear-gradient(to right, #ffffff, #93c5fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-banner p {
  color: #cbd5e1;
  font-size: 1.05rem;
  margin: 0 0 24px;
}

.search-box {
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  padding: 6px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.search-box input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: white;
  padding: 10px 16px;
  font-size: 1rem;
}

.search-box input::placeholder {
  color: #94a3b8;
}

.btn-search {
  background: #38bdf8;
  color: #0f172a;
  font-weight: 700;
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
}

.btn-search:hover {
  background: #7dd3fc;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  background: white;
  padding: 16px 20px;
  border-radius: 14px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border);
}

.category-pills {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  background: #f1f5f9;
  color: #475569;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.pill:hover {
  background: #e2e8f0;
  color: #0f172a;
}

.pill.active {
  background: #4f46e5;
  color: white;
  box-shadow: 0 4px 10px rgba(79, 70, 229, 0.3);
}

.sort-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

.sort-selector select {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid var(--border);
  background: white;
  font-weight: 600;
  color: #1e293b;
  outline: none;
  cursor: pointer;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 24px;
}

.card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid var(--border);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: #cbd5e1;
}

.card-img-wrapper {
  position: relative;
  width: 100%;
  height: 250px;
  background: #f1f5f9;
  overflow: hidden;
}

.card-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.card:hover .card-img-wrapper img {
  transform: scale(1.05);
}

.badge-stock {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 700;
  backdrop-filter: blur(6px);
}

.badge-stock.in {
  background: rgba(16, 185, 129, 0.9);
  color: white;
}

.badge-stock.out {
  background: rgba(239, 68, 68, 0.9);
  color: white;
}

.card-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-cat {
  font-size: 0.78rem;
  font-weight: 700;
  text-transform: uppercase;
  color: #6366f1;
  margin-bottom: 6px;
  letter-spacing: 0.04em;
}

.card-title {
  margin: 0 0 12px;
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 2.8em;
}

.card-bottom {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.card-price {
  font-size: 1.15rem;
  font-weight: 800;
  color: #dc2626;
}

.card-btn {
  font-size: 0.85rem;
  font-weight: 700;
  color: #4f46e5;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 16px;
  border: 1px dashed var(--border);
}

.empty-icon {
  font-size: 3.5rem;
  margin-bottom: 12px;
}

.empty-state h3 {
  margin: 0 0 8px;
  color: #1e293b;
}

.empty-state p {
  color: #64748b;
  margin: 0 0 20px;
}

.load-more {
  text-align: center;
  margin-top: 16px;
}

.btn-outline-dark {
  background: white;
  border: 1px solid var(--border);
  color: #1e293b;
  padding: 10px 24px;
  font-weight: 600;
  cursor: pointer;
  border-radius: 8px;
}

.btn-outline-dark:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}
</style>

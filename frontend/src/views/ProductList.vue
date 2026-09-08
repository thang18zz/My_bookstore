<template>
  <div>
    <div class="toolbar">
      <input v-model="keyword" placeholder="Tìm sách theo tên..." @keyup.enter="applySearch" />
      <select v-model="categoryId" @change="load">
        <option :value="null">Tất cả danh mục</option>
        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <select v-model="sort" @change="load">
        <option value="newest">Mới nhất</option>
        <option value="price_asc">Giá tăng dần</option>
        <option value="price_desc">Giá giảm dần</option>
      </select>
    </div>

    <div class="grid">
      <router-link v-for="p in products" :key="p.id" :to="`/products/${p.id}`" class="card">
        <img :src="p.imageUrl" alt="" />
        <h4>{{ p.name }}</h4>
        <p>{{ p.price.toLocaleString() }} đ</p>
        <p v-if="!p.inStock" class="out-of-stock">Hết hàng</p>
      </router-link>
    </div>

    <button v-if="hasMore" @click="page++, load(true)">Xem thêm</button>
  </div>
</template>

<script setup>
// UC2.1.1 (list), UC2.1.2/UC2.2.2 (by category), UC2.2.1 (search), UC2.2.3 (sort)
import { onMounted, ref } from 'vue'
import productApi from '../api/productApi'

const products = ref([])
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const sort = ref('newest')
const page = ref(0)
const hasMore = ref(false)

onMounted(async () => {
  categories.value = (await productApi.categories()).data
  load()
})

async function load(append = false) {
  const params = { page: page.value, size: 12, sort: sort.value }
  let res
  if (keyword.value) {
    res = await productApi.search({ keyword: keyword.value, ...params })
  } else if (categoryId.value) {
    res = await productApi.byCategory(categoryId.value, params)
  } else {
    res = await productApi.list(params)
  }
  products.value = append ? [...products.value, ...res.data.content] : res.data.content
  hasMore.value = !res.data.last
}

function applySearch() {
  page.value = 0
  categoryId.value = null
  load()
}
</script>

<style scoped>
.toolbar { display: flex; gap: 8px; margin-bottom: 16px; }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.card { border: 1px solid #eee; border-radius: 8px; padding: 8px; text-decoration: none; color: inherit; }
.card img { width: 100%; height: 140px; object-fit: cover; border-radius: 4px; }
.out-of-stock { color: #dc2626; font-size: 0.85rem; }
</style>

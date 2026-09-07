<template>
  <div v-if="stats">
    <h2>Dashboard</h2> <!-- UC12.1.1 -->
    <div class="stat-grid">
      <div class="stat-card"><h3>{{ stats.totalProducts }}</h3><p>Sản phẩm</p></div> <!-- UC12.1.2 -->
      <div class="stat-card"><h3>{{ stats.totalCustomers }}</h3><p>Khách hàng</p></div> <!-- UC12.1.3 -->
      <div class="stat-card"><h3>{{ stats.totalOrders }}</h3><p>Đơn hàng</p></div> <!-- UC12.1.4 -->
      <div class="stat-card"><h3>{{ stats.revenue.toLocaleString() }} đ</h3><p>Doanh thu</p></div> <!-- UC12.1.5 -->
    </div>

    <h3>Sản phẩm bán chạy</h3> <!-- UC12.2.2 / UC12.2.4 / UC12.2.5 -->
    <div v-for="b in bestSellers" :key="b.productId">{{ b.productName }} - {{ b.unitsSold }} đã bán</div>

    <h3>Doanh thu theo danh mục</h3> <!-- UC12.2.3 -->
    <div v-for="(v, k) in byCategory" :key="k">{{ k }}: {{ v.toLocaleString() }} đ</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import adminStatsApi from '../../api/adminStatsApi'

const stats = ref(null)
const bestSellers = ref([])
const byCategory = ref({})

onMounted(async () => {
  stats.value = (await adminStatsApi.dashboard()).data
  bestSellers.value = (await adminStatsApi.bestSellers(5)).data
  byCategory.value = (await adminStatsApi.byCategory()).data
})
</script>

<style scoped>
.stat-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
.stat-card { border: 1px solid #eee; border-radius: 8px; padding: 16px; text-align: center; }
</style>

<template>
  <div>
    <h2>Lịch sử đơn hàng</h2> <!-- UC5.1.1 -->
    <div v-for="o in orders" :key="o.id" class="order-row">
      <router-link :to="`/orders/${o.id}`">Đơn #{{ o.id }}</router-link>
      <span class="status">{{ o.status }}</span>
      <span>{{ o.totalAmount.toLocaleString() }} đ</span>
    </div>
    <p v-if="!orders.length">Bạn chưa có đơn hàng nào.</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import orderApi from '../api/orderApi'

const orders = ref([])
onMounted(async () => { orders.value = (await orderApi.myOrders()).data })
</script>

<style scoped>
.order-row { display: flex; gap: 16px; padding: 8px 0; border-bottom: 1px solid #eee; }
.status { font-weight: bold; }
</style>

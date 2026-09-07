<template>
  <div v-if="order">
    <h2>Đơn hàng #{{ order.id }}</h2>
    <p>Trạng thái: <strong>{{ order.status }}</strong></p> <!-- UC5.1.3 -->
    <p>Giao tới: {{ order.shippingAddress }}</p>
    <div v-for="i in order.items" :key="i.productId">
      {{ i.productName }} x{{ i.quantity }} - {{ i.priceAtPurchase.toLocaleString() }} đ
    </div>
    <strong>Tổng cộng: {{ order.totalAmount.toLocaleString() }} đ</strong>

    <div v-if="['PENDING','CONFIRMED'].includes(order.status)">
      <button @click="cancelOrder">Hủy đơn hàng</button> <!-- UC5.1.4 -->
    </div>
  </div>
</template>

<script setup>
// UC5.1.2 (detail) / UC5.1.3 (status = tracking) / UC5.1.4 (cancel)
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import orderApi from '../api/orderApi'

const route = useRoute()
const order = ref(null)

onMounted(load)
async function load() { order.value = (await orderApi.detail(route.params.id)).data }
async function cancelOrder() { order.value = (await orderApi.cancel(route.params.id)).data }
</script>

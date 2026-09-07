<template>
  <div>
    <h2>Giỏ hàng</h2> <!-- UC3.1.1 -->
    <div v-for="item in cart.items" :key="item.productId" class="cart-row">
      <img :src="item.imageUrl" />
      <span>{{ item.productName }}</span>
      <input type="number" min="1" :max="item.availableStock" v-model.number="item.quantity"
             @change="cart.updateQuantity(item.productId, item.quantity)" /> <!-- UC3.1.3 -->
      <span>{{ item.subtotal.toLocaleString() }} đ</span>
      <button @click="cart.remove(item.productId)">Xóa</button> <!-- UC3.1.4 -->
    </div>

    <p v-if="!cart.items.length">Giỏ hàng trống.</p>

    <div class="cart-footer">
      <strong>Tổng cộng: {{ cart.total.toLocaleString() }} đ</strong> <!-- UC3.1.6 -->
      <button v-if="cart.items.length" @click="cart.clear()">Xóa toàn bộ</button> <!-- UC3.1.5 -->
      <router-link v-if="cart.items.length" to="/checkout"><button>Thanh toán</button></router-link>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useCartStore } from '../stores/cart'

const cart = useCartStore()
onMounted(() => cart.fetch())
</script>

<style scoped>
.cart-row { display: flex; align-items: center; gap: 12px; padding: 8px 0; border-bottom: 1px solid #eee; }
.cart-row img { width: 48px; height: 48px; object-fit: cover; }
.cart-footer { margin-top: 16px; display: flex; align-items: center; gap: 16px; }
</style>

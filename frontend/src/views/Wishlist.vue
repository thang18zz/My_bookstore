<template>
  <div>
    <h2>Danh sách yêu thích</h2> <!-- UC2.3.1 -->
    <div class="grid">
      <div v-for="item in wishlist.items" :key="item.id" class="card">
        <router-link :to="`/products/${item.product.id}`">{{ item.product.name }}</router-link>
        <button @click="wishlist.remove(item.product.id)">Xóa</button> <!-- UC2.3.3 -->
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useWishlistStore } from '../stores/wishlist'

const wishlist = useWishlistStore()
onMounted(() => wishlist.fetch())
</script>

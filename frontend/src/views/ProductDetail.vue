<template>
  <div v-if="product">
    <div class="detail">
      <img :src="product.imageUrl" alt="" />
      <div>
        <h2>{{ product.name }}</h2>
        <p class="category">{{ product.categoryName }}</p>
        <p class="price">{{ product.price.toLocaleString() }} đ</p> <!-- UC2.1.4 -->
        <p :class="product.inStock ? 'in-stock' : 'out-of-stock'"> <!-- UC2.1.5 -->
          {{ product.inStock ? `Còn ${product.stockQuantity} sản phẩm` : 'Hết hàng' }}
        </p>
        <p>{{ product.description }}</p> <!-- UC2.1.3 -->

        <div class="actions">
          <input type="number" v-model.number="qty" min="1" />
          <button :disabled="!product.inStock" @click="addToCart">Thêm vào giỏ</button>
          <button @click="toggleWishlist">
            {{ wishlist.isWishlisted(product.id) ? '💔 Bỏ yêu thích' : '❤️ Yêu thích' }}
          </button>
        </div>
      </div>
    </div>

    <section>
      <h3>Đánh giá sản phẩm</h3> <!-- UC7.1.1 -->
      <div v-for="r in reviews" :key="r.id" class="review">
        <strong>{{ '★'.repeat(r.rating) }}</strong> - {{ r.comment }}
      </div>

      <form v-if="auth.isLoggedIn" @submit.prevent="submitReview"> <!-- UC7.1.2 -->
        <select v-model.number="newReview.rating">
          <option v-for="n in [1,2,3,4,5]" :key="n" :value="n">{{ n }} sao</option>
        </select>
        <input v-model="newReview.comment" placeholder="Viết đánh giá..." />
        <button type="submit">Gửi đánh giá</button>
      </form>
    </section>
  </div>
</template>

<script setup>
// UC2.1.3 / UC2.1.4 / UC2.1.5 + UC2.3.2/3 (wishlist) + UC3.1.2 (add to cart) + UC7.1.1/7.1.2 (reviews)
import { onMounted, reactive, ref } from 'vue'
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

const cart = useCartStore()
const wishlist = useWishlistStore()
const auth = useAuthStore()

onMounted(load)

async function load() {
  product.value = (await productApi.detail(route.params.id)).data
  reviews.value = (await reviewApi.forProduct(route.params.id)).data.content
  if (auth.isLoggedIn) await wishlist.fetch()
}

async function addToCart() {
  await cart.add(product.value.id, qty.value)
}

async function toggleWishlist() {
  if (wishlist.isWishlisted(product.value.id)) {
    await wishlist.remove(product.value.id)  // UC2.3.3
  } else {
    await wishlist.add(product.value.id)     // UC2.3.2
  }
}

async function submitReview() {
  await reviewApi.add({ productId: product.value.id, ...newReview })
  newReview.comment = ''
  await load()
}
</script>

<style scoped>
.detail { display: flex; gap: 24px; }
.detail img { width: 300px; height: 380px; object-fit: cover; border-radius: 8px; }
.price { font-size: 1.4rem; font-weight: bold; }
.in-stock { color: #16a34a; }
.out-of-stock { color: #dc2626; }
.actions { display: flex; gap: 8px; margin-top: 12px; }
.review { border-bottom: 1px solid #eee; padding: 8px 0; }
</style>

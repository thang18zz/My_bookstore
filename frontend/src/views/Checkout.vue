<template>
  <div>
    <h2>Thanh toán</h2> <!-- UC4.1.1 -->

    <section>
      <h3>Chọn địa chỉ giao hàng</h3> <!-- UC4.1.2 -->
      <label v-for="a in addresses" :key="a.id" class="addr-option">
        <input type="radio" v-model="addressId" :value="a.id" />
        {{ a.recipientName }} - {{ a.line1 }}, {{ a.city }}
      </label>
      <router-link to="/addresses">+ Thêm địa chỉ mới</router-link>
    </section>

    <section v-if="preview">
      <h3>Kiểm tra đơn hàng</h3> <!-- UC4.1.3 -->
      <div v-for="i in preview.cart.items" :key="i.productId">
        {{ i.productName }} x{{ i.quantity }} = {{ i.subtotal.toLocaleString() }} đ
      </div>
      <p>Giao tới: {{ preview.addressSummary }}</p>
      <strong>Tổng cộng: {{ preview.cart.total.toLocaleString() }} đ</strong>
    </section>

    <p v-if="error" class="error">{{ error }}</p>
    <button :disabled="!addressId" @click="confirm">Xác nhận đặt hàng</button> <!-- UC4.1.4 -->
  </div>
</template>

<script setup>
// Theme 4 end-to-end: preview -> confirm -> creates Order (Theme 5) and clears Cart (Theme 3)
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import addressApi from '../api/addressApi'
import orderApi from '../api/orderApi'
import { useCartStore } from '../stores/cart'

const addresses = ref([])
const addressId = ref(null)
const preview = ref(null)
const error = ref('')
const router = useRouter()
const cart = useCartStore()

onMounted(async () => {
  addresses.value = (await addressApi.list()).data
  const def = addresses.value.find((a) => a.default)
  if (def) addressId.value = def.id
})

watch(addressId, async (id) => {
  if (!id) return
  preview.value = (await orderApi.preview(id)).data // UC4.1.3
})

async function confirm() {
  try {
    const res = await orderApi.checkout(addressId.value) // UC4.1.4 -> UC4.2.1-4 on the backend
    await cart.fetch()
    router.push(`/orders/${res.data.id}`)
  } catch (e) {
    error.value = e.message || 'Đặt hàng thất bại'
  }
}
</script>

<style scoped>
.addr-option { display: block; margin: 4px 0; }
</style>

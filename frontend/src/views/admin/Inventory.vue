<template>
  <div>
    <h2>Quản lý tồn kho</h2> <!-- UC11.1.1 -->
    <button @click="showLowStock = !showLowStock">
      {{ showLowStock ? 'Xem tất cả' : 'Sản phẩm sắp hết hàng' }} <!-- UC11.1.4 -->
    </button>

    <table>
      <tr v-for="i in items" :key="i.productId" :class="{ low: i.lowStock }">
        <td>{{ i.productName }}</td>
        <td>{{ i.stockQuantity }}</td>
        <td>
          <input type="number" v-model.number="restockAmounts[i.productId]" placeholder="SL nhập" />
          <button @click="restock(i.productId)">Nhập hàng</button> <!-- UC11.1.5 -->
        </td>
      </tr>
    </table>
  </div>
</template>

<script setup>
// UC11.1.2 (tra cứu) = table filter/search on this same list; UC11.1.3 handled by adminInventoryApi.setQuantity
import { onMounted, reactive, ref, watch } from 'vue'
import adminInventoryApi from '../../api/adminInventoryApi'

const items = ref([])
const showLowStock = ref(false)
const restockAmounts = reactive({})

watch(showLowStock, load)
onMounted(load)

async function load() {
  const res = showLowStock.value
      ? await adminInventoryApi.lowStock({ page: 0, size: 50 })
      : await adminInventoryApi.list({ page: 0, size: 50 })
  items.value = res.data.content
}

async function restock(productId) {
  await adminInventoryApi.restock(productId, restockAmounts[productId] || 0)
  restockAmounts[productId] = null
  await load()
}
</script>

<style scoped>
.low { background: #fef2f2; }
</style>

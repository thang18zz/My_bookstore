<template>
  <div>
    <h2>Quản lý đơn hàng</h2> <!-- UC5.2.1 -->
    <select v-model="statusFilter" @change="load"> <!-- UC5.2.5 tra cứu theo trạng thái -->
      <option value="">Tất cả</option>
      <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
    </select>

    <table>
      <tr v-for="o in orders" :key="o.id">
        <td>#{{ o.id }}</td>
        <td>{{ o.totalAmount.toLocaleString() }} đ</td>
        <td>
          <select v-model="o.status" @change="updateStatus(o)"> <!-- UC5.2.3 / UC5.2.4 -->
            <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
          </select>
        </td>
      </tr>
    </table>
  </div>
</template>

<script setup>
// UC5.2.2 (chi tiết) is one click away via OrderDetail.vue reused at /orders/:id for admins too
import { onMounted, ref } from 'vue'
import adminOrderApi from '../../api/adminOrderApi'

const statuses = ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'CANCELLED']
const orders = ref([])
const statusFilter = ref('')

onMounted(load)
async function load() {
  orders.value = (await adminOrderApi.list({ page: 0, size: 50, status: statusFilter.value || undefined })).data.content
}
async function updateStatus(o) { await adminOrderApi.updateStatus(o.id, o.status) }
</script>

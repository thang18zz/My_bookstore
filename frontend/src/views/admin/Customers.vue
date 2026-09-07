<template>
  <div>
    <h2>Quản lý khách hàng</h2> <!-- UC10.1.1 -->
    <input v-model="q" placeholder="Tra cứu khách hàng..." @keyup.enter="load" /> <!-- UC10.1.6 -->
    <table>
      <tr v-for="c in customers" :key="c.id">
        <td>{{ c.fullName }}</td>
        <td>{{ c.email }}</td>
        <td>{{ c.phone }}</td>
        <td><button @click="remove(c.id)">Xóa</button></td> <!-- UC10.1.5 -->
      </tr>
    </table>
  </div>
</template>

<script setup>
// UC10.1.2 (view) is the row itself here; UC10.1.3/10.1.4 reuse the same RegisterRequest shape as Auth
import { onMounted, ref } from 'vue'
import adminCustomerApi from '../../api/adminCustomerApi'

const customers = ref([])
const q = ref('')

onMounted(load)
async function load() { customers.value = (await adminCustomerApi.list({ page: 0, size: 50, q: q.value })).data.content }
async function remove(id) { await adminCustomerApi.remove(id); await load() }
</script>

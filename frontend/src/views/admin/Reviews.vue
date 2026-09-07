<template>
  <div>
    <h2>Quản lý đánh giá</h2> <!-- UC7.2.1 -->
    <table>
      <tr v-for="r in reviews" :key="r.id">
        <td>{{ '★'.repeat(r.rating) }}</td>
        <td>{{ r.comment }}</td>
        <td>{{ r.status }}</td>
        <td>
          <button @click="hide(r.id)" :disabled="r.status === 'HIDDEN'">Ẩn (vi phạm)</button> <!-- UC7.2.4 -->
          <button @click="remove(r.id)">Xóa</button> <!-- UC7.2.3 -->
        </td>
      </tr>
    </table>
  </div>
</template>

<script setup>
// UC7.2.2 (chi tiết) is inline in this same table for simplicity
import { onMounted, ref } from 'vue'
import adminReviewApi from '../../api/adminReviewApi'

const reviews = ref([])
onMounted(load)
async function load() { reviews.value = (await adminReviewApi.list({ page: 0, size: 50 })).data.content }
async function hide(id) { await adminReviewApi.hide(id); await load() }
async function remove(id) { await adminReviewApi.remove(id); await load() }
</script>

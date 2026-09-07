<template>
  <div>
    <h2>Quản lý sản phẩm</h2> <!-- UC8.1.1 -->
    <button @click="openNew">+ Thêm sản phẩm</button> <!-- UC8.1.3 -->

    <table>
      <tr v-for="p in products" :key="p.id">
        <td>{{ p.name }}</td>
        <td>{{ p.price.toLocaleString() }} đ</td>
        <td>{{ p.stockQuantity }}</td>
        <td>
          <button @click="edit(p)">Sửa</button> <!-- UC8.1.4 / UC8.1.7 -->
          <button @click="remove(p.id)">Xóa</button> <!-- UC8.1.5 -->
        </td>
      </tr>
    </table>

    <form v-if="showForm" @submit.prevent="save">
      <input v-model="form.name" placeholder="Tên sách" required />
      <textarea v-model="form.description" placeholder="Mô tả"></textarea>
      <input v-model.number="form.price" type="number" placeholder="Giá" required /> <!-- UC8.1.6 -->
      <input v-model.number="form.stockQuantity" type="number" placeholder="Tồn kho" required />
      <input v-model="form.imageUrl" placeholder="Ảnh (URL)" />
      <select v-model.number="form.categoryId" required>
        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <button type="submit">Lưu</button>
    </form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import adminProductApi from '../../api/adminProductApi'
import adminCategoryApi from '../../api/adminCategoryApi'

const products = ref([])
const categories = ref([])
const showForm = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', description: '', price: 0, stockQuantity: 0, imageUrl: '', categoryId: null })

onMounted(async () => {
  categories.value = (await adminCategoryApi.list()).data
  await load()
})

async function load() { products.value = (await adminProductApi.list({ page: 0, size: 50 })).data.content }

function openNew() { editingId.value = null; Object.assign(form, { name: '', description: '', price: 0, stockQuantity: 0, imageUrl: '', categoryId: null }); showForm.value = true }
function edit(p) { editingId.value = p.id; Object.assign(form, { name: p.name, description: p.description, price: p.price, stockQuantity: p.stockQuantity, imageUrl: p.imageUrl, categoryId: p.category?.id }); showForm.value = true }

async function save() {
  if (editingId.value) await adminProductApi.update(editingId.value, form)
  else await adminProductApi.create(form)
  showForm.value = false
  await load()
}

async function remove(id) { await adminProductApi.remove(id); await load() }
</script>

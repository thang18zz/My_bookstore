<template>
  <div>
    <h2>Quản lý danh mục</h2> <!-- UC9.1.1 -->
    <form @submit.prevent="save">
      <input v-model="form.name" placeholder="Tên danh mục" required />
      <input v-model="form.description" placeholder="Mô tả" />
      <button type="submit">{{ editingId ? 'Cập nhật' : 'Thêm' }}</button> <!-- UC9.1.3 / UC9.1.4 -->
    </form>
    <table>
      <tr v-for="c in categories" :key="c.id">
        <td>{{ c.name }}</td>
        <td>{{ c.description }}</td>
        <td>
          <button @click="edit(c)">Sửa</button>
          <button @click="remove(c.id)">Xóa</button> <!-- UC9.1.5 -->
        </td>
      </tr>
    </table>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import adminCategoryApi from '../../api/adminCategoryApi'

const categories = ref([])
const editingId = ref(null)
const form = reactive({ name: '', description: '' })

onMounted(load)
async function load() { categories.value = (await adminCategoryApi.list()).data }

function edit(c) { editingId.value = c.id; Object.assign(form, c) }
async function save() {
  if (editingId.value) await adminCategoryApi.update(editingId.value, form)
  else await adminCategoryApi.create(form)
  editingId.value = null
  Object.assign(form, { name: '', description: '' })
  await load()
}
async function remove(id) { await adminCategoryApi.remove(id); await load() }
</script>

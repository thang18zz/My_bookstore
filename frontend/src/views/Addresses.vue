<template>
  <div>
    <h2>Địa chỉ giao hàng</h2> <!-- UC6.1.1 -->
    <div v-for="a in addresses" :key="a.id" class="addr-row">
      <span>{{ a.recipientName }} - {{ a.phone }} - {{ a.line1 }}, {{ a.city }}</span>
      <span v-if="a.default" class="badge">Mặc định</span>
      <button v-if="!a.default" @click="setDefault(a.id)">Đặt làm mặc định</button> <!-- UC6.1.5 -->
      <button @click="edit(a)">Sửa</button> <!-- UC6.1.3 -->
      <button @click="remove(a.id)">Xóa</button> <!-- UC6.1.4 -->
    </div>

    <form @submit.prevent="save">
      <input v-model="form.recipientName" placeholder="Tên người nhận" required />
      <input v-model="form.phone" placeholder="Số điện thoại" required />
      <input v-model="form.line1" placeholder="Địa chỉ" required />
      <input v-model="form.city" placeholder="Thành phố" />
      <label><input type="checkbox" v-model="form.isDefault" /> Đặt làm mặc định</label>
      <button type="submit">{{ editingId ? 'Cập nhật' : 'Thêm địa chỉ' }}</button> <!-- UC6.1.2 -->
    </form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import addressApi from '../api/addressApi'

const addresses = ref([])
const editingId = ref(null)
const form = reactive({ recipientName: '', phone: '', line1: '', city: '', isDefault: false })

onMounted(load)
async function load() { addresses.value = (await addressApi.list()).data }

async function save() {
  if (editingId.value) {
    await addressApi.update(editingId.value, form)
  } else {
    await addressApi.add(form)
  }
  Object.assign(form, { recipientName: '', phone: '', line1: '', city: '', isDefault: false })
  editingId.value = null
  await load()
}

function edit(a) { editingId.value = a.id; Object.assign(form, a) }
async function remove(id) { await addressApi.remove(id); await load() }
async function setDefault(id) { await addressApi.setDefault(id); await load() }
</script>

<style scoped>
.addr-row { display: flex; gap: 12px; align-items: center; padding: 8px 0; border-bottom: 1px solid #eee; }
.badge { background: #16a34a; color: white; padding: 2px 8px; border-radius: 4px; font-size: 0.8rem; }
</style>

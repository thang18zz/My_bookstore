<template>
  <div class="admin-products-page">
    <div class="page-header">
      <div>
        <h2>Quản lý sản phẩm</h2>
        <p class="subtitle">Xem danh sách, thêm mới, sửa thông tin và cập nhật trạng thái sách</p>
      </div>
      <button class="btn btn-primary" @click="openNew">
        <span>+</span> Thêm sản phẩm mới
      </button>
    </div>

    <!-- Search & Filter Bar -->
    <div class="filter-bar">
      <input 
        v-model="searchQuery" 
        type="text" 
        placeholder="🔍 Tìm kiếm sản phẩm theo tên..." 
        class="search-input"
      />
      <select v-model="filterCategory" class="category-select">
        <option :value="null">Tất cả danh mục</option>
        <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <span class="count-badge">Tổng: {{ filteredProducts.length }} sản phẩm</span>
    </div>

    <!-- Notification Message -->
    <div v-if="notice" :class="['alert', notice.type]">
      {{ notice.message }}
    </div>

    <!-- Products Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th style="width: 60px">Ảnh</th>
            <th>Tên sách</th>
            <th>Danh mục</th>
            <th>Giá bán</th>
            <th>Tồn kho</th>
            <th>Trạng thái</th>
            <th style="width: 140px; text-align: center">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredProducts.length === 0">
            <td colspan="7" class="empty-state">
              Không tìm thấy sản phẩm nào phù hợp.
            </td>
          </tr>
          <tr v-for="p in filteredProducts" :key="p.id">
            <td>
              <img 
                :src="p.imageUrl || 'https://via.placeholder.com/60x80?text=Book'" 
                :alt="p.name" 
                class="thumb"
              />
            </td>
            <td>
              <div class="product-title">{{ p.name }}</div>
              <div class="product-id">ID: #{{ p.id }}</div>
            </td>
            <td>
              <span class="badge-category">{{ p.categoryName || getCategoryName(p.categoryId) }}</span>
            </td>
            <td class="price-cell">
              {{ p.price ? p.price.toLocaleString() : 0 }} đ
            </td>
            <td>
              <span :class="['stock-badge', p.stockQuantity <= 5 ? 'low-stock' : 'in-stock']">
                {{ p.stockQuantity }} cuốn
              </span>
            </td>
            <td>
              <div v-if="p.active !== false">
                <span :class="['status-pill', p.inStock !== false && p.stockQuantity > 0 ? 'active' : 'out']">
                  {{ p.inStock !== false && p.stockQuantity > 0 ? 'Đang bán' : 'Hết hàng' }}
                </span>
              </div>
              <div v-else>
                <span class="status-pill soft-deleted">Đã ẩn (Xóa mềm)</span>
              </div>
            </td>
            <td>
              <div class="action-buttons">
                <button v-if="p.active !== false" class="btn btn-sm btn-edit" @click="edit(p)">Sửa</button>
                <button v-if="p.active !== false" class="btn btn-sm btn-soft-delete" @click="softDelete(p)" title="Xóa mềm: Ẩn khỏi cửa hàng, giữ nguyên đơn hàng cũ">Xóa mềm</button>
                <button v-else class="btn btn-sm btn-restore" @click="restoreProduct(p)" title="Khôi phục: Mở bán lại sản phẩm này">Khôi phục</button>
                <button class="btn btn-sm btn-hard-delete" @click="hardDelete(p)" title="Xóa cứng: Xóa vĩnh viễn khỏi Database">Xóa cứng</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Add / Edit Modal -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingId ? '✏️ Chỉnh sửa sản phẩm #' + editingId : '✨ Thêm sản phẩm mới' }}</h3>
          <button class="close-btn" @click="showForm = false">&times;</button>
        </div>

        <form @submit.prevent="save">
          <div class="modal-body">
            <div class="form-group">
              <label>Tên sách <span class="req">*</span></label>
              <input v-model="form.name" placeholder="Ví dụ: Đắc Nhân Tâm" required />
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label>Danh mục <span class="req">*</span></label>
                <select v-model.number="form.categoryId" required>
                  <option :value="null" disabled>-- Chọn danh mục --</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
              </div>

              <div class="form-group half">
                <label>Giá bán (VNĐ) <span class="req">*</span></label>
                <input v-model.number="form.price" type="number" min="0" step="1000" placeholder="100000" required />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label>Số lượng tồn kho <span class="req">*</span></label>
                <input v-model.number="form.stockQuantity" type="number" min="0" placeholder="50" required />
              </div>

              <div class="form-group half">
                <label>Đường dẫn ảnh bìa (URL)</label>
                <input v-model="form.imageUrl" placeholder="https://example.com/cover.jpg" />
              </div>
            </div>

            <div v-if="form.imageUrl" class="image-preview-box">
              <span class="preview-label">Xem trước ảnh:</span>
              <img :src="form.imageUrl" class="img-preview" @error="onImgError" alt="Preview" />
            </div>

            <div class="form-group">
              <label>Mô tả chi tiết / Tóm tắt nội dung sách</label>
              <textarea v-model="form.description" rows="4" placeholder="Nhập tóm tắt giới thiệu nội dung cuốn sách..."></textarea>
            </div>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="showForm = false">Hủy bỏ</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? 'Đang lưu...' : (editingId ? 'Cập nhật sản phẩm' : 'Lưu sản phẩm mới') }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import adminProductApi from '../../api/adminProductApi'
import adminCategoryApi from '../../api/adminCategoryApi'

const products = ref([])
const categories = ref([])
const showForm = ref(false)
const editingId = ref(null)
const saving = ref(false)
const searchQuery = ref('')
const filterCategory = ref(null)
const notice = ref(null)

const form = reactive({
  name: '',
  description: '',
  price: 0,
  stockQuantity: 0,
  imageUrl: '',
  categoryId: null
})

onMounted(async () => {
  await loadCategories()
  await load()
})

async function loadCategories() {
  try {
    categories.value = (await adminCategoryApi.list()).data
  } catch (err) {
    console.error('Lỗi tải danh mục:', err)
  }
}

async function load() {
  try {
    const res = await adminProductApi.list({ page: 0, size: 100 })
    products.value = res.data.content || []
  } catch (err) {
    showNotice('Lỗi tải danh sách sản phẩm: ' + err.message, 'error')
  }
}

const filteredProducts = computed(() => {
  return products.value.filter(p => {
    const matchName = !searchQuery.value || p.name.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchCat = filterCategory.value === null || p.categoryId === filterCategory.value
    return matchName && matchCat
  })
})

function getCategoryName(catId) {
  const c = categories.value.find(x => x.id === catId)
  return c ? c.name : 'Chưa phân loại'
}

function openNew() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    description: '',
    price: 50000,
    stockQuantity: 20,
    imageUrl: '',
    categoryId: categories.value.length ? categories.value[0].id : null
  })
  showForm.value = true
}

function edit(p) {
  editingId.value = p.id
  Object.assign(form, {
    name: p.name,
    description: p.description || '',
    price: p.price,
    stockQuantity: p.stockQuantity,
    imageUrl: p.imageUrl || '',
    categoryId: p.categoryId || (categories.value.length ? categories.value[0].id : null)
  })
  showForm.value = true
}

async function save() {
  saving.value = true
  try {
    if (editingId.value) {
      await adminProductApi.update(editingId.value, form)
      showNotice(`Đã cập nhật sản phẩm "${form.name}" thành công!`, 'success')
    } else {
      await adminProductApi.create(form)
      showNotice(`Đã thêm sản phẩm "${form.name}" thành công!`, 'success')
    }
    showForm.value = false
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi lưu'
    showNotice('Không thể lưu: ' + msg, 'error')
  } finally {
    saving.value = false
  }
}

async function softDelete(p) {
  if (!confirm(`Bạn có chắc muốn XÓA MỀM (Ẩn sản phẩm) "${p.name}" (ID #${p.id})?\n\n- Sản phẩm sẽ bị ẩn khỏi cửa hàng, khách hàng không thể tìm hoặc mua sách này.\n- Toàn bộ lịch sử đơn hàng cũ vẫn được giữ nguyên vẹn.\n- Bạn có thể nhấn 'Khôi phục' bất kỳ lúc nào.`)) {
    return
  }
  try {
    await adminProductApi.remove(p.id, false)
    showNotice(`Đã ẩn (xóa mềm) sản phẩm "${p.name}" khỏi cửa hàng!`, 'success')
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi xóa mềm'
    showNotice('Không thể xóa mềm: ' + msg, 'error')
  }
}

async function restoreProduct(p) {
  try {
    await adminProductApi.restore(p.id)
    showNotice(`Đã khôi phục sản phẩm "${p.name}" mở bán trở lại!`, 'success')
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi khôi phục'
    showNotice('Không thể khôi phục: ' + msg, 'error')
  }
}

async function hardDelete(p) {
  if (!confirm(`⚠️ CẢNH BÁO XÓA CỨNG: Bạn có chắc chắn muốn XÓA VĨNH VIỄN sản phẩm "${p.name}" (ID #${p.id}) khỏi cơ sở dữ liệu?\n\n- Hành động này KHÔNG THỂ HOÀN TÁC!\n- Nếu sản phẩm đã từng được đặt trong đơn hàng nào đó, hệ thống sẽ từ chối xóa cứng để bảo vệ dữ liệu.`)) {
    return
  }
  try {
    await adminProductApi.remove(p.id, true)
    showNotice(`Đã xóa vĩnh viễn (xóa cứng) sản phẩm "${p.name}" khỏi database!`, 'success')
    await load()
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Lỗi khi xóa cứng'
    showNotice(msg, 'error')
  }
}

function showNotice(message, type = 'success') {
  notice.value = { message, type }
  setTimeout(() => {
    if (notice.value && notice.value.message === message) notice.value = null
  }, 4000)
}

function onImgError(e) {
  e.target.src = 'https://via.placeholder.com/100x140?text=No+Image'
}
</script>

<style scoped>
.admin-products-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 { margin: 0 0 4px 0; color: #111827; }
.subtitle { margin: 0; color: #6b7280; font-size: 0.9rem; }

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.category-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  background: white;
}

.count-badge {
  font-size: 0.85rem;
  color: #4b5563;
  font-weight: 500;
  white-space: nowrap;
}

/* Alert notifications */
.alert {
  padding: 10px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
  font-size: 0.9rem;
}
.alert.success { background: #def7ec; color: #03543f; border: 1px solid #bcf0da; }
.alert.error { background: #fde8e8; color: #9b1c1c; border: 1px solid #fbd5d5; }

/* Table container */
.table-container {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.data-table th {
  background: #f9fafb;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 0.85rem;
  color: #4b5563;
  border-bottom: 1px solid #e5e7eb;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.data-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 0.9rem;
  vertical-align: middle;
}

.data-table tr:hover {
  background: #f9fafb;
}

.thumb {
  width: 44px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.product-title {
  font-weight: 600;
  color: #111827;
}
.product-id {
  font-size: 0.75rem;
  color: #9ca3af;
}

.badge-category {
  background: #eff6ff;
  color: #1d4ed8;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.price-cell {
  font-weight: 600;
  color: #059669;
}

.stock-badge {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}
.stock-badge.in-stock { background: #f0fdf4; color: #166534; }
.stock-badge.low-stock { background: #fef2f2; color: #991b1b; }

.status-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}
.status-pill.active { background: #d1fae5; color: #065f46; }
.status-pill.out { background: #fee2e2; color: #991b1b; }
.status-pill.soft-deleted { background: #f3f4f6; color: #6b7280; border: 1px solid #d1d5db; }

.action-buttons {
  display: flex;
  gap: 6px;
  justify-content: center;
}

/* Buttons */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.15s ease;
}
.btn-primary {
  background: #2563eb;
  color: white;
}
.btn-primary:hover { background: #1d4ed8; }
.btn-primary:disabled { background: #93c5fd; cursor: not-allowed; }

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
}
.btn-secondary:hover { background: #e5e7eb; }

.btn-sm {
  padding: 4px 10px;
  font-size: 0.8rem;
}
.btn-edit {
  background: #f3f4f6;
  color: #1f2937;
  border: 1px solid #d1d5db;
}
.btn-edit:hover { background: #e5e7eb; }

.btn-soft-delete {
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
}
.btn-soft-delete:hover { background: #fde68a; }

.btn-restore {
  background: #d1fae5;
  color: #065f46;
  border: 1px solid #a7f3d0;
}
.btn-restore:hover { background: #a7f3d0; }

.btn-hard-delete {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}
.btn-hard-delete:hover { background: #fca5a5; }

.empty-state {
  text-align: center;
  padding: 32px !important;
  color: #9ca3af;
  font-style: italic;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.modal-content {
  background: white;
  width: 90%;
  max-width: 600px;
  border-radius: 10px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}
.modal-header h3 { margin: 0; font-size: 1.1rem; color: #111827; }
.close-btn {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: #6b7280;
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 14px 20px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

.form-group {
  margin-bottom: 14px;
}
.form-group label {
  display: block;
  font-size: 0.85rem;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
}
.req { color: #dc2626; }

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.form-row {
  display: flex;
  gap: 12px;
}
.half { flex: 1; }

.image-preview-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  padding: 8px 12px;
  background: #f3f4f6;
  border-radius: 6px;
}
.preview-label { font-size: 0.8rem; color: #4b5563; }
.img-preview {
  height: 50px;
  width: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #d1d5db;
}
</style>

<template>
  <div class="contact-page container">
    <div class="page-title-row">
      <h1 class="font-serif page-title">Trung Tâm Hỗ Trợ & Liên Hệ</h1>
      <p class="page-sub">Đội ngũ chăm sóc khách hàng của BookStore luôn sẵn sàng giải đáp mọi thắc mắc của bạn</p>
    </div>

    <div class="contact-layout">
      <!-- Left Column: Info & FAQs -->
      <div class="contact-info-col">
        <div class="info-card card-surface">
          <h3 class="font-serif card-heading">Thông Tin Kết Nối</h3>

          <div class="contact-methods">
            <div class="method-item">
              <span class="m-icon">📞</span>
              <div>
                <strong>Hotline Chăm Sóc Khách Hàng</strong>
                <p>{{ info?.phone || '1900-1234 (Từ 8:00 - 21:00)' }}</p>
              </div>
            </div>

            <div class="method-item">
              <span class="m-icon">✉️</span>
              <div>
                <strong>Hộp Thư Điện Tử</strong>
                <p>{{ info?.email || 'support@bookstore.vn' }}</p>
              </div>
            </div>

            <div class="method-item">
              <span class="m-icon">📍</span>
              <div>
                <strong>Địa Chỉ Trụ Sở & Kho Sách</strong>
                <p>{{ info?.address || '123 Book Street, Hanoi' }}</p>
              </div>
            </div>
          </div>

          <div class="business-hours">
            <strong>Giờ làm việc:</strong> Thứ Hai – Chủ Nhật: 08:00 – 21:00 (Kể cả ngày lễ)
          </div>
        </div>

        <div class="faq-mini-card card-surface">
          <h4 class="font-serif">Câu Hỏi Thường Gặp</h4>
          <ul class="faq-list">
            <li><strong>Thời gian giao hàng mất bao lâu?</strong> Từ 1-3 ngày làm việc tùy khu vực.</li>
            <li><strong>Tôi có được kiểm tra sách trước khi nhận không?</strong> Bạn được đồng kiểm khi thanh toán COD.</li>
            <li><strong>Làm sao để theo dõi đơn hàng?</strong> Bạn có thể vào mục "Lịch sử đơn hàng" trên menu để xem trạng thái đơn hàng.</li>
          </ul>
        </div>
      </div>

      <!-- Right Column: Contact Form -->
      <div class="contact-form-col">
        <div class="form-card card-surface">
          <h3 class="font-serif card-heading">Gửi Tin Nhắn Cho BookStore</h3>
          <p class="form-desc">Hãy để lại lời nhắn hoặc yêu cầu hỗ trợ về đơn hàng, chúng tôi sẽ phản hồi trong vòng 2 giờ làm việc.</p>

          <form class="contact-form" @submit.prevent="submit">
            <div class="form-group">
              <label for="contact-name">Họ và tên của bạn</label>
              <input id="contact-name" v-model="form.name" placeholder="Nguyễn Văn A" required />
            </div>

            <div class="form-group">
              <label for="contact-email">Địa chỉ Email</label>
              <input id="contact-email" v-model="form.email" type="email" placeholder="email@example.com" required />
            </div>

            <div class="form-group">
              <label for="contact-msg">Nội dung thắc mắc / Yêu cầu</label>
              <textarea 
                id="contact-msg" 
                v-model="form.message" 
                rows="5" 
                placeholder="Mô tả chi tiết vấn đề bạn cần giải đáp..." 
                required
              ></textarea>
            </div>

            <div v-if="sent" class="success-alert">
              <span>✓ Đã gửi yêu cầu thành công! Chúng tôi sẽ liên hệ lại với bạn trong thời gian sớm nhất.</span>
            </div>

            <button type="submit" class="btn btn-primary btn-lg" :disabled="submitting">
              {{ submitting ? 'Đang gửi...' : 'Gửi yêu cầu hỗ trợ' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import contactApi from '../api/contactApi'

const info = ref(null)
const form = reactive({ name: '', email: '', message: '' })
const sent = ref(false)
const submitting = ref(false)

onMounted(async () => {
  try {
    const res = await contactApi.info()
    info.value = res.data
  } catch (e) {
    console.error('Error loading contact info:', e)
  }
})

async function submit() {
  submitting.value = true
  try {
    await contactApi.submit(form)
    sent.value = true
    Object.assign(form, { name: '', email: '', message: '' })
  } catch (e) {
    alert('Không thể gửi yêu cầu: ' + (e.response?.data?.message || e.message))
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.contact-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-title {
  font-size: 1.8rem;
  color: var(--tb-slate-900);
  margin-bottom: 4px;
}

.page-sub {
  color: var(--tb-slate-500);
  font-size: 0.92rem;
}

.contact-layout {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 28px;
  align-items: start;
}

.contact-info-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card, .faq-mini-card, .form-card {
  padding: 28px;
  background-color: #ffffff;
}

.card-heading {
  font-size: 1.3rem;
  margin-bottom: 18px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--tb-teal-100);
  color: var(--tb-slate-900);
}

.contact-methods {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 20px;
}

.method-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.m-icon {
  font-size: 1.4rem;
  line-height: 1;
}

.method-item strong {
  display: block;
  font-size: 0.92rem;
  color: var(--tb-slate-900);
  margin-bottom: 2px;
}

.method-item p {
  font-size: 0.85rem;
  color: var(--tb-slate-600);
  margin: 0;
}

.business-hours {
  background-color: var(--tb-teal-50);
  color: var(--tb-teal-800);
  padding: 12px 14px;
  border-radius: var(--tb-radius-md);
  font-size: 0.85rem;
}

.faq-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 0.88rem;
  color: var(--tb-slate-600);
}

.faq-list strong {
  color: var(--tb-slate-900);
  display: block;
  margin-bottom: 2px;
}

/* Right Form */
.form-desc {
  color: var(--tb-slate-500);
  font-size: 0.88rem;
  line-height: 1.5;
  margin-bottom: 20px;
}

.contact-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--tb-slate-700);
}

.success-alert {
  background-color: #dcfce7;
  color: #15803d;
  border: 1px solid #bbf7d0;
  padding: 12px 16px;
  border-radius: var(--tb-radius-md);
  font-size: 0.88rem;
  font-weight: 600;
}

@media (max-width: 860px) {
  .contact-layout {
    grid-template-columns: 1fr;
  }
}
</style>

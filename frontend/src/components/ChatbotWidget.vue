<template>
  <div class="chat-widget">
    <!-- Floating Action Button -->
    <button class="chat-fab" @click="open = !open" title="Trợ lý ảo BookStore">
      <span v-if="!open">
        <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
        </svg>
      </span>
      <span v-else>✕</span>
      <span v-if="!open && chatbot.messages.length === 0" class="fab-pulse-ring"></span>
    </button>

    <!-- Chat Window Container -->
    <div v-if="open" class="chat-window card-surface">
      <!-- Chat Header -->
      <div class="chat-header">
        <div class="header-bot-info">
          <div class="bot-avatar">🤖</div>
          <div class="bot-text">
            <span class="bot-name font-serif">Trợ Lý Sách AI</span>
            <span class="bot-status"><span class="status-dot"></span> Sẵn sàng tư vấn</span>
          </div>
        </div>

        <div class="header-actions">
          <button 
            v-if="!chatbot.handedOff" 
            class="btn-handoff" 
            title="Kết nối nhân viên tư vấn trực tiếp"
            @click="chatbot.requestHandoff()"
          >
            Gặp nhân viên
          </button>
          <button class="btn-close-chat" @click="open = false">✕</button>
        </div>
      </div>

      <!-- Chat Body -->
      <div class="chat-body" ref="body">
        <!-- Welcome banner -->
        <div class="welcome-box">
          <p class="welcome-title">Xin chào bạn đọc!</p>
          <p class="welcome-sub">Tôi là trợ lý AI của BookStore. Bạn muốn tìm sách theo chủ đề, tra cứu đơn hàng hay nhận gợi ý đọc sách?</p>
          
          <div v-if="chatbot.messages.length === 0" class="quick-prompts">
            <button @click="sendPrompt('Gợi ý cho tôi 3 cuốn sách văn học hay nhất')">📖 Sách văn học hay</button>
            <button @click="sendPrompt('Có cuốn sách kinh doanh khởi nghiệp nào nổi bật không?')">💡 Sách kinh doanh</button>
            <button @click="sendPrompt('Tra cứu trạng thái đơn hàng gần nhất của tôi')">📦 Tra cứu đơn hàng</button>
          </div>
        </div>

        <!-- Messages stream -->
        <div 
          v-for="(m, idx) in chatbot.messages" 
          :key="idx" 
          :class="['msg', m.sender.toLowerCase()]"
        >
          <div class="msg-bubble">
            <p>{{ m.content }}</p>
          </div>

          <!-- Star Rating for Bot Response (UC14.1.6) -->
          <div v-if="m.sender === 'BOT' && m.id" class="rate-row">
            <span class="rate-label">Hữu ích?</span>
            <button 
              v-for="n in [1,2,3,4,5]" 
              :key="n"
              :class="{ active: m.rating >= n }"
              @click="chatbot.rate(m.id, n)"
            >★</button>
          </div>
        </div>

        <!-- Typing Indicator -->
        <div v-if="chatbot.sending" class="typing-bubble">
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
        </div>

        <!-- Handoff Note -->
        <div v-if="chatbot.handedOff" class="handoff-alert">
          <span>🎧 Đã chuyển cuộc trò chuyện cho chuyên viên tư vấn khách hàng. Vui lòng chờ phản hồi...</span>
        </div>
      </div>

      <!-- Chat Input Form -->
      <form class="chat-input-bar" @submit.prevent="send">
        <input 
          v-model="draft" 
          placeholder="Hỏi về sách, gợi ý tác giả, đơn hàng..." 
          :disabled="chatbot.handedOff" 
        />
        <button 
          type="submit" 
          :disabled="chatbot.handedOff || !draft.trim()"
          class="btn-send"
          title="Gửi tin nhắn"
        >
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="22" y1="2" x2="11" y2="13"></line>
            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
          </svg>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { useChatbotStore } from '../stores/chatbot'

const chatbot = useChatbotStore()
const open = ref(false)
const draft = ref('')
const body = ref(null)

watch(() => chatbot.messages.length, () => {
  nextTick(() => { 
    if (body.value) body.value.scrollTop = body.value.scrollHeight 
  })
})

async function send() {
  const text = draft.value
  if (!text.trim()) return
  draft.value = ''
  await chatbot.send(text)
}

function sendPrompt(promptText) {
  draft.value = promptText
  send()
}
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

/* Floating Action Button */
.chat-fab {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--tb-teal-700) 0%, var(--tb-teal-600) 100%);
  color: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(0, 133, 117, 0.35);
  transition: transform var(--tb-transition-fast), box-shadow var(--tb-transition-fast);
  position: relative;
}

.chat-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(0, 133, 117, 0.45);
}

.fab-pulse-ring {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 50%;
  border: 2px solid var(--tb-teal-500);
  animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
  0% { transform: scale(0.95); opacity: 0.8; }
  50% { transform: scale(1.15); opacity: 0.2; }
  100% { transform: scale(0.95); opacity: 0; }
}

/* Chat Window */
.chat-window {
  position: absolute;
  bottom: 72px;
  right: 0;
  width: 380px;
  height: 520px;
  background-color: #ffffff;
  border-radius: var(--tb-radius-xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  border: 1px solid var(--tb-border);
  animation: slide-up 0.2s ease-out;
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Header */
.chat-header {
  padding: 14px 18px;
  background: linear-gradient(135deg, var(--tb-teal-900) 0%, var(--tb-teal-800) 100%);
  color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-bot-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bot-avatar {
  width: 36px;
  height: 36px;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.bot-text {
  display: flex;
  flex-direction: column;
}

.bot-name {
  font-size: 1rem;
  font-weight: 700;
  color: #ffffff;
}

.bot-status {
  font-size: 0.72rem;
  color: var(--tb-teal-200);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  background-color: #4ade80;
  border-radius: 50%;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-handoff {
  background-color: rgba(255, 255, 255, 0.15);
  color: #ffffff;
  border: none;
  font-size: 0.75rem;
  padding: 4px 8px;
  border-radius: var(--tb-radius-sm);
  cursor: pointer;
}

.btn-handoff:hover {
  background-color: rgba(255, 255, 255, 0.25);
}

.btn-close-chat {
  background: none;
  border: none;
  color: #cbd5e1;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 2px 6px;
}

.btn-close-chat:hover {
  color: #ffffff;
}

/* Chat Body */
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  background-color: #f8faf9;
}

.welcome-box {
  background-color: #ffffff;
  border: 1px solid var(--tb-border);
  border-radius: var(--tb-radius-lg);
  padding: 14px;
  text-align: center;
}

.welcome-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--tb-teal-800);
  margin-bottom: 4px;
}

.welcome-sub {
  font-size: 0.82rem;
  color: var(--tb-slate-600);
  line-height: 1.5;
  margin-bottom: 12px;
}

.quick-prompts {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-prompts button {
  background-color: var(--tb-teal-50);
  border: 1px solid var(--tb-teal-200);
  color: var(--tb-teal-800);
  font-size: 0.8rem;
  padding: 6px 10px;
  border-radius: var(--tb-radius-full);
  cursor: pointer;
  text-align: left;
  transition: all var(--tb-transition-fast);
}

.quick-prompts button:hover {
  background-color: var(--tb-teal-600);
  color: #ffffff;
  border-color: var(--tb-teal-600);
}

/* Messages */
.msg {
  display: flex;
  flex-direction: column;
}

.msg-bubble {
  max-width: 82%;
  padding: 10px 14px;
  border-radius: var(--tb-radius-lg);
  font-size: 0.88rem;
  line-height: 1.5;
  word-break: break-word;
}

.msg.user {
  align-items: flex-end;
}

.msg.user .msg-bubble {
  background-color: var(--tb-teal-600);
  color: #ffffff;
  border-bottom-right-radius: 2px;
}

.msg.bot, .msg.admin {
  align-items: flex-start;
}

.msg.bot .msg-bubble, .msg.admin .msg-bubble {
  background-color: #ffffff;
  color: var(--tb-slate-800);
  border: 1px solid var(--tb-border);
  border-bottom-left-radius: 2px;
}

.rate-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  padding-left: 4px;
}

.rate-label {
  font-size: 0.72rem;
  color: var(--tb-slate-400);
}

.rate-row button {
  border: none;
  background: none;
  cursor: pointer;
  color: #cbd5e1;
  font-size: 0.95rem;
  padding: 0 2px;
}

.rate-row button.active {
  color: var(--tb-amber-500);
}

/* Typing Indicator */
.typing-bubble {
  display: flex;
  gap: 4px;
  padding: 8px 12px;
  background-color: #ffffff;
  border: 1px solid var(--tb-border);
  border-radius: var(--tb-radius-full);
  width: fit-content;
}

.typing-dot {
  width: 6px;
  height: 6px;
  background-color: var(--tb-slate-400);
  border-radius: 50%;
  animation: typing 1.2s infinite ease-in-out;
}

.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}

.handoff-alert {
  background-color: #fef3c7;
  color: #92400e;
  font-size: 0.8rem;
  padding: 8px 12px;
  border-radius: var(--tb-radius-md);
  border: 1px solid #fde68a;
}

/* Input Bar */
.chat-input-bar {
  display: flex;
  align-items: center;
  padding: 10px 14px;
  background-color: #ffffff;
  border-top: 1px solid var(--tb-border);
}

.chat-input-bar input {
  flex: 1;
  border: none !important;
  box-shadow: none !important;
  padding: 8px 10px !important;
  font-size: 0.88rem;
  background: transparent;
}

.btn-send {
  background-color: var(--tb-teal-600);
  color: #ffffff;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color var(--tb-transition-fast);
}

.btn-send:hover:not(:disabled) {
  background-color: var(--tb-teal-700);
}

.btn-send:disabled {
  background-color: var(--tb-slate-200);
  color: var(--tb-slate-400);
  cursor: not-allowed;
}

@media (max-width: 480px) {
  .chat-window {
    width: calc(100vw - 32px);
    height: calc(100vh - 120px);
    right: -8px;
  }
}
</style>

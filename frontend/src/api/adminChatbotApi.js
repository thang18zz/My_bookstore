import http from './http'

// Epic 14.2 - UC14.2.1-5
export default {
  sessions: (params) => http.get('/admin/chatbot/sessions', { params }),
  sessionMessages: (id) => http.get(`/admin/chatbot/sessions/${id}/messages`),
  handoffQueue: (params) => http.get('/admin/chatbot/handoff-queue', { params }),
  adminReply: (id, content) => http.post(`/admin/chatbot/sessions/${id}/reply`, null, { params: { content } }),
  close: (id) => http.post(`/admin/chatbot/sessions/${id}/close`),
  stats: () => http.get('/admin/chatbot/stats'),
  listFaqs: () => http.get('/admin/chatbot/faqs'),
  createFaq: (payload) => http.post('/admin/chatbot/faqs', payload),
  updateFaq: (id, payload) => http.put(`/admin/chatbot/faqs/${id}`, payload),
  deleteFaq: (id) => http.delete(`/admin/chatbot/faqs/${id}`),
}

import http from './http'

// Epic 14.1 - the Vue side of the Groq-backed chatbot
export default {
  start: () => http.post('/chatbot/sessions'),
  send: (sessionId, content) => http.post(`/chatbot/sessions/${sessionId}/messages`, { content }),
  history: (sessionId) => http.get(`/chatbot/sessions/${sessionId}/messages`),
  handoff: (sessionId) => http.post(`/chatbot/sessions/${sessionId}/handoff`),
  rate: (messageId, rating) => http.post(`/chatbot/messages/${messageId}/rate`, { rating }),
}

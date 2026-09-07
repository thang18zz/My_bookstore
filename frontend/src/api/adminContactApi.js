import http from './http'

// UC13.1.3 / UC13.1.4
export default {
  list: (params) => http.get('/admin/contact', { params }),
  reply: (id, reply) => http.patch(`/admin/contact/${id}/reply`, null, { params: { reply } }),
}

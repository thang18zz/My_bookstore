import http from './http'

// Epic 7.2 - UC7.2.1-4
export default {
  list: (params) => http.get('/admin/reviews', { params }),
  detail: (id) => http.get(`/admin/reviews/${id}`),
  remove: (id) => http.delete(`/admin/reviews/${id}`),
  hide: (id) => http.patch(`/admin/reviews/${id}/hide`),
}

import http from './http'

// Theme 12 - UC12.1.1-5 / UC12.2.1-5
export default {
  dashboard: () => http.get('/admin/stats/dashboard'),
  revenue: (from, to) => http.get('/admin/stats/revenue', { params: { from, to } }),
  bestSellers: (limit) => http.get('/admin/stats/best-sellers', { params: { limit } }),
  byCategory: () => http.get('/admin/stats/by-category'),
}

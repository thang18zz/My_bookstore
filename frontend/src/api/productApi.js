import http from './http'

// Theme 2: UC2.1.x (browse) + UC2.2.x (search/filter/sort)
export default {
  list: (params) => http.get('/products', { params }),
  byCategory: (categoryId, params) => http.get(`/products/category/${categoryId}`, { params }),
  search: (params) => http.get('/products/search', { params }),
  detail: (id) => http.get(`/products/${id}`),
  categories: () => http.get('/categories'),
}

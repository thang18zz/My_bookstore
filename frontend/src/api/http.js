import axios from 'axios'

/** Every API module in this folder imports this single instance, so the JWT
 * (from UC1.1.2 login) is attached everywhere automatically - this is the piece
 * that links the Auth module to every other feature module on the frontend. */
const http = axios.create({ baseURL: '/api' })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (res) => res.data, // unwrap ApiResponse { success, message, data }
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
    }
    return Promise.reject(err.response?.data || err)
  }
)

export default http

import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 认证相关
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getInfo: () => api.get('/auth/info')
}

// 咖啡相关
export const coffeeApi = {
  list: () => api.get('/coffee'),
  page: (params) => api.get('/coffee/page', { params }),
  getById: (id) => api.get(`/coffee/${id}`),
  create: (data) => api.post('/coffee', data),
  update: (id, data) => api.put(`/coffee/${id}`, data),
  delete: (id) => api.delete(`/coffee/${id}`),
  updateStock: (id, stock) => api.put(`/coffee/${id}/stock`, null, { params: { stock } })
}

// 订单相关
export const orderApi = {
  page: (params) => api.get('/order/page', { params }),
  pending: () => api.get('/order/pending'),
  myOrders: () => api.get('/order/page', { params: { pageSize: 20 } }),
  getById: (id) => api.get(`/order/${id}`),
  create: (data) => api.post('/order', data),
  accept: (id) => api.post(`/order/${id}/accept`),
  complete: (id) => api.post(`/order/${id}/complete`),
  cancel: (id) => api.post(`/order/${id}/cancel`)
}

// 统计相关
export const statsApi = {
  dashboard: () => api.get('/stats/dashboard')
}

export default api

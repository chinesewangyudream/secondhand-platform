import axios from 'axios'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器 - 自动附带 Token
request.interceptors.request.use(
  config => {
    // 登录和注册请求不附带 Token
    if (config.url !== '/auth/login' && config.url !== '/auth/register') {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      // 将错误信息附加到 error 对象，由调用方决定是否显示
      const error = new Error(res.message || '请求失败')
      error.code = res.code
      error.showMessage = res.message || '请求失败'

      // 401 特殊处理：清除登录状态并跳转
      if (res.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        error.showMessage = '登录已过期，请重新登录'
      }

      return Promise.reject(error)
    }
    return res
  },
  error => {
    // 网络错误
    if (!error.response) {
      error.showMessage = '网络连接失败，请检查网络后重试'
      return Promise.reject(error)
    }

    const status = error.response.status
    const message = error.response?.data?.message

    // 根据状态码设置错误信息
    switch (status) {
      case 400:
        error.showMessage = message || '请求参数错误'
        break
      case 401:
        error.showMessage = '登录已过期，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
        break
      case 403:
        error.showMessage = message || '权限不足，无法访问该资源'
        break
      case 404:
        error.showMessage = message || '请求的资源不存在'
        break
      case 500:
        error.showMessage = message || '服务器内部错误，请稍后重试'
        break
      default:
        error.showMessage = message || `请求失败 (${status})`
    }

    return Promise.reject(error)
  }
)

export default request

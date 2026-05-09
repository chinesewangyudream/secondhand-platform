import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(data) {
    // 登录前先清除旧的登录状态，避免缓存问题
    logout()

    const res = await authApi.login(data)
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res
  }

  async function loadProfile() {
    if (token.value) {
      try {
        const res = await authApi.getProfile()
        userInfo.value = res.data
      } catch (e) {
        logout()
      }
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 初始化时从 localStorage 读取
  const stored = localStorage.getItem('userInfo')
  if (stored && stored !== 'undefined') {
    try {
      userInfo.value = JSON.parse(stored)
    } catch (error) {
      console.warn('本地用户信息解析失败，已移除无效缓存')
      localStorage.removeItem('userInfo')
    }
  }

  return { token, userInfo, isLoggedIn, login, loadProfile, logout }
})

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue') },
      { path: 'search', name: 'Search', component: () => import('@/views/Search.vue') },
      { path: 'goods/:id', name: 'GoodsDetail', component: () => import('@/views/GoodsDetail.vue') },
      { path: 'auction', name: 'Auction', component: () => import('@/views/Auction.vue') },
      { path: 'auction/:id', name: 'AuctionDetail', component: () => import('@/views/AuctionDetail.vue') },
      // 需要登录的路由
      { 
        path: 'publish', 
        name: 'Publish', 
        component: () => import('@/views/Publish.vue'),
        meta: { requiresAuth: true }
      },
      { 
        path: 'orders', 
        name: 'Orders', 
        component: () => import('@/views/Orders.vue'),
        meta: { requiresAuth: true }
      },
      { 
        path: 'order/:orderId/logistics', 
        name: 'Logistics', 
        component: () => import('@/views/Logistics.vue'),
        meta: { requiresAuth: true }
      },
      { 
        path: 'chat', 
        name: 'Chat', 
        component: () => import('@/views/Chat.vue'),
        meta: { requiresAuth: true }
      },
      { 
        path: 'profile', 
        name: 'Profile', 
        component: () => import('@/views/Profile.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'my-goods',
        name: 'MyGoods',
        component: () => import('@/views/MyGoods.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/Favorites.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'edit-goods/:id',
        name: 'EditGoods',
        component: () => import('@/views/EditGoods.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/Audit.vue'),
        meta: { requiresAuth: true, requiresAuditor: true }
      },
    ]
  },
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
<!-- src/layouts/MainLayout.vue -->
<template>
  <div class="main-layout">
    <!-- 顶部导航 -->
    <header class="header" :class="{ 'header-scrolled': isScrolled }">
      <div class="header-inner">
        <!-- Logo -->
        <router-link to="/home" class="logo">
          <div class="logo-icon">
            <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M16 2L4 8v16l12 6 12-6V8L16 2z" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M16 14l-8-4v8l8 4 8-4v-8l-8 4z" fill="currentColor" opacity="0.3"/>
              <circle cx="16" cy="16" r="3" fill="currentColor"/>
            </svg>
          </div>
          <div class="logo-text">
            <span class="logo-name">闲置宝</span>
            <span class="logo-tagline">寻宝 · 分享 · 循环</span>
          </div>
        </router-link>

        <!-- 搜索框 -->
        <div class="search-container">
          <div class="search-wrapper">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索你想要的二手好物..."
              class="search-input"
              @keyup.enter="doSearch"
            />
            <button class="search-btn" @click="doSearch">搜索</button>
          </div>
        </div>

        <!-- 右侧导航 -->
        <nav class="header-nav">
          <router-link to="/publish" class="nav-btn nav-btn-primary">
            <el-icon><Plus /></el-icon>
            <span>发布闲置</span>
          </router-link>

          <!-- 未登录 -->
          <template v-if="!userStore.isLoggedIn">
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="nav-btn nav-btn-secondary">注册</router-link>
          </template>

          <!-- 已登录 -->
          <template v-else>
            <!-- 消息提醒 -->
            <div class="nav-icon-btn" @click="router.push('/chat')">
              <el-badge :value="chatStore.unreadCount" :hidden="chatStore.unreadCount === 0" class="nav-badge">
                <el-icon :size="22"><ChatDotRound /></el-icon>
              </el-badge>
              <span class="nav-icon-label">消息</span>
            </div>

            <!-- 用户头像下拉 -->
            <el-dropdown @command="handleCommand" trigger="click" class="user-dropdown">
              <div class="user-trigger">
                <el-avatar
                  :size="40"
                  :src="userStore.userInfo?.avatar"
                  class="user-avatar"
                >
                  <el-icon :size="20"><UserFilled /></el-icon>
                </el-avatar>
                <div class="user-info">
                  <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
                  <span class="user-role">{{ getRoleLabel(userStore.userInfo?.role) }}</span>
                </div>
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-menu">
                  <div class="menu-header">
                    <el-avatar :size="48" :src="userStore.userInfo?.avatar">
                      <el-icon :size="24"><UserFilled /></el-icon>
                    </el-avatar>
                    <div class="menu-user-info">
                      <span class="menu-user-name">{{ userStore.userInfo?.nickname }}</span>
                      <span class="menu-user-email">{{ userStore.userInfo?.email }}</span>
                    </div>
                  </div>
                  <el-dropdown-item command="profile" class="menu-item">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="orders" class="menu-item">
                    <el-icon><List /></el-icon>
                    <span>我的订单</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites" class="menu-item">
                    <el-icon><Star /></el-icon>
                    <span>我的收藏</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="my-goods" class="menu-item">
                    <el-icon><Goods /></el-icon>
                    <span>我发布的</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="chat" class="menu-item">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>我的消息</span>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAuditor" command="audit" class="menu-item menu-item-highlight">
                    <el-icon><Checked /></el-icon>
                    <span>商品审核</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="menu-item menu-item-danger">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </nav>
      </div>

      <!-- 分类导航栏 -->
      <div class="category-bar">
        <div class="category-inner">
          <button
            v-for="cat in categories"
            :key="cat.id"
            class="category-chip"
            @click="searchByCategory(cat.id)"
          >
            <span class="chip-icon">{{ cat.icon }}</span>
            <span class="chip-label">{{ cat.name }}</span>
          </button>
          <router-link to="/auction" class="category-chip category-chip-auction">
            <span class="chip-icon">🔨</span>
            <span class="chip-label">竞拍专区</span>
          </router-link>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">闲置宝</div>
          <p class="footer-desc">让闲置找到新主人，让生活更有温度</p>
        </div>
        <div class="footer-links">
          <a href="#" class="footer-link">关于我们</a>
          <a href="#" class="footer-link">帮助中心</a>
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">用户协议</a>
        </div>
        <div class="footer-copyright">
          <p>© 2026 闲置宝 · 让闲置流转起来</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { goodsApi } from '@/api/goods'
import {
  Plus, Search, ChatDotRound, UserFilled,
  ArrowDown, User, List, SwitchButton, Star, Goods, Checked
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const searchKeyword = ref('')
const categories = ref([])
const isScrolled = ref(false)

// 判断是否是审核员/管理员
const isAuditor = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'ADMIN' || role === 'AUDITOR'
})

// 获取角色标签
function getRoleLabel(role) {
  const labels = {
    'ADMIN': '管理员',
    'AUDITOR': '审核员',
    'USER': '普通用户'
  }
  return labels[role] || '用户'
}

// 滚动监听
function handleScroll() {
  isScrolled.value = window.scrollY > 20
}

onMounted(async () => {
  window.addEventListener('scroll', handleScroll)

  try {
    const res = await goodsApi.getCategories()
    categories.value = res.data
  } catch {}

  // 已登录则连接WebSocket
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    chatStore.connectWebSocket(userStore.userInfo.id)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})

function doSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

function searchByCategory(id) {
  router.push({ path: '/search', query: { categoryId: id } })
}

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    chatStore.disconnect()
    router.push('/home')
  } else {
    router.push('/' + command)
  }
}
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-primary);
}

/* ========== Header ========== */
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  background: var(--color-bg-overlay);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border-light);
  transition: all var(--transition-base);
}

.header-scrolled {
  box-shadow: var(--shadow-md);
}

.header-inner {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  padding: var(--space-md) var(--space-xl);
  max-width: 1440px;
  margin: 0 auto;
}

/* ========== Logo ========== */
.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  width: 42px;
  height: 42px;
  color: var(--color-primary);
  transition: transform var(--transition-bounce);
}

.logo:hover .logo-icon {
  transform: rotate(10deg) scale(1.05);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-text-primary);
  letter-spacing: -0.02em;
}

.logo-tagline {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  letter-spacing: 0.02em;
}

/* ========== Search ========== */
.search-container {
  flex: 1;
  max-width: 560px;
}

.search-wrapper {
  display: flex;
  align-items: center;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  padding: var(--space-xs) var(--space-xs) var(--space-xs) var(--space-lg);
  transition: all var(--transition-fast);
}

.search-wrapper:focus-within {
  background: var(--color-bg-card);
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}

.search-icon {
  color: var(--color-text-muted);
  font-size: var(--text-lg);
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: var(--space-sm) var(--space-md);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
  outline: none;
}

.search-input::placeholder {
  color: var(--color-text-muted);
}

.search-btn {
  padding: var(--space-sm) var(--space-lg);
  background: var(--color-primary);
  color: var(--color-text-inverse);
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-btn:hover {
  background: var(--color-primary-dark);
  transform: scale(1.02);
}

/* ========== Navigation ========== */
.header-nav {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: 500;
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  border: none;
}

.nav-btn-primary {
  background: var(--color-primary);
  color: var(--color-text-inverse);
}

.nav-btn-primary:hover {
  background: var(--color-primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.nav-btn-secondary {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.nav-btn-secondary:hover {
  background: var(--color-bg-secondary);
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.nav-link {
  padding: var(--space-sm) var(--space-md);
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
  font-weight: 500;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-primary);
}

.nav-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-xs);
  cursor: pointer;
  transition: all var(--transition-fast);
  border-radius: var(--radius-md);
}

.nav-icon-btn:hover {
  background: var(--color-bg-secondary);
}

.nav-icon-btn .el-icon {
  color: var(--color-text-secondary);
}

.nav-icon-btn:hover .el-icon {
  color: var(--color-primary);
}

.nav-icon-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* ========== User Dropdown ========== */
.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.user-trigger:hover {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
}

.user-avatar {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.user-role {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.dropdown-arrow {
  color: var(--color-text-muted);
  transition: transform var(--transition-fast);
}

.user-dropdown.is-active .dropdown-arrow {
  transform: rotate(180deg);
}

/* ========== Category Bar ========== */
.category-bar {
  background: var(--color-bg-secondary);
  border-top: 1px solid var(--color-border-light);
}

.category-inner {
  display: flex;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-xl);
  max-width: 1440px;
  margin: 0 auto;
  overflow-x: auto;
  scrollbar-width: none;
}

.category-inner::-webkit-scrollbar {
  display: none;
}

.category-chip {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-sm) var(--space-md);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  text-decoration: none;
}

.category-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-soft);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.chip-icon {
  font-size: var(--text-base);
}

.category-chip-auction {
  background: linear-gradient(135deg, var(--color-secondary-soft) 0%, transparent 100%);
  border-color: var(--color-secondary);
  color: var(--color-secondary-dark);
}

.category-chip-auction:hover {
  background: var(--color-secondary);
  color: white;
  border-color: var(--color-secondary);
}

/* ========== Main Content ========== */
.main-content {
  flex: 1;
  padding: var(--space-xl);
  max-width: 1440px;
  margin: 0 auto;
  width: 100%;
}

/* ========== Footer ========== */
.footer {
  background: var(--color-text-primary);
  color: var(--color-bg-primary);
  padding: var(--space-3xl) var(--space-xl) var(--space-xl);
  margin-top: var(--space-4xl);
}

.footer-inner {
  max-width: 1440px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-xl);
  align-items: start;
}

.footer-logo {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: var(--space-sm);
  opacity: 0.9;
}

.footer-desc {
  color: var(--color-bg-tertiary);
  font-size: var(--text-sm);
  margin: 0;
}

.footer-links {
  display: flex;
  gap: var(--space-lg);
}

.footer-link {
  color: var(--color-bg-tertiary);
  font-size: var(--text-sm);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.footer-link:hover {
  color: var(--color-primary-light);
}

.footer-copyright {
  grid-column: 1 / -1;
  text-align: center;
  padding-top: var(--space-xl);
  border-top: 1px solid rgba(250, 248, 245, 0.1);
}

.footer-copyright p {
  color: var(--color-bg-secondary);
  font-size: var(--text-sm);
  margin: 0;
}

/* ========== Page Transitions ========== */
.page-enter-active,
.page-leave-active {
  transition: all var(--transition-base);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* ========== Responsive ========== */
@media (max-width: 1024px) {
  .header-inner {
    flex-wrap: wrap;
    gap: var(--space-md);
  }

  .search-container {
    order: 3;
    max-width: none;
    width: 100%;
  }

  .logo-tagline {
    display: none;
  }
}

@media (max-width: 640px) {
  .header-inner {
    padding: var(--space-sm) var(--space-md);
  }

  .logo-text {
    display: none;
  }

  .nav-btn span {
    display: none;
  }

  .user-info {
    display: none;
  }

  .main-content {
    padding: var(--space-md);
  }

  .category-inner {
    padding: var(--space-sm) var(--space-md);
  }
}
</style>

<style>
/* Global dropdown menu styles */
.user-menu {
  padding: var(--space-sm);
  border-radius: var(--radius-xl) !important;
  border: 1px solid var(--color-border) !important;
  box-shadow: var(--shadow-xl) !important;
}

.menu-header {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  margin-bottom: var(--space-sm);
  background: var(--color-bg-secondary);
  border-radius: var(--radius-lg);
}

.menu-user-info {
  display: flex;
  flex-direction: column;
}

.menu-user-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.menu-user-email {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.menu-item {
  padding: var(--space-sm) var(--space-md) !important;
  border-radius: var(--radius-md) !important;
  margin: 2px 0 !important;
}

.menu-item:hover {
  background: var(--color-bg-secondary) !important;
}

.menu-item-highlight {
  background: var(--color-primary-soft) !important;
  color: var(--color-primary) !important;
}

.menu-item-danger:hover {
  background: rgba(196, 77, 77, 0.1) !important;
  color: var(--color-danger) !important;
}
</style>

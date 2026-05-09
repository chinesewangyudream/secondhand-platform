<!-- src/layouts/MainLayout.vue -->
<template>
  <el-container class="main-layout">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-content">
        <!-- Logo -->
        <div class="logo" @click="router.push('/home')">
          <el-icon size="28" color="#409EFF"><ShoppingBag /></el-icon>
          <span>闲置宝</span>
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索你想要的二手好物..."
            size="large"
            clearable
            @keyup.enter="doSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="doSearch" />
            </template>
          </el-input>
        </div>

        <!-- 右侧导航 -->
        <div class="header-right">
          <el-button
            type="primary"
            :icon="Plus"
            @click="router.push('/publish')"
          >发布闲置</el-button>

          <!-- 未登录 -->
          <template v-if="!userStore.isLoggedIn">
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" plain @click="router.push('/register')">注册</el-button>
          </template>

          <!-- 已登录 -->
          <template v-else>
            <!-- 消息提醒 -->
            <el-badge :value="chatStore.unreadCount" :hidden="chatStore.unreadCount === 0">
              <el-button :icon="ChatDotRound" circle @click="router.push('/chat')" />
            </el-badge>

            <!-- 用户头像下拉 -->
            <el-dropdown @command="handleCommand">
              <div class="user-avatar">
                <el-avatar
                  :size="36"
                  :src="userStore.userInfo?.avatar"
                  :icon="UserFilled"
                />
                <span>{{ userStore.userInfo?.nickname }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="orders">
                    <el-icon><List /></el-icon>我的订单
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites">
                    <el-icon><Star /></el-icon>我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="my-goods">
                    <el-icon><Goods /></el-icon>我发布的
                  </el-dropdown-item>
                  <el-dropdown-item command="chat">
                    <el-icon><ChatDotRound /></el-icon>我的消息
                  </el-dropdown-item>
                  <!-- 审核员/管理员可见 -->
                  <el-dropdown-item v-if="isAuditor" command="audit">
                    <el-icon><Checked /></el-icon>商品审核
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>

      <!-- 分类导航栏 -->
      <div class="category-nav">
        <el-link
          v-for="cat in categories"
          :key="cat.id"
          @click="searchByCategory(cat.id)"
          :underline="false"
          class="category-item"
        >
          {{ cat.icon }} {{ cat.name }}
        </el-link>
        <el-link
          :underline="false"
          class="category-item auction-link"
          @click="router.push('/auction')"
        >
          🔨 竞拍专区
        </el-link>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>

    <!-- 页脚 -->
    <el-footer class="footer">
      <p>© 2026 闲置宝 - 让闲置找到新主人</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useChatStore } from '@/store/chat'
import { goodsApi } from '@/api/goods'
import {
  Plus, Search, ChatDotRound, UserFilled,
  ArrowDown, User, List, SwitchButton, ShoppingBag, Star, Goods, Checked
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const searchKeyword = ref('')
const categories = ref([])

// 判断是否是审核员/管理员
const isAuditor = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'ADMIN' || role === 'AUDITOR'
})

onMounted(async () => {
  try {
    const res = await goodsApi.getCategories()
    categories.value = res.data
  } catch {}

  // 已登录则连接WebSocket
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    chatStore.connectWebSocket(userStore.userInfo.id)
  }
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
.main-layout { min-height: 100vh; background: #f5f7fa; }

.header {
  background: #fff;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  height: auto !important;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  padding: 12px 24px;
  gap: 16px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 22px;
  font-weight: 700;
  color: #409EFF;
  white-space: nowrap;
}

.search-box { flex: 1; max-width: 600px; }

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  white-space: nowrap;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  color: #333;
}

.category-nav {
  padding: 0 24px 10px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.category-item {
  padding: 4px 12px;
  border-radius: 20px;
  color: #555;
  transition: all 0.2s;
}

.category-item:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.auction-link { color: #E6A23C !important; }

.main-content { padding: 20px 24px; }

.footer {
  text-align: center;
  color: #999;
  font-size: 12px;
  background: #fff;
  border-top: 1px solid #eee;
}

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
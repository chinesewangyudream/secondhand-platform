<!-- src/views/Home.vue -->
<template>
  <div class="home-page">
    <!-- Hero Banner -->
    <section class="hero">
      <div class="hero-bg">
        <div class="hero-shape hero-shape-1"></div>
        <div class="hero-shape hero-shape-2"></div>
        <div class="hero-shape hero-shape-3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="title-line">发现</span>
            <span class="title-line title-highlight">宝藏好物</span>
          </h1>
          <p class="hero-subtitle">每一件闲置，都值得一次新的旅程</p>
          <div class="hero-actions">
            <button class="btn btn-primary" @click="router.push('/search')">
              <span>探索好物</span>
              <el-icon><ArrowRight /></el-icon>
            </button>
            <button class="btn btn-secondary" @click="router.push('/publish')">
              <span>发布闲置</span>
            </button>
          </div>
        </div>
        <div class="hero-visual">
          <div class="floating-cards">
            <div class="float-card float-card-1">
              <span class="float-emoji">👗</span>
            </div>
            <div class="float-card float-card-2">
              <span class="float-emoji">📱</span>
            </div>
            <div class="float-card float-card-3">
              <span class="float-emoji">📚</span>
            </div>
            <div class="float-card float-card-4">
              <span class="float-emoji">🎸</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div class="hero-stats">
        <div class="stat-item">
          <span class="stat-value">10,000+</span>
          <span class="stat-label">活跃用户</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">50,000+</span>
          <span class="stat-label">在售商品</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-value">98%</span>
          <span class="stat-label">好评率</span>
        </div>
      </div>
    </section>

    <!-- Categories Section -->
    <section class="section categories-section">
      <div class="section-header">
        <div class="section-title-group">
          <span class="section-label">探索</span>
          <h2 class="section-title">热门分类</h2>
        </div>
        <p class="section-desc">发现你感兴趣的品类</p>
      </div>

      <div class="category-grid">
        <button
          v-for="(cat, index) in categories"
          :key="cat.id"
          class="category-card"
          :class="[`cat-style-${(index % 4) + 1}`]"
          @click="searchByCategory(cat.id)"
        >
          <div class="cat-card-bg"></div>
          <div class="cat-card-content">
            <span class="cat-emoji">{{ cat.icon }}</span>
            <span class="cat-name">{{ cat.name }}</span>
          </div>
          <div class="cat-card-shine"></div>
        </button>
      </div>
    </section>

    <!-- Latest Goods Section -->
    <section class="section goods-section">
      <div class="section-header">
        <div class="section-title-group">
          <span class="section-label">新鲜上架</span>
          <h2 class="section-title">最新商品</h2>
        </div>
        <router-link to="/search" class="section-link">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div class="goods-grid" v-loading="loading">
        <template v-if="goodsList.length > 0">
          <GoodsCard
            v-for="(goods, index) in goodsList"
            :key="goods.id"
            :goods="goods"
            :class="[`goods-item-${index + 1}`]"
            @click="router.push('/goods/' + goods.id)"
          />
        </template>
        <template v-else-if="!loading">
          <div class="empty-state">
            <span class="empty-icon">📦</span>
            <p>暂无商品，快来发布第一个吧！</p>
            <button class="btn btn-primary" @click="router.push('/publish')">
              发布商品
            </button>
          </div>
        </template>
      </div>

      <div v-if="error" class="error-message">
        <el-alert :title="error" type="error" show-icon />
      </div>
    </section>

    <!-- Auction Banner -->
    <section class="auction-section">
      <div class="auction-card">
        <div class="auction-bg">
          <div class="auction-pattern"></div>
        </div>
        <div class="auction-content">
          <div class="auction-badge">限时竞拍</div>
          <h2 class="auction-title">拍卖专区</h2>
          <p class="auction-desc">
            公开竞价，透明出价<br/>
            用心仪价格拍下心头好
          </p>
          <div class="auction-features">
            <div class="feature-item">
              <el-icon><Timer /></el-icon>
              <span>限时拍卖</span>
            </div>
            <div class="feature-item">
              <el-icon><TrendCharts /></el-icon>
              <span>价格透明</span>
            </div>
            <div class="feature-item">
              <el-icon><Medal /></el-icon>
              <span>品质保障</span>
            </div>
          </div>
          <button class="btn btn-auction" @click="router.push('/auction')">
            <span>立即参与</span>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <div class="auction-visual">
          <div class="auction-hammer">
            <span>🔨</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="section features-section">
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🛡️</div>
          <h3 class="feature-title">安全保障</h3>
          <p class="feature-desc">平台担保交易，资金安全有保障</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">⚡</div>
          <h3 class="feature-title">极速发布</h3>
          <p class="feature-desc">一键发布，AI智能估价助你定价</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">💬</div>
          <h3 class="feature-title">即时沟通</h3>
          <p class="feature-desc">内置聊天系统，买卖沟通更便捷</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🎯</div>
          <h3 class="feature-title">智能推荐</h3>
          <p class="feature-desc">AI算法推荐，发现更多好物</p>
        </div>
      </div>
    </section>

    <!-- AI Consultant -->
    <AiConsultant />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import GoodsCard from '@/components/GoodsCard.vue'
import AiConsultant from '@/components/AiConsultant.vue'
import { ArrowRight, Timer, TrendCharts, Medal } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const goodsList = ref([])
const categories = ref([])
const error = ref(null)

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    const [goodsResult, catResult] = await Promise.allSettled([
      goodsApi.getList({ page: 1, size: 8 }),
      goodsApi.getCategories()
    ])

    if (goodsResult.status === 'fulfilled') {
      goodsList.value = goodsResult.value.data.records || []
    } else {
      console.error('加载商品列表失败:', goodsResult.reason)
      const err = goodsResult.reason
      if (err.response?.status === 403) {
        error.value = '访问被拒绝 (403)。可能需要登录或缺少权限。'
      } else {
        error.value = '商品列表加载失败: ' + (err.message || '请检查网络连接')
      }
    }

    if (catResult.status === 'fulfilled') {
      categories.value = catResult.value.data || []
    } else {
      console.error('加载分类失败:', catResult.reason)
      if (!error.value) {
        error.value = '分类加载失败: ' + (catResult.reason.message || '请检查网络连接')
      }
    }
  } catch (err) {
    error.value = err.message || '加载失败，请稍后重试'
    console.error('加载数据失败:', err)
  } finally {
    loading.value = false
  }
})

function searchByCategory(id) {
  router.push({ path: '/search', query: { categoryId: id } })
}
</script>

<style scoped>
.home-page {
  animation: fadeIn 0.5s ease-out;
}

/* ========== Hero Section ========== */
.hero {
  position: relative;
  padding: var(--space-4xl) var(--space-xl);
  margin: calc(-1 * var(--space-xl));
  margin-bottom: var(--space-3xl);
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-bg-secondary) 0%, var(--color-bg-tertiary) 100%);
}

.hero-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.hero-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
}

.hero-shape-1 {
  width: 400px;
  height: 400px;
  background: var(--color-primary-soft);
  top: -100px;
  right: -100px;
  animation: float 8s ease-in-out infinite;
}

.hero-shape-2 {
  width: 300px;
  height: 300px;
  background: rgba(184, 134, 11, 0.15);
  bottom: -50px;
  left: 10%;
  animation: float 10s ease-in-out infinite reverse;
}

.hero-shape-3 {
  width: 200px;
  height: 200px;
  background: rgba(45, 90, 90, 0.1);
  top: 50%;
  left: 30%;
  animation: float 6s ease-in-out infinite;
}

.hero-content {
  position: relative;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3xl);
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-text {
  max-width: 560px;
}

.hero-title {
  font-family: var(--font-display);
  font-size: clamp(2.5rem, 6vw, 4rem);
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: var(--space-lg);
}

.title-line {
  display: block;
}

.title-highlight {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  font-size: var(--text-xl);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2xl);
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-xl);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all var(--transition-base);
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  box-shadow: 0 4px 16px rgba(196, 92, 62, 0.3);
}

.btn-primary:hover {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(196, 92, 62, 0.4);
}

.btn-secondary {
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* Floating Cards Animation */
.hero-visual {
  position: relative;
  height: 400px;
}

.floating-cards {
  position: relative;
  width: 100%;
  height: 100%;
}

.float-card {
  position: absolute;
  width: 100px;
  height: 100px;
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-lg);
  animation: float 6s ease-in-out infinite;
}

.float-emoji {
  font-size: 48px;
}

.float-card-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.float-card-2 {
  top: 20%;
  right: 15%;
  animation-delay: 1.5s;
  width: 80px;
  height: 80px;
}

.float-card-3 {
  bottom: 25%;
  left: 20%;
  animation-delay: 3s;
  width: 90px;
  height: 90px;
}

.float-card-4 {
  bottom: 15%;
  right: 25%;
  animation-delay: 4.5s;
}

/* Hero Stats */
.hero-stats {
  position: relative;
  display: flex;
  justify-content: center;
  gap: var(--space-2xl);
  padding: var(--space-xl);
  margin-top: var(--space-3xl);
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
  max-width: 700px;
  margin-left: auto;
  margin-right: auto;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.stat-divider {
  width: 1px;
  background: var(--color-border);
}

/* ========== Section Common Styles ========== */
.section {
  margin-bottom: var(--space-4xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: var(--space-xl);
}

.section-title-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.section-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.section-desc {
  font-size: var(--text-base);
  color: var(--color-text-muted);
  margin: 0;
}

.section-link {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  color: var(--color-primary);
  font-weight: 500;
  text-decoration: none;
  transition: gap var(--transition-fast);
}

.section-link:hover {
  gap: var(--space-sm);
}

/* ========== Categories Section ========== */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: var(--space-md);
}

.category-card {
  position: relative;
  aspect-ratio: 1;
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  overflow: hidden;
  transition: all var(--transition-base);
}

.cat-card-bg {
  position: absolute;
  inset: 0;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  transition: all var(--transition-base);
}

.cat-style-1 .cat-card-bg {
  background: linear-gradient(135deg, #fff5f0 0%, var(--color-bg-card) 100%);
}

.cat-style-2 .cat-card-bg {
  background: linear-gradient(135deg, #f5f8ff 0%, var(--color-bg-card) 100%);
}

.cat-style-3 .cat-card-bg {
  background: linear-gradient(135deg, #f5fff5 0%, var(--color-bg-card) 100%);
}

.cat-style-4 .cat-card-bg {
  background: linear-gradient(135deg, #fff8f5 0%, var(--color-bg-card) 100%);
}

.category-card:hover .cat-card-bg {
  transform: scale(1.02);
  box-shadow: var(--shadow-lg);
}

.cat-card-content {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: var(--space-lg);
  gap: var(--space-sm);
}

.cat-emoji {
  font-size: 40px;
  transition: transform var(--transition-bounce);
}

.category-card:hover .cat-emoji {
  transform: scale(1.2) rotate(5deg);
}

.cat-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.cat-card-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.4) 50%,
    transparent 100%
  );
  transition: left 0.6s ease;
}

.category-card:hover .cat-card-shine {
  left: 100%;
}

/* ========== Goods Section ========== */
.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-4xl);
  text-align: center;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-lg);
}

.empty-state p {
  color: var(--color-text-muted);
  margin-bottom: var(--space-xl);
}

.error-message {
  grid-column: 1 / -1;
}

/* ========== Auction Section ========== */
.auction-section {
  margin: var(--space-4xl) 0;
}

.auction-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-3xl);
  padding: var(--space-3xl);
  background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-secondary-light) 100%);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  color: white;
}

.auction-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auction-pattern {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.auction-content {
  position: relative;
  max-width: 500px;
}

.auction-badge {
  display: inline-block;
  padding: var(--space-xs) var(--space-md);
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 600;
  margin-bottom: var(--space-md);
  backdrop-filter: blur(4px);
}

.auction-title {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  margin-bottom: var(--space-md);
}

.auction-desc {
  font-size: var(--text-lg);
  opacity: 0.9;
  line-height: 1.8;
  margin-bottom: var(--space-xl);
}

.auction-features {
  display: flex;
  gap: var(--space-lg);
  margin-bottom: var(--space-xl);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: var(--text-sm);
  opacity: 0.9;
}

.btn-auction {
  background: white;
  color: var(--color-secondary-dark);
  padding: var(--space-md) var(--space-xl);
  border-radius: var(--radius-lg);
  font-weight: 600;
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  transition: all var(--transition-base);
}

.btn-auction:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.auction-visual {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.auction-hammer {
  font-size: 120px;
  animation: float 4s ease-in-out infinite;
  filter: drop-shadow(0 10px 30px rgba(0, 0, 0, 0.2));
}

/* ========== Features Section ========== */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: var(--space-lg);
}

.feature-card {
  padding: var(--space-xl);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  text-align: center;
  transition: all var(--transition-base);
}

.feature-card:hover {
  border-color: var(--color-primary-soft);
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: var(--space-md);
}

.feature-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--space-sm);
}

.feature-desc {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
  line-height: 1.6;
}

/* ========== Animations ========== */
@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-15px);
  }
}

/* ========== Responsive ========== */
@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .hero-text {
    max-width: none;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-visual {
    display: none;
  }

  .auction-card {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .auction-features {
    justify-content: center;
    flex-wrap: wrap;
  }

  .auction-visual {
    display: none;
  }
}

@media (max-width: 640px) {
  .hero {
    padding: var(--space-2xl) var(--space-md);
  }

  .hero-stats {
    flex-direction: column;
    gap: var(--space-lg);
  }

  .stat-divider {
    width: 100%;
    height: 1px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-sm);
  }

  .auction-card {
    padding: var(--space-xl);
  }

  .auction-title {
    font-size: var(--text-2xl);
  }
}
</style>

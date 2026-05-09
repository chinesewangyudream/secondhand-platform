<!-- src/views/Home.vue -->
<template>
  <div class="home-page">
    <!-- Banner轮播 -->
    <el-carousel height="300px" class="banner">
      <el-carousel-item v-for="b in banners" :key="b.id">
        <div class="banner-item" :style="{ background: b.color }">
          <h2>{{ b.title }}</h2>
          <p>{{ b.desc }}</p>
          <el-button type="primary" size="large" @click="router.push(b.link)">
            {{ b.btn }}
          </el-button>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 热门分类 -->
    <div class="section">
      <div class="section-title">
        <h3>热门分类</h3>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in categories"
          :key="cat.id"
          class="cat-card"
          @click="searchByCategory(cat.id)"
        >
          <span class="cat-icon">{{ cat.icon }}</span>
          <span>{{ cat.name }}</span>
        </div>
      </div>
    </div>

    <!-- 最新商品 -->
    <div class="section">
      <div class="section-header">
        <h3>最新上架</h3>
        <el-link @click="router.push('/search')">查看更多 →</el-link>
      </div>
      <div class="goods-grid" v-loading="loading">
        <GoodsCard
          v-for="goods in goodsList"
          :key="goods.id"
          :goods="goods"
          @click="router.push('/goods/' + goods.id)"
        />
        <div v-if="error" class="error-message">
          <el-alert :title="error" type="error" show-icon />
        </div>
      </div>
    </div>

    <!-- 拍卖专区入口 -->
    <div class="auction-banner" @click="router.push('/auction')">
      <div class="auction-content">
        <h2>拍卖专区</h2>
        <p>好物竞拍，价格透明，公平公正</p>
        <el-button type="warning" size="large">立即参与</el-button>
      </div>
    </div>

    <!-- AI智能导购 -->
    <AiConsultant />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import GoodsCard from '@/components/GoodsCard.vue'
import AiConsultant from '@/components/AiConsultant.vue'

const router = useRouter()
const loading = ref(false)
const goodsList = ref([])
const categories = ref([])
const error = ref(null)

const banners = [
  { id: 1, title: '找好货，上闲置宝', desc: '海量优质二手商品等你来捡漏', btn: '立即逛逛', link: '/search', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { id: 2, title: '卖闲置，变现快', desc: '轻松发布，快速变现', btn: '立即发布', link: '/publish', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { id: 3, title: '竞拍专区上线', desc: '公平竞价，低价好物', btn: '参与竞拍', link: '/auction', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
]

onMounted(async () => {
  loading.value = true
  error.value = null
  try {
    const [goodsResult, catResult] = await Promise.allSettled([
      goodsApi.getList({ page: 1, size: 12 }),
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
      // Don't overwrite error if goods already failed
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
.home-page { max-width: 1200px; margin: 0 auto; }

.banner { margin-bottom: 40px; }
.banner-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: white;
  text-align: center;
  padding: 20px;
}
.banner-item h2 { margin: 0 0 10px 0; font-size: 32px; }
.banner-item p { margin: 0 0 20px 0; font-size: 18px; }

.section {
  margin-bottom: 40px;
  padding: 0 20px;
}
.section-title h3 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}
.cat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}
.cat-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
.cat-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-header h3 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.auction-banner {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  border-radius: 12px;
  padding: 40px;
  margin: 0 20px 40px 20px;
  cursor: pointer;
  text-align: center;
  color: white;
}
.auction-content h2 {
  font-size: 28px;
  margin: 0 0 10px 0;
}
.auction-content p {
  font-size: 16px;
  margin: 0 0 20px 0;
  opacity: 0.9;
}

.error-message {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px;
}
</style>

<!-- src/views/Favorites.vue -->
<template>
  <div class="favorites-page">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>

    <div class="favorites-content" v-loading="loading">
      <div v-if="favoritesList.length === 0" class="no-favorites">
        <el-empty description="暂无收藏商品">
          <el-button type="primary" @click="router.push('/home')">去逛逛</el-button>
        </el-empty>
      </div>

      <div v-else class="goods-grid">
        <div
          v-for="item in favoritesList"
          :key="item.id"
          class="goods-card"
          @click="viewGoods(item.goods)"
        >
          <div class="goods-image">
            <img :src="getFirstImage(item.goods?.images)" :alt="item.goods?.title" />
            <div v-if="item.goods?.status !== 1" class="off-shelf-badge">
              {{ getStatusText(item.goods?.status) }}
            </div>
          </div>
          <div class="goods-info">
            <h4>{{ item.goods?.title || '商品已下架' }}</h4>
            <p class="price">¥{{ item.goods?.price }}</p>
            <p class="collect-time">收藏于 {{ formatDate(item.createdAt) }}</p>
            <div class="actions">
              <el-button
                size="small"
                type="primary"
                :disabled="item.goods?.status !== 1"
                @click.stop="contactSeller(item.goods)"
              >
                联系卖家
              </el-button>
              <el-button
                size="small"
                type="danger"
                plain
                @click.stop="removeFavorite(item.goodsId)"
              >
                取消收藏
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const favoritesList = ref([])

onMounted(async () => {
  await loadFavorites()
})

async function loadFavorites() {
  loading.value = true
  try {
    const res = await goodsApi.getMyFavorites()
    favoritesList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

function getFirstImage(images) {
  if (!images) return ''
  try {
    const arr = typeof images === 'string' ? JSON.parse(images) : images
    return arr[0] || ''
  } catch {
    return images
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function getStatusText(status) {
  const map = {
    0: '待审核',
    1: '在售',
    2: '已售出',
    3: '已下架',
    4: '拍卖中'
  }
  return map[status] || '未知'
}

function viewGoods(goods) {
  if (goods?.id) {
    router.push(`/goods/${goods.id}`)
  }
}

async function contactSeller(goods) {
  if (!goods) return

  if (Number(userStore.userInfo?.id) === Number(goods.sellerId)) {
    ElMessage.warning('这是您自己发布的商品')
    return
  }

  router.push({
    path: '/chat',
    query: {
      sellerId: goods.sellerId,
      goodsId: goods.id
    }
  })
}

async function removeFavorite(goodsId) {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await goodsApi.removeFavorite(goodsId)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}
</script>

<style scoped>
.favorites-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.favorites-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
  min-height: 400px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.goods-card {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
  cursor: pointer;
}

.goods-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.goods-image {
  position: relative;
  padding-top: 75%;
  overflow: hidden;
  background: #f5f5f5;
}

.goods-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.off-shelf-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #909399;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.goods-info {
  padding: 16px;
}

.goods-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price {
  font-size: 18px;
  color: #f5222d;
  font-weight: 600;
  margin: 8px 0;
}

.collect-time {
  font-size: 12px;
  color: #999;
  margin: 4px 0;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.no-favorites {
  padding: 60px 20px;
  text-align: center;
}
</style>

<!-- src/views/Auction.vue -->
<template>
  <div class="auction-page">
    <div class="page-header">
      <h2>拍卖专区</h2>
      <p>公平竞价，优质好物</p>
    </div>

    <div class="auction-list" v-loading="loading">
      <div v-if="auctions.length === 0" class="no-auctions">
        <p>暂无拍卖商品</p>
      </div>
      <div v-else class="auction-grid">
        <div
          v-for="auction in auctions"
          :key="auction.id"
          class="auction-card"
          @click="router.push(`/auction/${auction.id}`)"
        >
          <div class="auction-image">
            <img :src="auction.image" :alt="auction.title" />
            <div class="auction-badge">拍卖中</div>
          </div>
          <div class="auction-info">
            <h3 class="auction-title">{{ auction.title }}</h3>
            <div class="auction-price">
              <div class="current-bid">当前价: ¥{{ auction.currentPrice }}</div>
              <div class="bid-count">{{ auction.bidCount }} 人竞拍</div>
            </div>
            <div class="auction-time">
              <el-icon><Timer /></el-icon>
              <span>结束时间: {{ formatTime(auction.endTime) }}</span>
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
import { auctionApi } from '@/api/auction'
import { Timer } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const auctions = ref([])

onMounted(async () => {
  await loadAuctions()
})

async function loadAuctions() {
  loading.value = true
  try {
    const res = await auctionApi.getList()
    // 后端返回的是 { auction, goods } 结构，需要转换
    const records = res.data?.records || res.data || []
    auctions.value = records
      .filter(item => item != null && item.auction && item.goods)
      .map(item => ({
        id: item.auction.id,
        goodsId: item.auction.goodsId,
        title: item.goods.title,
        currentPrice: item.auction.currentPrice,
        startPrice: item.auction.startPrice,
        bidCount: item.auction.bidCount,
        endTime: item.auction.endTime,
        startTime: item.auction.startTime,
        status: item.auction.status,
        image: item.goods.images ? JSON.parse(item.goods.images)[0] : null,
        buyNowPrice: item.auction.buyNowPrice
      }))
  } catch (error) {
    console.error('加载拍卖列表失败:', error)
    ElMessage.error('加载拍卖列表失败')
  } finally {
    loading.value = false
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped>
.auction-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

.auction-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.auction-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.auction-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
}

.auction-image {
  position: relative;
  padding-top: 75%;
  overflow: hidden;
  background: #f5f7fa;
}

.auction-image img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.auction-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #f5222d;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.auction-info {
  padding: 16px;
}

.auction-title {
  font-size: 16px;
  color: #333;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.auction-price {
  margin-bottom: 12px;
}

.current-bid {
  font-size: 18px;
  color: #f5222d;
  font-weight: 600;
}

.bid-count {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.auction-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.no-auctions {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}
</style>
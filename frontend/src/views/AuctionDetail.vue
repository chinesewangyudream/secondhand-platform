<!-- src/views/AuctionDetail.vue -->
<template>
  <div class="auction-detail-page" v-loading="loading">
    <div v-if="auction" class="detail-container">
      <!-- 左侧图片区 -->
      <div class="image-section">
        <el-image
          :src="currentImage"
          fit="contain"
          class="main-image"
        />
        <div class="thumbnail-list" v-if="auctionImages.length > 1">
          <img
            v-for="(img, idx) in auctionImages"
            :key="idx"
            :src="img"
            :class="['thumbnail', { active: currentImageIdx === idx }]"
            @click="currentImageIdx = idx"
          />
        </div>
      </div>

      <!-- 右侧信息区 -->
      <div class="info-section">
        <h1 class="auction-title">{{ auction.title }}</h1>

        <!-- 拍卖状态和倒计时 -->
        <div class="status-countdown">
          <el-tag :type="getStatusType(auction.status)" size="large">
            {{ getStatusText(auction.status) }}
          </el-tag>
          <div class="countdown" v-if="timeLeft > 0">
            <el-icon><Timer /></el-icon>
            <span class="time">{{ formatCountdown(timeLeft) }}</span>
          </div>
          <div class="countdown ended" v-else-if="auction.status === 2">
            <span>拍卖已结束</span>
          </div>
        </div>

        <!-- 拍卖价格区 -->
        <div class="price-section">
          <div class="price-row">
            <span class="label">当前价</span>
            <span class="current-price">¥{{ auction.currentPrice }}</span>
          </div>
          <div class="price-row">
            <span class="label">起拍价</span>
            <span class="starting-price">¥{{ auction.startPrice }}</span>
          </div>
          <div class="price-row" v-if="auction.minIncrement">
            <span class="label">加价幅度</span>
            <span>¥{{ auction.minIncrement }}</span>
          </div>
          <div class="price-row" v-if="auction.buyNowPrice">
            <span class="label">一口价</span>
            <span class="buy-now-price">¥{{ auction.buyNowPrice }}</span>
          </div>
          <div class="price-row">
            <span class="label">出价次数</span>
            <span class="bid-count">{{ auction.bidCount || 0 }} 次</span>
          </div>
        </div>

        <!-- 拍卖时间 -->
        <div class="time-section">
          <div class="time-item">
            <el-icon><Clock /></el-icon>
            <span>开始: {{ formatTime(auction.startTime) }}</span>
          </div>
          <div class="time-item">
            <el-icon><Clock /></el-icon>
            <span>结束: {{ formatTime(auction.endTime) }}</span>
          </div>
        </div>

        <!-- 出价区域 -->
        <div class="bid-section" v-if="canBid">
          <el-input-number
            v-model="bidAmount"
            :min="minBid"
            :step="auction.minIncrement || 1"
            :precision="2"
            controls-position="right"
            placeholder="请输入出价"
            class="bid-input"
          />
          <div class="bid-buttons">
            <el-button type="primary" size="large" @click="placeBid" :loading="bidding">
              出价竞拍
            </el-button>
            <el-button
              v-if="auction.buyNowPrice"
              type="danger"
              size="large"
              @click="buyNow"
              :loading="bidding"
            >
              一口价 ¥{{ auction.buyNowPrice }}
            </el-button>
          </div>
          <p class="bid-hint">最低出价: ¥{{ minBid }}</p>
        </div>

        <!-- 拍卖结果 -->
        <div class="result-section" v-if="auction.status === 2">
          <el-result
            v-if="auction.winnerId"
            icon="success"
            title="拍卖已成交"
          >
            <template #sub-title>
              成交价: ¥{{ auction.currentPrice }}
            </template>
          </el-result>
          <el-result
            v-else
            icon="warning"
            title="拍卖流拍"
            sub-title="无人出价"
          />
        </div>
      </div>
    </div>

    <!-- 商品描述和历史出价 -->
    <div v-if="auction" class="bottom-section">
      <!-- 商品描述 -->
      <div class="description-section">
        <h3>商品描述</h3>
        <div class="description">{{ auction.description || '暂无描述' }}</div>
        <div class="location" v-if="auction.location">
          <el-icon><Location /></el-icon>
          <span>{{ auction.location }}</span>
        </div>
      </div>

      <!-- 历史出价 -->
      <div class="bid-history-section">
        <h3>出价记录 ({{ bidHistory.length }}次)</h3>
        <div class="bid-history" v-if="bidHistory.length > 0">
          <div class="bid-item" v-for="(bid, index) in bidHistory" :key="bid.id">
            <div class="bid-rank">{{ index + 1 }}</div>
            <div class="bid-info">
              <div class="bid-user">
                <span class="user-name">{{ bid.userName || '用户' + bid.userId }}</span>
                <el-tag v-if="index === 0" type="danger" size="small">领先</el-tag>
              </div>
              <div class="bid-time">{{ formatTime(bid.createdAt) }}</div>
            </div>
            <div class="bid-price">¥{{ bid.bidPrice }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无出价记录" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { auctionApi } from '@/api/auction'
import { useUserStore } from '@/store/user'
import { Timer, Clock, Location } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const auction = ref(null)
const currentImageIdx = ref(0)
const bidAmount = ref(0)
const bidding = ref(false)
const timeLeft = ref(0)
const bidHistory = ref([])
let countdownTimer = null

const auctionImages = computed(() => {
  if (!auction.value?.images) return []
  try {
    return JSON.parse(auction.value.images)
  } catch {
    return auction.value.images || []
  }
})

const currentImage = computed(() => {
  return auctionImages.value[currentImageIdx.value] || 'https://via.placeholder.com/400'
})

const minBid = computed(() => {
  if (!auction.value) return 0
  return Number(auction.value.currentPrice) + Number(auction.value.minIncrement || 1)
})

const canBid = computed(() => {
  if (!auction.value || !userStore.isLoggedIn) return false
  if (auction.value.status !== 0 && auction.value.status !== 1) return false
  // 不能对自己的商品出价
  if (auction.value.sellerId === userStore.userInfo?.id) return false
  return timeLeft.value > 0
})

onMounted(async () => {
  await loadAuctionDetail()
  await loadBidHistory()
  startCountdown()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})

async function loadAuctionDetail() {
  loading.value = true
  try {
    const auctionId = route.params.id
    const res = await auctionApi.getDetail(auctionId)
    auction.value = res.data
    bidAmount.value = minBid.value
  } catch (error) {
    ElMessage.error(error.showMessage || '加载拍卖详情失败')
  } finally {
    loading.value = false
  }
}

async function loadBidHistory() {
  try {
    const auctionId = route.params.id
    const res = await auctionApi.getBidHistory(auctionId)
    bidHistory.value = res.data || []
  } catch (error) {
    console.error('加载出价历史失败:', error)
  }
}

function startCountdown() {
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000)
}

function updateCountdown() {
  if (auction.value?.endTime) {
    const endTime = new Date(auction.value.endTime).getTime()
    const now = Date.now()
    timeLeft.value = Math.max(0, endTime - now)

    // 倒计时结束，刷新数据
    if (timeLeft.value === 0 && auction.value.status !== 2) {
      loadAuctionDetail()
    }
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

function formatCountdown(ms) {
  if (ms <= 0) return '已结束'

  const days = Math.floor(ms / (1000 * 60 * 60 * 24))
  const hours = Math.floor((ms % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((ms % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((ms % (1000 * 60)) / 1000)

  if (days > 0) {
    return `${days}天 ${hours}时 ${minutes}分 ${seconds}秒`
  } else if (hours > 0) {
    return `${hours}时 ${minutes}分 ${seconds}秒`
  } else if (minutes > 0) {
    return `${minutes}分 ${seconds}秒`
  } else {
    return `${seconds}秒`
  }
}

function getStatusText(status) {
  const statusMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statusMap[status] || '未知'
}

function getStatusType(status) {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

async function placeBid() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!bidAmount.value || bidAmount.value < minBid.value) {
    ElMessage.warning(`出价必须不低于 ¥${minBid.value}`)
    return
  }

  // 出价确认弹窗
  try {
    await ElMessageBox.confirm(
      `您即将出价 ¥${bidAmount.value} 竞拍该商品，确认出价吗？`,
      '确认出价',
      {
        confirmButtonText: '确认出价',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
  } catch {
    return // 用户取消
  }

  bidding.value = true
  try {
    await auctionApi.bid(auction.value.id, bidAmount.value)
    ElMessage.success('出价成功')
    await Promise.all([loadAuctionDetail(), loadBidHistory()])
  } catch (error) {
    ElMessage.error(error.showMessage || error.response?.data?.message || '出价失败')
  } finally {
    bidding.value = false
  }
}

async function buyNow() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认以一口价 ¥${auction.value.buyNowPrice} 购买该商品？`,
      '确认购买',
      { confirmButtonText: '确认购买', cancelButtonText: '取消', type: 'warning' }
    )

    bidding.value = true
    await auctionApi.bid(auction.value.id, auction.value.buyNowPrice)
    ElMessage.success('购买成功')
    await loadAuctionDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '购买失败')
    }
  } finally {
    bidding.value = false
  }
}
</script>

<style scoped>
.auction-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.detail-container {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
}

.image-section {
  flex: 1;
  max-width: 500px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  background: #f5f5f5;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.thumbnail {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  opacity: 0.7;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.thumbnail:hover,
.thumbnail.active {
  opacity: 1;
  border-color: #409EFF;
}

.info-section {
  flex: 1;
}

.auction-title {
  font-size: 24px;
  margin-bottom: 16px;
  color: #333;
}

.status-countdown {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
  border-radius: 20px;
  color: white;
  font-weight: 500;
}

.countdown.ended {
  background: #999;
}

.countdown .time {
  font-size: 16px;
}

.price-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px dashed #e8e8e8;
}

.price-row:last-child {
  border-bottom: none;
}

.price-row .label {
  color: #666;
  font-size: 14px;
}

.current-price {
  font-size: 28px;
  color: #f5222d;
  font-weight: bold;
}

.starting-price {
  font-size: 16px;
  color: #666;
}

.buy-now-price {
  font-size: 18px;
  color: #E6A23C;
  font-weight: 500;
}

.bid-count {
  color: #409EFF;
  font-weight: 500;
}

.time-section {
  margin-bottom: 20px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  margin-bottom: 8px;
}

.bid-section {
  background: #fff8f0;
  border: 1px solid #ffd591;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.bid-input {
  width: 100%;
  margin-bottom: 12px;
}

.bid-buttons {
  display: flex;
  gap: 12px;
}

.bid-buttons .el-button {
  flex: 1;
}

.bid-hint {
  margin-top: 12px;
  font-size: 12px;
  color: #999;
  text-align: center;
}

.result-section {
  margin-top: 20px;
}

.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.description-section,
.bid-history-section {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.description-section h3,
.bid-history-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
  border-bottom: 2px solid #409EFF;
  padding-bottom: 8px;
}

.description {
  line-height: 1.8;
  color: #666;
  margin-bottom: 16px;
}

.location {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
}

.bid-history {
  max-height: 400px;
  overflow-y: auto;
}

.bid-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.bid-item:hover {
  background: #f9f9f9;
}

.bid-rank {
  width: 28px;
  height: 28px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  margin-right: 12px;
}

.bid-item:first-child .bid-rank {
  background: #f5222d;
  color: white;
}

.bid-info {
  flex: 1;
}

.bid-user {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.bid-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.bid-price {
  font-size: 18px;
  font-weight: 600;
  color: #f5222d;
}

@media (max-width: 768px) {
  .detail-container {
    flex-direction: column;
  }

  .bottom-section {
    grid-template-columns: 1fr;
  }

  .image-section {
    max-width: 100%;
  }
}
</style>

<!-- src/views/GoodsDetail.vue -->
<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="goods">
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        <router-link to="/home">首页</router-link>
        <span class="separator">/</span>
        <router-link to="/search">商品</router-link>
        <span class="separator">/</span>
        <span class="current">{{ goods.title }}</span>
      </nav>

      <!-- Main Content -->
      <div class="detail-main">
        <!-- Left: Images -->
        <div class="gallery-section">
          <div class="gallery-main">
            <el-image
              :src="currentImage"
              fit="contain"
              class="main-image"
              :preview-src-list="goodsImages"
              :initial-index="currentImageIdx"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="64"><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
              </template>
            </el-image>

            <!-- Navigation Arrows -->
            <button
              v-if="goodsImages.length > 1"
              class="nav-arrow nav-prev"
              @click="prevImage"
            >
              <el-icon><ArrowLeft /></el-icon>
            </button>
            <button
              v-if="goodsImages.length > 1"
              class="nav-arrow nav-next"
              @click="nextImage"
            >
              <el-icon><ArrowRight /></el-icon>
            </button>

            <!-- Image Counter -->
            <div v-if="goodsImages.length > 1" class="image-counter">
              {{ currentImageIdx + 1 }} / {{ goodsImages.length }}
            </div>
          </div>

          <!-- Thumbnails -->
          <div v-if="goodsImages.length > 1" class="gallery-thumbs">
            <button
              v-for="(img, idx) in goodsImages"
              :key="idx"
              :class="['thumb-btn', { active: currentImageIdx === idx }]"
              @click="currentImageIdx = idx"
            >
              <img :src="img" :alt="`图片${idx + 1}`" />
            </button>
          </div>
        </div>

        <!-- Right: Info -->
        <div class="info-section">
          <!-- Status Tags -->
          <div class="status-tags">
            <span v-if="goods.isAuction" class="tag tag-auction">
              <el-icon><Timer /></el-icon>
              拍卖中
            </span>
            <span v-if="goods.status === 2" class="tag tag-sold">已售出</span>
            <span class="tag tag-id">ID: {{ goods.id }}</span>
          </div>

          <!-- Title -->
          <h1 class="goods-title">{{ goods.title }}</h1>

          <!-- Price Card -->
          <div class="price-card">
            <div class="price-row">
              <div class="price-main">
                <span class="price-label">售价</span>
                <div class="price-value">
                  <span class="currency">¥</span>
                  <span class="amount">{{ formatPrice(goods.price) }}</span>
                </div>
              </div>
              <div v-if="goods.originalPrice" class="price-original">
                <span class="original-label">原价</span>
                <span class="original-value">¥{{ formatPrice(goods.originalPrice) }}</span>
                <span class="discount">省 ¥{{ formatPrice(goods.originalPrice - goods.price) }}</span>
              </div>
            </div>

            <!-- AI Estimate -->
            <div v-if="goods.aiEstimatedPrice" class="ai-estimate">
              <div class="ai-badge">
                <el-icon><Cpu /></el-icon>
                <span>AI 智能估价</span>
              </div>
              <div class="ai-price">¥{{ formatPrice(goods.aiEstimatedPrice) }}</div>
              <el-tooltip content="由AI根据商品信息智能估算的参考价格" placement="top">
                <el-icon class="ai-help"><QuestionFilled /></el-icon>
              </el-tooltip>
            </div>
          </div>

          <!-- Attributes -->
          <div class="attributes">
            <div class="attr-item">
              <div class="attr-icon">
                <el-icon><Medal /></el-icon>
              </div>
              <div class="attr-content">
                <span class="attr-label">成色等级</span>
                <span class="attr-value">{{ conditionLabel }}</span>
              </div>
            </div>
            <div class="attr-item">
              <div class="attr-icon">
                <el-icon><Location /></el-icon>
              </div>
              <div class="attr-content">
                <span class="attr-label">发货地区</span>
                <span class="attr-value">{{ goods.location || '未设置' }}</span>
              </div>
            </div>
            <div class="attr-item">
              <div class="attr-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="attr-content">
                <span class="attr-label">发布时间</span>
                <span class="attr-value">{{ formatDate(goods.createdAt) }}</span>
              </div>
            </div>
            <div class="attr-item">
              <div class="attr-icon">
                <el-icon><View /></el-icon>
              </div>
              <div class="attr-content">
                <span class="attr-label">浏览次数</span>
                <span class="attr-value">{{ goods.viewCount || 0 }} 次</span>
              </div>
            </div>
          </div>

          <!-- Seller Card -->
          <div class="seller-card">
            <div class="seller-info">
              <el-avatar :size="48" :src="goods.sellerAvatar" class="seller-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="seller-detail">
                <span class="seller-name">{{ goods.sellerName || '匿名卖家' }}</span>
                <span class="seller-joined">注册用户</span>
              </div>
            </div>
            <div class="seller-stats">
              <div class="stat">
                <span class="stat-value">{{ goods.sellerGoodsCount || 0 }}</span>
                <span class="stat-label">在售</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ goods.sellerSoldCount || 0 }}</span>
                <span class="stat-label">已售</span>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="action-buttons">
            <button class="btn btn-primary btn-lg" @click="contactSeller">
              <el-icon><ChatDotRound /></el-icon>
              <span>联系卖家</span>
            </button>
            <button
              class="btn btn-lg"
              :class="isFavorited ? 'btn-favorited' : 'btn-outline'"
              :loading="favoriteLoading"
              @click="toggleFavorite"
            >
              <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
              <span>{{ isFavorited ? '已收藏' : '收藏' }}</span>
            </button>
            <!-- 审核员标记违规 -->
            <button
              v-if="isAuditor && (goods.status === 1 || goods.status === 4)"
              class="btn btn-danger btn-lg"
              @click="showViolationDialog"
            >
              <el-icon><Warning /></el-icon>
              <span>标记违规</span>
            </button>
            <!-- 普通用户举报 -->
            <button
              v-if="!isAuditor && (goods.status === 1 || goods.status === 4) && isNotMyGoods"
              class="btn btn-outline btn-lg"
              @click="showReportDialog"
            >
              <el-icon><Warning /></el-icon>
              <span>举报</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Description Section -->
      <div class="description-section">
        <div class="section-header">
          <h2 class="section-title">商品描述</h2>
        </div>
        <div class="description-content">
          <div v-if="goods.description" class="description-text" v-html="goods.description"></div>
          <div v-else class="description-empty">
            <el-icon :size="32"><Document /></el-icon>
            <span>暂无描述</span>
          </div>
        </div>
      </div>
    </template>

    <!-- Violation Dialog -->
    <el-dialog
      v-model="violationDialogVisible"
      title="标记商品违规"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="violationForm" label-width="100px">
        <el-form-item label="商品标题">
          <span>{{ goods?.title }}</span>
        </el-form-item>
        <el-form-item label="违规原因" required>
          <el-select v-model="violationForm.reason" placeholder="请选择违规原因" style="width: 100%">
            <el-option label="涉嫌出售违禁品" value="涉嫌出售违禁品" />
            <el-option label="商品描述与实际不符" value="商品描述与实际不符" />
            <el-option label="图片涉嫌侵权" value="图片涉嫌侵权" />
            <el-option label="价格异常" value="价格异常" />
            <el-option label="涉嫌欺诈" value="涉嫌欺诈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="violationForm.reason === '其他'" label="详细说明" required>
          <el-input
            v-model="violationForm.customReason"
            type="textarea"
            :rows="3"
            placeholder="请输入详细的违规原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="violationDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleMarkViolation" :loading="violationLoading">
          确认标记违规
        </el-button>
      </template>
    </el-dialog>

    <!-- Report Dialog -->
    <el-dialog
      v-model="reportDialogVisible"
      title="举报商品"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="reportForm" label-width="100px">
        <el-form-item label="商品标题">
          <span>{{ goods?.title }}</span>
        </el-form-item>
        <el-form-item label="举报原因" required>
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width: 100%">
            <el-option label="涉嫌出售违禁品" value="涉嫌出售违禁品" />
            <el-option label="商品描述与实际不符" value="商品描述与实际不符" />
            <el-option label="涉嫌欺诈" value="涉嫌欺诈" />
            <el-option label="图片涉嫌侵权" value="图片涉嫌侵权" />
            <el-option label="假冒伪劣产品" value="假冒伪劣产品" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="reportForm.reason === '其他'" label="详细说明" required>
          <el-input
            v-model="reportForm.customReason"
            type="textarea"
            :rows="3"
            placeholder="请输入详细的举报原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleReport" :loading="reportLoading">
          提交举报
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { reportApi } from '@/api/report'
import { useUserStore } from '@/store/user'
import {
  Cpu, QuestionFilled, Star, StarFilled, Picture,
  ArrowLeft, ArrowRight, Timer, Medal, Location, Clock,
  View, User, ChatDotRound, Warning, Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const goods = ref(null)
const currentImageIdx = ref(0)
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// Violation dialog
const violationDialogVisible = ref(false)
const violationLoading = ref(false)
const violationForm = ref({ reason: '', customReason: '' })

// Report dialog
const reportDialogVisible = ref(false)
const reportLoading = ref(false)
const reportForm = ref({ reason: '', customReason: '' })

const goodsImages = computed(() => goods.value?.images || [])
const currentImage = computed(() => goodsImages.value[currentImageIdx.value] || '')

const isAuditor = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'ADMIN' || role === 'AUDITOR'
})

const isNotMyGoods = computed(() => {
  if (!goods.value) return false
  if (!userStore.userInfo?.id) return true
  return Number(userStore.userInfo.id) !== Number(goods.value.sellerId)
})

const conditionLabel = computed(() => {
  const level = goods.value?.conditionLevel || 7
  if (level >= 9) return '全新未拆'
  if (level >= 8) return '几乎全新'
  if (level >= 6) return '轻微使用痕迹'
  if (level >= 4) return '明显使用痕迹'
  return '有明显瑕疵'
})

onMounted(async () => {
  await loadGoodsDetail()
})

async function loadGoodsDetail() {
  loading.value = true
  try {
    const goodsId = route.params.id
    const res = await goodsApi.getDetail(goodsId)
    goods.value = res.data

    if (userStore.isLoggedIn) {
      try {
        const favRes = await goodsApi.checkFavorite(goodsId)
        isFavorited.value = favRes.data === true
      } catch (e) {
        isFavorited.value = false
      }
    }
  } catch (error) {
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

function prevImage() {
  if (currentImageIdx.value > 0) {
    currentImageIdx.value--
  } else {
    currentImageIdx.value = goodsImages.value.length - 1
  }
}

function nextImage() {
  if (currentImageIdx.value < goodsImages.value.length - 1) {
    currentImageIdx.value++
  } else {
    currentImageIdx.value = 0
  }
}

function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

function contactSeller() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!goods.value) return
  const sellerId = goods.value.sellerId
  const goodsId = goods.value.id
  if (Number(userStore.userInfo?.id) === Number(sellerId)) {
    ElMessage.warning('这是您自己发布的商品')
    return
  }
  router.push({ path: '/chat', query: { sellerId, goodsId } })
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!goods.value) return
  if (Number(userStore.userInfo?.id) === Number(goods.value.sellerId)) {
    ElMessage.warning('不能收藏自己发布的商品')
    return
  }

  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      await goodsApi.removeFavorite(goods.value.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await goodsApi.addFavorite(goods.value.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    ElMessage.error(error.showMessage || '操作失败')
  } finally {
    favoriteLoading.value = false
  }
}

function showViolationDialog() {
  violationForm.value = { reason: '', customReason: '' }
  violationDialogVisible.value = true
}

async function handleMarkViolation() {
  if (!violationForm.value.reason) {
    ElMessage.warning('请选择违规原因')
    return
  }
  if (violationForm.value.reason === '其他' && !violationForm.value.customReason) {
    ElMessage.warning('请输入详细的违规原因')
    return
  }

  const reason = violationForm.value.reason === '其他'
    ? violationForm.value.customReason
    : violationForm.value.reason

  violationLoading.value = true
  try {
    await goodsApi.markAsViolation(goods.value.id, reason)
    ElMessage.success('已标记为违规商品')
    violationDialogVisible.value = false
    await loadGoodsDetail()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  } finally {
    violationLoading.value = false
  }
}

function showReportDialog() {
  reportForm.value = { reason: '', customReason: '' }
  reportDialogVisible.value = true
}

async function handleReport() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (!reportForm.value.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  if (reportForm.value.reason === '其他' && !reportForm.value.customReason) {
    ElMessage.warning('请输入详细的举报原因')
    return
  }

  const reason = reportForm.value.reason === '其他'
    ? reportForm.value.customReason
    : reportForm.value.reason

  reportLoading.value = true
  try {
    await reportApi.submit({ goodsId: goods.value.id, reason })
    ElMessage.success('举报成功，我们会尽快处理')
    reportDialogVisible.value = false
  } catch (error) {
    ElMessage.error('举报失败：' + (error.response?.data?.message || error.message))
  } finally {
    reportLoading.value = false
  }
}
</script>

<style scoped>
.detail-page {
  max-width: 1200px;
  margin: 0 auto;
  animation: fadeIn 0.4s ease-out;
}

/* Breadcrumb */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
  font-size: var(--text-sm);
}

.breadcrumb a {
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.breadcrumb a:hover {
  color: var(--color-primary);
}

.separator {
  color: var(--color-border-dark);
}

.breadcrumb .current {
  color: var(--color-text-primary);
  font-weight: 500;
}

/* Main Layout */
.detail-main {
  display: grid;
  grid-template-columns: 1fr 480px;
  gap: var(--space-3xl);
  margin-bottom: var(--space-3xl);
}

/* Gallery */
.gallery-section {
  position: sticky;
  top: 120px;
  align-self: start;
}

.gallery-main {
  position: relative;
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  aspect-ratio: 4/3;
  box-shadow: var(--shadow-lg);
}

.main-image {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: var(--space-md);
  color: var(--color-text-muted);
}

.nav-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-card);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  box-shadow: var(--shadow-md);
  transition: all var(--transition-fast);
  opacity: 0;
}

.gallery-main:hover .nav-arrow {
  opacity: 1;
}

.nav-arrow:hover {
  background: var(--color-primary);
  color: white;
}

.nav-prev { left: var(--space-md); }
.nav-next { right: var(--space-md); }

.image-counter {
  position: absolute;
  bottom: var(--space-md);
  right: var(--space-md);
  padding: var(--space-xs) var(--space-md);
  background: rgba(0, 0, 0, 0.6);
  border-radius: var(--radius-full);
  color: white;
  font-size: var(--text-xs);
}

.gallery-thumbs {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-md);
  overflow-x: auto;
  padding: var(--space-xs);
}

.thumb-btn {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  padding: 2px;
  background: var(--color-bg-card);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  overflow: hidden;
}

.thumb-btn:hover,
.thumb-btn.active {
  border-color: var(--color-primary);
}

.thumb-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
}

/* Info Section */
.info-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.status-tags {
  display: flex;
  gap: var(--space-sm);
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
}

.tag-auction {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
}

.tag-sold {
  background: var(--color-text-muted);
  color: white;
}

.tag-id {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
}

.goods-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 600;
  line-height: 1.4;
  margin: 0;
  color: var(--color-text-primary);
}

/* Price Card */
.price-card {
  padding: var(--space-lg);
  background: linear-gradient(135deg, var(--color-bg-secondary) 0%, var(--color-bg-tertiary) 100%);
  border-radius: var(--radius-xl);
}

.price-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}

.price-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.price-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.price-value {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-primary);
  margin-right: 2px;
}

.amount {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.price-original {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.original-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.original-value {
  font-size: var(--text-base);
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.discount {
  font-size: var(--text-xs);
  color: var(--color-success);
  font-weight: 500;
}

.ai-estimate {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding-top: var(--space-md);
  border-top: 1px solid var(--color-border-light);
}

.ai-badge {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-xs) var(--space-sm);
  background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-secondary-light) 100%);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  font-weight: 600;
  color: white;
}

.ai-price {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-secondary-dark);
}

.ai-help {
  color: var(--color-text-muted);
  cursor: help;
}

/* Attributes */
.attributes {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md);
}

.attr-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}

.attr-item:hover {
  border-color: var(--color-primary-soft);
  box-shadow: var(--shadow-sm);
}

.attr-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-md);
  color: var(--color-primary);
}

.attr-content {
  display: flex;
  flex-direction: column;
}

.attr-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.attr-value {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-primary);
}

/* Seller Card */
.seller-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-lg);
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
}

.seller-info {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.seller-avatar {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.seller-detail {
  display: flex;
  flex-direction: column;
}

.seller-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.seller-joined {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.seller-stats {
  display: flex;
  gap: var(--space-xl);
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: all var(--transition-base);
}

.btn-lg {
  padding: var(--space-md) var(--space-xl);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
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

.btn-outline {
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.btn-outline:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-favorited {
  background: var(--color-primary-soft);
  color: var(--color-primary);
  border: 1px solid var(--color-primary);
}

.btn-danger {
  background: var(--color-danger);
  color: white;
}

.btn-danger:hover {
  opacity: 0.9;
}

/* Description Section */
.description-section {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-2xl);
  padding: var(--space-xl);
}

.section-header {
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--color-border-light);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.description-content {
  min-height: 120px;
}

.description-text {
  line-height: 1.8;
  color: var(--color-text-secondary);
}

.description-text :deep(p) {
  margin-bottom: var(--space-md);
}

.description-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-3xl);
  color: var(--color-text-muted);
  gap: var(--space-sm);
}

/* Responsive */
@media (max-width: 1024px) {
  .detail-main {
    grid-template-columns: 1fr;
  }

  .gallery-section {
    position: static;
  }
}

@media (max-width: 640px) {
  .detail-page {
    padding: var(--space-sm);
  }

  .attributes {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .btn {
    width: 100%;
  }
}
</style>

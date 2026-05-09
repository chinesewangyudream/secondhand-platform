<!-- src/views/GoodsDetail.vue -->
<template>
  <div class="detail-page" v-loading="loading">
    <div v-if="goods" class="detail-container">
      <!-- 左侧图片区 -->
      <div class="image-section">
        <el-image
          :src="currentImage"
          fit="contain"
          class="main-image"
        />
        <div class="thumbnail-list">
          <img
            v-for="(img, idx) in goodsImages"
            :key="idx"
            :src="img"
            :class="['thumbnail', { active: currentImageIdx === idx }]"
            @click="currentImageIdx = idx"
          />
        </div>
      </div>

      <!-- 右侧信息区 -->
      <div class="info-section">
        <h1 class="goods-title">{{ goods.title }}</h1>

        <!-- 价格区 -->
        <div class="price-section">
          <div class="current-price">¥ {{ goods.price }}</div>
          <div v-if="goods.originalPrice" class="original">
            原价: ¥{{ goods.originalPrice }}
          </div>
          <!-- AI估价 -->
          <div v-if="goods.aiEstimatedPrice" class="ai-estimate">
            <el-icon color="#E6A23C"><Cpu /></el-icon>
            <span>AI估价参考: ¥{{ goods.aiEstimatedPrice }}</span>
            <el-tooltip content="由AI根据商品信息智能估算的参考价格" placement="top">
              <el-icon color="#ccc"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
        </div>

        <!-- 商品属性 -->
        <div class="attrs">
          <div class="attr-row">
            <span class="label">成色</span>
            <el-rate
              :model-value="(goods.conditionLevel || 7) / 2"
              disabled
              show-score
              :max="5"
              score-template="{value}成新"
            />
          </div>
          <div class="attr-row">
            <span class="label">地区</span>
            <span>{{ goods.location || '未填写' }}</span>
          </div>
          <div class="attr-row">
            <span class="label">发布时间</span>
            <span>{{ formatDate(goods.createdAt) }}</span>
          </div>
          <div class="attr-row">
            <span class="label">浏览</span>
            <span>{{ goods.viewCount }} 次</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button type="primary" size="large" @click="contactSeller">
            联系卖家
          </el-button>
          <el-button
            size="large"
            :type="isFavorited ? 'danger' : 'default'"
            :icon="isFavorited ? StarFilled : Star"
            :loading="favoriteLoading"
            @click="toggleFavorite"
          >
            {{ isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <!-- 审核员可见的标记违规按钮 -->
          <el-button
            v-if="isAuditor && (goods.status === 1 || goods.status === 4)"
            size="large"
            type="danger"
            @click="showViolationDialog"
          >
            标记违规
          </el-button>
          <!-- 普通用户可见的举报按钮（不能举报自己的商品） -->
          <el-button
            v-if="!isAuditor && (goods.status === 1 || goods.status === 4) && isNotMyGoods"
            size="large"
            type="warning"
            plain
            @click="showReportDialog"
          >
            举报
          </el-button>
        </div>
      </div>
    </div>

    <!-- 商品描述 -->
    <div v-if="goods" class="description-section">
      <h3>商品描述</h3>
      <div class="description" v-html="goods.description"></div>
    </div>

    <!-- 标记违规对话框 -->
    <el-dialog v-model="violationDialogVisible" title="标记商品违规" width="500px">
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

    <!-- 举报对话框 -->
    <el-dialog v-model="reportDialogVisible" title="举报商品" width="500px">
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
import { Cpu, QuestionFilled, Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const goods = ref(null)
const currentImageIdx = ref(0)
const isFavorited = ref(false)
const favoriteLoading = ref(false)

// 标记违规相关
const violationDialogVisible = ref(false)
const violationLoading = ref(false)
const violationForm = ref({
  reason: '',
  customReason: ''
})

// 举报相关
const reportDialogVisible = ref(false)
const reportLoading = ref(false)
const reportForm = ref({
  reason: '',
  customReason: ''
})

const goodsImages = computed(() => {
  return goods.value?.images || []
})

const currentImage = computed(() => {
  return goodsImages.value[currentImageIdx.value] || ''
})

// 判断是否是审核员/管理员
const isAuditor = computed(() => {
  const role = userStore.userInfo?.role
  return role === 'ADMIN' || role === 'AUDITOR'
})

// 判断是否不是自己的商品（未登录时返回true，允许看到举报按钮）
const isNotMyGoods = computed(() => {
  if (!goods.value) return false
  if (!userStore.userInfo?.id) return true  // 未登录时允许看到按钮
  return Number(userStore.userInfo.id) !== Number(goods.value.sellerId)
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

    // 检查是否已收藏
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

  // 不能联系自己（类型转换确保比较正确）
  if (Number(userStore.userInfo?.id) === Number(sellerId)) {
    ElMessage.warning('这是您自己发布的商品')
    return
  }

  router.push({
    path: '/chat',
    query: {
      sellerId: sellerId,
      goodsId: goodsId
    }
  })
}

async function toggleFavorite() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!goods.value) return

  // 不能收藏自己的商品
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

  const violationReason = violationForm.value.reason === '其他'
    ? violationForm.value.customReason
    : violationForm.value.reason

  violationLoading.value = true
  try {
    await goodsApi.markAsViolation(goods.value.id, violationReason)
    ElMessage.success('已标记为违规商品')
    violationDialogVisible.value = false
    // 刷新商品详情
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
    await reportApi.submit({
      goodsId: goods.value.id,
      reason
    })
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
  padding: 20px;
}

.detail-container {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

.image-section {
  flex: 1;
  max-width: 500px;
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
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
  border-radius: 4px;
  cursor: pointer;
  opacity: 0.7;
  border: 2px solid transparent;
}

.thumbnail.active {
  opacity: 1;
  border-color: #409EFF;
}

.info-section {
  flex: 1;
}

.goods-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.price-section {
  margin-bottom: 20px;
}

.current-price {
  font-size: 28px;
  color: #f5222d;
  font-weight: bold;
  margin-bottom: 8px;
}

.original {
  color: #999;
  text-decoration: line-through;
  font-size: 16px;
}

.ai-estimate {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 14px;
  color: #E6A23C;
}

.attrs {
  margin-bottom: 30px;
}

.attr-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.label {
  width: 80px;
  color: #666;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 12px;
}

.description-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.description-section h3 {
  margin-bottom: 16px;
  color: #333;
}

.description {
  line-height: 1.6;
  color: #666;
}
</style>
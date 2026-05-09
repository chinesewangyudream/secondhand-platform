<template>
  <div class="ai-consultant">
    <div class="consultant-header" @click="togglePanel">
      <div class="header-icon">
        <el-icon :size="28"><ChatDotRound /></el-icon>
      </div>
      <div class="header-text">
        <h4>AI 智能导购</h4>
        <p>告诉我你的需求，帮你找好物</p>
      </div>
      <el-icon class="toggle-icon" :class="{ expanded: isExpanded }">
        <ArrowDown />
      </el-icon>
    </div>

    <el-collapse-transition>
      <div v-show="isExpanded" class="consultant-body">
        <!-- 需求输入区 -->
        <div class="input-section">
          <el-input
            v-model="requirement"
            type="textarea"
            :rows="2"
            placeholder="描述你的需求，如：想买个二手手机拍照用、需要一台办公笔记本..."
            maxlength="200"
            show-word-limit
          />

          <div class="budget-section">
            <span class="budget-label">预算范围：</span>
            <el-input-number
              v-model="minBudget"
              :min="0"
              :max="99999"
              :step="100"
              placeholder="最低"
              controls-position="right"
            />
            <span class="budget-separator">-</span>
            <el-input-number
              v-model="maxBudget"
              :min="0"
              :max="99999"
              :step="100"
              placeholder="最高"
              controls-position="right"
            />
            <span class="budget-unit">元</span>
          </div>

          <el-button
            type="primary"
            :loading="loading"
            @click="getRecommendation"
            :disabled="!requirement.trim()"
          >
            <el-icon><MagicStick /></el-icon>
            获取推荐
          </el-button>
        </div>

        <!-- AI推荐结果 -->
        <div v-if="recommendation" class="result-section">
          <div class="analysis-box">
            <h5><el-icon><DataAnalysis /></el-icon> 需求分析</h5>
            <p>{{ recommendation.analysis }}</p>
            <el-tag type="success" size="small">推荐分类：{{ recommendation.category }}</el-tag>
          </div>

          <div class="recommendations-box">
            <h5><el-icon><Goods /></el-icon> 商品推荐</h5>
            <div class="recommendation-list">
              <div
                v-for="(rec, index) in recommendation.recommendations"
                :key="index"
                class="recommendation-item"
                @click="searchProduct(rec.name)"
              >
                <div class="rec-name">{{ rec.name }}</div>
                <div class="rec-reason">{{ rec.reason }}</div>
                <div class="rec-price">
                  <el-icon><PriceTag /></el-icon>
                  {{ rec.priceRange }}
                </div>
                <el-button type="primary" size="small" text>
                  搜索商品 <el-icon><ArrowRight /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <div class="quick-search">
            <el-button type="success" @click="searchAll">
              <el-icon><Search /></el-icon>
              查看全部推荐商品
            </el-button>
          </div>
        </div>

        <!-- 推荐商品展示 -->
        <div v-if="recommendedGoods.length > 0" class="goods-section">
          <h5><el-icon><ShoppingBag /></el-icon> 为你推荐的商品</h5>
          <div class="goods-list">
            <div
              v-for="goods in recommendedGoods"
              :key="goods.id"
              class="goods-item"
              @click="goToGoods(goods.id)"
            >
              <img :src="goods.images?.[0] || '/placeholder.jpg'" alt="" />
              <div class="goods-info">
                <div class="goods-title">{{ goods.title }}</div>
                <div class="goods-price">¥{{ goods.price }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, ArrowDown, MagicStick, DataAnalysis,
  Goods, PriceTag, ArrowRight, Search, ShoppingBag
} from '@element-plus/icons-vue'

const router = useRouter()

const STORAGE_KEY = 'ai_consultant_data'

const isExpanded = ref(false)
const requirement = ref('')
const minBudget = ref(null)
const maxBudget = ref(null)
const loading = ref(false)
const recommendation = ref(null)
const recommendedGoods = ref([])

// 页面加载时恢复数据
onMounted(() => {
  const saved = sessionStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      const data = JSON.parse(saved)
      requirement.value = data.requirement || ''
      minBudget.value = data.minBudget || null
      maxBudget.value = data.maxBudget || null
      recommendation.value = data.recommendation || null
      recommendedGoods.value = data.recommendedGoods || []
      // 如果有保存的数据，展开面板
      if (recommendation.value || requirement.value) {
        isExpanded.value = true
      }
    } catch (e) {
      console.error('恢复AI咨询数据失败:', e)
    }
  }
})

// 保存数据到sessionStorage
function saveData() {
  const data = {
    requirement: requirement.value,
    minBudget: minBudget.value,
    maxBudget: maxBudget.value,
    recommendation: recommendation.value,
    recommendedGoods: recommendedGoods.value
  }
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data))
}

function togglePanel() {
  isExpanded.value = !isExpanded.value
}

async function getRecommendation() {
  if (!requirement.value.trim()) {
    ElMessage.warning('请描述你的需求')
    return
  }

  loading.value = true
  recommendation.value = null
  recommendedGoods.value = []

  try {
    const res = await goodsApi.aiRecommend({
      requirement: requirement.value,
      minBudget: minBudget.value,
      maxBudget: maxBudget.value
    })

    if (res.data?.success) {
      recommendation.value = res.data
      // 自动加载推荐商品
      if (res.data.recommendations?.length > 0) {
        await loadRecommendedGoods(res.data.recommendations[0].name)
      }
      // 保存数据
      saveData()
    } else {
      ElMessage.error('获取推荐失败，请稍后重试')
    }
  } catch (error) {
    console.error('AI推荐失败:', error)
    ElMessage.error('获取推荐失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function loadRecommendedGoods(keyword) {
  try {
    const res = await goodsApi.recommendSearch({
      keyword: keyword,
      minPrice: minBudget.value,
      maxPrice: maxBudget.value,
      limit: 6
    })
    recommendedGoods.value = res.data || []
    // 保存数据
    saveData()
  } catch (error) {
    console.error('加载推荐商品失败:', error)
  }
}

function searchProduct(keyword) {
  router.push({
    path: '/search',
    query: { q: keyword }
  })
}

function searchAll() {
  const keyword = recommendation.value?.recommendations?.[0]?.name || requirement.value
  router.push({
    path: '/search',
    query: {
      q: keyword,
      minPrice: minBudget.value,
      maxPrice: maxBudget.value
    }
  })
}

function goToGoods(id) {
  router.push(`/goods/${id}`)
}
</script>

<style scoped>
.ai-consultant {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  margin: 0 20px 40px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.consultant-header {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  cursor: pointer;
  color: white;
  transition: background 0.3s;
}

.consultant-header:hover {
  background: rgba(255, 255, 255, 0.1);
}

.header-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.header-text h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.header-text p {
  margin: 4px 0 0;
  font-size: 13px;
  opacity: 0.9;
}

.toggle-icon {
  margin-left: auto;
  transition: transform 0.3s;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.consultant-body {
  background: white;
  padding: 24px;
}

.input-section {
  margin-bottom: 20px;
}

.budget-section {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 16px 0;
  flex-wrap: wrap;
}

.budget-label {
  font-size: 14px;
  color: #666;
}

.budget-separator {
  color: #999;
}

.budget-unit {
  font-size: 14px;
  color: #666;
}

.budget-section .el-input-number {
  width: 120px;
}

.result-section {
  margin-top: 24px;
}

.analysis-box {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
}

.analysis-box h5 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  color: #333;
  font-size: 15px;
}

.analysis-box p {
  margin: 0 0 12px;
  color: #666;
  line-height: 1.6;
}

.recommendations-box h5 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  color: #333;
  font-size: 15px;
}

.recommendation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.recommendation-item {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.recommendation-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.rec-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.rec-reason {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.rec-price {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
  margin-bottom: 12px;
}

.quick-search {
  margin-top: 20px;
  text-align: center;
}

.goods-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.goods-section h5 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  color: #333;
  font-size: 15px;
}

.goods-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.goods-item {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.goods-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.goods-item img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.goods-info {
  padding: 12px;
}

.goods-title {
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b6b;
}
</style>

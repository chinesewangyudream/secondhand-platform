<!-- src/views/MyGoods.vue -->
<template>
  <div class="my-goods-page">
    <div class="page-header">
      <h2>我的商品</h2>
      <el-button type="primary" @click="router.push('/publish')">
        发布新商品
      </el-button>
    </div>

    <div class="goods-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="在售商品" name="selling">
          <div class="goods-list" v-loading="loading">
            <div v-if="goodsList.length === 0" class="no-goods">
              <p>暂无在售商品</p>
              <el-button type="primary" @click="router.push('/publish')">
                去发布商品
              </el-button>
            </div>
            <div v-else class="goods-grid">
              <div
                v-for="goods in goodsList"
                :key="goods.id"
                class="goods-card"
              >
                <div class="goods-image">
                  <img :src="goods.images?.[0]" :alt="goods.title" />
                </div>
                <div class="goods-info">
                  <h4>{{ goods.title }}</h4>
                  <p class="price">¥{{ goods.price }}</p>
                  <p class="status">状态: {{ getStatusText(goods.status) }}</p>
                  <div class="actions">
                    <el-button size="small" @click="editGoods(goods.id)">
                      编辑
                    </el-button>
                    <el-button size="small" type="danger" @click="deleteGoods(goods.id)">
                      下架
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="违规商品" name="violation">
          <div class="goods-list" v-loading="loading">
            <div v-if="violationList.length === 0" class="no-goods">
              <p>暂无违规商品</p>
            </div>
            <div v-else class="goods-grid">
              <div
                v-for="goods in violationList"
                :key="goods.id"
                class="goods-card violation"
              >
                <div class="goods-image">
                  <img :src="goods.images?.[0]" :alt="goods.title" />
                  <div class="violation-badge" :class="{ 'pending-badge': goods.status === 6 }">
                    {{ goods.status === 6 ? '已提交申请' : '违规' }}
                  </div>
                </div>
                <div class="goods-info">
                  <h4>{{ goods.title }}</h4>
                  <p class="price">¥{{ goods.price }}</p>
                  <p class="violation-reason">
                    <el-icon><Warning /></el-icon>
                    {{ goods.violationReason }}
                  </p>
                  <div class="actions">
                    <template v-if="goods.status === 5">
                      <el-button size="small" type="primary" @click="editAndResubmit(goods)">
                        整改重新提交
                      </el-button>
                    </template>
                    <template v-else-if="goods.status === 6">
                      <el-tag type="warning" size="small">审核中，请耐心等待</el-tag>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="已售出" name="sold">
          <div class="goods-list" v-loading="loading">
            <div v-if="soldGoodsList.length === 0" class="no-goods">
              <p>暂无已售商品</p>
            </div>
            <div v-else class="goods-grid">
              <div
                v-for="goods in soldGoodsList"
                :key="goods.id"
                class="goods-card sold"
              >
                <div class="goods-image">
                  <img :src="goods.images?.[0]" :alt="goods.title" />
                  <div class="sold-badge">已售出</div>
                </div>
                <div class="goods-info">
                  <h4>{{ goods.title }}</h4>
                  <p class="price">¥{{ goods.price }}</p>
                  <p class="sold-date">售出时间: {{ formatDate(goods.updatedAt) }}</p>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('selling')
const loading = ref(false)
const goodsList = ref([])
const soldGoodsList = ref([])
const violationList = ref([])

onMounted(async () => {
  await loadGoods()
})

async function loadGoods() {
  loading.value = true
  try {
    if (activeTab.value === 'selling') {
      const res = await goodsApi.getMyGoods({ status: 'selling' })
      goodsList.value = res.data || []
    } else if (activeTab.value === 'violation') {
      const res = await goodsApi.getMyGoods({ status: 'violation' })
      violationList.value = res.data || []
    } else {
      const res = await goodsApi.getMyGoods({ status: 'sold' })
      soldGoodsList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载商品失败')
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

function handleTabClick() {
  loadGoods()
}

function editGoods(goodsId) {
  router.push(`/edit-goods/${goodsId}`)
}

async function editAndResubmit(goods) {
  try {
    await ElMessageBox.confirm(
      '整改完成后将重新提交审核，确定要继续吗？',
      '提示',
      {
        confirmButtonText: '去整改',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // 跳转到编辑页面
    router.push(`/edit-goods/${goods.id}?resubmit=1`)
  } catch (error) {
    // 用户取消
  }
}

async function deleteGoods(goodsId) {
  try {
    await ElMessageBox.confirm('确定要下架这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await goodsApi.offShelf(goodsId)
    ElMessage.success('商品已下架')
    await loadGoods()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下架失败')
    }
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function getStatusText(statusCode) {
  const statusMap = {
    0: '待审核',
    1: '在售',
    2: '已售出',
    3: '已下架',
    4: '拍卖中',
    5: '违规',
    6: '已提交申请'
  }
  return statusMap[statusCode] || '未知状态'
}
</script>

<style scoped>
.my-goods-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.goods-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 20px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.goods-card {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.goods-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.goods-card.sold {
  opacity: 0.7;
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

.sold-badge {
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

.status, .sold-date {
  font-size: 12px;
  color: #666;
  margin: 4px 0;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.no-goods {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.goods-card.violation {
  border-color: #ff4d4f;
}

.violation-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #ff4d4f;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.violation-badge.pending-badge {
  background: #E6A23C;
}

.violation-reason {
  display: flex;
  align-items: flex-start;
  gap: 4px;
  font-size: 12px;
  color: #ff4d4f;
  background: #fff2f0;
  padding: 8px;
  border-radius: 4px;
  margin: 8px 0;
  line-height: 1.4;
}
</style>
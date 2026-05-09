<!-- src/views/Orders.vue -->
<template>
  <div class="orders-page">
    <h2>我的订单</h2>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="order-tabs">
      <el-tab-pane label="我买的" name="buy"></el-tab-pane>
      <el-tab-pane label="我卖的" name="sell"></el-tab-pane>
    </el-tabs>

    <div class="orders-list" v-loading="loading">
      <div v-if="orders.length === 0" class="no-orders">
        <el-empty :description="activeTab === 'buy' ? '暂无购买订单' : '暂无销售订单'" />
      </div>
      <div v-else v-for="order in orders" :key="order.id" class="order-card" @click="viewOrder(order)">
        <div class="order-header">
          <div class="order-no-row">
            <span class="order-no">订单号: {{ order.orderNo }}</span>
            <el-tag v-if="isAuctionOrder(order.orderNo)" type="warning" size="small" class="auction-tag">
              拍卖
            </el-tag>
          </div>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        <div class="order-content">
          <div class="goods-info">
            <img :src="getFirstImage(order.goodsImages)" :alt="order.goodsTitle" class="goods-img" />
            <div class="goods-details">
              <h4>{{ order.goodsTitle || '商品已下架' }}</h4>
              <div class="price-row">
                <span v-if="order.priceChanged" class="original-price">原价: ¥{{ order.originalPrice }}</span>
                <span class="price" :class="{ 'price-changed': order.priceChanged }">
                  ¥{{ order.amount }}
                </span>
                <span v-if="order.priceChanged" class="price-tag">已改价</span>
              </div>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <span class="order-time">{{ formatTime(order.createdAt) }}</span>
          <div class="order-actions">
            <!-- 买家操作 -->
            <template v-if="activeTab === 'buy'">
              <el-button
                v-if="order.status === 0"
                type="primary"
                size="small"
                @click.stop="payOrder(order)"
              >
                立即付款
              </el-button>
              <el-button
                v-if="order.status === 2"
                type="primary"
                size="small"
                @click.stop="confirmReceipt(order)"
              >
                确认收货
              </el-button>
            </template>
            <!-- 卖家操作 -->
            <template v-else>
              <el-button
                v-if="order.status === 1"
                type="primary"
                size="small"
                @click.stop="showShipDialog(order)"
              >
                发货
              </el-button>
            </template>
            <el-button size="small" @click.stop="viewOrder(order)">查看详情</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipDialogVisible" title="填写物流信息" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="物流公司">
          <el-select v-model="shipForm.company" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="邮政EMS" value="邮政EMS" />
            <el-option label="京东物流" value="京东物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="运单号">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入运单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="shipOrder" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="订单详情" width="600px" destroy-on-close>
      <div v-if="orderDetail" class="order-detail-content">
        <!-- 订单状态 -->
        <div class="detail-section">
          <div class="section-title">订单状态</div>
          <div class="status-info">
            <el-tag :type="getStatusType(orderDetail.status)" size="large">
              {{ getStatusText(orderDetail.status) }}
            </el-tag>
            <span v-if="orderDetail.priceChanged" class="price-changed-tag">已改价</span>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-section">
          <div class="section-title">商品信息</div>
          <div class="goods-detail-card">
            <img :src="getFirstImage(orderDetail.goods?.images)" class="goods-detail-img" />
            <div class="goods-detail-info">
              <div class="goods-detail-title">{{ orderDetail.goods?.title || '商品已下架' }}</div>
              <div class="goods-detail-desc">{{ orderDetail.goods?.description || '' }}</div>
              <div class="goods-detail-price">
                <span v-if="orderDetail.priceChanged" class="original">原价: ¥{{ orderDetail.originalPrice }}</span>
                <span class="actual">成交价: ¥{{ orderDetail.amount }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单信息 -->
        <div class="detail-section">
          <div class="section-title">订单信息</div>
          <div class="info-list">
            <div class="info-item">
              <span class="label">订单编号</span>
              <span class="value">{{ orderDetail.orderNo }}</span>
            </div>
            <div class="info-item">
              <span class="label">创建时间</span>
              <span class="value">{{ formatDateTime(orderDetail.createdAt) }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.payTime">
              <span class="label">付款时间</span>
              <span class="value">{{ formatDateTime(orderDetail.payTime) }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.shipTime">
              <span class="label">发货时间</span>
              <span class="value">{{ formatDateTime(orderDetail.shipTime) }}</span>
            </div>
            <div class="info-item" v-if="orderDetail.confirmTime">
              <span class="label">完成时间</span>
              <span class="value">{{ formatDateTime(orderDetail.confirmTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 物流信息 -->
        <div class="detail-section" v-if="logisticsInfo">
          <div class="section-title">物流信息</div>
          <div class="logistics-info">
            <div class="logistics-header">
              <span class="company">{{ logisticsInfo.company }}</span>
              <span class="tracking-no">运单号: {{ logisticsInfo.trackingNo }}</span>
            </div>
            <div class="logistics-timeline" v-if="logisticsDetails && logisticsDetails.length > 0">
              <el-timeline>
                <el-timeline-item
                  v-for="(item, index) in logisticsDetails"
                  :key="index"
                  :timestamp="formatDateTime(item.time)"
                  placement="top"
                >
                  {{ item.status }}
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="detail-actions">
          <!-- 买家操作 -->
          <template v-if="activeTab === 'buy'">
            <el-button
              v-if="orderDetail.status === 0"
              type="primary"
              @click="payOrder(orderDetail)"
            >
              立即付款
            </el-button>
            <el-button
              v-if="orderDetail.status === 2"
              type="primary"
              @click="confirmReceipt(orderDetail)"
            >
              确认收货
            </el-button>
          </template>
          <!-- 卖家操作 -->
          <template v-else>
            <el-button
              v-if="orderDetail.status === 1"
              type="primary"
              @click="showShipDialog(orderDetail)"
            >
              发货
            </el-button>
          </template>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('buy')
const loading = ref(false)
const orders = ref([])

// 发货相关
const shipDialogVisible = ref(false)
const shipForm = ref({ company: '', trackingNo: '' })
const currentShipOrder = ref(null)
const shipping = ref(false)

// 订单详情
const detailDialogVisible = ref(false)
const orderDetail = ref(null)
const logisticsInfo = ref(null)
const logisticsDetails = ref([])

onMounted(async () => {
  await loadOrders()
})

async function loadOrders() {
  loading.value = true
  try {
    const res = activeTab.value === 'buy'
      ? await orderApi.getMyBuyOrders()
      : await orderApi.getMySellOrders()

    orders.value = res.data || []
  } catch (error) {
    ElMessage.error(error.showMessage || '加载订单失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  loadOrders()
}

// 判断是否是拍卖订单
function isAuctionOrder(orderNo) {
  return orderNo && orderNo.startsWith('AU')
}

// 查看订单详情
async function viewOrder(order) {
  try {
    const res = await orderApi.getDetail(order.id)
    orderDetail.value = res.data

    // 获取物流信息
    if (orderDetail.value.status >= 2) {
      try {
        const logisticsRes = await orderApi.getLogistics(order.id)
        logisticsInfo.value = logisticsRes.data?.logistics
        logisticsDetails.value = logisticsRes.data?.details || []
      } catch (e) {
        logisticsInfo.value = null
        logisticsDetails.value = []
      }
    } else {
      logisticsInfo.value = null
      logisticsDetails.value = []
    }

    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.showMessage || '获取订单详情失败')
  }
}

function getStatusType(status) {
  const map = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'danger',
    5: 'warning'
  }
  return map[status] || 'info'
}

function getStatusText(status) {
  const map = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '待确认'
  }
  return map[status] || '未知'
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function getFirstImage(images) {
  if (!images) return ''
  try {
    const arr = JSON.parse(images)
    return arr[0] || ''
  } catch {
    return images
  }
}

// 买家付款
async function payOrder(order) {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '付款确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })

    await orderApi.pay(order.orderNo)
    ElMessage.success('支付成功')
    detailDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.showMessage || '支付失败')
    }
  }
}

// 买家确认收货
async function confirmReceipt(order) {
  try {
    await ElMessageBox.confirm('确认已收到货物吗？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })

    await orderApi.confirm(order.id)
    ElMessage.success('确认收货成功')
    detailDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.showMessage || '操作失败')
    }
  }
}

// 显示发货弹窗
function showShipDialog(order) {
  currentShipOrder.value = order
  shipForm.value = { company: '', trackingNo: '' }
  detailDialogVisible.value = false
  shipDialogVisible.value = true
}

// 卖家发货
async function shipOrder() {
  if (!shipForm.value.company) {
    ElMessage.warning('请选择物流公司')
    return
  }
  if (!shipForm.value.trackingNo) {
    ElMessage.warning('请输入运单号')
    return
  }

  shipping.value = true
  try {
    await orderApi.ship(currentShipOrder.value.id, shipForm.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.showMessage || '发货失败')
  } finally {
    shipping.value = false
  }
}
</script>

<style scoped>
.orders-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.orders-page h2 {
  margin: 0 0 20px 0;
  color: #333;
}

.order-tabs {
  margin-bottom: 20px;
}

.orders-list {
  min-height: 300px;
}

.no-orders {
  padding: 60px 20px;
  text-align: center;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.auction-tag {
  font-size: 11px;
}

.order-content {
  margin-bottom: 12px;
}

.goods-info {
  display: flex;
  gap: 16px;
}

.goods-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.goods-details {
  flex: 1;
}

.goods-details h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #333;
  line-height: 1.4;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.price {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.price.price-changed {
  color: #f56c6c;
}

.price-tag {
  font-size: 12px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 6px;
  border-radius: 4px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.order-time {
  font-size: 13px;
  color: #999;
}

.order-actions {
  display: flex;
  gap: 8px;
}

/* 订单详情样式 */
.order-detail-content {
  padding: 0 10px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-changed-tag {
  font-size: 12px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 4px 10px;
  border-radius: 4px;
}

.goods-detail-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.goods-detail-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.goods-detail-info {
  flex: 1;
}

.goods-detail-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.goods-detail-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-detail-price .original {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-right: 12px;
}

.goods-detail-price .actual {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.info-list {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}

.info-item .value {
  color: #333;
}

.logistics-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
}

.logistics-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e6e6e6;
}

.logistics-header .company {
  font-weight: 500;
  color: #333;
}

.logistics-header .tracking-no {
  color: #666;
  font-size: 14px;
}

.logistics-timeline {
  max-height: 300px;
  overflow-y: auto;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e6e6e6;
}
</style>

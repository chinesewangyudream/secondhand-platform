<!-- src/components/OrderList.vue -->
<template>
  <div class="order-list">
    <div v-if="orders.length === 0" class="no-orders">
      <p>暂无订单</p>
    </div>
    <div v-else v-for="order in orders" :key="order.id" class="order-item">
      <div class="order-header">
        <div class="order-info">
          <span class="order-id">订单号: {{ order.id }}</span>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ order.status }}
          </el-tag>
        </div>
        <div class="order-time">{{ formatDate(order.createdAt) }}</div>
      </div>

      <div class="order-content">
        <div class="goods-info">
          <img :src="order.goodsImage" :alt="order.goodsTitle" class="goods-img" />
          <div class="goods-details">
            <h4>{{ order.goodsTitle }}</h4>
            <p class="goods-price">¥{{ order.price }}</p>
            <p class="goods-quantity">数量: {{ order.quantity }}</p>
          </div>
        </div>

        <div class="order-total">
          <div class="total-price">
            总价: <span class="price">¥{{ order.totalPrice }}</span>
          </div>
          <div class="buyer-info" v-if="type === 'sell'">
            <p>买家: {{ order.buyerName }}</p>
            <p>联系方式: {{ order.buyerContact }}</p>
          </div>
          <div class="seller-info" v-if="type === 'buy'">
            <p>卖家: {{ order.sellerName }}</p>
            <p>联系方式: {{ order.sellerContact }}</p>
          </div>
        </div>
      </div>

      <div class="order-actions">
        <el-button size="small" @click="viewOrderDetail(order.id)">
          查看详情
        </el-button>
        <el-button
          v-if="order.status === '待发货' && type === 'sell'"
          size="small"
          type="primary"
          @click="shipOrder(order.id)"
        >
          发货
        </el-button>
        <el-button
          v-if="order.status === '待收货' && type === 'buy'"
          size="small"
          type="success"
          @click="confirmReceipt(order.id)"
        >
          确认收货
        </el-button>
        <el-button
          v-if="canCancel(order)"
          size="small"
          type="danger"
          @click="cancelOrder(order.id)"
        >
          取消订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  orders: {
    type: Array,
    default: () => []
  },
  type: {
    type: String,
    default: 'buy' // 'buy' 或 'sell'
  }
})

const emit = defineEmits(['refresh'])

function getStatusType(status) {
  const statusMap = {
    '待付款': 'warning',
    '待发货': 'info',
    '待收货': 'primary',
    '已完成': 'success',
    '已取消': 'danger',
    '已退款': 'info'
  }
  return statusMap[status] || 'info'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

function viewOrderDetail(orderId) {
  // 跳转到订单详情页
  console.log('查看订单详情:', orderId)
}

async function shipOrder(orderId) {
  try {
    await ElMessageBox.confirm('确认已发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用发货API
    // await orderApi.shipOrder(orderId)
    ElMessage.success('发货成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发货失败')
    }
  }
}

async function confirmReceipt(orderId) {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用确认收货API
    // await orderApi.confirmReceipt(orderId)
    ElMessage.success('确认收货成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认收货失败')
    }
  }
}

async function cancelOrder(orderId) {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 调用取消订单API
    // await orderApi.cancelOrder(orderId)
    ElMessage.success('订单已取消')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

function canCancel(order) {
  // 只有待付款状态可以取消
  return order.status === '待付款'
}
</script>

<style scoped>
.order-list {
  margin-top: 20px;
}

.no-orders {
  text-align: center;
  padding: 40px;
  color: #999;
}

.order-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.order-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-id {
  font-weight: 500;
  color: #333;
}

.order-time {
  color: #999;
  font-size: 12px;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.goods-info {
  display: flex;
  gap: 12px;
  flex: 1;
}

.goods-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.goods-details h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #333;
}

.goods-price {
  color: #f5222d;
  font-weight: 600;
  margin: 4px 0;
}

.goods-quantity {
  color: #666;
  font-size: 12px;
  margin: 4px 0;
}

.order-total {
  text-align: right;
  min-width: 150px;
}

.total-price {
  font-size: 16px;
  margin-bottom: 8px;
}

.total-price .price {
  color: #f5222d;
  font-weight: 600;
  font-size: 18px;
}

.buyer-info, .seller-info {
  font-size: 12px;
  color: #666;
  margin-top: 8px;
}

.buyer-info p, .seller-info p {
  margin: 2px 0;
}

.order-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>
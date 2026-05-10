<!-- src/views/Chat.vue -->
<template>
  <div class="chat-page" v-loading="loading">
    <div class="chat-container">
      <!-- 左侧会话列表 -->
      <div class="conversation-list">
        <div class="list-header">
          <h3>消息</h3>
        </div>
        <div class="conversations">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            :class="['conversation-item', { active: conv.id === currentConversationId }]"
            @click="selectConversation(conv.id)"
          >
            <div class="avatar">
              <img :src="conv.partnerAvatar || defaultAvatar" :alt="conv.partnerName" />
            </div>
            <div class="info">
              <div class="name">{{ conv.partnerName || '用户' }}</div>
              <div class="last-message">{{ conv.lastMessage || '暂无消息' }}</div>
            </div>
            <div class="time">{{ formatTime(conv.lastMessageTime) }}</div>
          </div>
          <div v-if="conversations.length === 0" class="empty-tip">
            暂无会话，去商品详情联系卖家吧
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area">
        <div v-if="chatPartner" class="chat-header">
          <div class="user-info">
            <img :src="chatPartner.avatar || defaultAvatar" :alt="chatPartner.nickname" class="avatar" />
            <span class="name">{{ chatPartner.nickname }}</span>
          </div>
          <div class="header-actions">
            <!-- 订单按钮 -->
            <el-button type="primary" size="small" @click="showOrderDialog" v-if="currentConversationId">
              {{ isSeller ? '发起订单' : '查看订单' }}
            </el-button>
            <!-- 结束聊天按钮 -->
            <el-popconfirm
              v-if="currentConversationId"
              title="确定结束该聊天吗？结束后聊天记录将不再显示，重新联系卖家可恢复。"
              @confirm="endChat"
              confirm-button-text="确定"
              cancel-button-text="取消"
            >
              <template #reference>
                <el-button type="danger" size="small" plain>结束聊天</el-button>
              </template>
            </el-popconfirm>
          </div>
        </div>

        <div v-if="currentConversationId" class="messages" ref="messagesRef">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message', msg.isOwn ? 'own' : 'other']"
          >
            <!-- 对方消息：头像在左 -->
            <img
              v-if="!msg.isOwn"
              :src="chatPartner?.avatar || defaultAvatar"
              class="message-avatar"
            />
            <div class="message-body">
              <div class="message-content">{{ msg.content }}</div>
              <div class="message-footer">
                <span class="message-time">{{ formatTime(msg.createdAt) }}</span>
                <el-popconfirm
                  v-if="msg.isOwn"
                  title="确定撤回这条消息吗？"
                  @confirm="deleteMessage(msg.id)"
                  confirm-button-text="撤回"
                  cancel-button-text="取消"
                >
                  <template #reference>
                    <el-button type="danger" size="small" text>撤回</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <!-- 自己消息：头像在右 -->
            <img
              v-if="msg.isOwn"
              :src="userStore.userInfo?.avatar || defaultAvatar"
              class="message-avatar"
            />
          </div>
          <div v-if="messages.length === 0" class="empty-messages">
            暂无消息，发送第一条消息吧
          </div>
        </div>

        <div v-else class="no-chat-selected">
          <el-empty description="选择一个会话开始聊天" />
        </div>

        <div v-if="currentConversationId" class="message-input">
          <el-input
            v-model="newMessage"
            placeholder="输入消息..."
            @keyup.enter="sendMessage"
            clearable
          >
            <template #suffix>
              <el-button type="primary" @click="sendMessage" :disabled="!newMessage.trim()">
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 订单弹窗 -->
    <el-dialog
      v-model="orderDialogVisible"
      :title="isSeller ? '发起订单' : '订单详情'"
      width="500px"
      destroy-on-close
    >
      <div v-if="orderLoading" class="order-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载中...</span>
      </div>

      <!-- 无订单时：卖家发起订单 -->
      <div v-else-if="!currentOrder && isSeller" class="create-order">
        <el-form :model="orderForm" label-width="80px">
          <el-form-item label="商品信息">
            <div class="goods-preview" v-if="goodsInfo">
              <img :src="getFirstImage(goodsInfo.images)" class="goods-img" />
              <div class="goods-detail">
                <div class="goods-title">{{ goodsInfo.title }}</div>
                <div class="goods-price">原价：¥{{ goodsInfo.price }}</div>
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="createOrder" :loading="creatingOrder">
              发起订单
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 无订单时：买家等待 -->
      <div v-else-if="!currentOrder && !isSeller" class="wait-order">
        <el-empty description="卖家尚未发起订单，请与卖家沟通" />
      </div>

      <!-- 有订单时：显示订单详情 -->
      <div v-else class="order-detail">
        <div class="order-status">
          <el-tag :type="getOrderStatusType(currentOrder.status)" size="large">
            {{ getOrderStatusText(currentOrder.status) }}
          </el-tag>
          <span v-if="currentOrder.priceChanged" class="price-changed-tag">已改价</span>
        </div>

        <div class="goods-preview" v-if="currentOrder.goods">
          <img :src="getFirstImage(currentOrder.goods.images)" class="goods-img" />
          <div class="goods-detail">
            <div class="goods-title">{{ currentOrder.goods.title }}</div>
            <div class="goods-price">商品原价：¥{{ currentOrder.originalPrice }}</div>
            <div class="goods-price actual-price">
              交易价格：¥{{ currentOrder.amount }}
            </div>
          </div>
        </div>

        <div class="order-info">
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.createdAt) }}</span>
          </div>
        </div>

        <!-- 待确认状态：卖家改价 / 买家确认或拒绝 -->
        <div v-if="currentOrder.status === 5" class="order-actions">
          <!-- 卖家操作 -->
          <template v-if="isSeller">
            <el-form :inline="true" class="change-price-form">
              <el-form-item label="改价">
                <el-input-number
                  v-model="newPrice"
                  :min="0.01"
                  :precision="2"
                  :step="10"
                  size="small"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="warning"
                  size="small"
                  @click="changePrice"
                  :loading="changingPrice"
                  :disabled="newPrice === currentOrder.amount"
                >
                  确认改价
                </el-button>
              </el-form-item>
            </el-form>
            <div class="action-tip">等待买家确认订单</div>
          </template>
          <!-- 买家操作 -->
          <template v-else>
            <div class="action-tip">请确认是否接受此订单</div>
            <div class="buyer-actions">
              <el-button type="danger" @click="rejectOrder" :loading="rejectingOrder">
                拒绝订单
              </el-button>
              <el-button type="primary" @click="confirmOrder" :loading="confirmingOrder">
                确认订单
              </el-button>
            </div>
          </template>
        </div>

        <!-- 待付款状态 -->
        <div v-else-if="currentOrder.status === 0" class="order-actions">
          <template v-if="isSeller">
            <div class="action-tip">等待买家付款</div>
          </template>
          <template v-else>
            <el-button type="primary" @click="payOrder" :loading="payingOrder">
              立即付款
            </el-button>
          </template>
        </div>

        <!-- 其他状态 -->
        <div v-else class="order-actions">
          <el-button @click="goToOrderDetail">查看订单详情</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { chatApi } from '@/api/chat'
import { userApi } from '@/api/user'
import { goodsApi } from '@/api/goods'
import { orderApi } from '@/api/order'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const conversations = ref([])
const currentConversationId = ref('')
const currentConversation = ref(null)
const messages = ref([])
const newMessage = ref('')
const messagesRef = ref()
const loading = ref(false)
const sellerInfo = ref(null)

// 订单相关
const orderDialogVisible = ref(false)
const orderLoading = ref(false)
const currentOrder = ref(null)
const goodsInfo = ref(null)
const orderForm = ref({})
const newPrice = ref(0)
const creatingOrder = ref(false)
const changingPrice = ref(false)
const confirmingOrder = ref(false)
const rejectingOrder = ref(false)
const payingOrder = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 当前用户是否是卖家（根据当前会话判断）
const isSeller = computed(() => {
  if (!currentConversation.value || !userStore.userInfo?.id) return false
  return Number(currentConversation.value.sellerId) === Number(userStore.userInfo.id)
})

// 当前聊天的对方用户信息
const chatPartner = computed(() => {
  if (sellerInfo.value) {
    return {
      id: sellerInfo.value.id,
      nickname: sellerInfo.value.nickname || sellerInfo.value.username || '用户',
      avatar: sellerInfo.value.avatar
    }
  }
  if (currentConversation.value) {
    return {
      id: currentConversation.value.partnerId,
      nickname: currentConversation.value.partnerName || '用户',
      avatar: currentConversation.value.partnerAvatar
    }
  }
  return null
})

onMounted(async () => {
  // 确保用户信息已加载
  if (userStore.token && !userStore.userInfo?.id) {
    try {
      await userStore.loadProfile()
    } catch (e) {
      console.error('加载用户信息失败:', e)
    }
  }

  // 检查是否需要创建新会话
  const { sellerId, goodsId } = route.query

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (sellerId && goodsId) {
    await initSessionFromGoods(Number(sellerId), Number(goodsId))
  } else {
    await loadConversations()
  }
})

async function initSessionFromGoods(sellerId, goodsId) {
  loading.value = true
  try {
    // 参数校验
    if (!sellerId || !goodsId) {
      throw new Error('缺少必要参数：卖家ID或商品ID')
    }

    // 获取卖家信息
    const userRes = await userApi.getUserInfo(sellerId)
    const userData = userRes.data

    if (!userData) {
      throw new Error('无法获取卖家信息，该用户可能不存在')
    }

    sellerInfo.value = {
      id: userData.id,
      nickname: userData.nickname || userData.username || '卖家',
      avatar: userData.avatar
    }

    // 创建或获取会话
    const sessionRes = await chatApi.getOrCreateSession({ sellerId, goodsId })
    const session = sessionRes.data

    if (!session) {
      throw new Error('无法创建会话，请稍后重试')
    }

    // 加载会话列表
    await loadConversations()

    // 选中当前会话
    currentConversationId.value = session.id
    currentConversation.value = {
      id: session.id,
      buyerId: session.buyerId,
      sellerId: session.sellerId,
      goodsId: session.goodsId,
      partnerId: sellerId,
      partnerName: sellerInfo.value?.nickname || '卖家',
      partnerAvatar: sellerInfo.value?.avatar
    }

    await loadMessages(session.id)
  } catch (error) {
    console.error('初始化会话失败:', error)
    ElMessage.error(error.showMessage || error.message || '初始化会话失败，请稍后重试')
    // 回退到会话列表
    await loadConversations()
  } finally {
    loading.value = false
  }
}

async function loadConversations() {
  try {
    const res = await chatApi.getSessions()
    conversations.value = res.data || []
    // 只在没有选中会话时自动选择第一个
    if (conversations.value.length > 0 && !currentConversationId.value) {
      selectConversation(conversations.value[0].id)
    }
  } catch (error) {
    console.error('加载会话失败:', error)
  }
}

async function selectConversation(id) {
  currentConversationId.value = id
  const conv = conversations.value.find(c => c.id === id)
  currentConversation.value = conv
  sellerInfo.value = null // 清除从商品详情带来的卖家信息
  await loadMessages(id)
}

async function loadMessages(conversationId) {
  try {
    const res = await chatApi.getMessages(conversationId)
    const currentUserId = userStore.userInfo?.id

    if (!currentUserId) {
      console.warn('当前用户ID不存在，请重新登录')
      return
    }

    // 处理消息，标记是否是自己发的
    // 后端返回的消息是按时间倒序（最新在前），需要反转为最新在底部
    messages.value = (res.data || [])
      .reverse() // 反转数组，使最新消息在底部
      .map(msg => {
        // 统一转为数字比较
        const senderId = Number(msg.senderId)
        const myId = Number(currentUserId)
        return {
          ...msg,
          isOwn: senderId === myId
        }
      })
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error(error.showMessage || '加载消息失败')
  }
}

async function sendMessage() {
  if (!newMessage.value.trim()) return

  try {
    await chatApi.sendMessage(currentConversationId.value, newMessage.value)
    newMessage.value = ''
    await loadMessages(currentConversationId.value)
  } catch (error) {
    ElMessage.error(error.showMessage || '发送消息失败')
  }
}

async function deleteMessage(messageId) {
  try {
    await chatApi.deleteMessage(messageId)
    await loadMessages(currentConversationId.value)
    // 刷新会话列表以更新最后消息
    await loadConversations()
  } catch (error) {
    ElMessage.error(error.showMessage || '撤回消息失败')
  }
}

// 结束聊天（隐藏会话）
async function endChat() {
  try {
    await chatApi.hideSession(currentConversationId.value)
    ElMessage.success('聊天已结束')
    // 清除当前会话
    currentConversationId.value = ''
    currentConversation.value = null
    messages.value = []
    sellerInfo.value = null
    // 重新加载会话列表
    await loadConversations()
  } catch (error) {
    ElMessage.error(error.showMessage || '操作失败')
  }
}

// 显示订单弹窗
async function showOrderDialog() {
  orderDialogVisible.value = true
  await loadOrderData()
}

// 加载订单数据
async function loadOrderData() {
  orderLoading.value = true
  try {
    // 获取商品信息
    const goodsId = currentConversation.value?.goodsId
    if (goodsId) {
      const goodsRes = await goodsApi.getDetail(goodsId)
      goodsInfo.value = goodsRes.data
      newPrice.value = goodsInfo.value?.price || 0
    }

    // 获取当前会话关联的订单
    const orderRes = await orderApi.getOrderBySession(currentConversationId.value)
    currentOrder.value = orderRes.data
    if (currentOrder.value) {
      newPrice.value = currentOrder.value.amount
    }
  } catch (error) {
    console.error('加载订单数据失败:', error)
  } finally {
    orderLoading.value = false
  }
}

// 创建订单
async function createOrder() {
  if (!goodsInfo.value) {
    ElMessage.error('商品信息不存在')
    return
  }

  creatingOrder.value = true
  try {
    const res = await orderApi.createFromSession(
      currentConversationId.value,
      goodsInfo.value.id
    )
    currentOrder.value = res.data
    newPrice.value = res.data.amount
    ElMessage.success('订单已发起，等待买家确认')
  } catch (error) {
    ElMessage.error(error.showMessage || '创建订单失败')
  } finally {
    creatingOrder.value = false
  }
}

// 改价
async function changePrice() {
  if (newPrice.value <= 0) {
    ElMessage.error('价格必须大于0')
    return
  }

  changingPrice.value = true
  try {
    const res = await orderApi.changePrice(currentOrder.value.id, newPrice.value)
    currentOrder.value = res.data
    ElMessage.success('改价成功')
  } catch (error) {
    ElMessage.error(error.showMessage || '改价失败')
  } finally {
    changingPrice.value = false
  }
}

// 买家确认订单
async function confirmOrder() {
  confirmingOrder.value = true
  try {
    await orderApi.confirmOrder(currentOrder.value.id)
    currentOrder.value.status = 0
    ElMessage.success('订单已确认，请尽快付款')
  } catch (error) {
    ElMessage.error(error.showMessage || '确认订单失败')
  } finally {
    confirmingOrder.value = false
  }
}

// 买家拒绝订单
async function rejectOrder() {
  rejectingOrder.value = true
  try {
    await orderApi.rejectOrder(currentOrder.value.id)
    currentOrder.value.status = 4
    ElMessage.success('订单已取消')
    orderDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.showMessage || '取消订单失败')
  } finally {
    rejectingOrder.value = false
  }
}

// 付款
async function payOrder() {
  payingOrder.value = true
  try {
    await orderApi.pay(currentOrder.value.orderNo)
    currentOrder.value.status = 1
    ElMessage.success('支付成功')
  } catch (error) {
    ElMessage.error(error.showMessage || '支付失败')
  } finally {
    payingOrder.value = false
  }
}

// 跳转订单详情
function goToOrderDetail() {
  router.push(`/orders?id=${currentOrder.value.id}`)
  orderDialogVisible.value = false
}

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const oneDay = 24 * 60 * 60 * 1000

  if (diff < oneDay) {
    // 今天，显示时间
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (diff < 7 * oneDay) {
    // 一周内，显示星期
    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return days[date.getDay()]
  } else {
    // 超过一周，显示日期
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
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

function getOrderStatusType(status) {
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

function getOrderStatusText(status) {
  const map = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '待确认'
  }
  return map[status] || '未知状态'
}
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 60px);
  display: flex;
  background: #f5f5f5;
}

.chat-container {
  display: flex;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.conversation-list {
  width: 300px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 20px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  color: #333;
}

.conversations {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.conversation-item:hover,
.conversation-item.active {
  background: #f5f5f5;
}

.conversation-item .avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.conversation-item .avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.info {
  flex: 1;
  margin-left: 12px;
}

.name {
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.last-message {
  font-size: 12px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  font-size: 12px;
  color: #999;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header .user-info {
  display: flex;
  align-items: center;
}

.chat-header .avatar {
  width: 36px;
  height: 36px;
  margin-right: 12px;
  flex-shrink: 0;
}

.chat-header .avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.chat-header .name {
  font-size: 16px;
  font-weight: 500;
}

.messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f5f7fa;
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 70%;
}

.message.other {
  align-self: flex-start;
}

.message.own {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.message-body {
  display: flex;
  flex-direction: column;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 14px;
}

.message.other .message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message.own .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 1px 2px rgba(102, 126, 234, 0.3);
}

.message-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  padding: 0 4px;
}

.message-time {
  font-size: 11px;
  color: #999;
}

.message.own .message-footer {
  justify-content: flex-end;
}

.message-input {
  padding: 20px;
  border-top: 1px solid #e6e6e6;
}

.empty-tip {
  padding: 40px 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.empty-messages {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 40px 0;
}

.no-chat-selected {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 订单弹窗样式 */
.order-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #666;
}

.goods-preview {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.goods-preview .goods-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.goods-detail {
  flex: 1;
}

.goods-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.goods-price {
  font-size: 14px;
  color: #666;
}

.goods-price.actual-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: 500;
  margin-top: 4px;
}

.order-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.price-changed-tag {
  font-size: 12px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 4px;
}

.order-info {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
  margin-top: 16px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .label {
  color: #666;
  width: 80px;
}

.info-item .value {
  color: #333;
}

.order-actions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e6e6e6;
}

.change-price-form {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-tip {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
}

.buyer-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.wait-order {
  padding: 20px;
}
</style>

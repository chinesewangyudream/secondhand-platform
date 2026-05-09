<!-- src/components/GoodsCard.vue -->
<template>
  <div class="goods-card" @click="$emit('click')">
    <!-- Image Container -->
    <div class="card-media">
      <img
        v-if="goods.images && goods.images.length > 0"
        :src="goods.images[0]"
        :alt="goods.title"
        class="product-img"
        loading="lazy"
      />
      <div v-else class="img-placeholder">
        <el-icon :size="48"><Picture /></el-icon>
        <span>暂无图片</span>
      </div>

      <!-- Overlay Actions -->
      <div class="card-overlay">
        <button class="overlay-btn" @click.stop>
          <el-icon><View /></el-icon>
        </button>
        <button class="overlay-btn" @click.stop>
          <el-icon><Star /></el-icon>
        </button>
      </div>

      <!-- Status Tags -->
      <div class="card-tags">
        <span v-if="goods.isAuction" class="tag tag-auction">
          <el-icon><Timer /></el-icon>
          <span>拍卖中</span>
        </span>
        <span v-if="goods.status === 2" class="tag tag-sold">
          已售
        </span>
        <span v-if="goods.conditionLevel" class="tag tag-condition">
          {{ conditionLabel }}
        </span>
      </div>
    </div>

    <!-- Content -->
    <div class="card-content">
      <!-- Title -->
      <h3 class="card-title">{{ goods.title }}</h3>

      <!-- Price -->
      <div class="price-section">
        <div class="price-main">
          <span class="price-symbol">¥</span>
          <span class="price-value">{{ formatPrice(goods.price) }}</span>
        </div>
        <span v-if="goods.originalPrice" class="price-original">
          ¥{{ formatPrice(goods.originalPrice) }}
        </span>
      </div>

      <!-- Meta Info -->
      <div class="card-meta">
        <div class="meta-item">
          <el-icon><Location /></el-icon>
          <span>{{ goods.location || '未设置' }}</span>
        </div>
        <div class="meta-item" v-if="goods.viewCount">
          <el-icon><View /></el-icon>
          <span>{{ goods.viewCount }}次浏览</span>
        </div>
      </div>

      <!-- AI Price Estimate -->
      <div v-if="goods.aiEstimatedPrice" class="ai-badge">
        <el-icon><Cpu /></el-icon>
        <span>AI估价 ¥{{ formatPrice(goods.aiEstimatedPrice) }}</span>
      </div>

      <!-- Seller Info -->
      <div class="card-footer">
        <div class="seller-info">
          <el-avatar :size="24" :src="goods.sellerAvatar" class="seller-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="seller-name">{{ goods.sellerName || '匿名用户' }}</span>
        </div>
        <span class="post-time">{{ formatTime(goods.createTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Picture, Location, Cpu, Timer, View, Star, User } from '@element-plus/icons-vue'

const props = defineProps({
  goods: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

// 成色标签
const conditionLabel = computed(() => {
  const level = props.goods.conditionLevel
  if (level >= 9) return '全新'
  if (level >= 7) return '几乎全新'
  if (level >= 5) return '轻微使用'
  if (level >= 3) return '明显使用'
  return '有明显瑕疵'
})

// 格式化价格
function formatPrice(price) {
  if (!price) return '0'
  return Number(price).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

// 格式化时间
function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  return `${Math.floor(days / 30)}月前`
}
</script>

<style scoped>
.goods-card {
  position: relative;
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid var(--color-border-light);
}

.goods-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
  border-color: transparent;
}

/* ========== Media Section ========== */
.card-media {
  position: relative;
  aspect-ratio: 4/3;
  overflow: hidden;
  background: var(--color-bg-secondary);
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-slow);
}

.goods-card:hover .product-img {
  transform: scale(1.08);
}

.img-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  color: var(--color-text-muted);
}

.img-placeholder span {
  font-size: var(--text-xs);
}

/* Overlay Actions */
.card-overlay {
  position: absolute;
  top: var(--space-sm);
  right: var(--space-sm);
  display: flex;
  gap: var(--space-xs);
  opacity: 0;
  transform: translateY(-8px);
  transition: all var(--transition-base);
}

.goods-card:hover .card-overlay {
  opacity: 1;
  transform: translateY(0);
}

.overlay-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  backdrop-filter: blur(4px);
}

.overlay-btn:hover {
  background: var(--color-primary);
  color: white;
  transform: scale(1.1);
}

/* Tags */
.card-tags {
  position: absolute;
  bottom: var(--space-sm);
  left: var(--space-sm);
  display: flex;
  gap: var(--space-xs);
  flex-wrap: wrap;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.tag-auction {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
}

.tag-sold {
  background: rgba(0, 0, 0, 0.6);
  color: white;
}

.tag-condition {
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-text-secondary);
}

/* ========== Content Section ========== */
.card-content {
  padding: var(--space-md);
}

.card-title {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 var(--space-sm) 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}

/* Price */
.price-section {
  display: flex;
  align-items: baseline;
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.price-main {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-primary);
  margin-right: 2px;
}

.price-value {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.price-original {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  text-decoration: line-through;
}

/* Meta Info */
.card-meta {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.meta-item .el-icon {
  font-size: var(--text-sm);
}

/* AI Badge */
.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-xs) var(--space-sm);
  background: linear-gradient(135deg, rgba(184, 134, 11, 0.1) 0%, rgba(218, 165, 32, 0.1) 100%);
  border: 1px solid rgba(184, 134, 11, 0.2);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  color: var(--color-secondary-dark);
  margin-bottom: var(--space-sm);
}

/* Footer */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-sm);
  border-top: 1px solid var(--color-border-light);
}

.seller-info {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.seller-avatar {
  background: var(--color-bg-tertiary);
  color: var(--color-text-muted);
}

.seller-name {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

/* ========== Hover Effects ========== */
.goods-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform var(--transition-base);
}

.goods-card:hover::before {
  transform: scaleX(1);
}
</style>

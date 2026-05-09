<!-- src/components/GoodsCard.vue -->
<template>
  <div class="goods-card" @click="$emit('click')">
    <div class="card-image">
      <img
        v-if="goods.images && goods.images.length > 0"
        :src="goods.images[0]"
        :alt="goods.title"
        class="product-img"
      />
      <div v-else class="img-placeholder">
        <el-icon size="48" color="#ccc"><Picture /></el-icon>
      </div>

      <!-- 商品标签 -->
      <div v-if="goods.isAuction" class="auction-tag">
        <el-icon><Timer /></el-icon>
        拍卖
      </div>
      <div v-if="goods.status === 'sold'" class="sold-tag">
        已售出
      </div>
    </div>

    <!-- 商品信息 -->
    <div class="card-info">
      <p class="title">{{ goods.title }}</p>
      <div class="price-row">
        <span class="price">¥ {{ goods.price }}</span>
        <span v-if="goods.originalPrice" class="original-price">
          ¥{{ goods.originalPrice }}
        </span>
      </div>
      <div class="meta-row">
        <el-rate
          v-if="goods.conditionLevel"
          :model-value="goods.conditionLevel / 2"
          disabled
          show-score
          :max="5"
          size="small"
          score-template="{value}成新"
        />
        <span class="location">
          <el-icon><Location /></el-icon>
          {{ goods.location || '未知' }}
        </span>
      </div>
      <!-- AI估价 -->
      <div v-if="goods.aiEstimatedPrice" class="ai-price">
        <el-icon color="#E6A23C"><Cpu /></el-icon>
        AI估价: ¥{{ goods.aiEstimatedPrice }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { Picture, Location, Cpu, Timer } from '@element-plus/icons-vue'

defineProps({
  goods: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])
</script>

<style scoped>
.goods-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.goods-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}

.card-image {
  position: relative;
  padding-top: 75%;
  overflow: hidden;
  background: #f5f7fa;
}
.product-img {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  object-fit: cover;
}
.img-placeholder {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
}
.auction-tag, .sold-tag {
  position: absolute;
  top: 8px; left: 8px;
  background: #409EFF;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.sold-tag {
  left: auto; right: 8px;
  background: #f5222d;
}

.card-info { padding: 12px; }
.title {
  font-size: 14px;
  color: #2c3e50;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}
.price { color: #f5222d; font-size: 18px; font-weight: 700; }
.original-price { color: #999; font-size: 12px; text-decoration: line-through; }

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.location {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #999;
}
.ai-price {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #E6A23C;
  margin-top: 4px;
}
</style>
<!-- src/views/Logistics.vue -->
<template>
  <div class="logistics-page">
    <h2>物流信息</h2>
    <div class="logistics-content" v-loading="loading">
      <div v-if="logistics" class="logistics-detail">
        <div class="order-info">
          <p><strong>订单号:</strong> {{ logistics.orderId }}</p>
          <p><strong>物流公司:</strong> {{ logistics.company }}</p>
          <p><strong>运单号:</strong> {{ logistics.trackingNumber }}</p>
        </div>

        <div class="tracking-steps">
          <el-steps direction="vertical" :active="logistics.currentStep">
            <el-step
              v-for="(step, index) in logistics.steps"
              :key="index"
              :title="step.title"
              :description="step.description"
              :timestamp="step.timestamp"
            />
          </el-steps>
        </div>
      </div>
      <div v-else class="no-logistics">
        <p>暂无物流信息</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(false)
const logistics = ref(null)

onMounted(async () => {
  await loadLogistics()
})

async function loadLogistics() {
  loading.value = true
  try {
    const orderId = route.params.orderId
    const res = await orderApi.getLogistics(orderId)
    logistics.value = res.data
  } catch (error) {
    ElMessage.error('加载物流信息失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.logistics-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.logistics-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.order-info {
  margin-bottom: 30px;
}

.order-info p {
  margin: 8px 0;
  font-size: 14px;
}

.tracking-steps {
  margin-top: 20px;
}

.no-logistics {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
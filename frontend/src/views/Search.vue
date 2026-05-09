<!-- src/views/Search.vue -->
<template>
  <div class="search-page">
    <div class="search-header">
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品..."
        @keyup.enter="performSearch"
        clearable
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="performSearch">搜索</el-button>
    </div>

    <div class="filters">
      <el-select v-model="selectedCategory" placeholder="选择分类" clearable>
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      <el-select v-model="sortBy" placeholder="排序方式">
        <el-option label="最新发布" value="newest" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
      </el-select>
    </div>

    <div class="goods-grid" v-loading="loading">
      <GoodsCard
        v-for="goods in goodsList"
        :key="goods.id"
        :goods="goods"
        @click="router.push(`/goods/${goods.id}`)"
      />
    </div>

    <div v-if="goodsList.length === 0 && !loading" class="no-results">
      <p>没有找到相关商品</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { Search } from '@element-plus/icons-vue'
import GoodsCard from '@/components/GoodsCard.vue'

const route = useRoute()
const router = useRouter()

const searchQuery = ref('')
const selectedCategory = ref('')
const sortBy = ref('newest')
const loading = ref(false)
const goodsList = ref([])
const categories = ref([])
const isInitialized = ref(false)

onMounted(async () => {
  await loadCategories()
  // 从路由参数获取初始搜索条件
  searchQuery.value = route.query.q || ''
  if (route.query.categoryId) {
    selectedCategory.value = Number(route.query.categoryId)
  }
  isInitialized.value = true
  await performSearch()
})

// 监听路由变化（从其他页面跳转过来时）
watch(() => route.query, (newQuery) => {
  if (!isInitialized.value) return

  const newCategory = newQuery.categoryId ? Number(newQuery.categoryId) : ''
  const newKeyword = newQuery.q || ''

  // 只在参数确实变化时更新
  if (newCategory !== selectedCategory.value || newKeyword !== searchQuery.value) {
    selectedCategory.value = newCategory
    searchQuery.value = newKeyword
    performSearch()
  }
}, { deep: true })

// 监听筛选条件变化（用户在页面上操作时）
watch([selectedCategory, sortBy], () => {
  if (isInitialized.value) {
    performSearch()
  }
})

async function loadCategories() {
  try {
    const res = await goodsApi.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

async function performSearch() {
  loading.value = true
  try {
    const params = {
      keyword: searchQuery.value,
      categoryId: selectedCategory.value || undefined,
      sortBy: sortBy.value,
      page: 1,
      size: 20
    }
    // 移除空值参数
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    const res = await goodsApi.search(params)
    goodsList.value = res.data.records || []
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.search-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-header {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.no-results {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
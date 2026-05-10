<!-- src/views/Search.vue -->
<template>
  <div class="search-page">
    <div class="search-header">
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品名称、描述、地点..."
        @keyup.enter="performSearch"
        clearable
        size="large"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" size="large" @click="performSearch">搜索</el-button>
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
      <el-select v-model="selectedCondition" placeholder="最低成色" clearable>
        <el-option label="全新(10)" :value="10" />
        <el-option label="几乎全新(9)" :value="9" />
        <el-option label="轻微使用(8)" :value="8" />
        <el-option label="明显使用(6)" :value="6" />
      </el-select>
      <el-select v-model="sortBy" placeholder="排序方式">
        <el-option label="相关度优先" value="relevance" />
        <el-option label="最新发布" value="newest" />
        <el-option label="价格从低到高" value="price_asc" />
        <el-option label="价格从高到低" value="price_desc" />
      </el-select>
      <div class="price-filter">
        <el-input-number
          v-model="minPrice"
          :min="0"
          :precision="0"
          placeholder="最低价"
          controls-position="right"
          size="default"
        />
        <span class="price-separator">-</span>
        <el-input-number
          v-model="maxPrice"
          :min="0"
          :precision="0"
          placeholder="最高价"
          controls-position="right"
          size="default"
        />
      </div>
      <el-button @click="resetFilters" text>重置筛选</el-button>
    </div>

    <div class="search-info" v-if="searched && !loading">
      <span v-if="goodsList.length > 0">
        找到 <strong>{{ total }}</strong> 件相关商品
        <template v-if="searchQuery">
          ，关键词：<el-tag v-for="kw in keywords" :key="kw" size="small" closable @close="removeKeyword(kw)">{{ kw }}</el-tag>
        </template>
      </span>
    </div>

    <div class="goods-grid" v-loading="loading">
      <GoodsCard
        v-for="goods in goodsList"
        :key="goods.id"
        :goods="goods"
        @click="router.push(`/goods/${goods.id}`)"
      />
    </div>

    <div v-if="goodsList.length === 0 && !loading && searched" class="no-results">
      <span class="no-results-icon">🔍</span>
      <p>没有找到相关商品</p>
      <p class="no-results-tip">试试其他关键词，或减少筛选条件</p>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { Search } from '@element-plus/icons-vue'
import GoodsCard from '@/components/GoodsCard.vue'

const route = useRoute()
const router = useRouter()

const searchQuery = ref('')
const selectedCategory = ref('')
const selectedCondition = ref('')
const sortBy = ref('relevance')
const minPrice = ref(undefined)
const maxPrice = ref(undefined)
const loading = ref(false)
const searched = ref(false)
const goodsList = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = 20
const total = ref(0)
const isInitialized = ref(false)

const keywords = computed(() => {
  if (!searchQuery.value) return []
  return searchQuery.value.trim().split(/\s+/).filter(k => k)
})

onMounted(async () => {
  await loadCategories()
  searchQuery.value = route.query.q || ''
  if (route.query.categoryId) {
    selectedCategory.value = Number(route.query.categoryId)
  }
  isInitialized.value = true
  await performSearch()
})

watch(() => route.query, (newQuery) => {
  if (!isInitialized.value) return
  const newCategory = newQuery.categoryId ? Number(newQuery.categoryId) : ''
  const newKeyword = newQuery.q || ''
  if (newCategory !== selectedCategory.value || newKeyword !== searchQuery.value) {
    selectedCategory.value = newCategory
    searchQuery.value = newKeyword
    currentPage.value = 1
    performSearch()
  }
}, { deep: true })

watch([selectedCategory, selectedCondition, sortBy], () => {
  if (isInitialized.value) {
    currentPage.value = 1
    performSearch()
  }
})

watch([minPrice, maxPrice], () => {
  if (isInitialized.value) {
    currentPage.value = 1
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
  searched.value = true
  try {
    const params = {
      keyword: searchQuery.value || undefined,
      categoryId: selectedCategory.value || undefined,
      conditionLevel: selectedCondition.value || undefined,
      minPrice: minPrice.value || undefined,
      maxPrice: maxPrice.value || undefined,
      sortBy: sortBy.value,
      page: currentPage.value,
      size: pageSize
    }
    Object.keys(params).forEach(key => {
      if (params[key] === undefined || params[key] === '') {
        delete params[key]
      }
    })
    const res = await goodsApi.search(params)
    goodsList.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

function onPageChange(page) {
  currentPage.value = page
  performSearch()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function removeKeyword(kw) {
  const parts = searchQuery.value.trim().split(/\s+/).filter(k => k !== kw)
  searchQuery.value = parts.join(' ')
  currentPage.value = 1
  performSearch()
}

function resetFilters() {
  selectedCategory.value = ''
  selectedCondition.value = ''
  sortBy.value = 'relevance'
  minPrice.value = undefined
  maxPrice.value = undefined
  searchQuery.value = ''
  currentPage.value = 1
  performSearch()
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

.search-header .el-input {
  flex: 1;
}

.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-filter .el-input-number {
  width: 110px;
}

.price-separator {
  color: #999;
  margin: 0 2px;
}

.search-info {
  margin-bottom: 16px;
  color: #666;
  font-size: 14px;
}

.search-info strong {
  color: var(--color-primary, #c45c3e);
}

.search-info .el-tag {
  margin-left: 4px;
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.no-results {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.no-results-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.no-results p {
  margin: 4px 0;
}

.no-results-tip {
  font-size: 13px;
  color: #bbb;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>

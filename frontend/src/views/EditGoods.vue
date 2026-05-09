<!-- src/views/EditGoods.vue -->
<template>
  <div class="edit-goods-page">
    <div class="page-header">
      <h2>{{ isResubmit ? '整改商品' : '编辑商品' }}</h2>
    </div>

    <!-- 违规提示 -->
    <el-alert
      v-if="isResubmit && violationReason"
      title="该商品被标记为违规，请根据违规原因进行整改后重新提交审核"
      type="error"
      :closable="false"
      show-icon
      class="violation-alert"
    >
      <template #default>
        <p><strong>违规原因：</strong>{{ violationReason }}</p>
        <p>请修改商品信息后点击「提交审核」按钮重新提交。</p>
      </template>
    </el-alert>

    <el-form
      :model="form"
      :rules="rules"
      ref="formRef"
      label-width="80px"
      class="edit-form"
    >
      <el-form-item label="商品标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入商品标题" />
      </el-form-item>

      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="成色等级" prop="conditionLevel">
        <el-rate
          v-model="form.conditionLevel"
          :max="10"
          show-score
          score-template="{value}成新"
        />
      </el-form-item>

      <el-form-item>
        <el-button
          type="success"
          @click="generateAiContent"
          :loading="generating"
          :disabled="!form.title || !form.categoryId"
        >
          <el-icon><Cpu /></el-icon>
          AI 生成描述和估价
        </el-button>
        <span class="ai-tip" v-if="!form.title || !form.categoryId">
          （请先填写标题和分类）
        </span>
      </el-form-item>

      <el-form-item label="商品价格" prop="price">
        <el-input-number
          v-model="form.price"
          :min="0"
          :precision="2"
          controls-position="right"
        />
        <span v-if="aiPrice" class="ai-price-hint">
          AI 建议价格：¥{{ aiPrice }}
        </span>
      </el-form-item>

      <el-form-item label="商品图片" prop="images">
        <el-upload
          v-model:file-list="fileList"
          action=""
          list-type="picture-card"
          :auto-upload="false"
          :multiple="true"
          :limit="9"
          accept="image/*"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="6"
          placeholder="请详细描述商品信息（可选）"
        />
      </el-form-item>

      <el-form-item label="所在地区" prop="location">
        <el-input v-model="form.location" placeholder="请输入所在地区" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isResubmit ? '提交审核' : '保存修改' }}
        </el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { Plus, Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const submitting = ref(false)
const generating = ref(false)
const fileList = ref([])
const categories = ref([])
const goodsId = ref(null)
const aiPrice = ref(null)
const violationReason = ref('')
const goodsStatus = ref(null)

// 是否是整改重新提交
const isResubmit = computed(() => route.query.resubmit === '1')

const form = ref({
  title: '',
  categoryId: '',
  price: 0,
  description: '',
  conditionLevel: 7,
  location: '',
  images: []
})

const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  description: [
    { required: false, message: '请输入商品描述', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入所在地区', trigger: 'blur' }
  ]
}

onMounted(async () => {
  goodsId.value = route.params.id
  await Promise.all([
    loadCategories(),
    loadGoodsDetail()
  ])
})

async function loadCategories() {
  try {
    const res = await goodsApi.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

async function loadGoodsDetail() {
  if (!goodsId.value) return
  try {
    const res = await goodsApi.getDetail(goodsId.value)
    const goods = res.data
    if (goods) {
      form.value = {
        title: goods.title || '',
        categoryId: goods.categoryId || '',
        price: goods.price || 0,
        description: goods.description || '',
        conditionLevel: goods.conditionLevel || 7,
        location: goods.location || '',
        images: goods.images || []
      }
      goodsStatus.value = goods.status
      violationReason.value = goods.violationReason || ''
      // 设置图片列表
      if (goods.images && Array.isArray(goods.images)) {
        fileList.value = goods.images.map(url => ({
          name: url.substring(url.lastIndexOf('/') + 1),
          url: url
        }))
      }
    }
  } catch (error) {
    ElMessage.error('加载商品详情失败')
    console.error('加载商品详情失败:', error)
  }
}

async function generateAiContent() {
  if (!form.value.title || !form.value.categoryId) {
    ElMessage.warning('请先填写商品标题和分类')
    return
  }

  generating.value = true
  try {
    const res = await goodsApi.aiGenerate({
      title: form.value.title,
      categoryId: form.value.categoryId,
      conditionLevel: form.value.conditionLevel
    })

    if (res.data) {
      // 填充描述
      if (res.data.description) {
        form.value.description = res.data.description
      }
      // 显示建议价格
      if (res.data.price) {
        aiPrice.value = res.data.price
        // 如果用户还没设置价格，自动填充
        if (form.value.price === 0) {
          form.value.price = res.data.price
        }
      }
      ElMessage.success('AI 生成成功')
    }
  } catch (error) {
    console.error('AI生成失败:', error)
    ElMessage.error('AI 生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

async function submitForm() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 先上传新图片
        const imageUrls = []
        for (const file of fileList.value) {
          if (file.raw) {
            const res = await goodsApi.uploadImage(file.raw)
            imageUrls.push(res.data)
          } else if (file.url) {
            imageUrls.push(file.url)
          }
        }

        // 提交商品数据
        const data = {
          ...form.value,
          images: imageUrls
        }
        await goodsApi.update(goodsId.value, data)

        // 如果是整改重新提交
        if (isResubmit.value && goodsStatus.value === 5) {
          await goodsApi.resubmitForAudit(goodsId.value)
          ElMessage.success('商品已整改并重新提交审核')
        } else {
          ElMessage.success('商品更新成功')
        }
        router.push('/my-goods')
      } catch (error) {
        ElMessage.error('更新失败，请重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

function resetForm() {
  formRef.value?.resetFields()
  fileList.value = []
  aiPrice.value = null
}

function goBack() {
  router.push('/my-goods')
}
</script>

<style scoped>
.edit-goods-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 24px;
  color: #333;
}

.edit-form {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.ai-tip {
  margin-left: 12px;
  color: #999;
  font-size: 12px;
}

.ai-price-hint {
  margin-left: 12px;
  color: #E6A23C;
  font-size: 13px;
}

.violation-alert {
  margin-bottom: 20px;
}

.violation-alert p {
  margin: 4px 0;
}
</style>

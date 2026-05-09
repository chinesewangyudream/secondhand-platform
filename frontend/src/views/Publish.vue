<!-- src/views/Publish.vue -->
<template>
  <div class="publish-page">
    <div class="page-header">
      <h2>发布商品</h2>
    </div>

    <el-form
      :model="form"
      :rules="rules"
      ref="formRef"
      label-width="80px"
      class="publish-form"
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

      <!-- 拍卖设置 -->
      <el-divider>
        <el-checkbox v-model="form.isAuction" :true-label="1" :false-label="0">
          作为拍卖商品发布
        </el-checkbox>
      </el-divider>

      <template v-if="form.isAuction === 1">
        <el-form-item label="起拍价" prop="startPrice" :rules="auctionRules.startPrice">
          <el-input-number
            v-model="form.startPrice"
            :min="0.01"
            :precision="2"
            controls-position="right"
            placeholder="起拍价"
          />
        </el-form-item>

        <el-form-item label="加价幅度" prop="minIncrement">
          <el-input-number
            v-model="form.minIncrement"
            :min="1"
            :precision="2"
            controls-position="right"
            placeholder="每次加价最少金额"
          />
        </el-form-item>

        <el-form-item label="一口价" prop="buyNowPrice">
          <el-input-number
            v-model="form.buyNowPrice"
            :min="form.startPrice || 0.01"
            :precision="2"
            controls-position="right"
            placeholder="可选，达到此价格直接成交"
          />
          <span class="form-tip">（可选，设置后买家可一键购买）</span>
        </el-form-item>

        <el-form-item label="拍卖时间" required>
          <div class="auction-time-picker">
            <el-date-picker
              v-model="form.auctionStartTime"
              type="datetime"
              placeholder="开始时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledStartDate"
            />
            <span class="time-separator">至</span>
            <el-date-picker
              v-model="form.auctionEndTime"
              type="datetime"
              placeholder="结束时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled-date="disabledEndDate"
            />
          </div>
        </el-form-item>
      </template>

      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          发布商品
        </el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { goodsApi } from '@/api/goods'
import { Plus, Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const generating = ref(false)
const fileList = ref([])
const categories = ref([])
const aiPrice = ref(null)

const form = ref({
  title: '',
  categoryId: '',
  price: 0,
  description: '',
  conditionLevel: 7,
  location: '',
  images: [],
  isAuction: 0,
  startPrice: null,
  minIncrement: 10,
  buyNowPrice: null,
  auctionStartTime: '',
  auctionEndTime: ''
})

const auctionRules = {
  startPrice: [
    { required: true, message: '请输入起拍价', trigger: 'blur' }
  ]
}

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
  await loadCategories()
})

async function loadCategories() {
  try {
    const res = await goodsApi.getCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败:', error)
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

  // 验证拍卖信息
  if (form.value.isAuction === 1) {
    if (!form.value.startPrice) {
      ElMessage.warning('请输入起拍价')
      return
    }
    if (!form.value.auctionStartTime || !form.value.auctionEndTime) {
      ElMessage.warning('请选择拍卖时间')
      return
    }
    const startTime = new Date(form.value.auctionStartTime).getTime()
    const endTime = new Date(form.value.auctionEndTime).getTime()
    if (endTime <= startTime) {
      ElMessage.warning('结束时间必须晚于开始时间')
      return
    }
  }

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 先上传图片
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
        await goodsApi.publish(data)
        ElMessage.success('商品发布成功')
        router.push('/home')
      } catch (error) {
        ElMessage.error('发布失败，请重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 禁用过去的日期
function disabledStartDate(time) {
  return time.getTime() < Date.now() - 8.64e7
}

function disabledEndDate(time) {
  if (form.value.auctionStartTime) {
    return time.getTime() < new Date(form.value.auctionStartTime).getTime()
  }
  return time.getTime() < Date.now() - 8.64e7
}

function resetForm() {
  formRef.value?.resetFields()
  fileList.value = []
  aiPrice.value = null
}
</script>

<style scoped>
.publish-page {
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

.publish-form {
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

.form-tip {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}

.auction-time-picker {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-separator {
  color: #666;
}
</style>
<template>
  <div class="audit-page">
    <div class="page-header">
      <h2>商品审核</h2>
    </div>

    <div class="audit-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 待审核商品 -->
        <el-tab-pane label="待审核" name="pending">
          <div class="goods-list" v-loading="loading">
            <div v-if="pendingList.length === 0" class="no-data">
              <p>暂无待审核商品</p>
            </div>
            <div v-else class="goods-table">
              <el-table :data="pendingList" stripe>
                <el-table-column label="商品图片" width="100">
                  <template #default="{ row }">
                    <el-image
                      :src="row.images?.[0]"
                      :preview-src-list="row.images"
                      fit="cover"
                      class="goods-image"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="150" />
                <el-table-column label="价格" width="100">
                  <template #default="{ row }">
                    <span class="price">¥{{ row.price }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="conditionLevel" label="成色" width="80">
                  <template #default="{ row }">
                    {{ row.conditionLevel }}成新
                  </template>
                </el-table-column>
                <el-table-column prop="location" label="所在地" width="100" />
                <el-table-column label="提交时间" width="150">
                  <template #default="{ row }">
                    {{ formatDate(row.updatedAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-button size="small" type="primary" @click="viewDetail(row)">
                        查看
                      </el-button>
                      <el-button size="small" type="success" @click="handleApprove(row)">
                        通过
                      </el-button>
                      <el-button size="small" type="danger" @click="showRejectDialog(row)">
                        驳回
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination">
                <el-pagination
                  v-model:current-page="pendingPage"
                  :page-size="10"
                  :total="pendingTotal"
                  layout="prev, pager, next"
                  @current-change="loadPendingList"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 违规商品 -->
        <el-tab-pane label="违规商品" name="violation">
          <div class="goods-list" v-loading="loading">
            <div v-if="violationList.length === 0" class="no-data">
              <p>暂无违规商品</p>
            </div>
            <div v-else class="goods-table">
              <el-table :data="violationList" stripe>
                <el-table-column label="商品图片" width="100">
                  <template #default="{ row }">
                    <el-image
                      :src="row.images?.[0]"
                      :preview-src-list="row.images"
                      fit="cover"
                      class="goods-image"
                    />
                  </template>
                </el-table-column>
                <el-table-column prop="title" label="标题" min-width="150" />
                <el-table-column label="价格" width="100">
                  <template #default="{ row }">
                    <span class="price">¥{{ row.price }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="violationReason" label="违规原因" min-width="200">
                  <template #default="{ row }">
                    <span class="violation-reason">{{ row.violationReason }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="标记时间" width="150">
                  <template #default="{ row }">
                    {{ formatDate(row.updatedAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-button size="small" type="primary" @click="viewDetail(row)">
                        查看
                      </el-button>
                      <template v-if="row.status === 6">
                        <el-button size="small" type="success" @click="handleViolationApprove(row)">
                          通过
                        </el-button>
                        <el-button size="small" type="danger" @click="showViolationRejectDialog(row)">
                          驳回
                        </el-button>
                      </template>
                    </div>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination">
                <el-pagination
                  v-model:current-page="violationPage"
                  :page-size="10"
                  :total="violationTotal"
                  layout="prev, pager, next"
                  @current-change="loadViolationList"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 举报列表 -->
        <el-tab-pane label="用户举报" name="report">
          <div class="goods-list" v-loading="loading">
            <div v-if="reportList.length === 0" class="no-data">
              <p>暂无用户举报</p>
            </div>
            <div v-else class="goods-table">
              <el-table :data="reportList" stripe>
                <el-table-column label="商品图片" width="100">
                  <template #default="{ row }">
                    <el-image
                      v-if="row.goods"
                      :src="row.goods.images?.[0]"
                      :preview-src-list="row.goods.images"
                      fit="cover"
                      class="goods-image"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="商品标题" min-width="150">
                  <template #default="{ row }">
                    {{ row.goods?.title || '商品已删除' }}
                  </template>
                </el-table-column>
                <el-table-column label="价格" width="100">
                  <template #default="{ row }">
                    <span class="price">¥{{ row.goods?.price }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="reason" label="举报原因" min-width="150">
                  <template #default="{ row }">
                    <span class="report-reason">{{ row.reason }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="举报时间" width="150">
                  <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 5 ? 'danger' : 'warning'">
                      {{ row.status === 5 ? '待整改' : '已提交申请' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right">
                  <template #default="{ row }">
                    <template v-if="row.status === 0">
                      <div class="action-buttons">
                        <el-button size="small" type="primary" @click="viewReportDetail(row)">
                          查看
                        </el-button>
                        <el-button size="small" type="warning" @click="showJudgeDialog(row)">
                          判决
                        </el-button>
                      </div>
                    </template>
                    <span v-else class="handled-text">已处理</span>
                  </template>
                </el-table-column>
              </el-table>

              <div class="pagination">
                <el-pagination
                  v-model:current-page="reportPage"
                  :page-size="10"
                  :total="reportTotal"
                  layout="prev, pager, next"
                  @current-change="loadReportList"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回商品" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="商品标题">
          <span>{{ currentGoods?.title }}</span>
        </el-form-item>
        <el-form-item label="违规原因" required>
          <el-select v-model="rejectForm.reason" placeholder="请选择违规原因" style="width: 100%">
            <el-option label="涉嫌出售违禁品" value="涉嫌出售违禁品" />
            <el-option label="商品描述与实际不符" value="商品描述与实际不符" />
            <el-option label="图片涉嫌侵权" value="图片涉嫌侵权" />
            <el-option label="价格异常" value="价格异常" />
            <el-option label="涉嫌欺诈" value="涉嫌欺诈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="rejectForm.reason === '其他'" label="详细说明" required>
          <el-input
            v-model="rejectForm.customReason"
            type="textarea"
            :rows="3"
            placeholder="请输入详细的违规原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject" :loading="rejecting">
          确认驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 违规商品驳回对话框 -->
    <el-dialog v-model="violationRejectDialogVisible" title="驳回整改" width="500px">
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="商品标题">
          <span>{{ currentViolationGoods?.title }}</span>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-select v-model="rejectForm.reason" placeholder="请选择驳回原因" style="width: 100%">
            <el-option label="整改不彻底，仍有违规内容" value="整改不彻底，仍有违规内容" />
            <el-option label="涉嫌出售违禁品" value="涉嫌出售违禁品" />
            <el-option label="商品描述与实际不符" value="商品描述与实际不符" />
            <el-option label="图片涉嫌侵权" value="图片涉嫌侵权" />
            <el-option label="价格异常" value="价格异常" />
            <el-option label="涉嫌欺诈" value="涉嫌欺诈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="rejectForm.reason === '其他'" label="详细说明" required>
          <el-input
            v-model="rejectForm.customReason"
            type="textarea"
            :rows="3"
            placeholder="请输入详细的驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="violationRejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleViolationReject" :loading="rejecting">
          确认驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="商品详情" width="900px">
      <div v-if="currentGoods" class="goods-detail">
        <div class="detail-images">
          <el-carousel height="350px" indicator-position="outside">
            <el-carousel-item v-for="(img, index) in currentGoods.images" :key="index">
              <el-image :src="img" fit="contain" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
        </div>
        <div class="detail-info">
          <h3>{{ currentGoods.title }}</h3>
          <div class="price-row">
            <span class="price">¥{{ currentGoods.price }}</span>
            <span v-if="currentGoods.originalPrice" class="original-price">原价: ¥{{ currentGoods.originalPrice }}</span>
          </div>
          <p><strong>成色：</strong>{{ currentGoods.conditionLevel }}成新</p>
          <p><strong>所在地：</strong>{{ currentGoods.location || '未填写' }}</p>
          <p><strong>浏览量：</strong>{{ currentGoods.viewCount || 0 }} 次</p>
          <p><strong>发布时间：</strong>{{ formatDate(currentGoods.createdAt) }}</p>
          <div class="description-block">
            <p><strong>商品描述：</strong></p>
            <div class="description">{{ currentGoods.description || '无描述' }}</div>
          </div>
          <!-- 违规原因（违规商品显示） -->
          <p v-if="currentGoods.violationReason" class="violation-info">
            <strong>违规原因：</strong>{{ currentGoods.violationReason }}
          </p>
          <!-- 举报原因（举报详情显示） -->
          <p v-if="currentGoods.reportReason" class="report-info">
            <strong>举报原因：</strong>{{ currentGoods.reportReason }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <!-- 待审核商品的操作按钮 -->
        <template v-if="currentGoods?.status === 0 && !currentGoods?.reportId">
          <el-button type="success" @click="handleApprove(currentGoods)">通过</el-button>
          <el-button type="danger" @click="showRejectDialog(currentGoods)">驳回</el-button>
        </template>
        <!-- 违规-待审核商品的操作按钮 -->
        <template v-if="currentGoods?.status === 6">
          <el-button type="success" @click="handleViolationApprove(currentGoods)">通过</el-button>
          <el-button type="danger" @click="showViolationRejectDialog(currentGoods)">驳回</el-button>
        </template>
        <!-- 举报商品的操作按钮 -->
        <template v-if="currentGoods?.reportId">
          <el-button type="warning" @click="showJudgeDialogFromDetail">判决</el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 判决对话框 -->
    <el-dialog v-model="judgeDialogVisible" title="举报判决" width="500px">
      <div class="judge-content">
        <p class="judge-tip">请对该举报进行判决：</p>
        <div class="judge-goods-info">
          <p><strong>商品：</strong>{{ currentReport?.goods?.title }}</p>
          <p><strong>举报原因：</strong>{{ currentReport?.reason }}</p>
        </div>
        <el-divider />
        <div class="judge-options">
          <div class="judge-option violation" @click="handleJudge(true)">
            <el-icon size="24"><CircleCloseFilled /></el-icon>
            <span>判定违规</span>
            <p class="option-desc">商品将被下架，卖家需要整改后重新提交</p>
          </div>
          <div class="judge-option valid" @click="handleJudge(false)">
            <el-icon size="24"><CircleCheckFilled /></el-icon>
            <span>判定符合</span>
            <p class="option-desc">商品内容符合规范，举报不成立</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { goodsApi } from '@/api/goods'
import { reportApi } from '@/api/report'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CircleCloseFilled, CircleCheckFilled } from '@element-plus/icons-vue'

const activeTab = ref('pending')
const loading = ref(false)

// 待审核列表
const pendingList = ref([])
const pendingPage = ref(1)
const pendingTotal = ref(0)

// 违规列表
const violationList = ref([])
const violationPage = ref(1)
const violationTotal = ref(0)

// 举报列表
const reportList = ref([])
const reportPage = ref(1)
const reportTotal = ref(0)

// 驳回对话框
const rejectDialogVisible = ref(false)
const currentGoods = ref(null)
const rejectForm = ref({
  reason: '',
  customReason: ''
})
const rejecting = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)

// 判决对话框
const judgeDialogVisible = ref(false)
const currentReport = ref(null)

// 违规商品驳回对话框
const violationRejectDialogVisible = ref(false)
const currentViolationGoods = ref(null)

onMounted(async () => {
  await loadPendingList()
})

async function handleTabClick() {
  if (activeTab.value === 'pending') {
    await loadPendingList()
  } else if (activeTab.value === 'violation') {
    await loadViolationList()
  } else if (activeTab.value === 'report') {
    await loadReportList()
  }
}

async function loadPendingList() {
  loading.value = true
  try {
    const res = await goodsApi.getPendingGoods({ page: pendingPage.value, size: 10 })
    pendingList.value = res.data?.records || []
    pendingTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载待审核商品失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function loadViolationList() {
  loading.value = true
  try {
    const res = await goodsApi.getViolationGoods({ page: violationPage.value, size: 10 })
    violationList.value = res.data?.records || []
    violationTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载违规商品失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function loadReportList() {
  loading.value = true
  try {
    const res = await reportApi.getList({ page: reportPage.value, size: 10, status: 0 })
    reportList.value = res.data?.records || []
    reportTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载举报列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function viewDetail(goods) {
  currentGoods.value = goods
  detailDialogVisible.value = true
}

function showRejectDialog(goods) {
  currentGoods.value = goods
  rejectForm.value = { reason: '', customReason: '' }
  detailDialogVisible.value = false
  rejectDialogVisible.value = true
}

async function handleApprove(goods) {
  try {
    await ElMessageBox.confirm('确定通过该商品的审核吗？', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })

    await goodsApi.auditGoods({
      goodsId: goods.id,
      approved: true
    })

    ElMessage.success('审核通过')
    detailDialogVisible.value = false
    await loadPendingList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

async function handleReject() {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请选择违规原因')
    return
  }

  if (rejectForm.value.reason === '其他' && !rejectForm.value.customReason) {
    ElMessage.warning('请输入详细的违规原因')
    return
  }

  const violationReason = rejectForm.value.reason === '其他'
    ? rejectForm.value.customReason
    : rejectForm.value.reason

  rejecting.value = true
  try {
    await goodsApi.auditGoods({
      goodsId: currentGoods.value.id,
      approved: false,
      violationReason
    })

    ElMessage.success('已驳回该商品')
    rejectDialogVisible.value = false
    await loadPendingList()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  } finally {
    rejecting.value = false
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 违规商品审核通过
async function handleViolationApprove(goods) {
  try {
    await ElMessageBox.confirm('确定通过该商品的审核吗？商品将重新上架。', '确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    })

    await goodsApi.auditGoods({
      goodsId: goods.id,
      approved: true
    })

    ElMessage.success('审核通过，商品已重新上架')
    detailDialogVisible.value = false
    await loadViolationList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
    }
  }
}

// 显示违规商品驳回对话框
function showViolationRejectDialog(goods) {
  currentViolationGoods.value = goods
  rejectForm.value = { reason: '', customReason: '' }
  detailDialogVisible.value = false
  violationRejectDialogVisible.value = true
}

// 违规商品驳回处理
async function handleViolationReject() {
  if (!rejectForm.value.reason) {
    ElMessage.warning('请选择驳回原因')
    return
  }

  if (rejectForm.value.reason === '其他' && !rejectForm.value.customReason) {
    ElMessage.warning('请输入详细的驳回原因')
    return
  }

  const violationReason = rejectForm.value.reason === '其他'
    ? rejectForm.value.customReason
    : rejectForm.value.reason

  rejecting.value = true
  try {
    await goodsApi.auditGoods({
      goodsId: currentViolationGoods.value.id,
      approved: false,
      violationReason
    })

    ElMessage.success('已驳回，商品仍为违规状态')
    violationRejectDialogVisible.value = false
    await loadViolationList()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  } finally {
    rejecting.value = false
  }
}

function viewReportDetail(report) {
  if (report.goods) {
    currentGoods.value = {
      ...report.goods,
      reportReason: report.reason,
      reportId: report.id
    }
    currentReport.value = report
    detailDialogVisible.value = true
  } else {
    ElMessage.warning('该商品已被删除')
  }
}

function showJudgeDialog(report) {
  currentReport.value = report
  judgeDialogVisible.value = true
}

function showJudgeDialogFromDetail() {
  detailDialogVisible.value = false
  judgeDialogVisible.value = true
}

async function handleJudge(isViolation) {
  try {
    await reportApi.handle(currentReport.value.id, {
      isViolation,
      handleResult: isViolation ? '确认违规，商品已下架' : '经核实符合规范'
    })

    ElMessage.success(isViolation ? '已判定违规，商品已下架' : '已判定符合规范')
    judgeDialogVisible.value = false
    detailDialogVisible.value = false
    await loadReportList()
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.audit-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.audit-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.goods-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.price {
  color: #f5222d;
  font-weight: 600;
}

.violation-reason {
  color: #ff4d4f;
}

.report-reason {
  color: #E6A23C;
}

.handled-text {
  color: #999;
  font-size: 12px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.no-data {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.goods-detail {
  display: flex;
  gap: 24px;
}

.detail-images {
  width: 300px;
  flex-shrink: 0;
}

.carousel-image {
  width: 100%;
  height: 100%;
}

.detail-info {
  flex: 1;
}

.detail-info h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #333;
}

.detail-info .price {
  font-size: 28px;
  font-weight: bold;
}

.detail-info p {
  margin: 8px 0;
  color: #666;
}

.description {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
  line-height: 1.8;
  max-height: 200px;
  overflow-y: auto;
}

.violation-info {
  margin-top: 16px;
  padding: 12px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  color: #ff4d4f;
}

.report-info {
  margin-top: 16px;
  padding: 12px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 4px;
  color: #d48806;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.original-price {
  color: #999;
  text-decoration: line-through;
  font-size: 14px;
}

.description-block {
  margin-top: 12px;
}

/* 判决对话框样式 */
.judge-content {
  text-align: center;
}

.judge-tip {
  font-size: 16px;
  color: #333;
  margin-bottom: 16px;
}

.judge-goods-info {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 8px;
  text-align: left;
  margin-bottom: 16px;
}

.judge-goods-info p {
  margin: 8px 0;
}

.judge-options {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-top: 20px;
}

.judge-option {
  flex: 1;
  max-width: 180px;
  padding: 24px 16px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.judge-option:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.judge-option.violation {
  color: #ff4d4f;
}

.judge-option.violation:hover {
  border-color: #ff4d4f;
  background: #fff2f0;
}

.judge-option.valid {
  color: #52c41a;
}

.judge-option.valid:hover {
  border-color: #52c41a;
  background: #f6ffed;
}

.judge-option span {
  font-size: 16px;
  font-weight: 500;
  margin-top: 8px;
}

.judge-option .option-desc {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  line-height: 1.4;
}
</style>

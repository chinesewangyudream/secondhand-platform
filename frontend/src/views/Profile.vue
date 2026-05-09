<!-- src/views/Profile.vue -->
<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="profile-header">
        <div class="avatar-section">
          <img :src="avatarPreview || user.avatar || defaultAvatar" :alt="user.nickname" class="avatar" />
          <div class="upload-overlay" @click="triggerFileSelect">
            <el-icon v-if="!uploadingAvatar" :size="24"><Camera /></el-icon>
            <el-icon v-else :size="24" class="is-loading"><Loading /></el-icon>
            <input
              ref="fileInput"
              type="file"
              accept="image/png,image/jpeg,image/gif,image/webp"
              @change="handleAvatarChange"
              style="display: none"
            />
          </div>
        </div>
        <div class="user-info">
          <h2>{{ user.nickname || user.username }}</h2>
          <p class="user-id">ID: {{ user.id }}</p>
          <p class="join-date">加入时间: {{ formatDate(user.createdAt) }}</p>
        </div>
      </div>

      <div class="profile-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-form
              :model="profileForm"
              :rules="profileRules"
              ref="profileFormRef"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="profileForm.nickname" placeholder="给自己取个名字" maxlength="20" show-word-limit />
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="选填，用于交易认证" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="用于接收通知" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="用于找回密码" maxlength="11" />
              </el-form-item>
              <el-form-item label="所在地" prop="address">
                <el-input v-model="profileForm.address" placeholder="你所在的城市" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="updating">
                  保存修改
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="账号安全" name="security">
            <div class="security-section">
              <div class="security-item">
                <div class="item-header">
                  <span>修改密码</span>
                  <el-button type="text" @click="showPasswordDialog = true">
                    修改
                  </el-button>
                </div>
                <p class="item-desc">定期修改密码可以保护账号安全</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="修改密码"
      width="400px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="changingPassword">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api/auth'
import { Camera, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect fill="%23667eea" width="100" height="100"/><text x="50" y="62" text-anchor="middle" fill="white" font-size="40">U</text></svg>')

const userStore = useUserStore()
const fileInput = ref()
const profileFormRef = ref()
const activeTab = ref('basic')
const updating = ref(false)
const uploadingAvatar = ref(false)
const avatarPreview = ref('')
const showPasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref()

const user = ref({
  id: '',
  username: '',
  nickname: '',
  avatar: '',
  createdAt: ''
})

const profileForm = reactive({
  nickname: '',
  realName: '',
  email: '',
  phone: '',
  address: ''
})

const profileRules = {
  nickname: [
    { max: 20, message: '昵称最多20个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^$|^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  await loadUserProfile()
})

async function loadUserProfile() {
  try {
    const res = await authApi.getProfile()
    user.value = res.data
    Object.assign(profileForm, {
      nickname: res.data.nickname || '',
      realName: res.data.realName || '',
      email: res.data.email || '',
      phone: res.data.phone || '',
      address: res.data.address || ''
    })
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

function triggerFileSelect() {
  fileInput.value?.click()
}

async function handleAvatarChange(event) {
  const file = event.target.files[0]
  if (!file) return

  const allowedTypes = ['image/png', 'image/jpeg', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('仅支持 PNG、JPG、GIF、WebP 格式的图片')
    return
  }

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }

  avatarPreview.value = URL.createObjectURL(file)
  uploadingAvatar.value = true

  try {
    const res = await authApi.uploadAvatar(file)
    const avatarUrl = res.data
    user.value.avatar = avatarUrl
    syncStoreAvatar(avatarUrl)
    ElMessage.success('头像更新成功')
  } catch (error) {
    ElMessage.error(error.showMessage || '头像上传失败')
    avatarPreview.value = ''
  } finally {
    uploadingAvatar.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function syncStoreAvatar(avatarUrl) {
  if (userStore.userInfo) {
    userStore.userInfo.avatar = avatarUrl
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
  }
}

function syncStoreProfile(data) {
  if (userStore.userInfo) {
    if (data.nickname) userStore.userInfo.nickname = data.nickname
    if (data.avatar) userStore.userInfo.avatar = data.avatar
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
  }
}

async function updateProfile() {
  if (!profileFormRef.value) return

  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return

    updating.value = true
    try {
      await authApi.updateProfile(profileForm)
      ElMessage.success('个人信息更新成功')
      await loadUserProfile()
      syncStoreProfile(profileForm)
    } catch (error) {
      ElMessage.error(error.showMessage || '更新失败')
    } finally {
      updating.value = false
    }
  })
}

function resetForm() {
  Object.assign(profileForm, {
    nickname: user.value.nickname || '',
    realName: user.value.realName || '',
    email: user.value.email || '',
    phone: user.value.phone || '',
    address: user.value.address || ''
  })
  profileFormRef.value?.clearValidate()
}

async function changePassword() {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      changingPassword.value = true
      try {
        await authApi.changePassword(passwordForm)
        ElMessage.success('密码修改成功')
        showPasswordDialog.value = false
        Object.assign(passwordForm, {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        })
      } catch (error) {
        ElMessage.error(error.showMessage || '密码修改失败')
      } finally {
        changingPassword.value = false
      }
    }
  })
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-section {
  position: relative;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
  background: rgba(255,255,255,0.2);
}

.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-section:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .is-loading {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.user-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
}

.user-id, .join-date {
  margin: 4px 0;
  opacity: 0.8;
  font-size: 14px;
}

.profile-content {
  padding: 30px;
}

.profile-form {
  max-width: 500px;
}

.security-section {
  max-width: 600px;
}

.security-item {
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-desc {
  color: #666;
  font-size: 14px;
  margin: 0;
}
</style>

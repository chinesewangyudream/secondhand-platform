<!-- src/views/Profile.vue -->
<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="profile-header">
        <div class="avatar-section">
          <img :src="user.avatar" :alt="user.name" class="avatar" />
          <div class="upload-overlay" @click="triggerFileSelect">
            <el-icon><Camera /></el-icon>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              @change="handleAvatarChange"
              style="display: none"
            />
          </div>
        </div>
        <div class="user-info">
          <h2>{{ user.name }}</h2>
          <p class="user-id">ID: {{ user.id }}</p>
          <p class="join-date">加入时间: {{ formatDate(user.createdAt) }}</p>
        </div>
      </div>

      <div class="profile-content">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="profileForm" label-width="100px" class="profile-form">
              <el-form-item label="昵称">
                <el-input v-model="profileForm.name" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.phone" />
              </el-form-item>
              <el-form-item label="所在地">
                <el-input v-model="profileForm.location" />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input
                  v-model="profileForm.bio"
                  type="textarea"
                  :rows="4"
                  placeholder="介绍一下自己..."
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateProfile" :loading="updating">
                  保存修改
                </el-button>
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
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const fileInput = ref()
const activeTab = ref('basic')
const updating = ref(false)
const showPasswordDialog = ref(false)
const changingPassword = ref(false)
const passwordFormRef = ref()

const user = ref({
  id: '',
  name: '',
  avatar: '',
  createdAt: ''
})

const profileForm = reactive({
  name: '',
  email: '',
  phone: '',
  location: '',
  bio: ''
})

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
    Object.assign(profileForm, res.data)
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

function triggerFileSelect() {
  fileInput.value?.click()
}

async function handleAvatarChange(event) {
  const file = event.target.files[0]
  if (file) {
    // 处理头像上传
    console.log('上传头像:', file)
  }
}

async function updateProfile() {
  updating.value = true
  try {
    await authApi.updateProfile(profileForm)
    ElMessage.success('个人信息更新成功')
    await loadUserProfile()
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    updating.value = false
  }
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
        ElMessage.error('密码修改失败')
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
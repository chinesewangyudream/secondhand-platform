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
          <p class="user-username">@{{ user.username }}</p>
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
              <!-- 绑定手机号 -->
              <div class="security-item">
                <div class="item-header">
                  <div class="item-left">
                    <el-icon class="item-icon"><Phone /></el-icon>
                    <span>手机号</span>
                  </div>
                  <div class="item-right">
                    <span class="item-value">{{ maskPhone(user.phone) }}</span>
                    <el-button type="text" @click="openPhoneDialog">{{ user.phone ? '更换' : '绑定' }}</el-button>
                  </div>
                </div>
                <p class="item-desc">绑定手机号可用于找回密码和接收通知</p>
              </div>

              <!-- 绑定邮箱 -->
              <div class="security-item">
                <div class="item-header">
                  <div class="item-left">
                    <el-icon class="item-icon"><Message /></el-icon>
                    <span>邮箱</span>
                  </div>
                  <div class="item-right">
                    <span class="item-value">{{ maskEmail(user.email) }}</span>
                    <el-button type="text" @click="openEmailDialog">{{ user.email ? '更换' : '绑定' }}</el-button>
                  </div>
                </div>
                <p class="item-desc">绑定邮箱可用于找回密码和接收重要通知</p>
              </div>

              <!-- 修改密码 -->
              <div class="security-item">
                <div class="item-header">
                  <div class="item-left">
                    <el-icon class="item-icon"><Lock /></el-icon>
                    <span>登录密码</span>
                  </div>
                  <div class="item-right">
                    <span class="item-value">已设置</span>
                    <el-button type="text" @click="showPasswordDialog = true">修改</el-button>
                  </div>
                </div>
                <p class="item-desc">定期修改密码可以保护账号安全</p>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420px" @close="resetPasswordForm">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword" :loading="changingPassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 换绑手机号对话框 -->
    <el-dialog v-model="showPhoneDialog" title="换绑手机号" width="420px" @close="resetPhoneForm">
      <el-form :model="phoneForm" :rules="phoneFormRules" ref="phoneFormRef" label-width="100px">
        <el-form-item label="当前手机号">
          <span class="current-value">{{ maskPhone(user.phone) || '未绑定' }}</span>
        </el-form-item>
        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="phoneForm.password" type="password" show-password placeholder="请输入登录密码以验证身份" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPhoneDialog = false">取消</el-button>
        <el-button type="primary" @click="changePhone" :loading="changingPhone">确定</el-button>
      </template>
    </el-dialog>

    <!-- 换绑邮箱对话框 -->
    <el-dialog v-model="showEmailDialog" title="换绑邮箱" width="420px" @close="resetEmailForm">
      <el-form :model="emailForm" :rules="emailFormRules" ref="emailFormRef" label-width="100px">
        <el-form-item label="当前邮箱">
          <span class="current-value">{{ maskEmail(user.email) || '未绑定' }}</span>
        </el-form-item>
        <el-form-item label="新邮箱" prop="newEmail">
          <el-input v-model="emailForm.newEmail" placeholder="请输入新邮箱地址" />
        </el-form-item>
        <el-form-item label="登录密码" prop="password">
          <el-input v-model="emailForm.password" type="password" show-password placeholder="请输入登录密码以验证身份" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEmailDialog = false">取消</el-button>
        <el-button type="primary" @click="changeEmail" :loading="changingEmail">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api/auth'
import { Camera, Loading, Phone, Message, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const defaultAvatar = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect fill="%23667eea" width="100" height="100"/><text x="50" y="62" text-anchor="middle" fill="white" font-size="40">U</text></svg>')

const userStore = useUserStore()
const fileInput = ref()
const profileFormRef = ref()
const passwordFormRef = ref()
const phoneFormRef = ref()
const emailFormRef = ref()

const activeTab = ref('basic')
const updating = ref(false)
const uploadingAvatar = ref(false)
const avatarPreview = ref('')
const showPasswordDialog = ref(false)
const changingPassword = ref(false)
const showPhoneDialog = ref(false)
const changingPhone = ref(false)
const showEmailDialog = ref(false)
const changingEmail = ref(false)

const user = ref({
  id: '',
  username: '',
  nickname: '',
  avatar: '',
  phone: '',
  email: '',
  createdAt: ''
})

const profileForm = reactive({
  nickname: '',
  realName: '',
  address: ''
})

const profileRules = {
  nickname: [
    { max: 20, message: '昵称最多20个字符', trigger: 'blur' }
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

const phoneForm = reactive({
  newPhone: '',
  password: ''
})

const phoneFormRules = {
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
  ]
}

const emailForm = reactive({
  newEmail: '',
  password: ''
})

const emailFormRules = {
  newEmail: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入登录密码', trigger: 'blur' }
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
    syncStore({ avatar: avatarUrl })
    ElMessage.success('头像更新成功')
  } catch (error) {
    ElMessage.error(error.showMessage || '头像上传失败')
    avatarPreview.value = ''
  } finally {
    uploadingAvatar.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

function syncStore(data) {
  if (userStore.userInfo) {
    if (data.nickname !== undefined) userStore.userInfo.nickname = data.nickname
    if (data.avatar !== undefined) userStore.userInfo.avatar = data.avatar
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
      syncStore({ nickname: profileForm.nickname })
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
    address: user.value.address || ''
  })
  profileFormRef.value?.clearValidate()
}

function openPhoneDialog() {
  Object.assign(phoneForm, { newPhone: '', password: '' })
  phoneFormRef.value?.clearValidate()
  showPhoneDialog.value = true
}

function openEmailDialog() {
  Object.assign(emailForm, { newEmail: '', password: '' })
  emailFormRef.value?.clearValidate()
  showEmailDialog.value = true
}

function resetPasswordForm() {
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  passwordFormRef.value?.clearValidate()
}

function resetPhoneForm() {
  Object.assign(phoneForm, { newPhone: '', password: '' })
  phoneFormRef.value?.clearValidate()
}

function resetEmailForm() {
  Object.assign(emailForm, { newEmail: '', password: '' })
  emailFormRef.value?.clearValidate()
}

async function changePassword() {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    changingPassword.value = true
    try {
      await authApi.changePassword(passwordForm)
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
    } catch (error) {
      ElMessage.error(error.showMessage || '密码修改失败')
    } finally {
      changingPassword.value = false
    }
  })
}

async function changePhone() {
  if (!phoneFormRef.value) return

  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return

    changingPhone.value = true
    try {
      await authApi.changePhone({ password: phoneForm.password, newPhone: phoneForm.newPhone })
      ElMessage.success('手机号换绑成功')
      showPhoneDialog.value = false
      await loadUserProfile()
    } catch (error) {
      ElMessage.error(error.showMessage || '换绑失败')
    } finally {
      changingPhone.value = false
    }
  })
}

async function changeEmail() {
  if (!emailFormRef.value) return

  await emailFormRef.value.validate(async (valid) => {
    if (!valid) return

    changingEmail.value = true
    try {
      await authApi.changeEmail({ password: emailForm.password, newEmail: emailForm.newEmail })
      ElMessage.success('邮箱换绑成功')
      showEmailDialog.value = false
      await loadUserProfile()
    } catch (error) {
      ElMessage.error(error.showMessage || '换绑失败')
    } finally {
      changingEmail.value = false
    }
  })
}

function maskPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

function maskEmail(email) {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (!domain) return email
  const visible = name.length <= 2 ? name[0] : name.slice(0, 2)
  return visible + '***@' + domain
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
  margin: 0 0 4px 0;
  font-size: 24px;
}

.user-username {
  margin: 0 0 4px 0;
  opacity: 0.7;
  font-size: 14px;
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

.security-item:last-child {
  border-bottom: none;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.item-icon {
  color: #667eea;
  font-size: 18px;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-value {
  color: #999;
  font-size: 14px;
}

.item-desc {
  color: #999;
  font-size: 13px;
  margin: 0;
  padding-left: 26px;
}

.current-value {
  color: #666;
  font-size: 14px;
}
</style>

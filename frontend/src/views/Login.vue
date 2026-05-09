<!-- src/views/Login.vue -->
<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h2>登录闲置宝</h2>
        <p>发现更多好物，开启二手之旅</p>
      </div>

      <el-form
        :model="loginForm"
        :rules="loginRules"
        ref="loginFormRef"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" @click="router.push('/forgot-password')">
              忘记密码？
            </el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleLogin"
            :loading="loggingIn"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="router.push('/register')">
          立即注册
        </el-link>
      </div>

      <!-- 第三方登录 -->
      <div class="third-party-login">
        <div class="divider">
          <span>或</span>
        </div>
        <div class="third-party-buttons">
          <el-button type="outline" size="large" @click="loginWithWechat">
            <el-icon><ChatDotRound /></el-icon>
            微信登录
          </el-button>
          <el-button type="outline" size="large" @click="loginWithQQ">
            <el-icon><ChatDotRound /></el-icon>
            QQ登录
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { authApi } from '@/api/auth'
import { User, Lock, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const loggingIn = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loggingIn.value = true
      try {
        await userStore.login(loginForm)

        ElMessage.success('登录成功')

        // 跳转到之前的页面或首页
        const redirect = router.currentRoute.value.query.redirect || '/home'
        router.push(redirect)
      } catch (error) {
        ElMessage.error(error.showMessage || error.message || '登录失败，请稍后重试')
      } finally {
        loggingIn.value = false
      }
    }
  })
}

function loginWithWechat() {
  // 微信登录逻辑
  ElMessage.info('微信登录功能开发中')
}

function loginWithQQ() {
  // QQ登录逻辑
  ElMessage.info('QQ登录功能开发中')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
}

.login-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.login-footer {
  text-align: center;
  margin-bottom: 30px;
  color: #666;
}

.login-footer .el-link {
  margin-left: 8px;
}

.third-party-login {
  text-align: center;
}

.divider {
  position: relative;
  margin: 20px 0;
  color: #999;
  font-size: 14px;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e6e6e6;
}

.divider span {
  background: white;
  padding: 0 16px;
  position: relative;
  z-index: 1;
}

.third-party-buttons {
  display: flex;
  gap: 12px;
}

.third-party-buttons .el-button {
  flex: 1;
}
</style>

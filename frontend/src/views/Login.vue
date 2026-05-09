<!-- src/views/Login.vue -->
<template>
  <div class="login-page">
    <!-- Decorative Background -->
    <div class="bg-decoration">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>

    <!-- Login Card -->
    <div class="login-card">
      <!-- Left Side - Branding -->
      <div class="card-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M24 4L6 12v24l18 8 18-8V12L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M24 20l-12-6v12l12 6 12-6v-12l-12 6z" fill="currentColor" opacity="0.3"/>
              <circle cx="24" cy="24" r="4" fill="currentColor"/>
            </svg>
          </div>
          <h1 class="brand-title">闲置宝</h1>
          <p class="brand-tagline">让闲置流转，让生活更美好</p>

          <div class="brand-features">
            <div class="feature">
              <span class="feature-icon">🌱</span>
              <span class="feature-text">绿色环保</span>
            </div>
            <div class="feature">
              <span class="feature-icon">💎</span>
              <span class="feature-text">品质保证</span>
            </div>
            <div class="feature">
              <span class="feature-icon">🔒</span>
              <span class="feature-text">安全交易</span>
            </div>
          </div>
        </div>

        <div class="brand-illustration">
          <div class="floating-item item-1">👗</div>
          <div class="floating-item item-2">📱</div>
          <div class="floating-item item-3">📚</div>
          <div class="floating-item item-4">🎸</div>
        </div>
      </div>

      <!-- Right Side - Form -->
      <div class="card-form">
        <div class="form-header">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-subtitle">登录您的账户，继续探索好物</p>
        </div>

        <el-form
          :model="loginForm"
          :rules="loginRules"
          ref="loginFormRef"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <div class="input-group">
              <label class="input-label">用户名 / 邮箱</label>
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名或邮箱"
                size="large"
                :prefix-icon="User"
              />
            </div>
          </el-form-item>

          <el-form-item prop="password">
            <div class="input-group">
              <label class="input-label">密码</label>
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </div>
          </el-form-item>

          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe" class="custom-checkbox" />
              <span class="checkbox-text">记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button
            type="submit"
            class="btn btn-primary btn-block"
            :class="{ 'btn-loading': loggingIn }"
            :disabled="loggingIn"
            @click.prevent="handleLogin"
          >
            <span v-if="!loggingIn">登录</span>
            <span v-else>登录中...</span>
          </button>
        </el-form>

        <div class="divider">
          <span>或使用以下方式登录</span>
        </div>

        <div class="social-buttons">
          <button class="social-btn social-btn-wechat" @click="loginWithWechat">
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
              <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.006-.27-.022-.405-.032zm-2.404 3.126c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
            </svg>
            <span>微信登录</span>
          </button>
          <button class="social-btn social-btn-qq" @click="loginWithQQ">
            <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
              <path d="M12.003 2c-2.265 0-6.29 1.364-6.29 7.325v1.195S3.55 14.96 3.55 17.474c0 .665.17 1.025.281 1.025.114 0 .902-.484 1.748-2.072 0 0-.18 2.197 1.904 3.967 0 0-1.77.495-1.77 1.182 0 .686 4.078.43 6.29.43 2.213 0 6.29.256 6.29-.43 0-.687-1.77-1.182-1.77-1.182 2.085-1.77 1.904-3.967 1.904-3.967.846 1.588 1.634 2.072 1.748 2.072.111 0 .281-.36.281-1.025 0-2.514-2.166-6.954-2.166-6.954V9.325C18.29 3.364 14.268 2 12.003 2z"/>
            </svg>
            <span>QQ登录</span>
          </button>
        </div>

        <div class="form-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="link-primary">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { User, Lock } from '@element-plus/icons-vue'
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
  ElMessage.info('微信登录功能开发中')
}

function loginWithQQ() {
  ElMessage.info('QQ登录功能开发中')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  background: var(--color-bg-secondary);
  position: relative;
  overflow: hidden;
}

/* Background Decoration */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.bg-shape-1 {
  width: 500px;
  height: 500px;
  background: var(--color-primary-soft);
  top: -150px;
  right: -100px;
}

.bg-shape-2 {
  width: 400px;
  height: 400px;
  background: rgba(184, 134, 11, 0.15);
  bottom: -100px;
  left: -100px;
}

.bg-shape-3 {
  width: 300px;
  height: 300px;
  background: rgba(45, 90, 90, 0.1);
  top: 40%;
  left: 30%;
}

/* Login Card */
.login-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 900px;
  width: 100%;
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* Brand Side */
.card-brand {
  padding: var(--space-3xl);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
}

.brand-logo {
  width: 64px;
  height: 64px;
  margin-bottom: var(--space-lg);
}

.brand-title {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  margin: 0 0 var(--space-sm);
}

.brand-tagline {
  font-size: var(--text-base);
  opacity: 0.9;
  margin: 0 0 var(--space-2xl);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.feature {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.feature-icon {
  font-size: var(--text-xl);
}

.feature-text {
  font-size: var(--text-sm);
  opacity: 0.9;
}

/* Floating Illustration */
.brand-illustration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.floating-item {
  position: absolute;
  font-size: 48px;
  opacity: 0.2;
  animation: float 6s ease-in-out infinite;
}

.item-1 { top: 20%; right: 20%; animation-delay: 0s; }
.item-2 { top: 60%; right: 10%; animation-delay: 1.5s; }
.item-3 { bottom: 30%; right: 30%; animation-delay: 3s; }
.item-4 { bottom: 10%; right: 25%; animation-delay: 4.5s; }

/* Form Side */
.card-form {
  padding: var(--space-3xl);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: var(--space-2xl);
}

.form-title {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 var(--space-xs);
}

.form-subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.input-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
}

.custom-checkbox {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.checkbox-text {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.forgot-link {
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
}

.forgot-link:hover {
  text-decoration: underline;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  padding: var(--space-md) var(--space-xl);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all var(--transition-base);
}

.btn-primary {
  background: var(--color-primary);
  color: white;
  box-shadow: 0 4px 16px rgba(196, 92, 62, 0.3);
}

.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(196, 92, 62, 0.4);
}

.btn-block {
  width: 100%;
}

.btn-loading {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Divider */
.divider {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin: var(--space-lg) 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.divider span {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  white-space: nowrap;
}

/* Social Buttons */
.social-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-md);
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}

.social-btn:hover {
  border-color: var(--color-primary);
  background: var(--color-primary-soft);
}

.social-btn-wechat:hover {
  border-color: #07c160;
  background: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.social-btn-qq:hover {
  border-color: #12b7f5;
  background: rgba(18, 183, 245, 0.1);
  color: #12b7f5;
}

/* Footer */
.form-footer {
  margin-top: var(--space-xl);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.link-primary {
  color: var(--color-primary);
  font-weight: 500;
  text-decoration: none;
  margin-left: var(--space-xs);
}

.link-primary:hover {
  text-decoration: underline;
}

/* Animations */
@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

/* Responsive */
@media (max-width: 768px) {
  .login-card {
    grid-template-columns: 1fr;
  }

  .card-brand {
    display: none;
  }

  .card-form {
    padding: var(--space-xl);
  }
}
</style>

<!-- src/views/Register.vue -->
<template>
  <div class="register-page">
    <!-- Decorative Background -->
    <div class="bg-decoration">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
    </div>

    <!-- Register Card -->
    <div class="register-card">
      <!-- Header -->
      <div class="card-header">
        <router-link to="/home" class="logo">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M24 4L6 12v24l18 8 18-8V12L24 4z" stroke="currentColor" stroke-width="2" fill="none"/>
            <path d="M24 20l-12-6v12l12 6 12-6v-12l-12 6z" fill="currentColor" opacity="0.3"/>
            <circle cx="24" cy="24" r="4" fill="currentColor"/>
          </svg>
        </router-link>
        <h1 class="title">创建账户</h1>
        <p class="subtitle">加入闲置宝，开启绿色循环生活</p>
      </div>

      <!-- Progress Steps -->
      <div class="progress-steps">
        <div class="step" :class="{ active: step >= 1, completed: step > 1 }">
          <div class="step-dot"></div>
          <span class="step-label">基本信息</span>
        </div>
        <div class="step-line" :class="{ active: step > 1 }"></div>
        <div class="step" :class="{ active: step >= 2 }">
          <div class="step-dot"></div>
          <span class="step-label">设置密码</span>
        </div>
      </div>

      <!-- Form -->
      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        class="register-form"
      >
        <!-- Step 1: Basic Info -->
        <div v-show="step === 1" class="form-step">
          <div class="input-row">
            <el-form-item prop="username" class="input-item">
              <div class="input-group">
                <label class="input-label">用户名</label>
                <el-input
                  v-model="registerForm.username"
                  placeholder="3-20个字符"
                  size="large"
                  :prefix-icon="User"
                />
              </div>
            </el-form-item>
          </div>

          <div class="input-row">
            <el-form-item prop="email" class="input-item">
              <div class="input-group">
                <label class="input-label">邮箱</label>
                <el-input
                  v-model="registerForm.email"
                  placeholder="example@email.com"
                  size="large"
                  :prefix-icon="Message"
                />
              </div>
            </el-form-item>
          </div>

          <div class="input-row">
            <el-form-item prop="phone" class="input-item">
              <div class="input-group">
                <label class="input-label">手机号</label>
                <el-input
                  v-model="registerForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  :prefix-icon="Phone"
                />
              </div>
            </el-form-item>
          </div>

          <button type="button" class="btn btn-primary btn-block" @click="nextStep">
            下一步
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>

        <!-- Step 2: Password -->
        <div v-show="step === 2" class="form-step">
          <div class="input-row">
            <el-form-item prop="password" class="input-item">
              <div class="input-group">
                <label class="input-label">密码</label>
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="至少6位字符"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
                <div class="password-strength">
                  <div class="strength-bar">
                    <div
                      class="strength-fill"
                      :class="passwordStrength"
                      :style="{ width: passwordStrengthWidth }"
                    ></div>
                  </div>
                  <span class="strength-text">{{ passwordStrengthText }}</span>
                </div>
              </div>
            </el-form-item>
          </div>

          <div class="input-row">
            <el-form-item prop="confirmPassword" class="input-item">
              <div class="input-group">
                <label class="input-label">确认密码</label>
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  size="large"
                  :prefix-icon="Lock"
                  show-password
                />
              </div>
            </el-form-item>
          </div>

          <el-form-item prop="agreeToTerms" class="terms-item">
            <label class="checkbox-label">
              <input type="checkbox" v-model="registerForm.agreeToTerms" class="custom-checkbox" />
              <span class="checkbox-text">
                我已阅读并同意
                <a href="#" class="link-primary" @click.prevent="showTerms">《用户协议》</a>
                和
                <a href="#" class="link-primary" @click.prevent="showPrivacy">《隐私政策》</a>
              </span>
            </label>
          </el-form-item>

          <div class="button-group">
            <button type="button" class="btn btn-outline" @click="prevStep">
              <el-icon><ArrowLeft /></el-icon>
              上一步
            </button>
            <button
              type="button"
              class="btn btn-primary"
              :class="{ 'btn-loading': registering }"
              :disabled="registering"
              @click="handleRegister"
            >
              <span v-if="!registering">注册</span>
              <span v-else>注册中...</span>
            </button>
          </div>
        </div>
      </el-form>

      <!-- Divider -->
      <div class="divider">
        <span>或</span>
      </div>

      <!-- Social Register -->
      <div class="social-buttons">
        <button class="social-btn social-btn-wechat" @click="registerWithWechat">
          <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
            <path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.047c.134 0 .24-.111.24-.247 0-.06-.023-.12-.038-.177l-.327-1.233a.582.582 0 0 1-.023-.156.49.49 0 0 1 .201-.398C23.024 18.48 24 16.82 24 14.98c0-3.21-2.931-5.837-6.656-6.088V8.89c-.135-.006-.27-.022-.405-.032zm-2.404 3.126c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.969.983.976.976 0 0 1-.969-.983c0-.542.434-.982.969-.982z"/>
          </svg>
          <span>微信注册</span>
        </button>
        <button class="social-btn social-btn-qq" @click="registerWithQQ">
          <svg viewBox="0 0 24 24" fill="currentColor" width="20" height="20">
            <path d="M12.003 2c-2.265 0-6.29 1.364-6.29 7.325v1.195S3.55 14.96 3.55 17.474c0 .665.17 1.025.281 1.025.114 0 .902-.484 1.748-2.072 0 0-.18 2.197 1.904 3.967 0 0-1.77.495-1.77 1.182 0 .686 4.078.43 6.29.43 2.213 0 6.29.256 6.29-.43 0-.687-1.77-1.182-1.77-1.182 2.085-1.77 1.904-3.967 1.904-3.967.846 1.588 1.634 2.072 1.748 2.072.111 0 .281-.36.281-1.025 0-2.514-2.166-6.954-2.166-6.954V9.325C18.29 3.364 14.268 2 12.003 2z"/>
          </svg>
          <span>QQ注册</span>
        </button>
      </div>

      <!-- Footer -->
      <div class="card-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="link-primary">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { User, Message, Phone, Lock, ArrowRight, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref()
const registering = ref(false)
const step = ref(1)

const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  agreeToTerms: false
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// Password strength
const passwordStrength = computed(() => {
  const pwd = registerForm.password
  if (!pwd) return ''
  if (pwd.length < 6) return 'weak'
  if (pwd.length < 8) return 'medium'
  if (/[A-Z]/.test(pwd) && /[0-9]/.test(pwd) && /[^A-Za-z0-9]/.test(pwd)) return 'strong'
  return 'medium'
})

const passwordStrengthWidth = computed(() => {
  switch (passwordStrength.value) {
    case 'weak': return '33%'
    case 'medium': return '66%'
    case 'strong': return '100%'
    default: return '0%'
  }
})

const passwordStrengthText = computed(() => {
  switch (passwordStrength.value) {
    case 'weak': return '弱'
    case 'medium': return '中等'
    case 'strong': return '强'
    default: return ''
  }
})

function nextStep() {
  registerFormRef.value.validateField(['username', 'email', 'phone'], (valid) => {
    if (valid) {
      step.value = 2
    }
  })
}

function prevStep() {
  step.value = 1
}

async function handleRegister() {
  if (!registerForm.value) return

  if (!registerForm.agreeToTerms) {
    ElMessage.warning('请同意用户协议和隐私政策')
    return
  }

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      registering.value = true
      try {
        const { confirmPassword, agreeToTerms, ...registerData } = registerForm
        await authApi.register(registerData)
        ElMessage.success('注册成功，即将跳转到登录页面')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.showMessage || error.message || '注册失败，请稍后重试')
      } finally {
        registering.value = false
      }
    }
  })
}

function showTerms() {
  ElMessage.info('用户协议功能开发中')
}

function showPrivacy() {
  ElMessage.info('隐私政策功能开发中')
}

function registerWithWechat() {
  ElMessage.info('微信注册功能开发中')
}

function registerWithQQ() {
  ElMessage.info('QQ注册功能开发中')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-xl);
  background: var(--color-bg-secondary);
  position: relative;
  overflow: hidden;
}

/* Background */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.3;
}

.bg-shape-1 {
  width: 600px;
  height: 600px;
  background: var(--color-primary-soft);
  top: -200px;
  left: -100px;
}

.bg-shape-2 {
  width: 500px;
  height: 500px;
  background: rgba(184, 134, 11, 0.15);
  bottom: -150px;
  right: -100px;
}

/* Register Card */
.register-card {
  width: 100%;
  max-width: 480px;
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  padding: var(--space-2xl);
  position: relative;
  z-index: 1;
}

/* Header */
.card-header {
  text-align: center;
  margin-bottom: var(--space-xl);
}

.logo {
  display: inline-block;
  width: 48px;
  height: 48px;
  color: var(--color-primary);
  margin-bottom: var(--space-md);
}

.title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 var(--space-xs);
}

.subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin: 0;
}

/* Progress Steps */
.progress-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-xl);
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-full);
  background: var(--color-bg-tertiary);
  border: 2px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-base);
}

.step.active .step-dot {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.step.completed .step-dot {
  background: var(--color-success);
  border-color: var(--color-success);
}

.step-label {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.step.active .step-label {
  color: var(--color-primary);
  font-weight: 500;
}

.step-line {
  width: 60px;
  height: 2px;
  background: var(--color-border);
  margin: 0 var(--space-sm);
  margin-bottom: var(--space-lg);
  transition: background var(--transition-base);
}

.step-line.active {
  background: var(--color-primary);
}

/* Form */
.register-form {
  margin-bottom: var(--space-lg);
}

.form-step {
  animation: fadeIn 0.3s ease-out;
}

.input-row {
  margin-bottom: var(--space-md);
}

.input-item {
  margin-bottom: 0;
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

/* Password Strength */
.password-strength {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-top: var(--space-xs);
}

.strength-bar {
  flex: 1;
  height: 4px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: all var(--transition-base);
  border-radius: var(--radius-full);
}

.strength-fill.weak { background: var(--color-danger); }
.strength-fill.medium { background: var(--color-warning); }
.strength-fill.strong { background: var(--color-success); }

.strength-text {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  min-width: 32px;
}

/* Terms */
.terms-item {
  margin-bottom: var(--space-lg);
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: var(--space-sm);
  cursor: pointer;
}

.custom-checkbox {
  width: 16px;
  height: 16px;
  margin-top: 2px;
  accent-color: var(--color-primary);
}

.checkbox-text {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.link-primary {
  color: var(--color-primary);
  text-decoration: none;
}

.link-primary:hover {
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

.btn-outline {
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.btn-outline:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-block {
  width: 100%;
}

.btn-loading {
  opacity: 0.7;
  cursor: not-allowed;
}

.button-group {
  display: flex;
  gap: var(--space-md);
}

.button-group .btn {
  flex: 1;
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
.card-footer {
  margin-top: var(--space-xl);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.card-footer .link-primary {
  margin-left: var(--space-xs);
  font-weight: 500;
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 480px) {
  .register-card {
    padding: var(--space-lg);
  }

  .social-buttons {
    grid-template-columns: 1fr;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>

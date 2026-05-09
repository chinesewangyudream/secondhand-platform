<!-- src/views/Register.vue -->
<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h2>注册闲置宝</h2>
        <p>加入我们，发现更多好物</p>
      </div>

      <el-form
        :model="registerForm"
        :rules="registerRules"
        ref="registerFormRef"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="agreeToTerms">
          <el-checkbox v-model="registerForm.agreeToTerms">
            我已阅读并同意
            <el-link type="primary" @click="showTerms">《用户协议》</el-link>
            和
            <el-link type="primary" @click="showPrivacy">《隐私政策》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            @click="handleRegister"
            :loading="registering"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" @click="router.push('/login')">
          立即登录
        </el-link>
      </div>

      <!-- 第三方注册 -->
      <div class="third-party-register">
        <div class="divider">
          <span>或</span>
        </div>
        <div class="third-party-buttons">
          <el-button type="outline" size="large" @click="registerWithWechat">
            <el-icon><ChatDotRound /></el-icon>
            微信注册
          </el-button>
          <el-button type="outline" size="large" @click="registerWithQQ">
            <el-icon><ChatDotRound /></el-icon>
            QQ注册
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'
import { User, Message, Phone, Lock, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const registerFormRef = ref()
const registering = ref(false)

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
  ],
  agreeToTerms: [
    { required: true, message: '请同意用户协议和隐私政策', trigger: 'change' }
  ]
}

async function handleRegister() {
  if (!registerFormRef.value) return

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
  // 显示用户协议
  ElMessage.info('用户协议功能开发中')
}

function showPrivacy() {
  // 显示隐私政策
  ElMessage.info('隐私政策功能开发中')
}

function registerWithWechat() {
  // 微信注册逻辑
  ElMessage.info('微信注册功能开发中')
}

function registerWithQQ() {
  // QQ注册逻辑
  ElMessage.info('QQ注册功能开发中')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 24px;
}

.register-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.register-footer {
  text-align: center;
  margin-bottom: 30px;
  color: #666;
}

.register-footer .el-link {
  margin-left: 8px;
}

.third-party-register {
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
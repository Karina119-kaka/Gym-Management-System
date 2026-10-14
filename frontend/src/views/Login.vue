<template>
  <main class="login-page">
    <section class="brand-panel">
      <div class="brand-glow brand-glow-a"></div>
      <div class="brand-glow brand-glow-b"></div>
      <div class="brand-top">
        <div class="brand-mark">GYM</div>
        <span>GYM MANAGEMENT</span>
      </div>

      <div class="brand-stage">
        <div class="brand-copy">
          <p class="eyebrow">Performance Center</p>
          <h1>让每一次训练<br /><strong>都更有价值。</strong></h1>
          <p class="subtitle">会员训练、课程预约与场馆运营，一站式高效管理。</p>
        </div>

        <AnimatedCharacters
          class="login-characters"
          :is-typing="isTyping"
          :show-password="showPassword"
          :password-length="form.password.length"
          :has-error="hasError"
        />
      </div>

      <div class="brand-stats">
        <span><b>24/7</b><small>全天候管理</small></span>
        <span><b>3</b><small>角色协同</small></span>
        <span><b>100%</b><small>数据可追踪</small></span>
      </div>
    </section>

    <section class="login-card">
      <div class="card-header">
        <p class="eyebrow">Sign in</p>
        <h2>{{ currentRoleLabel }}登录</h2>
      </div>

      <div class="login-tabs">
        <button
          v-for="role in roles"
          :key="role.value"
          type="button"
          :class="{ active: currentRole === role.value }"
          @click="switchRole(role.value)"
        >
          {{ role.label }}
        </button>
      </div>

      <form class="login-form" @submit.prevent="login">
        <div class="form-group">
          <label>{{ currentRoleLabel }}账号</label>
          <input
            v-model.trim="form.username"
            type="text"
            autocomplete="username"
            @focus="isTyping = true"
            @blur="isTyping = false"
          />
        </div>
        <div class="form-group">
          <label>密码</label>
          <div class="password-field">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              @focus="isTyping = true"
              @blur="isTyping = false"
            />
            <button
              type="button"
              class="password-toggle"
              :aria-label="showPassword ? '隐藏密码' : '显示密码'"
              @click="showPassword = !showPassword"
            >
              <el-icon :size="18">
                <Hide v-if="showPassword" />
                <View v-else />
              </el-icon>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label>验证码</label>
          <div class="verification-code">
            <input
              v-model.trim="form.code"
              type="text"
              autocomplete="off"
              @focus="isTyping = true"
              @blur="isTyping = false"
            />
            <img :src="captchaUrl" @click="refreshCaptcha" alt="验证码" title="点击刷新验证码" />
          </div>
        </div>

        <div class="form-options">
          <label class="remember-option">
            <input v-model="rememberPassword" type="checkbox" />
            <span>记住密码</span>
          </label>
          <a href="#" @click.prevent>忘记密码？</a>
        </div>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <button class="login-btn" type="submit" :disabled="submitting">
          {{ submitting ? '登录中...' : '进入系统' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script>
import { View, Hide } from '@element-plus/icons-vue'
import AnimatedCharacters from '../components/AnimatedCharacters.vue'
import { postForm, pickData } from '../utils/api'

const REMEMBER_KEY = 'gymLoginRemember'

export default {
  name: 'Login',
  components: { AnimatedCharacters, View, Hide },
  data() {
    const savedRole = sessionStorage.getItem('loginRole')
    const defaultRole = ['member', 'coach', 'admin'].includes(savedRole) ? savedRole : 'member'
    return {
      currentRole: defaultRole,
      roles: [
        { label: '会员', value: 'member' },
        { label: '教练', value: 'coach' },
        { label: '管理员', value: 'admin' }
      ],
      form: { username: '', password: '', code: '' },
      captchaUrl: '/api/verifyCode',
      errorMessage: '',
      submitting: false,
      showPassword: false,
      rememberPassword: false,
      isTyping: false,
      hasError: false,
      errorTimer: null
    }
  },
  computed: {
    currentRoleLabel() {
      return { member: '会员', coach: '教练', admin: '管理员' }[this.currentRole] || '用户'
    }
  },
  methods: {
    switchRole(role) {
      if (this.submitting) return
      this.currentRole = role
      sessionStorage.setItem('loginRole', role)
      this.form = { username: '', password: '', code: '' }
      this.errorMessage = ''
      this.isTyping = false
      this.clearErrorState()
      this.loadRememberedCredentials(role)
      this.refreshCaptcha()
    },
    refreshCaptcha() {
      this.captchaUrl = `/api/verifyCode?timestamp=${Date.now()}`
    },
    clearErrorState() {
      if (this.errorTimer) {
        clearTimeout(this.errorTimer)
        this.errorTimer = null
      }
      this.hasError = false
    },
    triggerErrorAnimation() {
      this.clearErrorState()
      this.hasError = true
      this.errorTimer = setTimeout(() => {
        this.hasError = false
        this.errorTimer = null
      }, 1600)
    },
    loadRememberedCredentials(role = this.currentRole) {
      try {
        const raw = localStorage.getItem(REMEMBER_KEY)
        if (!raw) {
          this.rememberPassword = false
          return
        }
        const saved = JSON.parse(raw)
        if (!saved || saved.role !== role) {
          this.rememberPassword = false
          return
        }
        this.rememberPassword = true
        this.form.username = saved.username || ''
        this.form.password = saved.password || ''
      } catch (error) {
        this.rememberPassword = false
      }
    },
    persistRememberedCredentials() {
      if (this.rememberPassword) {
        localStorage.setItem(REMEMBER_KEY, JSON.stringify({
          role: this.currentRole,
          username: this.form.username,
          password: this.form.password
        }))
      } else {
        localStorage.removeItem(REMEMBER_KEY)
      }
    },
    async login() {
      if (this.submitting) return
      this.errorMessage = ''
      this.clearErrorState()
      sessionStorage.setItem('loginRole', this.currentRole)

      if (!this.form.username || !this.form.password || !this.form.code) {
        this.errorMessage = '请完整输入账号、密码和验证码'
        this.triggerErrorAnimation()
        return
      }

      const accountKeyMap = { member: 'memberNo', coach: 'coachNo', admin: 'adminNo' }
      const passwordKeyMap = { member: 'memberPwd', coach: 'coachPwd', admin: 'adminPwd' }

      this.submitting = true
      try {
        const result = await postForm(`/api/login/${this.currentRole}`, {
          [accountKeyMap[this.currentRole]]: this.form.username,
          [passwordKeyMap[this.currentRole]]: this.form.password,
          code: this.form.code
        })

        this.persistRememberedCredentials()
        localStorage.setItem('role', this.currentRole)
        localStorage.setItem('user', JSON.stringify(pickData(result) || {}))

        const target = { member: '/member', coach: '/coach', admin: '/admin' }[this.currentRole] || '/login'
        await this.$router.push(target)
      } catch (error) {
        this.errorMessage = error.message || '登录失败，请稍后重试'
        this.triggerErrorAnimation()
        this.refreshCaptcha()
      } finally {
        this.submitting = false
      }
    }
  },
  mounted() {
    if (window.location.hostname === '127.0.0.1') {
      const target = `${window.location.protocol}//localhost:${window.location.port}${window.location.pathname}${window.location.search}${window.location.hash}`
      window.location.replace(target)
      return
    }
    this.loadRememberedCredentials()
    this.refreshCaptcha()
  },
  beforeUnmount() {
    this.clearErrorState()
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(360px, 1fr) minmax(390px, 520px);
  background: #f8fafc;
  color: #17202b;
  overflow: hidden;
}

.brand-panel {
  position: relative;
  min-height: 100vh;
  padding: 52px clamp(36px, 7vw, 96px) 36px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(145deg, #dfe3e8 0%, #9ba3ad 100%);
}

.brand-panel::after {
  content: '';
  position: absolute;
  right: -100px;
  bottom: -150px;
  width: 520px;
  height: 520px;
  border: 1px solid rgba(255, 255, 255, .45);
  border-radius: 50%;
  box-shadow: 0 0 0 42px rgba(255, 255, 255, .12), 0 0 0 84px rgba(255, 255, 255, .08);
  animation: orbit-pulse 8s ease-in-out infinite;
}

.brand-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
}

.brand-glow-a {
  top: 18%;
  right: 18%;
  width: 220px;
  height: 220px;
  background: rgba(255, 255, 255, .28);
  animation: float-a 7s ease-in-out infinite;
}

.brand-glow-b {
  bottom: 22%;
  left: 12%;
  width: 280px;
  height: 280px;
  background: rgba(108, 63, 245, .16);
  animation: float-b 9s ease-in-out infinite;
}

.brand-top,
.brand-stage,
.brand-stats {
  position: relative;
  z-index: 1;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #26313d;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 1.5px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border: 2px solid #26313d;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 900;
}

.brand-stage {
  display: grid;
  gap: 18px;
  align-content: end;
}

.brand-copy {
  max-width: 560px;
  animation: rise-in .8s ease both;
}

.eyebrow {
  margin: 0 0 14px;
  color: #4f6478;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.brand-copy h1 {
  margin: 0;
  color: #17202b;
  font-size: clamp(36px, 4.4vw, 62px);
  line-height: 1.08;
  letter-spacing: -1px;
}

.brand-copy h1 strong {
  color: #fff;
  font-weight: 800;
}

.subtitle {
  max-width: 420px;
  margin-top: 24px;
  color: #3f4b58;
  font-size: 16px;
  line-height: 1.8;
}

.login-characters {
  animation: rise-in .9s ease .1s both;
}

.brand-stats {
  display: flex;
  gap: clamp(22px, 5vw, 58px);
}

.brand-stats span {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.brand-stats b {
  color: #17202b;
  font-size: 23px;
}

.brand-stats small {
  color: #536170;
  font-size: 12px;
}

.login-card {
  align-self: center;
  width: min(100%, 440px);
  padding: 42px 34px;
  color: #26313d;
  animation: rise-in .75s ease .05s both;
}

.card-header {
  margin-bottom: 26px;
}

.card-header h2 {
  margin: 0;
  color: #111827;
  font-size: 30px;
}

.login-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 24px;
  padding: 5px;
  border-radius: 10px;
  background: #edf0f3;
}

.login-tabs button {
  min-height: 40px;
  border: 0;
  border-radius: 7px;
  background: transparent;
  color: #718096;
  cursor: pointer;
  font-weight: 700;
  transition: background .2s, color .2s, transform .2s;
}

.login-tabs button:hover {
  transform: translateY(-1px);
}

.login-tabs button.active {
  background: #26313d;
  color: #fff;
}

.form-group {
  margin-bottom: 17px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #374151;
  font-size: 13px;
  font-weight: 700;
}

.form-group input {
  width: 100%;
  height: 48px;
  padding: 0 14px;
  border: 1px solid #d7dde4;
  border-radius: 8px;
  outline: 0;
  background: #fff;
  transition: border .2s, box-shadow .2s;
}

.form-group input:focus {
  border-color: #64778a;
  box-shadow: 0 0 0 3px rgba(100, 119, 138, .12);
}

.password-field {
  position: relative;
}

.password-field input {
  padding-right: 64px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
}

.password-toggle:hover {
  color: #374151;
  background: #f3f4f6;
}

.verification-code {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 10px;
  align-items: center;
}

.verification-code img {
  width: 120px;
  height: 48px;
  border-radius: 8px;
  border: 1px solid #d7dde4;
  cursor: pointer;
  object-fit: cover;
  background: #fff;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0 18px;
  color: #6b7280;
  font-size: 13px;
}

.remember-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.form-options a {
  color: #4b5563;
  text-decoration: none;
  font-weight: 700;
}

.form-options a:hover {
  text-decoration: underline;
}

.error-message {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef2f2;
  color: #b91c1c;
  font-size: 13px;
}

.login-btn {
  width: 100%;
  height: 48px;
  border: 0;
  border-radius: 8px;
  background: #26313d;
  color: #fff;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
  transition: transform .2s, opacity .2s, box-shadow .2s;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(38, 49, 61, .18);
}

.login-btn:disabled {
  opacity: .7;
  cursor: not-allowed;
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes float-a {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(18px, -22px) scale(1.08); }
}

@keyframes float-b {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-16px, 20px) scale(1.05); }
}

@keyframes orbit-pulse {
  0%, 100% { transform: scale(1); opacity: .9; }
  50% { transform: scale(1.05); opacity: 1; }
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .brand-panel {
    min-height: auto;
    padding: 36px 24px 18px;
  }

  .brand-stats {
    display: none;
  }

  .login-card {
    width: 100%;
    padding: 28px 24px 42px;
  }
}
</style>

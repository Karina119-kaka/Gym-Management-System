<template>
  <main class="login-page">
    <section class="brand-panel">
      <div class="brand-mark">GYM</div>
      <div class="brand-copy">
        <p class="eyebrow">Performance Center</p>
        <h1>健身房管理系统</h1>
        <p class="subtitle">会员训练、课程预约与场馆运营，一站式高效管理。</p>
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
          <input v-model.trim="form.username" type="text" autocomplete="username" />
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" autocomplete="current-password" />
        </div>
        <div class="form-group">
          <label>验证码</label>
          <div class="verification-code">
            <input v-model.trim="form.code" type="text" autocomplete="off" />
            <img :src="captchaUrl" @click="refreshCaptcha" alt="验证码" title="点击刷新验证码" />
          </div>
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
import { postForm, pickData } from '../utils/api'

export default {
  name: 'Login',
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
      submitting: false
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
      this.refreshCaptcha()
    },
    refreshCaptcha() {
      this.captchaUrl = `/api/verifyCode?timestamp=${Date.now()}`
    },
    async login() {
      if (this.submitting) return
      this.errorMessage = ''
      sessionStorage.setItem('loginRole', this.currentRole)

      if (!this.form.username || !this.form.password || !this.form.code) {
        this.errorMessage = '请完整输入账号、密码和验证码'
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

        localStorage.setItem('role', this.currentRole)
        localStorage.setItem('user', JSON.stringify(pickData(result) || {}))

        const target = { member: '/member', coach: '/coach', admin: '/admin' }[this.currentRole] || '/login'
        await this.$router.push(target)
      } catch (error) {
        this.errorMessage = error.message || '登录失败，请稍后重试'
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
    this.refreshCaptcha()
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; padding: 48px; display: grid; grid-template-columns: minmax(320px, 1.08fr) minmax(340px, 440px); align-items: center; gap: 42px; background: linear-gradient(135deg, rgba(26,31,39,.96), rgba(43,48,58,.92)); color: #f8fafc; }
.brand-panel { min-height: 560px; padding: 56px; border: 1px solid rgba(255,255,255,.12); border-radius: 8px; background: rgba(255,255,255,.06); display: flex; flex-direction: column; justify-content: center; gap: 24px; }
.brand-mark { width: 76px; height: 76px; display: grid; place-items: center; border: 1px solid rgba(255,255,255,.32); border-radius: 8px; color: #7df3d5; font-weight: 800; }
.eyebrow { margin: 0 0 12px; color: #7df3d5; font-size: 13px; font-weight: 700; text-transform: uppercase; }
.brand-copy h1 { margin: 0; font-size: 42px; }
.subtitle { margin-top: 16px; color: rgba(248,250,252,.78); font-size: 17px; }
.login-card { padding: 36px; border-radius: 8px; background: #fff; color: #1f2937; box-shadow: 0 30px 80px rgba(0,0,0,.32); }
.card-header h2 { margin: 0; color: #111827; font-size: 30px; }
.login-tabs { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-bottom: 26px; padding: 6px; border-radius: 8px; background: #f3f4f6; }
.login-tabs button { min-height: 42px; border: none; border-radius: 6px; background: transparent; color: #6b7280; cursor: pointer; font-weight: 700; }
.login-tabs button.active { background: #232832; color: #fff; }
.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 8px; color: #374151; font-size: 14px; font-weight: 700; }
.form-group input { width: 100%; height: 46px; padding: 0 14px; border: 1px solid #d1d5db; border-radius: 6px; background: #f9fafb; }
.verification-code { display: grid; grid-template-columns: 1fr 132px; gap: 10px; }
.verification-code img { width: 132px; height: 46px; cursor: pointer; border: 1px solid #d1d5db; border-radius: 6px; }
.login-btn { width: 100%; height: 48px; border: none; border-radius: 6px; background: linear-gradient(135deg, #232832, #3f4652); color: #fff; cursor: pointer; font-size: 16px; font-weight: 800; }
.login-btn:disabled { opacity: 0.7; cursor: not-allowed; }
.error-message { margin-bottom: 14px; padding: 10px 12px; border-radius: 6px; background: #fef2f2; color: #dc2626; text-align: center; }
</style>

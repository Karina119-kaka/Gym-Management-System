<template>
  <div class="profile-page">
    <section class="profile-hero">
      <p>Coach Profile</p>
      <h2>个人信息管理</h2>
      <span>姓名和性别由管理员维护，教练可修改年龄、电话、擅长课程与密码。</span>
    </section>

    <section class="profile-card">
      <form class="profile-form" @submit.prevent="updateProfile">
        <div class="form-grid">
          <div class="form-group"><label>姓名</label><input type="text" v-model="form.coachName" disabled></div>
          <div class="form-group"><label>性别</label><input type="text" v-model="form.coachSex" disabled></div>
          <div class="form-group"><label>年龄</label><input type="number" v-model="form.coachAge" :disabled="!isEditing"></div>
          <div class="form-group"><label>电话</label><input type="text" v-model="form.coachTel" :disabled="!isEditing"></div>
          <div class="form-group"><label>擅长课程</label><input type="text" v-model="form.goodAt" :disabled="!isEditing"></div>
          <div class="form-group"><label>账号状态</label><input type="text" v-model="form.coachStatus" disabled></div>
          <div class="form-group"><label>注册时间</label><input type="text" v-model="form.createTime" disabled></div>
        </div>
        <div class="button-group">
          <button v-if="!isEditing" type="button" class="edit-btn" @click="isEditing = true">编辑资料</button>
          <template v-else>
            <button type="submit" class="save-btn">保存资料</button>
            <button type="button" class="cancel-btn" @click="cancelEdit">取消</button>
          </template>
        </div>
      </form>
    </section>

    <section class="profile-card">
      <h3>修改密码</h3>
      <div class="profile-form">
        <div class="form-grid">
          <div class="form-group"><label>旧密码</label><input type="password" v-model="passwordForm.oldPassword"></div>
          <div class="form-group"><label>新密码</label><input type="password" v-model="passwordForm.newPassword"></div>
          <div class="form-group"><label>确认新密码</label><input type="password" v-model="passwordForm.confirmPassword"></div>
        </div>
        <button type="button" class="submit-btn" @click="updatePassword">修改密码</button>
      </div>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'CoachProfile',
  data() {
    return {
      isEditing: false,
      form: { coachName: '', coachSex: '', coachAge: '', coachTel: '', goodAt: '', coachStatus: '', createTime: '' },
      passwordForm: { oldPassword: '', newPassword: '', confirmPassword: '' }
    }
  },
  mounted() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const result = await get('/api/coach/info')
        const profile = pickData(result) || {}
        this.form = {
          coachName: profile.coachName || '',
          coachSex: this.normalizeSex(profile.coachSex),
          coachAge: profile.coachAge || '',
          coachTel: profile.coachTel || '',
          goodAt: profile.goodAt || '',
          coachStatus: profile.coachStatus || '',
          createTime: profile.createTime ? this.formatDate(profile.createTime) : ''
        }
        this.syncLocalUser(profile)
      } catch (error) {
        this.$message.error(error.message || '个人信息加载失败')
      }
    },
    cancelEdit() {
      this.isEditing = false
      this.loadProfile()
    },
    async updateProfile() {
      const age = Number(this.form.coachAge)
      if (this.form.coachAge === '' || this.form.coachAge === null || Number.isNaN(age) || age <= 0 || age > 150) {
        return this.$message.warning('请填写正确的年龄')
      }
      if (!this.form.coachTel) return this.$message.warning('请填写联系电话')
      const payload = {
        coachName: this.form.coachName,
        coachSex: this.form.coachSex,
        coachAge: age,
        coachTel: this.form.coachTel,
        goodAt: this.form.goodAt
      }
      try {
        const result = await postJson('/api/coach/update', payload, { method: 'PUT' })
        this.$message.success('个人信息更新成功')
        this.syncLocalUser(pickData(result) || payload)
        this.isEditing = false
        this.loadProfile()
      } catch (error) {
        this.$message.error(error.message || '个人信息更新失败')
      }
    },
    async updatePassword() {
      if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword) return this.$message.warning('请填写旧密码和新密码')
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) return this.$message.error('两次输入的新密码不一致')
      try {
        await postJson('/api/coach/password', this.passwordForm, { method: 'PUT' })
        this.$message.success('密码修改成功')
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      } catch (error) {
        this.$message.error(error.message || '密码修改失败')
      }
    },
    syncLocalUser(updatedUser) {
      const currentUser = JSON.parse(localStorage.getItem('user')) || {}
      localStorage.setItem('user', JSON.stringify({ ...currentUser, ...updatedUser }))
      window.dispatchEvent(new Event('user-updated'))
    },
    normalizeSex(sex) {
      if (sex === 'M') return '男'
      if (sex === 'F') return '女'
      return sex || ''
    },
    formatDate(date) {
      return date ? new Date(date).toLocaleString('zh-CN') : ''
    }
  }
}
</script>

<style scoped>
.profile-page { max-width: 920px; margin: 0 auto; }
.profile-hero { margin-bottom: 18px; padding: 28px; border-radius: 8px; background: linear-gradient(135deg, #232832, #4b5563); color: #fff; }
.profile-hero p { margin: 0 0 8px; color: #7df3d5; font-size: 13px; font-weight: 800; text-transform: uppercase; }
.profile-hero h2 { margin: 0; font-size: 30px; }
.profile-hero span { display: block; margin-top: 10px; color: rgba(255,255,255,.78); }
.profile-card { margin-bottom: 18px; padding: 24px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; box-shadow: 0 14px 34px rgba(31,41,55,.08); }
.profile-card h3 { margin: 0 0 18px; color: #111827; }
.profile-form { display: flex; flex-direction: column; gap: 18px; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.form-group { display: flex; flex-direction: column; gap: 7px; }
.form-group label { color: #374151; font-size: 14px; font-weight: 800; }
.form-group input { height: 42px; padding: 0 12px; border: 1px solid #d1d5db; border-radius: 6px; color: #111827; font-size: 15px; }
.form-group input:disabled { background-color: #f3f4f6; color: #4b5563; cursor: not-allowed; }
.button-group { display: flex; gap: 10px; }
.edit-btn, .save-btn, .cancel-btn, .submit-btn { min-width: 108px; height: 42px; padding: 0 18px; border: none; border-radius: 6px; cursor: pointer; color: #fff; font-size: 15px; font-weight: 800; }
.edit-btn, .save-btn, .submit-btn { background: #3f4652; }
.cancel-btn { background: #9ca3af; }
@media (max-width: 720px) { .form-grid { grid-template-columns: 1fr; } }
</style>

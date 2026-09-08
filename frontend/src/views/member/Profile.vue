<template>
  <div class="profile-page">
    <section class="profile-hero">
      <p>Member Profile</p>
      <h2>个人信息管理</h2>
      <span>查看并维护会员资料与登录密码。</span>
    </section>

    <section class="profile-card">
      <form class="profile-form" @submit.prevent="updateProfile">
        <div class="form-grid">
          <div class="form-group">
            <label>会员账号</label>
            <input type="text" v-model="form.memberNo" disabled>
          </div>
          <div class="form-group">
            <label>姓名</label>
            <input type="text" v-model="form.memberName" :disabled="!isEditing">
          </div>
          <div class="form-group">
            <label>性别</label>
            <select v-model="form.memberSex" :disabled="!isEditing">
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </div>
          <div class="form-group">
            <label>年龄</label>
            <input type="number" v-model="form.memberAge" :disabled="!isEditing">
          </div>
          <div class="form-group">
            <label>电话</label>
            <input type="text" v-model="form.memberTel" :disabled="!isEditing">
          </div>
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
          <div class="form-group">
            <label>旧密码</label>
            <input type="password" v-model="passwordForm.oldPassword">
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="passwordForm.newPassword">
          </div>
          <div class="form-group">
            <label>确认新密码</label>
            <input type="password" v-model="passwordForm.confirmPassword">
          </div>
        </div>
        <button type="button" class="submit-btn" @click="updatePassword">修改密码</button>
      </div>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'MemberProfile',
  data() {
    return {
      isEditing: false,
      form: { memberNo: '', memberName: '', memberSex: '男', memberAge: '', memberTel: '' },
      passwordForm: { oldPassword: '', newPassword: '', confirmPassword: '' }
    }
  },
  mounted() {
    this.loadProfile()
  },
  methods: {
    async loadProfile() {
      try {
        const result = await get('/api/member/info')
        const profile = pickData(result) || {}
        this.form = {
          memberNo: profile.memberNo || '',
          memberName: profile.memberName || '',
          memberSex: profile.memberSex || '男',
          memberAge: profile.memberAge || '',
          memberTel: profile.memberTel || ''
        }
      } catch (error) {
        this.$message.error(error.message || '个人信息加载失败')
      }
    },
    cancelEdit() {
      this.isEditing = false
      this.loadProfile()
    },
    async updateProfile() {
      if (!this.form.memberName) return this.$message.warning('请填写姓名')
      if (this.form.memberTel && !/^1\d{10}$/.test(this.form.memberTel)) return this.$message.warning('请填写正确的 11 位手机号')
      try {
        await postJson('/api/member/update', {
          memberName: this.form.memberName,
          memberSex: this.form.memberSex,
          memberAge: this.form.memberAge ? Number(this.form.memberAge) : null,
          memberTel: this.form.memberTel
        }, { method: 'PUT' })
        this.$message.success('更新成功')
        this.isEditing = false
        this.loadProfile()
      } catch (error) {
        this.$message.error(error.message || '更新失败')
      }
    },
    async updatePassword() {
      if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword) {
        this.$message.warning('请填写旧密码和新密码')
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.error('两次输入的新密码不一致')
        return
      }
      try {
        await postJson('/api/member/password', this.passwordForm, { method: 'PUT' })
        this.$message.success('密码修改成功')
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      } catch (error) {
        this.$message.error(error.message || '密码修改失败')
      }
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
.form-group input, .form-group select { height: 42px; padding: 0 12px; border: 1px solid #d1d5db; border-radius: 6px; }
.form-group input:disabled, .form-group select:disabled { background-color: #f3f4f6; color: #4b5563; cursor: not-allowed; }
.button-group { display: flex; gap: 10px; }
.edit-btn, .save-btn, .cancel-btn, .submit-btn { min-width: 108px; height: 42px; border: none; border-radius: 6px; cursor: pointer; color: #fff; font-size: 15px; font-weight: 800; }
.edit-btn, .save-btn, .submit-btn { background: #3f4652; }
.cancel-btn { background: #9ca3af; }
</style>

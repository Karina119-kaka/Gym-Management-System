<template>
  <div class="coach-course-page">
    <section class="panel publish-panel">
      <div class="panel-head">
        <h2>{{ isEdit ? '编辑课程' : '发布新课程' }}</h2>
      </div>
      <form class="form-grid" @submit.prevent="submitCourse">
        <div class="form-item">
          <label>课程名称</label>
          <input v-model.trim="form.courseName" type="text" />
        </div>
        <div class="form-item">
          <label>课程价格</label>
          <input v-model.number="form.coursePrice" type="number" min="0" step="0.01" />
        </div>
        <div class="form-item full">
          <label>课程简介</label>
          <textarea v-model.trim="form.courseIntro" rows="3"></textarea>
        </div>
        <div class="form-item full">
          <label>详细描述</label>
          <textarea v-model.trim="form.courseDetail" rows="5"></textarea>
        </div>
        <div class="form-item">
          <label>状态</label>
          <input type="text" :value="isEdit ? '保存后将重新提交管理员审核' : '提交后由管理员审核'" disabled />
        </div>
        <div class="actions">
          <button type="submit" class="btn primary">{{ isEdit ? '保存修改' : '发布课程' }}</button>
          <button v-if="isEdit" type="button" class="btn" @click="resetForm">取消编辑</button>
        </div>
      </form>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>我的课程</h2>
        <span>{{ courses.length }} 门</span>
      </div>
      <table class="course-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>课程名称</th>
            <th>价格</th>
            <th>简介</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in courses" :key="item.courseId">
            <td>{{ item.courseId }}</td>
            <td>{{ item.courseName }}</td>
            <td>¥{{ Number(item.coursePrice || 0).toFixed(2) }}</td>
            <td class="intro-cell">{{ item.courseIntro || '-' }}</td>
            <td><span :class="['status', statusClass(item.saleStatus)]">{{ normalizeStatus(item.saleStatus) }}</span></td>
            <td>{{ formatDate(item.createTime) }}</td>
            <td>
              <button class="mini" @click="startEdit(item)">编辑</button>
              <button v-if="canOffShelf(item)" class="mini warn" @click="offShelf(item)">下架</button>
              <button class="mini danger" @click="deleteCourse(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!courses.length">
            <td colspan="7" class="empty">暂无课程，请先发布</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'CoachCoursePublish',
  data() {
    return {
      courses: [],
      isEdit: false,
      form: {
        courseId: null,
        courseName: '',
        coursePrice: '',
        courseIntro: '',
        courseDetail: '',
        saleStatus: '待审核'
      }
    }
  },
  mounted() {
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      try {
        const res = await get('/api/coach/course/list')
        const list = pickData(res)
        this.courses = Array.isArray(list) ? list : []
      } catch (error) {
        this.$message.error(error.message || '课程列表加载失败')
      }
    },
    async submitCourse() {
      if (!this.form.courseName) return this.$message.warning('请填写课程名称')
      if (this.form.coursePrice === '' || Number(this.form.coursePrice) < 0) return this.$message.warning('请填写正确价格')
      try {
        await postJson('/api/coach/course', this.form, { method: this.isEdit ? 'PUT' : 'POST' })
        this.$message.success(this.isEdit ? '课程更新成功，等待管理员审核' : '课程发布成功')
        this.resetForm()
        this.loadCourses()
      } catch (error) {
        this.$message.error(error.message || '保存失败')
      }
    },
    startEdit(item) {
      this.isEdit = true
      this.form = {
        courseId: item.courseId,
        courseName: item.courseName || '',
        coursePrice: item.coursePrice || '',
        courseIntro: item.courseIntro || '',
        courseDetail: item.courseDetail || '',
        saleStatus: this.normalizeStatus(item.saleStatus)
      }
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    resetForm() {
      this.isEdit = false
      this.form = {
        courseId: null,
        courseName: '',
        coursePrice: '',
        courseIntro: '',
        courseDetail: '',
        saleStatus: '待审核'
      }
    },
    async deleteCourse(item) {
      this.$confirm(`确定删除课程“${item.courseName}”吗？删除后不可恢复。`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await postJson(`/api/coach/course/${item.courseId}`, {}, { method: 'DELETE' })
            this.$message.success('删除成功')
            this.loadCourses()
          } catch (error) {
            this.$message.error(error.message || '删除失败')
          }
        })
        .catch(() => {})
    },
    canOffShelf(item) {
      return this.normalizeStatus(item.saleStatus) === '上架'
    },
    offShelf(item) {
      this.$confirm(`确定下架课程“${item.courseName}”吗？`, '提示', { type: 'warning' })
        .then(async () => {
          try {
            await postJson(`/api/coach/course/status/${item.courseId}?status=${encodeURIComponent('下架')}`, {}, { method: 'PUT' })
            this.$message.success('课程已下架')
            this.loadCourses()
          } catch (error) {
            this.$message.error(error.message || '下架失败')
          }
        })
        .catch(() => {})
    },
    normalizeStatus(status) {
      if (status === 'ON_SALE') return '上架'
      if (!status) return '下架'
      return status
    },
    statusClass(status) {
      return this.normalizeStatus(status).includes('上架') ? 'on' : 'off'
    },
    formatDate(value) {
      return value ? new Date(value).toLocaleString('zh-CN') : '-'
    }
  }
}
</script>

<style scoped>
.coach-course-page { display: grid; gap: 16px; max-width: 1180px; margin: 0 auto; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 18px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.panel-head h2 { margin: 0; color: #111827; }
.panel-head span { color: #6b7280; font-size: 13px; font-weight: 700; }
.form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item.full, .actions { grid-column: span 2; }
.form-item label { color: #374151; font-weight: 700; }
.form-item input, .form-item textarea { border: 1px solid #d1d5db; border-radius: 6px; padding: 10px 12px; font-size: 14px; }
.actions { display: flex; gap: 10px; margin-top: 6px; }
.btn { height: 38px; padding: 0 14px; border: 1px solid #d1d5db; border-radius: 6px; background: #fff; cursor: pointer; }
.btn.primary { border: none; background: #3f4652; color: #fff; font-weight: 700; }
.course-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.course-table th, .course-table td { padding: 10px 8px; border-bottom: 1px solid #e5e7eb; text-align: left; vertical-align: top; }
.intro-cell { max-width: 280px; color: #4b5563; }
.status { padding: 4px 8px; border-radius: 999px; font-size: 12px; font-weight: 700; }
.status.on { background: #dcfce7; color: #166534; }
.status.off { background: #e2e8f0; color: #334155; }
.mini { margin-right: 6px; height: 30px; padding: 0 10px; border-radius: 6px; border: 1px solid #cbd5e1; background: #fff; cursor: pointer; }
.mini.warn { color: #b45309; border-color: #fde68a; background: #fffbeb; }
.mini.danger { color: #b91c1c; border-color: #fecaca; background: #fff1f2; }
.empty { text-align: center; color: #94a3b8; }
</style>

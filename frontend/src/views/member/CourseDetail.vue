<template>
  <div class="course-detail-page">
    <div class="top-tools"><button type="button" class="back-btn" @click="$router.back()">返回</button></div>
    <section class="detail-card" v-if="course">
      <div class="hero"></div>
      <div class="detail-content">
        <div class="title-row"><h1>{{ course.courseName }}</h1><strong>¥{{ formatPrice(course.coursePrice) }}</strong></div>
        <p class="intro">{{ course.courseIntro || '暂无课程简介' }}</p>
        <h3>课程详细描述</h3><p class="detail">{{ course.courseDetail || '暂无详细描述' }}</p>
        <h3>授课教练</h3>
        <p class="coach-line">姓名：{{ course.coachName || '-' }}</p>
        <p class="coach-line">性别：{{ normalizeSex(course.coachSex) || '-' }}</p>
        <p class="coach-line">年龄：{{ course.coachAge ? `${course.coachAge}岁` : '-' }}</p>
        <div class="meta"><span>课程ID：{{ course.courseId }}</span><span>发布时间：{{ formatDate(course.createTime) }}</span></div>
        <button type="button" class="buy-btn" @click="buyCourse">购买课程</button>
      </div>
    </section>
  </div>
</template>

<script>
import { get, postForm, pickData } from '../../utils/api'

export default {
  name: 'MemberCourseDetail',
  data() {
    return { course: null }
  },
  mounted() {
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      const id = this.$route.params.id
      const result = await get(`/api/member/course/${id}`)
      this.course = pickData(result) || null
    },
    async buyCourse() {
      if (!this.course) return
      try {
        await this.$confirm(`确认购买课程“${this.course.courseName}”吗？`, '确认购买', {
          type: 'warning',
          confirmButtonText: '确认购买',
          cancelButtonText: '取消',
          closeOnClickModal: false,
          closeOnPressEscape: true,
          customClass: 'purchase-confirm-dialog'
        })
        await postForm(`/api/member/course/buy/${this.course.courseId}`, {}, { method: 'POST' })
        this.$message.success('购买成功')
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error(error.message || '购买失败')
        }
      }
    },
    formatPrice(price) {
      return Number(price || 0).toFixed(2)
    },
    normalizeSex(sex) {
      if (sex === 'M' || sex === '男') return '男'
      if (sex === 'F' || sex === '女') return '女'
      return sex || ''
    },
    formatDate(date) {
      return date ? new Date(date).toLocaleString('zh-CN') : '-'
    }
  }
}
</script>

<style scoped>
.course-detail-page { max-width: 1100px; margin: 0 auto; padding: 16px 0 24px; }
.top-tools { margin-bottom: 12px; }
.back-btn { height: 36px; padding: 0 14px; border: 1px solid #d1d5db; border-radius: 6px; background: #fff; cursor: pointer; font-weight: 700; }
.detail-card { border: 1px solid #e5e7eb; border-radius: 8px; overflow: hidden; background: #fff; }
.hero { height: 170px; background: url('https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=1600&q=80') center/cover; }
.detail-content { padding: 22px 24px; text-align: center; color: #1f2937; max-width: 980px; margin: 0 auto; }
.title-row { display: flex; flex-direction: column; justify-content: center; align-items: center; gap: 8px; }
.title-row h1 { margin: 0; font-size: 28px; line-height: 1.2; color: #0f172a; }
.title-row strong { color: #10b981; font-size: 24px; }
.intro { margin: 12px 0 20px; font-size: 15px; color: #334155; }
h3 { margin: 0 0 8px; font-size: 24px; color: #0f172a; }
.detail { margin: 0 auto 22px; max-width: 860px; font-size: 15px; line-height: 1.8; white-space: pre-wrap; }
.coach-line { margin: 5px 0; font-size: 15px; color: #1e293b; }
.meta { margin: 22px 0 12px; display: flex; justify-content: center; gap: 26px; flex-wrap: wrap; color: #64748b; font-size: 14px; }
.buy-btn { width: 100%; height: 52px; border: none; border-radius: 6px; background: #3f4652; color: #fff; font-size: 18px; font-weight: 800; cursor: pointer; }
</style>

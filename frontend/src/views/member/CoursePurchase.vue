<template>
  <div class="course-purchase">
    <section class="course-hero">
      <div>
        <p>Course Store</p>
        <h1>课程购买</h1>
        <span>仅展示已上架课程，点击课程名称可查看详情。</span>
      </div>
    </section>

    <section class="toolbar">
      <input v-model.trim="keyword" type="text" placeholder="搜索课程名称、简介或教练" />
    </section>

    <section class="course-grid" v-if="filteredCourses.length">
      <article class="course-card" v-for="c in filteredCourses" :key="c.courseId">
        <div class="course-body">
          <h3 @click="$router.push(`/member/course/${c.courseId}`)">{{ c.courseName }}</h3>
          <strong>¥{{ Number(c.coursePrice || 0).toFixed(2) }}</strong>
          <p>{{ c.courseIntro || '暂无课程简介' }}</p>
          <p>课程时长：1小时</p>
          <p>教练：{{ c.coachName || '-' }} / {{ sex(c.coachSex) }} / {{ c.coachAge || '-' }}岁</p>
          <button @click="buyCourse(c)">购买</button>
        </div>
      </article>
    </section>

    <section v-else class="empty-state">
      <p>暂无上架课程</p>
    </section>
  </div>
</template>

<script>
import { get, postForm, pickData } from '../../utils/api'

export default {
  name: 'CoursePurchase',
  data() {
    return { courses: [], keyword: '' }
  },
  computed: {
    filteredCourses() {
      const k = this.keyword.toLowerCase()
      return !k
        ? this.courses
        : this.courses.filter((c) => `${c.courseName || ''}${c.courseIntro || ''}${c.coachName || ''}`.toLowerCase().includes(k))
    }
  },
  mounted() {
    this.loadCourses()
  },
  methods: {
    async loadCourses() {
      const res = await get('/api/member/course/list')
      const list = pickData(res)
      this.courses = Array.isArray(list) ? list : []
    },
    async buyCourse(c) {
      try {
        await this.$confirm(`确认购买课程“${c.courseName}”吗？`, '确认购买', {
          type: 'warning',
          confirmButtonText: '确认购买',
          cancelButtonText: '取消',
          closeOnClickModal: false,
          closeOnPressEscape: true,
          customClass: 'purchase-confirm-dialog'
        })
        await postForm(`/api/member/course/buy/${c.courseId}`, {}, { method: 'POST' })
        this.$message.success('购买成功')
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          this.$message.error(error.message || '购买失败')
        }
      }
    },
    sex(v) {
      if (v === 'M' || v === '男') return '男'
      if (v === 'F' || v === '女') return '女'
      return v || '-'
    }
  }
}
</script>

<style scoped>
.course-purchase { min-height: calc(100vh - 100px); color: #1f2937; }
.course-hero { min-height: 220px; padding: 36px; display: flex; align-items: flex-end; border-radius: 8px; overflow: hidden; background: linear-gradient(90deg, rgba(17, 24, 39, 0.88), rgba(17, 24, 39, 0.24)), url('https://images.unsplash.com/photo-1571019613914-85f342c6a11e?auto=format&fit=crop&w=1500&q=80') center/cover; color: #fff; }
.course-hero p { margin: 0 0 10px; color: #7df3d5; font-size: 13px; font-weight: 800; text-transform: uppercase; }
.course-hero h1 { margin: 0; font-size: 40px; }
.toolbar { margin: 22px 0; }
.toolbar input { width: min(360px, 100%); height: 42px; padding: 0 14px; border: 1px solid #d1d5db; border-radius: 6px; }
.course-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 18px; }
.course-card { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; box-shadow: 0 14px 34px rgba(31, 41, 55, 0.08); }
.course-body { display: flex; flex-direction: column; gap: 10px; padding: 18px; }
.course-body h3 { margin: 0; cursor: pointer; color: #111827; }
.course-body strong { color: #10b981; }
.course-body p { margin: 0; color: #6b7280; }
.course-body button { margin-top: 10px; width: 100%; height: 42px; border: none; border-radius: 6px; background: #3f4652; color: #fff; font-weight: 800; cursor: pointer; }
.empty-state { padding: 48px; border: 1px dashed #cbd5e1; border-radius: 8px; background: #fff; text-align: center; color: #64748b; }
</style>

<template>
  <div class="course-reserve">
    <section class="hero">
      <div>
        <p>Course Reservation</p>
        <h1>课程预约</h1>
        <span>仅显示可预约课程，按剩余次数预约并选择上课时间。</span>
      </div>
    </section>

    <section v-if="visibleCourses.length" class="grid">
      <article v-for="item in visibleCourses" :key="item.courseId" class="card">
        <div class="title-row">
          <h3>{{ item.courseName || '未命名课程' }}</h3>
          <strong>¥{{ formatPrice(item.coursePrice) }}</strong>
        </div>
        <p class="intro">{{ item.courseIntro || '暂无课程简介' }}</p>
        <div class="coach">
          <span>教练：{{ item.coachName || '-' }}</span>
          <span>{{ normalizeSex(item.coachSex) }} {{ item.coachAge ? `${item.coachAge}岁` : '' }}</span>
        </div>
        <div class="meta">
          <span>已购：{{ item.totalBought || 0 }} 次</span>
          <span class="remain">剩余：{{ item.remainingCount || 0 }} 次</span>
        </div>
        <div class="duration">课程时长：1小时</div>
        <div class="reserve-row">
          <input v-model="reserveTimeMap[item.courseId]" type="datetime-local" />
          <button type="button" @click="reserve(item)">预约课程</button>
        </div>
      </article>
    </section>

    <section v-else class="empty">
      <p>暂无可预约课程</p>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'MemberCourseReserve',
  data() {
    return {
      courses: [],
      reserveTimeMap: {}
    }
  },
  computed: {
    visibleCourses() {
      return this.courses.filter((item) => Number(item.remainingCount || 0) > 0)
    }
  },
  mounted() {
    this.loadPurchased()
  },
  methods: {
    async loadPurchased() {
      const res = await get('/api/member/order/purchased')
      const list = pickData(res)
      this.courses = Array.isArray(list) ? list : []
    },
    async reserve(item) {
      const orderTime = this.reserveTimeMap[item.courseId]
      if (!orderTime) {
        this.$message.warning('请先选择预约时间')
        return
      }
      await postJson('/api/member/order/reserve', { courseId: item.courseId, orderTime }, { method: 'POST' })
      this.$message.success('预约成功，等待教练确认')
      this.reserveTimeMap[item.courseId] = ''
      this.loadPurchased()
    },
    formatPrice(v) {
      const n = Number(v || 0)
      return Number.isFinite(n) ? n.toFixed(2) : '0.00'
    },
    normalizeSex(v) {
      if (!v) return '-'
      const t = String(v).trim().toLowerCase()
      if (t === 'm' || t === 'male' || t === '男') return '男'
      if (t === 'f' || t === 'female' || t === '女') return '女'
      return String(v)
    }
  }
}
</script>

<style scoped>
.course-reserve { max-width: 1180px; margin: 0 auto; display: grid; gap: 16px; }
.hero { min-height: 170px; border-radius: 8px; padding: 28px; color: #fff; display: flex; align-items: flex-end; background: linear-gradient(105deg, #111827, #374151 60%, #4b5563); }
.hero p { margin: 0 0 8px; color: #7df3d5; font-size: 12px; font-weight: 800; text-transform: uppercase; }
.hero h1 { margin: 0; font-size: 34px; }
.hero span { display: block; margin-top: 10px; color: rgba(255, 255, 255, 0.85); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
.card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; background: #fff; display: grid; gap: 10px; }
.title-row { display: flex; justify-content: space-between; gap: 10px; align-items: flex-start; }
.title-row h3 { margin: 0; font-size: 18px; color: #111827; }
.title-row strong { color: #10b981; white-space: nowrap; }
.intro { margin: 0; color: #6b7280; min-height: 44px; }
.coach, .meta { display: flex; justify-content: space-between; gap: 8px; color: #4b5563; font-size: 13px; }
.remain { font-weight: 700; color: #0f766e; }
.duration { font-size: 13px; color: #475569; font-weight: 700; }
.reserve-row { display: grid; grid-template-columns: 1fr auto; gap: 10px; align-items: center; }
.reserve-row input { height: 40px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 10px; }
.reserve-row button { height: 40px; border: none; border-radius: 6px; background: #3f4652; color: #fff; font-weight: 700; cursor: pointer; padding: 0 16px; }
.empty { min-height: 140px; border: 1px dashed #cbd5e1; border-radius: 8px; display: grid; place-items: center; color: #64748b; background: #fff; }
</style>

<template>
  <div class="coach-dashboard">
    <section class="hero">
      <div>
        <p class="eyebrow">Coach Dashboard</p>
        <h1>{{ heroTitle }}</h1>
        <span>今日已确认课表 {{ todaySchedule.length }} 节，明日 {{ tomorrowSchedule.length }} 节。</span>
      </div>
    </section>

    <section v-if="notice.hasUnread" class="notice-bar">
      <span>您有 {{ notice.unreadCount }} 条新的预约待查看</span>
      <button type="button" @click="markNoticeRead">标记为已读</button>
    </section>

    <section class="next-class-card" v-if="nextClass">
      <div class="head"><h2>下一节课</h2><span>{{ nextClass.status }}</span></div>
      <div class="next-grid">
        <div>
          <h3>{{ nextClass.memberName }}</h3>
          <p>联系方式：{{ nextClass.phone }}</p>
          <p>课程：{{ nextClass.goal }}</p>
        </div>
        <div>
          <p>开课时间：{{ nextClass.startAt }}</p>
          <p>课程时长：1小时</p>
          <p>开课倒计时：{{ nextClass.countdown }}</p>
        </div>
      </div>
    </section>

    <section class="schedule-grid">
      <article class="panel">
        <div class="panel-head"><h2>今日课表</h2><span>{{ todaySchedule.length }} 节</span></div>
        <ul class="timeline">
          <li v-for="item in todaySchedule" :key="`today-${item.id}`">
            <time>{{ item.time }}</time>
            <div class="timeline-content">
              <header><h3>{{ item.memberName }}</h3><span :class="['status', statusClass(item.status)]">{{ item.status }}</span></header>
              <p>课程：{{ item.goal }}</p><p>时长：1小时</p><p>联系方式：{{ item.phone }}</p>
            </div>
          </li>
          <li v-if="!todaySchedule.length"><time>--:--</time><div class="timeline-content"><p>今日暂无已确认课程</p></div></li>
        </ul>
      </article>

      <article class="panel">
        <div class="panel-head"><h2>明日课表</h2><span>{{ tomorrowSchedule.length }} 节</span></div>
        <ul class="timeline">
          <li v-for="item in tomorrowSchedule" :key="`tomorrow-${item.id}`">
            <time>{{ item.time }}</time>
            <div class="timeline-content">
              <header><h3>{{ item.memberName }}</h3><span class="status idle">未开始</span></header>
              <p>课程：{{ item.goal }}</p><p>时长：1小时</p><p>联系方式：{{ item.phone }}</p>
            </div>
          </li>
          <li v-if="!tomorrowSchedule.length"><time>--:--</time><div class="timeline-content"><p>明日暂无已确认课程</p></div></li>
        </ul>
      </article>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'CoachHome',
  data() {
    return {
      user: {},
      todaySchedule: [],
      tomorrowSchedule: [],
      nextClass: null,
      nowTimer: null,
      notice: { hasUnread: false, unreadCount: 0 },
      pendingRows: []
    }
  },
  computed: {
    heroTitle() { return `${this.user.coachName || '教练'}，今天训练顺利` }
  },
  mounted() {
    this.loadUser()
    this.loadSchedule()
    this.loadNotice()
    this.nowTimer = setInterval(() => this.refreshDerivedData(), 1000)
  },
  beforeUnmount() { if (this.nowTimer) clearInterval(this.nowTimer) },
  methods: {
    async loadUser() {
      try {
        const res = await get('/api/coach/info')
        const data = pickData(res)
        if (data) this.user = data
      } catch (error) {
        console.error('教练信息加载失败', error)
      }
    },
    async loadSchedule() {
      try {
        const res = await get('/api/coach/order/schedule')
      const list = Array.isArray(pickData(res)) ? pickData(res) : []
      const now = new Date()
      const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
      const toItem = (row) => {
        const dt = new Date(row.orderTime)
        return {
          id: row.orderId,
          rawTime: dt,
          time: dt.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }),
          memberName: row.memberName || '-',
          phone: row.memberTel || '-',
          goal: row.courseName || '-',
          status: '未开始'
        }
      }
        this.todaySchedule = list.map(toItem).filter((x) => this.isSameDay(x.rawTime, now)).sort((a, b) => a.rawTime - b.rawTime)
        this.tomorrowSchedule = list.map(toItem).filter((x) => this.isSameDay(x.rawTime, tomorrow)).sort((a, b) => a.rawTime - b.rawTime)
        this.refreshDerivedData()
      } catch (error) {
        console.error('课表加载失败', error)
        this.todaySchedule = []
        this.tomorrowSchedule = []
      }
    },
    async loadNotice() {
      try {
        const res = await get('/api/coach/order/notice/unread')
        const data = pickData(res) || {}
        this.notice = { hasUnread: !!data.hasUnread, unreadCount: Number(data.unreadCount || 0) }
      } catch (_) {
        await this.loadNoticeFallback()
      }
    },
    async markNoticeRead() {
      try {
        await postJson('/api/coach/order/notice/read', {}, { method: 'PUT' })
      } catch (_) {
        const maxId = this.pendingRows.reduce((max, item) => Math.max(max, Number(item.orderId || 0)), 0)
        localStorage.setItem(this.noticeStorageKey(), String(maxId))
      }
      this.notice = { hasUnread: false, unreadCount: 0 }
      this.$message.success('已标记已读')
    },
    async loadNoticeFallback() {
      const res = await get('/api/coach/order/pending')
      const list = Array.isArray(pickData(res)) ? pickData(res) : []
      this.pendingRows = list
      const readMaxId = Number(localStorage.getItem(this.noticeStorageKey()) || 0)
      const unread = list.filter((item) => Number(item.orderId || 0) > readMaxId).length
      this.notice = { hasUnread: unread > 0, unreadCount: unread }
    },
    noticeStorageKey() { return `coach_notice_read_max_${this.user.coachNo || 'default'}` },
    refreshDerivedData() {
      const now = new Date()
      for (const item of this.todaySchedule) {
        const start = item.rawTime.getTime()
        const end = start + 60 * 60 * 1000
        if (now.getTime() < start) item.status = '未开始'
        else if (now.getTime() >= start && now.getTime() < end) item.status = '进行中'
        else item.status = '已完成'
      }
      const upcoming = this.todaySchedule.find((x) => x.rawTime.getTime() > now.getTime()) || null
      if (!upcoming) { this.nextClass = null; return }
      this.nextClass = {
        memberName: upcoming.memberName,
        phone: upcoming.phone,
        goal: upcoming.goal,
        startAt: upcoming.rawTime.toLocaleString('zh-CN'),
        countdown: this.formatCountdown(upcoming.rawTime.getTime() - now.getTime()),
        status: upcoming.status
      }
    },
    formatCountdown(ms) {
      const total = Math.max(0, Math.floor(ms / 1000))
      const h = String(Math.floor(total / 3600)).padStart(2, '0')
      const m = String(Math.floor((total % 3600) / 60)).padStart(2, '0')
      const s = String(total % 60).padStart(2, '0')
      return `${h}:${m}:${s}`
    },
    isSameDay(dateValue, base) {
      return dateValue.getFullYear() === base.getFullYear() &&
        dateValue.getMonth() === base.getMonth() &&
        dateValue.getDate() === base.getDate()
    },
    statusClass(status) {
      if (status === '已完成') return 'done'
      if (status === '进行中') return 'active'
      return 'idle'
    }
  }
}
</script>

<style scoped>
.coach-dashboard { max-width: 1180px; margin: 0 auto; display: grid; gap: 18px; }
.hero { min-height: 170px; padding: 28px; border-radius: 8px; background: linear-gradient(120deg, #0f172a, #334155 55%, #475569); color: #fff; }
.eyebrow { margin: 0 0 8px; font-size: 12px; font-weight: 800; text-transform: uppercase; color: #7df3d5; }
.hero h1 { margin: 0; font-size: 34px; }
.hero span { display: block; margin-top: 10px; color: rgba(255, 255, 255, 0.82); }
.next-class-card, .panel { padding: 20px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; }
.notice-bar { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-radius: 8px; border: 1px solid #bbf7d0; background: #f0fdf4; color: #166534; }
.notice-bar button { height: 34px; border: none; border-radius: 6px; background: #16a34a; color: #fff; font-weight: 700; padding: 0 12px; cursor: pointer; }
.head, .panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.head span, .status { padding: 4px 9px; border-radius: 999px; font-size: 12px; font-weight: 700; background: #e2e8f0; color: #334155; }
.next-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }
.schedule-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.timeline { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.timeline li { display: grid; grid-template-columns: 68px 1fr; gap: 14px; }
.timeline time { color: #64748b; font-size: 13px; font-weight: 700; padding-top: 10px; }
.timeline-content { padding: 12px; border-radius: 6px; background: #f8fafc; }
.timeline-content header { display: flex; justify-content: space-between; align-items: center; }
.timeline-content h3 { margin: 0; color: #111827; font-size: 16px; }
.timeline-content p { margin: 8px 0 0; color: #4b5563; }
.status.done { background: #dbeafe; color: #1d4ed8; }
.status.active { background: #dcfce7; color: #15803d; }
@media (max-width: 1000px) { .schedule-grid { grid-template-columns: 1fr; } }
</style>

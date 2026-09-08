<template>
  <div class="member-dashboard">
    <section class="hero">
      <div>
        <p class="eyebrow">Member Dashboard</p>
        <h1>{{ welcomeTitle }}</h1>
        <span>训练数据来自健身打卡记录，首页统计自动按本月汇总。</span>
      </div>
      <button type="button" @click="goToClockIn">健身打卡入口</button>
    </section>

    <section class="metrics-grid">
      <article class="metric-card">
        <h3>最近体重</h3>
        <strong>{{ trainingData.weight }} kg</strong>
        <p>来自最新一条打卡记录</p>
      </article>
      <article class="metric-card">
        <h3>最近体脂</h3>
        <strong>{{ trainingData.bodyFat }}%</strong>
        <p>数据来源：{{ trainingData.dataSourceText }}</p>
      </article>
      <article class="metric-card">
        <h3>BMI</h3>
        <strong>{{ trainingData.bmi }}</strong>
        <p>{{ trainingData.bmiText }}</p>
      </article>
      <article class="metric-card">
        <h3>本月训练次数</h3>
        <strong>{{ monthlyData.sessions }} 次</strong>
        <p>训练次数 = 打卡次数</p>
      </article>
      <article class="metric-card">
        <h3>消耗卡路里</h3>
        <strong>{{ monthlyData.calories }}</strong>
        <p>本月累计</p>
      </article>
    </section>

    <AiAdvisor />

    <section class="schedule-grid">
      <article class="schedule-card">
        <div class="schedule-head">
          <h2>今日课表</h2>
          <span>{{ todaySchedule.length }} 节</span>
        </div>
        <div class="schedule-table-wrap">
          <table class="schedule-table">
            <thead>
              <tr><th>时间</th><th>课程</th><th>教练</th><th>状态</th></tr>
            </thead>
            <tbody>
              <tr v-for="item in todaySchedule" :key="item.orderId">
                <td>{{ formatTime(item.orderTime) }}</td>
                <td>{{ item.courseName || '-' }}</td>
                <td>{{ item.coachName || '-' }}</td>
                <td><span class="status-tag">{{ normalizeStatus(item.orderStatus) }}</span></td>
              </tr>
              <tr v-if="!todaySchedule.length"><td colspan="4" class="empty">今日暂无课程</td></tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="schedule-card">
        <div class="schedule-head">
          <h2>明日课表</h2>
          <span>{{ tomorrowSchedule.length }} 节</span>
        </div>
        <div class="schedule-table-wrap">
          <table class="schedule-table">
            <thead>
              <tr><th>时间</th><th>课程</th><th>教练</th><th>状态</th></tr>
            </thead>
            <tbody>
              <tr v-for="item in tomorrowSchedule" :key="item.orderId">
                <td>{{ formatTime(item.orderTime) }}</td>
                <td>{{ item.courseName || '-' }}</td>
                <td>{{ item.coachName || '-' }}</td>
                <td><span class="status-tag">{{ normalizeStatus(item.orderStatus) }}</span></td>
              </tr>
              <tr v-if="!tomorrowSchedule.length"><td colspan="4" class="empty">明日暂无课程</td></tr>
            </tbody>
          </table>
        </div>
      </article>
    </section>
  </div>
</template>

<script>
import { get, pickData } from '../../utils/api'
import AiAdvisor from '../../components/AiAdvisor.vue'

export default {
  name: 'MemberHome',
  components: {
    AiAdvisor
  },
  data() {
    return {
      user: {},
      trainingData: { weight: '-', bodyFat: '-', bmi: '-', bmiText: '等待打卡数据', dataSourceText: '手动录入' },
      monthlyData: { sessions: 0, calories: '0 kcal' },
      todaySchedule: [],
      tomorrowSchedule: []
    }
  },
  computed: {
    welcomeTitle() { return `${this.user.memberName || '会员'}，欢迎回来` }
  },
  mounted() {
    this.loadUser()
    this.loadDashboard()
    this.loadClockInSummary()
    this.loadSchedules()
  },
  methods: {
    goToClockIn() { this.$router.push('/member/clockin') },
    async loadUser() {
      try { const res = await get('/api/member/info'); const data = pickData(res); if (data) this.user = data } catch (_) {}
    },
    async loadDashboard() {
      try {
        const res = await get('/api/member/dashboard')
        const data = pickData(res) || {}
        const latest = data.latestMetrics || {}
        const monthly = data.monthlySummary || {}
        this.trainingData = {
          weight: latest.weightKg ?? '-',
          bodyFat: latest.bodyFatPct ?? '-',
          bmi: latest.bmi ?? '-',
          bmiText: latest.recordDate ? `记录日期：${latest.recordDate}` : '等待打卡数据',
          dataSourceText: latest.dataSource === 'device' ? '设备同步' : '手动录入'
        }
        const calories = Number(monthly.totalCalories || 0)
        this.monthlyData = { sessions: Number(monthly.sessions || 0), calories: `${calories.toLocaleString()} kcal` }
      } catch (error) {
        this.$message.error(error.message || '训练数据加载失败')
      }
    },
    async loadClockInSummary() {
      try { const res = await get('/api/member/clockin'); const data = pickData(res) || {}; this.monthlyData.sessions = Number(data.monthCount || 0) } catch (_) {}
    },
    async loadSchedules() {
      try {
        const res = await get('/api/member/order/my')
        const list = Array.isArray(pickData(res)) ? pickData(res) : []
        const now = new Date()
        const tomorrow = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1)
        this.todaySchedule = list.filter((x) => this.isSameDay(x.orderTime, now)).sort((a, b) => new Date(a.orderTime) - new Date(b.orderTime))
        this.tomorrowSchedule = list.filter((x) => this.isSameDay(x.orderTime, tomorrow)).sort((a, b) => new Date(a.orderTime) - new Date(b.orderTime))
      } catch (_) {
        this.todaySchedule = []
        this.tomorrowSchedule = []
      }
    },
    isSameDay(dateValue, base) {
      if (!dateValue) return false
      const d = new Date(dateValue)
      return d.getFullYear() === base.getFullYear() && d.getMonth() === base.getMonth() && d.getDate() === base.getDate()
    },
    formatTime(v) { return v ? new Date(v).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }) : '-' },
    normalizeStatus(v) {
      return v || '-'
    }
  }
}
</script>

<style scoped>
.member-dashboard { max-width: 1180px; margin: 0 auto; display: grid; gap: 18px; }
.hero { min-height: 180px; padding: 28px; border-radius: 8px; display: flex; justify-content: space-between; align-items: flex-end; gap: 18px; background: linear-gradient(120deg, #1f2937, #4b5563 56%, #64748b); color: #fff; }
.eyebrow { margin: 0 0 10px; font-size: 12px; font-weight: 800; text-transform: uppercase; color: #7df3d5; }
.hero h1 { margin: 0; font-size: 34px; }
.hero span { display: block; margin-top: 10px; color: rgba(255, 255, 255, 0.82); }
.hero button { min-width: 130px; height: 42px; padding: 0 16px; border: none; border-radius: 6px; background: #7df3d5; color: #0f172a; cursor: pointer; font-weight: 800; }
.metrics-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.metric-card { padding: 16px; border-radius: 8px; background: #fff; border: 1px solid #e5e7eb; }
.metric-card h3 { margin: 0 0 10px; font-size: 14px; color: #4b5563; }
.metric-card strong { display: block; font-size: 27px; color: #111827; }
.metric-card p { margin: 8px 0 0; color: #6b7280; font-size: 13px; }
.schedule-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.schedule-card { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; overflow: hidden; }
.schedule-head { display: flex; justify-content: space-between; align-items: center; padding: 16px 18px; border-bottom: 1px solid #e5e7eb; }
.schedule-head h2 { margin: 0; font-size: 20px; color: #111827; }
.schedule-head span { color: #6b7280; font-size: 13px; font-weight: 700; }
.schedule-table-wrap { width: 100%; overflow-x: auto; }
.schedule-table { width: 100%; border-collapse: collapse; min-width: 560px; }
.schedule-table th, .schedule-table td { padding: 12px 14px; border-bottom: 1px solid #eef2f7; text-align: left; color: #334155; }
.schedule-table th { background: #f8fafc; color: #334155; font-weight: 700; }
.status-tag { display: inline-block; padding: 4px 10px; border-radius: 999px; background: #e2e8f0; color: #334155; font-size: 12px; font-weight: 700; }
.empty { text-align: center; color: #94a3b8; }
@media (max-width: 1100px) { .metrics-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } .schedule-grid { grid-template-columns: 1fr; } }
@media (max-width: 760px) { .hero { flex-direction: column; align-items: flex-start; } .metrics-grid { grid-template-columns: 1fr; } }
</style>

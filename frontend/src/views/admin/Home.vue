<template>
  <div class="admin-home">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin Console</p>
        <h1>运营数据总览</h1>
        <span>会员、课程、预约和在店情况的动态看板。</span>
      </div>
    </section>

    <section class="grid">
      <article class="card">
        <label>会员总数</label>
        <strong>{{ stats.memberCount }}</strong>
        <p>当前系统会员规模</p>
      </article>
      <article class="card">
        <label>课程总数</label>
        <strong>{{ stats.courseCount }}</strong>
        <p>包含上架与待审核课程</p>
      </article>
      <article class="card">
        <label>预约总数</label>
        <strong>{{ stats.orderCount }}</strong>
        <p>累计预约记录</p>
      </article>
      <article class="card">
        <label>待处理预约</label>
        <strong>{{ stats.pendingCount }}</strong>
        <p>等待教练或管理员处理</p>
      </article>
      <article class="card success">
        <label>在店人数</label>
        <strong>{{ stats.arrivedCount }}</strong>
        <p>今日已到店人数</p>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel">
        <div class="panel-head">
          <h2>运营动态看板</h2>
          <span>实时比例</span>
        </div>
        <div class="bars">
          <div v-for="item in barItems" :key="item.key" class="bar-item">
            <div class="bar-meta">
              <label>{{ item.label }}</label>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: `${item.percent}%`, background: item.color }"></div>
            </div>
          </div>
        </div>
      </article>

      <article class="panel ring-panel">
        <div class="panel-head">
          <h2>本月预约状态占比</h2>
          <span>成功 / 待处理 / 失败</span>
        </div>
        <div class="ring-wrap">
          <div class="ring" :style="{ background: ringBackground }">
            <div class="ring-center">
              <strong>{{ monthOrderCount }}</strong>
              <span>本月预约</span>
            </div>
          </div>
          <div class="legend">
            <div class="legend-item">
              <span class="dot success-dot"></span>
              <label>预约成功</label>
              <strong>{{ stats.monthApprovedCount || 0 }}</strong>
            </div>
            <div class="legend-item">
              <span class="dot pending-dot"></span>
              <label>待处理</label>
              <strong>{{ stats.monthPendingCount || 0 }}</strong>
            </div>
            <div class="legend-item">
              <span class="dot failed-dot"></span>
              <label>预约失败</label>
              <strong>{{ stats.monthFailedCount || 0 }}</strong>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="quick-grid">
      <button type="button" class="quick-card" @click="goTo('/admin/member')">
        <strong>会员管理</strong>
        <span>查看与维护会员信息</span>
      </button>
      <button type="button" class="quick-card" @click="goTo('/admin/coach')">
        <strong>教练管理</strong>
        <span>处理教练资料与状态</span>
      </button>
      <button type="button" class="quick-card" @click="goTo('/admin/course')">
        <strong>课程管理</strong>
        <span>审核课程与上架状态</span>
      </button>
      <button type="button" class="quick-card" @click="goTo('/admin/order')">
        <strong>订单管理</strong>
        <span>查看预约与考勤结果</span>
      </button>
      <button type="button" class="quick-card" @click="goTo('/admin/ai-stats')">
        <strong>AI 调用统计</strong>
        <span>调用量、成本与成功率监控</span>
      </button>
    </section>
  </div>
</template>

<script>
import { get, pickData } from '../../utils/api'

export default {
  name: 'AdminHome',
  data() {
    return {
      stats: {
        memberCount: 0,
        courseCount: 0,
        orderCount: 0,
        pendingCount: 0,
        arrivedCount: 0,
        approvedCount: 0,
        failedCount: 0,
        monthApprovedCount: 0,
        monthPendingCount: 0,
        monthFailedCount: 0,
        monthOrderCount: 0
      }
    }
  },
  computed: {
    monthOrderCount() {
      return Number(this.stats.monthOrderCount || 0)
    },
    barItems() {
      const maxValue = Math.max(
        1,
        this.stats.memberCount,
        this.stats.courseCount,
        this.stats.orderCount,
        this.stats.pendingCount,
        this.stats.arrivedCount
      )
      return [
        { key: 'member', label: '会员', value: this.stats.memberCount, percent: this.toPercent(this.stats.memberCount, maxValue), color: '#3b82f6' },
        { key: 'course', label: '课程', value: this.stats.courseCount, percent: this.toPercent(this.stats.courseCount, maxValue), color: '#10b981' },
        { key: 'order', label: '预约', value: this.stats.orderCount, percent: this.toPercent(this.stats.orderCount, maxValue), color: '#f59e0b' },
        { key: 'pending', label: '待处理', value: this.stats.pendingCount, percent: this.toPercent(this.stats.pendingCount, maxValue), color: '#f97316' },
        { key: 'arrived', label: '在店', value: this.stats.arrivedCount, percent: this.toPercent(this.stats.arrivedCount, maxValue), color: '#22c55e' }
      ]
    },
    ringBackground() {
      const total = Math.max(1, this.monthOrderCount)
      const success = Number(this.stats.monthApprovedCount || 0)
      const pending = Number(this.stats.monthPendingCount || 0)
      const failed = Number(this.stats.monthFailedCount || 0)

      const successDeg = (success / total) * 360
      const pendingDeg = (pending / total) * 360
      const failedDeg = (failed / total) * 360
      const split1 = successDeg
      const split2 = successDeg + pendingDeg
      const split3 = successDeg + pendingDeg + failedDeg

      return `conic-gradient(#22c55e 0deg ${split1}deg, #f59e0b ${split1}deg ${split2}deg, #ef4444 ${split2}deg ${split3}deg, #e5e7eb ${split3}deg 360deg)`
    }
  },
  mounted() {
    this.loadStats()
  },
  methods: {
    goTo(path) {
      this.$router.push(path)
    },
    toPercent(value, maxValue) {
      return Math.max(8, Math.round((Number(value || 0) / maxValue) * 100))
    },
    async loadStats() {
      try {
        const res = await get('/api/admin/order/stats')
        this.stats = pickData(res) || this.stats
      } catch (error) {
        console.error('管理端统计加载失败', error)
      }
    }
  }
}
</script>

<style scoped>
.admin-home { max-width: 1180px; margin: 0 auto; display: grid; gap: 16px; }
.hero { border-radius: 8px; padding: 24px; background: linear-gradient(135deg, #1f2937, #475569 62%, #64748b); color: #fff; display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; }
.eyebrow { margin: 0 0 8px; color: #7df3d5; font-size: 12px; font-weight: 800; text-transform: uppercase; }
.hero h1 { margin: 0; font-size: 32px; }
.hero span { display: block; margin-top: 8px; color: rgba(255, 255, 255, 0.82); }

.grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.card { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; padding: 18px 16px; display: grid; gap: 10px; min-height: 128px; align-content: center; }
.card label { color: #64748b; font-size: 14px; font-weight: 700; }
.card strong { color: #0f172a; font-size: 38px; line-height: 1; }
.card p { margin: 0; color: #94a3b8; font-size: 13px; }
.card.success { background: #ecfdf5; border-color: #86efac; }

.dashboard-grid { display: grid; grid-template-columns: 1.15fr 0.85fr; gap: 12px; }
.panel { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; padding: 18px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.panel-head h2 { margin: 0; font-size: 20px; color: #111827; }
.panel-head span { color: #6b7280; font-size: 13px; font-weight: 700; }

.bars { display: grid; gap: 14px; }
.bar-item { display: grid; gap: 8px; }
.bar-meta { display: flex; justify-content: space-between; align-items: center; }
.bar-meta label { color: #475569; font-weight: 700; }
.bar-meta strong { color: #0f172a; font-size: 20px; }
.bar-track { height: 10px; border-radius: 999px; background: #f1f5f9; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 999px; transition: width 0.45s ease; }

.ring-panel { display: grid; align-content: start; }
.ring-wrap { display: grid; grid-template-columns: 180px 1fr; gap: 18px; align-items: center; }
.ring { width: 180px; height: 180px; border-radius: 50%; display: grid; place-items: center; }
.ring-center { width: 118px; height: 118px; border-radius: 50%; background: #fff; display: grid; place-items: center; text-align: center; }
.ring-center strong { font-size: 30px; color: #0f172a; line-height: 1; }
.ring-center span { font-size: 13px; color: #64748b; }
.legend { display: grid; gap: 14px; }
.legend-item { display: grid; grid-template-columns: 16px 1fr auto; gap: 10px; align-items: center; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.success-dot { background: #22c55e; }
.pending-dot { background: #f59e0b; }
.failed-dot { background: #ef4444; }
.legend-item label { color: #475569; font-weight: 700; }
.legend-item strong { color: #111827; }

.quick-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.quick-card { border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; background: #fff; cursor: pointer; text-align: left; display: grid; gap: 8px; transition: transform 0.18s ease, box-shadow 0.18s ease; }
.quick-card:hover { transform: translateY(-2px); box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08); }
.quick-card strong { color: #0f172a; font-size: 18px; }
.quick-card span { color: #64748b; font-size: 13px; }

@media (max-width: 1100px) {
  .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .dashboard-grid, .quick-grid { grid-template-columns: 1fr; }
}

@media (max-width: 760px) {
  .hero { flex-direction: column; align-items: flex-start; }
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .ring-wrap { grid-template-columns: 1fr; justify-items: center; }
}
</style>

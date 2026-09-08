<template>
  <div class="data-view">
    <section class="hero">
      <h2>运营数据总览</h2>
      <p>会员、课程、预约、在店人数一屏查看。</p>
    </section>

    <section class="grid">
      <article class="card"><label>会员总数</label><strong>{{ stats.memberCount }}</strong></article>
      <article class="card"><label>课程总数</label><strong>{{ stats.courseCount }}</strong></article>
      <article class="card"><label>预约总数</label><strong>{{ stats.orderCount }}</strong></article>
      <article class="card"><label>待处理预约</label><strong>{{ stats.pendingCount }}</strong></article>
      <article class="card success"><label>在店人数</label><strong>{{ stats.arrivedCount }}</strong></article>
    </section>
  </div>
</template>

<script>
import { get, pickData } from '../../utils/api'

export default {
  name: 'AdminDataView',
  data() {
    return {
      stats: {
        memberCount: 0,
        courseCount: 0,
        orderCount: 0,
        pendingCount: 0,
        arrivedCount: 0
      }
    }
  },
  mounted() {
    this.loadStats()
  },
  methods: {
    async loadStats() {
      try {
        const res = await get('/api/admin/order/stats')
        this.stats = pickData(res) || this.stats
      } catch (error) {
        this.$message.error(error.message || '加载失败')
      }
    }
  }
}
</script>

<style scoped>
.data-view { max-width: 1180px; margin: 0 auto; display: grid; gap: 14px; }
.hero { border-radius: 8px; padding: 20px 22px; background: #fff; border: 1px solid #e5e7eb; }
.hero h2 { margin: 0; color: #0f172a; font-size: 30px; }
.hero p { margin: 8px 0 0; color: #64748b; font-size: 15px; }
.grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.card { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; padding: 18px 16px; display: grid; gap: 10px; min-height: 120px; align-content: center; }
.card label { color: #64748b; font-size: 14px; font-weight: 700; }
.card strong { color: #0f172a; font-size: 38px; line-height: 1; }
.card.success { background: #ecfdf5; border-color: #86efac; }
@media (max-width: 1100px) { .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
@media (max-width: 760px) { .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>

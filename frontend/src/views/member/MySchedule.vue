<template>
  <div class="my-schedule">
    <section class="hero">
      <p>My Schedule</p>
      <h1>我的课表</h1>
      <span>按预约时间展示你的课程安排。</span>
    </section>

    <section class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>预约时间</th>
            <th>课程名称</th>
            <th>课程简介</th>
            <th>时长</th>
            <th>教练</th>
            <th>联系方式</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in pagedRows" :key="row.orderId">
            <td>{{ formatDate(row.orderTime) }}</td>
            <td>{{ row.courseName || '-' }}</td>
            <td>{{ row.courseIntro || '-' }}</td>
            <td>1小时</td>
            <td>{{ row.coachName || '-' }}</td>
            <td>{{ row.coachTel || '-' }}</td>
            <td>{{ row.orderStatus || '-' }}</td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="7" class="empty">暂无预约课程</td>
          </tr>
        </tbody>
      </table>
      <div class="pager" v-if="rows.length">
        <button :disabled="currentPage === 1" @click="currentPage--">上一页</button>
        <span>第 {{ currentPage }} / {{ totalPages }} 页</span>
        <button :disabled="currentPage === totalPages" @click="currentPage++">下一页</button>
        <select v-model.number="pageSize">
          <option :value="5">5条/页</option>
          <option :value="10">10条/页</option>
          <option :value="20">20条/页</option>
        </select>
      </div>
    </section>
  </div>
</template>

<script>
import { get, pickData } from '../../utils/api'

export default {
  name: 'MemberMySchedule',
  data() {
    return {
      rows: [],
      currentPage: 1,
      pageSize: 10
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.rows.length / this.pageSize))
    },
    pagedRows() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.rows.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadSchedule()
  },
  methods: {
    async loadSchedule() {
      const res = await get('/api/member/order/my')
      const list = pickData(res)
      this.rows = Array.isArray(list) ? list : []
      this.currentPage = 1
    },
    formatDate(value) {
      return value ? new Date(value).toLocaleString('zh-CN') : '-'
    }
  }
}
</script>

<style scoped>
.my-schedule { max-width: 1180px; margin: 0 auto; display: grid; gap: 16px; }
.hero { min-height: 150px; border-radius: 8px; padding: 24px; color: #fff; background: linear-gradient(105deg, #111827, #374151 60%, #4b5563); display: flex; flex-direction: column; justify-content: flex-end; }
.hero p { margin: 0 0 8px; color: #7df3d5; font-size: 12px; font-weight: 800; text-transform: uppercase; }
.hero h1 { margin: 0; font-size: 30px; }
.hero span { margin-top: 8px; color: rgba(255, 255, 255, 0.84); }
.table-wrap { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; overflow: auto; }
.table { width: 100%; border-collapse: collapse; min-width: 860px; }
.table th, .table td { padding: 12px 10px; border-bottom: 1px solid #edf2f7; text-align: left; }
.table th { background: #f8fafc; color: #334155; font-weight: 700; }
.empty { text-align: center; color: #94a3b8; }
.pager { margin: 12px; display: flex; gap: 8px; align-items: center; justify-content: flex-end; flex-wrap: wrap; }
.pager button { min-width: 74px; height: 36px; padding: 0 12px; border: 1px solid #cbd5e1; border-radius: 6px; background: #fff; color: #334155; cursor: pointer; font-weight: 600; }
.pager button:disabled { cursor: not-allowed; color: #9ca3af; background: #f3f4f6; border-color: #e5e7eb; opacity: 1; }
.pager span { font-weight: 700; color: #0f172a; min-width: 110px; text-align: center; }
.pager select { min-width: 92px; height: 36px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 8px; background: #fff; color: #334155; }
</style>

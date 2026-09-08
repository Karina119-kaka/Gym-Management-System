<template>
  <div class="attendance-page">
    <section class="panel">
      <div class="head">
        <h2>课程考勤</h2>
        <span>{{ rows.length }} 节</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>上课时间</th>
            <th>会员</th>
            <th>联系方式</th>
            <th>课程</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.orderId">
            <td>{{ formatDate(row.orderTime) }}</td>
            <td>{{ row.memberName || '-' }}</td>
            <td>{{ row.memberTel || '-' }}</td>
            <td>{{ row.courseName || '-' }}</td>
            <td>{{ normalizeStatus(row.orderStatus) }}</td>
            <td>
              <button :disabled="!canAttend(row.orderTime)" @click="setStatus(row, '已到店')">已到店</button>
              <button :disabled="!canAttend(row.orderTime)" @click="setStatus(row, '未到店')">未到店</button>
            </td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="6" class="empty">暂无可考勤课程</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'CoachAttendance',
  data() {
    return { rows: [] }
  },
  mounted() {
    this.loadRows()
  },
  methods: {
    async loadRows() {
      const result = await get('/api/coach/order/attendance')
      const list = pickData(result)
      this.rows = Array.isArray(list) ? list : []
    },
    async setStatus(row, status) {
      try {
        await postJson(`/api/coach/order/attendance/${row.orderId}?status=${encodeURIComponent(status)}`, {}, { method: 'PUT' })
        this.$message.success('更新成功')
        this.loadRows()
      } catch (error) {
        this.$message.error(error.message || '更新失败')
      }
    },
    formatDate(v) {
      return v ? new Date(v).toLocaleString('zh-CN') : '-'
    },
    canAttend(v) {
      if (!v) return false
      const orderTime = new Date(v)
      return orderTime.getTime() <= new Date().getTime()
    },
    normalizeStatus(v) {
      if (v === '棰勭害鎴愬姛') return '预约成功'
      if (v === '宸插埌搴?') return '已到店'
      if (v === '鏈埌搴?') return '未到店'
      return v || '-'
    }
  }
}
</script>

<style scoped>
.attendance-page { max-width: 1180px; margin: 0 auto; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; }
.head { display: flex; justify-content: space-between; margin-bottom: 12px; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid #e5e7eb; padding: 10px 8px; text-align: left; }
button { height: 30px; padding: 0 10px; margin-right: 6px; border: 1px solid #cbd5e1; border-radius: 6px; background: #fff; cursor: pointer; }
button:disabled { opacity: 0.5; cursor: not-allowed; }
.empty { text-align: center; color: #94a3b8; }
</style>


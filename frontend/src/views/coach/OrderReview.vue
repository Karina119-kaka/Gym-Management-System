<template>
  <div class="order-review">
    <section class="panel">
      <div class="head">
        <h2>预约查看</h2>
        <span>{{ rows.length }} 条待处理</span>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>预约时间</th>
            <th>会员</th>
            <th>联系方式</th>
            <th>课程</th>
            <th>简介</th>
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
            <td>{{ row.courseIntro || '-' }}</td>
            <td>{{ row.orderStatus || '-' }}</td>
            <td>
              <button class="ok" @click="audit(row, 'approve')">同意</button>
              <button class="bad" @click="audit(row, 'reject')">拒绝</button>
            </td>
          </tr>
          <tr v-if="!rows.length"><td colspan="7" class="empty">暂无待处理预约</td></tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'

export default {
  name: 'CoachOrderReview',
  data() {
    return { rows: [] }
  },
  mounted() {
    this.loadPending()
  },
  methods: {
    async loadPending() {
      try {
        const result = await get('/api/coach/order/pending')
        const list = pickData(result)
        this.rows = Array.isArray(list) ? list : []
      } catch (error) {
        this.$message.error(error.message || '待处理预约加载失败')
      }
    },
    async audit(row, action) {
      try {
        await postJson(`/api/coach/order/audit/${row.orderId}?action=${action}`, {}, { method: 'PUT' })
        this.$message.success(action === 'approve' ? '已同意预约' : '已拒绝预约')
        this.loadPending()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },
    formatDate(v) {
      return v ? new Date(v).toLocaleString('zh-CN') : '-'
    }
  }
}
</script>

<style scoped>
.order-review { max-width: 1180px; margin: 0 auto; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.head h2 { margin: 0; }
.head span { color: #6b7280; font-size: 13px; font-weight: 700; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid #e5e7eb; padding: 10px 8px; text-align: left; vertical-align: top; }
.ok, .bad { height: 30px; border-radius: 6px; border: none; padding: 0 10px; cursor: pointer; margin-right: 6px; }
.ok { background: #16a34a; color: #fff; }
.bad { background: #dc2626; color: #fff; }
.empty { text-align: center; color: #94a3b8; }
</style>

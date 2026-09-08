<template>
  <div class="order-manage">
    <section class="panel">
      <div class="head">
        <h2>订单管理</h2>
        <select v-model="statusFilter" @change="loadOrders">
          <option value="">全部状态</option>
          <option value="待教练查看">待教练查看</option>
          <option value="预约成功">预约成功</option>
          <option value="已到店">已到店</option>
          <option value="预约失败">预约失败</option>
        </select>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>预约时间</th>
            <th>会员</th>
            <th>教练</th>
            <th>课程</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in pagedRows" :key="row.orderId">
            <td>{{ formatDate(row.orderTime) }}</td>
            <td>{{ row.memberName || '-' }}</td>
            <td>{{ row.coachName || '-' }}</td>
            <td>{{ row.courseName || '-' }}</td>
            <td>{{ row.orderStatus || '-' }}</td>
          </tr>
          <tr v-if="!rows.length">
            <td colspan="5" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>

      <div class="pager" v-if="rows.length">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="rows.length"
          :current-page="page"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          @current-change="page = $event"
          @size-change="onSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script>
import { get, pickData } from '../../utils/api'
import { getPagedRows, resetPageBySize } from '../../utils/pagination'

export default {
  name: 'AdminOrderManage',
  data() {
    return {
      rows: [],
      statusFilter: '',
      page: 1,
      pageSize: 10
    }
  },
  computed: {
    pagedRows() {
      return getPagedRows(this.rows, this.page, this.pageSize)
    }
  },
  mounted() {
    this.loadOrders()
  },
  methods: {
    onSizeChange(size) {
      resetPageBySize(this)(size)
    },
    async loadOrders() {
      try {
        const params = this.statusFilter ? { status: this.statusFilter } : {}
        const res = await get('/api/admin/order/list', params)
        const list = pickData(res)
        this.rows = Array.isArray(list) ? list : []
        this.page = 1
      } catch (error) {
        this.$message.error(error.message || '订单数据加载失败')
      }
    },
    formatDate(value) {
      return value ? new Date(value).toLocaleString('zh-CN') : '-'
    }
  }
}
</script>

<style scoped>
.order-manage { max-width: 1180px; margin: 0 auto; }
.panel { background: #fff; border: 1px solid #e5e7eb; border-radius: 8px; padding: 16px; }
.head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
select { height: 36px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 10px; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { border-bottom: 1px solid #e5e7eb; padding: 10px 8px; text-align: left; }
.empty { text-align: center; color: #94a3b8; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

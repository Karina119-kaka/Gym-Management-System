<template>
  <div class="coach-manage">
    <div class="header">
      <h2>教练管理</h2>
      <el-button type="primary" @click="openAddDialog">添加教练</el-button>
    </div>

    <el-table :data="pagedCoaches" border stripe>
      <el-table-column prop="coachId" label="ID" width="80" />
      <el-table-column prop="coachNo" label="工号" width="120" />
      <el-table-column prop="coachName" label="姓名" width="100" />
      <el-table-column prop="coachSex" label="性别" width="80" />
      <el-table-column prop="coachAge" label="年龄" width="80" />
      <el-table-column prop="coachTel" label="电话" width="130" />
      <el-table-column prop="goodAt" label="擅长领域" min-width="150" />
      <el-table-column prop="coachStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.coachStatus === '启用' ? 'success' : 'danger'">{{ row.coachStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.coachStatus === '启用' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.coachStatus === '启用' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteCoach(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" v-if="coaches.length">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="coaches.length"
        :current-page="page"
        :page-sizes="[5, 10, 20]"
        :page-size="pageSize"
        @current-change="page = $event"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="工号"><el-input v-model="form.coachNo" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.coachName" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.coachSex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="form.coachAge" :min="1" :max="150" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.coachTel" /></el-form-item>
        <el-form-item label="擅长领域"><el-input v-model="form.goodAt" /></el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.coachPwd" type="password" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.coachStatus">
            <el-radio label="启用">启用</el-radio>
            <el-radio label="禁用">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { get, postJson, pickData } from '../../utils/api'
import { getPagedRows, resetPageBySize } from '../../utils/pagination'

export default {
  name: 'CoachManage',
  data() {
    return {
      coaches: [],
      dialogVisible: false,
      dialogTitle: '添加教练',
      isEdit: false,
      form: {
        coachId: null,
        coachNo: '',
        coachName: '',
        coachSex: '男',
        coachAge: 30,
        coachTel: '',
        goodAt: '',
        coachStatus: '启用',
        coachPwd: '123456'
      },
      page: 1,
      pageSize: 10
    }
  },
  computed: {
    pagedCoaches() {
      return getPagedRows(this.coaches, this.page, this.pageSize)
    }
  },
  mounted() {
    this.loadCoaches()
  },
  methods: {
    onSizeChange(size) {
      resetPageBySize(this)(size)
    },
    async loadCoaches() {
      try {
        const result = await get('/api/admin/coach/list')
        this.coaches = Array.isArray(pickData(result)) ? pickData(result) : []
      } catch (error) {
        this.$message.error(error.message || '获取教练列表失败')
      }
    },
    openAddDialog() {
      this.dialogTitle = '添加教练'
      this.isEdit = false
      this.form = {
        coachId: null,
        coachNo: '',
        coachName: '',
        coachSex: '男',
        coachAge: 30,
        coachTel: '',
        goodAt: '',
        coachStatus: '启用',
        coachPwd: '123456'
      }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.dialogTitle = '编辑教练'
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    async submitForm() {
      if (!this.form.coachNo || !this.form.coachName) return this.$message.warning('请填写完整信息')
      try {
        await postJson('/api/admin/coach', this.form, { method: this.isEdit ? 'PUT' : 'POST' })
        this.$message.success('操作成功')
        this.dialogVisible = false
        this.loadCoaches()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },
    async toggleStatus(row) {
      const newStatus = row.coachStatus === '启用' ? '禁用' : '启用'
      try {
        await postJson(`/api/admin/coach/status/${row.coachId}?status=${encodeURIComponent(newStatus)}`, {}, { method: 'PUT' })
        this.$message.success('状态已更新')
        this.loadCoaches()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },
    deleteCoach(row) {
      this.$confirm(`确定删除教练“${row.coachName}”吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await postJson(`/api/admin/coach/${row.coachId}`, {}, { method: 'DELETE' })
          this.$message.success('删除成功')
          this.loadCoaches()
        })
        .catch(() => {})
    },
    formatDate(date) {
      return date ? new Date(date).toLocaleString('zh-CN') : ''
    }
  }
}
</script>

<style scoped>
.coach-manage { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header h2 { margin: 0; color: #333; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

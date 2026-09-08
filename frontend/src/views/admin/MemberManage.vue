<template>
  <div class="member-manage">
    <div class="header">
      <h2>会员管理</h2>
      <el-button type="primary" @click="openAddDialog">添加会员</el-button>
    </div>

    <el-table :data="pagedMembers" border stripe>
      <el-table-column prop="memberId" label="ID" width="80" />
      <el-table-column prop="memberNo" label="会员账号" width="140" />
      <el-table-column prop="memberName" label="姓名" width="110" />
      <el-table-column prop="memberSex" label="性别" width="90" />
      <el-table-column prop="memberAge" label="年龄" width="90" />
      <el-table-column prop="memberTel" label="电话" width="140" />
      <el-table-column prop="cardStatus" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.cardStatus === '正常' ? 'success' : 'danger'">{{ row.cardStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.cardStatus === '正常' ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.cardStatus === '正常' ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteMember(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager" v-if="members.length">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next"
        :total="members.length"
        :current-page="page"
        :page-sizes="[5, 10, 20]"
        :page-size="pageSize"
        @current-change="page = $event"
        @size-change="onSizeChange"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="会员账号"><el-input v-model="form.memberNo" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.memberName" /></el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.memberSex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="form.memberAge" :min="1" :max="150" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.memberTel" /></el-form-item>
        <el-form-item label="初始密码"><el-input v-model="form.memberPwd" type="password" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.cardStatus">
            <el-radio label="正常">正常</el-radio>
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
  name: 'MemberManage',
  data() {
    return {
      members: [],
      dialogVisible: false,
      dialogTitle: '添加会员',
      isEdit: false,
      form: {
        memberId: null,
        memberNo: '',
        memberName: '',
        memberSex: '男',
        memberAge: 25,
        memberTel: '',
        cardStatus: '正常',
        memberPwd: '123456'
      },
      page: 1,
      pageSize: 10
    }
  },
  computed: {
    pagedMembers() {
      return getPagedRows(this.members, this.page, this.pageSize)
    }
  },
  mounted() {
    this.loadMembers()
  },
  methods: {
    onSizeChange(size) {
      resetPageBySize(this)(size)
    },
    async loadMembers() {
      try {
        const result = await get('/api/admin/member/list')
        this.members = Array.isArray(pickData(result)) ? pickData(result) : []
      } catch (error) {
        this.$message.error(error.message || '获取会员列表失败')
      }
    },
    openAddDialog() {
      this.isEdit = false
      this.dialogTitle = '添加会员'
      this.form = {
        memberId: null,
        memberNo: '',
        memberName: '',
        memberSex: '男',
        memberAge: 25,
        memberTel: '',
        cardStatus: '正常',
        memberPwd: '123456'
      }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.dialogTitle = '编辑会员'
      this.form = { ...row, memberPwd: row.memberPwd || '123456' }
      this.dialogVisible = true
    },
    async submitForm() {
      if (!this.form.memberNo || !this.form.memberName) return this.$message.warning('请填写完整信息')
      try {
        await postJson('/api/admin/member', this.form, { method: this.isEdit ? 'PUT' : 'POST' })
        this.$message.success('操作成功')
        this.dialogVisible = false
        this.loadMembers()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },
    async toggleStatus(row) {
      const newStatus = row.cardStatus === '正常' ? '禁用' : '正常'
      try {
        await postJson(`/api/admin/member/status/${row.memberId}?status=${encodeURIComponent(newStatus)}`, {}, { method: 'PUT' })
        this.$message.success('状态已更新')
        this.loadMembers()
      } catch (error) {
        this.$message.error(error.message || '更新失败')
      }
    },
    deleteMember(row) {
      this.$confirm(`确定删除会员“${row.memberName}”吗？`, '提示', { type: 'warning' })
        .then(async () => {
          await postJson(`/api/admin/member/${row.memberId}`, {}, { method: 'DELETE' })
          this.$message.success('删除成功')
          this.loadMembers()
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
.member-manage { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header h2 { margin: 0; color: #333; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

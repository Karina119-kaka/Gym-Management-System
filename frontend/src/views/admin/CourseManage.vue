<template>
  <div class="course-manage">
    <div class="header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="openAddDialog">添加课程</el-button>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="全部课程" name="all">
        <el-table :data="pagedCourses" border stripe>
          <el-table-column prop="courseId" label="ID" width="80" />
          <el-table-column prop="courseName" label="课程名称" width="150" />
          <el-table-column prop="coachId" label="教练ID" width="100" />
          <el-table-column prop="coursePrice" label="价格" width="100" />
          <el-table-column prop="courseIntro" label="课程简介" min-width="180" show-overflow-tooltip />
          <el-table-column prop="courseDetail" label="详细描述" min-width="240" show-overflow-tooltip />
          <el-table-column prop="saleStatus" label="状态" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="220">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button size="small" type="success" @click="setStatus(row, '上架')">上架</el-button>
              <el-button size="small" type="warning" @click="setStatus(row, '下架')">下架</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager" v-if="courses.length">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="courses.length"
            :current-page="pageAll"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSizeAll"
            @current-change="pageAll = $event"
            @size-change="onAllSizeChange"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="待审核" name="pending">
        <el-table :data="pagedPendingCourses" border stripe>
          <el-table-column prop="courseId" label="ID" width="80" />
          <el-table-column prop="courseName" label="课程名称" width="150" />
          <el-table-column prop="coachId" label="教练ID" width="100" />
          <el-table-column prop="coursePrice" label="价格" width="100" />
          <el-table-column prop="courseIntro" label="课程简介" min-width="180" show-overflow-tooltip />
          <el-table-column prop="courseDetail" label="详细描述" min-width="240" show-overflow-tooltip />
          <el-table-column prop="saleStatus" label="状态" width="120" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="auditCourse(row, '已通过')">通过</el-button>
              <el-button size="small" type="danger" @click="auditCourse(row, '已拒绝')">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager" v-if="pendingCourses.length">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :total="pendingCourses.length"
            :current-page="pagePending"
            :page-sizes="[5, 10, 20]"
            :page-size="pageSizePending"
            @current-change="pagePending = $event"
            @size-change="onPendingSizeChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="课程名称"><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="教练">
          <el-select v-model="form.coachId" style="width: 100%">
            <el-option v-for="coach in coaches" :key="coach.coachId" :label="coach.coachName" :value="coach.coachId" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="form.coursePrice" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="课程简介"><el-input v-model="form.courseIntro" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="详细描述"><el-input v-model="form.courseDetail" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态"><el-input v-model="form.saleStatus" /></el-form-item>
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
  name: 'CourseManage',
  data() {
    return {
      activeTab: 'all',
      courses: [],
      pendingCourses: [],
      coaches: [],
      dialogVisible: false,
      dialogTitle: '添加课程',
      isEdit: false,
      form: { courseId: null, courseName: '', coachId: null, coursePrice: 0, courseIntro: '', courseDetail: '', saleStatus: '待审核' },
      pageAll: 1,
      pageSizeAll: 10,
      pagePending: 1,
      pageSizePending: 10
    }
  },
  computed: {
    pagedCourses() {
      return getPagedRows(this.courses, this.pageAll, this.pageSizeAll)
    },
    pagedPendingCourses() {
      return getPagedRows(this.pendingCourses, this.pagePending, this.pageSizePending)
    }
  },
  mounted() {
    this.loadAll()
  },
  methods: {
    onAllSizeChange(size) {
      resetPageBySize(this, 'pageSizeAll', 'pageAll')(size)
    },
    onPendingSizeChange(size) {
      resetPageBySize(this, 'pageSizePending', 'pagePending')(size)
    },
    async loadAll() {
      await Promise.all([this.loadCourses(), this.loadPendingCourses(), this.loadCoaches()])
    },
    async loadCourses() {
      const res = await get('/api/admin/course/list')
      this.courses = Array.isArray(pickData(res)) ? pickData(res) : []
    },
    async loadPendingCourses() {
      const res = await get('/api/admin/course/pending')
      this.pendingCourses = Array.isArray(pickData(res)) ? pickData(res) : []
    },
    async loadCoaches() {
      const res = await get('/api/admin/coach/list')
      this.coaches = Array.isArray(pickData(res)) ? pickData(res) : []
    },
    openAddDialog() {
      this.isEdit = false
      this.dialogTitle = '添加课程'
      this.form = { courseId: null, courseName: '', coachId: null, coursePrice: 0, courseIntro: '', courseDetail: '', saleStatus: '待审核' }
      this.dialogVisible = true
    },
    openEditDialog(row) {
      this.isEdit = true
      this.dialogTitle = '编辑课程'
      this.form = { ...row }
      this.dialogVisible = true
    },
    async submitForm() {
      if (!this.form.courseName || !this.form.coachId) return this.$message.warning('请填写完整信息')
      try {
        await postJson('/api/admin/course', this.form, { method: this.isEdit ? 'PUT' : 'POST' })
        this.$message.success('操作成功')
        this.dialogVisible = false
        this.loadAll()
      } catch (error) {
        this.$message.error(error.message || '操作失败')
      }
    },
    async auditCourse(row, status) {
      try {
        await postJson(`/api/admin/course/audit/${row.courseId}?status=${encodeURIComponent(status)}`, {}, { method: 'PUT' })
        this.$message.success('审核成功')
        this.loadAll()
      } catch (error) {
        this.$message.error(error.message || '审核失败')
      }
    },
    async setStatus(row, status) {
      try {
        await postJson(`/api/admin/course/status/${row.courseId}?status=${encodeURIComponent(status)}`, {}, { method: 'PUT' })
        this.$message.success('状态已更新')
        this.loadAll()
      } catch (error) {
        this.$message.error(error.message || '状态更新失败')
      }
    },
    formatDate(date) {
      return date ? new Date(date).toLocaleString('zh-CN') : ''
    }
  }
}
</script>

<style scoped>
.course-manage { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header h2 { margin: 0; color: #333; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>

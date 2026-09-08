<template>
  <div class="clockin-page">
    <section class="hero">
      <h2>健身打卡</h2>
      <div class="stats">本月打卡：<strong>{{ monthCount }}</strong> 次</div>
      <button class="clock-btn" :disabled="todayClocked" @click="handleClockIn">
        {{ todayClocked ? '今日已打卡' : '今日打卡' }}
      </button>
    </section>

    <section class="panel">
      <h3>训练数据录入</h3>
      <p class="hint">BMI 公式：体重(kg) / 身高²(m²)</p>
      <div class="form-grid">
        <input v-model.number="metricsForm.weightKg" placeholder="本次体重 (kg)" />
        <input v-model.number="metricsForm.bodyFatPct" placeholder="本次体脂 (%)" />
        <input v-model.number="metricsForm.bmi" placeholder="本次 BMI" />
        <input v-model.number="metricsForm.sessionCalories" placeholder="本次消耗卡路里 (kcal)" />
        <select v-model="metricsForm.dataSource">
          <option value="manual">手动录入</option>
          <option value="device">设备同步</option>
        </select>
      </div>
      <button class="save-btn" @click="saveMetrics">保存本次数据</button>
    </section>

    <section class="panel">
      <h3>打卡记录</h3>
      <table class="record-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>时间</th>
            <th>体重</th>
            <th>体脂</th>
            <th>BMI</th>
            <th>卡路里</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in pagedRecords" :key="r.clockId || r.clock_id">
            <td>{{ field(r, 'clockId') }}</td>
            <td>{{ format(field(r, 'clockTime')) }}</td>
            <td>{{ field(r, 'weightKg') }}</td>
            <td>{{ field(r, 'bodyFatPct') }}</td>
            <td>{{ field(r, 'bmi') }}</td>
            <td>{{ field(r, 'sessionCalories') }}</td>
          </tr>
          <tr v-if="!records.length">
            <td colspan="6" class="empty">暂无打卡记录</td>
          </tr>
        </tbody>
      </table>

      <div class="pager" v-if="records.length">
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
import { get, postJson, postForm, pickData } from '../../utils/api'

export default {
  name: 'MemberClockIn',
  data() {
    return {
      records: [],
      currentPage: 1,
      pageSize: 10,
      todayClocked: false,
      monthCount: 0,
      metricsForm: {
        weightKg: null,
        bodyFatPct: null,
        bmi: null,
        sessionCalories: 0,
        dataSource: 'manual'
      }
    }
  },
  computed: {
    totalPages() {
      return Math.max(1, Math.ceil(this.records.length / this.pageSize))
    },
    pagedRecords() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.records.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadData()
    this.loadLatestMetrics()
  },
  methods: {
    async loadData() {
      try {
        const res = await get('/api/member/clockin')
        const data = pickData(res) || {}
        this.records = Array.isArray(data.records) ? data.records : []
        this.todayClocked = !!data.todayClocked
        this.monthCount = Number(data.monthCount || 0)
        this.currentPage = 1
      } catch (error) {
        this.$message.error(error.message || '打卡记录加载失败')
      }
    },
    async loadLatestMetrics() {
      try {
        const res = await get('/api/member/dashboard')
        const latest = (pickData(res) || {}).latestMetrics || {}
        this.metricsForm = {
          ...this.metricsForm,
          weightKg: latest.weightKg ?? null,
          bodyFatPct: latest.bodyFatPct ?? null,
          bmi: latest.bmi ?? null,
          sessionCalories: latest.sessionCalories ?? 0,
          dataSource: latest.dataSource || 'manual'
        }
      } catch (_) {
        // 看板数据加载失败不影响打卡主流程
      }
    },
    async handleClockIn() {
      try {
        await postForm('/api/member/clockin', {}, { method: 'POST' })
        this.$message.success('打卡成功')
        this.loadData()
      } catch (error) {
        this.$message.error(error.message || '打卡失败')
      }
    },
    async saveMetrics() {
      try {
        await postJson('/api/member/dashboard/metrics', this.metricsForm, { method: 'POST' })
        this.$message.success('本次训练数据已保存')
        this.loadData()
      } catch (error) {
        this.$message.error(error.message || '保存失败')
      }
    },
    format(value) {
      return value ? new Date(value).toLocaleString('zh-CN') : '-'
    },
    field(item, name) {
      if (!item) return '-'
      if (item[name] !== undefined) return item[name]
      const snake = name.replace(/[A-Z]/g, (m) => `_${m.toLowerCase()}`)
      return item[snake] ?? '-'
    }
  }
}
</script>

<style scoped>
.clockin-page { max-width: 980px; margin: 0 auto; display: grid; gap: 16px; }
.hero { padding: 20px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
.stats { margin: 8px 0 14px; color: #374151; }
.clock-btn, .save-btn { height: 42px; padding: 0 18px; border: none; border-radius: 6px; background: #3f4652; color: #fff; font-weight: 700; cursor: pointer; }
.clock-btn:disabled { background: #9ca3af; cursor: not-allowed; }
.panel { padding: 20px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
.hint { margin: 6px 0 12px; color: #64748b; font-size: 13px; }
.form-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 12px; }
.form-grid input, .form-grid select { height: 40px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 10px; }
.record-table { width: 100%; border-collapse: collapse; }
.record-table th, .record-table td { border-bottom: 1px solid #e5e7eb; padding: 10px 8px; text-align: left; }
.empty { text-align: center; color: #94a3b8; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; align-items: center; gap: 8px; flex-wrap: wrap; }
.pager button { min-width: 74px; height: 36px; padding: 0 12px; border: 1px solid #cbd5e1; border-radius: 6px; background: #fff; color: #334155; cursor: pointer; font-weight: 600; }
.pager button:disabled { cursor: not-allowed; color: #9ca3af; background: #f3f4f6; border-color: #e5e7eb; opacity: 1; }
.pager span { font-weight: 700; color: #0f172a; min-width: 110px; text-align: center; }
.pager select { min-width: 92px; height: 36px; border: 1px solid #cbd5e1; border-radius: 6px; padding: 0 8px; background: #fff; color: #334155; }
@media (max-width: 900px) { .form-grid { grid-template-columns: 1fr 1fr; } }
@media (max-width: 620px) { .form-grid { grid-template-columns: 1fr; } }
</style>

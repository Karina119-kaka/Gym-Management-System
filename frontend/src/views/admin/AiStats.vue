<template>
  <div class="ai-stats">
    <section class="hero">
      <div>
        <p class="eyebrow">Admin Console</p>
        <h1>AI 调用统计</h1>
        <span>监控会员 AI 健身助手的调用量、成本、成功率与降级情况。</span>
      </div>
      <button type="button" class="refresh-btn" :disabled="loading" @click="loadAll">
        {{ loading ? '刷新中…' : '刷新数据' }}
      </button>
    </section>

    <section class="grid">
      <article class="card">
        <label>今日调用</label>
        <strong>{{ s.todayCount }}</strong>
        <p>{{ s.todayMembers }} 位会员使用</p>
      </article>
      <article class="card">
        <label>今日成功率</label>
        <strong>{{ s.todaySuccessRate }}%</strong>
        <p>成功 {{ s.todaySuccess }} · 失败 {{ s.todayFail }}</p>
      </article>
      <article class="card warn">
        <label>今日降级次数</label>
        <strong>{{ s.todayFallback }}</strong>
        <p>大模型失败，回退本地知识库</p>
      </article>
      <article class="card">
        <label>今日成本</label>
        <strong>¥{{ fmtMoney(s.todayCost) }}</strong>
        <p>消耗 {{ fmtNum(s.todayTokens) }} tokens</p>
      </article>
      <article class="card success">
        <label>本月调用</label>
        <strong>{{ s.monthCount }}</strong>
        <p>累计 ¥{{ fmtMoney(s.monthCost) }} · 成功率 {{ s.monthSuccessRate }}%</p>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="panel">
        <div class="panel-head">
          <h2>近 {{ days }} 天调用趋势</h2>
          <div class="range-btns">
            <button
              v-for="d in [7, 14, 30]"
              :key="d"
              type="button"
              :class="{ active: days === d }"
              @click="changeDays(d)"
            >{{ d }}天</button>
          </div>
        </div>
        <div class="chart">
          <div v-for="p in series" :key="p.day" class="bar-col" :title="`${p.label}：${p.count} 次，¥${fmtMoney(p.cost)}，失败 ${p.failCount}`">
            <div class="bar-track-v">
              <div class="bar-fill-v" :style="{ height: barHeight(p.count) + '%' }">
                <span v-if="p.count" class="bar-num">{{ p.count }}</span>
              </div>
            </div>
            <label>{{ p.label }}</label>
          </div>
        </div>
      </article>

      <article class="panel">
        <div class="panel-head">
          <h2>本月高频会员 TOP10</h2>
          <span>调用次数 / 成本</span>
        </div>
        <div v-if="!topMembers.length" class="empty">本月暂无 AI 调用记录</div>
        <ol v-else class="rank-list">
          <li v-for="m in topMembers" :key="m.memberId">
            <span class="rank-name">{{ m.memberName || '未命名' }}（{{ m.memberNo || m.memberId }}）</span>
            <span class="rank-meta">{{ m.count }} 次 · ¥{{ fmtMoney(m.cost) }}</span>
          </li>
        </ol>
      </article>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>最近调用记录</h2>
        <span>最新 {{ recent.length }} 条</span>
      </div>
      <div class="table-wrap">
        <table class="log-table">
          <thead>
            <tr>
              <th>时间</th>
              <th>会员</th>
              <th>问题摘要</th>
              <th>来源</th>
              <th>状态</th>
              <th class="num">Tokens</th>
              <th class="num">成本</th>
              <th class="num">耗时</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="!recent.length">
              <td colspan="8" class="empty-cell">暂无调用记录</td>
            </tr>
            <tr v-for="r in recent" :key="r.logId">
              <td class="nowrap">{{ fmtTime(r.createTime) }}</td>
              <td>{{ r.memberName || '未命名' }}<div class="sub">{{ r.memberNo }}</div></td>
              <td class="question-cell" :title="r.questionSummary">{{ r.questionSummary || '—' }}</td>
              <td><span class="src-tag" :class="sourceClass(r.source)">{{ sourceLabel(r.source) }}</span></td>
              <td>
                <span v-if="r.success" class="ok">成功</span>
                <span v-else class="fail" :title="r.errorMessage">失败</span>
              </td>
              <td class="num">{{ r.totalTokens ? fmtNum(r.totalTokens) : '—' }}</td>
              <td class="num">{{ r.costYuan ? '¥' + fmtMoney(r.costYuan) : '—' }}</td>
              <td class="num">{{ r.elapsedMs != null ? (r.elapsedMs / 1000).toFixed(1) + 's' : '—' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

<script>
import { get, pickData } from '../../utils/api'

export default {
  name: 'AdminAiStats',
  data() {
    return {
      loading: false,
      days: 7,
      s: {
        todayCount: 0,
        todaySuccess: 0,
        todayFail: 0,
        todayFallback: 0,
        todayCost: 0,
        todayTokens: 0,
        todayMembers: 0,
        todayAvgMs: 0,
        todaySuccessRate: 100,
        monthCount: 0,
        monthFail: 0,
        monthCost: 0,
        monthTokens: 0,
        monthSuccessRate: 100
      },
      series: [],
      recent: [],
      topMembers: []
    }
  },
  mounted() {
    this.loadAll()
  },
  methods: {
    async loadAll() {
      this.loading = true
      try {
        await Promise.all([this.loadSummary(), this.loadSeries(), this.loadRecent(), this.loadTop()])
      } finally {
        this.loading = false
      }
    },
    async loadSummary() {
      const res = await get('/api/admin/ai/stats/summary')
      this.s = { ...this.s, ...(pickData(res) || {}) }
    },
    async loadSeries() {
      const res = await get('/api/admin/ai/stats/daily?days=' + this.days)
      this.series = pickData(res) || []
    },
    async loadRecent() {
      const res = await get('/api/admin/ai/stats/recent?limit=30')
      this.recent = pickData(res) || []
    },
    async loadTop() {
      const res = await get('/api/admin/ai/stats/top-members')
      this.topMembers = pickData(res) || []
    },
    changeDays(d) {
      this.days = d
      this.loadSeries()
    },
    barHeight(count) {
      const max = Math.max(1, ...this.series.map(p => Number(p.count) || 0))
      return Math.max(count ? 6 : 2, Math.round((Number(count) || 0) / max * 100))
    },
    fmtMoney(v) {
      return Number(v || 0).toFixed(4).replace(/0+$/, '').replace(/\.$/, '')
    },
    fmtNum(v) {
      return Number(v || 0).toLocaleString('zh-CN')
    },
    fmtTime(t) {
      if (!t) return '—'
      const d = new Date(typeof t === 'string' ? t.replace(' ', 'T') : t)
      if (isNaN(d.getTime())) return String(t).slice(5, 16)
      const p = n => String(n).padStart(2, '0')
      return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
    },
    sourceLabel(s) {
      return ({ llm: '大模型', local: '本地', fallback: '降级', 'llm-stream': '大模型', chitchat: '闲聊' })[s] || (s || '—')
    },
    sourceClass(s) {
      if (s === 'fallback') return 'src-fb'
      if (s === 'local' || s === 'chitchat') return 'src-local'
      if (s === 'llm' || s === 'llm-stream') return 'src-llm'
      return ''
    }
  }
}
</script>

<style scoped>
.ai-stats { max-width: 1180px; margin: 0 auto; display: grid; gap: 16px; }
.hero { border-radius: 8px; padding: 24px; background: linear-gradient(135deg, #1f2937, #475569 62%, #64748b); color: #fff; display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; }
.eyebrow { margin: 0 0 8px; color: #7df3d5; font-size: 12px; font-weight: 800; text-transform: uppercase; }
.hero h1 { margin: 0; font-size: 32px; }
.hero span { display: block; margin-top: 8px; color: rgba(255, 255, 255, 0.82); }
.refresh-btn { padding: 9px 18px; border-radius: 6px; border: 1px solid rgba(255,255,255,.4); background: rgba(255,255,255,.12); color: #fff; font-weight: 700; cursor: pointer; white-space: nowrap; }
.refresh-btn:hover { background: rgba(255,255,255,.22); }
.refresh-btn:disabled { opacity: .5; cursor: not-allowed; }

.grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 12px; }
.card { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; padding: 18px 16px; display: grid; gap: 8px; min-height: 120px; align-content: center; }
.card label { color: #64748b; font-size: 14px; font-weight: 700; }
.card strong { color: #0f172a; font-size: 32px; line-height: 1; }
.card p { margin: 0; color: #94a3b8; font-size: 12px; }
.card.success { background: #ecfdf5; border-color: #86efac; }
.card.warn { background: #fffbeb; border-color: #fde68a; }

.dashboard-grid { display: grid; grid-template-columns: 1.3fr 0.7fr; gap: 12px; }
.panel { border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; padding: 18px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.panel-head h2 { margin: 0; font-size: 18px; color: #111827; }
.panel-head span { color: #6b7280; font-size: 13px; }

.range-btns { display: flex; gap: 6px; }
.range-btns button { padding: 4px 12px; border: 1px solid #d1d5db; border-radius: 6px; background: #fff; color: #475569; font-size: 12px; cursor: pointer; }
.range-btns button.active { background: #3f4652; color: #fff; border-color: #3f4652; }

.chart { display: flex; align-items: flex-end; gap: 6px; height: 220px; padding-top: 10px; }
.bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 8px; height: 100%; }
.bar-track-v { flex: 1; width: 100%; max-width: 38px; display: flex; align-items: flex-end; background: #f8fafc; border-radius: 6px; }
.bar-fill-v { width: 100%; min-height: 2px; background: linear-gradient(180deg, #6366f1, #818cf8); border-radius: 6px 6px 0 0; position: relative; transition: height .3s ease; }
.bar-num { position: absolute; top: -18px; left: 50%; transform: translateX(-50%); font-size: 11px; color: #4338ca; font-weight: 700; }
.bar-col label { font-size: 11px; color: #94a3b8; }

.empty, .empty-cell { color: #94a3b8; font-size: 13px; text-align: center; padding: 20px 0; }
.rank-list { margin: 0; padding: 0; list-style: none; display: grid; gap: 10px; }
.rank-list li { display: flex; justify-content: space-between; gap: 8px; padding: 8px 10px; background: #f8fafc; border-radius: 6px; font-size: 13px; }
.rank-name { color: #1f2937; font-weight: 700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-meta { color: #6b7280; white-space: nowrap; }

.table-wrap { overflow-x: auto; }
.log-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.log-table th, .log-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; text-align: left; vertical-align: top; }
.log-table th { color: #64748b; font-weight: 700; background: #f8fafc; white-space: nowrap; }
.log-table td.num, .log-table th.num { text-align: right; white-space: nowrap; }
.nowrap { white-space: nowrap; color: #64748b; }
.sub { color: #94a3b8; font-size: 11px; }
.question-cell { max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #334155; }
.ok { color: #15803d; font-weight: 700; }
.fail { color: #dc2626; font-weight: 700; cursor: help; }
.src-tag { padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; }
.src-llm { background: #eef2ff; color: #4338ca; }
.src-local { background: #f1f5f9; color: #475569; }
.src-fb { background: #fffbeb; color: #b45309; }

@media (max-width: 1100px) {
  .grid { grid-template-columns: repeat(3, 1fr); }
  .dashboard-grid { grid-template-columns: 1fr; }
}
@media (max-width: 760px) {
  .hero { flex-direction: column; align-items: flex-start; }
  .grid { grid-template-columns: repeat(2, 1fr); }
}
</style>

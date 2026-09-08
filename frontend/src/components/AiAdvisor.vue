<template>
  <section class="ai-advisor">
    <header class="ai-head">
      <div class="ai-title">
        <h2>AI 健身助手</h2>
        <p>基于你的身体数据与打卡记录，解答训练、饮食与恢复问题</p>
      </div>
      <span class="source-tag" :title="sourceTip">{{ providerLabel }}</span>
    </header>

    <div class="chips">
      <button v-for="item in presets" :key="item" type="button" class="chip" @click="ask(item)">
        {{ item }}
      </button>
    </div>

    <div class="input-row">
      <textarea
        v-model="question"
        rows="2"
        :disabled="loading"
        placeholder="例如：我最近想减脂，每周能练 3 天，怎么安排？"
        @keydown.enter.exact.prevent="send"
      ></textarea>
      <button type="button" class="send-btn" :disabled="loading || !question.trim()" @click="send">
        {{ loading ? '思考中…' : '发送' }}
      </button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="context && (context.bmi || context.weightKg)" class="context-row">
      <span v-if="context.weightKg">体重 {{ context.weightKg }} kg</span>
      <span v-if="context.bmi">BMI {{ Number(context.bmi).toFixed(1) }}（{{ context.bmiCategory }}）</span>
      <span>本月训练 {{ context.monthSessions || 0 }} 次</span>
      <span>本月消耗 {{ context.monthCalories || 0 }} kcal</span>
    </div>

    <div v-if="answer" class="answer">
      <div class="answer-head">
        <strong>建议</strong>
        <span>{{ sourceLabel }}</span>
      </div>
      <div class="answer-body">{{ answer }}</div>
      <ul v-if="tips.length" class="tips">
        <li v-for="tip in tips" :key="tip">{{ tip }}</li>
      </ul>
    </div>
  </section>
</template>

<script>
import { get, postJson, pickData } from '../utils/api'

export default {
  name: 'AiAdvisor',
  data() {
    return {
      question: '',
      loading: false,
      error: '',
      answer: '',
      sourceLabel: '',
      providerLabel: '本地知识库',
      tips: [],
      context: null,
      presets: [
        '我想减脂，怎么安排训练和饮食？',
        '增肌期每天要吃多少蛋白质？',
        '我的 BMI 正常吗？该怎么调整？',
        '训练后拉伸多久，怎么做？'
      ]
    }
  },
  computed: {
    sourceTip() {
      return this.providerLabel === 'AI 大模型'
        ? '已接入大模型，回答更灵活'
        : '内置健身知识库，无需联网即可使用'
    }
  },
  mounted() {
    this.loadEngineInfo()
  },
  methods: {
    async loadEngineInfo() {
      try {
        const res = await get('/api/member/ai/info')
        const info = pickData(res) || {}
        if (info.label) this.providerLabel = info.label
      } catch (error) {
        this.providerLabel = '本地知识库'
      }
    },
    ask(text) {
      this.question = text
      this.send()
    },
    async send() {
      const text = this.question.trim()
      if (!text || this.loading) return
      this.loading = true
      this.error = ''
      try {
        const res = await postJson('/api/member/ai/advice', { question: text })
        const data = pickData(res) || {}
        this.answer = data.answer || '暂时没有拿到建议，请换个问法试试。'
        this.sourceLabel = data.sourceLabel || ''
        this.tips = Array.isArray(data.tips) ? data.tips : []
        this.context = data.context || null
      } catch (error) {
        this.error = error.message || 'AI 服务暂时不可用，请稍后再试'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.ai-advisor { padding: 18px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
.ai-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 14px; }
.ai-title h2 { margin: 0; font-size: 20px; color: #111827; }
.ai-title p { margin: 6px 0 0; color: #6b7280; font-size: 13px; }
.source-tag { padding: 4px 10px; border-radius: 999px; background: #eef2ff; color: #4338ca; font-size: 12px; font-weight: 700; white-space: nowrap; }

.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.chip { height: 32px; padding: 0 12px; border: 1px solid #dbeafe; border-radius: 999px; background: #f8fafc; color: #334155; cursor: pointer; font-size: 13px; }
.chip:hover { background: #eef2ff; color: #3730a3; }

.input-row { display: grid; grid-template-columns: 1fr 92px; gap: 10px; align-items: stretch; }
.input-row textarea { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; resize: vertical; font-size: 14px; line-height: 1.5; }
.send-btn { border: none; border-radius: 6px; background: #3f4652; color: #fff; font-weight: 700; cursor: pointer; }
.send-btn:disabled { background: #9ca3af; cursor: not-allowed; }

.error { margin: 10px 0 0; color: #dc2626; font-size: 13px; }
.context-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.context-row span { padding: 3px 9px; border-radius: 999px; background: #f1f5f9; color: #475569; font-size: 12px; font-weight: 700; }

.answer { margin-top: 14px; padding: 14px; border-radius: 6px; background: #f8fafc; border: 1px solid #e5e7eb; }
.answer-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.answer-head strong { color: #0f172a; }
.answer-head span { color: #6b7280; font-size: 12px; font-weight: 700; }
.answer-body { white-space: pre-wrap; color: #1f2937; font-size: 14px; line-height: 1.75; }
.tips { margin: 12px 0 0; padding-left: 18px; color: #6b7280; font-size: 12px; line-height: 1.8; }

@media (max-width: 640px) {
  .input-row { grid-template-columns: 1fr; }
  .send-btn { height: 42px; }
}
</style>

<template>
  <section class="ai-advisor">
    <header class="ai-head">
      <div class="ai-title">
        <h2>AI 健身助手</h2>
        <p>基于你的身体数据与打卡记录，解答训练、饮食与恢复问题</p>
      </div>
      <div class="head-right">
        <span class="source-tag" :title="sourceTip">{{ providerLabel }}</span>
        <span v-if="dailyLimit" class="quota-tag" :title="'今日 AI 咨询剩余次数（上限 ' + dailyLimit + ' 次）'">
          今日剩余 {{ remaining === null ? dailyLimit : remaining }}/{{ dailyLimit }}
        </span>
        <button v-if="messages.length" type="button" class="clear-btn" @click="clearConversation">清空对话</button>
      </div>
    </header>

    <div class="chips">
      <button v-for="item in presets" :key="item" type="button" class="chip" :disabled="loading" @click="ask(item)">
        {{ item }}
      </button>
    </div>

    <div ref="historyRef" class="history">
      <div v-if="!messages.length && !loading" class="empty-hint">
        告诉我你的目标，或点击上面的预设问题开始对话。
      </div>

      <div
        v-for="msg in messages"
        :key="msg.id"
        class="msg"
        :class="msg.role === 'user' ? 'msg-user' : 'msg-ai'"
      >
        <div class="bubble">
          <div v-if="msg.role === 'user'" class="bubble-text">{{ msg.content }}</div>
          <template v-else>
            <div v-if="msg.fallback" class="fallback-badge">大模型暂时不可用，已为你切换到本地知识库</div>
            <!-- 流式输出中：用等宽预排版文本逐字显示，结束后再整体渲染 Markdown -->
            <div v-if="msg.streaming" class="bubble-stream">
              <span v-if="!msg.content" class="typing">思考中…</span>{{ msg.content }}<span v-if="msg.content" class="cursor">▍</span>
            </div>
            <div v-else class="bubble-md" v-html="renderMarkdown(msg.content)"></div>
            <div v-if="msg.cancelled && !msg.content" class="bubble-error">已停止生成，未收到内容。</div>
            <div v-else-if="msg.cancelled" class="cancelled-note">（已停止生成，以上为部分内容）</div>
            <div v-if="msg.error" class="bubble-error">{{ msg.error }}</div>
          </template>
        </div>
      </div>
    </div>

    <div class="input-row">
      <textarea
        v-model="question"
        rows="2"
        :disabled="loading"
        placeholder="例如：我最近想减脂，每周能练 3 天，怎么安排？&#10;Enter 发送，Shift+Enter 换行"
        @keydown.enter.exact.prevent="send"
      ></textarea>
      <button
        v-if="!loading"
        type="button"
        class="send-btn"
        :disabled="!question.trim()"
        @click="send"
      >发送</button>
      <button v-else type="button" class="send-btn stop-btn" @click="stopGeneration">停止</button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="lastContext && (lastContext.bmi || lastContext.weightKg)" class="context-row">
      <span v-if="lastContext.weightKg">体重 {{ lastContext.weightKg }} kg</span>
      <span v-if="lastContext.bmi">BMI {{ Number(lastContext.bmi).toFixed(1) }}（{{ lastContext.bmiCategory }}）</span>
      <span>本月训练 {{ lastContext.monthSessions || 0 }} 次</span>
      <span>本月消耗 {{ lastContext.monthCalories || 0 }} kcal</span>
    </div>

    <ul v-if="lastTips.length" class="tips">
      <li v-for="tip in lastTips" :key="tip">{{ tip }}</li>
    </ul>
  </section>
</template>

<script>
import { get, postJson, pickData } from '../utils/api'
import { marked } from 'marked'
import DOMPurify from 'dompurify'

const STORAGE_PREFIX = 'ai_advisor_history_'
const MAX_STORED_TURNS = 20
const MAX_HISTORY_FOR_REQUEST = 10

marked.setOptions({ async: false, breaks: true, gfm: true })

export default {
  name: 'AiAdvisor',
  data() {
    return {
      question: '',
      loading: false,
      error: '',
      providerLabel: '本地知识库',
      messages: [],
      lastContext: null,
      lastTips: [],
      memberId: null,
      dailyLimit: 0,
      remaining: null,
      abortController: null,
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
    },
    storageKey() {
      return STORAGE_PREFIX + (this.memberId || 'anon')
    }
  },
  mounted() {
    this.loadEngineInfo()
    this.loadMemberId().finally(() => this.restoreHistory())
  },
  methods: {
    async loadEngineInfo() {
      try {
        const res = await get('/api/member/ai/info')
        const info = pickData(res) || {}
        if (info.label) this.providerLabel = info.label
        if (info.dailyLimit) this.dailyLimit = info.dailyLimit
      } catch (error) {
        this.providerLabel = '本地知识库'
      }
    },
    async loadMemberId() {
      try {
        const res = await get('/api/member/info')
        const m = pickData(res)
        if (m && m.memberId) this.memberId = m.memberId
      } catch (_) {}
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
      this.pushMessage('user', text)
      this.question = ''
      const history = this.buildHistoryForRequest()
      // 占位的 AI 流式消息，后续 chunk 不断追加到 content
      const aiMsg = this.pushMessage('assistant', '', { streaming: true })
      const controller = new AbortController()
      this.abortController = controller

      try {
        await this.streamAdvice(text, history, aiMsg, controller)
      } catch (err) {
        if (err && err.name === 'AbortError') {
          // 用户主动停止：保留已生成的部分内容
          aiMsg.cancelled = true
        } else {
          // 流式通道异常（如旧后端/网络问题），回退到同步接口
          await this.fallbackSyncAdvice(text, history, aiMsg, err)
        }
      } finally {
        // 结束流式态：若一条内容都没拿到且没有错误提示，给出兜底文案
        if (!aiMsg.content && !aiMsg.error && !aiMsg.cancelled) {
          aiMsg.content = '暂时没有拿到建议，请换个问法试试。'
        }
        aiMsg.streaming = false
        this.loading = false
        this.abortController = null
        this.persistHistory()
        this.$nextTick(this.scrollToBottom)
      }
    },
    // 读取 SSE 流并解析 event/data，逐字更新占位消息
    async streamAdvice(text, history, aiMsg, controller) {
      const res = await fetch('/api/member/ai/advice/stream', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: text, history }),
        signal: controller.signal
      })
      if (!res.ok || !res.body) {
        throw new Error('STREAM_UNAVAILABLE_' + res.status)
      }
      const reader = res.body.getReader()
      const decoder = new TextDecoder('utf-8')
      let buffer = ''
      try {
        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          // SSE 事件以空行（\n\n）分隔
          const parts = buffer.split('\n\n')
          buffer = parts.pop() || ''
          for (const raw of parts) {
            this.handleSseEvent(raw, aiMsg)
            if (aiMsg.error) return // 服务端报错，停止读取
          }
        }
      } finally {
        // 主动关闭可能悬挂的流（用户停止/服务端 error），避免连接被浏览器标记为 ERR_ABORTED
        if (!controller.signal.aborted) {
          try { await reader.cancel() } catch (_) { /* 已关闭则忽略 */ }
        }
        try { reader.releaseLock() } catch (_) { /* ignore */ }
      }
    },
    handleSseEvent(raw, aiMsg) {
      let event = 'message'
      const dataLines = []
      raw.split('\n').forEach(line => {
        if (line.startsWith('event:')) event = line.slice(6).trim()
        else if (line.startsWith('data:')) dataLines.push(line.slice(5).trim())
      })
      if (!dataLines.length) return
      let payload
      try { payload = JSON.parse(dataLines.join('\n')) } catch (_) { return }

      if (event === 'meta') {
        if (payload.context) this.lastContext = payload.context
        if (Array.isArray(payload.tips)) this.lastTips = payload.tips
        if (payload.fallback) aiMsg.fallback = true
      } else if (event === 'chunk') {
        if (payload.content) {
          aiMsg.content += payload.content
          this.$nextTick(this.scrollToBottom)
        }
      } else if (event === 'error') {
        const msg = payload.message || 'AI 服务暂时不可用，请稍后再试'
        aiMsg.error = msg
        this.error = msg
      } else if (event === 'done') {
        if (typeof payload.remaining === 'number') this.remaining = payload.remaining
        if (payload.dailyLimit) this.dailyLimit = payload.dailyLimit
      }
    },
    // 流式不可用时回退到同步接口（兼容旧后端/网络抖动）
    async fallbackSyncAdvice(text, history, aiMsg, originalErr) {
      try {
        const res = await postJson('/api/member/ai/advice', { question: text, history })
        const data = pickData(res) || {}
        aiMsg.content = data.answer || '暂时没有拿到建议，请换个问法试试。'
        aiMsg.fallback = !!data.fallback
        this.lastContext = data.context || null
        this.lastTips = Array.isArray(data.tips) ? data.tips : []
        if (typeof data.remaining === 'number') this.remaining = data.remaining
        if (data.dailyLimit) this.dailyLimit = data.dailyLimit
      } catch (syncErr) {
        const msg = (syncErr && syncErr.message) || (originalErr && originalErr.message) || 'AI 服务暂时不可用，请稍后再试'
        aiMsg.error = msg
        this.error = msg
      }
    },
    stopGeneration() {
      if (this.abortController) {
        try { this.abortController.abort() } catch (_) {}
      }
    },
    pushMessage(role, content, extra = {}) {
      const msg = {
        id: Date.now() + '-' + Math.random().toString(36).slice(2, 7),
        role,
        content,
        ...extra
      }
      this.messages.push(msg)
      this.$nextTick(this.scrollToBottom)
      return msg
    },
    // 把当前历史组装成后端可用的 messages；不包含刚刚 push 进去的本轮 user 提问。
    buildHistoryForRequest() {
      const usable = this.messages.slice(0, -1)
      const trimmed = usable.slice(-MAX_HISTORY_FOR_REQUEST)
      return trimmed.map(m => ({ role: m.role, content: m.content }))
    },
    clearConversation() {
      if (!window.confirm('确定清空当前对话？')) return
      this.messages = []
      this.lastContext = null
      this.lastTips = []
      this.error = ''
      try { localStorage.removeItem(this.storageKey) } catch (_) {}
    },
    persistHistory() {
      try {
        const trimmed = this.messages.slice(-MAX_STORED_TURNS * 2).map(m => ({
          role: m.role,
          content: m.content,
          error: m.error,
          fallback: m.fallback,
          cancelled: m.cancelled
        }))
        localStorage.setItem(this.storageKey, JSON.stringify(trimmed))
      } catch (_) {}
    },
    restoreHistory() {
      try {
        const raw = localStorage.getItem(this.storageKey)
        if (!raw) return
        const arr = JSON.parse(raw)
        if (Array.isArray(arr)) {
          this.messages = arr.map((m, i) => ({
            id: `restored-${i}-${Math.random().toString(36).slice(2, 6)}`,
            role: m.role === 'user' ? 'user' : 'assistant',
            content: String(m.content || ''),
            error: m.error,
            fallback: !!m.fallback,
            cancelled: !!m.cancelled,
            streaming: false
          }))
          this.$nextTick(this.scrollToBottom)
        }
      } catch (_) {}
    },
    scrollToBottom() {
      const el = this.$refs.historyRef
      if (el) el.scrollTop = el.scrollHeight
    },
    renderMarkdown(text) {
      if (!text) return ''
      // 本地知识库大量用单 ~ 表示范围（如 300~500、3~5 次、60%~70%），
      // marked 的 GFM strikethrough 会把单 ~ 当成删除线标记并吃掉字符。
      // 渲染前替换为视觉等价的全角波浪号 〜，既保持显示又不触发任何 markdown 语法。
      const safe = String(text).replace(/~/g, '〜')
      const html = marked.parse(safe)
      return DOMPurify.sanitize(html, { USE_PROFILES: { html: true } })
    }
  }
}
</script>

<style scoped>
.ai-advisor { padding: 18px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; display: flex; flex-direction: column; }
.ai-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; margin-bottom: 14px; }
.ai-title h2 { margin: 0; font-size: 20px; color: #111827; }
.ai-title p { margin: 6px 0 0; color: #6b7280; font-size: 13px; }
.head-right { display: flex; align-items: center; gap: 8px; }
.source-tag { padding: 4px 10px; border-radius: 999px; background: #eef2ff; color: #4338ca; font-size: 12px; font-weight: 700; white-space: nowrap; }
.quota-tag { padding: 4px 10px; border-radius: 999px; background: #f0fdf4; color: #15803d; font-size: 12px; font-weight: 700; white-space: nowrap; }
.clear-btn { height: 28px; padding: 0 10px; border: 1px solid #e5e7eb; border-radius: 6px; background: #fff; color: #6b7280; font-size: 12px; cursor: pointer; }
.clear-btn:hover { background: #fef2f2; color: #dc2626; border-color: #fecaca; }

.chips { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.chip { height: 32px; padding: 0 12px; border: 1px solid #dbeafe; border-radius: 999px; background: #f8fafc; color: #334155; cursor: pointer; font-size: 13px; }
.chip:hover:not(:disabled) { background: #eef2ff; color: #3730a3; }
.chip:disabled { opacity: .5; cursor: not-allowed; }

.history { max-height: 420px; min-height: 140px; overflow-y: auto; padding: 6px 2px; margin-bottom: 12px; background: #fafbfc; border-radius: 6px; border: 1px solid #f1f5f9; }
.empty-hint { padding: 28px 12px; text-align: center; color: #94a3b8; font-size: 13px; }
.msg { display: flex; margin: 8px 10px; }
.msg-user { justify-content: flex-end; }
.msg-ai { justify-content: flex-start; }
.bubble { max-width: 86%; padding: 10px 14px; border-radius: 12px; line-height: 1.65; font-size: 14px; word-break: break-word; }
.msg-user .bubble { background: #3f4652; color: #fff; border-bottom-right-radius: 4px; }
.msg-ai .bubble { background: #fff; color: #1f2937; border: 1px solid #e5e7eb; border-bottom-left-radius: 4px; }
.bubble-text { white-space: pre-wrap; }
.bubble-error { margin-top: 6px; padding-top: 6px; border-top: 1px dashed #fecaca; color: #dc2626; font-size: 12px; }
.typing { color: #6b7280; font-size: 13px; }

/* 流式输出与降级提示 */
.bubble-stream { white-space: pre-wrap; min-height: 1.4em; }
.cursor { color: #4338ca; font-weight: 700; animation: blink 1s steps(2, start) infinite; }
@keyframes blink { to { visibility: hidden; } }
.fallback-badge { margin-bottom: 6px; padding: 3px 8px; border-radius: 4px; background: #fffbeb; color: #b45309; border: 1px solid #fde68a; font-size: 12px; }
.cancelled-note { margin-top: 6px; color: #9ca3af; font-size: 12px; }
.stop-btn { background: #dc2626 !important; }

/* Markdown 渲染样式 */
.bubble-md :deep(h1),
.bubble-md :deep(h2),
.bubble-md :deep(h3) { margin: 8px 0 6px; font-size: 15px; font-weight: 700; color: #0f172a; }
.bubble-md :deep(p) { margin: 6px 0; }
.bubble-md :deep(ul),
.bubble-md :deep(ol) { margin: 6px 0; padding-left: 22px; }
.bubble-md :deep(li) { margin: 2px 0; }
.bubble-md :deep(strong) { color: #0f172a; font-weight: 700; }
.bubble-md :deep(em) { color: #4338ca; font-style: normal; font-weight: 700; }
.bubble-md :deep(code) { padding: 1px 5px; border-radius: 4px; background: #f1f5f9; font-size: 13px; color: #be123c; }
.bubble-md :deep(pre) { margin: 8px 0; padding: 10px; background: #f8fafc; border-radius: 6px; overflow-x: auto; }
.bubble-md :deep(pre code) { background: transparent; color: inherit; padding: 0; }
.bubble-md :deep(blockquote) { margin: 6px 0; padding: 4px 10px; border-left: 3px solid #cbd5e1; color: #64748b; }
.bubble-md :deep(hr) { margin: 10px 0; border: none; border-top: 1px dashed #e5e7eb; }

.input-row { display: grid; grid-template-columns: 1fr 92px; gap: 10px; align-items: stretch; }
.input-row textarea { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; resize: vertical; font-size: 14px; line-height: 1.5; font-family: inherit; }
.send-btn { border: none; border-radius: 6px; background: #3f4652; color: #fff; font-weight: 700; cursor: pointer; }
.send-btn:disabled { background: #9ca3af; cursor: not-allowed; }

.error { margin: 10px 0 0; color: #dc2626; font-size: 13px; }
.context-row { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px; }
.context-row span { padding: 3px 9px; border-radius: 999px; background: #f1f5f9; color: #475569; font-size: 12px; font-weight: 700; }
.tips { margin: 12px 0 0; padding-left: 18px; color: #6b7280; font-size: 12px; line-height: 1.8; }

@media (max-width: 640px) {
  .input-row { grid-template-columns: 1fr; }
  .send-btn { height: 42px; }
  .bubble { max-width: 92%; }
}
</style>

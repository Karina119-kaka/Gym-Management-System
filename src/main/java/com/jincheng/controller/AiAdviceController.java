package com.jincheng.controller;

import com.jincheng.entity.AiCallLog;
import com.jincheng.entity.Member;
import com.jincheng.mapper.AiCallLogMapper;
import com.jincheng.service.AiAdviceService;
import com.jincheng.service.AiCallRateLimiter;
import com.jincheng.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/member/ai")
public class AiAdviceController {

    private static final int MAX_QUESTION_LENGTH = 200;
    private static final int MAX_HISTORY_TURNS = 10;
    private static final int MAX_HISTORY_MESSAGE_LENGTH = 1000;

    @Autowired
    private AiAdviceService aiAdviceService;

    @Autowired
    private AiCallRateLimiter rateLimiter;

    @Autowired
    private AiCallLogMapper aiCallLogMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // 用于异步落库，避免阻塞响应
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ai-call-log-writer");
        t.setDaemon(true);
        return t;
    });

    @GetMapping("/info")
    public Result engineInfo() {
        Map<String, Object> info = aiAdviceService.engineInfo();
        return Result.success(info);
    }

    @PostMapping("/advice")
    public Result advice(@SessionAttribute(name = "member", required = false) Member member,
                         @RequestBody(required = false) Map<String, Object> body) {
        if (member == null) {
            return Result.error("未登录");
        }
        // 1) 限流
        String reject = rateLimiter.tryAcquire(member.getMemberId());
        if (reject != null) {
            return Result.error(reject);
        }
        String question = body == null ? "" : Objects.toString(body.get("question"), "");
        if (question.length() > MAX_QUESTION_LENGTH) {
            question = question.substring(0, MAX_QUESTION_LENGTH);
        }
        List<Map<String, String>> history = extractHistory(body == null ? null : body.get("history"));
        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;
        Map<String, Object> result = null;
        try {
            result = aiAdviceService.advise(member, question, history);
            // 把 dailyLimit 信息塞回 result 供前端展示"今日剩余"
            result.put("remaining", rateLimiter.remaining(member.getMemberId()));
            result.put("dailyLimit", rateLimiter.getDailyLimit());
        } catch (RuntimeException e) {
            success = false;
            errorMessage = e.getMessage();
            rateLimiter.release(member.getMemberId());
            throw e;
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            // 异步落库，不阻塞响应
            final Map<String, Object> finResult = result;
            final boolean finSuccess = success;
            final String finError = errorMessage;
            final String finQuestion = question;
            final int finHistoryTurns = history.size();
            final long finElapsed = elapsed;
            try {
                logExecutor.submit(() -> writeLog(member.getMemberId(), finQuestion, finHistoryTurns, finElapsed, finSuccess, finError, finResult));
            } catch (Exception ignore) { /* 日志失败不影响主流程 */ }
        }
        return Result.success(result);
    }

    /**
     * 流式输出端点：SSE 推送 token-by-token，前端可逐字渲染。
     * 调用方需先 POST 同步版本（已限流），这里只做转发，避免重复限流。
     * 实际：本端点独立限流，因为响应是流式，无法走 Result 包装。
     */
    @PostMapping(value = "/advice/stream", produces = "text/event-stream; charset=utf-8")
    public void adviceStream(@SessionAttribute(name = "member", required = false) Member member,
                             @RequestBody(required = false) Map<String, Object> body,
                             HttpServletResponse response) throws Exception {
        response.setContentType("text/event-stream; charset=utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        OutputStream out = response.getOutputStream();

        // 校验与限流
        if (member == null) {
            writeSse(out, "error", objectMapper.writeValueAsString(Map.of("message", "未登录")));
            writeSse(out, "done", "{}");
            return;
        }
        String reject = rateLimiter.tryAcquire(member.getMemberId());
        if (reject != null) {
            writeSse(out, "error", objectMapper.writeValueAsString(Map.of("message", reject)));
            writeSse(out, "done", "{\"remaining\":0}");
            return;
        }
        String question = body == null ? "" : Objects.toString(body.get("question"), "");
        if (question.length() > MAX_QUESTION_LENGTH) {
            question = question.substring(0, MAX_QUESTION_LENGTH);
        }
        List<Map<String, String>> history = extractHistory(body == null ? null : body.get("history"));

        long start = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;
        Map<String, Object> logMeta = new LinkedHashMap<>();
        try {
            final Map<String, Object> usageHolder = logMeta;
            aiAdviceService.adviseStream(member, question, history, new AiAdviceService.StreamHandler() {
                @Override
                public void onMeta(Map<String, Object> meta) throws Exception {
                    writeSse(out, "meta", objectMapper.writeValueAsString(meta));
                    out.flush();
                    // 流末尾 usage：累积用于落库
                    Object usage = meta.get("usage");
                    if (usage instanceof Map<?, ?> u) {
                        for (Map.Entry<?, ?> en : u.entrySet()) {
                            usageHolder.put(String.valueOf(en.getKey()), en.getValue());
                        }
                    }
                    if (Boolean.TRUE.equals(meta.get("fallback"))) {
                        usageHolder.put("source", "fallback");
                    }
                    // 确定性拦截（闲聊/打招呼）等场景带来源标记，仅在尚未标记时使用
                    Object topSource = meta.get("source");
                    if (topSource != null) {
                        usageHolder.putIfAbsent("source", String.valueOf(topSource));
                    }
                }

                @Override
                public void onChunk(String chunk) throws Exception {
                    writeSse(out, "chunk", "{\"content\":" + objectMapper.writeValueAsString(chunk) + "}");
                    out.flush();
                }
            });
            // 结束元信息
            String meta = "{\"remaining\":" + rateLimiter.remaining(member.getMemberId())
                    + ",\"dailyLimit\":" + rateLimiter.getDailyLimit() + "}";
            writeSse(out, "done", meta);
            out.flush();
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            rateLimiter.release(member.getMemberId());
            String msg = e.getMessage() == null ? "AI 服务异常" : e.getMessage();
            writeSse(out, "error", objectMapper.writeValueAsString(Map.of("message", msg)));
            writeSse(out, "done", "{}");
            out.flush();
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            final boolean finSuccess = success;
            final String finError = errorMessage;
            final String finQuestion = question;
            final int finHistoryTurns = history.size();
            final long finElapsed = elapsed;
            // 流式调用的来源标记（若 usage 已带来 source=llm 则不覆盖）
            logMeta.putIfAbsent("source", "llm-stream");
            logExecutor.submit(() -> writeLog(member.getMemberId(), finQuestion, finHistoryTurns, finElapsed, finSuccess, finError, logMeta));
        }
    }

    // ------------------------------------------------------------------
    // 工具方法
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> extractHistory(Object raw) {
        if (!(raw instanceof List<?> list) || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, String>> result = new ArrayList<>();
        int start = Math.max(0, list.size() - MAX_HISTORY_TURNS);
        for (int i = start; i < list.size(); i++) {
            Object item = list.get(i);
            if (!(item instanceof Map<?, ?> msg)) {
                continue;
            }
            Object roleObj = msg.get("role");
            Object contentObj = msg.get("content");
            if (roleObj == null || contentObj == null) {
                continue;
            }
            String role = String.valueOf(roleObj).toLowerCase();
            if (!"user".equals(role) && !"assistant".equals(role)) {
                continue;
            }
            String content = String.valueOf(contentObj);
            if (content.length() > MAX_HISTORY_MESSAGE_LENGTH) {
                content = content.substring(0, MAX_HISTORY_MESSAGE_LENGTH);
            }
            if (content.isBlank()) {
                continue;
            }
            Map<String, String> safe = new LinkedHashMap<>();
            safe.put("role", role);
            safe.put("content", content);
            result.add(safe);
        }
        return result;
    }

    private void writeLog(Integer memberId, String question, int historyTurns, long elapsedMs,
                          boolean success, String errorMessage, Map<String, Object> result) {
        try {
            AiCallLog log = new AiCallLog();
            log.setMemberId(memberId);
            log.setQuestion(truncate(question, 500));
            log.setQuestionSummary(truncate(question, 100));
            log.setAnswer(null);
            log.setElapsedMs((int) Math.min(elapsedMs, Integer.MAX_VALUE));
            log.setSuccess(success);
            log.setErrorMessage(errorMessage == null ? null : truncate(errorMessage, 500));
            log.setHistoryTurns(historyTurns);
            log.setCreateTime(new Date());
            if (result != null) {
                log.setSource(Objects.toString(result.get("source"), null));
                log.setModel(Objects.toString(result.get("model"), null));
                log.setPromptTokens(toInt(result.get("promptTokens")));
                log.setCompletionTokens(toInt(result.get("completionTokens")));
                log.setTotalTokens(toInt(result.get("totalTokens")));
                Object cost = result.get("costYuan");
                if (cost instanceof Number n) {
                    log.setCostYuan(n.doubleValue());
                }
                // LLM 失败回退本地时记录 fallback 标记，便于排查
                if (Boolean.TRUE.equals(result.get("fallback"))) {
                    log.setSource("fallback");
                }
            }
            aiCallLogMapper.insert(log);
        } catch (Exception ignore) {
            // 日志写入失败不影响主流程
        }
    }

    private Integer toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        if (v != null) {
            try { return Integer.parseInt(String.valueOf(v)); } catch (NumberFormatException ignore) { return null; }
        }
        return null;
    }

    private void writeSse(OutputStream out, String event, String data) throws Exception {
        out.write(("event: " + event + "\n").getBytes(StandardCharsets.UTF_8));
        out.write(("data: " + data + "\n\n").getBytes(StandardCharsets.UTF_8));
    }

    private String truncate(String text, int max) {
        if (text == null) return null;
        return text.length() > max ? text.substring(0, max) + "…" : text;
    }
}

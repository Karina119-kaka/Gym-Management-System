package com.jincheng.entity;

import java.util.Date;

/**
 * AI 调用日志，对应 ai_call_log 表。
 * 用于追踪每次 AI 问答的成本、Token、是否回退、错误信息等，便于成本监控与限流审计。
 */
public class AiCallLog {
    private Long logId;
    private Integer memberId;
    private String question;
    private String questionSummary;
    private String answer;
    private String source;        // llm / local / fallback
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Double costYuan;
    private Integer elapsedMs;
    private Boolean success;
    private String errorMessage;
    private Integer historyTurns;
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getQuestionSummary() { return questionSummary; }
    public void setQuestionSummary(String questionSummary) { this.questionSummary = questionSummary; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getPromptTokens() { return promptTokens; }
    public void setPromptTokens(Integer promptTokens) { this.promptTokens = promptTokens; }
    public Integer getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(Integer completionTokens) { this.completionTokens = completionTokens; }
    public Integer getTotalTokens() { return totalTokens; }
    public void setTotalTokens(Integer totalTokens) { this.totalTokens = totalTokens; }
    public Double getCostYuan() { return costYuan; }
    public void setCostYuan(Double costYuan) { this.costYuan = costYuan; }
    public Integer getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(Integer elapsedMs) { this.elapsedMs = elapsedMs; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Integer getHistoryTurns() { return historyTurns; }
    public void setHistoryTurns(Integer historyTurns) { this.historyTurns = historyTurns; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}

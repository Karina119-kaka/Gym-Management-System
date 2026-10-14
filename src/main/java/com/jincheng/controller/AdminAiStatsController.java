package com.jincheng.controller;

import com.jincheng.mapper.AiCallLogMapper;
import com.jincheng.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员端 AI 调用统计。
 * 提供今日/本月聚合、近 N 天趋势、最近调用明细与会员调用排行。
 */
@RestController
@RequestMapping("/api/admin/ai")
public class AdminAiStatsController {

    private static final int DEFAULT_DAYS = 7;
    private static final int MAX_DAYS = 31;
    private static final int DEFAULT_RECENT_LIMIT = 30;
    private static final int MAX_RECENT_LIMIT = 100;
    private static final int TOP_MEMBER_LIMIT = 10;

    @Autowired
    private AiCallLogMapper aiCallLogMapper;

    /** 今日 + 本月汇总卡片数据 */
    @GetMapping("/stats/summary")
    public Result summary() {
        Map<String, Object> row = aiCallLogMapper.selectSummary();
        Map<String, Object> data = new LinkedHashMap<>();
        if (row == null) {
            row = new HashMap<>();
        }
        data.put("todayCount", intVal(row.get("todayCount")));
        data.put("todaySuccess", intVal(row.get("todaySuccess")));
        data.put("todayFail", intVal(row.get("todayFail")));
        data.put("todayFallback", intVal(row.get("todayFallback")));
        data.put("todayCost", round2(row.get("todayCost")));
        data.put("todayTokens", intVal(row.get("todayTokens")));
        data.put("todayMembers", intVal(row.get("todayMembers")));
        data.put("todayAvgMs", intVal(row.get("todayAvgMs")));
        data.put("monthCount", intVal(row.get("monthCount")));
        data.put("monthFail", intVal(row.get("monthFail")));
        data.put("monthCost", round2(row.get("monthCost")));
        data.put("monthTokens", intVal(row.get("monthTokens")));
        int tc = intVal(row.get("todayCount"));
        data.put("todaySuccessRate", tc == 0 ? 100 : Math.round(intVal(row.get("todaySuccess")) * 1000.0 / tc) / 10.0);
        int mc = intVal(row.get("monthCount"));
        data.put("monthSuccessRate", mc == 0 ? 100 : Math.round((mc - intVal(row.get("monthFail"))) * 1000.0 / mc) / 10.0);
        return Result.success(data);
    }

    /** 近 N 天调用趋势（自动补齐无数据日期） */
    @GetMapping("/stats/daily")
    public Result daily(@RequestParam(required = false, defaultValue = "7") Integer days) {
        int n = days == null ? DEFAULT_DAYS : Math.max(1, Math.min(MAX_DAYS, days));
        List<Map<String, Object>> rows = aiCallLogMapper.selectDailySeries(n - 1);
        Map<String, Map<String, Object>> byDay = new HashMap<>();
        for (Map<String, Object> r : rows) {
            byDay.put(String.valueOf(r.get("day")), r);
        }
        List<Map<String, Object>> series = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = n - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.toString();
            Map<String, Object> r = byDay.get(key);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("day", key);
            point.put("label", d.getMonthValue() + "/" + d.getDayOfMonth());
            if (r == null) {
                point.put("count", 0);
                point.put("cost", 0.0);
                point.put("tokens", 0);
                point.put("failCount", 0);
            } else {
                point.put("count", intVal(r.get("count")));
                point.put("cost", round2(r.get("cost")));
                point.put("tokens", intVal(r.get("tokens")));
                point.put("failCount", intVal(r.get("failCount")));
            }
            series.add(point);
        }
        return Result.success(series);
    }

    /** 最近调用明细 */
    @GetMapping("/stats/recent")
    public Result recent(@RequestParam(required = false, defaultValue = "30") Integer limit) {
        int n = limit == null ? DEFAULT_RECENT_LIMIT : Math.max(1, Math.min(MAX_RECENT_LIMIT, limit));
        return Result.success(aiCallLogMapper.selectRecent(n));
    }

    /** 本月高频会员 TOP10 */
    @GetMapping("/stats/top-members")
    public Result topMembers() {
        return Result.success(aiCallLogMapper.selectTopMembers(TOP_MEMBER_LIMIT));
    }

    private int intVal(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try { return (int) Math.round(Double.parseDouble(String.valueOf(v))); } catch (NumberFormatException e) { return 0; }
    }

    private double round2(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return Math.round(n.doubleValue() * 100.0) / 100.0;
        try { return Math.round(Double.parseDouble(String.valueOf(v)) * 100.0) / 100.0; } catch (NumberFormatException e) { return 0.0; }
    }
}
